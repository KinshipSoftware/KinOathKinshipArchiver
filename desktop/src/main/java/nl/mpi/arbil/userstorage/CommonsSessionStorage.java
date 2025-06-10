/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbil.userstorage;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import nl.mpi.flap.plugin.PluginDialogHandler;
import nl.mpi.flap.plugin.PluginSessionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on : Nov 8, 2012, 4:25:54 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public abstract class CommonsSessionStorage implements PluginSessionStorage {

    private final static Logger logger = LoggerFactory.getLogger(CommonsSessionStorage.class);
    protected File localCacheDirectory = null;
    private File storageDirectory = null;
    protected PluginDialogHandler messageDialogHandler;

    protected abstract String[] getAppDirectoryAlternatives();

    protected abstract String getProjectDirectoryName();

    protected abstract void logError(Exception exception);

    protected abstract void logError(String message, Exception exception);

    public abstract Object loadObject(String filename) throws Exception;

    private void checkForMultipleStorageDirectories(String[] locationOptions) {
        // look for any additional storage directories
        int foundDirectoryCount = 0;
        StringBuilder storageDirectoryMessageString = new StringBuilder();
        for (String currentStorageDirectory : locationOptions) {
            File storageFile = new File(currentStorageDirectory);
            if (storageFile.exists()) {
                foundDirectoryCount++;
                storageDirectoryMessageString.append(currentStorageDirectory).append("\n");
            }
        }
        if (foundDirectoryCount > 1) {
            String errorMessage = "More than one storage directory has been found.\nIt is recommended to remove any unused directories in this list.\nNote that the first occurrence is currently in use:\n" + storageDirectoryMessageString;
            logError(new Exception(errorMessage));
            try {
                if (!GraphicsEnvironment.isHeadless()) {
                    JOptionPane.showMessageDialog(null,
                            "More than one storage directory has been found.\nIt is recommended to remove any unused directories in this list.\nNote that the first occurrence is currently in use:\n" + storageDirectoryMessageString, "Multiple storage directories",
                            JOptionPane.WARNING_MESSAGE);
                }
            } catch (HeadlessException hEx) {
                // Should never occur since we're checking whether headless
                throw new AssertionError(hEx);
            }
        }
    }

    protected String[] getLocationOptions() {
        List<String> locationOptions = new ArrayList<String>();
        for (String appDir : getAppDirectoryAlternatives()) {
            locationOptions.add(System.getProperty("user.home") + File.separatorChar + "Local Settings" + File.separatorChar + "Application Data" + File.separatorChar + appDir + File.separatorChar);
            locationOptions.add(System.getenv("APPDATA") + File.separatorChar + appDir + File.separatorChar);
            locationOptions.add(System.getProperty("user.home") + File.separatorChar + appDir + File.separatorChar);
            locationOptions.add(System.getenv("USERPROFILE") + File.separatorChar + appDir + File.separatorChar);
            locationOptions.add(System.getProperty("user.dir") + File.separatorChar + appDir + File.separatorChar);
        }

        List<String> uniqueArray = new ArrayList<String>();
        for (String location : locationOptions) {
            if (location != null
                    && !location.startsWith("null")
                    && !uniqueArray.contains(location)) {
                uniqueArray.add(location);
            }
        }
        locationOptions = uniqueArray;

        for (String currentLocationOption : locationOptions) {
            logger.debug("LocationOption: " + currentLocationOption);
        }
        return locationOptions.toArray(new String[]{});
    }

    private File determineStorageDirectory() throws RuntimeException {
        File storageDirectoryFile = null;
        String storageDirectoryArray[] = getLocationOptions();

        // look for an existing storage directory
        for (String currentStorageDirectory : storageDirectoryArray) {
            File storageFile = new File(currentStorageDirectory);
            if (storageFile.exists()) {
                logger.debug("existing storage directory found: " + currentStorageDirectory);
                storageDirectoryFile = storageFile;
                break;
            }
        }

        String testedStorageDirectories = "";
        if (storageDirectoryFile == null) {
            for (String currentStorageDirectory : storageDirectoryArray) {
                if (!currentStorageDirectory.startsWith("null")) {
                    File storageFile = new File(currentStorageDirectory);
                    if (!storageFile.exists()) {
                        if (!storageFile.mkdir()) {
                            testedStorageDirectories = testedStorageDirectories + currentStorageDirectory + "\n";
                            logError("failed to create: " + currentStorageDirectory, null);
                        } else {
                            logger.debug("created new storage directory: " + currentStorageDirectory);
                            storageDirectoryFile = storageFile;
                            break;
                        }
                    }
                }
            }
        }
        if (storageDirectoryFile == null) {
            logError("Could not create a working directory in any of the potential location:\n" + testedStorageDirectories + "Please check that you have write permissions in at least one of these locations.\nThe application will now exit.", null);
            System.exit(-1);
        } else {
            try {
                File testFile = File.createTempFile("testfile", ".tmp", storageDirectoryFile);
                boolean success = testFile.exists();
                if (!success) {
                    success = testFile.createNewFile();
                }
                if (success) {
                    testFile.deleteOnExit();
                    success = testFile.exists();
                    if (success) {
                        success = testFile.delete();
                    }
                }
                if (!success) {
                    // test the storage directory is writable and add a warning message box here if not
                    logError("Could not write to the working directory.\nThere will be issues creating, editing and saving any file.", null);
                }
            } catch (IOException exception) {
                logger.debug("Sending exception to logger", exception);
                logError(exception);
                messageDialogHandler.addMessageDialogToQueue("Could not create a test file in the working directory.", "Arbil Critical Error");
                throw new RuntimeException("Exception while testing working directory writability", exception);
            }
        }
        logger.debug("storageDirectory: " + storageDirectoryFile);
        checkForMultipleStorageDirectories(storageDirectoryArray);

        return storageDirectoryFile;
    }

    /**
     * @return the storageDirectory
     */
    public synchronized File getApplicationSettingsDirectory() {
        if (storageDirectory == null) {
            storageDirectory = determineStorageDirectory();
        }
        return storageDirectory;
    }

    /**
     * @return the project directory
     */
    public File getProjectDirectory() {
        return getProjectWorkingDirectory().getParentFile();
    }

    /**
     * Tests that the project directory exists and creates it if it does not.
     *
     * @return the project working files directory
     */
    public File getProjectWorkingDirectory() {
        if (localCacheDirectory == null) {
            // load from the text based properties file
            String localCacheDirectoryPathString = loadString("cacheDirectory");
            if (localCacheDirectoryPathString != null) {
                localCacheDirectory = new File(localCacheDirectoryPathString);
            } else {
                // otherwise load from the to be replaced binary based storage file
                try {
                    File localWorkingDirectory = (File) loadObject("cacheDirectory");
                    localCacheDirectory = localWorkingDirectory;
                } catch (Exception exception) {
                    if (new File(getApplicationSettingsDirectory(), "imdicache").exists()) {
                        localCacheDirectory = new File(getApplicationSettingsDirectory(), "imdicache");
                    } else {
                        localCacheDirectory = new File(getApplicationSettingsDirectory(), getProjectDirectoryName());
                    }
                }
                saveString("cacheDirectory", localCacheDirectory.getAbsolutePath());
            }
            boolean cacheDirExists = localCacheDirectory.exists();
            if (!cacheDirExists) {
                if (!localCacheDirectory.mkdirs()) {
                    logError("Could not create cache directory", null);
                    return null;
                }
            }
        }
        return localCacheDirectory;
    }
}
