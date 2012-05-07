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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.kindocument.EntityDocument;
import nl.mpi.kinnate.kindocument.ImportTranslator;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : EntityImporter
 * Created on : May 30, 2011, 10:28:59 AM
 * Author : Peter Withers
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
    private SessionStorage sessionStorage;

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
        int currentProgressPercent = (int) ((double) currntLineCounter / (double) inputLineCount * 100);
        if (progressBar != null) {
            progressBar.setValue(currentProgressPercent);
        }
    }

    protected void incrementSaveProgress(int documentCount, int savedCount) {
        int currentProgressPercent = (int) ((double) savedCount / (double) documentCount * 100);
        if (progressBar != null) {
            progressBar.setValue(currentProgressPercent);
        }
    }

    protected File getDestinationDirectory() {
        File destinationDirectory = new File(sessionStorage.getCacheDirectory(), inputFileMd5Sum);
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdir();
        }
        return destinationDirectory;
    }

    protected EntityDocument getEntityDocument(ArrayList<URI> createdNodes, String typeString, String idString, ImportTranslator importTranslator) throws ImportException {
        idString = cleanFileName(idString);
        EntityDocument currentEntity = createdDocuments.get(idString);
        if (currentEntity == null) {
            UniqueIdentifier uniqueIdentifier = new UniqueIdentifier(inputFileMd5Sum + ":" + idString, UniqueIdentifier.IdentifierType.iid);
            // create a new entity file
            currentEntity = new EntityDocument(getDestinationDirectory(), uniqueIdentifier, typeString, importTranslator, sessionStorage);
//            appendToTaskOutput("created: " + currentEntity.getFilePath());
            createdNodes.add(currentEntity.getFile().toURI());
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
        progressBar.setValue(0);
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

    public String cleanFileName(String fileName) {
        // prevent bad file names being created from the gedcom internal name part
        return fileName.replaceAll("[^A-z0-9]", "_");
    }

    public URI[] importFile(File inputFile, String profileId) throws IOException, ImportException {
        inputFileUri = inputFile.toURI();
        calculateFileNameAndFileLength(new FileInputStream(inputFile));
        return importFile(new InputStreamReader(new FileInputStream(inputFile)), profileId);
    }

    public URI[] importFile(String testFileString, String profileId) throws IOException, ImportException {
        try {
            inputFileUri = getClass().getResource(testFileString).toURI();
        } catch (URISyntaxException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            appendToTaskOutput("Error getting the import directory attached resources might not be correctly resolved");
        }
        calculateFileNameAndFileLength(getClass().getResourceAsStream(testFileString));
        return importFile(new InputStreamReader(getClass().getResourceAsStream(testFileString)), profileId);
    }

    public URI[] importFile(InputStreamReader inputStreamReader, String profileId) throws IOException, ImportException {
        throw new UnsupportedOperationException("Not supported");
    }

    public boolean canImport(String inputFileString) {
        throw new UnsupportedOperationException("Not supported");
    }
}
