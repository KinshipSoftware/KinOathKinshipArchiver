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
package nl.mpi.kinnate.kindata;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *  Document   : RelationArray
 *  Created on : Aug 2, 2011, 5:15:09 PM
 *  Author     : Peter Withers
 */
@XmlRootElement(name = "Relations", namespace = "http://mpi.nl/tla/kin")
public class RelationArray {

    @XmlElement(name = "Relation", namespace = "http://mpi.nl/tla/kin")
    private EntityRelation[] entityRelationsArray;

    public RelationArray() {
    }

    public RelationArray(EntityRelation[] entityRelationsArray) {
        this.entityRelationsArray = entityRelationsArray;
    }

    public EntityRelation[] getEntityRelationsArray() {
        return entityRelationsArray;
    }
}
