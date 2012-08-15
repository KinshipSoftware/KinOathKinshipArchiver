package nl.mpi.pluginloader;

import nl.mpi.kinnate.plugin.BasePlugin;

/**
 * Document : PluginSample Created on : Dec 22, 2011, 3:58:34 PM
 *
 * @author Peter Withers
 */
public class PluginSample implements BasePlugin, PluginSettings {

    public String getName() {
        return "Sample Plugin Name";
    }

    public String getDescription() {
        return "Sample Plugin Description String";
    }

    public int getBuildVersionNumber() {
        return 3;
    }

    public int getMajorVersionNumber() {
        return 1;
    }

    public int getMinorVersionNumber() {
        return 2;
    }
}
