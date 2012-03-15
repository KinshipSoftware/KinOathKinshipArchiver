package nl.mpi.kinnate.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.kindocument.RelationLinker;
import nl.mpi.kinnate.kindocument.EntityDocument;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.kindocument.ImportTranslator;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.kindocument.EntityMerger;
import nl.mpi.kinnate.svg.DataStoreSvg.DiagramMode;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.svg.SvgUpdateHandler;

/**
 *  Document   : GraphPanelContextMenu
 *  Created on : Feb 18, 2011, 11:51:00 AM
 *  Author     : Peter Withers
 */
public class GraphPanelContextMenu extends JPopupMenu implements ActionListener {

    private KinDiagramPanel kinDiagramPanel;
    private GraphPanel graphPanel;
    private JMenuItem addEntityMenuItem;
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
    private UniqueIdentifier[] selectedIdentifiers = null; // keep the selected paths as shown at the time of the menu intereaction
    private float xPos;
    private float yPos;
    private ArbilDataNodeLoader dataNodeLoader;

    public GraphPanelContextMenu(KinDiagramPanel egoSelectionPanelLocal, GraphPanel graphPanelLocal, final EntityCollection entityCollection, final ArbilWindowManager arbilWindowManager, ArbilDataNodeLoader dataNodeLoaderL, final SessionStorage sessionStorage) {
        kinDiagramPanel = egoSelectionPanelLocal;
        graphPanel = graphPanelLocal;
        this.dataNodeLoader = dataNodeLoaderL;
        if (egoSelectionPanelLocal != null) {
            addEntityMenuItem = new JMenuItem("Add Entity");
            addEntityMenuItem.setActionCommand(EntityDocument.defaultEntityType);
            addEntityMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    new Thread() {

                        @Override
                        public void run() {
                            // node type will be used to determine the schema used from the diagram options
                            kinDiagramPanel.showProgressBar();
                            String nodeType = evt.getActionCommand();
                            try {
                                EntityDocument entityDocument = new EntityDocument(nodeType, new ImportTranslator(true), sessionStorage);
                                entityDocument.saveDocument();
                                URI addedEntityUri = entityDocument.getFile().toURI();
                                entityCollection.updateDatabase(addedEntityUri);
                                kinDiagramPanel.addRequiredNodes(new UniqueIdentifier[]{entityDocument.getUniqueIdentifier()});
                            } catch (ImportException exception) {
                                BugCatcherManager.getBugCatcher().logError(exception);
                                arbilWindowManager.addMessageDialogToQueue("Failed to create entity: " + exception.getMessage(), "Add Entity");
                            }
                        }
                    }.start();
                }
            });
            this.add(addEntityMenuItem);

            duplicateEntitiesMenu = new JMenuItem("Duplicate Selected Entities");
            duplicateEntitiesMenu.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        final UniqueIdentifier[] duplicateEntities = new EntityMerger(sessionStorage, arbilWindowManager, entityCollection).duplicateEntities(selectedIdentifiers);
                        kinDiagramPanel.entityRelationsChanged(selectedIdentifiers);
                        kinDiagramPanel.addRequiredNodes(duplicateEntities);
                    } catch (ImportException exception) {
                        arbilWindowManager.addMessageDialogToQueue("Failed to duplicate: " + exception.getMessage(), duplicateEntitiesMenu.getText());
                    }
                }
            });
            this.add(duplicateEntitiesMenu);

            mergeEntitiesMenu = new JMenuItem("Merge Selected Entities");
            mergeEntitiesMenu.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        UniqueIdentifier[] affectedIdentifiers = new EntityMerger(sessionStorage, arbilWindowManager, entityCollection).mergeEntities(selectedIdentifiers);
                        kinDiagramPanel.entityRelationsChanged(affectedIdentifiers);
                    } catch (ImportException exception) {
                        arbilWindowManager.addMessageDialogToQueue("Failed to merge: " + exception.getMessage(), mergeEntitiesMenu.getText());
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
                            UniqueIdentifier[] affectedIdentifiers = new RelationLinker(sessionStorage, arbilWindowManager, entityCollection).linkEntities(selectedIdentifiers, RelationType.valueOf(evt.getActionCommand()), null, null); // todo: custom relation types could be enabled here as could dcr values..
                            kinDiagramPanel.entityRelationsChanged(affectedIdentifiers);
                        } catch (ImportException exception) {
                            arbilWindowManager.addMessageDialogToQueue("Failed to create relation: " + exception.getMessage(), addRelationEntityMenu.getText());
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
                    new RelationLinker(sessionStorage, arbilWindowManager, entityCollection).unlinkEntities(graphPanel, selectedIdentifiers);
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

        JMenuItem searchEntityServiceMenuItem = new JMenuItem("Search Entity Service");
        searchEntityServiceMenuItem.setToolTipText("Search the entity database for entities matching the current kin terms and populate he diagram with the results");
        searchEntityServiceMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // todo: Search the entity database for entities matching the current kin terms and populate he diagram with the results
            }
        });
        searchEntityServiceMenuItem.setEnabled(false);
        this.add(searchEntityServiceMenuItem);

        saveFileMenuItem = new JMenuItem();
        saveFileMenuItem.setText("Save All Data Changes");
        saveFileMenuItem.setEnabled(false);
        saveFileMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    arbilWindowManager.stopEditingInCurrentWindow();
                    dataNodeLoader.saveNodesNeedingSave(true);
                } catch (Exception ex) {
                    BugCatcherManager.getBugCatcher().logError(ex);
                }
            }
        });
        this.add(saveFileMenuItem);
    }

//    private URI[] getSelectedUriArray() {
//        URI[] selectedUriArray = new URI[selectedIdentifiers.length];
//        for (int currentIndex = 0; currentIndex < selectedIdentifiers.length; currentIndex++) {
//            try {
//                selectedUriArray[currentIndex] = new URI(graphPanel.getPathForElementId(selectedIdentifiers[currentIndex]));
//            } catch (URISyntaxException ex) {
//                bugCatcher.logError(ex);
//                // todo: warn user with a dialog
//            }
//        }
//        return selectedUriArray;
//    }
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
        saveFileMenuItem.setEnabled(dataNodeLoader.nodesNeedSave());

        // enable/disable the menus based on the diagram type
        addEntityMenuItem.setEnabled(graphPanel.dataStoreSvg.diagramMode != DiagramMode.FreeForm);
        removeRelationEntityMenu.setEnabled(graphPanel.dataStoreSvg.diagramMode != DiagramMode.FreeForm);

        super.show(cmpnt, i, i1);
    }

    public void actionPerformed(ActionEvent e) {
        graphPanel.svgUpdateHandler.addGraphics(SvgUpdateHandler.GraphicsTypes.valueOf(e.getActionCommand()), xPos, yPos);
    }
}
