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
package nl.mpi.arbil.util.task;

import java.util.EventObject;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilTaskEvent extends EventObject {

    public enum ArbilTaskEventType {

	/**
	 * Task has been started
	 */
	STARTED,
	/**
	 * Task name, description or status has changed
	 */
	CHANGED,
	/**
	 * Progress or target value has changed or (in)determinate status has changed
	 */
	PROGRESS,
	/**
	 * Task has been completed
	 */
	COMPLETED,
	/**
	 * Task has been canceled
	 */
	CANCELED
    }
    private ArbilTaskEventType eventType;

    public ArbilTaskEvent(ArbilTask task, ArbilTaskEventType eventType) {
	super(task);
	this.eventType = eventType;
    }

    @Override
    public ArbilTask getSource() {
	return (ArbilTask) super.getSource();
    }

    public ArbilTaskEventType getEventType() {
	return eventType;
    }
}
