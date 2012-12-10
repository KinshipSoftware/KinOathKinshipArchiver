package nl.mpi.kinnate.projects;

import nl.mpi.arbil.userstorage.SessionStorage;

/**
 * Created on : Oct 22, 2011, 09:43
 *
 * @author Peter Withers
 */
public class ProjectManager {

    private ProjectRecord[] projectRecords;
    private ProjectRecord defaultProject = null; // should the default project be discarded and a mandatory import be required?

    public ProjectManager() {
//    this.projectRecords = projectRecords;
    }

    public ProjectRecord getDefaultProject(SessionStorage sessionStorage) {
        if (defaultProject == null) {
            defaultProject = new ProjectRecord(sessionStorage.getProjectDirectory(), "nl-mpi-kinnate");
        }
        return defaultProject;
    }

    public ProjectRecord[] getProjectRecords(SessionStorage sessionStorage) {
        return projectRecords;
    }
}
