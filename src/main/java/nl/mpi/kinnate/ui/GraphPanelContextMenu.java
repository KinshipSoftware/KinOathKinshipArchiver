package nl.mpi.kinnate.ui;

import java.awt.Component;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.arbil.clarin.CmdiComponentBuilder;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.svg.GraphPanelSize;
import nl.mpi.kinnate.uniqueidentifiers.LocalIdentifier;

/**
 *  Document   : GraphPanelContextMenu
 *  Created on : Feb 18, 2011, 11:51:00 AM
 *  Author     : Peter Withers
 */
public class GraphPanelContextMenu extends JPopupMenu {

    KinTypeEgoSelectionTestPanel egoSelectionPanel;
    GraphPanel graphPanel;
    GraphPanelSize graphPanelSize;
    JMenuItem addRelationEntityMenuItem;
    JMenuItem setAsEgoMenuItem;
    String[] selectedPaths = null; // keep the selected paths as shown at the time of the menu intereaction
    String[] selectedIdentifiers = null;

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
                    URI targetFileURI = LinorgSessionStorage.getSingleInstance().getNewImdiFileName(LinorgSessionStorage.getSingleInstance().getCacheDirectory(), nodeType);
                    CmdiComponentBuilder componentBuilder = new CmdiComponentBuilder();
                    try {
                        addedNodePath = componentBuilder.createComponentFile(targetFileURI, new URI(nodeType), false);
                        String localIdentifier = new LocalIdentifier().setLocalIdentifier(new File(addedNodePath));
//                        ArrayList<String> entityArray = new ArrayList<String>(Arrays.asList(LinorgSessionStorage.getSingleInstance().loadStringArray("KinGraphTree")));
//                        entityArray.add(addedNodePath.toASCIIString());
//                        LinorgSessionStorage.getSingleInstance().saveStringArray("KinGraphTree", entityArray.toArray(new String[]{}));
                        // todo: update the main entity tree
                        ArrayList<URI> egoUriList = new ArrayList<URI>(Arrays.asList(graphPanel.getEgoList()));
                        egoUriList.add(addedNodePath);
                        ArrayList<String> egoIdentifierList = new ArrayList<String>(Arrays.asList(graphPanel.getEgoUniquiIdentifiersList()));
                        egoUriList.add(addedNodePath);
                        egoIdentifierList.add(localIdentifier);
                        egoSelectionPanel.addEgoNodes(egoUriList.toArray(new URI[]{}), egoIdentifierList.toArray(new String[]{}));
                    } catch (URISyntaxException ex) {
                        GuiHelper.linorgBugCatcher.logError(ex);
                        // todo: warn user with a dialog
                    }
                }
            });
            this.add(addEntityMenuItem);
            addRelationEntityMenuItem = new JMenuItem("Add Relation");
            addRelationEntityMenuItem.setActionCommand(GraphPanelContextMenu.class.getResource("/xsd/StandardEntity.xsd").toString());
            addRelationEntityMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
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
//                        GuiHelper.linorgBugCatcher.logError(ex);
//                        // todo: warn user with a dialog
//                    }
                }
            });
            this.add(addRelationEntityMenuItem);
        }
        setAsEgoMenuItem = new JMenuItem("Set as Ego");
        setAsEgoMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                URI[] selectedUriArray = new URI[selectedPaths.length];
                for (int currentIndex = 0; currentIndex < selectedPaths.length; currentIndex++) {
                    try {
                        selectedUriArray[currentIndex] = new URI(selectedPaths[currentIndex]);
                    } catch (URISyntaxException ex) {
                        GuiHelper.linorgBugCatcher.logError(ex);
                        // todo: warn user with a dialog
                    }
                }
                egoSelectionPanel.addEgoNodes(selectedUriArray, selectedIdentifiers);
            }
        });
        this.add(setAsEgoMenuItem);
        JMenuItem resetZoomMenuItem = new JMenuItem("Reset Zoom");
        resetZoomMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphPanel.resetZoom();
            }
        });
        this.add(resetZoomMenuItem);

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
        JCheckBoxMenuItem showSanguinLinesMenuItem = new JCheckBoxMenuItem("Show Sanguin Lines");
        showSanguinLinesMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // todo: Hide/Show Sanguin Lines on the graph
            }
        });
        this.add(showSanguinLinesMenuItem);
        JCheckBoxMenuItem showKinTermsMenuItem = new JCheckBoxMenuItem("Show Kin Terms");
        showKinTermsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // todo: Hide/Show Kin Terms on the graph
            }
        });
        this.add(showKinTermsMenuItem);
        JMenuItem searchEntityServiceMenuItem = new JMenuItem("Search Entity Service");
        searchEntityServiceMenuItem.setToolTipText("Search the entity database for entities matching the current kin terms and populate he diagram with the results");
        searchEntityServiceMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // todo: Search the entity database for entities matching the current kin terms and populate he diagram with the results
            }
        });
        this.add(searchEntityServiceMenuItem);
    }

    private void setGraphPanelSize(String sizeString) {
        graphPanelSize.setSize(sizeString);
        graphPanel.drawNodes();
    }

    @Override
    public void show(Component cmpnt, int i, int i1) {
        selectedPaths = graphPanel.getSelectedPaths();
        selectedIdentifiers = graphPanel.getEgoUniquiIdentifiersList();
        addRelationEntityMenuItem.setVisible(selectedPaths.length == 2);
        setAsEgoMenuItem.setVisible(selectedPaths.length > 0);
        super.show(cmpnt, i, i1);
    }
}
