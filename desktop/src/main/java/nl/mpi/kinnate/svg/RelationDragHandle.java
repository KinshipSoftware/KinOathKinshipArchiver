package nl.mpi.kinnate.svg;

import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.kindata.RelationTypeDefinition;
import nl.mpi.kinnate.kindata.RelationTypeDefinition.CurveLineOrientation;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 *  Document   : RelationDragHandle
 *  Created on : Sep 6, 2011, 1:45:49 PM
 *  Author     : Peter Withers
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
        this.customTypeDefinition = customTypeDefinition;
        this.relationType = relationType;
        this.elementStartX = elementStartX;
        this.elementStartY = elementStartY;
        this.mouseStartX = mouseStartX;
        this.mouseStartY = mouseStartY;
        this.diagramScaleFactor = (float) diagramScaleFactor;
    }

    protected float getTranslatedX(float localDragNodeX) {
        return elementStartX + (localDragNodeX - mouseStartX) / diagramScaleFactor;
    }

    protected float getTranslatedY(float localDragNodeY) {
        return elementStartY + (localDragNodeY - mouseStartY) / diagramScaleFactor;
    }

    public RelationType getRelationType() {
        if (customTypeDefinition != null) {
            return customTypeDefinition.getRelationType();
        } else {
            return relationType;
        }
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
