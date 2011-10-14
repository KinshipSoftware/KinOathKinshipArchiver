package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.ui.ArbilNodeSearchPanel;
import nl.mpi.arbil.ui.ArbilSplitPanel;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilTableModel;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.kinnate.svg.GraphPanel;

/**
 *  Document   : ArchiveEntityLinkerPanel
 *  Created on : Feb 3, 2011, 10:23:32 AM
 *  Author     : Peter Withers
 */
public class ArchiveEntityLinkerPanel extends JPanel implements ActionListener {

    private JTabbedPane tabbedPane;
    private KinTree archiveTree;
    private JButton nextButton;

    public ArchiveEntityLinkerPanel(GraphPanel graphPanel, KinDragTransferHandler dragTransferHandler) {
        archiveTree = new KinTree(graphPanel);
        this.setLayout(new BorderLayout());
        JPanel treePanel = new JPanel(new BorderLayout());
        tabbedPane = new JTabbedPane();
        tabbedPane.add("Archive Branch Selection", treePanel);
        this.add(tabbedPane, BorderLayout.CENTER);
        nextButton = new JButton("Search Selected");
        nextButton.setActionCommand("Search");
        nextButton.addActionListener(this);
        treePanel.add(new JScrollPane(archiveTree), BorderLayout.CENTER);
        treePanel.add(nextButton, BorderLayout.PAGE_END);
        archiveTree.setTransferHandler(dragTransferHandler);
        archiveTree.setDragEnabled(true);
        loadTreeNodes();
    }

    private void loadTreeNodes() {
        try {
            ArbilNode imdiCorporaNode = ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, new URI("http://corpus1.mpi.nl/IMDI/metadata/IMDI.imdi"));
            ArbilNode[] allEntities = new ArbilNode[]{imdiCorporaNode};
            archiveTree.rootNodeChildren = allEntities;
            archiveTree.requestResort();
        } catch (URISyntaxException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        }
    }

    private void getSeachPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout());
        String frameTitle = "Archive Search";
        ArbilTableModel resultsTableModel = new ArbilTableModel();
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
//        imdiSplitPanel.addFocusListener(searchFrame);
//        searchFrame.pack();
        tabbedPane.add("Archive Branch Search", searchPanel);
        tabbedPane.setSelectedComponent(searchPanel);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("Search")) {
            getSeachPanel();
        }
        if (ae.getActionCommand().equals("Close Search")) {
            tabbedPane.remove(tabbedPane.getSelectedComponent());
        }
    }
}
