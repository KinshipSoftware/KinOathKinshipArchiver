package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
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
import nl.mpi.arbil.ImdiTree;
import nl.mpi.arbil.LinorgBugCatcher;
import nl.mpi.arbil.data.ImdiLoader;
import nl.mpi.arbil.data.ImdiTreeObject;
import nl.mpi.kinnate.entityindexer.EntityCollection;

/**
 *  Document   : EntitySearchPanel
 *  Created on : Mar 14, 2011, 4:01:11 PM
 *  Author     : Peter Withers
 */
public class EntitySearchPanel extends JPanel {

    private EntityCollection entityCollection;
    private ImdiTree leftTree;
    private JTextArea resultsArea = new JTextArea();
    private JTextField searchField;

    public EntitySearchPanel(EntityCollection entityCollectionLocal) {
        entityCollection = entityCollectionLocal;
        this.setLayout(new BorderLayout());
        leftTree = new ImdiTree();
        leftTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Test Tree"), true));
//                ArrayList<URI> allEntityUris = new ArrayList<URI>();
//        String[] treeNodesArray = LinorgSessionStorage.getSingleInstance().loadStringArray("KinGraphTree");
//        if (treeNodesArray != null) {
//            ArrayList<ImdiTreeObject> tempArray = new ArrayList<ImdiTreeObject>();
//            for (String currentNodeString : treeNodesArray) {
//                try {
//                    ImdiTreeObject currentImdiNode = ImdiLoader.getSingleInstance().getImdiObject(null, new URI(currentNodeString));
//                    tempArray.add(currentImdiNode);
//                    allEntityUris.add(currentImdiNode.getURI());
//                } catch (URISyntaxException exception) {
//                    GuiHelper.linorgBugCatcher.logError(exception);
//                }
//            }
//            ImdiTreeObject[] allEntities = tempArray.toArray(new ImdiTreeObject[]{});
//            leftTree.rootNodeChildren = allEntities;
//            imdiTableModel.removeAllImdiRows();
//            imdiTableModel.addImdiObjects(leftTree.rootNodeChildren);
//        } //else {
//        //   leftTree.rootNodeChildren = new ImdiTreeObject[]{graphPanel.imdiNode};
//        // }
        leftTree.requestResort();
        JLabel searchLabel = new JLabel("Search Entity Names");
        searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ArrayList<ImdiTreeObject> resultsArray = new ArrayList<ImdiTreeObject>();
                EntityCollection.SearchResults searchResults = entityCollection.searchByName(searchField.getText());
                String[] rawResultsArray = searchResults.resultsPathArray;
                resultsArea.setText(searchResults.statusMessage + "\n");
                for (String resultLine : rawResultsArray) {
                    try {
                        if (resultsArray.size() < 100) {
                            ImdiTreeObject currentImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, new URI(resultLine));
                            currentImdiObject.reloadNode();
                            resultsArray.add(currentImdiObject);
                        } else {
                            resultsArea.append("results limited to 100\n");
                            break;
                        }
                    } catch (URISyntaxException exception) {
                        new LinorgBugCatcher().logError(exception);
                        resultsArea.append("error: " + resultLine + "\n");
                    }
                }
                leftTree.rootNodeChildren = resultsArray.toArray(new ImdiTreeObject[]{});
                leftTree.requestResort();
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
}
