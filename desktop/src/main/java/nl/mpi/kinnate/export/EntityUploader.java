package nl.mpi.kinnate.export;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
            HttpURLConnection httpCon = (HttpURLConnection) serverRestUrl.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("PUT");
//                    connection.setRequestProperty("Content-Type","text/xml");
            OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
            out.write("Resource content");
            out.close();
            outputArea.append(httpCon.getResponseMessage() + "\n");
            outputArea.append(httpCon.getResponseCode() + "\n");
        } catch (IOException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
            outputArea.append(exception.getMessage());
        }

    }
}
