package nl.mpi.kinnate.userstorage;

import java.io.File;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.kinnate.KinOathVersion;
import nl.mpi.kinnate.projects.ProjectRecord;

/**
 * Created on : Dec 9, 2011, 10:20:43 AM
 *
 * @author : Peter Withers
 */
public class KinSessionStorage extends ArbilSessionStorage /* CommonsSessionStorage */ {

    final private ApplicationVersionManager versionManager;
    private ProjectRecord projectRecord = null;

    public KinSessionStorage(ApplicationVersionManager versionManager) {
        this.versionManager = versionManager;
    }

    public void setProjectRecord(ProjectRecord projectRecord) {
        this.projectRecord = projectRecord;
        getProjectWorkingDirectory().mkdir();
    }

    // The major, minor version numbers will change the working directory name so that each minor version requires
    // an export import operation allowing the internal data structure to be changed. When the internal data structure
    // is stable the minor version can be replaced with an x so that the directory does not change. Exporting will
    // require the use of the old version of the application and this could be achieved by creating a jnlp for the
    // old jars and an export dialog instead of the main application.
    @Override
    protected String[] getAppDirectoryAlternatives() {
        return new String[]{".kinoath-" + new KinOathVersion().currentMajor + "-" + new KinOathVersion().currentMinor};
    }
    // todo: remove ArbilWorkingFiles from the working files path and use KinshipData or such like

    @Override
    public File getProjectDirectory() {
        if (projectRecord == null) {
            return super.getProjectDirectory();
        }
        return projectRecord.getProjectDirectory();
    }

    @Override
    public File getProjectWorkingDirectory() {
        if (projectRecord == null) {
            return super.getProjectWorkingDirectory();
        }
        return new File(projectRecord.getProjectDirectory(), "KinDataFiles");
    }
}
