package nl.mpi.kinnate.ui.entityprofiles;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader.ProfileSelection;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.kinnate.kindocument.CmdiTransformer;

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
    private SessionStorage sessionStorage;
    private BugCatcher bugCatcher;

    public CmdiProfileSelectionPanel(String panelName, SessionStorage sessionStorage, BugCatcher bugCatcher) {
        this.sessionStorage = sessionStorage;
        this.bugCatcher = bugCatcher;
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

    public void loadProfiles(final boolean forceUpdate) {
        CmdiProfileReader.getSingleInstance().setSelection(ProfileSelection.ALL);
        statusLabel.setText("Loading, please wait...");
        reloadButton.setEnabled(false);
        profileReloadProgressBar.setVisible(true);
        this.doLayout();
        new Thread("loadProfiles") {

            @Override
            public void run() {
                // load the profiles selected for use on this diagram
                for (String profileId : new String[]{"clarin.eu:cr1:p_1320657629627"}) { // todo: this array should come from the list of selected profiles in the diagram
                    try {
                        statusLabel.setText("Loading: " + profileId + ", please wait...");
                        File xsdFile = new File(sessionStorage.getCacheDirectory(), "individual" + "-" + profileId + ".xsd");
                        if (!xsdFile.exists() || forceUpdate) {
                            new CmdiTransformer(sessionStorage, bugCatcher).transformProfileXmlToXsd(xsdFile, profileId);
                        }
                    } catch (IOException exception) {
                        System.out.println("exception: " + exception.getMessage());
                    } catch (TransformerException exception) {
                        System.out.println("exception: " + exception.getMessage());
                    }
                }
                // load the profile list from the clarin server
                statusLabel.setText("Loading the profile list from the server, please wait...");
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
