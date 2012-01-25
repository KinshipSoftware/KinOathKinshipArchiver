package nl.mpi.kinnate.userstorage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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
    // #1305 Refactoring now needs to be done to accomodate the latest changes from Arbil trunk.
    public String[] getLocationOptions() {
        // todo: the use of new KinOathVersion() must be removed once the refactoring is complete
        String directoryName = ".kinoath-" + new KinOathVersion().currentMajor + "-" + new KinOathVersion().currentMinor;
        String[] locationOptions = new String[]{
            System.getProperty("user.home") + File.separatorChar + "Local Settings" + File.separatorChar + "Application Data" + File.separatorChar + directoryName + File.separatorChar,
            System.getenv("APPDATA") + File.separatorChar + directoryName + File.separatorChar,
            System.getProperty("user.home") + File.separatorChar + directoryName + File.separatorChar,
            System.getenv("USERPROFILE") + File.separatorChar + directoryName + File.separatorChar,
            System.getProperty("user.dir") + File.separatorChar + directoryName + File.separatorChar,};
        List<String> uniqueArray = new ArrayList<String>();
        uniqueArray.addAll(Arrays.asList(locationOptions));
        for (Iterator<String> iterator = uniqueArray.iterator(); iterator.hasNext();) {
            String element = iterator.next();
            if (element.startsWith("null")) {
                iterator.remove();
            }
        }
        locationOptions = uniqueArray.toArray(new String[]{});
        for (String currentLocationOption : locationOptions) {
            System.out.println("LocationOption: " + currentLocationOption);
        }
        return locationOptions;
    }
}
