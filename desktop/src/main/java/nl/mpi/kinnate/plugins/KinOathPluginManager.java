package nl.mpi.kinnate.plugins;

import java.awt.Component;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;
import nl.mpi.pluginloader.KinOathPlugin;
import nl.mpi.pluginloader.PluginManager;

/**
 * Document : KinOathPluginManager
 * Created on : Aug 13, 2012, 5:58:42 PM
 * Author : Peter Withers
 */
public class KinOathPluginManager implements PluginManager {

    final private AbstractDiagramManager diagramWindowManager;
    final private ArbilWindowManager dialogHandler;
    final private Component parentComponent;

    public KinOathPluginManager(AbstractDiagramManager diagramWindowManager, ArbilWindowManager dialogHandler, Component parentComponent) {
        this.diagramWindowManager = diagramWindowManager;
        this.dialogHandler = dialogHandler;
        this.parentComponent = parentComponent;
    }

    public void activatePlugin(KinOathPlugin kinOathPlugin) {
        dialogHandler.addMessageDialogToQueue("activate: \n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getVersionNumber() + "\n" + kinOathPlugin.getDescription(), "Enable Plugin");
    }

    public void deactivatePlugin(KinOathPlugin kinOathPlugin) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isActivated(KinOathPlugin kinOathPlugin) {
        return false;
    }
}
