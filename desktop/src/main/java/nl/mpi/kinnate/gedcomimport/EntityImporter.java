package nl.mpi.kinnate.gedcomimport;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 *  Document   : EntityImporter
 *  Created on : May 30, 2011, 10:28:59 AM
 *  Author     : Peter Withers
 */
public class EntityImporter implements GenericImporter {

    protected JProgressBar progressBar = null;
    protected JTextArea importTextArea;
    protected int inputLineCount;
    protected String inputFileMd5Sum;
    protected boolean overwriteExisting;
    protected HashMap<String, ArrayList<UniqueIdentifier>> createdNodeIds;
    HashMap<String, EntityDocument> createdDocuments = new HashMap<String, EntityDocument>();
//    private MetadataBuilder metadataBuilder;

    public EntityImporter(JProgressBar progressBarLocal, JTextArea importTextAreaLocal, boolean overwriteExistingLocal) {
        overwriteExisting = overwriteExistingLocal;
        importTextArea = importTextAreaLocal;
        progressBar = progressBarLocal;
//        metadataBuilder = new MetadataBuilder();
    }

    public HashMap<String, ArrayList<UniqueIdentifier>> getCreatedNodeIds() {
        return createdNodeIds;
    }

    public void appendToTaskOutput(String lineOfText) {
        importTextArea.append(lineOfText + "\n");
        importTextArea.setCaretPosition(importTextArea.getText().length());
    }

    public void calculateFileNameAndFileLength(BufferedReader bufferedReader) {
        // count the lines in the file (for progress) and calculate the md5 sum (for unique file naming)
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            StringBuilder hexString = new StringBuilder();
            String strLine;
            inputLineCount = 0;
            while ((strLine = bufferedReader.readLine()) != null) {
                digest.update(strLine.getBytes());
                inputLineCount++;
            }
            byte[] md5sum = digest.digest();
            for (int byteCounter = 0; byteCounter < md5sum.length; ++byteCounter) {
                hexString.append(Integer.toHexString(0x0100 + (md5sum[byteCounter] & 0x00FF)).substring(1));
            }
            inputFileMd5Sum = hexString.toString();
        } catch (NoSuchAlgorithmException algorithmException) {
            new ArbilBugCatcher().logError(algorithmException);
        } catch (IOException iOException) {
            new ArbilBugCatcher().logError(iOException);
        }
    }

    protected File getDestinationDirectory() {
        File destinationDirectory = new File(ArbilSessionStorage.getSingleInstance().getCacheDirectory(), inputFileMd5Sum);
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdir();
        }
        return destinationDirectory;
    }

    protected EntityDocument getEntityDocument(File destinationDirectory, ArrayList<URI> createdNodes, String idString) throws ImportException {
        idString = cleanFileName(idString);
        EntityDocument currentEntity = createdDocuments.get(idString);
        if (currentEntity == null) {
            // create a new entity file
            currentEntity = new EntityDocument(destinationDirectory, idString);
            appendToTaskOutput("created: " + currentEntity.getFilePath());
            createdNodes.add(currentEntity.createDocument(overwriteExisting));
            createdDocuments.put(idString, currentEntity);
            String typeString = "Entity";
            if (createdNodeIds.get(typeString) == null) {
                ArrayList<UniqueIdentifier> idArray = new ArrayList<UniqueIdentifier>();
                idArray.add(currentEntity.getUniqueIdentifier());
                createdNodeIds.put(typeString, idArray);
            } else {
                createdNodeIds.get(typeString).add(currentEntity.getUniqueIdentifier());
            }
        }
        return currentEntity;
    }

    protected void saveAllDocuments() {
        appendToTaskOutput("Saving all documents");
        for (EntityDocument currentDocument : createdDocuments.values()) {
            // todo: add progress for this
            try {
                currentDocument.saveDocument();
            } catch (ImportException exception) {
                new ArbilBugCatcher().logError(exception);
                appendToTaskOutput("Error saving file: " + exception.getMessage());
            }
//                appendToTaskOutput(importTextArea, "saved: " + currentDocument.getFilePath());
        }
    }

    public String cleanFileName(String fileName) {
        // prevent bad file names being created from the gedcom internal name part
        return fileName.replaceAll("[^A-z0-9]", "_") + ".cmdi";
    }

    public URI[] importFile(File testFile) {
        try {
            calculateFileNameAndFileLength(new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(testFile)))));
            return importFile(new InputStreamReader(new DataInputStream(new FileInputStream(testFile))));
        } catch (FileNotFoundException exception) {
            // todo: handle this
            return null;
        }
    }

    public URI[] importFile(String testFileString) {
        calculateFileNameAndFileLength(new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(testFileString))));
        return importFile(new InputStreamReader(getClass().getResourceAsStream(testFileString)));
    }

    public URI[] importFile(InputStreamReader inputStreamReader) {
        throw new UnsupportedOperationException("Not supported");
    }

    public boolean canImport(String inputFileString) {
        throw new UnsupportedOperationException("Not supported");
    }
}
