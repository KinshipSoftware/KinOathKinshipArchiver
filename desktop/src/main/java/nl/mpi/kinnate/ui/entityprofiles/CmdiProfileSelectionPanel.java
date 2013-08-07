/**
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.ui.entityprofiles;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import nl.mpi.kinnate.kindocument.ProfileManager;
import nl.mpi.kinnate.svg.GraphPanel;

/**
 * Document : CmdiProfileSelectionPanel
 * Created on : Jan 19, 2012, 3:57:26 PM
 * Author : Peter Withers
 */
public class CmdiProfileSelectionPanel extends JPanel implements ActionListener {
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Widgets");

    private JTable profileTable;
    private JPanel topPanel;
    private JProgressBar profileReloadProgressBar;
    private JLabel statusLabel;
    private JButton reloadButton;
    private ProfileTableModel profileTableModel;
    private ProfileManager profileManager;
    private GraphPanel graphPanel;
    private Color foregroundColour;

    public CmdiProfileSelectionPanel(String panelName, ProfileManager profileManager, GraphPanel graphPanel) {
        this.profileManager = profileManager;
        this.graphPanel = graphPanel;
        this.setName(panelName);
        this.setLayout(new BorderLayout());
        profileTableModel = new ProfileTableModel();
        profileTable = new JTable(profileTableModel);
        topPanel = new JPanel(new BorderLayout());
        profileReloadProgressBar = new JProgressBar();
        statusLabel = new JLabel();
        foregroundColour = new JLabel().getForeground();
        reloadButton = new JButton(widgets.getString("RELOAD LIST"));
        reloadButton.addActionListener(this);
        topPanel.add(statusLabel, BorderLayout.CENTER);
        topPanel.add(reloadButton, BorderLayout.LINE_END);
        this.add(topPanel, BorderLayout.PAGE_START);
        this.add(profileReloadProgressBar, BorderLayout.PAGE_END);
        this.add(new JScrollPane(profileTable), BorderLayout.CENTER);
    }

    public void setStatus(final boolean reloadEnable, final String statusText, final boolean isError) {

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (isError) {
                    statusLabel.setForeground(Color.red);
                } else {
                    statusLabel.setForeground(foregroundColour);
                }
                statusLabel.setText(statusText);
                reloadButton.setEnabled(reloadEnable);
                profileReloadProgressBar.setVisible(!reloadEnable);
                profileReloadProgressBar.setIndeterminate(true);
                profileTableModel.fireTableDataChanged(); //todo: this might not be required in some cases, but when a profile fails to load then the table needs to refresh so that the faild profile shows as unchecked 
            }
        });
//        this.doLayout(); // seems not to be required
    }

    public void setProfileManager(ProfileManager profileManager) {
        profileTableModel.setProfileManager(profileManager);
    }

//    public JProgressBar getProfileReloadProgressBar() {
//        return profileReloadProgressBar;
//    }
    public void actionPerformed(ActionEvent e) {
        profileManager.loadProfiles(true, CmdiProfileSelectionPanel.this, graphPanel);
    }
}
