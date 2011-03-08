package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import nl.mpi.arbil.ImdiNodeSearchPanel;
import nl.mpi.arbil.ImdiTable;
import nl.mpi.arbil.ImdiTableModel;
import nl.mpi.arbil.ImdiTree;
import nl.mpi.arbil.LinorgSplitPanel;
import nl.mpi.arbil.data.ImdiLoader;
import nl.mpi.arbil.data.ImdiTreeObject;

/**
 *  Document   : ArchiveEntityLinkerPanel
 *  Created on : Feb 3, 2011, 10:23:32 AM
 *  Author     : Peter Withers
 */
public class ArchiveEntityLinkerPanel extends JPanel implements ActionListener {

    private JTabbedPane tabbedPane;
    private ImdiTree archiveTree = new ImdiTree();
    private JButton nextButton;
//    ImdiNodeSearchPanel archiveSearch = new ImdiNodeSearchPanel();

    public ArchiveEntityLinkerPanel() {
        this.setLayout(new BorderLayout());
        JPanel treePanel = new JPanel(new BorderLayout());
        tabbedPane = new JTabbedPane();
        tabbedPane.add("Archive Branch Selection", treePanel);
        this.add(tabbedPane, BorderLayout.CENTER);
        nextButton = new JButton("Search Selected");
        nextButton.setActionCommand("Search");
        nextButton.addActionListener(this);
        treePanel.add(archiveTree, BorderLayout.CENTER);
        treePanel.add(nextButton, BorderLayout.PAGE_END);
        loadTreeNodes();
    }

    private void loadTreeNodes() {
        try {
            ImdiTreeObject imdiCorporaNode = ImdiLoader.getSingleInstance().getImdiObject(null, new URI("http://corpus1.mpi.nl/IMDI/metadata/IMDI.imdi"));
            ImdiTreeObject[] allEntities = new ImdiTreeObject[]{imdiCorporaNode};
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
        ImdiTableModel resultsTableModel = new ImdiTableModel();
        ImdiTable imdiTable = new ImdiTable(resultsTableModel, frameTitle);
        LinorgSplitPanel imdiSplitPanel = new LinorgSplitPanel(imdiTable);
        // todo: take care of main window actions such as pack that might cause odd visuals
        JInternalFrame searchFrame = new JInternalFrame();
        searchPanel.add(new ImdiNodeSearchPanel(searchFrame, resultsTableModel, archiveTree.getSelectedNodes()), BorderLayout.PAGE_START);
        searchPanel.add(imdiSplitPanel, BorderLayout.CENTER);
        imdiSplitPanel.setSplitDisplay();
//        imdiSplitPanel.addFocusListener(searchFrame);
//        searchFrame.pack();
        tabbedPane.add("Archive Branch Search", searchPanel);
        tabbedPane.setSelectedIndex(1);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("Search")) {
            getSeachPanel();
        }
    }
}
