package nl.mpi.kinnate.plugins.export;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import nl.mpi.arbil.plugin.PluginDialogHandler;
import nl.mpi.arbil.plugin.PluginSessionStorage;
import nl.mpi.kinnate.plugin.BasePlugin;
import nl.mpi.pluginloader.KinOathPanelPlugin;

/**
 * Document : CsvExport Created on : Aug 13, 2012, 6:34:52 PM Author : Peter
 * Withers
 */
public class CsvExport implements BasePlugin, KinOathPanelPlugin {

    public String getDescription() {
        return "Exports CSV files.";
    }

    public String getName() {
        return "CSV file export";
    }

    public int getBuildVersionNumber() {
        return 0;
    }

    public int getMajorVersionNumber() {
        return 0;
    }

    public int getMinorVersionNumber() {
        return 0;
    }

    public JScrollPane getUiPanel(PluginDialogHandler dialogHandler, PluginSessionStorage sessionStorage) {
        final JPanel pluginPanel = new JPanel();
        pluginPanel.add(new JLabel(this.getDescription()));
        return new JScrollPane(pluginPanel);
    }
}
