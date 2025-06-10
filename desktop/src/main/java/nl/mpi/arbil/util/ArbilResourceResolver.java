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
package nl.mpi.arbil.util;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.mpi.arbil.data.ArbilEntityResolver;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilResourceResolver implements LSResourceResolver {
    //private CatalogResolver catRes = new CatalogResolver();

    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
	try {
	    ArbilEntityResolver resolver = new ArbilEntityResolver(new URI(baseURI));
	    InputSource resolveEntity = resolver.resolveEntity(publicId,systemId);

	    DOMImplementationLS domImplementation;
	    try {
		domImplementation = (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
	    } catch (ClassCastException e) {
		throw new RuntimeException(e);
	    } catch (ClassNotFoundException e) {
		throw new RuntimeException(e);
	    } catch (InstantiationException e) {
		throw new RuntimeException(e);
	    } catch (IllegalAccessException e) {
		throw new RuntimeException(e);
	    }
	    LSInput lsInput = domImplementation.createLSInput();
	    lsInput.setByteStream(resolveEntity.getByteStream());
	    lsInput.setCharacterStream(resolveEntity.getCharacterStream());
	    return lsInput;
	} catch (Exception ex) {
	    Logger.getLogger(ArbilResourceResolver.class.getName()).log(Level.SEVERE, null, ex);
	}
	return null;
    }
}
