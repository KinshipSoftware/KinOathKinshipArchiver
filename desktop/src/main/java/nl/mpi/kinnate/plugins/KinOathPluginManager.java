package nl.mpi.kinnate.plugins;

import java.awt.Component;
import java.util.HashSet;
import nl.mpi.arbil.plugin.KinOathPanelPlugin;
import nl.mpi.arbil.plugin.PluginException;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.plugin.BasePlugin;
import nl.mpi.kinnate.ui.KinDiagramPanel;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;
import nl.mpi.pluginloader.PluginManager;

/**
 * Document : KinOathPluginManager Created on : Aug 13, 2012, 5:58:42 PM Author
 * : Peter Withers
 */
public class KinOathPluginManager implements PluginManager {

    final private AbstractDiagramManager diagramWindowManager;
    final private ArbilWindowManager dialogHandler;
    final private Component parentComponent;
    final private HashSet<BasePlugin> hashSet = new HashSet<BasePlugin>();

    public KinOathPluginManager(AbstractDiagramManager diagramWindowManager, ArbilWindowManager dialogHandler, Component parentComponent) {
        this.diagramWindowManager = diagramWindowManager;
        this.dialogHandler = dialogHandler;
        this.parentComponent = parentComponent;
    }

    private void setPluginPanel(KinOathPanelPlugin kinOathPanelPlugin, boolean isVisible) throws PluginException {
        final SavePanel currentSavePanel = diagramWindowManager.getCurrentSavePanel(parentComponent);
        if (currentSavePanel instanceof KinDiagramPanel) {
            ((KinDiagramPanel) currentSavePanel).addPluginPanel(kinOathPanelPlugin, isVisible);
        }
    }

    public void activatePlugin(BasePlugin kinOathPlugin) {
        if (kinOathPlugin instanceof KinOathPanelPlugin) {
            try {
                setPluginPanel((KinOathPanelPlugin) kinOathPlugin, true);
                hashSet.add(kinOathPlugin);
            } catch (PluginException exception) {
                dialogHandler.addMessageDialogToQueue("Failed to activate the requested plugin.\n" + exception.getMessage() + "\n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription(), "Enable Plugin Error");
            }
        } else {
            dialogHandler.addMessageDialogToQueue("No method to activate this type of plugin yet.\n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription(), "Enable Plugin");
        }
    }

    public void deactivatePlugin(BasePlugin kinOathPlugin) {
        if (kinOathPlugin instanceof KinOathPanelPlugin) {
            try {
                setPluginPanel((KinOathPanelPlugin) kinOathPlugin, false);
                hashSet.remove(kinOathPlugin);
            } catch (PluginException exception) {
                dialogHandler.addMessageDialogToQueue("Failed to deactivate the requested plugin.\n" + exception.getMessage() + "\n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription(), "Enable Plugin Error");
            }
        } else {
            dialogHandler.addMessageDialogToQueue("No method to deactivate this type of plugin yet.\n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription(), "Enable Plugin");
        }
    }

    public boolean isActivated(BasePlugin kinOathPlugin) {
        return hashSet.contains(kinOathPlugin);
    }
}
