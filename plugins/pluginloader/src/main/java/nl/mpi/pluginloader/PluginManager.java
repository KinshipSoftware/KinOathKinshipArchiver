package nl.mpi.pluginloader;

/**
 * Document : PluginManager
 * Created on : Aug 13, 2012, 5:19:59 PM
 * Author : Peter Withers
 */
public interface PluginManager {

    public void activatePlugin(KinOathPlugin kinOathPlugin);

    public void deactivatePlugin(KinOathPlugin kinOathPlugin);
}
