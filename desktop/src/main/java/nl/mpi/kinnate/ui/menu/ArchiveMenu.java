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
import java.util.ResourceBundle;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import nl.mpi.kinnate.KinTermSavePanel;
import nl.mpi.kinnate.kindata.VisiblePanelSetting.PanelType;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;

/**
 * Document : KinTermsMenu
 * Created on : Apr 1, 2011, 9:32:40 AM
 * Author : Peter Withers
 */
public class ArchiveMenu extends JMenu {
    private static final ResourceBundle menus = ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Menus");

    AbstractDiagramManager diagramWindowManager;
    KinTermSavePanel currentKinTermSavePanel;
    JCheckBoxMenuItem hideShowMenu;
    JMenuItem showRemoteTreeMenu;
    JMenuItem showLocalTreeMenu;
    JMenu showResultsMenu;
    JMenu closeResultsMenu;

    public ArchiveMenu(AbstractDiagramManager diagramWindowManager, Component parentComponent) {
        this.diagramWindowManager = diagramWindowManager;
        initMenu(parentComponent);
    }

    private void initMenu(final Component parentComponent) {
        hideShowMenu = new JCheckBoxMenuItem(menus.getString("SHOW"));
        showRemoteTreeMenu = new JMenuItem(menus.getString("ARCHIVE TREE"));
        showLocalTreeMenu = new JMenuItem(menus.getString("LOCAL TREE"));
        closeResultsMenu = new JMenu(menus.getString("CLOSE RESULTS"));
        showResultsMenu = new JMenu(menus.getString("SHOW RESULTS"));
        this.setText(menus.getString("ARCHIVE LINKER"));
        this.add(hideShowMenu);
        this.add(showRemoteTreeMenu);
        this.add(showLocalTreeMenu);
        this.add(closeResultsMenu);
        this.add(showResultsMenu);
        this.addMenuListener(new javax.swing.event.MenuListener() {

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                // todo: check if a kin term pane is showing or associated with the diagram and that a diagam is showing
                currentKinTermSavePanel = diagramWindowManager.getKinTermPanel(parentComponent);
                if (currentKinTermSavePanel != null) {
//                    hideShowMenu.setEnabled(true);
                    if (currentKinTermSavePanel.getPanelState(PanelType.ArchiveLinker)) {
//                        exportMenu.setEnabled(false);
//                        importMenu.setEnabled(false);
//                        newMenu.setEnabled(false);
//                        deleteMenu.setEnabled(false);
                        hideShowMenu.setSelected(true);
                    } else {
//                        exportMenu.setEnabled(true);
//                        importMenu.setEnabled(true);
//                        newMenu.setEnabled(true);
//                        deleteMenu.setEnabled(false); // todo: enable deleting the current kin term group and update the menu to reflect the group name that would be deleted
                        hideShowMenu.setSelected(false);
                    }
                } else {
//                    exportMenu.setEnabled(false);
//                    importMenu.setEnabled(false);
//                    hideShowMenu.setEnabled(false);
//                    newMenu.setEnabled(false);
//                    deleteMenu.setEnabled(false);
                }
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
        });
        hideShowMenu.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currentKinTermSavePanel.setPanelState(PanelType.ArchiveLinker, hideShowMenu.isSelected());
            }
        });
//        exportMenu.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                currentKinTermSavePanel.exportKinTerms();
//            }
//        });
//        importMenu.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                currentKinTermSavePanel.importKinTerms();
//            }
//        });
//        newMenu.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                currentKinTermSavePanel.addKinTermGroup();
//            }
//        });
    }
}
