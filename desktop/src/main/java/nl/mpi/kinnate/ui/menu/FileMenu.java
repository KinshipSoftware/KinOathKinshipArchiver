/**
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.ui.menu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.filechooser.FileFilter;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.export.ExportToR;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.svg.DiagramTranscoder;
import nl.mpi.kinnate.ui.DiagramTranscoderPanel;
import nl.mpi.kinnate.ui.ImportSamplesFileMenu;
import nl.mpi.kinnate.ui.KinDiagramPanel;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;
import org.apache.batik.transcoder.TranscoderException;

/**
 * Document : FileMenu Created on : Dec 1, 2011, 4:04:06 PM
 *
 * @author Peter Withers
 */
public class FileMenu extends javax.swing.JMenu {

    private javax.swing.JMenuItem importGedcomUrl;
    private javax.swing.JMenuItem importGedcomFile;
//    private javax.swing.JMenuItem importCsvFile;
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
    private javax.swing.JMenuItem projectOpenMenu;
    private ProjectFileMenu projectRecentMenu;
    private javax.swing.JMenuItem saveAsDefaultMenuItem;
    private javax.swing.JMenuItem saveDiagram;
    private javax.swing.JMenuItem saveDiagramAs;
    private javax.swing.JMenuItem savePdfMenuItem;
    private AbstractDiagramManager diagramWindowManager;
    private SessionStorage sessionStorage;
    private MessageDialogHandler dialogHandler; //ArbilWindowManager
    private Component parentComponent;

    public FileMenu(AbstractDiagramManager diagramWindowManager, SessionStorage sessionStorage, MessageDialogHandler dialogHandler, Component parentComponent) {
        this.diagramWindowManager = diagramWindowManager;
        this.sessionStorage = sessionStorage;
        this.dialogHandler = dialogHandler;
        this.diagramWindowManager = diagramWindowManager;
        this.parentComponent = parentComponent;
        importGedcomUrl = new javax.swing.JMenuItem();
        importGedcomFile = new javax.swing.JMenuItem();
//        importCsvFile = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        newDiagramMenuItem = new javax.swing.JMenuItem();
        jMenu3 = new DocumentNewMenu(diagramWindowManager, parentComponent, dialogHandler);
        openDiagram = new javax.swing.JMenuItem();
        recentFileMenu = new RecentFileMenu(diagramWindowManager, sessionStorage, parentComponent, dialogHandler);
        projectOpenMenu = new javax.swing.JMenuItem();
        projectRecentMenu = new ProjectFileMenu(diagramWindowManager, sessionStorage, parentComponent, dialogHandler);
        jMenu1 = new SamplesFileMenu(diagramWindowManager, dialogHandler, parentComponent);
        jMenu2 = new ImportSamplesFileMenu(diagramWindowManager, dialogHandler, parentComponent);
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
        // todo: Ticket #1297 add an import gedcom and csv menu item


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
        openDiagram.setText("Open Diagram");
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

        this.add(new javax.swing.JPopupMenu.Separator());
        projectOpenMenu.setText("Open Project");
        projectOpenMenu.setActionCommand("browse");
        projectOpenMenu.addActionListener(projectRecentMenu);
        this.add(projectOpenMenu);

        this.add(projectRecentMenu);

        this.add(jSeparator1);

        importGedcomFile.setText("Import Gedcom / CSV / TIP File");
        importGedcomFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importGedcomFileActionPerformed(evt);
            }
        });
        this.add(importGedcomFile);

//        importCsvFile.setText("Import CSV File");
//        importCsvFile.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                importCsvFileActionPerformed(evt);
//            }
//        });
//        this.add(importCsvFile);

        jMenu2.setText("Import Sample Data");
        this.add(jMenu2);

        importGedcomUrl.setText("Import Gedcom Samples (from internet)");
        importGedcomUrl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importGedcomUrlActionPerformed(evt);
            }
        });
        importGedcomUrl.setEnabled(false);
        this.add(importGedcomUrl);

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

        savePdfMenuItem.setText("Export as PDF/JPEG/PNG/TIFF");
        savePdfMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savePdfMenuItemActionPerformed(evt);
            }
        });
        this.add(savePdfMenuItem);

        exportToR.setText("Export for R / SPSS");
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

    private HashMap<String, FileFilter> getSvgFileFilter() {
        HashMap<String, FileFilter> fileFilterMap = new HashMap<String, FileFilter>(2);
        for (final String[] currentType : new String[][]{{"Kinship Diagram (SVG format)", ".svg"}}) { // "Scalable Vector Graphics (SVG)";
            fileFilterMap.put(currentType[0], new FileFilter() {
                @Override
                public boolean accept(File selectedFile) {
                    final String extensionLowerCase = currentType[1].toLowerCase();
                    return (selectedFile.exists() && (selectedFile.isDirectory() || selectedFile.getName().toLowerCase().endsWith(extensionLowerCase)));
                }

                @Override
                public String getDescription() {
                    return currentType[0];
                }
            });
        }
        return fileFilterMap;
    }

    private void openDiagramActionPerformed(java.awt.event.ActionEvent evt) {
        final File[] selectedFilesArray = dialogHandler.showFileSelectBox("Open Diagram", false, true, getSvgFileFilter(), MessageDialogHandler.DialogueType.open, null);
        final Dimension parentSize = parentComponent.getSize();
        final Point parentLocation = parentComponent.getLocation();
        int offset = 10;
        final Rectangle windowRectangle = new Rectangle(parentLocation.x + offset, parentLocation.y + offset, parentSize.width - offset, parentSize.height - offset);
        if (selectedFilesArray != null) {
            for (File selectedFile : selectedFilesArray) {
                try {
                    diagramWindowManager.openDiagram(selectedFile.getName(), selectedFile.toURI(), true, windowRectangle);
                } catch (EntityServiceException entityServiceException) {
                    dialogHandler.addMessageDialogToQueue("Failed to create a new diagram: " + entityServiceException.getMessage(), "Open Diagram Error");
                }
            }
        }
    }

    private void saveDiagramActionPerformed(java.awt.event.ActionEvent evt) {
        int tabIndex = Integer.valueOf(evt.getActionCommand());
        SavePanel savePanel = diagramWindowManager.getSavePanel(tabIndex);
        savePanel.saveToFile();
    }

    private void saveDiagramAsActionPerformed(java.awt.event.ActionEvent evt) {
        final File[] selectedFilesArray = dialogHandler.showFileSelectBox("Save Diagram As", false, false, getSvgFileFilter(), MessageDialogHandler.DialogueType.save, null);
        if (selectedFilesArray != null) {
            for (File selectedFile : selectedFilesArray) {
                if (!selectedFile.getName().toLowerCase().endsWith(".svg")) {
                    selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".svg");
                }
                int tabIndex = Integer.valueOf(evt.getActionCommand());
                SavePanel savePanel = diagramWindowManager.getSavePanel(tabIndex);
                savePanel.saveToFile(selectedFile);
                RecentFileMenu.addRecentFile(sessionStorage, selectedFile);
                diagramWindowManager.setDiagramTitle(tabIndex, selectedFile.getName());
            }
        }
    }

    private void exitApplicationActionPerformed(java.awt.event.ActionEvent evt) {
        // check that things are saved and ask user if not
        if (diagramWindowManager.offerUserToSaveAll()) {
            System.exit(0);
        }
    }

    private void fileMenuMenuSelected(javax.swing.event.MenuEvent evt) {
        // set the save, save as and close text to include the tab to which the action will occur
        SavePanel savePanel = diagramWindowManager.getCurrentSavePanel(parentComponent);
        int selectedIndex = diagramWindowManager.getSavePanelIndex(parentComponent);
        String currentTabText = diagramWindowManager.getSavePanelTitle(selectedIndex);
        if (selectedIndex > -1) {
            saveDiagramAs.setText("Save As (" + currentTabText + ")");
            saveDiagramAs.setActionCommand(Integer.toString(selectedIndex));
            saveDiagram.setText("Save (" + currentTabText + ")");
            saveDiagram.setActionCommand(Integer.toString(selectedIndex));
            closeTabMenuItem.setText("Close (" + currentTabText + ")");
            closeTabMenuItem.setActionCommand(Integer.toString(selectedIndex));
            saveAsDefaultMenuItem.setText("Set Default Diagram as (" + currentTabText + ")");
            saveAsDefaultMenuItem.setActionCommand(Integer.toString(selectedIndex));
        }
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
        String diagramTitle = diagramWindowManager.getSavePanelTitle(tabIndex);
        boolean userCanceled = diagramWindowManager.offerUserToSave(savePanel, diagramTitle);
        if (!userCanceled) {
            diagramWindowManager.closeSavePanel(tabIndex);
        }
    }

    private void newDiagramMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        final Dimension parentSize = parentComponent.getSize();
        final Point parentLocation = parentComponent.getLocation();
        int offset = 10;
        try {
            diagramWindowManager.newDiagram(new Rectangle(parentLocation.x + offset, parentLocation.y + offset, parentSize.width - offset, parentSize.height - offset));
        } catch (EntityServiceException entityServiceException) {
            dialogHandler.addMessageDialogToQueue("Failed to create a new diagram: " + entityServiceException.getMessage(), "Open Diagram Error");
        }
    }

    private void importGedcomFileActionPerformed(java.awt.event.ActionEvent evt) {
        HashMap<String, FileFilter> fileFilterMap = new HashMap<String, FileFilter>(2);
        fileFilterMap.put("importfiles", new FileFilter() {
            @Override
            public boolean accept(File selectedFile) {
                if (selectedFile.isDirectory()) {
                    return true;
                }
                final String currentFileName = selectedFile.getName().toLowerCase();
                if (currentFileName.endsWith(".gedcom")) {
                    return true;
                }
                if (currentFileName.endsWith(".ged")) {
                    return true;
                }
                if (currentFileName.endsWith(".txt")) {
                    return true;
                }
                if (currentFileName.endsWith(".csv")) {
                    return true;
                }
                if (currentFileName.endsWith(".tip")) {
                    return true;
                }
                if (currentFileName.endsWith(".kinoath")) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "GEDCOM, CSV, TIP Kinship Data";
            }
        });
        File[] importFiles = dialogHandler.showFileSelectBox("Import Kinship Data", false, true, fileFilterMap, MessageDialogHandler.DialogueType.open, null);
        if (importFiles != null) {
            if (importFiles.length == 0) {
                dialogHandler.addMessageDialogToQueue("No files selected for import", "Import Kinship Data");
            } else {
                for (File importFile : importFiles) {
                    try {
                        diagramWindowManager.openImportPanel(importFile, parentComponent, getEntityCollection());
                    } catch (ImportException exception1) {
                        dialogHandler.addMessageDialogToQueue(exception1.getMessage() + "\n" + importFile.getAbsolutePath(), "Import File");
                    }
                }
            }
        }
    }

    private void importCsvFileActionPerformed(java.awt.event.ActionEvent evt) {
        importGedcomFileActionPerformed(evt);
    }

    private void importGedcomUrlActionPerformed(java.awt.event.ActionEvent evt) {
        // todo: Ticket #1297 either remove this or change it so it does not open so many tabs / windows
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
            try {
                diagramWindowManager.openImportPanel(importUrlString, parentComponent, getEntityCollection());
            } catch (ImportException exception1) {
                dialogHandler.addMessageDialogToQueue(exception1.getMessage() + "\n" + importUrlString, "Import File");
            }
        }
    }

    private void savePdfMenuItemActionPerformed(java.awt.event.ActionEvent evt) {

        final DiagramTranscoder diagramTranscoder = new DiagramTranscoder(diagramWindowManager.getCurrentSavePanel(parentComponent));
        DiagramTranscoderPanel diagramTranscoderPanel = new DiagramTranscoderPanel(diagramTranscoder);
        final File[] selectedFilesArray = dialogHandler.showFileSelectBox("Export as PDF/JPEG/PNG/TIFF", false, false, null, MessageDialogHandler.DialogueType.save, diagramTranscoderPanel);
        if (selectedFilesArray != null) {
            try {
                for (File selectedFile : selectedFilesArray) {
                    diagramTranscoder.exportDiagram(selectedFile);
                }
            } catch (TranscoderException exception) {
                dialogHandler.addMessageDialogToQueue(exception.getMessage() + "\nThis may occur when using the webstart version.", "Export Image Error");
                BugCatcherManager.getBugCatcher().logError(exception);
            } catch (IOException exception) {
                dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Export Image Error");
                BugCatcherManager.getBugCatcher().logError(exception);
            }

        }
    }

    private void exportToRActionPerformed(java.awt.event.ActionEvent evt) {
        SavePanel currentSavePanel = diagramWindowManager.getCurrentSavePanel(parentComponent);

        HashMap<String, FileFilter> fileFilterMap = new HashMap<String, FileFilter>(2);
        for (final String[] currentType : new String[][]{{"Data Frame Tab-separated Values", ".tab"}}) { // "Data Frame (CSV)"
            fileFilterMap.put(currentType[0], new FileFilter() {
                @Override
                public boolean accept(File selectedFile) {
                    final String extensionLowerCase = currentType[1].toLowerCase();
                    return (selectedFile.exists() && (selectedFile.isDirectory() || selectedFile.getName().toLowerCase().endsWith(extensionLowerCase)));
                }

                @Override
                public String getDescription() {
                    return currentType[0];
                }
            });
        }
        final File[] selectedFilesArray = dialogHandler.showFileSelectBox("Export Tab-separated Values", false, false, fileFilterMap, MessageDialogHandler.DialogueType.save, null);
        if (selectedFilesArray != null) {
            for (File selectedFile : selectedFilesArray) {
                if (!selectedFile.getName().toLowerCase().endsWith(".tab")) {
                    selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".tab");
                }
                new ExportToR(sessionStorage, dialogHandler).doExport(this, currentSavePanel, selectedFile);
            }
        }
    }

    private void entityUploadMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        diagramWindowManager.openEntityUploadPanel(null, getEntityCollection());
    }

    private void saveAsDefaultMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        int tabIndex = Integer.valueOf(evt.getActionCommand());
        SavePanel savePanel = diagramWindowManager.getSavePanel(tabIndex);
        savePanel.saveToFile(KinDiagramPanel.getDefaultDiagramFile(sessionStorage));
    }

    private EntityCollection getEntityCollection() {
        SavePanel currentSavePanel = diagramWindowManager.getCurrentSavePanel(parentComponent);
        if (currentSavePanel instanceof KinDiagramPanel) {
            final KinDiagramPanel diagramPanel = (KinDiagramPanel) currentSavePanel;
            return diagramPanel.getEntityCollection();
        } else {
            throw new UnsupportedOperationException("Cannot perform this menu action on this type of window");
        }
    }
}
