/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for
 * Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.ResourceBundle;
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
import nl.mpi.kinnate.entityindexer.EntityServiceException;
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
 * Created on : Feb 18, 2011, 11:51:00 AM
 *
 * @author Peter Withers
 */
public class GraphPanelContextMenu extends JPopupMenu implements ActionListener {

    private static final ResourceBundle menus = ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Menus");
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
                    if (!kinDiagramPanel.verifyDiagramDataLoaded()) {
                        return;
                    }
                    new Thread() {
                        @Override
                        public void run() {
                            // node type will be used to determine the schema used from the diagram options
                            kinDiagramPanel.showProgressBar();
                            String nodeType = evt.getActionCommand();
                            try {
                                EntityDocument entityDocument = new EntityDocument(nodeType, new ImportTranslator(true), sessionStorage, entityCollection.getProjectRecord());
                                entityDocument.saveDocument();
                                URI addedEntityUri = entityDocument.getFile().toURI();
                                entityCollection.updateDatabase(entityDocument.getFile().toURI(), entityDocument.getUniqueIdentifier());
                                kinDiagramPanel.addRequiredNodes(new UniqueIdentifier[]{entityDocument.getUniqueIdentifier()}, GraphPanelContextMenu.this.eventLocation);
                            } catch (ImportException exception) {
                                kinDiagramPanel.clearProgressBar();
                                BugCatcherManager.getBugCatcher().logError(exception);
                                final String message = java.text.MessageFormat.format(menus.getString("FAILED TO CREATE ENTITY: {0}"), new Object[]{exception.getMessage()});
                                arbilWindowManager.addMessageDialogToQueue(message, menus.getString("ADD ENTITY"));
                            } catch (EntityServiceException exception) {
                                BugCatcherManager.getBugCatcher().logError(exception);
                                arbilWindowManager.addMessageDialogToQueue(exception.getMessage(), "Update Database");
                            }
                        }
                    }.start();
                }
            };

            addEntityMenu = new JMenu(menus.getString("ADD"));
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

            JMenu shapeSubMenu = new JMenu(menus.getString("ADD GEOMETRY"));
            for (SvgUpdateHandler.GraphicsTypes graphicsType : SvgUpdateHandler.GraphicsTypes.values()) {
                JMenuItem addLabel = new JMenuItem(java.text.MessageFormat.format(menus.getString("ADD {0}"), new Object[]{graphicsType.name()}));
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

            deleteMenu = new JMenuItem(menus.getString("DELETE SELECTED FROM PROJECT"));
            deleteMenu.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    int entityCount = 0;
                    for (UniqueIdentifier uniqueIdentifier : selectedIdentifiers) {
                        if (!uniqueIdentifier.isGraphicsIdentifier() && !uniqueIdentifier.isTransientIdentifier()) {
                            entityCount++;
                        }
                    }
                    boolean doDelete = false;
                    if (entityCount == 0) {
                        doDelete = true;
                    } else if (JOptionPane.OK_OPTION == arbilWindowManager.showDialogBox(entityCount + menus.getString(" ENTITIES WILL BE DELETED FROM THE CURRENT PROJECT"), menus.getString("DELETE ENTITY"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
                        doDelete = true;
                    }
                    if (doDelete) {
                        new Thread(new Runnable() {
                            public void run() {
                                kinDiagramPanel.showProgressBar();
                                ArrayList<UniqueIdentifier> affectedIdentifiers = new ArrayList<UniqueIdentifier>();
                                for (UniqueIdentifier uniqueIdentifier : selectedIdentifiers) {
                                    if (uniqueIdentifier.isGraphicsIdentifier()) {
                                        graphPanel.svgUpdateHandler.deleteGraphics(uniqueIdentifier);
                                    } else if (uniqueIdentifier.isTransientIdentifier()) {
                                        kinDiagramPanel.clearProgressBar();
                                        throw new UnsupportedOperationException(menus.getString("CANNOT DELETE TRANSIENT ENTITIES."));
                                    } else {
                                        affectedIdentifiers.add(uniqueIdentifier);
                                    }
                                }
                                final UniqueIdentifier[] affectedIdentifiersArray = affectedIdentifiers.toArray(new UniqueIdentifier[]{});
                                try {
                                    final RelationLinker relationLinker = new RelationLinker(sessionStorage, arbilWindowManager, entityCollection);
                                    relationLinker.deleteEntity(affectedIdentifiersArray);
                                } catch (ImportException exception) {
                                    final String message = java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Menus").getString("FAILED TO DELETE: {0}"), new Object[]{exception.getMessage()});
                                    arbilWindowManager.addMessageDialogToQueue(message, mergeEntitiesMenu.getText());
                                }
                                kinDiagramPanel.removeRequiredNodes(affectedIdentifiersArray);
                                kinDiagramPanel.removeEgoNodes(affectedIdentifiersArray);
                                kinDiagramPanel.clearProgressBar();
                            }
                        }).start();
                    }
                }
            });
            this.add(deleteMenu);

            mergeEntitiesMenu = new JMenuItem(menus.getString("MERGE SELECTED ENTITIES"));
            mergeEntitiesMenu.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    String messageString = java.text.MessageFormat.format(menus.getString("THE SELECTED ENTITES WILL BE MERGED,ALL RELATIONS WILL BE PRESERVED AND {0} ENTITIES WILL BE DELETED."), new Object[]{(selectedIdentifiers.length - 1)});
                    messageString = messageString + menus.getString("DO YOU WISH TO CONTINUE?");
                    if (JOptionPane.OK_OPTION == arbilWindowManager.showDialogBox(messageString, menus.getString("MERGE ENTITIES"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
                        new Thread(new Runnable() {
                            public void run() {
                                kinDiagramPanel.showProgressBar();
                                try {
                                    final EntityMerger entityMerger = new EntityMerger(sessionStorage, arbilWindowManager, entityCollection);
                                    final UniqueIdentifier leadMergedEntity = entityMerger.mergeEntities(selectedIdentifiers);
                                    dataNodeLoader.requestReload(dataNodeLoader.getArbilDataNode(null, leadMergedEntity.getFileInProject(
                                            kinDiagramPanel.getEntityCollection().getProjectRecord()).toURI()));
                                    kinDiagramPanel.entityRelationsChanged(entityMerger.getAffectedIdentifiersArray());
                                    kinDiagramPanel.removeRequiredNodes(entityMerger.getDeletedIdentifiersArray());
                                } catch (ImportException exception) {
                                    arbilWindowManager.addMessageDialogToQueue(java.text.MessageFormat.format(menus.getString("FAILED TO MERGE: {0}"), new Object[]{exception.getMessage()}), mergeEntitiesMenu.getText());
                                } catch (EntityServiceException exception) {
                                    arbilWindowManager.addMessageDialogToQueue(java.text.MessageFormat.format(menus.getString("FAILED TO MERGE: {0}"), new Object[]{exception.getMessage()}), mergeEntitiesMenu.getText());
                                }
                                kinDiagramPanel.clearProgressBar();
                            }
                        }).start();
                    }
                }
            });
            this.add(mergeEntitiesMenu);

            duplicateEntitiesMenu = new JMenuItem(menus.getString("DUPLICATE SELECTED ENTITIES"));
            duplicateEntitiesMenu.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    new Thread(new Runnable() {
                        public void run() {
                            kinDiagramPanel.showProgressBar();
                            try {
                                final UniqueIdentifier[] duplicateEntities = new EntityMerger(sessionStorage, arbilWindowManager, entityCollection).duplicateEntities(selectedIdentifiers);
                                kinDiagramPanel.entityRelationsChanged(selectedIdentifiers);
                                kinDiagramPanel.addRequiredNodes(duplicateEntities, eventLocation);
                            } catch (ImportException exception) {
                                arbilWindowManager.addMessageDialogToQueue(java.text.MessageFormat.format(menus.getString("FAILED TO DUPLICATE: {0}"), new Object[]{exception.getMessage()}), duplicateEntitiesMenu.getText());
                            } catch (EntityServiceException exception) {
                                arbilWindowManager.addMessageDialogToQueue(java.text.MessageFormat.format(menus.getString("FAILED TO DUPLICATE: {0}"), new Object[]{exception.getMessage()}), duplicateEntitiesMenu.getText());
                            }
                            kinDiagramPanel.clearProgressBar();
                        }
                    }).start();
                }
            });
            this.add(duplicateEntitiesMenu);

            addRelationEntityMenu = new JMenu(menus.getString("ADD RELATION"));
            this.add(addRelationEntityMenu);
            for (RelationType relationType : RelationType.values()) {
                JMenuItem addRelationEntityMenuItem = new JMenuItem(relationType.name());
                addRelationEntityMenuItem.setActionCommand(relationType.name());
                addRelationEntityMenuItem.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(final java.awt.event.ActionEvent evt) {
                        new Thread(new Runnable() {
                            public void run() {
                                kinDiagramPanel.showProgressBar();
                                try {
                                    UniqueIdentifier[] affectedIdentifiers = new RelationLinker(sessionStorage, arbilWindowManager, entityCollection).linkEntities(selectedIdentifiers, RelationType.valueOf(evt.getActionCommand()), null, null); // todo: custom relation types could be enabled here as could dcr values..
                                    kinDiagramPanel.entityRelationsChanged(affectedIdentifiers);
                                } catch (ImportException exception) {
                                    arbilWindowManager.addMessageDialogToQueue(java.text.MessageFormat.format(menus.getString("FAILED TO CREATE RELATION: {0}"), new Object[]{exception.getMessage()}), addRelationEntityMenu.getText());
                                }
                                kinDiagramPanel.clearProgressBar();
                            }
                        }).start();
                    }
                });
                addRelationEntityMenu.add(addRelationEntityMenuItem);
            }

            removeRelationEntityMenu = new JMenu(menus.getString("REMOVE RELATIONS")); // todo: if one node only then "remove all relations of this entity"
            this.add(removeRelationEntityMenu);
//            for (RelationType relationType : RelationType.values()) {
//            relationType.name()
            //todo: add a remove all relations to selection (including unselected and not shown entities)

            String actionString = menus.getString("REMOVE RELATIONS BETWEEN SELECTED");
            JMenuItem removeRelationEntityMenuItem = new JMenuItem(actionString);
            removeRelationEntityMenuItem.setActionCommand(actionString);
            removeRelationEntityMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    new Thread(new Runnable() {
                        public void run() {
                            kinDiagramPanel.showProgressBar();
                            try {
                                new RelationLinker(sessionStorage, arbilWindowManager, entityCollection).unlinkEntities(graphPanel, selectedIdentifiers);
                                kinDiagramPanel.entityRelationsChanged(selectedIdentifiers);
                            } catch (ImportException exception) {
                                arbilWindowManager.addMessageDialogToQueue(java.text.MessageFormat.format(menus.getString("FAILED TO REMOVE RELATIONS: {0}"), new Object[]{exception.getMessage()}), removeRelationEntityMenu.getText());
                            }
                            kinDiagramPanel.clearProgressBar();
                        }
                    }).start();
                }
            });
            removeRelationEntityMenu.add(removeRelationEntityMenuItem);
//            }
            this.add(new JSeparator());
        }
        setAsEgoMenuItem = new JMenuItem(menus.getString("SET AS EGO (LIST WILL BE CLEARED)"));
        setAsEgoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinDiagramPanel.setEgoNodes(selectedIdentifiers); // getSelectedUriArray(),
            }
        });
        this.add(setAsEgoMenuItem);
        addAsEgoMenuItem = new JMenuItem(menus.getString("ADD TO EGO LIST"));
        addAsEgoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinDiagramPanel.addEgoNodes(selectedIdentifiers); //getSelectedUriArray(),
            }
        });
        this.add(addAsEgoMenuItem);
        removeEgoMenuItem = new JMenuItem(menus.getString("REMOVE FROM EGO LIST"));
        removeEgoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinDiagramPanel.removeEgoNodes(selectedIdentifiers);
            }
        });
        this.add(removeEgoMenuItem);
        this.add(jSeparator2);

        addAsRequiredMenuItem = new JMenuItem(menus.getString("ATTACH TO DIAGRAM"));
        addAsRequiredMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinDiagramPanel.addRequiredNodes(selectedIdentifiers, null); //getSelectedUriArray(),
            }
        });
        this.add(addAsRequiredMenuItem);
        removeRequiredMenuItem = new JMenuItem(menus.getString("RELEASE FROM DIAGRAM"));
        removeRequiredMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinDiagramPanel.removeRequiredNodes(selectedIdentifiers);
            }
        });
        this.add(removeRequiredMenuItem);
        this.add(jSeparator3);

        JMenuItem resetZoomMenuItem = new JMenuItem(menus.getString("RESET ZOOM"));
        resetZoomMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphPanel.resetZoom();
            }
        });
        this.add(resetZoomMenuItem);

        JMenuItem resetLayoutMenuItem = new JMenuItem(menus.getString("RESET LAYOUT"));
        resetLayoutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (!kinDiagramPanel.verifyDiagramDataLoaded()) {
                    return;
                }
                graphPanel.resetLayout(true);
            }
        });
        this.add(resetLayoutMenuItem);

        JMenuItem searchEntityServiceMenuItem = new JMenuItem(menus.getString("SEARCH ENTITY SERVICE"));
        searchEntityServiceMenuItem.setToolTipText(menus.getString("SEARCH THE ENTITY DATABASE FOR ENTITIES MATCHING THE CURRENT KIN TERMS AND POPULATE HE DIAGRAM WITH THE RESULTS"));
        searchEntityServiceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // todo: Search the entity database for entities matching the current kin terms and populate he diagram with the results
            }
        });
        searchEntityServiceMenuItem.setEnabled(false);
        this.add(searchEntityServiceMenuItem);

        saveFileMenuItem = new JMenuItem();
        saveFileMenuItem.setText(menus.getString("SAVE ALL DATA CHANGES"));
        saveFileMenuItem.setEnabled(false);
        saveFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new Thread(new Runnable() {
                    public void run() {
                        kinDiagramPanel.showProgressBar();
                        try {
                            arbilWindowManager.stopEditingInCurrentWindow();
                            dataNodeLoader.saveNodesNeedingSave(true);
                        } catch (Exception ex) {
                            BugCatcherManager.getBugCatcher().logError(ex);
                        }
                        kinDiagramPanel.clearProgressBar();
                    }
                }).start();
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

    public void actionPerformed(final ActionEvent e) {
        if (!kinDiagramPanel.verifyDiagramDataLoaded()) {
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                kinDiagramPanel.showProgressBar();
                graphPanel.svgUpdateHandler.addGraphics(SvgUpdateHandler.GraphicsTypes.valueOf(e.getActionCommand()), eventLocation);
                kinDiagramPanel.clearProgressBar();
            }
        }).start();
    }
}
