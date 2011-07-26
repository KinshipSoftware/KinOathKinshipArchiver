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
    protected int inputLineCount;
    protected String inputFileMd5Sum;
    protected boolean overwriteExisting;
    protected HashMap<String, ArrayList<UniqueIdentifier>> createdNodeIds;
//    private MetadataBuilder metadataBuilder;

    public EntityImporter(boolean overwriteExistingLocal) {
        overwriteExisting = overwriteExistingLocal;
//        metadataBuilder = new MetadataBuilder();
    }

    public HashMap<String, ArrayList<UniqueIdentifier>> getCreatedNodeIds() {
        return createdNodeIds;
    }

    public void appendToTaskOutput(JTextArea importTextArea, String lineOfText) {
        importTextArea.append(lineOfText + "\n");
        importTextArea.setCaretPosition(importTextArea.getText().length());
    }

    public void setProgressBar(JProgressBar progressBarLocal) {
        progressBar = progressBarLocal;
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

    public String cleanFileName(String fileName) {
        // prevent bad file names being created from the gedcom internal name part
        return fileName.replaceAll("[^A-z0-9]", "_") + ".cmdi";
    }

    public URI[] importFile(JTextArea importTextArea, File testFile) {
        try {
            calculateFileNameAndFileLength(new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(testFile)))));
            return importFile(importTextArea, new InputStreamReader(new DataInputStream(new FileInputStream(testFile))));
        } catch (FileNotFoundException exception) {
            // todo: handle this
            return null;
        }
    }

    public URI[] importFile(JTextArea importTextArea, String testFileString) {
        calculateFileNameAndFileLength(new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(testFileString))));
        return importFile(importTextArea, new InputStreamReader(getClass().getResourceAsStream(testFileString)));
    }

    public URI[] importFile(JTextArea importTextArea, InputStreamReader inputStreamReader) {
        throw new UnsupportedOperationException("Not supported");
    }

    public boolean canImport(String inputFileString) {
        throw new UnsupportedOperationException("Not supported");
    }
}
