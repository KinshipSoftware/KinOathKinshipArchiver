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
package nl.mpi.kinoath.help;

import nl.mpi.flap.module.AbstractBaseModule;
import nl.mpi.flap.plugin.PluginException;

/**
 * Document : HelpResources <br> Created on Oct 2, 2012, 11:53 <br> based on
 * ArbilHelpResources
 *
 * @author Peter Withers <br>
 */
public class HelpResources extends AbstractBaseModule {

    public final static String HELP_RESOURCE_BASE = "/nl/mpi/kinoath/resources/html/help/kinoath/";
    public final static String HELP_INDEX_XML = HELP_RESOURCE_BASE + "kinoath.xml";

    public HelpResources() throws PluginException {
        super("KinOathHelp", "Package containing help resources. Not a plugin.", "nl.mpi.kinoath.help");
    }
}
