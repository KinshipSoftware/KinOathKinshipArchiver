package nl.mpi.pluginloader;

import nl.mpi.arbil.plugin.ActivatablePlugin;
import nl.mpi.arbil.plugin.PluginDialogHandler;
import nl.mpi.arbil.plugin.PluginException;
import nl.mpi.arbil.plugin.PluginSessionStorage;
import nl.mpi.kinnate.plugin.BasePlugin;

/**
 * Document : PluginSample Created on : Dec 22, 2011, 3:58:34 PM
 *
 * @author Peter Withers
 */
public class PluginSampleActivate implements BasePlugin, PluginSettings, ActivatablePlugin {

    boolean isActivated = false;

    public String getName() {
        return "Sample Activate Plugin Name";
    }

    public String getDescription() {
        return "Sample Activate Plugin Description String";
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

    public void activatePlugin(PluginDialogHandler dialogHandler, PluginSessionStorage sessionStorage) throws PluginException {
        isActivated = true;
    }

    public void deactivatePlugin(PluginDialogHandler dialogHandler, PluginSessionStorage sessionStorage) throws PluginException {
        isActivated = false;
    }

    public boolean getIsActivated() throws PluginException {
        return isActivated;
    }
}
