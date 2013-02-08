package nl.mpi.kinnate.plugins.export;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import nl.mpi.flap.module.AbstractBaseModule;
import nl.mpi.flap.plugin.KinOathPanelPlugin;
import nl.mpi.flap.plugin.PluginBugCatcher;
import nl.mpi.flap.plugin.PluginDialogHandler;
import nl.mpi.flap.plugin.PluginException;
import nl.mpi.flap.plugin.PluginSessionStorage;

/**
 * Document : CsvExport Created on : Aug 13, 2012, 6:34:52 PM
 *
 * @author Peter Withers
 */
public class CsvExport extends AbstractBaseModule implements KinOathPanelPlugin {

    public CsvExport() throws PluginException {
        super("CSV file export", "Exports CSV files.", "nl.mpi.kinnate.plugins.export");
    }

    public JScrollPane getUiPanel(PluginDialogHandler dialogHandler, PluginSessionStorage sessionStorage, PluginBugCatcher bugCatcher) throws PluginException {
        final JPanel pluginPanel = new JPanel();
        pluginPanel.add(new JLabel(this.getDescription()));
        return new JScrollPane(pluginPanel);
    }
}
