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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.clarin.profiles;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX event handler for getting the description out of a Data category DCIF
 * e.g. http://www.isocat.org/rest/dc/2564.dcif?workingLanguage=en
 * 
 * Stores first encounter of 'definition' element as description
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class DataCategoryDescriptionHandler extends DefaultHandler {
    protected static final String DEFINITION_ELEMENT = "dcif:definition";
    
    private boolean inDescription = false;
    private String description = null;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
	if (DEFINITION_ELEMENT.equals(qName)) {
	    inDescription = true;
	}
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
	if (inDescription && description == null) {
	    description = new String(ch, start, length);
	}
    }

    /**
     * 
     * @return Found description (null if none found)
     */
    public String getDescription() {
	return description;
    }
    
}
