package nl.mpi.kinnate.ui.entityprofiles;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader.ProfileSelection;

/**
 *  Document   : CmdiProfileSelectionPanel
 *  Created on : Jan 19, 2012, 3:57:26 PM
 *  Author     : Peter Withers
 */
public class CmdiProfileSelectionPanel extends JPanel implements ActionListener {

    private JTable profileTable;
    private JPanel topPanel;
    private JProgressBar profileReloadProgressBar;
    private JLabel statusLabel;
    private JButton reloadButton;
    private ProfileTableModel profileTableModel;

    public CmdiProfileSelectionPanel(String panelName) {
        this.setName(panelName);
        this.setLayout(new BorderLayout());
        profileTableModel = new ProfileTableModel();
        profileTable = new JTable(profileTableModel);
        topPanel = new JPanel(new BorderLayout());
        profileReloadProgressBar = new JProgressBar();
        statusLabel = new JLabel();
        reloadButton = new JButton("Reload List");
        reloadButton.addActionListener(this);
        topPanel.add(statusLabel, BorderLayout.CENTER);
        topPanel.add(reloadButton, BorderLayout.LINE_END);
        this.add(topPanel, BorderLayout.PAGE_START);
        this.add(profileReloadProgressBar, BorderLayout.PAGE_END);
        this.add(new JScrollPane(profileTable), BorderLayout.CENTER);
        loadProfiles(false); // care should be taken here because this could run when an unsuspecting user opens the application for the first time and if the profile loading slowness is not resolved it could create a bad first experience
    }

    private void loadProfiles(final boolean forceUpdate) {
        CmdiProfileReader.getSingleInstance().setSelection(ProfileSelection.ALL);
        statusLabel.setText("Loading, please wait...");
        reloadButton.setEnabled(false);
        profileReloadProgressBar.setVisible(true);
        this.doLayout();
        new Thread("loadProfiles") {

            @Override
            public void run() {
                CmdiProfileReader cmdiProfileReader = CmdiProfileReader.getSingleInstance();
                cmdiProfileReader.refreshProfiles(profileReloadProgressBar, forceUpdate);
                profileReloadProgressBar.setVisible(false);
                profileTableModel.setCmdiProfileReader(cmdiProfileReader);
                statusLabel.setText("");
                reloadButton.setEnabled(true);
            }
        }.start();
    }

    public void actionPerformed(ActionEvent e) {
        loadProfiles(true);
    }
}
