/**
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.gedcomimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.kindocument.EntityDocument;
import nl.mpi.kinnate.kindocument.ImportTranslator;
import nl.mpi.kinnate.uniqueidentifiers.IdentifierException;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : EntityImporter Created on : May 30, 2011, 10:28:59 AM
 *
 * @ author Peter Withers
 */
public class EntityImporter implements GenericImporter {

    protected JProgressBar progressBar = null;
    protected URI inputFileUri = null; // used only for copying resource files
    protected JTextArea importTextArea;
    private int inputLineCount;
    private int currntLineCounter;
    protected String inputFileMd5Sum;
    protected boolean overwriteExisting;
    protected HashMap<String, HashSet<UniqueIdentifier>> createdNodeIds;
    HashMap<String, EntityDocument> createdDocuments = new HashMap<String, EntityDocument>();
//    private MetadataBuilder metadataBuilder;
    final private SessionStorage sessionStorage;

    public EntityImporter(JProgressBar progressBarLocal, JTextArea importTextAreaLocal, boolean overwriteExistingLocal, SessionStorage sessionStorage) {
        overwriteExisting = overwriteExistingLocal;
        importTextArea = importTextAreaLocal;
        progressBar = progressBarLocal;
//        metadataBuilder = new MetadataBuilder();
        createdNodeIds = new HashMap<String, HashSet<UniqueIdentifier>>();
        this.sessionStorage = sessionStorage;
    }

    public HashMap<String, HashSet<UniqueIdentifier>> getCreatedNodeIds() {
        return createdNodeIds;
    }

    public void appendToTaskOutput(String lineOfText) {
        importTextArea.append(lineOfText + "\n");
        importTextArea.setCaretPosition(importTextArea.getText().length());
    }

    public void calculateFileNameAndFileLength(InputStream inputStream) {
        // count the lines in the file (for progress) and calculate the md5 sum (for unique file naming)
        currntLineCounter = 0;
        byte newLineByte = "\n".getBytes()[0]; // to keep things simple we only use the one char where, this will not work on old macs which are the only ones to still use only \r, however new macs now use \n so this is a minumal issue.
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            byte[] buffer = new byte[1024];
            int numRead;
            do {
                numRead = inputStream.read(buffer);
                if (numRead > 0) {
                    for (byte currentByte : buffer) {
                        if (currentByte == newLineByte) {
                            inputLineCount++;
                        }
                    }
                    digest.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
            inputStream.close();

            StringBuilder hexString = new StringBuilder();
            byte[] md5sum = digest.digest();
            for (int byteCounter = 0; byteCounter < md5sum.length; ++byteCounter) {
                hexString.append(Integer.toHexString(0x0100 + (md5sum[byteCounter] & 0x00FF)).substring(1));
            }
            inputFileMd5Sum = hexString.toString();
        } catch (NoSuchAlgorithmException algorithmException) {
            BugCatcherManager.getBugCatcher().logError(algorithmException);
        } catch (IOException iOException) {
            BugCatcherManager.getBugCatcher().logError(iOException);
        }
    }

    protected void incrementLineProgress() {
        currntLineCounter++;
        final int currentProgressPercent = (int) ((double) currntLineCounter / (double) inputLineCount * 100);
        if (progressBar != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressBar.setValue(currentProgressPercent);
                }
            });
        }
    }

    protected void incrementSaveProgress(int documentCount, int savedCount) {
        final int currentProgressPercent = (int) ((double) savedCount / (double) documentCount * 100);
        if (progressBar != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressBar.setValue(currentProgressPercent);
                }
            });
        }
    }

    protected File getDestinationDirectory() {
        File destinationDirectory = new File(sessionStorage.getProjectWorkingDirectory(), inputFileMd5Sum);
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdir();
        }
        return destinationDirectory;
    }

    protected void deleteEntityDocument(EntityDocument entityDocument) throws ImportException {
        if (entityDocument.entityData.getAllRelations().length > 0) {
            throw new ImportException("Cannot delete entity that has still has relations.");
        }
        String entityKey = null;
        for (Map.Entry<String, EntityDocument> entry : createdDocuments.entrySet()) {
            if (entry.getValue().equals(entityDocument)) {
                entityKey = entry.getKey();
                break;
            }
        }
        if (entityKey != null) {
            createdDocuments.remove(entityKey);
            for (HashSet<UniqueIdentifier> identifierSet : createdNodeIds.values()) {
                identifierSet.remove(entityDocument.getUniqueIdentifier());
            }
            entityDocument.getFile().delete();
        }
    }

    protected EntityDocument getEntityDocument(HashSet<UniqueIdentifier> createdNodes, String typeString, String idString, ImportTranslator importTranslator) throws ImportException {
        EntityDocument currentEntity = createdDocuments.get(idString);
        UniqueIdentifier uniqueIdentifier;
        if (currentEntity == null) {
            try {
                String cleanedIdString = idString;
                if (idString.startsWith("@") && idString.endsWith("@")) {
                    cleanedIdString = idString.substring(1, idString.length() - 1);
                }
                // if an existing identifier is provided the keep using it
                uniqueIdentifier = new UniqueIdentifier(cleanedIdString);
            } catch (IdentifierException exception) {
                // otherwise create a new identifier
                String cleanedIdString = idString;
                if (idString.startsWith("@") && idString.endsWith("@")) {
                    // keep the old format so that the import results can be compared with version 1.0 import resutls
                    cleanedIdString = "_" + idString.substring(1, idString.length() - 1) + "_";
                }
                uniqueIdentifier = new UniqueIdentifier(inputFileMd5Sum + ":" + cleanedIdString, UniqueIdentifier.IdentifierType.iid);
            }
            // create a new entity file
            currentEntity = new EntityDocument(/* getDestinationDirectory(),*/uniqueIdentifier, typeString, importTranslator, sessionStorage);
//            appendToTaskOutput("created: " + currentEntity.getFilePath());
            createdNodes.add(currentEntity.getUniqueIdentifier());
            createdDocuments.put(idString, currentEntity);
        }
        if (typeString != null) {
            // make sure the entity is listed under its type for later in the UI
            if (createdNodeIds.get(typeString) == null) {
                HashSet<UniqueIdentifier> idSet = new HashSet<UniqueIdentifier>();
                idSet.add(currentEntity.getUniqueIdentifier());
                createdNodeIds.put(typeString, idSet);
            } else {
                createdNodeIds.get(typeString).add(currentEntity.getUniqueIdentifier());
            }
        }
        return currentEntity;
    }

    protected void saveAllDocuments() {
        appendToTaskOutput(createdDocuments.size() + " entities imported");
        appendToTaskOutput("Saving imported documents (step 2/4)");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setValue(0);
            }
        });
        int documentCount = createdDocuments.size();
        int savedCount = 0;
        for (EntityDocument currentDocument : createdDocuments.values()) {
            // todo: add progress for this
//            if (overwriteExisting || !currentDocument.getFile().exists()) { // at this point the file has already been overwritten so there is no point holding back on saving here
            try {
                currentDocument.saveDocument();
                savedCount++;
                incrementSaveProgress(documentCount, savedCount);
            } catch (ImportException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                appendToTaskOutput("Error saving file: " + exception.getMessage());
            }
//            }
//                appendToTaskOutput(importTextArea, "saved: " + currentDocument.getFilePath());
        }
    }

//    public String cleanFileName(String fileName) {
//        // prevent bad file names being created from the gedcom internal name part
//        return fileName.replaceAll("[^A-z0-9]", "_");
//    }
    public UniqueIdentifier[] importFile(File inputFile, String profileId) throws IOException, ImportException {
        inputFileUri = inputFile.toURI();
        calculateFileNameAndFileLength(new FileInputStream(inputFile));
        return importFile(new InputStreamReader(new FileInputStream(inputFile)), profileId);
    }

    public UniqueIdentifier[] importFile(String testFileString, String profileId) throws IOException, ImportException {
        try {
            inputFileUri = getClass().getResource(testFileString).toURI();
        } catch (URISyntaxException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            appendToTaskOutput("Error getting the import directory attached resources might not be correctly resolved");
        }
        calculateFileNameAndFileLength(getClass().getResourceAsStream(testFileString));
        return importFile(new InputStreamReader(getClass().getResourceAsStream(testFileString)), profileId);
    }

    public UniqueIdentifier[] importFile(InputStreamReader inputStreamReader, String profileId) throws IOException, ImportException {
        throw new UnsupportedOperationException("Not supported");
    }

    public boolean canImport(String inputFileString) {
        throw new UnsupportedOperationException("Not supported");
    }
}
