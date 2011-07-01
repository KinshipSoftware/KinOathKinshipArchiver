package nl.mpi.kinnate.export;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    private EntityCollection.SearchResults searchResults = null;
    private File[] modifiedFiles = null;
//    private boolean uploadComplete = false;
    URI workspaceUri = null;

    public boolean canUpload() {
        return (searchResults != null && searchResults.resultCount > 0) || (modifiedFiles != null && modifiedFiles.length > 0);
    }

    public boolean isUploadComplete() {
        return workspaceUri != null; //uploadComplete;
    }

    public URI getWorkspaceUri() {
        return workspaceUri;
    }

    public String getFoundMessage() {
        String messageString = "";
        if (searchResults != null) {
            messageString += "Found " + searchResults.resultCount + " new files to upload\n";
        }
        if (modifiedFiles != null) {
            messageString += "Found " + modifiedFiles.length + " new files to upload\n";
        }
        if (searchResults != null && modifiedFiles != null) {
            messageString += "No results found\n";
        }
        return messageString;
    }

    public String getSearchMessage() {
        return searchResults.statusMessage;
    }

    public void findLocalEntities(final ActionListener actionListener) {
        new Thread() {

            public void run() {
                // search the database
                EntityCollection entityCollection = new EntityCollection();
                searchResults = entityCollection.searchForLocalEntites();
                actionListener.actionPerformed(new ActionEvent(this, 0, "seachcomplete"));
            }
        }.start();
    }

    public void findModifiedEntities(final ActionListener actionListener) {
        new Thread() {

            public void run() {
                // search file system
                ModifiedFileSearch modifiedFileSearch = new ModifiedFileSearch();
                modifiedFileSearch.setSearchType(ModifiedFileSearch.SearchType.cmdi); // todo: change this to kmdi when implemented
                modifiedFiles = modifiedFileSearch.getModifiedFiles(ArbilSessionStorage.getSingleInstance().getCacheDirectory()).toArray(new File[]{});
                actionListener.actionPerformed(new ActionEvent(this, 0, "seachcomplete"));
            }
        }.start();
    }

    public void uploadLocalEntites(final ActionListener actionListener, final JProgressBar uploadProgress, final JTextArea outputArea, final String workspaceName, final char[] workspacePassword/*, final boolean createWorkspace*/) {
        new Thread() {

            public void run() {
                try {
                    URL serverRestUrl = new URL("http://localhost:8080/kinoath-rest/kinoath/kinspace/" + workspaceName); // todo: put this into a config file
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
                                convertLocalIdentifierToUnique(null, null); // todo: get the unique identifier from the server response
                            } catch (URISyntaxException exception) {
                                GuiHelper.linorgBugCatcher.logError(exception);
                                outputArea.append(exception.getMessage() + "\n");
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
                    workspaceUri = serverRestUrl.toURI();
                } catch (MalformedURLException exception) {
                    GuiHelper.linorgBugCatcher.logError(exception);
                    outputArea.append(exception.getMessage() + "\n");
                } catch (URISyntaxException exception) {
                    GuiHelper.linorgBugCatcher.logError(exception);
                    outputArea.append(exception.getMessage() + "\n");
                }
                uploadProgress.setValue(0);
                searchResults = null;
                modifiedFiles = null;
                for (int charCount = 0; charCount < workspacePassword.length; charCount++) {
                    // clear the password data so that it is not left hanging around in the virtual machine
                    workspacePassword[charCount] = 0;
                }
//                uploadComplete = true;
                actionListener.actionPerformed(new ActionEvent(this, 0, "seachcomplete"));
            }
        }.start();
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
            bufferedOutputStream.close();
            bufferedInputStream.close();
            // show the response code
            outputArea.append(httpURLConnection.getResponseCode() + "\n");
            outputArea.append(httpURLConnection.getResponseMessage() + "\n");
            // show the response page
            InputStream inputStream;
            inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            for (String responseLine = bufferedReader.readLine(); responseLine != null; responseLine = bufferedReader.readLine()) {
                outputArea.append(responseLine + "\n");
            }
            outputArea.append("\n");
            stripHistoryFiles(uploadFile);
        } catch (IOException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
            outputArea.append(exception.getMessage() + "\n");
        }

    }

    private void convertLocalIdentifierToUnique(String localIdentifier, String uniqueIdentifier){
        // todo: change in the target file
        // todo: update all relatives of the target file
    }

    private void stripHistoryFiles(File targetFile) {
        // todo: strip the history files, eg .x .0 .1 .2 etc. but keep the main file
    }
}
