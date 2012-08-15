package nl.mpi.pluginloader;

import nl.mpi.kinnate.plugin.BasePlugin;

/**
 * Document : PluginManager
 * Created on : Aug 13, 2012, 5:19:59 PM
 * Author : Peter Withers
 */
public interface PluginManager {

    public boolean isActivated(BasePlugin kinOathPlugin);

    public void activatePlugin(BasePlugin kinOathPlugin);

    public void deactivatePlugin(BasePlugin kinOathPlugin);
}
