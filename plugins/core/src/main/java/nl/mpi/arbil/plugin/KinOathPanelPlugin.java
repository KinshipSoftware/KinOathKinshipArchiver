package nl.mpi.arbil.plugin;

import javax.swing.JScrollPane;

/**
 * Document : KinOathPanelPlugin Created on : Dec 20, 2011, 2:49:57 PM
 *
 * @author Peter Withers
 */
public interface KinOathPanelPlugin {

    public JScrollPane getUiPanel(PluginDialogHandler dialogHandler, PluginSessionStorage sessionStorage, PluginBugCatcher bugCatcher) throws PluginException;
}
