package nl.mpi.pluginloader.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import nl.mpi.kinnate.plugin.BasePlugin;
import nl.mpi.pluginloader.PluginManager;

/**
 * Document : PluginMenuAction
 * Created on : Aug 13, 2012, 4:20:17 PM
 * Author : Peter Withers
 */
public class PluginMenuAction extends AbstractAction {

    final private PluginManager pluginManager;
    final private BasePlugin kinOathPlugin;

    public PluginMenuAction(PluginManager pluginManager, BasePlugin kinOathPlugin) {
        super(kinOathPlugin.getName());
        this.pluginManager = pluginManager;
        this.kinOathPlugin = kinOathPlugin;

    }

    public void actionPerformed(ActionEvent e) {
        if (pluginManager.isActivated(kinOathPlugin)) {
            pluginManager.deactivatePlugin(kinOathPlugin);
        } else {
            pluginManager.activatePlugin(kinOathPlugin);
        }
        // we check if the plugin was actualy enabled and set the menu accordingly
        ((JCheckBoxMenuItem) e.getSource()).setSelected(pluginManager.isActivated(kinOathPlugin));
        System.out.println("kinOathPlugin: " + kinOathPlugin.getName());
        System.out.println("kinOathPluginDescription: " + kinOathPlugin.getDescription());
        System.out.println("kinOathPluginNumber: " + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber());
    }
}
