package nl.mpi.pluginloader.ui;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import nl.mpi.pluginloader.KinOathPlugin;
import nl.mpi.pluginloader.PluginManager;
import nl.mpi.pluginloader.PluginService;

/**
 * Document : PluginMenu
 * Created on : Aug 13, 2012, 3:47:55 PM
 * Author : Peter Withers
 */
public class PluginMenu extends JMenu {

    final PluginService pluginService;

    public PluginMenu(PluginService pluginService, PluginManager pluginManager) {
        super("Plugins");
        this.pluginService = pluginService;
        try {
            Iterator<KinOathPlugin> pluginIterator = pluginService.getPlugins();
            while (pluginIterator.hasNext()) {
                final KinOathPlugin kinOathPlugin = pluginIterator.next();
                System.out.println("Plugin: " + kinOathPlugin.getName());
                JMenuItem menuItem = new JMenuItem(new PluginMenuAction(pluginManager, kinOathPlugin));
                menuItem.addActionListener(actionListener);
                this.add(menuItem);
            }
        } catch (ServiceConfigurationError serviceError) {
            this.add(new JLabel("<could not load any plugins>"));
        }
    }

    public static void main(String[] args) {
        JFrame jFrame = new JFrame();
        jFrame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        JMenuBar jMenuBar = new JMenuBar();
        final JTextArea jTextArea = new JTextArea();
        PluginManager pluginManager = new PluginManager() {

            public void activatePlugin(KinOathPlugin kinOathPlugin) {
                jTextArea.setText("activate: \n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getVersionNumber() + "\n" + kinOathPlugin.getDescription());
            }

            public void deactivatePlugin(KinOathPlugin kinOathPlugin) {
                jTextArea.setText("deactivate: \n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getVersionNumber() + "\n" + kinOathPlugin.getDescription());
            }
        };
        jMenuBar.add(new PluginMenu(PluginService.getInstance(), pluginManager));
        jFrame.setJMenuBar(jMenuBar);
        jFrame.setContentPane(new JScrollPane(jTextArea));
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
