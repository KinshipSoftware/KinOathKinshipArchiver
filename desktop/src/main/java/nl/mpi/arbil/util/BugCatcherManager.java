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

import nl.mpi.flap.plugin.PluginException;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class BugCatcherManager {

    private static BugCatcher bugCatcher;
    private static BugCatcher fallBackBugCatcher;

    /**
     * @return the bugCatcher
     */
    public static synchronized BugCatcher getBugCatcher() {
        if (bugCatcher == null) {
            if (fallBackBugCatcher == null) {
                System.err.println("BugCatcher requested but no instance has been configured. Using fallback BugCatcher.");
                fallBackBugCatcher = new FallbackBugCatcher();
            }
            return fallBackBugCatcher;
        } else {
            return bugCatcher;
        }
    }

    /**
     * @param aBugCatcher the bugCatcher to set
     */
    public static synchronized void setBugCatcher(BugCatcher aBugCatcher) {
        bugCatcher = aBugCatcher;
    }

    /**
     * Fallback BugCatcher implementation that simply puts the logs on the
     * standard error output
     */
    private static class FallbackBugCatcher implements BugCatcher {

        public void logError(Exception exception) {
            if (exception != null) {
                exception.printStackTrace(System.err);
            }
        }

        public void logError(String messageString, Exception exception) {
            System.err.println(messageString);
            if (exception != null) {
                exception.printStackTrace(System.err);
            }
        }

        public void logException(PluginException exception) {
            logError("plugin error: ", exception);;
        }
    }
}
