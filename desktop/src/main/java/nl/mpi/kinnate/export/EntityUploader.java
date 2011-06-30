package nl.mpi.kinnate.export;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.kinnate.entityindexer.EntityCollection;

/**
 *  Document   : EntityUpload
 *  Created on : Jun 29, 2011, 3:00:33 PM
 *  Author     : Peter Withers
 */
public class EntityUploader {

    EntityCollection.SearchResults searchResults = null;
    File[] modifiedFiles = null;

    public boolean canUpload() {
        return (searchResults != null && searchResults.resultCount > 0) || (modifiedFiles != null && modifiedFiles.length > 0);
    }

    public String getSearchMessage() {
        return searchResults.statusMessage;
    }

    public int findLocalEntities(JProgressBar uploadProgress) {
        EntityCollection entityCollection = new EntityCollection();
        uploadProgress.setIndeterminate(true);
        searchResults = entityCollection.searchForLocalEntites();
        uploadProgress.setString(searchResults.statusMessage);
        uploadProgress.setIndeterminate(false);
        return searchResults.resultCount;
    }

    public int findModifiedEntities(JProgressBar uploadProgress) {
        // search file system
        ModifiedFileSearch modifiedFileSearch = new ModifiedFileSearch();
        modifiedFileSearch.setSearchType(ModifiedFileSearch.SearchType.cmdi); // todo: change this to kmdi when implemented
        modifiedFiles = modifiedFileSearch.getModifiedFiles(ArbilSessionStorage.getSingleInstance().getCacheDirectory()).toArray(new File[]{});
        return modifiedFiles.length;
    }

    public void uploadLocalEntites(JProgressBar uploadProgress, JTextArea outputArea, String workspaceName, char[] workspacePassword) {
        try {
            URL serverRestUrl = new URL("http://localhost:8080/kinoath-rest/"); // todo: put this into a config file
            uploadProgress.setIndeterminate(false);
            uploadProgress.setMinimum(0);
            int maxCount = 0;
            if (searchResults != null) {
                maxCount += searchResults.resultCount;
            }
            if (modifiedFiles != null) {
                maxCount += modifiedFiles.length;
            }
            uploadProgress.setMaximum(maxCount);
            uploadProgress.setValue(0);
            if (searchResults != null) {
                for (String resultLine : searchResults.resultsPathArray) {
                    try {
                        uploadFile(serverRestUrl, outputArea, new File(new URI(resultLine)));
                    } catch (URISyntaxException exception) {
                        GuiHelper.linorgBugCatcher.logError(exception);
                        outputArea.append(exception.getMessage());
                    }
                    uploadProgress.setValue(uploadProgress.getValue() + 1);
                }
            }
            if (modifiedFiles != null) {
                for (File uploadFile : modifiedFiles) {
                    uploadFile(serverRestUrl, outputArea, uploadFile);
                    uploadProgress.setValue(uploadProgress.getValue() + 1);
                }
            }
            outputArea.append("Done\n");
        } catch (MalformedURLException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
            outputArea.append(exception.getMessage());
        }
        uploadProgress.setValue(0);
        searchResults = null;
        for (int charCount = 0; charCount < workspacePassword.length; charCount++) {
            // clear the password data so that it is not left hanging around in the virtual machine
            workspacePassword[charCount] = 0;
        }
    }

    private void uploadFile(URL serverRestUrl, JTextArea outputArea, File uploadFile) {
        try {
            outputArea.append(uploadFile.toString() + "\n");
            HttpURLConnection httpURLConnection = (HttpURLConnection) serverRestUrl.openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("PUT");
            httpURLConnection.setRequestProperty("Content-Type", "text/xml");
            // upload the file
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(uploadFile));
            int bytesRead;
            while ((bytesRead = bufferedInputStream.read()) > 0) {
                bufferedOutputStream.write(bytesRead);
            }
            bufferedInputStream.close();
            // show the response code
            outputArea.append(httpURLConnection.getResponseCode() + "\n");
            outputArea.append(httpURLConnection.getResponseMessage() + "\n");
            // show the response page
            InputStream inputStream;
            inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            for (String responseLine = bufferedReader.readLine(); responseLine != null; responseLine = bufferedReader.readLine()) {
                outputArea.append(responseLine);
            }
            outputArea.append("\n");
            stripHistoryFiles(uploadFile);
        } catch (IOException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
            outputArea.append(exception.getMessage());
        }

    }

    private void stripHistoryFiles(File targetFile) {
        // todo: strip the history files, eg .x .0 .1 .2 etc. but keep the main file
    }
}
