package nl.mpi.kinnate.kindocument;

import java.util.ArrayList;
import java.util.Arrays;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader.ProfileSelection;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.ui.entityprofiles.CmdiProfileSelectionPanel;
import nl.mpi.kinnate.ui.entityprofiles.ProfileRecord;

/**
 *  Document   : ProfileManager
 *  Created on : Jan 30, 2012, 12:03:11 PM
 *  Author     : Peter Withers
 */
public class ProfileManager {

    private SessionStorage sessionStorage;
    private MessageDialogHandler dialogHandler;
    private GraphPanel graphPanel;
    private CmdiProfileSelectionPanel cmdiProfileSelectionPanel;

    public ProfileManager(SessionStorage sessionStorage, MessageDialogHandler dialogHandler) {
        this.sessionStorage = sessionStorage;
        this.dialogHandler = dialogHandler;
    }

    public void loadProfiles(final boolean forceUpdate, final CmdiProfileSelectionPanel cmdiProfileSelectionPanel, final GraphPanel graphPanel) {
        this.cmdiProfileSelectionPanel = cmdiProfileSelectionPanel;
        this.graphPanel = graphPanel;
        CmdiProfileReader.getSingleInstance().setSelection(ProfileSelection.ALL);
        cmdiProfileSelectionPanel.setStatus(false, "Loading, please wait...", false);
        new Thread("loadProfiles") {

            @Override
            public void run() {
                ArrayList<String> problemProfiles = new ArrayList<String>();
                // load the profiles selected for use on this diagram
                for (ProfileRecord profileRecord : graphPanel.dataStoreSvg.selectedProfiles) {
                    cmdiProfileSelectionPanel.setStatus(false, "Loading: " + profileRecord.profileName + ", please wait...", false);
                    try {
                        preloadProfile(profileRecord.profileId, forceUpdate);
                    } catch (KinXsdException exception) {
                        problemProfiles.add(profileRecord.profileId);
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

    public void addProfileSelection(final String profileId, final String profileName) {
        new Thread("addProfileSelection") {

            @Override
            public void run() {
                try {
                    cmdiProfileSelectionPanel.setStatus(false, "Loading, please wait...", false);
                    preloadProfile(profileId, false);
                    ArrayList<ProfileRecord> selectedProfiles = new ArrayList<ProfileRecord>(Arrays.asList(graphPanel.dataStoreSvg.selectedProfiles));
                    selectedProfiles.add(new ProfileRecord(profileName, profileId));
                    graphPanel.dataStoreSvg.selectedProfiles = selectedProfiles.toArray(new ProfileRecord[]{});
                    graphPanel.setRequiresSave();
                } catch (KinXsdException exception) {
                    BugCatcherManager.getBugCatcher().logError(exception);
                    dialogHandler.addMessageDialogToQueue("The selected profile (" + profileName + ") could not be loaded.", "Profile Selection Error");
                }
                cmdiProfileSelectionPanel.setStatus(true, "", false);
            }
        }.start();
    }

    public void removeProfileSelection(String profileId) {
        ArrayList<ProfileRecord> selectedProfiles = new ArrayList<ProfileRecord>();
        for (ProfileRecord profileRecord : graphPanel.dataStoreSvg.selectedProfiles) {
            if (!profileRecord.profileId.equals(profileId)) {
                selectedProfiles.add(profileRecord);
            }
        }
        graphPanel.dataStoreSvg.selectedProfiles = selectedProfiles.toArray(new ProfileRecord[]{});
        graphPanel.setRequiresSave();
    }

    public boolean profileIsSelected(String profileId) {
        for (ProfileRecord profileRecord : graphPanel.dataStoreSvg.selectedProfiles) {
            if (profileRecord.profileId.equals(profileId)) {
                return true;
            }
        }
        return false;
    }
}
