package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.kinnate.entityindexer.EntityCollection;

/**
 *  Document   : EntitySearchPanel
 *  Created on : Mar 14, 2011, 4:01:11 PM
 *  Author     : Peter Withers
 */
public class EntitySearchPanel extends JPanel {

    private EntityCollection entityCollection;
    private ArbilTree leftTree;
    private JTextArea resultsArea = new JTextArea();
    private JTextField searchField;

    public EntitySearchPanel(EntityCollection entityCollectionLocal, ArbilTable arbilTable) {
        entityCollection = entityCollectionLocal;
        this.setLayout(new BorderLayout());
        leftTree = new ArbilTree();
        leftTree.setCustomPreviewTable(arbilTable);
        leftTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Test Tree"), true));
//                ArrayList<URI> allEntityUris = new ArrayList<URI>();
//        String[] treeNodesArray = LinorgSessionStorage.getSingleInstance().loadStringArray("KinGraphTree");
//        if (treeNodesArray != null) {
//            ArrayList<ArbilTreeObject> tempArray = new ArrayList<ArbilTreeObject>();
//            for (String currentNodeString : treeNodesArray) {
//                try {
//                    ArbilTreeObject currentArbilNode = ArbilLoader.getSingleInstance().getArbilObject(null, new URI(currentNodeString));
//                    tempArray.add(currentArbilNode);
//                    allEntityUris.add(currentArbilNode.getURI());
//                } catch (URISyntaxException exception) {
//                    GuiHelper.linorgBugCatcher.logError(exception);
//                }
//            }
//            ArbilTreeObject[] allEntities = tempArray.toArray(new ArbilTreeObject[]{});
//            leftTree.rootNodeChildren = allEntities;
//            imdiTableModel.removeAllArbilRows();
//            imdiTableModel.addArbilObjects(leftTree.rootNodeChildren);
//        } //else {
//        //   leftTree.rootNodeChildren = new ArbilTreeObject[]{graphPanel.imdiNode};
//        // }
        leftTree.setRootVisible(false);
        leftTree.requestResort();
        JLabel searchLabel = new JLabel("Search Entity Names");
        searchField = new JTextField();
        searchField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    EntitySearchPanel.this.performSearch();
                }
                super.keyReleased(e);
            }
        });
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EntitySearchPanel.this.performSearch();
            }
        });
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        searchPanel.add(searchLabel, BorderLayout.PAGE_START);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.PAGE_END);
        this.add(searchPanel, BorderLayout.PAGE_START);
        this.add(new JScrollPane(leftTree), BorderLayout.CENTER);
        this.add(resultsArea, BorderLayout.PAGE_END);
    }

    public void setTransferHandler(DragTransferHandler dragTransferHandler) {
        leftTree.setTransferHandler(dragTransferHandler);
    }

    protected void performSearch() {
        ArrayList<ArbilDataNode> resultsArray = new ArrayList<ArbilDataNode>();
        EntityCollection.SearchResults searchResults = entityCollection.searchByName(searchField.getText());
        String[] rawResultsArray = searchResults.resultsPathArray;
        resultsArea.setText(searchResults.statusMessage + "\n");
        for (String resultLine : rawResultsArray) {
            try {
                if (resultsArray.size() < 100) {
                    ArbilDataNode currentArbilObject = ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, new URI(resultLine));
                    currentArbilObject.reloadNode();
                    resultsArray.add(currentArbilObject);
                } else {
                    resultsArea.append("results limited to 100\n");
                    break;
                }
            } catch (URISyntaxException exception) {
                new ArbilBugCatcher().logError(exception);
                resultsArea.append("error: " + resultLine + "\n");
            }
        }
        leftTree.rootNodeChildren = resultsArray.toArray(new ArbilDataNode[]{});
        leftTree.requestResort();
    }
}
