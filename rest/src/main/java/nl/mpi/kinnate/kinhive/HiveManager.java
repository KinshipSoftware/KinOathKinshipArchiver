package nl.mpi.kinnate.kinhive;

import java.io.File;
import java.io.FilenameFilter;

/**
 *  Document   : HiveManager
 *  Created on : Jul 1, 2011, 11:00:36 AM
 *  Author     : Peter Withers
 */
public class HiveManager {

    public File getKinArkDir() {
        // todo: put these in a config file
        return new File("/Users/petwit/Desktop/kinhive/kinark");
    }

    public File getKinSpaceDir() {
        // todo: put these in a config file
        return new File("/Users/petwit/Desktop/kinhive/kinspace");
    }

    private File getWorkspaceDir(String workspaceName) {
        File workspaceDir = new File(getKinSpaceDir(), workspaceName);
        return workspaceDir;
    }

    public String[] listWorkspaces() {
        return getKinSpaceDir().list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                // remove .. and . and any hidden files etc.
                return name.matches("[a-zA-Z0-9]+");
            }
        });
    }

    public String[] listWorkspaceFiles(String workspaceName) {
        return getWorkspaceDir(workspaceName).list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                // remove .. and . and any hidden files etc.
                return name.matches("[^\\.].*");
            }
        });
    }

    public void createWorkspace(String workspaceName) throws HiveException {
        File workspaceDir = getWorkspaceDir(workspaceName);
        if (workspaceDir.exists()) {
            throw new HiveException("The workspace already exists");
        }
        if (!workspaceDir.mkdir()) {
            throw new HiveException("Could not make the workspace");
        }
    }

    public String addToWorkspace(String workspaceName, String fileContents) {
        return DummyPersistentIds.getPID();
    }

    public void migrateToHive(String workspaceName, String persistentID) {
    }
}
