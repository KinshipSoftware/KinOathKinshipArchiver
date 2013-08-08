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

import javax.xml.bind.annotation.XmlAttribute;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;

/**
 * Created on : Jan 2, 2012, 3:29:25 PM
 *
 * @author Peter Withers
 */
public class RelationTypeDefinition {

    public enum CurveLineOrientation {

        horizontal, vertical
    }
    @XmlAttribute(name = "name", namespace = "http://mpi.nl/tla/kin")
    private String displayName;
    @XmlAttribute(name = "type", namespace = "http://mpi.nl/tla/kin")
    private RelationType[] relationType;
    @XmlAttribute(name = "dcr", namespace = "http://mpi.nl/tla/kin")
    private String dataCategory;
    @XmlAttribute(name = "colour", namespace = "http://mpi.nl/tla/kin")
    private String lineColour;
    @XmlAttribute(name = "width", namespace = "http://mpi.nl/tla/kin")
    private int lineWidth;
    @XmlAttribute(name = "dash", namespace = "http://mpi.nl/tla/kin")
    private int lineDash = 0;
    @XmlAttribute(name = "orientation", namespace = "http://mpi.nl/tla/kin")
    private CurveLineOrientation curveLineOrientation = CurveLineOrientation.horizontal; // used to define the line orientation of curve lines only

    public RelationTypeDefinition() {
    }

    public RelationTypeDefinition(String displayName, String dataCategory, RelationType relationType[], String lineColour, int lineWidth, int lineDash, CurveLineOrientation curveLineOrientation) {
        this.displayName = displayName;
        this.relationType = relationType;
        this.dataCategory = dataCategory;
        this.lineColour = lineColour;
        this.lineWidth = lineWidth;
        this.lineDash = lineDash;
        this.curveLineOrientation = curveLineOrientation;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLineColour() {
        return lineColour;
    }

    public int getLineDash() {
        return lineDash;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public String getDataCategory() {
        return dataCategory;
    }

    public RelationType[] getRelationType() {
        return relationType;
    }

    public CurveLineOrientation getCurveLineOrientation() {
        return curveLineOrientation;
    }

    public boolean matchesType(EntityRelation entityRelation) {
        if (entityRelation == null) {
            return false;
        }
        if (dataCategory == null) {
            if (entityRelation.dcrType != null) {
                return false;
            }
        } else {
            if (!dataCategory.equals(entityRelation.dcrType)) {
                return false;
            }
        }
        if (relationType == null) {
            if (entityRelation.getRelationType() != null) {
                return false;
            }
        } else {
            boolean foundType = false;
            final RelationType entityRelationType = entityRelation.getRelationType();
            final RelationType entityRelationOppositeType = DataTypes.getOpposingRelationType(entityRelation.getRelationType());
            for (RelationType currentType : relationType) {
                if (currentType.equals(entityRelationType) || currentType.equals(entityRelationOppositeType)) {
                    foundType = true;
                }
            }
            if (!foundType) {
                return false;
            }
        }
        if (displayName == null) {
            if (entityRelation.customType != null) {
                return false;
            }
        } else {
            if (!displayName.equals(entityRelation.customType)) {
                return false;
            }
        }
        return true;
    }
}
