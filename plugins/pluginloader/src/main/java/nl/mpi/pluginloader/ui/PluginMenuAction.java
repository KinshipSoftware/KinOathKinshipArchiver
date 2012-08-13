package nl.mpi.pluginloader.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import nl.mpi.pluginloader.KinOathPlugin;
import nl.mpi.pluginloader.PluginManager;

/**
 * Document : PluginMenuAction
 * Created on : Aug 13, 2012, 4:20:17 PM
 * Author : Peter Withers
 */
public class PluginMenuAction extends AbstractAction {

    final private PluginManager pluginManager;
    final private KinOathPlugin kinOathPlugin;

    public PluginMenuAction(PluginManager pluginManager, KinOathPlugin kinOathPlugin) {
        super(kinOathPlugin.getName());
        this.pluginManager = pluginManager;
        this.kinOathPlugin = kinOathPlugin;

    }

    public void actionPerformed(ActionEvent e) {
        if (pluginManager.isActivated(kinOathPlugin)) {
            pluginManager.deactivatePlugin(kinOathPlugin);
            ((JCheckBoxMenuItem) e.getSource()).setSelected(false);
        } else {
            pluginManager.activatePlugin(kinOathPlugin);
            ((JCheckBoxMenuItem) e.getSource()).setSelected(true);
        }
        System.out.println("kinOathPlugin: " + kinOathPlugin.getName());
        System.out.println("kinOathPluginDescription: " + kinOathPlugin.getDescription());
        System.out.println("kinOathPluginNumber: " + kinOathPlugin.getVersionNumber());
    }
}
