package nl.mpi.pluginloader;

import javax.swing.table.TableModel;

/**
 *  Document   : PluginSettings
 *  Created on : Dec 30, 2011, 3:04:27 PM
 *  Author     : Peter Withers
 */
public interface PluginSettings extends TableModel {

    public int versionMajor = 0;
    public int versionMinor = 0;
    public int versionRevision = 1;
}
