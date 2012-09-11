package nl.mpi.kinnate.plugins.metadatasearch;

import javax.swing.JPanel;
import nl.mpi.arbil.plugin.ArbilWindowPlugin;
import nl.mpi.arbil.plugin.PluginArbilDataNodeLoader;
import nl.mpi.arbil.plugin.PluginBugCatcher;
import nl.mpi.arbil.plugin.PluginDialogHandler;
import nl.mpi.arbil.plugin.PluginException;
import nl.mpi.arbil.plugin.PluginSessionStorage;
import nl.mpi.kinnate.plugin.BasePlugin;
import nl.mpi.kinnate.plugins.metadatasearch.ui.FacetedTreePanel;

/**
 * Document : FacetedPlugin <br> Created on Sep 10, 2012, 5:13:47 PM <br>
 *
 * @author Peter Withers <br>
 */
public class FacetedPlugin implements BasePlugin, ArbilWindowPlugin {

    public String getName() {
        return "Faceted Tree Plugin";
    }

    public int getMajorVersionNumber() {
        return 0;
    }

    public int getMinorVersionNumber() {
        return 0;
    }

    public int getBuildVersionNumber() {
        return 0;
    }

    public String getDescription() {
        return "A plugin for Arbil that provides a faceted tree via a XML DB.";
    }

    public JPanel getUiPanel(PluginDialogHandler dialogHandler, PluginSessionStorage sessionStorage, PluginBugCatcher bugCatcher, PluginArbilDataNodeLoader arbilDataNodeLoader) throws PluginException {
        final FacetedTreePanel facetedTreePanel = new FacetedTreePanel(arbilDataNodeLoader, dialogHandler);
        // trigger the facets to load
//        new Thread(facetedTreePanel.getRunnable("add")).start();
        new Thread(facetedTreePanel.getRunnable("options")).start();
        return facetedTreePanel;
    }
}
