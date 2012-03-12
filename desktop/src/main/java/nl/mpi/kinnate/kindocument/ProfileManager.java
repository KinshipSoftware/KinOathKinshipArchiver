package nl.mpi.kinnate.kindocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader.ProfileSelection;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.ui.entityprofiles.CmdiProfileSelectionPanel;

/**
 *  Document   : ProfileManager
 *  Created on : Jan 30, 2012, 12:03:11 PM
 *  Author     : Peter Withers
 */
public class ProfileManager {

    private SessionStorage sessionStorage;

    public ProfileManager(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public void loadProfiles(final boolean forceUpdate, final CmdiProfileSelectionPanel cmdiProfileSelectionPanel) {
        CmdiProfileReader.getSingleInstance().setSelection(ProfileSelection.ALL);
        cmdiProfileSelectionPanel.setStatus(false, "Loading, please wait...", false);
        new Thread("loadProfiles") {

            @Override
            public void run() {
                ArrayList<String> problemProfiles = new ArrayList<String>();
                // load the profiles selected for use on this diagram
                for (String profileId : new String[]{"clarin.eu:cr1:p_1320657629627"}) { // todo: this array should come from the list of selected profiles in the diagram
                    try {
                        cmdiProfileSelectionPanel.setStatus(false, "Loading: " + profileId + ", please wait...", false);
                        File xsdFile = new File(sessionStorage.getCacheDirectory(), "individual" + "-" + profileId + ".xsd");
                        if (!xsdFile.exists() || forceUpdate) {
                            new CmdiTransformer(sessionStorage).transformProfileXmlToXsd(xsdFile, profileId);
                        }
                    } catch (IOException exception) {
                        problemProfiles.add(profileId);
                        BugCatcherManager.getBugCatcher().logError(exception);
                    } catch (TransformerException exception) {
                        problemProfiles.add(profileId);
                        BugCatcherManager.getBugCatcher().logError(exception);
                    }
                }
                // load the profile list from the clarin server
                cmdiProfileSelectionPanel.setStatus(false, "Loading the profile list from the server, please wait...", false);
                CmdiProfileReader cmdiProfileReader = CmdiProfileReader.getSingleInstance();
                cmdiProfileReader.refreshProfilesAndUpdateCache(cmdiProfileSelectionPanel.getProfileReloadProgressBar(), forceUpdate);

                cmdiProfileSelectionPanel.setCmdiProfileReader(cmdiProfileReader);
                if (!problemProfiles.isEmpty()) {
                    // todo: show a message dialogue
                    cmdiProfileSelectionPanel.setStatus(true, "There were " + problemProfiles.size() + " selected profiles that could not be retrieved", true);
                } else {
                    cmdiProfileSelectionPanel.setStatus(true, "", false);
                }
            }
        }.start();
    }
}
