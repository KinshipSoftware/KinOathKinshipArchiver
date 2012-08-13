package nl.mpi.pluginloader;

/**
 *  Document   : PluginEntity
 *  Created on : Dec 20, 2011, 3:03:29 PM
 *  Author     : Peter Withers
 */
public interface PluginEntity {

    public int versionMajor = 0;
    public int versionMinor = 0;
    public int versionRevision = 1;
    // todo: do we want to expose EntityData or offer a subset here?
}
