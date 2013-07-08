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
package nl.mpi.kinnate.projects;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.ui.KinDiagramPanel;

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
            return (RecentProjects) unmarshaller.unmarshal(recentProjectsFile);
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
        if (updateInProjectDirectory) {
            // update the date in the recent projects list
            final RecentProjects recentProjectsList = getRecentProjectsList();
            recentProjectsList.updateProjectRecord(projectRecord);
            saveRecentProjectsList(recentProjectsList);
        }
    }

    public ProjectRecord loadProjectRecord(File projectDirectory) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ProjectRecord.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        final String kinoathproj = "kinoath.proj";
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

    private void checkProjectChangeDate(ProjectRecord databaseProjectRecord, ProjectRecord projectRecord, KinDiagramPanel diagramPanel) {
        if (!databaseProjectRecord.getLastChangeId().equals(projectRecord.getLastChangeId())) {
//            dialogHandler.addMessageDialogToQueue("The project '" + projectRecord.projectName + "' appears to be out of date, please check if there is another more recently modified version.", "KinOath Project Check");
//        } else if (databaseProjectRecord.getLastChangeDate().before(projectRecord.getLastChangeDate())) {
            if (JOptionPane.OK_OPTION == dialogHandler.showDialogBox("The project '" + projectRecord.projectName + "' has been modified externally,\ndo you want to update the database so that the changes are visible?", "KinOath Project Check", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                recreateDatabse(projectRecord, diagramPanel);
            }
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
}
