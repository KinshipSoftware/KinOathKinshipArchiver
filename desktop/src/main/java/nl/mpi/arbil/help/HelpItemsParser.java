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
package nl.mpi.arbil.help;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.digester.Digester;
import org.apache.commons.logging.impl.SimpleLog;
import org.xml.sax.SAXException;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class HelpItemsParser {

    public HelpIndex parse(InputStream helpInputStream) throws IOException, SAXException {
	Digester digester = new Digester();
	digester.addObjectCreate("helpToc", HelpIndex.class);
	digester.addObjectCreate("*/item", HelpItem.class);
	digester.addBeanPropertySetter("*/item/file", "file");
	digester.addBeanPropertySetter("*/item/name", "name");
	digester.addSetNext("*/item", "addSubItem");
	digester.setLogger(new SimpleLog("digester"));
	return (HelpIndex) digester.parse(helpInputStream);
    }
}
