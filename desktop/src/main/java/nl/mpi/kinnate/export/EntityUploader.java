package nl.mpi.kinnate.export;

import javax.swing.JProgressBar;
import nl.mpi.kinnate.entityindexer.EntityCollection;

/**
 *  Document   : EntityUpload
 *  Created on : Jun 29, 2011, 3:00:33 PM
 *  Author     : Peter Withers
 */
public class EntityUploader {

    EntityCollection.SearchResults searchResults = null;

    public boolean canUpload() {
        return (searchResults != null && searchResults.resultCount > 0);
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

    public void uploadLocalEntites(JProgressBar uploadProgress, String workspaceName, char[] workspacePassword) {
        uploadProgress.setIndeterminate(false);
        uploadProgress.setMinimum(0);
        uploadProgress.setMaximum(searchResults.resultCount);
        uploadProgress.setValue(0);
        for (String resultLine : searchResults.resultsPathArray) {
            uploadProgress.setValue(uploadProgress.getValue() + 1);
        }
        uploadProgress.setValue(0);
        searchResults = null;
        for (int charCount = 0; charCount < workspacePassword.length; charCount++) {
            // clear the password data so that it is not left hanging around in the virtual machine
            workspacePassword[charCount] = 0;
        }
    }
}
