package nl.mpi.arbil.plugin;

import javax.swing.JScrollPane;
import nl.mpi.arbil.plugin.PluginDialogHandler;
import nl.mpi.arbil.plugin.PluginSessionStorage;

/**
 * Document : KinOathPanelPlugin Created on : Dec 20, 2011, 2:49:57 PM
 *
 * @author Peter Withers
 */
public interface KinOathPanelPlugin {

    public JScrollPane getUiPanel(PluginDialogHandler dialogHandler, PluginSessionStorage sessionStorage);
}
