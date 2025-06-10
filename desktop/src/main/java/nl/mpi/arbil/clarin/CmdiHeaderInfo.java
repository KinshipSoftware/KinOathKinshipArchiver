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
package nl.mpi.arbil.clarin;

import java.net.URI;
import java.util.Date;
import org.apache.xmlbeans.XmlDate;

/**
 * Class to represent the information stored in the /CMD/Header element of CMDI instances
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CmdiHeaderInfo {

    protected final static String COMPONENT_REGISTRY_PATTERN = "^http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/(.*)/xsd$";
    protected String MdCreator;
    protected String MdCreationDate;
    protected String MdSelfLink;
    protected String MdProfile;
    protected String MdCollectionDisplayName;

    /**
     * Creates an empty CmdiHeaderInfo instance with all fields set to null
     */
    public CmdiHeaderInfo() {
    }

    /**
     * Will create a new CmdiHeaderInfo with default values. 
     * <p>
     * {@link #MdProfile} will be set according to the specified profileSchemaURI. If it  
     * matches the component registry location, the ID is extracted and used. Otherwise, the entire string value of the URI is used.
     * </p><p>
     * The other values will be set as by {@link #createDefault(java.lang.String)}.
     * </p>
     * @param profileSchemaURI URI of profile schema. e.g. http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:c_1302702320465/xsd
     * @return new CmdiHeaderInfo instance with MdCreator, MdProfile and MdCreationDate set
     */
    public static CmdiHeaderInfo createDefault(final URI profileSchemaURI) {
	String uriString = profileSchemaURI.toString();
	// For profiles that come from the component registry, we just want to specify the ID
	if (uriString.matches(COMPONENT_REGISTRY_PATTERN)) {
	    // extract ID from profile URI
	    uriString = uriString.replaceAll(COMPONENT_REGISTRY_PATTERN, "$1");
	}
	return createDefault(uriString);
    }

    /**
     * Will create a new CmdiHeaderInfo with default values. 
     * <p>
     * {@link #MdProfile} will be the specified profile string (should be a Component Registry ID or URI according to CMDI specification)
     * </p><p>
     * {@link #MdCreator} will be the user's username as provided by the environment; if not provided, it will be set to "Arbil"
     * </p><p>
     * {@link #MdCreationDate} will be the current date/time coverted to string through {@code XmlDateTime.getStringValue()}
     * </p><p>
     * {@link #MdSelfLink} and {@link #MdCollectionDisplayName} will be null
     * </p>
     * @param profile value for the MdProfile element
     * @return new CmdiHeaderInfo instance with MdCreator, MdProfile and MdCreationDate set
     */
    public static CmdiHeaderInfo createDefault(final String profile) {
	// Get user name from system properties
	String userName = System.getProperty("user.name");
	if (userName == null || "".equals(userName)) {
	    userName = "Arbil";
	}

	// Prepare date/time in proper format
	final XmlDate dateTime = XmlDate.Factory.newInstance();
	dateTime.setDateValue(new Date());

	// Create and initialize header info object
	CmdiHeaderInfo headerInfo = new CmdiHeaderInfo();
	headerInfo.setMdCreator(userName);
	headerInfo.setMdProfile(profile);
	headerInfo.setMdCreationDate(dateTime.getStringValue());

	return headerInfo;
    }

    /**
     * @return the MdCreator
     */
    public String getMdCreator() {
	return MdCreator;
    }

    /**
     * @param MdCreator the MdCreator to set
     */
    public void setMdCreator(String MdCreator) {
	this.MdCreator = MdCreator;
    }

    /**
     * @return the MdCreationDate
     */
    public String getMdCreationDate() {
	return MdCreationDate;
    }

    /**
     * @param MdCreationDate the MdCreationDate to set
     */
    public void setMdCreationDate(String MdCreationDate) {
	this.MdCreationDate = MdCreationDate;
    }

    /**
     * @return the MdSelfLink
     */
    public String getMdSelfLink() {
	return MdSelfLink;
    }

    /**
     * @param MdSelfLink the MdSelfLink to set
     */
    public void setMdSelfLink(String MdSelfLink) {
	this.MdSelfLink = MdSelfLink;
    }

    /**
     * @return the MdProfile
     */
    public String getMdProfile() {
	return MdProfile;
    }

    /**
     * @param MdProfile the MdProfile to set
     */
    public void setMdProfile(String MdProfile) {
	this.MdProfile = MdProfile;
    }

    /**
     * @return the MdCollectionDisplayName
     */
    public String getMdCollectionDisplayName() {
	return MdCollectionDisplayName;
    }

    /**
     * @param MdCollectionDisplayName the MdCollectionDisplayName to set
     */
    public void setMdCollectionDisplayName(String MdCollectionDisplayName) {
	this.MdCollectionDisplayName = MdCollectionDisplayName;
    }
}
