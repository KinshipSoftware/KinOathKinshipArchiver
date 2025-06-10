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

import java.io.IOException;
import org.apache.commons.digester.Digester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CmdiProfileSchemaHeaderReader implements CmdiProfileProvider {

    private final static Logger logger = LoggerFactory.getLogger(CmdiProfileSchemaHeaderReader.class);

    public CmdiProfile getProfile(String profileUrl) {
	final CmdiProfile profile = new CmdiProfile();
	profile.href = profileUrl;
	profile.name = getNameFromUrl(profileUrl);
	
	final Digester digester = new Digester();
	digester.push(new Object() {
	    public void setInfo(String id, String name, String description) {
		profile.name = name;
		profile.description = description;
		profile.id = id;
	    }
	});

	digester.addCallMethod("xs:schema/xs:annotation/xs:appinfo/ann:Header", "setInfo", 3);
	digester.addCallParam("xs:schema/xs:annotation/xs:appinfo/ann:Header/ann:ID", 0);
	digester.addCallParam("xs:schema/xs:annotation/xs:appinfo/ann:Header/ann:Name", 1);
	digester.addCallParam("xs:schema/xs:annotation/xs:appinfo/ann:Header/ann:Description", 2);

	try {
	    digester.parse(profileUrl);
	} catch (IOException ex) {
	    logger.warn("IOException while reading CMDI profile header", ex);
	    profile.description = "Could not read profile details. See error log for more information";
	} catch (SAXException ex) {
	    logger.warn("SAXException while reading CMDI profile header", ex);
	    profile.description = "Could not read profile details. See error log for more information";
	}
	return profile;
    }

    private String getNameFromUrl(String profileUrl) {
	String customName = profileUrl.replaceAll("[/.]xsd$", "");
	if (customName.contains("/")) {
	    customName = customName.substring(customName.lastIndexOf("/") + 1);
	}
	return customName;
    }
}
