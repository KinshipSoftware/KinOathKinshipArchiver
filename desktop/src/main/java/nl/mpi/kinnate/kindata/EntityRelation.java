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
package nl.mpi.kinnate.kindata;

import javax.xml.bind.annotation.XmlAttribute;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *  Document   : EntityRelation
 *  Created on : Mar 25, 2011, 6:18:44 PM
 *  Author     : Peter Withers
 */
public class EntityRelation implements Comparable<EntityRelation> {

    @XmlTransient
    private EntityData alterNode;
//    @XmlElement(name = "GenerationalDistance", namespace = "http://mpi.nl/tla/kin")
//    public int generationalDistance;
    @XmlElement(name = "Identifier", namespace = "http://mpi.nl/tla/kin")
    public UniqueIdentifier alterUniqueIdentifier = null;
    @XmlAttribute(name = "Type", namespace = "http://mpi.nl/tla/kin")
    private DataTypes.RelationType relationType;
    @XmlElement(name = "Label", namespace = "http://mpi.nl/tla/kin")
    public String labelString;
    @XmlElement(name = "Colour", namespace = "http://mpi.nl/tla/kin")
    public String lineColour = null; // line colour is normally only assigned in memory and will not be serialised except into the svg, so with the exception of kin terms that are automatically added to the diagram, the colour will be set by the diagram relation settings
    @XmlAttribute(name = "dcr", namespace = "http://mpi.nl/tla/kin")
    public String dcrType = null;
    @XmlAttribute(name = "CustomType", namespace = "http://mpi.nl/tla/kin")
    public String customType = null;
    @XmlTransient
    private int relationOrder = 0; // this is used to indicate for instance; first, second child and fist, second husband etc.

    public EntityRelation() {
    }

    public EntityRelation(String dcrType, String customType, String lineColour, DataTypes.RelationType relationType, String labelString) {
        this.dcrType = dcrType;
        this.customType = customType;
        this.lineColour = lineColour;
        this.relationType = relationType;
        this.labelString = labelString;
    }

    public RelationType getRelationType() {
        if (relationType == null) {
            relationType = DataTypes.RelationType.other; //  update any old format types (that failed to deserialise) to other
        }
        return relationType;
    }

    public void setAlterNode(EntityData graphDataNode) {
//        if (graphDataNode != null) { // if the nodes has been reloaded then it must always be updated here
        // todo: it might be better to use a hashmap of all current nodes
        alterNode = graphDataNode;
        if (alterUniqueIdentifier == null) {
            alterUniqueIdentifier = alterNode.getUniqueIdentifier();
        }
//        }
    }

    @XmlTransient
    public EntityData getAlterNode() {
        return alterNode;
    }

    public int compareTo(EntityRelation o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EntityRelation other = (EntityRelation) obj;
        if (this.alterUniqueIdentifier != other.alterUniqueIdentifier && (this.alterUniqueIdentifier == null || !this.alterUniqueIdentifier.equals(other.alterUniqueIdentifier))) {
            return false;
        }
        if (this.relationType != other.relationType) {
            return false;
        }
        if ((this.labelString == null) ? (other.labelString != null) : !this.labelString.equals(other.labelString)) {
            return false;
        }
        if ((this.lineColour == null) ? (other.lineColour != null) : !this.lineColour.equals(other.lineColour)) {
            return false;
        }
        if ((this.dcrType == null) ? (other.dcrType != null) : !this.dcrType.equals(other.dcrType)) {
            return false;
        }
        if ((this.customType == null) ? (other.customType != null) : !this.customType.equals(other.customType)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.alterUniqueIdentifier != null ? this.alterUniqueIdentifier.hashCode() : 0);
        hash = 71 * hash + (this.relationType != null ? this.relationType.hashCode() : 0);
        hash = 71 * hash + (this.labelString != null ? this.labelString.hashCode() : 0);
        hash = 71 * hash + (this.lineColour != null ? this.lineColour.hashCode() : 0);
        hash = 71 * hash + (this.dcrType != null ? this.dcrType.hashCode() : 0);
        hash = 71 * hash + (this.customType != null ? this.customType.hashCode() : 0);
        return hash;
    }

    @XmlTransient
    public int getRelationOrder() {
        // todo: this is limited and a richer syntax will be required because there could be multiple birth orders eg maternal, paternal or only on shared parents or all parents etc.
//        for (EntityRelation entityRelation : getDistinctRelateNodes()){
        if (!alterUniqueIdentifier.isTransientIdentifier()) {
            throw new UnsupportedOperationException("Getting the birth order on a non transient entity is not yet supported");
        }
//        }
        return relationOrder;
    }

    public void setRelationOrder(int birthOrder) {
        if (!alterUniqueIdentifier.isTransientIdentifier()) {
            throw new UnsupportedOperationException("Cannot set the birth order on a non transient entity, it must be calculated from the birth dates");
        }
        this.relationOrder = birthOrder;
    }
}
