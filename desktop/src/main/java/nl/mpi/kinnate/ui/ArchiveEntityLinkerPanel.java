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
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.ui.ArbilNodeSearchPanel;
import nl.mpi.arbil.ui.ArbilSplitPanel;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilTableModel;
import nl.mpi.arbil.ui.ArbilTree;

/**
 *  Document   : ArchiveEntityLinkerPanel
 *  Created on : Feb 3, 2011, 10:23:32 AM
 *  Author     : Peter Withers
 */
public class ArchiveEntityLinkerPanel extends JPanel implements ActionListener {

    private JTabbedPane tabbedPane;
    private ArbilTree kinTree;
    private ArbilTree archiveTree;
    private JButton nextButton;
//    ArbilNodeSearchPanel archiveSearch = new ArbilNodeSearchPanel();

    public ArchiveEntityLinkerPanel(ArbilTable previewTable) {
        kinTree = new ArbilTree();
        archiveTree = new ArbilTree();
        ArchiveEntityLinkerDragHandler linkerDragHandler = new ArchiveEntityLinkerDragHandler(kinTree);
        kinTree.setTransferHandler(linkerDragHandler);
        archiveTree.setTransferHandler(linkerDragHandler);
        this.setLayout(new BorderLayout());
        HidePane hidePane = new HidePane(HidePane.HidePanePosition.left, 0);
        hidePane.add(kinTree, "Kin Entities");
        this.add(hidePane, BorderLayout.LINE_START);
        JPanel treePanel = new JPanel(new BorderLayout());
        tabbedPane = new JTabbedPane();
        tabbedPane.add("Archive Branch Selection", treePanel);
        this.add(tabbedPane, BorderLayout.CENTER);
        nextButton = new JButton("Search Selected");
        nextButton.setActionCommand("Search");
        nextButton.addActionListener(this);
        treePanel.add(new JScrollPane(archiveTree), BorderLayout.CENTER);
        treePanel.add(nextButton, BorderLayout.PAGE_END);
        kinTree.setCustomPreviewTable(previewTable);
        archiveTree.setCustomPreviewTable(previewTable);
        loadTreeNodes();
    }

    private void loadTreeNodes() {
        try {
            ArbilDataNode imdiCorporaNode = ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, new URI("http://corpus1.mpi.nl/IMDI/metadata/IMDI.imdi"));
            ArbilDataNode[] allEntities = new ArbilDataNode[]{imdiCorporaNode};
            archiveTree.rootNodeChildren = allEntities;
            archiveTree.requestResort();
        } catch (URISyntaxException exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
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
