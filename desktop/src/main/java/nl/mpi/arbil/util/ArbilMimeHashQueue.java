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

import java.net.CookieHandler;
import java.util.Collection;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.task.ArbilTaskListener;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author Peter.Withers@mpi.nl
 */
public class ArbilMimeHashQueue extends DefaultMimeHashQueue {

    private static boolean allowCookies = false; // this is a silly place for this and should find a better home, but the cookies are only dissabled for the permissions test in this class
    private ArbilWindowManager windowManager;

    public ArbilMimeHashQueue(ArbilWindowManager windowManager, SessionStorage sessionStorage) {
	super(sessionStorage);
	this.windowManager = windowManager;
    }

    public void init() {
	CookieHandler.setDefault(new ShibCookieHandler());
	startMimeHashQueueThread();
    }

    /**
     * @param aAllowCookies the allowCookies to set
     */
    public static void setAllowCookies(boolean aAllowCookies) {
	allowCookies = aAllowCookies;
    }

    @Override
    protected Collection<ArbilTaskListener> getTaskListeners() {
	return windowManager.getTaskListeners();
    }
}
