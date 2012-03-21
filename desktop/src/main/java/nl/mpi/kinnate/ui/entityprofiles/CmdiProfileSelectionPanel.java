package nl.mpi.kinnate.ui.entityprofiles;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.kinnate.kindocument.ProfileManager;

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
    private ProfileManager profileManager;

    public CmdiProfileSelectionPanel(String panelName, ProfileManager profileManager) {
        this.profileManager = profileManager;
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
    }

    public void setStatus(boolean reloadEnable, String statusText, boolean isError) {
        if (isError) {
            statusLabel.setForeground(Color.red);
        } else {
            statusLabel.setForeground(new JLabel().getForeground());
        }
        statusLabel.setText(statusText);
        reloadButton.setEnabled(reloadEnable);
        profileReloadProgressBar.setVisible(!reloadEnable);
        profileReloadProgressBar.setIndeterminate(true);
//        this.doLayout(); // seems not to be required
    }

    public void setCmdiProfileReader(CmdiProfileReader cmdiProfileReader, ProfileManager profileManager) {
        profileTableModel.setCmdiProfileReader(cmdiProfileReader, profileManager);
    }

    public JProgressBar getProfileReloadProgressBar() {
        return profileReloadProgressBar;
    }

    public void actionPerformed(ActionEvent e) {
        profileManager.loadProfiles(true, CmdiProfileSelectionPanel.this);
    }
}
