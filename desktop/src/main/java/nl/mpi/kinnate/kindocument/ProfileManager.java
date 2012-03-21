package nl.mpi.kinnate.kindocument;

import java.util.ArrayList;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader.ProfileSelection;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.ui.entityprofiles.CmdiProfileSelectionPanel;

/**
 *  Document   : ProfileManager
 *  Created on : Jan 30, 2012, 12:03:11 PM
 *  Author     : Peter Withers
 */
public class ProfileManager {

    private SessionStorage sessionStorage;
    private MessageDialogHandler dialogHandler;
    private CmdiProfileSelectionPanel cmdiProfileSelectionPanel;
    private ArrayList<String> selectedProfiles = new ArrayList<String>();

    public ProfileManager(SessionStorage sessionStorage, MessageDialogHandler dialogHandler) {
        this.sessionStorage = sessionStorage;
        this.dialogHandler = dialogHandler;
    }

    public void loadProfiles(final boolean forceUpdate, final CmdiProfileSelectionPanel cmdiProfileSelectionPanel) {
        this.cmdiProfileSelectionPanel = cmdiProfileSelectionPanel;
        CmdiProfileReader.getSingleInstance().setSelection(ProfileSelection.ALL);
        cmdiProfileSelectionPanel.setStatus(false, "Loading, please wait...", false);
        new Thread("loadProfiles") {

            @Override
            public void run() {
                ArrayList<String> problemProfiles = new ArrayList<String>();
                // load the profiles selected for use on this diagram
                for (String profileId : new String[]{"clarin.eu:cr1:p_1320657629627"}) { // todo: this array should come from the list of selected profiles in the diagram
                    cmdiProfileSelectionPanel.setStatus(false, "Loading: " + profileId + ", please wait...", false);
                    try {
                        preloadProfile(profileId, forceUpdate);
                    } catch (KinXsdException exception) {
                        problemProfiles.add(profileId);
                        BugCatcherManager.getBugCatcher().logError(exception);
                    }
                }
                // load the profile list from the clarin server
                cmdiProfileSelectionPanel.setStatus(false, "Loading the profile list from the server, please wait...", false);
                CmdiProfileReader cmdiProfileReader = CmdiProfileReader.getSingleInstance();
                cmdiProfileReader.refreshProfiles(forceUpdate);

                cmdiProfileSelectionPanel.setCmdiProfileReader(cmdiProfileReader, ProfileManager.this);
                if (!problemProfiles.isEmpty()) {
                    // todo: show a message dialogue
                    cmdiProfileSelectionPanel.setStatus(true, "There were " + problemProfiles.size() + " selected profiles that could not be retrieved", true);
                } else {
                    cmdiProfileSelectionPanel.setStatus(true, "", false);
                }
            }
        }.start();
    }

    private void preloadProfile(String profileId, boolean forceUpdate) throws KinXsdException {
//        File xsdFile = new File(sessionStorage.getCacheDirectory(), "individual" + "-" + profileId + ".xsd");
//        if (!xsdFile.exists() || forceUpdate) {
        new CmdiTransformer(sessionStorage).getXsd(profileId, forceUpdate);
//        }
    }

    public void addProfileSelection(final String profileId, final String displayName) {
        new Thread("addProfileSelection") {

            @Override
            public void run() {
                try {
                    cmdiProfileSelectionPanel.setStatus(false, "Loading, please wait...", false);
                    preloadProfile(profileId, false);
                    selectedProfiles.add(profileId);
//            return true;
                } catch (KinXsdException exception) {
                    BugCatcherManager.getBugCatcher().logError(exception);
                    dialogHandler.addMessageDialogToQueue("The selected profile (" + displayName + ") could not be loaded.", "Profile Selection Error");
//            return false;
                }
                cmdiProfileSelectionPanel.setStatus(true, "", false);
            }
        }.start();

    }

    public void removeProfileSelection(String profileId) {
        selectedProfiles.remove(profileId);
    }

    public boolean profileIsSelected(String profileId) {
        return selectedProfiles.contains(profileId);
    }
}
