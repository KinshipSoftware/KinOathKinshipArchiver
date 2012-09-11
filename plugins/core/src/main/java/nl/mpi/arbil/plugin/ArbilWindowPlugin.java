package nl.mpi.arbil.plugin;

import javax.swing.JPanel;

/**
 * Document : ArbilWindowPlugin <br> Created on Sep 10, 2012, 5:38:42 PM <br>
 *
 * @author Peter Withers <br>
 */
public interface ArbilWindowPlugin {

    public JPanel getUiPanel(PluginDialogHandler dialogHandler, PluginSessionStorage sessionStorage, PluginBugCatcher bugCatcher, PluginArbilDataNodeLoader arbilDataNodeLoader) throws PluginException;
}
