package nl.mpi.kinnate.ui;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import nl.mpi.kinnate.KinTermSavePanel;
import nl.mpi.kinnate.kindata.VisiblePanelSetting.PanelType;

/**
 *  Document   : KinTermsMenu
 *  Created on : Apr 1, 2011, 9:32:40 AM
 *  Author     : Peter Withers
 */
public class KinTermsMenu extends JMenu {

    DiagramWindowManager diagramWindowManager;
    KinTermSavePanel currentKinTermSavePanel;
    JCheckBoxMenuItem hideShowMenu;
    JMenuItem newMenu;
    JMenuItem exportMenu;
    JMenuItem importMenu;
    JMenuItem deleteMenu;

    public KinTermsMenu(DiagramWindowManager diagramWindowManager) {
        this.diagramWindowManager = diagramWindowManager;
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
                currentKinTermSavePanel = diagramWindowManager.getKinTermPanel();
                if (currentKinTermSavePanel != null) {
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
