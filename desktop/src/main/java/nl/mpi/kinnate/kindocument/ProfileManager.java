/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.kindocument;

import java.util.ArrayList;
import java.util.Arrays;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader.ProfileSelection;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.ui.entityprofiles.CmdiProfileSelectionPanel;
import nl.mpi.kinnate.ui.entityprofiles.ProfileRecord;

/**
 * Document : ProfileManager Created on : Jan 30, 2012, 12:03:11 PM
 *
 * @author Peter Withers
 */
public class ProfileManager {

    private SessionStorage sessionStorage;
    private MessageDialogHandler dialogHandler;
    private GraphPanel graphPanel;
    private CmdiProfileSelectionPanel cmdiProfileSelectionPanel;
    private ArrayList<nl.mpi.arbil.clarin.profiles.CmdiProfileReader.CmdiProfile> cmdiProfileArray = new ArrayList<nl.mpi.arbil.clarin.profiles.CmdiProfileReader.CmdiProfile>();

    public ProfileManager(SessionStorage sessionStorage, MessageDialogHandler dialogHandler) {
        this.sessionStorage = sessionStorage;
        this.dialogHandler = dialogHandler;
    }

    public void loadProfiles(final boolean forceUpdate, final CmdiProfileSelectionPanel cmdiProfileSelectionPanel, final GraphPanel graphPanel) {
        this.cmdiProfileSelectionPanel = cmdiProfileSelectionPanel;
        this.graphPanel = graphPanel;
        nl.mpi.arbil.clarin.profiles.CmdiProfileReader.getSingleInstance().setSelection(ProfileSelection.ALL);
        cmdiProfileSelectionPanel.setProfileManager(ProfileManager.this);
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
                nl.mpi.arbil.clarin.profiles.CmdiProfileReader cmdiProfileReader = nl.mpi.arbil.clarin.profiles.CmdiProfileReader.getSingleInstance();
                cmdiProfileReader.refreshProfiles(forceUpdate);

                // find any selected profiles that are not in the list of profiles from the server
                ArrayList<ProfileRecord> selectedProfiles = new ArrayList<ProfileRecord>(Arrays.asList(graphPanel.dataStoreSvg.selectedProfiles));
                for (nl.mpi.arbil.clarin.profiles.CmdiProfileReader.CmdiProfile currentProfile : cmdiProfileReader.cmdiProfileArray) {
                    for (ProfileRecord profileRecord : selectedProfiles.toArray(new ProfileRecord[0])) {
                        if (profileRecord.profileId.equals(currentProfile.id)) {
                            selectedProfiles.remove(profileRecord);
                        }
                    }
                }
                cmdiProfileArray.addAll(cmdiProfileReader.cmdiProfileArray);
                // add any profiles into the list that are in use but are not published
                for (ProfileRecord profileRecord : selectedProfiles) {
                    final nl.mpi.arbil.clarin.profiles.CmdiProfileReader.CmdiProfile cmdiProfile = new nl.mpi.arbil.clarin.profiles.CmdiProfileReader.CmdiProfile();
                    cmdiProfile.id = profileRecord.profileId;
                    cmdiProfile.name = profileRecord.profileName;
                    final String unpublished_Profile = "Unpublished Profile";
                    cmdiProfile.description = unpublished_Profile;
                    cmdiProfile.creatorName = unpublished_Profile;
                    cmdiProfile.href = unpublished_Profile;
                    cmdiProfile.registrationDate = "------------";

                    cmdiProfileArray.add(0, cmdiProfile);
                }
                cmdiProfileSelectionPanel.setProfileManager(ProfileManager.this);
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

    public int getProfileCount() {
        return cmdiProfileArray.size();
    }

    public nl.mpi.arbil.clarin.profiles.CmdiProfileReader.CmdiProfile getProfileAt(int index) {
        return cmdiProfileArray.get(index);
    }
}
