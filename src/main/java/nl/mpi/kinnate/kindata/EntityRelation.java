package nl.mpi.kinnate.kindata;

import javax.xml.bind.annotation.XmlElement;
import nl.mpi.arbil.LinorgBugCatcher;

/**
 *  Document   : EntityRelation
 *  Created on : Mar 25, 2011, 6:18:44 PM
 *  Author     : Peter Withers
 */
public class EntityRelation {

        protected EntityData alterNode;
        public int generationalDistance;
        @XmlElement(name = "Identifier")
        public String alterUniqueIdentifier;
        @XmlElement(name = "Type")
        public DataTypes.RelationType relationType;
        @XmlElement(name = "Line")
        public DataTypes.RelationLineType relationLineType;
        @XmlElement(name = "Label")
        public String labelString;

        public void setAlterNode(EntityData graphDataNode) {
            if (graphDataNode != null) {
                alterNode = graphDataNode;
            }
        }

        public EntityData getAlterNode() {
            if (alterNode == null) {
                new LinorgBugCatcher().logError(new Exception("getAlterNode called but alterNode is null, this should not happen"));
            }
            return alterNode;
        }
}
