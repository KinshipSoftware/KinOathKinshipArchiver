package nl.mpi.kinnate.plugins.export;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import nl.mpi.pluginloader.BasePlugin;
import nl.mpi.pluginloader.KinOathPanelPlugin;

/**
 * Document : KinOathExport
 * Created on : Aug 13, 2012, 6:35:12 PM
 * Author : Peter Withers
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

    public JScrollPane getUiPanel() {
        final JPanel pluginPanel = new JPanel();
        pluginPanel.add(new JLabel(this.getDescription()));
        return new JScrollPane(pluginPanel);
    }
}
