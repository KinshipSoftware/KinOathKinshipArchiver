package nl.mpi.kinnate.kindata;

import javax.xml.bind.annotation.XmlAttribute;
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
    public DataTypes.RelationType relationType;
    @XmlAttribute(name = "Line", namespace = "http://mpi.nl/tla/kin")
    public DataTypes.RelationLineType relationLineType;
    @XmlElement(name = "Label", namespace = "http://mpi.nl/tla/kin")
    public String labelString;
    public String lineColour = null;
    @XmlTransient
    private int relationOrder = 0; // this is used to indicate for instance; first, second child and fist, second husband etc.

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
        if (o.alterUniqueIdentifier.equals(alterUniqueIdentifier)
                //                && o.generationalDistance == generationalDistance
                && o.relationLineType.equals(relationLineType)
                && o.relationType.equals(relationType)
                && ((labelString == null && labelString == null) || o.labelString.equals(labelString))
                && ((lineColour == null && lineColour == null) || o.lineColour.equals(lineColour))) {
            return 0;
        }
        return -1;
    }

    public int getRelationOrder() {
        // todo: this is limited and a richer syntax will be required because there could be multiple birth orders eg maternal, paternal or only on shared parents or all parents etc.
//        for (EntityRelation entityRelation : getDistinctRelateNodes()){
        if (!alterUniqueIdentifier.isTransientIdentifier()) {
//            throw new UnsupportedOperationException("Getting the birth order on a non transient entity is not yet supported");
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
