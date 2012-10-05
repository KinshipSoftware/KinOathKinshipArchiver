package nl.mpi.kinnate.userstorage;

import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.kinnate.KinOathVersion;

/**
 *  Document   : KinSessionStorage
 *  Created on : Dec 9, 2011, 10:20:43 AM
 *  Author     : Peter Withers
 */
public class KinSessionStorage extends ArbilSessionStorage {

    private ApplicationVersionManager versionManager;

    public KinSessionStorage(ApplicationVersionManager versionManager) {
        this.versionManager = versionManager;
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
    
}
