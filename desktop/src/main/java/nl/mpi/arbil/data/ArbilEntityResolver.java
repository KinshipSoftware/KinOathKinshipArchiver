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
package nl.mpi.arbil.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *  Document   : ArbilEntityResolver
 *  Created on : Sep 16, 2010, 1:36:19 PM
 *  Author     : Peter Withers
 * 
 * @author Peter.Withers@mpi.nl
 * @author Twan.Goosen@mpi.nl
 */
public class ArbilEntityResolver implements EntityResolver {

    public static final int EXPIRE_CACHE_DAYS = 7;
    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private URI parentUri;

    public ArbilEntityResolver(URI parentUriLocal) {
	parentUri = parentUriLocal;
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
//        logger.debug("publicId: " + publicId);
//        logger.debug("systemId: " + systemId);
	String targetString;
	// todo: test with http://www.loc.gov/ndnp/xml/alto-1-2.xsd issue: exception: no protocol: ./xlink.xsd need to resolve local references
	if (parentUri != null) {
	    URI resolvedSystemIdURL = parentUri.resolve(systemId);
	    targetString = resolvedSystemIdURL.toString();
	} else {
	    targetString = systemId;
	}
	final File cachedfile = sessionStorage.updateCache(targetString, EXPIRE_CACHE_DAYS, false);
	if (!cachedfile.exists()) {
	    // todo: pull the file out of the jar
	    BugCatcherManager.getBugCatcher().logError("dependant xsd not stored in the jar for offline first time use: " + cachedfile, null);
	}
	String cachedfileString = cachedfile.toURI().toString();
//        logger.debug("cachedfileString: " + cachedfileString);
	return new InputSource(cachedfileString) {

	    @Override
	    public InputStream getByteStream() {
		try {
		    return new FileInputStream(cachedfile);
		} catch (FileNotFoundException ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		    return null;
		}
	    }

	    @Override
	    public Reader getCharacterStream() {
		try {
		    return new FileReader(cachedfile);
		} catch (FileNotFoundException ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		    return null;
		}
	    }
	};
	// to use the default behaviour by returning null
    }
}
