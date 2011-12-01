package nl.mpi.kinnate.ui;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.kinnate.KinTermSavePanel;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.export.ExportToR;
import nl.mpi.kinnate.transcoder.DiagramTranscoder;

/**
 *  Document   : FileMenu
 *  Created on : Dec 1, 2011, 4:04:06 PM
 *  Author     : Peter Withers
 */
public class FileMenu extends javax.swing.JMenu {

    private javax.swing.JMenuItem ImportGedcomUrl;
    private javax.swing.JMenuItem closeTabMenuItem;
    private javax.swing.JMenuItem entityUploadMenuItem;
    private javax.swing.JMenuItem exitApplication;
    private javax.swing.JMenuItem exportToR;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JMenuItem newDiagramMenuItem;
    private javax.swing.JMenuItem openDiagram;
    private RecentFileMenu recentFileMenu;
    private javax.swing.JMenuItem saveAsDefaultMenuItem;
    private javax.swing.JMenuItem saveDiagram;
    private javax.swing.JMenuItem saveDiagramAs;
    private javax.swing.JMenuItem savePdfMenuItem;
    DiagramWindowManager diagramWindowManager;

    public FileMenu(DiagramWindowManager diagramWindowManager) {
        this.diagramWindowManager = diagramWindowManager;
        ImportGedcomUrl = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        newDiagramMenuItem = new javax.swing.JMenuItem();
        jMenu3 = new nl.mpi.kinnate.ui.DocumentNewMenu(diagramWindowManager);
        openDiagram = new javax.swing.JMenuItem();
        recentFileMenu = new RecentFileMenu(diagramWindowManager);
        ;
        jMenu1 = new SamplesFileMenu(diagramWindowManager);
        jMenu2 = new ImportSamplesFileMenu(diagramWindowManager);
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        entityUploadMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        saveDiagram = new javax.swing.JMenuItem();
        saveDiagramAs = new javax.swing.JMenuItem();
        savePdfMenuItem = new javax.swing.JMenuItem();
        exportToR = new javax.swing.JMenuItem();
        closeTabMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        saveAsDefaultMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        exitApplication = new javax.swing.JMenuItem();


        this.setText("File");
        this.addMenuListener(new javax.swing.event.MenuListener() {

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                fileMenuMenuSelected(evt);
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
        });
        this.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileMenuActionPerformed(evt);
            }
        });

        ImportGedcomUrl.setText("Import Gedcom Samples (from internet)");
        ImportGedcomUrl.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ImportGedcomUrlActionPerformed(evt);
            }
        });
        this.add(ImportGedcomUrl);
        this.add(jSeparator1);

        newDiagramMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newDiagramMenuItem.setText("New (default diagram)");
        newDiagramMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newDiagramMenuItemActionPerformed(evt);
            }
        });
        this.add(newDiagramMenuItem);

        jMenu3.setText("New Diagram of Type");
        this.add(jMenu3);

        openDiagram.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openDiagram.setText("Open");
        openDiagram.setActionCommand("open");
        openDiagram.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDiagramActionPerformed(evt);
            }
        });
        this.add(openDiagram);


        this.add(recentFileMenu);

        jMenu1.setText("Open Sample Diagram");
        this.add(jMenu1);

        jMenu2.setText("Import Sample Data");
        this.add(jMenu2);
        this.add(jSeparator2);

        entityUploadMenuItem.setText("Entity Upload");
        entityUploadMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                entityUploadMenuItemActionPerformed(evt);
            }
        });
        this.add(entityUploadMenuItem);
        this.add(jSeparator4);

        saveDiagram.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveDiagram.setText("Save");
        saveDiagram.setActionCommand("save");
        saveDiagram.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveDiagramActionPerformed(evt);
            }
        });
        this.add(saveDiagram);

        saveDiagramAs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        saveDiagramAs.setText("Save As");
        saveDiagramAs.setActionCommand("saveas");
        saveDiagramAs.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveDiagramAsActionPerformed(evt);
            }
        });
        this.add(saveDiagramAs);

        savePdfMenuItem.setText("Export as PDF");
        savePdfMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savePdfMenuItemActionPerformed(evt);
            }
        });
        this.add(savePdfMenuItem);

        exportToR.setText("Export to R / SPSS");
        exportToR.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportToRActionPerformed(evt);
            }
        });
        this.add(exportToR);

        closeTabMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        closeTabMenuItem.setText("Close");
        closeTabMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeTabMenuItemActionPerformed(evt);
            }
        });
        this.add(closeTabMenuItem);
        this.add(jSeparator3);

        saveAsDefaultMenuItem.setText("Save as Default Diagram");
        saveAsDefaultMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsDefaultMenuItemActionPerformed(evt);
            }
        });
        this.add(saveAsDefaultMenuItem);
        this.add(jSeparator5);

        exitApplication.setText("Exit");
        exitApplication.setActionCommand("exit");
        exitApplication.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitApplicationActionPerformed(evt);
            }
        });
        this.add(exitApplication);
    }

    private void fileMenuActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void openDiagramActionPerformed(java.awt.event.ActionEvent evt) {
        final File[] selectedFilesArray = ArbilWindowManager.getSingleInstance().showFileSelectBox("Open Kin Diagram", false, true, false);
        if (selectedFilesArray != null) {
            for (File selectedFile : selectedFilesArray) {
                diagramWindowManager.openDiagram(selectedFile.getName(), selectedFile.toURI(), true);
            }
        }
    }

    private void saveDiagramActionPerformed(java.awt.event.ActionEvent evt) {
        int tabIndex = Integer.valueOf(evt.getActionCommand());
        SavePanel savePanel = diagramWindowManager.getSavePanel(tabIndex);
        savePanel.saveToFile();
    }

    private void saveDiagramAsActionPerformed(java.awt.event.ActionEvent evt) {
        // todo: update the file select to limit to svg and test that a file has been selected
        // todo: move this into the arbil window manager and get the last used directory
        // todo: make sure the file has the svg suffix
        JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new FileFilter() {

            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                return (file.getName().toLowerCase().endsWith(".svg"));
            }

            @Override
            public String getDescription() {
                return "Scalable Vector Graphics (SVG)";
            }
        });

        int returnVal = fc.showSaveDialog(this);
        // make sure the file path ends in .svg lowercase
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File svgFile = fc.getSelectedFile();
            if (!svgFile.getName().toLowerCase().endsWith(".svg")) {
                svgFile = new File(svgFile.getParentFile(), svgFile.getName() + ".svg");
            }
            int tabIndex = Integer.valueOf(evt.getActionCommand());
            SavePanel savePanel = diagramWindowManager.getSavePanel(tabIndex);
            savePanel.saveToFile(svgFile);
            recentFileMenu.addRecentFile(svgFile);
            diagramWindowManager.setDiagramTitle(tabIndex, svgFile.getName());
        } else {
            // todo: warn user that no file selected and so cannot save
        }
//        File selectedFile[] = LinorgWindowManager.getSingleInstance().showFileSelectBox("Save Kin Diagram", false, false, false);
//        if (selectedFile != null && selectedFile.length > 0) {
//            int tabIndex = Integer.valueOf(evt.getActionCommand());
//            SavePanel savePanel = getSavePanel(tabIndex);
//            savePanel.saveToFile(selectedFile[0]);
//            jTabbedPane1.setTitleAt(tabIndex, selectedFile[0].getName());
//        } else {
//            // todo: warn user that no file selected and so cannot save
//        }
    }

    private void exitApplicationActionPerformed(java.awt.event.ActionEvent evt) {
        // todo: check that things are saved and ask user if not
        System.exit(0);
    }

    private void fileMenuMenuSelected(javax.swing.event.MenuEvent evt) {
        // set the save, save as and close text to include the tab to which the action will occur
        int selectedIndex = diagramWindowManager.getSavePanelIndex();
        String currentTabText = diagramWindowManager.getSavePanelTitle(selectedIndex);
        SavePanel savePanel = diagramWindowManager.getSavePanel(selectedIndex);
        saveDiagramAs.setText("Save As (" + currentTabText + ")");
        saveDiagramAs.setActionCommand(Integer.toString(selectedIndex));
        saveDiagram.setText("Save (" + currentTabText + ")");
        saveDiagram.setActionCommand(Integer.toString(selectedIndex));
        closeTabMenuItem.setText("Close (" + currentTabText + ")");
        closeTabMenuItem.setActionCommand(Integer.toString(selectedIndex));
        saveAsDefaultMenuItem.setText("Set Default Diagram as (" + currentTabText + ")");
        saveAsDefaultMenuItem.setActionCommand(Integer.toString(selectedIndex));
        if (savePanel != null) {
            saveDiagram.setEnabled(savePanel.hasSaveFileName() && savePanel.requiresSave());
            saveDiagramAs.setEnabled(true);
            exportToR.setEnabled(true);
            closeTabMenuItem.setEnabled(true);
            saveAsDefaultMenuItem.setEnabled(true);
            savePdfMenuItem.setEnabled(true);
        } else {
            saveDiagramAs.setEnabled(false);
            saveDiagram.setEnabled(false);
            exportToR.setEnabled(false);
            closeTabMenuItem.setEnabled(false);
            saveAsDefaultMenuItem.setEnabled(false);
            savePdfMenuItem.setEnabled(false);
        }
    }

    private void closeTabMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        int tabIndex = Integer.valueOf(evt.getActionCommand());
        SavePanel savePanel = diagramWindowManager.getSavePanel(tabIndex);
        if (savePanel.requiresSave()) {
            // todo: warn user to save
            if (JOptionPane.YES_OPTION == ArbilWindowManager.getSingleInstance().showDialogBox("There are unsaved changes, do you want to save before closing?", "Close Diagram", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                savePanel.saveToFile();
            }
        }
        diagramWindowManager.closeSavePanel(tabIndex);
    }

    private void newDiagramMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        diagramWindowManager.newDiagram();
    }

    private void ImportGedcomUrlActionPerformed(java.awt.event.ActionEvent evt) {
        String[] importList = new String[]{"http://gedcomlibrary.com/gedcoms.html",
            "http://GedcomLibrary.com/gedcoms/gl120365.ged", //	Tammy Carter Inman
            "http://GedcomLibrary.com/gedcoms/gl120366.ged", //	Luis Lemonnier
            "http://GedcomLibrary.com/gedcoms/gl120367.ged", //	Cheryl Marion Follansbee
            // New England Genealogical Detective
            "http://GedcomLibrary.com/gedcoms/gl120368.ged", //	Phil Willaims
            "http://GedcomLibrary.com/gedcoms/gl120369.ged", //	Francisco Casta�eda
            "http://GedcomLibrary.com/gedcoms/gl120370.ged", //	Kim Carter
            "http://GedcomLibrary.com/gedcoms/gl120371.ged", //	Maria Perusia
            "http://GedcomLibrary.com/gedcoms/gl120372.ged", //	R. J. Bosman
            "http://GedcomLibrary.com/gedcoms/liverpool.ged", //	William Robinette
            "http://GedcomLibrary.com/gedcoms/misc2a.ged", //	William Robinette
            "http://GedcomLibrary.com/gedcoms/myline.ged", //	William Robinette

            // also look into http://gedcomlibrary.com/list.html for sample files
            "http://gedcomlibrary.com/gedcoms/gl120368.ged", //
            "http://GedcomLibrary.com/gedcoms/gl120367.ged", //
            "http://GedcomLibrary.com/gedcoms/liverpool.ged", //
            "http://GedcomLibrary.com/gedcoms/misc2a.ged", //
            "http://GedcomLibrary.com/gedcoms/gl120372.ged"};
        for (String importUrlString : importList) {
            diagramWindowManager.openImportPanel(importUrlString);
        }
    }

    private void savePdfMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        // todo: implement pdf export
        new DiagramTranscoder().saveAsPdf(diagramWindowManager.getCurrentSavePanel());
        new DiagramTranscoder().saveAsJpg(diagramWindowManager.getCurrentSavePanel());
    }

    private void exportToRActionPerformed(java.awt.event.ActionEvent evt) {

//    public KinTermSavePanel getKinTermPanel() {
//        Object selectedComponent = jTabbedPane1.getComponentAt(jTabbedPane1.getSelectedIndex());
//        KinTermSavePanel kinTermSavePanel = null;
        SavePanel currentSavePanel = diagramWindowManager.getCurrentSavePanel();
        if (currentSavePanel instanceof KinTermSavePanel) {
            new ExportToR().doExport(this, (KinTermSavePanel) currentSavePanel);
        }
    }

    private void entityUploadMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        diagramWindowManager.openEntityUploadPanel();
    }

    private void saveAsDefaultMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        int tabIndex = Integer.valueOf(evt.getActionCommand());
        SavePanel savePanel = diagramWindowManager.getSavePanel(tabIndex);
        savePanel.saveToFile(KinDiagramPanel.getDefaultDiagramFile());
    }
}
