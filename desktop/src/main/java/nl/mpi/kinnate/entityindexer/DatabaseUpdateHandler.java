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
package nl.mpi.kinnate.entityindexer;

import java.util.HashSet;
import java.util.Set;

/**
 * Document : DatabaseUpdateHandler Created on : Apr 25, 2012, 5:09:44 PM 
 * @author Peter Withers
 */
public class DatabaseUpdateHandler {

    private final Set<DatabaseUpdateListener> registeredListeners = new HashSet<DatabaseUpdateListener>();

    public void addDatabaseUpdateListener(DatabaseUpdateListener databaseUpdateListener) {
        registeredListeners.add(databaseUpdateListener);
    }

    protected void updateOccured() {
        for (DatabaseUpdateListener databaseUpdateListener : registeredListeners) {
            databaseUpdateListener.updateOccured();
        }
    }
}
