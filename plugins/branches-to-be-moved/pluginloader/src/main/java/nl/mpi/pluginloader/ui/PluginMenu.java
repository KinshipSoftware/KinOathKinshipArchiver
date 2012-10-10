package nl.mpi.pluginloader.ui;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import nl.mpi.arbil.plugin.ActivatablePlugin;
import nl.mpi.arbil.plugin.PluginException;
import nl.mpi.kinnate.plugin.BasePlugin;
import nl.mpi.pluginloader.PluginManager;
import nl.mpi.pluginloader.PluginService;

/**
 * Document : PluginMenu Created on : Aug 13, 2012, 3:47:55 PM
 *
 * @ author Peter Withers
 */
public class PluginMenu extends JMenu {

    final PluginService pluginService;

    public PluginMenu(PluginService pluginService, PluginManager pluginManager, boolean hideIfNoPluginsFound) {
        super("Plugins");
        this.pluginService = pluginService;
        Iterator<BasePlugin> pluginIterator = pluginService.getPlugins();
        boolean hasPlugins = false;
        while (pluginIterator.hasNext()) {
            try {
                hasPlugins = true;
                final BasePlugin kinOathPlugin = pluginIterator.next();
                System.out.println("Plugin: " + kinOathPlugin.getName());
                JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(new PluginMenuAction(pluginManager, kinOathPlugin));
                menuItem.setSelected(pluginManager.isActivated(kinOathPlugin));
                this.add(menuItem);
            } catch (ServiceConfigurationError serviceError) {
                this.add(new JLabel("<failed to load plugin>"));
            }
        }
        if (!hasPlugins) {
            this.add(new JLabel("<no plugins found>"));
            this.setVisible(!hideIfNoPluginsFound);
        }
    }

    public static void main(String[] args) {
        JFrame jFrame = new JFrame();
        jFrame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        JMenuBar jMenuBar = new JMenuBar();
        final JTextArea jTextArea = new JTextArea();
        PluginManager pluginManager = new PluginManager() {
            public boolean isActivated(BasePlugin kinOathPlugin) {
                try {
                    if (kinOathPlugin instanceof ActivatablePlugin) {
                        return ((ActivatablePlugin) kinOathPlugin).getIsActivated();
                    }
                } catch (PluginException exception) {
                    System.err.println("error getting plugin state:" + exception.getMessage());
                }
                return false;
            }

            public void activatePlugin(BasePlugin kinOathPlugin) {
                try {
                    if (kinOathPlugin instanceof ActivatablePlugin) {
                        ((ActivatablePlugin) kinOathPlugin).activatePlugin(null, null);
                        jTextArea.setText("activate: \n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription());
                    } else {
                        jTextArea.setText("non activateable plugin: \n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription());
                    }
                } catch (PluginException exception) {
                    jTextArea.setText("Error activating plugin: \n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription());
                }
            }

            public void deactivatePlugin(BasePlugin kinOathPlugin) {
                try {
                    if (kinOathPlugin instanceof ActivatablePlugin) {
                        ((ActivatablePlugin) kinOathPlugin).deactivatePlugin(null, null);
                        jTextArea.setText("deactivate: \n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription());
                    } else {
                        jTextArea.setText("non deactivateable plugin: \n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription());
                    }
                } catch (PluginException exception) {
                    jTextArea.setText("error deactivating plugin: \n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription());
                }
            }
        };
        jMenuBar.add(new PluginMenu(PluginService.getInstance(), pluginManager, true));
        jFrame.setJMenuBar(jMenuBar);
        jFrame.setContentPane(new JScrollPane(jTextArea));
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
