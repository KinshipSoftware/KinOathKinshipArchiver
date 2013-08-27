/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
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
import javax.swing.SwingUtilities;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ContainerNode;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.data.KinTreeNode;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kintypestrings.ParserHighlight;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : EntitySearchPanel Created on : Mar 14, 2011, 4:01:11 PM
 *
 * @author Peter Withers
 */
public class EntitySearchPanel extends JPanel implements KinTypeStringProvider {

    private EntityCollection entityCollection;
    private KinTree resultsTree;
    private JTextArea resultsArea = new JTextArea();
    private JCheckBox graphSelectionCheckBox;
    private JCheckBox expandByKinTypeCheckBox;
    private JCheckBox diagramSelectionCheckBox;
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
        InitPanel(entityCollection, kinDiagramPanel, graphPanel, dialogHandler, dataNodeLoader, java.util.ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Widgets").getString("SEARCH ENTITY NAMES"), null);
    }

    private void InitPanel(EntityCollection entityCollection, final KinDiagramPanel kinDiagramPanel, GraphPanel graphPanel, MessageDialogHandler dialogHandler, ArbilDataNodeLoader dataNodeLoader, String nodeSetTitle, UniqueIdentifier[] entityIdentifiers) {
        this.entityCollection = entityCollection;
        this.graphPanel = graphPanel;
        this.dialogHandler = dialogHandler;
        this.dataNodeLoader = dataNodeLoader;
        this.setLayout(new BorderLayout());
        rootNode = new ContainerNode(null, "results", null, new ArbilNode[]{});
        resultsTree = new KinTree(kinDiagramPanel, graphPanel, rootNode);
//        resultsTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Test Tree"), true));
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
        searchButton = new JButton(java.util.ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Widgets").getString("SEARCH"));
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
        graphSelectionCheckBox = new JCheckBox(java.util.ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Widgets").getString("GRAPH SELECTION"), true);
        resultsTree.setUpdateGraphOnSelectionChange(true);
        // graph the selection when checked
        optionsPanel.add(graphSelectionCheckBox);
        expandByKinTypeCheckBox = new JCheckBox(java.util.ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Widgets").getString("EXPAND SELECTION BY KIN TYPE STRING"), false);
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
                            kinDiagramPanel.drawGraph(true);
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
                kinDiagramPanel.drawGraph(true);
                resultsTree.setUpdateGraphOnSelectionChange(graphSelectionCheckBox.isSelected());
            }
        });
        kinTypeStringTextArea.setEnabled(expandByKinTypeCheckBox.isSelected() && graphSelectionCheckBox.isSelected());
        expandByKinTypeCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                kinTypeStringTextArea.setEnabled(expandByKinTypeCheckBox.isSelected());
                kinDiagramPanel.drawGraph(true);
            }
        });
        diagramSelectionCheckBox = new JCheckBox(java.util.ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Widgets").getString("REPLACE SEARCH WITH DIAGRAM SELECTION"), false);
        // todo: link this selection when checked
        // todo: consider if this spring graph action is best in the search tree or the diagram tree and enable it
//        optionsPanel.add(diagramSelectionCheckBox);
        // todo: add button to make transient entities permanent.
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
                try {
                    ArrayList<ArbilNode> resultsArray = new ArrayList<ArbilNode>();
                    EntityData[] searchResults = entityCollection.getEntityByKeyWord(searchField.getText(), graphPanel.getIndexParameters());
                    resultsArea.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Widgets").getString("FOUND {0} ENTITIES\N"), new Object[] {searchResults.length}));
                    for (EntityData entityData : searchResults) {
//            if (resultsArray.size() < 1000) {
                        // todo: add cache and update of the tree nodes
                        resultsArray.add(new KinTreeNode(graphPanel.getSymbolGraphic(), entityData.getUniqueIdentifier(), entityData, graphPanel.dataStoreSvg, graphPanel.getIndexParameters(), dialogHandler, entityCollection, dataNodeLoader));
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
                } catch (EntityServiceException exception) {
                    dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Perform Search");
                }
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
                resultsArea.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Widgets").getString("LOADING {0} ENTITIES\N"), new Object[] {entityIdentifiers.length}));
                int loadedCount = 0;
                int unloadableEntityCount = 0;
                for (UniqueIdentifier entityId : entityIdentifiers) {
                    try {
                        EntityData entityData = entityCollection.getEntity(entityId, graphPanel.getIndexParameters());
                        // todo: add cache and update of the tree nodes
                        resultsArray.add(new KinTreeNode(graphPanel.getSymbolGraphic(), entityData.getUniqueIdentifier(), entityData, graphPanel.dataStoreSvg, graphPanel.getIndexParameters(), dialogHandler, entityCollection, dataNodeLoader));
                    } catch (EntityServiceException exception) {
                        unloadableEntityCount++;
                    }
                    rootNode.setChildNodes(resultsArray.toArray(new ArbilNode[]{}));
                    resultsTree.requestResort();
                    loadedCount++;
                    resultsArea.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Widgets").getString("LOADED {0} OF {1} ENTITIES\N"), new Object[] {loadedCount, entityIdentifiers.length}));
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            progressBar.setValue(progressBar.getValue() + 1);
                        }
                    });
                }
                resultsArea.setText("");
                resultsArea.setVisible(false);
                searchPanel.remove(progressBar);
                searchPanel.revalidate();
                if (unloadableEntityCount > 0) {
                    dialogHandler.addMessageDialogToQueue("Failed to load " + unloadableEntityCount + " entities.", "Error Loading Entities");
                }
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
                    for (String kinTypeStringItem : kinTypeStringExtention.split(",")) {
                        currentStrings.add(prefixString + "[Entity.Identifier=" + ((KinTreeNode) arbilNode).getUniqueIdentifier().getQueryIdentifier() + "]" + kinTypeStringItem);
                    }
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
