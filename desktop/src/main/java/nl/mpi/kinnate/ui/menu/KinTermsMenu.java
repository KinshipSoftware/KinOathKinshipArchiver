/**
 * Copyright (C) 2012 The Language Archive
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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import nl.mpi.kinnate.KinTermSavePanel;
import nl.mpi.kinnate.kindata.VisiblePanelSetting;
import nl.mpi.kinnate.kindata.VisiblePanelSetting.PanelType;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;

/**
 * Document : KinTermsMenu
 * Created on : Apr 1, 2011, 9:32:40 AM
 * Author : Peter Withers
 */
public class KinTermsMenu extends JMenu {

    AbstractDiagramManager diagramWindowManager;
    KinTermSavePanel currentKinTermSavePanel;
    JCheckBoxMenuItem hideShowMenu;
    JMenuItem newMenu;
    JMenuItem exportMenu;
    JMenuItem importMenu;
    JMenuItem deleteMenu;
    private Component parentComponent;

    public KinTermsMenu(AbstractDiagramManager diagramWindowManager, Component parentComponent) {
        this.diagramWindowManager = diagramWindowManager;
        this.parentComponent = parentComponent;
        initMenu();
    }

    public KinTermsMenu() {
        initMenu();
    }

    private void initMenu() {
        hideShowMenu = new JCheckBoxMenuItem("Show");
        newMenu = new JMenuItem("New Kin Term Group");
        exportMenu = new JMenuItem("Export");
        importMenu = new JMenuItem("Import");
        deleteMenu = new JMenuItem("Delete");
        this.setText("Kin Terms");
        this.add(hideShowMenu);
        this.add(newMenu);
        this.add(importMenu);
        this.add(exportMenu);
        this.add(deleteMenu);
        this.addMenuListener(new javax.swing.event.MenuListener() {

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                // todo: check if a kin term pane is showing or associated with the diagram and that a diagam is showing
                currentKinTermSavePanel = diagramWindowManager.getKinTermPanel(parentComponent);
                if (currentKinTermSavePanel != null) {
                    // hide the this menu if the diagram does not support the kin terms panel
                    VisiblePanelSetting[] visiblePanelsArray = currentKinTermSavePanel.getVisiblePanels();
                    for (VisiblePanelSetting panelSetting : visiblePanelsArray) {
                        if (panelSetting.getPanelType() != null && panelSetting.getPanelType() == PanelType.KinTerms) {
                            if (panelSetting.isMenuEnabled() == false) {
                                exportMenu.setEnabled(false);
                                importMenu.setEnabled(false);
                                hideShowMenu.setEnabled(false);
                                newMenu.setEnabled(false);
                                deleteMenu.setEnabled(false);
                                return;
                            }
                        }
                    }
                    if (currentKinTermSavePanel.getKinTermGroupCount() == 0) {
                        // if there are no kin term groups then present "new" not "show"
                        hideShowMenu.setSelected(false);
                        hideShowMenu.setEnabled(false);
                        exportMenu.setEnabled(false);
                        importMenu.setEnabled(false);
                        newMenu.setEnabled(true);
                        deleteMenu.setEnabled(false);
                    } else {
                        hideShowMenu.setEnabled(true);
                        if (!currentKinTermSavePanel.getPanelState(PanelType.KinTerms)) {
                            hideShowMenu.setSelected(false);
                            exportMenu.setEnabled(false);
                            importMenu.setEnabled(false);
                            newMenu.setEnabled(false);
                            deleteMenu.setEnabled(false);
//                        hideShowMenu.setText("Show");
                        } else {
                            hideShowMenu.setSelected(true);
                            exportMenu.setEnabled(true);
                            importMenu.setEnabled(true);
                            newMenu.setEnabled(true);
                            deleteMenu.setEnabled(false); // todo: Ticket #1063 enable deleting the current kin term group and update the menu to reflect the group name that would be deleted
//                        hideShowMenu.setText("Hide");
                        }
                    }
                } else {
                    exportMenu.setEnabled(false);
                    importMenu.setEnabled(false);
                    hideShowMenu.setEnabled(false);
                    newMenu.setEnabled(false);
                    deleteMenu.setEnabled(false);
                }
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
        });
        hideShowMenu.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currentKinTermSavePanel.setPanelState(PanelType.KinTerms, hideShowMenu.isSelected());
            }
        });
        exportMenu.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currentKinTermSavePanel.exportKinTerms();
            }
        });
        importMenu.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currentKinTermSavePanel.importKinTerms();
            }
        });
        newMenu.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currentKinTermSavePanel.setPanelState(PanelType.KinTerms, true);
                currentKinTermSavePanel.addKinTermGroup();
            }
        });
    }
}
