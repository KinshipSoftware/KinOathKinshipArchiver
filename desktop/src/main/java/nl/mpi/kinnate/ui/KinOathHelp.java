/**
 * Copyright (C) 2012 The Language Archive
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
package nl.mpi.kinnate.ui;

import java.io.IOException;
import java.util.Arrays;
import nl.mpi.arbil.ui.HelpViewerPanel;
import nl.mpi.arbil.ui.HelpViewerPanel.HelpResourceSet;
import org.xml.sax.SAXException;

/**
 * Document : KinOathHelp.java Created on : March 9, 2009, 1:38 PM
 *
 * @author Peter Withers <Peter.Withers@mpi.nl>
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class KinOathHelp extends HelpViewerPanel {

    public final static String KMDI_HELPSET = "KMDI";
    private final static String KMDI_HELP_RESOURCE_BASE = "/nl/mpi/kinoath/resources/html/help/kinoath/";
    private final static HelpResourceSet KMDI_HELP_SET = new HelpResourceSet(KMDI_HELPSET, KinOathHelp.class, KMDI_HELP_RESOURCE_BASE, KMDI_HELP_RESOURCE_BASE + "kinoath.xml");
    private static KinOathHelp singleInstance = null;

    public static synchronized KinOathHelp getArbilHelpInstance() throws IOException, SAXException {
        //TODO: This should not be a singleton...
        if (singleInstance == null) {

            singleInstance = new KinOathHelp();
        }
        return singleInstance;
    }

    public KinOathHelp() throws IOException, SAXException {
        super(Arrays.asList(KMDI_HELP_SET));
    }
}
