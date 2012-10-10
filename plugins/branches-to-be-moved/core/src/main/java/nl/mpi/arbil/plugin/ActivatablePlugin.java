package nl.mpi.arbil.plugin;

/**
 * Document : ActivatablePlugin <br> Created on Aug 20, 2012, 11:45:21 AM <br>
 *
 * @author Peter Withers <br>
 */
public interface ActivatablePlugin {

    public void activatePlugin(PluginDialogHandler dialogHandler, PluginSessionStorage sessionStorage) throws PluginException;

    public void deactivatePlugin(PluginDialogHandler dialogHandler, PluginSessionStorage sessionStorage) throws PluginException;

    public boolean getIsActivated() throws PluginException;
}
