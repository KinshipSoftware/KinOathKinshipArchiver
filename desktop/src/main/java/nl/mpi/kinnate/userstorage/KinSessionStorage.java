package nl.mpi.kinnate.userstorage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;

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

    @Override
    public String[] getLocationOptions() {
        String directoryName = ".kinoath-" + versionManager.getApplicationVersion().currentMajor + "-" + versionManager.getApplicationVersion().currentMinor;
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