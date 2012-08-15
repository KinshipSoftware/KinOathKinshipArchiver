package nl.mpi.arbil.plugin;

import java.io.File;

/**
 * Document : PluginSessionStorage <br> Created on Aug 15, 2012, 2:10:56 PM <br>
 *
 * @author Peter Withers <br>
 */
public interface PluginSessionStorage {

    /**
     * @return Current storage directory used to store all user working files
     */
    public File getStorageDirectory();
}
