package nl.mpi.kinnate.export;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 *  Document   : ModifiedFileSearch
 *  Created on : Jun 30, 2011, 9:54:30 AM
 *  Author     : Peter Withers
 */
public class ModifiedFileSearch {

    public enum SearchType {

        imdi, cmdi, kmdi, all
    }
    private String searchString = "mdi.0";

    public void setSearchType(SearchType searchType) {
        switch (searchType) {
            case cmdi:
                searchString = "cmdi.0";
                break;
            case imdi:
                searchString = "imdi.0";
                break;
            case kmdi:
                searchString = "kmdi.0";
                break;
            case all:
                searchString = "mdi.0";
                break;
        }
    }

    public ArrayList<File> getModifiedFiles(File currentDirectory) {
        ArrayList<File> modifiedFileList = new ArrayList<File>();
        File[] currentListing = currentDirectory.listFiles();
        for (File currentFile : currentListing) {
            if (currentFile.getName().endsWith(searchString)) {
                File targetFile = new File(currentFile.getParentFile(), currentFile.getName().replaceAll("mdi\\.0$", "mdi"));
                modifiedFileList.add(targetFile);
            }
            if (!currentFile.isFile()) {
                modifiedFileList.addAll(getModifiedFiles(currentFile));
            }
        }
        return modifiedFileList;
    }

    public void stripHistoryFiles(final File targetFile) {
        String[] historyFileArray = targetFile.getParentFile().list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                // list only history files
                return name.matches(targetFile.getName() + "\\.[0-9]+$");
            }
        });
        if (historyFileArray != null) {
            for (String currentHistory : historyFileArray) {
                System.out.println(currentHistory);
                if (currentHistory.matches("[^\\^/]+mdi\\.[0-9]+$")) {
                    // todo: are there any other checks we can do to make certain that don't get a rogue delete occur
                    new File(targetFile.getParentFile(), currentHistory).delete();
                }
            }
        }
    }
}
