package nl.mpi.kinnate.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.kindocument.EntityDocument;
import nl.mpi.kinnate.kindocument.EntityMerger;
import nl.mpi.kinnate.kindocument.ImportTranslator;
import nl.mpi.kinnate.kindocument.RelationLinker;
import nl.mpi.kinnate.svg.DataStoreSvg.DiagramMode;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.svg.SvgUpdateHandler;
import nl.mpi.kinnate.ui.entityprofiles.ProfileRecord;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : GraphPanelContextMenu
 * Created on : Feb 18, 2011, 11:51:00 AM
 * Author : Peter Withers
 */
public class GraphPanelContextMenu extends JPopupMenu implements ActionListener {

    private KinDiagramPanel kinDiagramPanel;
    private GraphPanel graphPanel;
    private JMenu addEntityMenu;
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
    private JMenuItem deleteMenu;
    final JSeparator jSeparator2 = new JSeparator();
    final JSeparator jSeparator3 = new JSeparator();
    private UniqueIdentifier[] selectedIdentifiers = null; // keep the selected paths as shown at the time of the menu intereaction
    private Point eventLocation;
    private ArbilDataNodeLoader dataNodeLoader;

    public GraphPanelContextMenu(KinDiagramPanel egoSelectionPanelLocal, final GraphPanel graphPanelLocal, final EntityCollection entityCollection, final ArbilWindowManager arbilWindowManager, ArbilDataNodeLoader dataNodeLoaderL, final SessionStorage sessionStorage) {
        kinDiagramPanel = egoSelectionPanelLocal;
        graphPanel = graphPanelLocal;
        this.dataNodeLoader = dataNodeLoaderL;
        if (egoSelectionPanelLocal != null) {
            final ActionListener addMenuActionListener = new java.awt.event.ActionListener() {

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
                                kinDiagramPanel.addRequiredNodes(new UniqueIdentifier[]{entityDocument.getUniqueIdentifier()}, GraphPanelContextMenu.this.eventLocation);
                            } catch (ImportException exception) {
                                kinDiagramPanel.clearProgressBar();
                                BugCatcherManager.getBugCatcher().logError(exception);
                                arbilWindowManager.addMessageDialogToQueue("Failed to create entity: " + exception.getMessage(), "Add Entity");
                            }
                        }
                    }.start();
                }
            };

            addEntityMenu = new JMenu("Add");
            addEntityMenu.addMenuListener(new MenuListener() {

                public void menuSelected(MenuEvent e) {
                    addEntityMenu.removeAll();
                    for (ProfileRecord profileRecord : graphPanelLocal.dataStoreSvg.selectedProfiles) {
                        JMenuItem addMenuItem = new JMenuItem(profileRecord.profileName);
                        addMenuItem.setActionCommand(profileRecord.profileId);
                        addMenuItem.addActionListener(addMenuActionListener);
                        addEntityMenu.add(addMenuItem);
                    }
                }

                public void menuDeselected(MenuEvent e) {
                }

                public void menuCanceled(MenuEvent e) {
                }
            });
            this.add(addEntityMenu);

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

            deleteMenu = new JMenuItem("Delete");
            deleteMenu.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    kinDiagramPanel.showProgressBar();
                    int entityCount = 0;
                    for (UniqueIdentifier uniqueIdentifier : selectedIdentifiers) {
                        if (!uniqueIdentifier.isGraphicsIdentifier() && !uniqueIdentifier.isTransientIdentifier()) {
                            entityCount++;
                        }
                    }
                    boolean doDelete = false;
                    if (entityCount == 0) {
                        doDelete = true;
                    } else if (JOptionPane.OK_OPTION == arbilWindowManager.showDialogBox(entityCount + " entities will be deleted from the database", "Delete Entity", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
                        doDelete = true;
                    }
                    if (doDelete) {
                        ArrayList<UniqueIdentifier> affectedIdentifiers = new ArrayList<UniqueIdentifier>();
                        for (UniqueIdentifier uniqueIdentifier : selectedIdentifiers) {
                            if (uniqueIdentifier.isGraphicsIdentifier()) {
                                graphPanel.svgUpdateHandler.deleteGraphics(uniqueIdentifier);
                            } else if (uniqueIdentifier.isTransientIdentifier()) {
                                kinDiagramPanel.clearProgressBar();
                                throw new UnsupportedOperationException("Cannot delete transient entities.");
                            } else {
                                affectedIdentifiers.add(uniqueIdentifier);
                            }
                        }
                        final UniqueIdentifier[] affectedIdentifiersArray = affectedIdentifiers.toArray(new UniqueIdentifier[]{});
                        try {
                            final RelationLinker relationLinker = new RelationLinker(sessionStorage, arbilWindowManager, entityCollection);
                            relationLinker.deleteEntity(affectedIdentifiersArray);
                        } catch (ImportException exception) {
                            arbilWindowManager.addMessageDialogToQueue("Failed to delete: " + exception.getMessage(), mergeEntitiesMenu.getText());
                        }
                        kinDiagramPanel.removeRequiredNodes(affectedIdentifiersArray);
                        kinDiagramPanel.removeEgoNodes(affectedIdentifiersArray);
                    }
                    kinDiagramPanel.clearProgressBar();
                }
            });
            this.add(deleteMenu);

            mergeEntitiesMenu = new JMenuItem("Merge Selected Entities");
            mergeEntitiesMenu.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    String messageString = "The selected entites will be merged,\nAll relations will be preserved and " + (selectedIdentifiers.length - 1) + " entities will be deleted.\nOnly the data of the initialy selected entity will be kept:\n\"";
                    for (String labelString : graphPanel.getEntityForElementId(selectedIdentifiers[0]).getLabel()) {
                        messageString = messageString + labelString;
                    }
                    messageString = messageString + "\"\nDo you wish to continue?";
                    if (JOptionPane.OK_OPTION == arbilWindowManager.showDialogBox(messageString, "Merge Entities", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {

                        kinDiagramPanel.showProgressBar();
                        try {
                            final EntityMerger entityMerger = new EntityMerger(sessionStorage, arbilWindowManager, entityCollection);
                            entityMerger.mergeEntities(selectedIdentifiers);
                            kinDiagramPanel.entityRelationsChanged(entityMerger.getAffectedIdentifiersArray());
                            kinDiagramPanel.removeRequiredNodes(entityMerger.getDeletedIdentifiersArray());
                        } catch (ImportException exception) {
                            arbilWindowManager.addMessageDialogToQueue("Failed to merge: " + exception.getMessage(), mergeEntitiesMenu.getText());
                        }
                        kinDiagramPanel.clearProgressBar();
                    }
                }
            });
            this.add(mergeEntitiesMenu);

            duplicateEntitiesMenu = new JMenuItem("Duplicate Selected Entities");
            duplicateEntitiesMenu.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    kinDiagramPanel.showProgressBar();
                    try {
                        final UniqueIdentifier[] duplicateEntities = new EntityMerger(sessionStorage, arbilWindowManager, entityCollection).duplicateEntities(selectedIdentifiers);
                        kinDiagramPanel.entityRelationsChanged(selectedIdentifiers);
                        kinDiagramPanel.addRequiredNodes(duplicateEntities, eventLocation);
                    } catch (ImportException exception) {
                        arbilWindowManager.addMessageDialogToQueue("Failed to duplicate: " + exception.getMessage(), duplicateEntitiesMenu.getText());
                    }
                    kinDiagramPanel.clearProgressBar();
                }
            });
            this.add(duplicateEntitiesMenu);

            addRelationEntityMenu = new JMenu("Add Relation");
            this.add(addRelationEntityMenu);
            for (RelationType relationType : RelationType.values()) {
                JMenuItem addRelationEntityMenuItem = new JMenuItem(relationType.name());
                addRelationEntityMenuItem.setActionCommand(relationType.name());
                addRelationEntityMenuItem.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        kinDiagramPanel.showProgressBar();
                        try {
                            UniqueIdentifier[] affectedIdentifiers = new RelationLinker(sessionStorage, arbilWindowManager, entityCollection).linkEntities(selectedIdentifiers, RelationType.valueOf(evt.getActionCommand()), null, null); // todo: custom relation types could be enabled here as could dcr values..
                            kinDiagramPanel.entityRelationsChanged(affectedIdentifiers);
                        } catch (ImportException exception) {
                            arbilWindowManager.addMessageDialogToQueue("Failed to create relation: " + exception.getMessage(), addRelationEntityMenu.getText());
                        }
                        kinDiagramPanel.clearProgressBar();
                    }
                });
                addRelationEntityMenu.add(addRelationEntityMenuItem);
            }

            removeRelationEntityMenu = new JMenu("Remove Relations"); // todo: if one node only then "remove all relations of this entity"
            this.add(removeRelationEntityMenu);
//            for (RelationType relationType : RelationType.values()) {
//            relationType.name()
            //todo: add a remove all relations to selection (including unselected and not shown entities)

            String actionString = "Remove relations between selected";
            JMenuItem removeRelationEntityMenuItem = new JMenuItem(actionString);
            removeRelationEntityMenuItem.setActionCommand(actionString);
            removeRelationEntityMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    kinDiagramPanel.showProgressBar();
                    try {
                        new RelationLinker(sessionStorage, arbilWindowManager, entityCollection).unlinkEntities(graphPanel, selectedIdentifiers);
                        kinDiagramPanel.entityRelationsChanged(selectedIdentifiers);
                    } catch (ImportException exception) {
                        arbilWindowManager.addMessageDialogToQueue("Failed to remove relations: " + exception.getMessage(), removeRelationEntityMenu.getText());
                    }
                    kinDiagramPanel.clearProgressBar();
                }
            });
            removeRelationEntityMenu.add(removeRelationEntityMenuItem);
//            }
            this.add(new JSeparator());
        }
        setAsEgoMenuItem = new JMenuItem("Set as Ego (list will be cleared)");
        setAsEgoMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinDiagramPanel.setEgoNodes(selectedIdentifiers); // getSelectedUriArray(),
            }
        });
        this.add(setAsEgoMenuItem);
        addAsEgoMenuItem = new JMenuItem("Add to ego list");
        addAsEgoMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinDiagramPanel.addEgoNodes(selectedIdentifiers); //getSelectedUriArray(),
            }
        });
        this.add(addAsEgoMenuItem);
        removeEgoMenuItem = new JMenuItem("Remove from ego list");
        removeEgoMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinDiagramPanel.removeEgoNodes(selectedIdentifiers);
            }
        });
        this.add(removeEgoMenuItem);
        this.add(jSeparator2);

        addAsRequiredMenuItem = new JMenuItem("Keep on diagram");
        addAsRequiredMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinDiagramPanel.addRequiredNodes(selectedIdentifiers, null); //getSelectedUriArray(),
            }
        });
        this.add(addAsRequiredMenuItem);
        removeRequiredMenuItem = new JMenuItem("Release from diagram");
        removeRequiredMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinDiagramPanel.removeRequiredNodes(selectedIdentifiers);
            }
        });
        this.add(removeRequiredMenuItem);
        this.add(jSeparator3);

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
                if (!kinDiagramPanel.verifyDiagramDataLoaded()) {
                    return;
                }
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
                kinDiagramPanel.showProgressBar();
                try {
                    arbilWindowManager.stopEditingInCurrentWindow();
                    dataNodeLoader.saveNodesNeedingSave(true);
                } catch (Exception ex) {
                    BugCatcherManager.getBugCatcher().logError(ex);
                }
                kinDiagramPanel.clearProgressBar();
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
        eventLocation = new Point(i, i1);
//        System.out.println("ContextMenu: " + i + ":" + i1);
        selectedIdentifiers = graphPanel.getSelectedIds();
        int nonTransientNodeCount = 0;
        int transientNodeCount = 0;
        int graphicsIdentifierCount = 0;
        for (UniqueIdentifier uniqueIdentifier : selectedIdentifiers) {
            // check to see if these selectedIdentifiers are transent nodes and if they are then do not allow the following menu items
            if (uniqueIdentifier.isGraphicsIdentifier()) {
                graphicsIdentifierCount++;
            } else if (uniqueIdentifier.isTransientIdentifier()) {
                transientNodeCount++;
            } else {
                nonTransientNodeCount++;
            }
        }
        boolean showNonTransientMenus;
        if (graphPanel.dataStoreSvg.diagramMode != DiagramMode.FreeForm) {
            duplicateEntitiesMenu.setEnabled(nonTransientNodeCount > 0);
            removeRelationEntityMenu.setEnabled(nonTransientNodeCount > 1);
            mergeEntitiesMenu.setEnabled(nonTransientNodeCount > 1);
            addRelationEntityMenu.setEnabled(nonTransientNodeCount > 1);
            setAsEgoMenuItem.setEnabled(nonTransientNodeCount > 0);
            addAsEgoMenuItem.setEnabled(nonTransientNodeCount > 0);
            removeEgoMenuItem.setEnabled(nonTransientNodeCount > 0); // todo: set these items based on the state of the selected entities, //graphPanel.selectionContainsEgo());
            addAsRequiredMenuItem.setEnabled(nonTransientNodeCount > 0);
            removeRequiredMenuItem.setEnabled(nonTransientNodeCount > 0);
            showNonTransientMenus = true;
        } else {
            showNonTransientMenus = false;
        }
        // hide/show the menus based on the diagram type
        removeRelationEntityMenu.setVisible(showNonTransientMenus);
        mergeEntitiesMenu.setVisible(showNonTransientMenus);
        duplicateEntitiesMenu.setVisible(showNonTransientMenus);
        addRelationEntityMenu.setVisible(showNonTransientMenus);
        setAsEgoMenuItem.setVisible(showNonTransientMenus);
        addAsEgoMenuItem.setVisible(showNonTransientMenus);
        removeEgoMenuItem.setVisible(showNonTransientMenus);
        addAsRequiredMenuItem.setVisible(showNonTransientMenus);
        removeRequiredMenuItem.setVisible(showNonTransientMenus);
        addEntityMenu.setVisible(showNonTransientMenus);
        jSeparator2.setVisible(showNonTransientMenus);
        jSeparator3.setVisible(showNonTransientMenus);

        deleteMenu.setEnabled(nonTransientNodeCount + graphicsIdentifierCount > 0 && transientNodeCount == 0);
        saveFileMenuItem.setEnabled(dataNodeLoader.nodesNeedSave());

        super.show(cmpnt, i, i1);
    }

    public void actionPerformed(ActionEvent e) {
        if (!kinDiagramPanel.verifyDiagramDataLoaded()) {
            return;
        }
        kinDiagramPanel.showProgressBar();
        graphPanel.svgUpdateHandler.addGraphics(SvgUpdateHandler.GraphicsTypes.valueOf(e.getActionCommand()), eventLocation);
        kinDiagramPanel.clearProgressBar();
    }
}
