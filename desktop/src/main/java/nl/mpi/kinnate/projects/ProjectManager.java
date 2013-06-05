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
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.EntityServiceException;

/**
 * Created on : Oct 22, 2011, 09:43
 *
 * @author Peter Withers
 */
public class ProjectManager {

    private final File recentProjectsFile;
    private ProjectRecord defaultProject = null; // should the default project be discarded and a mandatory import be required?

    public ProjectManager(SessionStorage sessionStorage) {
        recentProjectsFile = new File(sessionStorage.getApplicationSettingsDirectory(), "RecentProjects.xml");
    }

    // this should be replaced by the wizard that explains the difference between freeform diagrams and project diagrams
    @Deprecated
    public ProjectRecord getDefaultProject(SessionStorage sessionStorage) {
        if (defaultProject == null) {
            defaultProject = new ProjectRecord(sessionStorage.getProjectDirectory(), "nl-mpi-kinnate", "nl-mpi-kinnate");
        }
        return defaultProject;
    }

    public List<ProjectRecord> getProjectRecords(SessionStorage sessionStorage) throws JAXBException {
        return getRecentProjectsList().getProjectRecords();
    }

    public void addRecentProjectRecord(ProjectRecord projectRecord) throws JAXBException {
        final RecentProjects recentProjectsList = getRecentProjectsList();
        recentProjectsList.addProjectRecord(projectRecord);
        saveRecentProjectsList(recentProjectsList);
    }

    public void clearRecentProjectsList() throws JAXBException {
        final RecentProjects recentProjectsList = getRecentProjectsList();
        recentProjectsList.clearList();
        saveRecentProjectsList(recentProjectsList);
    }
    /*
     * todo: Ticket #2880 (new enhancement)
     * The open project window could also show which diagrams are known to use each project. Also it could show any known copies of a given project based on uuid.
     */

    public EntityCollection getEntityCollectionForProject(ProjectRecord projectRecord) throws EntityServiceException {
//         todo: keep track of these collections so that the db does not get locking errors
        throw new EntityServiceException("Test throw of EntityServiceException");
//        return new EntityCollection(projectRecord);
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

    public void saveRecentProjectsList(RecentProjects recentProjects) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(RecentProjects.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(recentProjects, recentProjectsFile);
    }

    public void saveProjectRecord(ProjectRecord projectRecord) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ProjectRecord.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(projectRecord, new File(projectRecord.projectDirectory, "kinoath.proj"));
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
}
