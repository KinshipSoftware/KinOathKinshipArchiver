package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import nl.mpi.arbil.ImdiTree;

/**
 *  Document   : EntitySearchPanel
 *  Created on : Mar 14, 2011, 4:01:11 PM
 *  Author     : Peter Withers
 */
public class EntitySearchPanel extends JPanel {

    ImdiTree leftTree;

    public EntitySearchPanel() {
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
        JTextField searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        searchPanel.add(searchLabel, BorderLayout.PAGE_START);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.PAGE_END);
        this.add(searchPanel, BorderLayout.PAGE_START);
        this.add(leftTree, BorderLayout.CENTER);
    }

    public void setTransferHandler(DragTransferHandler dragTransferHandler) {
        leftTree.setTransferHandler(dragTransferHandler);
    }
}
