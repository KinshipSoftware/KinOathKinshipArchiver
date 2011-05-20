package nl.mpi.kinnate.kindata;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *  Document   : EntityRelation
 *  Created on : Mar 25, 2011, 6:18:44 PM
 *  Author     : Peter Withers
 */
public class EntityRelation {

    @XmlTransient
    private EntityData alterNode;
    public int generationalDistance;
    @XmlElement(name = "Identifier")
    public String alterUniqueIdentifier = null;
    @XmlElement(name = "Type")
    public DataTypes.RelationType relationType;
    @XmlElement(name = "Line")
    public DataTypes.RelationLineType relationLineType;
    @XmlElement(name = "Label")
    public String labelString;
    public String lineColour = null;

    public void setAlterNode(EntityData graphDataNode) {
        if (graphDataNode != null) {
            alterNode = graphDataNode;
            if (alterUniqueIdentifier == null) {
                alterUniqueIdentifier = alterNode.getUniqueIdentifier();
            }
        }
    }

    @XmlTransient
    public EntityData getAlterNode() {
        return alterNode;
    }
}
