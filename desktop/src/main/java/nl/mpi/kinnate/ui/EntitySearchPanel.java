package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ContainerNode;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.data.KinTreeNode;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kintypestrings.ParserHighlight;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : EntitySearchPanel
 * Created on : Mar 14, 2011, 4:01:11 PM
 * Author : Peter Withers
 */
public class EntitySearchPanel extends JPanel implements KinTypeStringProvider {

    private EntityCollection entityCollection;
    private KinTree resultsTree;
    private JTextArea resultsArea = new JTextArea();
    private JCheckBox graphSelectionCheckBox;
    private JCheckBox expandByKinTypeCheckBox;
    private JTextField kinTypeStringTextArea;
    private JTextField searchField;
    private JProgressBar progressBar;
    private JButton searchButton;
    private JPanel searchPanel;
    private GraphPanel graphPanel;
    private MessageDialogHandler dialogHandler;
    private ArbilDataNodeLoader dataNodeLoader;
    private String kinTypeString = "P";
    ContainerNode rootNode;

    public EntitySearchPanel(EntityCollection entityCollection, KinDiagramPanel kinDiagramPanel, GraphPanel graphPanel, MessageDialogHandler dialogHandler, ArbilDataNodeLoader dataNodeLoader, String nodeSetTitle, UniqueIdentifier[] entityIdentifiers) {
        InitPanel(entityCollection, kinDiagramPanel, graphPanel, dialogHandler, dataNodeLoader, nodeSetTitle, entityIdentifiers);
    }

    public EntitySearchPanel(EntityCollection entityCollection, KinDiagramPanel kinDiagramPanel, GraphPanel graphPanel, MessageDialogHandler dialogHandler, ArbilDataNodeLoader dataNodeLoader) {
        InitPanel(entityCollection, kinDiagramPanel, graphPanel, dialogHandler, dataNodeLoader, "Search Entity Names", null);
    }

    private void InitPanel(EntityCollection entityCollection, final KinDiagramPanel kinDiagramPanel, GraphPanel graphPanel, MessageDialogHandler dialogHandler, ArbilDataNodeLoader dataNodeLoader, String nodeSetTitle, UniqueIdentifier[] entityIdentifiers) {
        this.entityCollection = entityCollection;
        this.graphPanel = graphPanel;
        this.dialogHandler = dialogHandler;
        this.dataNodeLoader = dataNodeLoader;
        this.setLayout(new BorderLayout());
        rootNode = new ContainerNode("results", null, new ArbilNode[]{});
        resultsTree = new KinTree(kinDiagramPanel, graphPanel, rootNode);
        resultsTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Test Tree"), true));
//        resultsTree.setRootVisible(false);
        // resultsTree.requestResort();// this resort is unrequred
        JLabel searchLabel = new JLabel(nodeSetTitle);
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
        progressBar = new JProgressBar();
        searchButton = new JButton("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EntitySearchPanel.this.performSearch();
            }
        });
        searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        searchPanel.add(searchLabel, BorderLayout.PAGE_START);
        final JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));
        searchPanel.add(optionsPanel, BorderLayout.CENTER);
        if (entityIdentifiers == null) {
            optionsPanel.add(searchField);
        }
        graphSelectionCheckBox = new JCheckBox("Graph selection", true);
        resultsTree.setUpdateGraphOnSelectionChange(true);
        // graph the selection when checked
        optionsPanel.add(graphSelectionCheckBox);
        expandByKinTypeCheckBox = new JCheckBox("Expand selection by kin type string", false);
        // expand the selection when checked
        optionsPanel.add(expandByKinTypeCheckBox);
        kinTypeStringTextArea = new JTextField(kinTypeString);
        kinTypeStringTextArea.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                synchronized (e) {
                    if (!kinTypeStringTextArea.getText().equals(kinTypeString)) {
                        kinTypeString = kinTypeStringTextArea.getText();
                        if (expandByKinTypeCheckBox.isSelected() && graphSelectionCheckBox.isSelected()) {
                            // update if text changed and selected
                            kinDiagramPanel.drawGraph();
                        }
                    }

                }
            }
        });
        optionsPanel.add(kinTypeStringTextArea);
        graphSelectionCheckBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                expandByKinTypeCheckBox.setEnabled(graphSelectionCheckBox.isSelected());
                kinTypeStringTextArea.setEnabled(expandByKinTypeCheckBox.isSelected() && graphSelectionCheckBox.isSelected());
                kinDiagramPanel.drawGraph();
                resultsTree.setUpdateGraphOnSelectionChange(graphSelectionCheckBox.isSelected());
            }
        });
        expandByKinTypeCheckBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                kinTypeStringTextArea.setEnabled(expandByKinTypeCheckBox.isSelected());
                kinDiagramPanel.drawGraph();
            }
        });
        // todo: link this selection when checked
        // optionsPanel.add(new JCheckBox("Link to graph selection to this tree", false));
        if (entityIdentifiers == null) {
            searchPanel.add(searchButton, BorderLayout.PAGE_END);
        }
        this.add(searchPanel, BorderLayout.PAGE_START);
        this.add(new JScrollPane(resultsTree), BorderLayout.CENTER);
        this.add(resultsArea, BorderLayout.PAGE_END);
        if (entityIdentifiers != null) {
            loadCollection(entityIdentifiers);
        }
    }

    public void setTransferHandler(KinDragTransferHandler dragTransferHandler) {
        resultsTree.setTransferHandler(dragTransferHandler);
        resultsTree.setDragEnabled(true);
    }

    protected void performSearch() {
        searchPanel.remove(searchButton);
        progressBar.setIndeterminate(true);
        searchPanel.add(progressBar, BorderLayout.PAGE_END);
        searchPanel.revalidate();
        new Thread() {

            @Override
            public void run() {
                ArrayList<ArbilNode> resultsArray = new ArrayList<ArbilNode>();
                EntityData[] searchResults = entityCollection.getEntityByKeyWord(searchField.getText(), graphPanel.getIndexParameters());
                resultsArea.setText("Found " + searchResults.length + " entities\n");
                for (EntityData entityData : searchResults) {
//            if (resultsArray.size() < 1000) {
                    resultsArray.add(new KinTreeNode(entityData, graphPanel.getIndexParameters(), dialogHandler, entityCollection, dataNodeLoader));
//            } else {
//                resultsArea.append("results limited to 1000\n");
//                break;
//            }
                }
                rootNode.setChildNodes(resultsArray.toArray(new ArbilNode[]{}));
                resultsTree.requestResort();
                searchPanel.remove(progressBar);
                searchPanel.add(searchButton, BorderLayout.PAGE_END);
                searchPanel.revalidate();
            }
        }.start();
    }

    protected void loadCollection(final UniqueIdentifier[] entityIdentifiers) {
        progressBar.setIndeterminate(false);
        progressBar.setMinimum(0);
        progressBar.setMaximum(entityIdentifiers.length);
        progressBar.setValue(0);
        searchPanel.add(progressBar, BorderLayout.PAGE_END);
        searchPanel.revalidate();
        new Thread() {

            @Override
            public void run() {
                HashSet<ArbilNode> resultsArray = new HashSet<ArbilNode>();
                resultsArea.setText("Loading " + entityIdentifiers.length + " entities\n");
                int loadedCount = 0;
                for (UniqueIdentifier entityId : entityIdentifiers) {
                    EntityData entityData = entityCollection.getEntity(entityId, graphPanel.getIndexParameters());
                    resultsArray.add(new KinTreeNode(entityData, graphPanel.getIndexParameters(), dialogHandler, entityCollection, dataNodeLoader));
                    rootNode.setChildNodes(resultsArray.toArray(new ArbilNode[]{}));
                    resultsTree.requestResort();
                    loadedCount++;
                    resultsArea.setText("Loaded " + loadedCount + " of " + entityIdentifiers.length + " entities\n");
                    progressBar.setValue(loadedCount);
                }
                resultsArea.setText("");
                resultsArea.setVisible(false);
                searchPanel.remove(progressBar);
                searchPanel.revalidate();
            }
        }.start();
    }

    public String[] getCurrentStrings() {
        ArrayList<String> currentStrings = new ArrayList<String>();
        if (graphSelectionCheckBox.isSelected()) {
            String kinTypeStringExtention = "";
            String prefixString = "x";
            if (expandByKinTypeCheckBox.isSelected()) {
                kinTypeStringExtention = kinTypeString;
                prefixString = "E";
            }
            for (ArbilNode arbilNode : resultsTree.getSelectedNodeArray()) { // return getSelectedNodesOfType(ArbilNode.class).toArray(new ArbilNode[]{});
                if (arbilNode instanceof KinTreeNode) {
                    currentStrings.add(prefixString + "[Entity.Identifier=" + ((KinTreeNode) arbilNode).entityData.getUniqueIdentifier().getQueryIdentifier() + "]" + kinTypeStringExtention);
                }
            }
        }
        return currentStrings.toArray(new String[]{});
    }

    public int getTotalLength() {
        if (graphSelectionCheckBox.isSelected()) {
            return resultsTree.getSelectedNodeArray().length;
        } else {
            return 0;
        }
    }

    public void highlightKinTypeStrings(ParserHighlight[] parserHighlight, String[] kinTypeStrings) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }
}
