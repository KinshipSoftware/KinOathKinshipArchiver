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
package nl.mpi.kinnate.svg;

import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.kindata.RelationTypeDefinition;
import nl.mpi.kinnate.kindata.RelationTypeDefinition.CurveLineOrientation;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : RelationDragHandle Created on : Sep 6, 2011, 1:45:49 PM
 *
 * @author Peter Withers
 */
public class RelationDragHandle {

    private DataTypes.RelationType relationType;
    private RelationTypeDefinition customTypeDefinition;
    protected float elementStartX;
    protected float elementStartY;
    protected float mouseStartX;
    protected float mouseStartY;
    protected float diagramScaleFactor;
    protected UniqueIdentifier targetIdentifier = null;

    public RelationDragHandle(RelationTypeDefinition customTypeDefinition, DataTypes.RelationType relationType, float elementStartX, float elementStartY, float mouseStartX, float mouseStartY, double diagramScaleFactor) {
//        if (relationType == null) {
//            throw new UnsupportedOperationException("relationType must be specified in RelationDragHandle");
//        }
        this.customTypeDefinition = customTypeDefinition;
        this.relationType = relationType;
        this.elementStartX = elementStartX;
        this.elementStartY = elementStartY;
        this.mouseStartX = mouseStartX;
        this.mouseStartY = mouseStartY;
        this.diagramScaleFactor = (float) diagramScaleFactor;
    }

    protected float getTranslatedX(float localDragNodeX) {
        return elementStartX + (localDragNodeX - mouseStartX) * diagramScaleFactor;
    }

    protected float getTranslatedY(float localDragNodeY) {
        return elementStartY + (localDragNodeY - mouseStartY) * diagramScaleFactor;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public String getRelationColour() {
        if (customTypeDefinition != null) {
            return customTypeDefinition.getLineColour();
        } else {
            return "blue";
        }
    }

    public String getDataCategory() {
        if (customTypeDefinition != null) {
            return customTypeDefinition.getDataCategory();
        } else {
            return null;
        }
    }

    public String getDisplayName() {
        if (customTypeDefinition != null) {
            return customTypeDefinition.getDisplayName();
        } else {
            return null;
        }
    }

    public CurveLineOrientation getCurveLineOrientation() {
        if (customTypeDefinition != null) {
            return customTypeDefinition.getCurveLineOrientation();
        } else {
            return null;
        }
    }
}
