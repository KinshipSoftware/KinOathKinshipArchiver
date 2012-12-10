package nl.mpi.kinnate.projects;

import java.io.File;
import java.util.UUID;

/**
 * Created on : Oct 22, 2011, 09:33
 *
 * @author Peter Withers
 */
public class ProjectRecord {

    final private File projectDirectory;
//    final private String projectName;
    final private String projectUUID;

//    public ProjectRecord(File projectDirectory, String projectName) {
//        this.projectDirectory = projectDirectory;
//        this.projectName = projectName;
//        this.projectUUID = UUID.randomUUID().toString();
//    }
    public ProjectRecord(File projectDirectory) {
        this.projectDirectory = projectDirectory;
        this.projectUUID = UUID.randomUUID().toString();
    }

    public ProjectRecord(File projectDirectory, String projectUUID) {
        this.projectDirectory = projectDirectory;
        this.projectUUID = projectUUID;
    }

    public File getProjectDirectory() {
        return projectDirectory;
    }

    public String getProjectUUID() {
        return projectUUID;
    }
}
