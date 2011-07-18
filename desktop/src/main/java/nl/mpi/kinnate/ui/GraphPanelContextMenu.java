package nl.mpi.kinnate.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.RelationLinker;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.svg.GraphPanelSize;
import nl.mpi.kinnate.uniqueidentifiers.LocalIdentifier;

/**
 *  Document   : GraphPanelContextMenu
 *  Created on : Feb 18, 2011, 11:51:00 AM
 *  Author     : Peter Withers
 */
public class GraphPanelContextMenu extends JPopupMenu implements ActionListener {

    private KinTypeEgoSelectionTestPanel egoSelectionPanel;
    private GraphPanel graphPanel;
    private GraphPanelSize graphPanelSize;
    private JMenuItem addRelationEntityMenu;
    private JMenuItem setAsEgoMenuItem;
    private JMenuItem addAsEgoMenuItem;
    private JMenuItem removeEgoMenuItem;
    private JMenuItem addAsRequiredMenuItem;
    private JMenuItem removeRequiredMenuItem;
    private JMenuItem saveFileMenuItem;
    private JCheckBoxMenuItem snapToGridMenuItem;
    private JCheckBoxMenuItem showKinTermLinesMenuItem;
    private JCheckBoxMenuItem showSanguineLinesMenuItem;
    private JCheckBoxMenuItem showLabelssMenuItem;
    private JCheckBoxMenuItem showKinTypeLabelssMenuItem;
    private JCheckBoxMenuItem showKinTermLabelssMenuItem;
    private JCheckBoxMenuItem showArchiveLinksMenuItem;
//    private JCheckBoxMenuItem showResourceLinksMenuItem;
    private String[] selectedIdentifiers = null; // keep the selected paths as shown at the time of the menu intereaction
    private float xPos;
    private float yPos;

    public GraphPanelContextMenu(KinTypeEgoSelectionTestPanel egoSelectionPanelLocal, GraphPanel graphPanelLocal, GraphPanelSize graphPanelSizeLocal) {
        egoSelectionPanel = egoSelectionPanelLocal;
        graphPanel = graphPanelLocal;
        graphPanelSize = graphPanelSizeLocal;
        if (egoSelectionPanelLocal != null) {
            JMenuItem addEntityMenuItem = new JMenuItem("Add Entity");
            addEntityMenuItem.setActionCommand(GraphPanelContextMenu.class.getResource("/xsd/StandardEntity.xsd").toString());
            addEntityMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    // todo: this could be simplified by adapting the Arbil code
                    String nodeType = evt.getActionCommand();
                    URI addedNodePath;
                    URI targetFileURI = ArbilSessionStorage.getSingleInstance().getNewArbilFileName(ArbilSessionStorage.getSingleInstance().getCacheDirectory(), nodeType);
                    ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
                    try {
                        addedNodePath = componentBuilder.createComponentFile(targetFileURI, new URI(nodeType), false);
                        String localIdentifier = new LocalIdentifier().setLocalIdentifier(new File(addedNodePath));
                        new EntityCollection().updateDatabase(addedNodePath);
//                        ArrayList<String> entityArray = new ArrayList<String>(Arrays.asList(LinorgSessionStorage.getSingleInstance().loadStringArray("KinGraphTree")));
//                        entityArray.add(addedNodePath.toASCIIString());
//                        LinorgSessionStorage.getSingleInstance().saveStringArray("KinGraphTree", entityArray.toArray(new String[]{}));
                        // todo: update the main entity tree
//                        ArrayList<URI> egoUriList = new ArrayList<URI>(Arrays.asList(graphPanel.getEgoList()));
//                        egoUriList.add(addedNodePath);
//                        ArrayList<String> egoIdentifierList = new ArrayList<String>(Arrays.asList(graphPanel.getEgoUniquiIdentifiersList()));
//                        egoUriList.add(addedNodePath);
//                        egoIdentifierList.add(localIdentifier);
//                        ArrayList<URI> egoUriList = new ArrayList<URI>(Arrays.asList(graphPanel.getEgoPaths()));
//                        egoUriList.add(addedNodePath);
                        // todo: look into the need or not of adding ego nodes, on one hand they should not be added as ego nodes but as working nodes, also it is likely that the jlist that is updated by this could better be updaed by the selection listner
//                        egoSelectionPanel.addEgoNodes(egoUriList.toArray(new URI[]{}), egoIdentifierList.toArray(new String[]{}));
                        egoSelectionPanel.addRequiredNodes(new URI[]{addedNodePath}, new String[]{localIdentifier});
                    } catch (URISyntaxException ex) {
                        new ArbilBugCatcher().logError(ex);
                        // todo: warn user with a dialog
                    }
                }
            });
            this.add(addEntityMenuItem);
            addRelationEntityMenu = new JMenu("Add Relation");
            this.add(addRelationEntityMenu);
            for (RelationType relationType : RelationType.values()) {
                JMenuItem addRelationEntityMenuItem = new JMenuItem(relationType.name());
                addRelationEntityMenuItem.setActionCommand(relationType.name());
                addRelationEntityMenuItem.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        new RelationLinker().linkEntities(graphPanel, selectedIdentifiers, RelationType.valueOf(evt.getActionCommand()));
                        egoSelectionPanel.entityRelationsChanged(selectedIdentifiers);
//                    graphPanel.
//                    selectedIdentifiers
//                            graphPanel.getPathForElementId()
                        // todo: add the relation to both selected nodes and show the new relation in the table
                        // todo: this could be simplified by adapting the Arbil code
//                    String nodeType = evt.getActionCommand();
//                    URI addedNodePath;
//                    URI targetFileURI = LinorgSessionStorage.getSingleInstance().getNewImdiFileName(LinorgSessionStorage.getSingleInstance().getCacheDirectory(), nodeType);
//                    CmdiComponentBuilder componentBuilder = new CmdiComponentBuilder();
//                    try {
//                        addedNodePath = componentBuilder.createComponentFile(targetFileURI, new URI(nodeType), false);
//                        ArrayList<String> entityArray = new ArrayList<String>(Arrays.asList(LinorgSessionStorage.getSingleInstance().loadStringArray("KinGraphTree")));
//                        entityArray.add(addedNodePath.toASCIIString());
//                        LinorgSessionStorage.getSingleInstance().saveStringArray("KinGraphTree", entityArray.toArray(new String[]{}));
//                        // todo: update the main entity tree
//                        ArrayList<URI> egoUriList = new ArrayList<URI>(Arrays.asList(graphPanel.getEgoList()));
//                        egoUriList.add(addedNodePath);
//                        egoSelectionPanel.addEgoNodes(egoUriList.toArray(new URI[]{}));
//                    } catch (URISyntaxException ex) {
//                        new ArbilBugCatcher().logError(ex);
//                        // todo: warn user with a dialog
//                    }
                    }
                });
                addRelationEntityMenu.add(addRelationEntityMenuItem);
            }
            JMenu shapeSubMenu = new JMenu("Add Geometry");
            for (String currentType : new String[]{"Label", "Circle", "Square", "Polyline"}) {
                JMenuItem addLabel = new JMenuItem("Add " + currentType);
                addLabel.setActionCommand(currentType);
                shapeSubMenu.add(addLabel);
                if (!"Label".equals(currentType)) {
                    addLabel.setEnabled(false);
                }
                addLabel.addActionListener(this);
                // todo: addthese into a layer behind the entities, athought lables could be above
                // todo: when geometry is selected construct an arbildatanode to allow the geometries attributes to be edited
            }
            this.add(shapeSubMenu);
        }
        setAsEgoMenuItem = new JMenuItem("Set as Ego (relacing existing)");
        setAsEgoMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                egoSelectionPanel.setEgoNodes(getSelectedUriArray(), selectedIdentifiers);
            }
        });
        this.add(setAsEgoMenuItem);
        addAsEgoMenuItem = new JMenuItem("Add as Ego");
        addAsEgoMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                egoSelectionPanel.addEgoNodes(getSelectedUriArray(), selectedIdentifiers);
            }
        });
        this.add(addAsEgoMenuItem);
        removeEgoMenuItem = new JMenuItem("Remove Ego");
        removeEgoMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                egoSelectionPanel.removeEgoNodes(selectedIdentifiers);
            }
        });
        this.add(removeEgoMenuItem);
        addAsRequiredMenuItem = new JMenuItem("Set as required");
        addAsRequiredMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                egoSelectionPanel.addRequiredNodes(getSelectedUriArray(), selectedIdentifiers);
            }
        });
        this.add(addAsRequiredMenuItem);
        removeRequiredMenuItem = new JMenuItem("Remove requirement");
        removeRequiredMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                egoSelectionPanel.removeRequiredNodes(selectedIdentifiers);
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
        if (addRelationEntityMenu != null) {
            addRelationEntityMenu.setVisible(selectedIdentifiers.length == 2);
            setAsEgoMenuItem.setVisible(selectedIdentifiers.length > 0);
            addAsEgoMenuItem.setVisible(selectedIdentifiers.length > 0);
            removeEgoMenuItem.setVisible(selectedIdentifiers.length > 0); // todo: set these items based on the state of the selected entities, //graphPanel.selectionContainsEgo());
            addAsRequiredMenuItem.setVisible(selectedIdentifiers.length > 0);
            removeRequiredMenuItem.setVisible(selectedIdentifiers.length > 0);
        } else {
            setAsEgoMenuItem.setVisible(false);
            addAsEgoMenuItem.setVisible(false);
            removeEgoMenuItem.setVisible(false);
            addAsRequiredMenuItem.setVisible(false);
            removeRequiredMenuItem.setVisible(false);
        }
        snapToGridMenuItem.setSelected(graphPanel.dataStoreSvg.snapToGrid);
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
        if (e.getActionCommand().equals("Label")) {
            graphPanel.svgUpdateHandler.addLabel("Label", xPos, yPos);
        }
    }
}
