package nl.mpi.kinnate.plugins.export;

import javax.swing.JScrollPane;
import nl.mpi.arbil.plugin.PluginDialogHandler;
import nl.mpi.arbil.plugin.PluginSessionStorage;
import nl.mpi.kinnate.plugins.export.ui.KinOathExportPanel;
import nl.mpi.kinnate.plugin.BasePlugin;
import nl.mpi.pluginloader.KinOathPanelPlugin;

/**
 * Document : KinOathExport Created on : Aug 13, 2012, 6:35:12 PM Author : Peter
 * Withers
 */
public class KinOathExport implements BasePlugin, KinOathPanelPlugin {

    public String getDescription() {
        return "Exports KinOath files.";
    }

    public String getName() {
        return "KinOath single file export";
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
        final KinOathExportPanel exportPanel = new KinOathExportPanel(dialogHandler, sessionStorage);
        return new JScrollPane(exportPanel);
    }
}
