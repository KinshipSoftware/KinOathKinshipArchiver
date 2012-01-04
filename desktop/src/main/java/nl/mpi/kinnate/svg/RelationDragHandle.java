package nl.mpi.kinnate.svg;

import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.RelationTypeDefinition;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 *  Document   : RelationDragHandle
 *  Created on : Sep 6, 2011, 1:45:49 PM
 *  Author     : Peter Withers
 */
public class RelationDragHandle {

    protected DataTypes.RelationType relationType;
    protected RelationTypeDefinition customTypeDefinition;
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
}
