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
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface MimeHashQueue {

    /**
     * Adds a node to the queue for processing. Only nodes with resources will
     * actually be processed
     *
     * @param dataNode Data node to be processed
     */
    void addToQueue(ArbilDataNode dataNode);

    /**
     * Adds a node to the queue and forces it to be processed, even if it shows no sign of having been changed since the last check.
     *
     * @param dataNode data node to be processed
     */
    void forceInQueue(ArbilDataNode dataNode);

    /**
     * @return Whether resource permissions are checked
     */
    boolean isCheckResourcePermissions();

    /**
     * @param checkResourcePermissions Whether to check resource permissions
     */
    void setCheckResourcePermissions(boolean checkResourcePermissions);

    /**
     * Makes sure the mime hash queue thread is started
     */
    void startMimeHashQueueThread();

    void stopMimeHashQueueThread();

    String[] getMimeType(URI fileUri);

    /**
     * Terminates and cleans the queue, saves all to disk. Should only be called when quitting application
     */
    void terminateQueue();
    
    ArbilNode getActiveNode();

    public enum TypeCheckerState {

	UNCHECKED {
	    @Override
	    public String toString() {
		return "Unchecked";
	    }
	},
	IN_QUEUE {
	    @Override
	    public String toString() {
		return "In queue";
	    }
	},
	IN_PROCESS {
	    @Override
	    public String toString() {
		return "In process";
	    }
	},
	CHECKED {
	    @Override
	    public String toString() {
		return "Checked";
	    }
	},
	ERROR {
	    @Override
	    public String toString() {
		return "Error";
	    }
	}
    };
}
