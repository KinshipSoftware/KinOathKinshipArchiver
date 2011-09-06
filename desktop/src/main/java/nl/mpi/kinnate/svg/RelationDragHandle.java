package nl.mpi.kinnate.svg;

import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;

/**
 *  Document   : RelationDragHandle
 *  Created on : Sep 6, 2011, 1:45:49 PM
 *  Author     : Peter Withers
 */
public class RelationDragHandle {

    protected DataTypes.RelationType relationType;
    protected float elementStartX;
    protected float elementStartY;
    protected float mouseStartX;
    protected float mouseStartY;

    public RelationDragHandle(RelationType relationType, float elementStartX, float elementStartY, float mouseStartX, float mouseStartY) {
        this.relationType = relationType;
        this.elementStartX = elementStartX;
        this.elementStartY = elementStartY;
        this.mouseStartX = mouseStartX;
        this.mouseStartY = mouseStartY;
    }
}
