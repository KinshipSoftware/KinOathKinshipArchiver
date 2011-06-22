package nl.mpi.kinnate.ui;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import nl.mpi.kinnate.KinTermSavePanel;

/**
 *  Document   : KinTermsMenu
 *  Created on : Apr 1, 2011, 9:32:40 AM
 *  Author     : Peter Withers
 */
public class KinTermsMenu extends JMenu {

    static private MainFrame mainFrame;
    KinTermSavePanel currentKinTermSavePanel;
    JMenuItem hideShowMenu;
    JMenuItem newMenu;
    JMenuItem exportMenu;
    JMenuItem importMenu;

    public KinTermsMenu(MainFrame mainFrameLocal) {
        mainFrame = mainFrameLocal;
        initMenu();
    }

    public KinTermsMenu() {
        initMenu();
    }

    private void initMenu() {
        hideShowMenu = new JMenuItem("Hide/Show");
        newMenu = new JMenuItem("New Kin Term Group");
        exportMenu = new JMenuItem("Export");
        importMenu = new JMenuItem("Import");
        this.setText("Kin Terms");
        this.add(hideShowMenu);
        this.add(newMenu);
        this.add(importMenu);
        this.add(exportMenu);
        this.addMenuListener(new javax.swing.event.MenuListener() {

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                // todo: check if a kin term pane is showing or associated with the diagram and that a diagam is showing
                currentKinTermSavePanel = mainFrame.getKinTermPanel();
                if (currentKinTermSavePanel != null) {
                    hideShowMenu.setEnabled(true);
                    if (currentKinTermSavePanel.isHidden()) {
                        exportMenu.setEnabled(false);
                        importMenu.setEnabled(false);
                        newMenu.setEnabled(false);
                        hideShowMenu.setText("Show");
                    } else {
                        exportMenu.setEnabled(true);
                        importMenu.setEnabled(true);
                        newMenu.setEnabled(true);
                        hideShowMenu.setText("Hide");
                    }
                } else {
                    exportMenu.setEnabled(false);
                    importMenu.setEnabled(false);
                    hideShowMenu.setEnabled(false);
                    newMenu.setEnabled(false);
                }
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
        });
        hideShowMenu.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currentKinTermSavePanel.hideShow();
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
                currentKinTermSavePanel.addKinTermGroup();
            }
        });
    }
}
