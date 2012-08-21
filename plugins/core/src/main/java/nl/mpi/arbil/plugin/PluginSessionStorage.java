package nl.mpi.arbil.plugin;

import java.io.File;

/**
 * Document : PluginSessionStorage <br> Created on Aug 15, 2012, 2:10:56 PM <br>
 *
 * @author Peter Withers <br>
 */
public interface PluginSessionStorage {

    /**
     * @return Application storage directory used to store all application
     * settings
     */
    public File getApplicationSettingsDirectory();

    /**
     * @return Current project directory used to store project configuration
     * files and project directories
     */
    public File getProjectDirectory();

    /**
     * @return Current project working directory used to store all users working
     * files
     */
    public File getProjectWorkingDirectory();
}
