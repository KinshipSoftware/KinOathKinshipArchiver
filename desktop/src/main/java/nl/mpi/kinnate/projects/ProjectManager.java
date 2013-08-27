/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for
 * Psycholinguistics
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
package nl.mpi.kinnate.projects;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.ui.KinDiagramPanel;
import nl.mpi.kinnate.ui.ProjectPreviewPanel;

/**
 * Created on : Oct 22, 2011, 09:43
 *
 * @author Peter Withers
 */
public class ProjectManager {

    private final File recentProjectsFile;
    private ProjectRecord defaultProject = null; // should the default project be discarded and a mandatory import be required?
    private HashMap<ProjectRecord, EntityCollection> projectEntityCollectionMap = new HashMap<ProjectRecord, EntityCollection>();
    private final ArbilWindowManager dialogHandler;
    static final public String kinoathproj = "kinoath.proj";
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Widgets");

    public ProjectManager(SessionStorage sessionStorage, ArbilWindowManager dialogHandler) {
        recentProjectsFile = new File(sessionStorage.getApplicationSettingsDirectory(), "RecentProjects.xml");
        this.dialogHandler = dialogHandler;
    }

    // this should be replaced by the wizard that explains the difference between freeform diagrams and project diagrams
    @Deprecated
    public ProjectRecord getDefaultProject(SessionStorage sessionStorage) {
        if (defaultProject == null) {
            try {
                defaultProject = loadProjectRecord(sessionStorage.getProjectDirectory());
            } catch (JAXBException exception) {
                defaultProject = new ProjectRecord(sessionStorage.getProjectDirectory(), sessionStorage.getProjectDirectory().getName(), "nl-mpi-kinnate");
            }
        }
        return defaultProject;
    }

    public List<ProjectRecord> getProjectRecords(SessionStorage sessionStorage) throws JAXBException {
        return getRecentProjectsList().getProjectRecords();
    }

    public void moveProjectRecordToTop(ProjectRecord projectRecord, KinDiagramPanel diagramPanel) throws JAXBException {
        final RecentProjects recentProjectsList = getRecentProjectsList();
        checkProjectChangeDate(recentProjectsList, projectRecord, diagramPanel);
        recentProjectsList.moveProjectRecordToTop(projectRecord);
        saveRecentProjectsList(recentProjectsList);
    }

    public void clearRecentProjectsList() throws JAXBException {
        final RecentProjects recentProjectsList = getRecentProjectsList();
        recentProjectsList.clearList();
        saveRecentProjectsList(recentProjectsList);
        // todo: remove the unused databases
    }
    /*
     * todo: Ticket #2880 (new enhancement)
     * The open project window could also show which diagrams are known to use each project. Also it could show any known copies of a given project based on uuid.
     */

    public EntityCollection getEntityCollectionForProject(ProjectRecord projectRecord) throws EntityServiceException {
        if (projectEntityCollectionMap.containsKey(projectRecord)) {
            return projectEntityCollectionMap.get(projectRecord);
        }
        final EntityCollection entityCollection = new EntityCollection(this, projectRecord);
        projectEntityCollectionMap.put(projectRecord, entityCollection);
        return entityCollection;
    }

    public RecentProjects getRecentProjectsList() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(RecentProjects.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        if (recentProjectsFile.exists()) {
            final RecentProjects recentProjects = (RecentProjects) unmarshaller.unmarshal(recentProjectsFile);
            recentProjects.removeMissingProjects();
            return recentProjects;
        } else {
            return new RecentProjects();
        }
    }

    private void saveRecentProjectsList(RecentProjects recentProjects) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(RecentProjects.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(recentProjects, recentProjectsFile);
    }

    public void saveProjectRecord(ProjectRecord projectRecord, boolean updateInRecentList, boolean updateInProjectDirectory) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ProjectRecord.class);
        if (updateInProjectDirectory) {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(projectRecord, new File(projectRecord.projectDirectory, "kinoath.proj"));
        }
        if (updateInRecentList) {
            // update the date in the recent projects list
            final RecentProjects recentProjectsList = getRecentProjectsList();
            recentProjectsList.updateProjectRecord(projectRecord);
            saveRecentProjectsList(recentProjectsList);
        }
    }

    public ProjectRecord checkForMissingProject(ProjectRecord projectRecord) throws JAXBException {
        if (projectRecord.getProjectDirectory().exists()) {
            return projectRecord;
        } else {
            try {
                final RecentProjects recentProjectsList = getRecentProjectsList();
                for (ProjectRecord recentProject : recentProjectsList.recentProjects) {
                    if (recentProject.getProjectUUID().equals(projectRecord.getProjectUUID()) && recentProject.getProjectDirectory().exists()) {
                        return recentProject;
                    }
                }
            } catch (JAXBException exception) {
                // if this fails we must ask the user to browse for the required project
            }
        }
        // all else failed so we ask the user to browse for the matching project
        ProjectPreviewPanel previewPanel = new ProjectPreviewPanel(this, false);
        dialogHandler.showDialogBox(widgets.getString("THE PROJECT FOR THIS DIAGRAM COULD NOT BE FOUND.PLEASE BROWSE FOR THE REQUIRED PROJECT."), widgets.getString("OPEN PROJECT ERROR"), JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE);
        final File[] selectedFilesArray = dialogHandler.showFileSelectBox(widgets.getString("OPEN PROJECT"), false, false, getProjectFileFilter(), MessageDialogHandler.DialogueType.open, previewPanel);
        if (selectedFilesArray != null) {
            System.out.println(selectedFilesArray[0].getAbsolutePath());
            return loadProjectRecord(selectedFilesArray[0]);
        }
        return projectRecord;
    }

    public ProjectRecord loadProjectRecord(File projectDirectory) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ProjectRecord.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        File projectFile;
        if (projectDirectory.isFile() && kinoathproj.equals(projectDirectory.getName())) {
            projectFile = projectDirectory;
        } else {
            projectFile = new File(projectDirectory, kinoathproj);
        }
        final ProjectRecord projectRecord = (ProjectRecord) unmarshaller.unmarshal(projectFile);
        projectRecord.setProjectDirectory(projectFile.getParentFile());
        return projectRecord;
    }

    private void checkProjectChangeDate(RecentProjects recentProjectsList, ProjectRecord projectRecord, KinDiagramPanel diagramPanel) {
        for (ProjectRecord recentProjectRecord : recentProjectsList.getProjectRecords()) {
            if (recentProjectRecord.equals(projectRecord)) {
                checkProjectChangeDate(recentProjectRecord, projectRecord, diagramPanel);
                return;
            }
        }
        // if we arrived here then the project is not in the recent list and we do not know if the database is up to date, so we offer to recreate the database
//        if (JOptionPane.OK_OPTION == dialogHandler.showDialogBox("The project '" + projectRecord.projectName + "' is not in your recent projects list, it is\nrecommended that you create / update the database.\nDo you want to do this now?", "KinOath Project Check", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
        //dialogHandler.addMessageDialogToQueue("Creating database for project '" + projectRecord.projectName + "'.", "KinOath Project");
        recreateDatabse(projectRecord, diagramPanel);
//        }
    }

    private void checkProjectChangeDate(final ProjectRecord databaseProjectRecord, final ProjectRecord projectRecord, final KinDiagramPanel diagramPanel) {
        if (!databaseProjectRecord.getLastChangeId().equals(projectRecord.getLastChangeId())) {
            new Thread(new Runnable() {
                public void run() {
                    final String message = java.text.MessageFormat.format(widgets.getString("THE PROJECT '{0}' HAS BEEN MODIFIED EXTERNALLY,DO YOU WANT TO UPDATE THE DATABASE SO THAT THE CHANGES ARE VISIBLE?"), new Object[]{projectRecord.projectName});
                    if (JOptionPane.OK_OPTION == dialogHandler.showDialogBox(message, widgets.getString("KINOATH PROJECT CHECK"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                        try {
                            diagramPanel.showProgressBar();
                            getEntityCollectionForProject(projectRecord).recreateDatabase();
//                    dialogHandler.addMessageDialogToQueue("Reindexing complete.", "KinOath Project Check");
                        } catch (EntityServiceException exception) {
                            dialogHandler.addMessageDialogToQueue("Database update failed: " + exception, "KinOath Project Check");
                        }
                        diagramPanel.clearProgressBar();
                    }
                }
            }).start();
        }
    }

    private void recreateDatabse(final ProjectRecord projectRecord, final KinDiagramPanel diagramPanel) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    diagramPanel.showProgressBar();
                    getEntityCollectionForProject(projectRecord).recreateDatabase();
//                    dialogHandler.addMessageDialogToQueue("Reindexing complete.", "KinOath Project Check");
                } catch (EntityServiceException exception) {
                    dialogHandler.addMessageDialogToQueue("Database update failed: " + exception, "KinOath Project Check");
                }
                diagramPanel.clearProgressBar();
            }
        }).start();
    }

    public HashMap<String, FileFilter> getProjectFileFilter() {
        HashMap<String, FileFilter> fileFilterMap = new HashMap<String, FileFilter>(2);
        for (final String[] currentType : new String[][]{{widgets.getString("KINOATH PROJECT"), "kinoath.proj"}}) {
            fileFilterMap.put(currentType[0], new FileFilter() {
                @Override
                public boolean accept(File selectedFile) {
//                    System.out.println("selectedFile: " + selectedFile);
                    if (selectedFile.isDirectory()) {
                        return true;
                    }
                    try {
                        final ProjectRecord projectRecord = loadProjectRecord(selectedFile);
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
}
