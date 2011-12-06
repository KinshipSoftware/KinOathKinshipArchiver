package nl.mpi.kinnate.ui.menu;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import nl.mpi.kinnate.KinTermSavePanel;
import nl.mpi.kinnate.kindata.VisiblePanelSetting.PanelType;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;

/**
 *  Document   : KinTermsMenu
 *  Created on : Apr 1, 2011, 9:32:40 AM
 *  Author     : Peter Withers
 */
public class ArchiveMenu extends JMenu {

    AbstractDiagramManager diagramWindowManager;
    KinTermSavePanel currentKinTermSavePanel;
    JCheckBoxMenuItem hideShowMenu;
    JMenuItem showRemoteTreeMenu;
    JMenuItem showLocalTreeMenu;
    JMenu showResultsMenu;
    JMenu closeResultsMenu;

    public ArchiveMenu(AbstractDiagramManager diagramWindowManager) {
        this.diagramWindowManager = diagramWindowManager;
        initMenu();
    }

    public ArchiveMenu() {
        initMenu();
    }

    private void initMenu() {
        hideShowMenu = new JCheckBoxMenuItem("Show");
        showRemoteTreeMenu = new JMenuItem("Archive Tree");
        showLocalTreeMenu = new JMenuItem("Local Tree");
        closeResultsMenu = new JMenu("Close Results");
        showResultsMenu = new JMenu("Show Results");
        this.setText("Archive Linker");
        this.add(hideShowMenu);
        this.add(showRemoteTreeMenu);
        this.add(showLocalTreeMenu);
        this.add(closeResultsMenu);
        this.add(showResultsMenu);
        this.addMenuListener(new javax.swing.event.MenuListener() {

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                // todo: check if a kin term pane is showing or associated with the diagram and that a diagam is showing
                currentKinTermSavePanel = diagramWindowManager.getKinTermPanel();
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
