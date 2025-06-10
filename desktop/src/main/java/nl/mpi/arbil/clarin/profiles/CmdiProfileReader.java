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
package nl.mpi.arbil.clarin.profiles;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JProgressBar;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import org.apache.commons.digester.Digester;

/**
 * CmdiProfileReader.java
 * Created on February 1, 2010, 14:22:03
 * @author Peter.Withers@mpi.nl
 */
public class CmdiProfileReader implements CmdiProfileProvider {

    private static SessionStorage sessionStorage;
    public static final String PARAM_PROFILESELECTION = "profileSelection";
    public static final String PARAM_PROFILES_URL = "profilesUrlAll";
    public static final String PARAM_SELECTED_PROFILES_URL = "profilesUrlSelected";
    public final static String DEFAULT_ALL_PROFILES_URL_STRING = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles";
    public final static String DEFAULT_SELECTED_PROFILES_URL_STRING = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles?mdEditor=true";
    private ProfileSelection selection;

    /**
     * Indication of profiles set to download (all or selected for manual editing)
     */
    public static enum ProfileSelection {

	SELECTED,
	ALL
    };

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    public ArrayList<CmdiProfile> cmdiProfileArray = null;
    static CmdiProfileReader singleInstance = null;

    static synchronized public CmdiProfileReader getSingleInstance() {
	// make sure the profiles xml need only be read once per session
	if (singleInstance == null) {
	    singleInstance = new CmdiProfileReader(getStoredProfileSelection());
	}
	return singleInstance;
    }

    public static boolean pathIsProfile(String pathString) {
	// TODO: make this smarter (this only needs to determin if the path is to a template file or an xsd)
	return (pathString.startsWith("http") || pathString.contains("clarin") || pathString.endsWith(".xsd") || pathString.endsWith("/xsd"));
    }

    @Override
    public CmdiProfile getProfile(String XsdHref) {
	for (CmdiProfile currentProfile : cmdiProfileArray) {
	    if (currentProfile.getXsdHref().equals(XsdHref)) {
		return currentProfile;
	    }
	}
	return null;
    }

    private CmdiProfileReader(ProfileSelection selection) {
	setSelection(selection);
	loadProfiles(getProfilesUrlStringForSelection(getSelection()));
    }

    /**
     * Refreshes profiles list but does not update/create cached schemas
     * @param forceUpdate Force update even if cached copies not out of date
     */
    public void refreshProfiles(boolean forceUpdate) {
	refreshProfiles(forceUpdate, false, null);
    }

    /**
     * Refreshes profiles list and creates or updates cached schemas for all profiles
     * @param progressBar Progress bar to monitor progress
     * @param forceUpdate whether to force update even if cached copies not out of date
     */
    public void refreshProfilesAndUpdateCache(JProgressBar progressBar, boolean forceUpdate) {
	refreshProfiles(forceUpdate, true, progressBar);
    }

    /**
     * 
     * @param forceUpdate whether to force update even if cached copies not out of date
     * @param updateCache whether to update the profiles cache as well
     * @param progressBar progress bar for monitoring state of cache update. Null allowed if and only if {@code updateCache == null}
     */
    private void refreshProfiles(boolean forceUpdate, boolean updateCache, JProgressBar progressBar) {
	if (updateCache) {
	    progressBar.setIndeterminate(true);
	    progressBar.setString("");
	}
	int updateDays;
	if (forceUpdate) {
	    updateDays = 0;
	} else {
	    updateDays = 100;
	}
	sessionStorage.updateCache(getProfilesUrlStringForSelection(getSelection()), updateDays, false);
	loadProfiles(getProfilesUrlStringForSelection(getSelection()));

	if (updateCache) {
	    storeProfilesInCache(updateDays, cmdiProfileArray, progressBar);
	}
    }

    private void storeProfilesInCache(int updateDays, Collection<CmdiProfile> profiles, JProgressBar progressBar) {
	progressBar.setIndeterminate(false);
	progressBar.setMinimum(0);
	progressBar.setMaximum(profiles.size() + 1);
	progressBar.setValue(1);

	// get all the xsd files from the profile listing and store them on disk for offline use
	for (CmdiProfile currentCmdiProfile : profiles) {
	    progressBar.setString(currentCmdiProfile.name);
	    storeProfileInCache(currentCmdiProfile.getXsdHref(), updateDays);
	    progressBar.setValue(progressBar.getValue() + 1);
	}
	progressBar.setString("");
	progressBar.setValue(0);
    }

    public final void storeProfileInCache(String xsdHref, int updateDays) {
	sessionStorage.updateCache(xsdHref, updateDays, false);
    }

    private void loadProfiles(final String profilesUrl) {
	File profileXmlFile = sessionStorage.updateCache(profilesUrl, 10, false);
	try {
	    Digester digester = new Digester();
	    // This method pushes this (SampleDigester) class to the Digesters
	    // object stack making its methods available to processing rules.
	    digester.push(this);
	    // This set of rules calls the addProfile method and passes
	    // in five parameters to the method.
	    digester.addCallMethod("profileDescriptions/profileDescription", "addProfile", 6);
	    digester.addCallParam("profileDescriptions/profileDescription/id", 0);
	    digester.addCallParam("profileDescriptions/profileDescription/description", 1);
	    digester.addCallParam("profileDescriptions/profileDescription/name", 2);
	    digester.addCallParam("profileDescriptions/profileDescription/registrationDate", 3);
	    digester.addCallParam("profileDescriptions/profileDescription/creatorName", 4);
	    digester.addCallParam("profileDescriptions/profileDescription/ns2:href", 5);

	    cmdiProfileArray = new ArrayList<CmdiProfile>();
	    digester.parse(profileXmlFile);
	} catch (Exception e) {
	    BugCatcherManager.getBugCatcher().logError(e);
	}
	// get all the xsd files from the profile listing and store them on disk for offline use
//        for (CmdiProfile currentCmdiProfile : cmdiProfileArray) {
//            LinorgSessionStorage.getSingleInstance().getFromCache(currentCmdiProfile.getXsdHref(), 90);
//        }
    }

    public void addProfile(String id,
	    String description,
	    String name,
	    String registrationDate,
	    String creatorName,
	    String href) {
	CmdiProfile cmdiProfile = new CmdiProfile();
	cmdiProfile.id = id;
	cmdiProfile.description = description;
	cmdiProfile.name = name;
	cmdiProfile.registrationDate = registrationDate;
	cmdiProfile.creatorName = creatorName;
	cmdiProfile.href = href;

	cmdiProfileArray.add(cmdiProfile);
    }

    /**
     * Get the value of selection
     *
     * @return the value of selection
     */
    public final ProfileSelection getSelection() {
	return selection;
    }

    /**
     * Set the value of selection
     *
     * @param selection new value of selection
     */
    public final void setSelection(ProfileSelection selection) {
	this.selection = selection;
	storeProfileSelection(selection);
    }

    protected final String getProfilesUrlStringForSelection(ProfileSelection selection) {
	return selection == ProfileSelection.SELECTED ? getSelectedProfileUrlString() : getAllProfilesUrlString();
    }

    public static void main(String args[]) {
	new CmdiProfileReader(ProfileSelection.SELECTED);
    }

    private synchronized static ProfileSelection getStoredProfileSelection() {
	String selectionString = sessionStorage.loadString(PARAM_PROFILESELECTION);
	if (selectionString == null) {
	    selectionString = ProfileSelection.SELECTED.toString();
	}
	return ProfileSelection.valueOf(selectionString);
    }

    private void storeProfileSelection(ProfileSelection selection) {
	sessionStorage.saveString(PARAM_PROFILESELECTION, selection.toString());
    }

    private String getAllProfilesUrlString() {
	String savedProfilesUrlString = sessionStorage.loadString(PARAM_PROFILES_URL);
	if (savedProfilesUrlString == null) {
	    sessionStorage.saveString(PARAM_PROFILES_URL, DEFAULT_ALL_PROFILES_URL_STRING);
	    return DEFAULT_ALL_PROFILES_URL_STRING;
	} else {
	    return savedProfilesUrlString;
	}
    }

    private String getSelectedProfileUrlString() {
	String savedProfilesUrlString = sessionStorage.loadString(PARAM_SELECTED_PROFILES_URL);
	if (savedProfilesUrlString == null) {
	    sessionStorage.saveString(PARAM_SELECTED_PROFILES_URL, DEFAULT_SELECTED_PROFILES_URL_STRING);
	    return DEFAULT_SELECTED_PROFILES_URL_STRING;
	} else {
	    return savedProfilesUrlString;
	}
    }
}
