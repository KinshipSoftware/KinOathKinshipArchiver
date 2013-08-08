/**
 * Copyright (C) 2013 The Language Archive
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.kinhive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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

    public String addToWorkspace(String workspaceName, InputStream entityXmlStream) throws HiveException {
        File currentWorkspaceDir = getWorkspaceDir(workspaceName);
        if (!currentWorkspaceDir.exists()) {
            throw new HiveException("The workspace does not exist: " + workspaceName);
        }
        // todo: look if this is a an existing entity that needs a lock or if it needs a new pid so that it can be added
        // todo: remove the dummy id and use the id from the kmdi file, updating it from local to server and sending the change back to the local copy so that it can also be changed to a server type
        String kinHivePid = DummyPersistentIds.getPID();
        File targetFile = new File(currentWorkspaceDir, kinHivePid + ".kmdi");
        try {
            FileWriter targetFileWriter = new FileWriter(targetFile);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entityXmlStream, "UTF-8"));
            for (String responseLine = bufferedReader.readLine(); responseLine != null; responseLine = bufferedReader.readLine()) {
                targetFileWriter.append(responseLine);
                targetFileWriter.append("\n");
            }
            targetFileWriter.append("\n");
            targetFileWriter.close();
        } catch (IOException exception) {
            return exception.getMessage();
        }
        return kinHivePid;
    }

    public void migrateToHive(String workspaceName, String persistentID) {
    }
}
