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
package nl.mpi.kinnate.data;

import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ContainerNode;
import nl.mpi.kinnate.entityindexer.EntityCollection;

/**
 * Document : ProjectNode
 * Created on : Apr 25, 2012, 11:33:19 AM
 * Author : Peter Withers
 */
public class ProjectNode extends ContainerNode {

    EntityCollection entityCollection;

    public ProjectNode(EntityCollection entityCollection, String labelString) {
        super(labelString, null, new ContainerNode[]{new ContainerNode("loading...", null, new ArbilNode[0])});
        this.entityCollection = entityCollection;
    }
}
