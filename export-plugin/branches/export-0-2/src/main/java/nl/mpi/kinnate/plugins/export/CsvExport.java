package nl.mpi.kinnate.plugins.export;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import nl.mpi.arbil.plugin.KinOathPanelPlugin;
import nl.mpi.arbil.plugin.PluginBugCatcher;
import nl.mpi.arbil.plugin.PluginDialogHandler;
import nl.mpi.arbil.plugin.PluginException;
import nl.mpi.arbil.plugin.PluginSessionStorage;
import nl.mpi.kinnate.plugin.AbstractBasePlugin;

/**
 * Document : CsvExport Created on : Aug 13, 2012, 6:34:52 PM
 *
 * @author Peter Withers
 */
public class CsvExport extends AbstractBasePlugin implements KinOathPanelPlugin {

    public CsvExport() throws PluginException {
        super("CSV file export", "Exports CSV files.", "nl.mpi.kinnate.plugins.export");
    }

    public JScrollPane getUiPanel(PluginDialogHandler dialogHandler, PluginSessionStorage sessionStorage, PluginBugCatcher bugCatcher) throws PluginException {
        final JPanel pluginPanel = new JPanel();
        pluginPanel.add(new JLabel(this.getDescription()));
        return new JScrollPane(pluginPanel);
    }
}
