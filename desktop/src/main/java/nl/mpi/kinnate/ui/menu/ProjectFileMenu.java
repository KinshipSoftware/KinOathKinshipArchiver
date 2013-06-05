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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBException;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.projects.ProjectManager;
import nl.mpi.kinnate.projects.ProjectRecord;
import nl.mpi.kinnate.ui.ProjectPreviewPanel;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;

/**
 * Created on : Oct 22, 2011, 09:14:39 AM
 *
 * @author Peter Withers
 */
public class ProjectFileMenu extends JMenu implements ActionListener {

    private final AbstractDiagramManager diagramWindowManager;
    private final Component parentComponent;
    private final MessageDialogHandler dialogHandler;
    final private ProjectManager projectManager;

    public ProjectFileMenu(AbstractDiagramManager diagramWindowManager, Component parentComponent, MessageDialogHandler dialogHandler, ProjectManager projectManager) {
        this.diagramWindowManager = diagramWindowManager;
        this.parentComponent = parentComponent;
        this.dialogHandler = dialogHandler;
        this.projectManager = projectManager;
        this.setText("Open Recent Project");
        this.addMenuListener(new MenuListener() {
            public void menuCanceled(MenuEvent evt) {
            }

            public void menuDeselected(MenuEvent evt) {
            }

            public void menuSelected(MenuEvent evt) {
                setupMenu();
            }
        });
    }

    private void setupMenu() {
        this.removeAll();
        try {
            for (ProjectRecord projectRecord : projectManager.getRecentProjectsList().getProjectRecords()) {
//                String currentFilePath = recentProjectFileArray[currentIndex];
                JMenuItem currentMenuItem = new JMenuItem(projectRecord.getProjectName());
                currentMenuItem.setToolTipText(projectRecord.getProjectDirectory().getAbsolutePath());
                currentMenuItem.setActionCommand(projectRecord.getProjectDirectory().getAbsolutePath());
                currentMenuItem.addActionListener(this);
                this.add(currentMenuItem);
            }
            this.add(new JSeparator());
            JMenuItem clearMenuItem = new JMenuItem("Clear List");
            clearMenuItem.setActionCommand("Clear List");
            clearMenuItem.addActionListener(this);
            this.add(clearMenuItem);
        } catch (JAXBException exception) {
            JMenuItem currentMenuItem = new JMenuItem("<recent projects could not be found>");
            currentMenuItem.setEnabled(false);
            this.add(currentMenuItem);
        }
    }

    private HashMap<String, FileFilter> getProjectFileFilter() {
        HashMap<String, FileFilter> fileFilterMap = new HashMap<String, FileFilter>(2);
        for (final String[] currentType : new String[][]{{"KinOath Project", "kinoath.proj"}}) {
            fileFilterMap.put(currentType[0], new FileFilter() {
                @Override
                public boolean accept(File selectedFile) {
//                    System.out.println("selectedFile: " + selectedFile);
                    try {
                        final ProjectRecord projectRecord = projectManager.loadProjectRecord(selectedFile);
                        if (projectRecord == null) {
                            return false;
                        }
                        return true;
                    } catch (JAXBException exception) {
                        // if we cannot read the project file then we cannot open the project
                        return false;
                    }
//                    } else {
//                    return (selectedFile.exists() && (selectedFile.isDirectory()));
//                    }
                }

                @Override
                public String getDescription() {
                    return currentType[0];
                }
            });
        }
        return fileFilterMap;
    }

    private void openProject(ProjectRecord projectRecord) {
        final Dimension parentSize = parentComponent.getSize();
        final Point parentLocation = parentComponent.getLocation();
        int offset = 10;
        try {
            diagramWindowManager.newDiagram(new Rectangle(parentLocation.x + offset, parentLocation.y + offset, parentSize.width - offset, parentSize.height - offset), projectRecord);
        } catch (EntityServiceException entityServiceException) {
            dialogHandler.addMessageDialogToQueue("Failed to create a new diagram: " + entityServiceException.getMessage(), "Open Diagram Error");
        }
    }

    public void actionPerformed(ActionEvent e) {
        if ("new".equals(e.getActionCommand())) {
//            ProjectPreviewPanel previewPanel = new ProjectPreviewPanel(true);
            final File[] selectedFilesArray = dialogHandler.showFileSelectBox("New Project", true, false, getProjectFileFilter(), MessageDialogHandler.DialogueType.save, null);
            if (selectedFilesArray != null) {
                final File selecteFile = selectedFilesArray[0];
                System.out.println(selecteFile.getAbsolutePath());
                if (selecteFile.exists()) {
                    // cannot use an existing file
                    // offer a project edit box
                    dialogHandler.addMessageDialogToQueue("The selected file already exists, please enter a unique name.", "Create Project");
                } else {
                    ProjectRecord projectRecord = new ProjectRecord(selecteFile, selecteFile.getName());
                    try {
                        selecteFile.mkdir();
                        projectManager.saveProjectRecord(projectRecord);
                        openProject(projectRecord);
                    } catch (JAXBException exception) {
                        dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Create Project Error");
                    }
                }
            }

        } else if ("browse".equals(e.getActionCommand())) {
            ProjectPreviewPanel previewPanel = new ProjectPreviewPanel(projectManager, false);
            final File[] selectedFilesArray = dialogHandler.showFileSelectBox("Open Project", false, false, getProjectFileFilter(), MessageDialogHandler.DialogueType.open, previewPanel);
            if (selectedFilesArray != null) {
                System.out.println(selectedFilesArray[0].getAbsolutePath());
                try {
                    openProject(projectManager.loadProjectRecord(selectedFilesArray[0]));
                } catch (JAXBException exception) {
                    dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Read Project Record Error");
                }
            }

        } else if ("Clear List".equals(e.getActionCommand())) {
            try {
                projectManager.clearRecentProjectsList();
            } catch (JAXBException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
            }
        } else {
            try {
                final String actionString = e.getActionCommand();
                final File recentProjectFile = new File(actionString);
                final ProjectRecord selectedProjectRecord = projectManager.loadProjectRecord(recentProjectFile);
                openProject(selectedProjectRecord);
            } catch (JAXBException exception) {
                dialogHandler.addMessageDialogToQueue("Failed to open project: " + exception.getMessage(), "Open Project Error");
            }
        }
    }
}
