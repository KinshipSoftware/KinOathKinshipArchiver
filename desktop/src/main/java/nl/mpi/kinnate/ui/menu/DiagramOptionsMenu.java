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
package nl.mpi.kinnate.ui.menu;

import java.awt.Component;
import java.util.ResourceBundle;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu.Separator;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;

/**
 * Document : DiagramOptionsMenu Created on : Dec 8, 2011, 10:35:50 AM
 *
 * @author Peter Withers
 */
public class DiagramOptionsMenu extends JMenu {
    private static final ResourceBundle menus = ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Menus");

    public DiagramOptionsMenu(final AbstractDiagramManager diagramWindowManager, final Component parentComponent) {
        this.setText(menus.getString("DIAGRAM OPTIONS"));

        this.addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent e) {
                final SavePanel currentSavePanel = diagramWindowManager.getCurrentSavePanel(parentComponent);
                DiagramOptionsMenu.this.removeAll();
                if (currentSavePanel != null) {
                    setupMenuItems(currentSavePanel, currentSavePanel.getGraphPanel());
                } else {
                    JMenuItem noItemsMenu = new JMenuItem(menus.getString("<NO ITEMS AVAILABLE IN THIS CONTEXT>"));
                    noItemsMenu.setEnabled(false);
                    DiagramOptionsMenu.this.add(noItemsMenu);
                }
            }

            public void menuDeselected(MenuEvent e) {
            }

            public void menuCanceled(MenuEvent e) {
            }
        });
    }

    private void setupMenuItems(final SavePanel currentSavePanel, final GraphPanel graphPanel) {
        JCheckBoxMenuItem snapToGridMenuItem = new JCheckBoxMenuItem(menus.getString("SNAP TO GRID"));
        snapToGridMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphPanel.dataStoreSvg.snapToGrid = !graphPanel.dataStoreSvg.snapToGrid;
            }
        });
        this.add(snapToGridMenuItem);

        JCheckBoxMenuItem highlightRelationsMenuItem = new JCheckBoxMenuItem(menus.getString("HIGHLIGHT SELECTED RELATIONS"));
        highlightRelationsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphPanel.dataStoreSvg.highlightRelationLines = !graphPanel.dataStoreSvg.highlightRelationLines;
                graphPanel.drawNodes(false);
            }
        });
        this.add(highlightRelationsMenuItem);

        JCheckBoxMenuItem showBorderMenuItem = new JCheckBoxMenuItem(menus.getString("SHOW DIAGRAM BORDER"));
        showBorderMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphPanel.dataStoreSvg.showDiagramBorder = !graphPanel.dataStoreSvg.showDiagramBorder;
                graphPanel.drawNodes(true);
            }
        });
        this.add(showBorderMenuItem);

        // todo: reassess if the diagram page layout/size is of value to the user
//        JMenu diagramSizeMenuItem = new JMenu("Diagram Size");
//        for (String currentString : graphPanel.graphPanelSize.getPreferredSizes()) {
//            JMenuItem currentMenuItem = new JMenuItem(currentString);
//            currentMenuItem.setActionCommand(currentString);
//            currentMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//                public void actionPerformed(java.awt.event.ActionEvent evt) {
//                    String sizeString = evt.getActionCommand();
//                    graphPanel.graphPanelSize.setSize(sizeString);
//                    graphPanel.drawNodes();
//                }
//            });
//            diagramSizeMenuItem.add(currentMenuItem);
//        }
//        this.add(diagramSizeMenuItem);

        JCheckBoxMenuItem showSanguineLinesMenuItem = new JCheckBoxMenuItem(menus.getString("SHOW SANGUIN LINES"));
        showSanguineLinesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Hide/Show Sanguin Lines on the graph
                graphPanel.dataStoreSvg.showSanguineLines = !graphPanel.dataStoreSvg.showSanguineLines;
                graphPanel.drawNodes(false);
            }
        });
        this.add(showSanguineLinesMenuItem);
//        JCheckBoxMenuItem showKinTermLinesMenuItem = new JCheckBoxMenuItem("Show Kin Term Lines");
//        showKinTermLinesMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                //  Hide/Show Kin Term lines on the graph
//                graphPanel.dataStoreSvg.showKinTermLines = !graphPanel.dataStoreSvg.showKinTermLines;
//                graphPanel.drawNodes();
//            }
//        });
//        this.add(showKinTermLinesMenuItem);

        this.add(new Separator());
        JCheckBoxMenuItem showLabelssMenuItem = new JCheckBoxMenuItem(menus.getString("SHOW ENTITY LABELS"));
        showLabelssMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Hide/Show labels on the graph
                graphPanel.dataStoreSvg.showLabels = !graphPanel.dataStoreSvg.showLabels;
                graphPanel.drawNodes(false);
            }
        });
        this.add(showLabelssMenuItem);

        JCheckBoxMenuItem showKinTypeLabelssMenuItem = new JCheckBoxMenuItem(menus.getString("SHOW KIN TYPE LABELS"));
        showKinTypeLabelssMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Hide/Show Kin type labels on the graph
                graphPanel.dataStoreSvg.showKinTypeLabels = !graphPanel.dataStoreSvg.showKinTypeLabels;
                graphPanel.drawNodes(false);
            }
        });
        this.add(showKinTypeLabelssMenuItem);
//        showKinTermLabelssMenuItem = new JCheckBoxMenuItem("Show Kin Term Labels");
//        showKinTermLabelssMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                // Hide/Show Kin Terms on the graph
//                graphPanel.dataStoreSvg.showKinTermLabels = !graphPanel.dataStoreSvg.showKinTermLabels;
//                graphPanel.drawNodes();
//            }
//        });
//        this.add(showKinTermLabelssMenuItem);

        // todo: this should not show when no ids are specified by the user
        JCheckBoxMenuItem showIdLabelsMenuItem = new JCheckBoxMenuItem(menus.getString("SHOW ID LABELS"));
        showIdLabelsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Hide/Show transient node ids on the graph
                graphPanel.dataStoreSvg.showIdLabels = !graphPanel.dataStoreSvg.showIdLabels;
                graphPanel.drawNodes(false);
            }
        });
        this.add(showIdLabelsMenuItem);

        JCheckBoxMenuItem showDateLabelsMenuItem = new JCheckBoxMenuItem(menus.getString("SHOW DATE LABELS"));
        showDateLabelsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Hide/Show dates on the graph
                graphPanel.dataStoreSvg.showDateLabels = !graphPanel.dataStoreSvg.showDateLabels;
                graphPanel.drawNodes(false);
            }
        });
        this.add(showDateLabelsMenuItem);

        JCheckBoxMenuItem showArchiveLinksMenuItem = new JCheckBoxMenuItem(menus.getString("SHOW EXTERNAL LINK LABELS"));
        showArchiveLinksMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphPanel.dataStoreSvg.showExternalLinks = !graphPanel.dataStoreSvg.showExternalLinks;
                graphPanel.drawNodes(false);
            }
        });
        this.add(showArchiveLinksMenuItem);

        this.add(new Separator());
        JMenuItem diagramSettings = new JMenuItem(menus.getString("SETTINGS"));
        diagramSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currentSavePanel.showSettings();
            }
        });
        this.add(diagramSettings);

//        showResourceLinksMenuItem = new JCheckBoxMenuItem("Show Archive Resource Links");
//        showResourceLinksMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                graphPanel.dataStoreSvg.showResourceLinks = !graphPanel.dataStoreSvg.showResourceLinks;
//                graphPanel.drawNodes();
//            }
//        });
//        this.add(showResourceLinksMenuItem);

//        JCheckBoxMenuItem recalculateDiagramMenuItem = new JCheckBoxMenuItem("Recalculate the Diagram");
//        recalculateDiagramMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                currentSavePanel.updateGraph();
//            }
//        });
//        this.add(recalculateDiagramMenuItem);

        snapToGridMenuItem.setSelected(graphPanel.dataStoreSvg.snapToGrid);
        highlightRelationsMenuItem.setSelected(graphPanel.dataStoreSvg.highlightRelationLines);
        showBorderMenuItem.setSelected(graphPanel.dataStoreSvg.showDiagramBorder);
        showSanguineLinesMenuItem.setSelected(graphPanel.dataStoreSvg.showSanguineLines);
//        showKinTermLinesMenuItem.setSelected(graphPanel.dataStoreSvg.showKinTermLines);
        showLabelssMenuItem.setSelected(graphPanel.dataStoreSvg.showLabels);
        showKinTypeLabelssMenuItem.setSelected(graphPanel.dataStoreSvg.showKinTypeLabels);
//        showKinTermLabelssMenuItem.setSelected(graphPanel.dataStoreSvg.showKinTermLabels);
        showIdLabelsMenuItem.setSelected(graphPanel.dataStoreSvg.showIdLabels);
        showDateLabelsMenuItem.setSelected(graphPanel.dataStoreSvg.showDateLabels);
        showArchiveLinksMenuItem.setSelected(graphPanel.dataStoreSvg.showExternalLinks);
//        showResourceLinksMenuItem.setSelected(graphPanel.dataStoreSvg.showResourceLinks);
    }
}
