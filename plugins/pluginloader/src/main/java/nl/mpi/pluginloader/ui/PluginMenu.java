package nl.mpi.pluginloader.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
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
            boolean hasPlugins = false;
            while (pluginIterator.hasNext()) {
                hasPlugins = true;
                final KinOathPlugin kinOathPlugin = pluginIterator.next();
                System.out.println("Plugin: " + kinOathPlugin.getName());
                JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(new PluginMenuAction(pluginManager, kinOathPlugin));
                menuItem.addActionListener(actionListener);
                menuItem.setSelected(pluginManager.isActivated(kinOathPlugin));
                this.add(menuItem);
            }
            if (!hasPlugins) {
                this.add(new JLabel("<no plugins found>"));
            }
        } catch (ServiceConfigurationError serviceError) {
            this.add(new JLabel("<failed to load any plugins>"));
        }
    }

    public static void main(String[] args) {
        JFrame jFrame = new JFrame();
        jFrame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        JMenuBar jMenuBar = new JMenuBar();
        final JTextArea jTextArea = new JTextArea();
        PluginManager pluginManager = new PluginManager() {

            HashSet<KinOathPlugin> hashSet = new HashSet<KinOathPlugin>();

            public boolean isActivated(KinOathPlugin kinOathPlugin) {
                return hashSet.contains(kinOathPlugin);
            }

            public void activatePlugin(KinOathPlugin kinOathPlugin) {
                hashSet.add(kinOathPlugin);
                jTextArea.setText("activate: \n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription());
            }

            public void deactivatePlugin(KinOathPlugin kinOathPlugin) {
                hashSet.remove(kinOathPlugin);
                jTextArea.setText("deactivate: \n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription());
            }
        };
        jMenuBar.add(new PluginMenu(PluginService.getInstance(), pluginManager));
        jFrame.setJMenuBar(jMenuBar);
        jFrame.setContentPane(new JScrollPane(jTextArea));
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
