/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for
 * Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinoath.localisation;

import junit.framework.TestCase;
import nl.mpi.flap.module.AbstractBaseModule;
import nl.mpi.flap.plugin.PluginException;

/**
 * Created on : Oct 11, 2012, 17:48
 *
 * @author Peter Withers
 */
public class VersionTest extends TestCase {

    /**
     * Test of getArtifactVersion method, of class AbstractBasePlugin.
     */
    public void testGetArtifactVersion() {
        System.out.println("getArtifactVersion");
        try {
            AbstractBaseModule abstractBasePlugin = new AbstractBaseModule("KinOathLocalisation", "Package containing localisation resources. Not a plugin.", "nl.mpi.kinoath.localisation") {
            };
            assertTrue(abstractBasePlugin.isMavenVersionCorrect());
        } catch (PluginException exception) {
            fail(exception.getMessage());
        }
    }
}
