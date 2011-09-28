package nl.mpi.kinnate.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.kindocument.RelationLinker;
import nl.mpi.kinnate.kindocument.EntityDocument;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.kindocument.ImportTranslator;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.kindocument.EntityMerger;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.svg.GraphPanelSize;
import nl.mpi.kinnate.svg.SvgUpdateHandler;

/**
 *  Document   : GraphPanelContextMenu
 *  Created on : Feb 18, 2011, 11:51:00 AM
 *  Author     : Peter Withers
 */
public class GraphPanelContextMenu extends JPopupMenu implements ActionListener {

    private KinDiagramPanel kinDiagramPanel;
    private GraphPanel graphPanel;
    private GraphPanelSize graphPanelSize;
    private JMenuItem duplicateEntitiesMenu;
    private JMenuItem mergeEntitiesMenu;
    private JMenuItem addRelationEntityMenu;
    private JMenuItem removeRelationEntityMenu;
    private JMenuItem setAsEgoMenuItem;
    private JMenuItem addAsEgoMenuItem;
    private JMenuItem removeEgoMenuItem;
    private JMenuItem addAsRequiredMenuItem;
    private JMenuItem removeRequiredMenuItem;
    private JMenuItem saveFileMenuItem;
    private JCheckBoxMenuItem snapToGridMenuItem;
    private JCheckBoxMenuItem showBorderMenuItem;
    private JCheckBoxMenuItem highlightRelationsMenuItem;
    private JCheckBoxMenuItem showKinTermLinesMenuItem;
    private JCheckBoxMenuItem showSanguineLinesMenuItem;
    private JCheckBoxMenuItem showLabelssMenuItem;
    private JCheckBoxMenuItem showKinTypeLabelssMenuItem;
    private JCheckBoxMenuItem showKinTermLabelssMenuItem;
    private JCheckBoxMenuItem showArchiveLinksMenuItem;
//    private JCheckBoxMenuItem showResourceLinksMenuItem;
    private UniqueIdentifier[] selectedIdentifiers = null; // keep the selected paths as shown at the time of the menu intereaction
    private float xPos;
    private float yPos;

    public GraphPanelContextMenu(KinDiagramPanel egoSelectionPanelLocal, GraphPanel graphPanelLocal, GraphPanelSize graphPanelSizeLocal) {
        kinDiagramPanel = egoSelectionPanelLocal;
        graphPanel = graphPanelLocal;
        graphPanelSize = graphPanelSizeLocal;
        if (egoSelectionPanelLocal != null) {
            JMenuItem addEntityMenuItem = new JMenuItem("Add Entity");
//            addEntityMenuItem.setActionCommand(GraphPanelContextMenu.class.getResource("/xsd/StandardEntity.xsd").toString());
            addEntityMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    // todo: node type is not used here anymore but could still be useful as a user option
//                    String nodeType = evt.getActionCommand();
                    EntityDocument entityDocument = new EntityDocument(ArbilSessionStorage.getSingleInstance().getCacheDirectory(), null, new ImportTranslator(true));
                    try {
                        entityDocument.createDocument(true);
                        entityDocument.insertDefaultMetadata();
                        entityDocument.saveDocument();
                        URI addedEntityUri = entityDocument.getFile().toURI();
                        new EntityCollection().updateDatabase(addedEntityUri);
                        kinDiagramPanel.addRequiredNodes(new UniqueIdentifier[]{entityDocument.getUniqueIdentifier()});
                    } catch (ImportException exception) {
                        // todo: warn user with a dialog
                        new ArbilBugCatcher().logError(exception);
//                    } catch (URISyntaxException ex) {
//                        new ArbilBugCatcher().logError(ex);
                        // todo: warn user with a dialog
                    }
                }
            });
            this.add(addEntityMenuItem);

            duplicateEntitiesMenu = new JMenuItem("Duplicate Selected Entities");
            duplicateEntitiesMenu.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        final UniqueIdentifier[] duplicateEntities = new EntityMerger().duplicateEntities(selectedIdentifiers);
                        kinDiagramPanel.entityRelationsChanged(selectedIdentifiers);
                        kinDiagramPanel.addRequiredNodes(duplicateEntities);
                    } catch (ImportException exception) {
                        ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Failed to duplicate: " + exception.getMessage(), duplicateEntitiesMenu.getText());
                    }
                }
            });
            this.add(duplicateEntitiesMenu);

            mergeEntitiesMenu = new JMenuItem("Merge Selected Entities");
            mergeEntitiesMenu.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        UniqueIdentifier[] affectedIdentifiers = new EntityMerger().mergeEntities(selectedIdentifiers);
                        kinDiagramPanel.entityRelationsChanged(affectedIdentifiers);
                    } catch (ImportException exception) {
                        ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Failed to merge: " + exception.getMessage(), mergeEntitiesMenu.getText());
                    }
                }
            });
            this.add(mergeEntitiesMenu);

            addRelationEntityMenu = new JMenu("Add Relation");
            this.add(addRelationEntityMenu);
            for (RelationType relationType : RelationType.values()) {
                JMenuItem addRelationEntityMenuItem = new JMenuItem(relationType.name());
                addRelationEntityMenuItem.setActionCommand(relationType.name());
                addRelationEntityMenuItem.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        try {
                            UniqueIdentifier[] affectedIdentifiers = new RelationLinker().linkEntities(selectedIdentifiers, RelationType.valueOf(evt.getActionCommand()));
                            kinDiagramPanel.entityRelationsChanged(affectedIdentifiers);
                        } catch (ImportException exception) {
                            ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Failed to create relation: " + exception.getMessage(), addRelationEntityMenu.getText());
                        }
                    }
                });
                addRelationEntityMenu.add(addRelationEntityMenuItem);
            }

            removeRelationEntityMenu = new JMenu("Remove Relations"); // todo: if one node only then "remove all relations of this entity"
            this.add(removeRelationEntityMenu);
//            for (RelationType relationType : RelationType.values()) {
//            relationType.name()
            String actionString = "Remove Relations to Lead Selection";
            JMenuItem removeRelationEntityMenuItem = new JMenuItem(actionString);
            removeRelationEntityMenuItem.setActionCommand(actionString);
            removeRelationEntityMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    new RelationLinker().unlinkEntities(graphPanel, selectedIdentifiers);
                    kinDiagramPanel.entityRelationsChanged(selectedIdentifiers);
                }
            });
            removeRelationEntityMenu.add(removeRelationEntityMenuItem);
//            }

            JMenu shapeSubMenu = new JMenu("Add Geometry");
            for (SvgUpdateHandler.GraphicsTypes graphicsType : SvgUpdateHandler.GraphicsTypes.values()) {
                JMenuItem addLabel = new JMenuItem("Add " + graphicsType.name());
                addLabel.setActionCommand(graphicsType.name());
                shapeSubMenu.add(addLabel);
                if (SvgUpdateHandler.GraphicsTypes.Polyline.equals(graphicsType)) {
                    addLabel.setEnabled(false);
                }
                addLabel.addActionListener(GraphPanelContextMenu.this);
                // todo: addthese into a layer behind the entities, athought lables could be above
                // todo: when geometry is selected construct an arbildatanode to allow the geometries attributes to be edited
            }
            this.add(shapeSubMenu);
        }
        setAsEgoMenuItem = new JMenuItem("Set as Ego (relacing existing)");
        setAsEgoMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinDiagramPanel.setEgoNodes(selectedIdentifiers); // getSelectedUriArray(),
            }
        });
        this.add(setAsEgoMenuItem);
        addAsEgoMenuItem = new JMenuItem("Add as Ego");
        addAsEgoMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinDiagramPanel.addEgoNodes(selectedIdentifiers); //getSelectedUriArray(),
            }
        });
        this.add(addAsEgoMenuItem);
        removeEgoMenuItem = new JMenuItem("Remove Ego");
        removeEgoMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinDiagramPanel.removeEgoNodes(selectedIdentifiers);
            }
        });
        this.add(removeEgoMenuItem);
        addAsRequiredMenuItem = new JMenuItem("Set as required");
        addAsRequiredMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinDiagramPanel.addRequiredNodes(selectedIdentifiers); //getSelectedUriArray(),
            }
        });
        this.add(addAsRequiredMenuItem);
        removeRequiredMenuItem = new JMenuItem("Remove requirement");
        removeRequiredMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinDiagramPanel.removeRequiredNodes(selectedIdentifiers);
            }
        });
        this.add(removeRequiredMenuItem);
        JMenuItem resetZoomMenuItem = new JMenuItem("Reset Zoom");
        resetZoomMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphPanel.resetZoom();
            }
        });
        this.add(resetZoomMenuItem);

        JMenuItem resetLayoutMenuItem = new JMenuItem("Reset Layout");
        resetLayoutMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphPanel.resetLayout();
            }
        });
        this.add(resetLayoutMenuItem);

        snapToGridMenuItem = new JCheckBoxMenuItem("Snap To Grid");
        snapToGridMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphPanel.dataStoreSvg.snapToGrid = !graphPanel.dataStoreSvg.snapToGrid;
            }
        });
        this.add(snapToGridMenuItem);

        highlightRelationsMenuItem = new JCheckBoxMenuItem("Highlight Selected Relations");
        highlightRelationsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphPanel.dataStoreSvg.highlightRelationLines = !graphPanel.dataStoreSvg.highlightRelationLines;
                graphPanel.drawNodes();
            }
        });
        this.add(highlightRelationsMenuItem);

        showBorderMenuItem = new JCheckBoxMenuItem("Show Diagram Border");
        showBorderMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphPanel.dataStoreSvg.showDiagramBorder = !graphPanel.dataStoreSvg.showDiagramBorder;
                graphPanel.drawNodes();
            }
        });
        this.add(showBorderMenuItem);

        JMenu diagramSizeMenuItem = new JMenu("Diagram Size");
        for (String currentString : graphPanelSize.getPreferredSizes()) {
            JMenuItem currentMenuItem = new JMenuItem(currentString);
            currentMenuItem.setActionCommand(currentString);
            currentMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setGraphPanelSize(evt.getActionCommand());
                }
            });
            diagramSizeMenuItem.add(currentMenuItem);
        }
        this.add(diagramSizeMenuItem);
        showSanguineLinesMenuItem = new JCheckBoxMenuItem("Show Sanguin Lines");
        showSanguineLinesMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Hide/Show Sanguin Lines on the graph
                graphPanel.dataStoreSvg.showSanguineLines = !graphPanel.dataStoreSvg.showSanguineLines;
                graphPanel.drawNodes();
            }
        });
        this.add(showSanguineLinesMenuItem);
        showKinTermLinesMenuItem = new JCheckBoxMenuItem("Show Kin Term Lines");
        showKinTermLinesMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //  Hide/Show Kin Term lines on the graph
                graphPanel.dataStoreSvg.showKinTermLines = !graphPanel.dataStoreSvg.showKinTermLines;
                graphPanel.drawNodes();
            }
        });
        this.add(showKinTermLinesMenuItem);
        showLabelssMenuItem = new JCheckBoxMenuItem("Show Labels");
        showLabelssMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Hide/Show labels on the graph
                graphPanel.dataStoreSvg.showLabels = !graphPanel.dataStoreSvg.showLabels;
                graphPanel.drawNodes();
            }
        });
        this.add(showLabelssMenuItem);

        showKinTypeLabelssMenuItem = new JCheckBoxMenuItem("Show Kin Type Labels");
        showKinTypeLabelssMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Hide/Show Kin type labels on the graph
                graphPanel.dataStoreSvg.showKinTypeLabels = !graphPanel.dataStoreSvg.showKinTypeLabels;
                graphPanel.drawNodes();
            }
        });
        this.add(showKinTypeLabelssMenuItem);
        showKinTermLabelssMenuItem = new JCheckBoxMenuItem("Show Kin Term Labels");
        showKinTermLabelssMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Hide/Show Kin Terms on the graph
                graphPanel.dataStoreSvg.showKinTermLabels = !graphPanel.dataStoreSvg.showKinTermLabels;
                graphPanel.drawNodes();
            }
        });
        this.add(showKinTermLabelssMenuItem);
        showArchiveLinksMenuItem = new JCheckBoxMenuItem("Show Archive Links");
        showArchiveLinksMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphPanel.dataStoreSvg.showArchiveLinks = !graphPanel.dataStoreSvg.showArchiveLinks;
                graphPanel.drawNodes();
            }
        });
        this.add(showArchiveLinksMenuItem);
//        showResourceLinksMenuItem = new JCheckBoxMenuItem("Show Archive Resource Links");
//        showResourceLinksMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                graphPanel.dataStoreSvg.showResourceLinks = !graphPanel.dataStoreSvg.showResourceLinks;
//                graphPanel.drawNodes();
//            }
//        });
//        this.add(showResourceLinksMenuItem);
        JMenuItem searchEntityServiceMenuItem = new JMenuItem("Search Entity Service");
        searchEntityServiceMenuItem.setToolTipText("Search the entity database for entities matching the current kin terms and populate he diagram with the results");
        searchEntityServiceMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // todo: Search the entity database for entities matching the current kin terms and populate he diagram with the results
            }
        });
        this.add(searchEntityServiceMenuItem);

        saveFileMenuItem = new JMenuItem();
        saveFileMenuItem.setText("Save All Metadata Changes");
        saveFileMenuItem.setEnabled(false);
        saveFileMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    ArbilWindowManager.getSingleInstance().stopEditingInCurrentWindow();
                    ArbilDataNodeLoader.getSingleInstance().saveNodesNeedingSave(true);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        this.add(saveFileMenuItem);
    }

    private URI[] getSelectedUriArray() {
        URI[] selectedUriArray = new URI[selectedIdentifiers.length];
        for (int currentIndex = 0; currentIndex < selectedIdentifiers.length; currentIndex++) {
            try {
                selectedUriArray[currentIndex] = new URI(graphPanel.getPathForElementId(selectedIdentifiers[currentIndex]));
            } catch (URISyntaxException ex) {
                new ArbilBugCatcher().logError(ex);
                // todo: warn user with a dialog
            }
        }
        return selectedUriArray;
    }

    private void setGraphPanelSize(String sizeString) {
        graphPanelSize.setSize(sizeString);
        graphPanel.drawNodes();
    }

    @Override
    public void show(Component cmpnt, int i, int i1) {
        xPos = cmpnt.getMousePosition().x;
        yPos = cmpnt.getMousePosition().y;
        selectedIdentifiers = graphPanel.getSelectedIds();
        int nonTransientNodeCount = 0;
        for (UniqueIdentifier uniqueIdentifier : selectedIdentifiers) {
            // check to see if these selectedIdentifiers are transent nodes and if they are then do not allow the following menu items
            if (!uniqueIdentifier.isTransientIdentifier() && !uniqueIdentifier.isGraphicsIdentifier()) {
                nonTransientNodeCount++;
            }
        }
        if (addRelationEntityMenu != null) {
            // todo: consider using disable rather than visible
            duplicateEntitiesMenu.setVisible(nonTransientNodeCount > 0);
            mergeEntitiesMenu.setVisible(nonTransientNodeCount > 1);
            addRelationEntityMenu.setVisible(nonTransientNodeCount > 1);
            setAsEgoMenuItem.setVisible(nonTransientNodeCount > 0);
            addAsEgoMenuItem.setVisible(nonTransientNodeCount > 0);
            removeEgoMenuItem.setVisible(nonTransientNodeCount > 0); // todo: set these items based on the state of the selected entities, //graphPanel.selectionContainsEgo());
            addAsRequiredMenuItem.setVisible(nonTransientNodeCount > 0);
            removeRequiredMenuItem.setVisible(nonTransientNodeCount > 0);
        } else {
            setAsEgoMenuItem.setVisible(false);
            addAsEgoMenuItem.setVisible(false);
            removeEgoMenuItem.setVisible(false);
            addAsRequiredMenuItem.setVisible(false);
            removeRequiredMenuItem.setVisible(false);
        }
        snapToGridMenuItem.setSelected(graphPanel.dataStoreSvg.snapToGrid);
        highlightRelationsMenuItem.setSelected(graphPanel.dataStoreSvg.highlightRelationLines);
        showBorderMenuItem.setSelected(graphPanel.dataStoreSvg.showDiagramBorder);
        showSanguineLinesMenuItem.setSelected(graphPanel.dataStoreSvg.showSanguineLines);
        showKinTermLinesMenuItem.setSelected(graphPanel.dataStoreSvg.showKinTermLines);
        showLabelssMenuItem.setSelected(graphPanel.dataStoreSvg.showLabels);
        showKinTypeLabelssMenuItem.setSelected(graphPanel.dataStoreSvg.showKinTypeLabels);
        showKinTermLabelssMenuItem.setSelected(graphPanel.dataStoreSvg.showKinTermLabels);
        showArchiveLinksMenuItem.setSelected(graphPanel.dataStoreSvg.showArchiveLinks);
//        showResourceLinksMenuItem.setSelected(graphPanel.dataStoreSvg.showResourceLinks);
        saveFileMenuItem.setEnabled(ArbilDataNodeLoader.getSingleInstance().nodesNeedSave());
        super.show(cmpnt, i, i1);
    }

    public void actionPerformed(ActionEvent e) {
        graphPanel.svgUpdateHandler.addGraphics(SvgUpdateHandler.GraphicsTypes.valueOf(e.getActionCommand()), xPos, yPos);
    }
}
