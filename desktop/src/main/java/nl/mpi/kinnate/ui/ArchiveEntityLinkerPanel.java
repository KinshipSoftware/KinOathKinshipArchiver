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
package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.data.ContainerNode;
import nl.mpi.arbil.ui.ArbilNodeSearchPanel;
import nl.mpi.arbil.ui.ArbilSplitPanel;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilTableModel;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.kindata.VisiblePanelSetting;
import nl.mpi.kinnate.svg.GraphPanel;

/**
 * Document : ArchiveEntityLinkerPanel
 * Created on : Feb 3, 2011, 10:23:32 AM
 * Author : Peter Withers
 */
public class ArchiveEntityLinkerPanel extends JPanel implements ActionListener {

    private KinTree archiveTree; // todo: do we really want to be using a kin tree not an arbil tree?
    private JButton nextButton;
    private TreeType treeType;
    private VisiblePanelSetting panelSetting;
    private ArbilTreeHelper treeHelper;
    private ArbilDataNodeLoader dataNodeLoader;
    private ContainerNode rootNode;

    public enum TreeType {

        RemoteTree, LocalTree, MpiTree
    }

    public ArchiveEntityLinkerPanel(VisiblePanelSetting panelSetting, KinDiagramPanel kinDiagramPanel, GraphPanel graphPanel, KinDragTransferHandler dragTransferHandler, TreeType treeType, ArbilTreeHelper treeHelper, ArbilDataNodeLoader dataNodeLoader) {
        this.treeHelper = treeHelper;
        this.dataNodeLoader = dataNodeLoader;
        this.treeType = treeType;
        this.panelSetting = panelSetting;
        rootNode = new ContainerNode("", null, new ArbilNode[]{}); // "corpus1.mpi.nl"
        archiveTree = new KinTree(kinDiagramPanel, graphPanel, rootNode);
        this.setLayout(new BorderLayout());
        JPanel treePanel = new JPanel(new BorderLayout());
//        tabbedPane = new JTabbedPane();
//        tabbedPane.add("Archive Branch Selection", treePanel);
//        this.add(tabbedPane, BorderLayout.CENTER);
        this.add(treePanel, BorderLayout.CENTER);
        nextButton = new JButton("Search Selected");
        nextButton.setActionCommand("Search");
        nextButton.addActionListener(this);
        treePanel.add(new JScrollPane(archiveTree), BorderLayout.CENTER);
        treePanel.add(nextButton, BorderLayout.PAGE_END);
        archiveTree.setTransferHandler(dragTransferHandler);
        archiveTree.setDragEnabled(true);
    }

    public void loadTreeNodes() {
        try {
            ArbilNode[] allEntities;
            switch (treeType) {
                case LocalTree:
                    allEntities = treeHelper.getLocalCorpusNodes();
                    this.setName("Local Corpus");
                    break;
                case RemoteTree:
                    allEntities = treeHelper.getRemoteCorpusNodes();
                    this.setName("Remote Corpus");
                    break;
                case MpiTree:
                default:
                    ArbilNode imdiCorporaNode = dataNodeLoader.getArbilDataNode(null, new URI("http://corpus1.mpi.nl/IMDI/metadata/IMDI.imdi"));
                    allEntities = new ArbilNode[]{imdiCorporaNode};
                    this.setName("Nijmegen Corpus");
                    break;
            }
            rootNode.setChildNodes(allEntities);
            archiveTree.requestResort();
        } catch (URISyntaxException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        }
    }

    private void getSeachPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout());
        String frameTitle = "Archive Search";
        ArbilTableModel resultsTableModel = new ArbilTableModel(null); // todo: pass a ImageBoxRenderer here if you want thumbnails
        ArbilTable imdiTable = new ArbilTable(resultsTableModel, frameTitle);
        ArbilSplitPanel imdiSplitPanel = new ArbilSplitPanel(imdiTable);
        // todo: take care of main window actions such as pack that might cause odd visuals
        JInternalFrame searchFrame = new JInternalFrame();
        searchPanel.add(new ArbilNodeSearchPanel(searchFrame, resultsTableModel, archiveTree.getSelectedNodes()), BorderLayout.PAGE_START);
        searchPanel.add(imdiSplitPanel, BorderLayout.CENTER);
        JButton closeSearch = new JButton("Close Search");
        closeSearch.setActionCommand("Close Search");
        closeSearch.addActionListener(this);
        searchPanel.add(closeSearch, BorderLayout.PAGE_END);
        imdiSplitPanel.setSplitDisplay();
        searchPanel.setName(this.getName() + " Search");
        panelSetting.addTargetPanel(searchPanel, true);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("Search")) {
            getSeachPanel();
        }
        if (ae.getActionCommand().equals("Close Search")) {
            panelSetting.removeTargetPanel(((Component) ae.getSource()).getParent());
        }
    }
}
