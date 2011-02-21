package nl.mpi.kinnate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.arbil.clarin.CmdiComponentBuilder;

/**
 *  Document   : GraphPanelContextMenu
 *  Created on : Feb 18, 2011, 11:51:00 AM
 *  Author     : Peter Withers
 */
public class GraphPanelContextMenu extends JPopupMenu {

    KinTypeEgoSelectionTestPanel egoSelectionPanel;
    GraphPanel graphPanel;
    GraphPanelSize graphPanelSize;

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
                        ArrayList<String> entityArray = new ArrayList<String>(Arrays.asList(LinorgSessionStorage.getSingleInstance().loadStringArray("KinGraphTree")));
                        entityArray.add(addedNodePath.toASCIIString());
                        LinorgSessionStorage.getSingleInstance().saveStringArray("KinGraphTree", entityArray.toArray(new String[]{}));
                        // todo: update the main entity tree
                        ArrayList<URI> egoUriList = new ArrayList<URI>(Arrays.asList(graphPanel.getEgoList()));
                        egoUriList.add(addedNodePath);
                        egoSelectionPanel.addEgoNodes(egoUriList.toArray(new URI[]{}));
                    } catch (URISyntaxException ex) {
                        GuiHelper.linorgBugCatcher.logError(ex);
                        // todo: warn user with a dialog
                    }
                }
            });
            this.add(addEntityMenuItem);
        }
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
    }

    private void setGraphPanelSize(String sizeString) {
        graphPanelSize.setSize(sizeString);
        graphPanel.drawNodes();
    }
}
