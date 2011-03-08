package nl.mpi.kinnate.kintypestrings;

/**
 *  Document   : KinTerms
 *  Created on : Mar 8, 2011, 2:13:30 PM
 *  Author     : Peter Withers
 */
public class KinTerms {

    public String getTermLabel(String kinTypeString) {
// todo: the following is a demo/test and should be expanded in a flexable way
        if (kinTypeString.trim().equals("MM")) {
            // todo: this uses the horizontal curve line for testing
            return ("Grand Mother");
        }
        if (kinTypeString.trim().equals("MZ") || kinTypeString.trim().equals("FZ")) {
            // todo: this uses the horizontal curve line for testing
            return ("Aunt");
        }
        if (kinTypeString.trim().equals("MB") || kinTypeString.trim().equals("FB")) {
            // todo: this uses the horizontal curve line for testing
            return ("Uncle");
        }
        if (kinTypeString.equals("FF")) {
            // todo: this uses the vertical curve line for testing
            return ("Grand Father");
        }
//                if (inputString.equals("MZS")) {
//                    // todo: this uses the vertical curve line for testing
//                    egoDataNode.addRelatedNode(parentDataNode, 2, GraphDataNode.RelationType.none, GraphDataNode.RelationLineType.horizontalCurve, "Sister's Brother");
//                }
//                if (inputString.equals("BB")) {
//                    // todo: this uses the vertical curve line for testing
//                    egoDataNode.addRelatedNode(parentDataNode, 2, GraphDataNode.RelationType.none, GraphDataNode.RelationLineType.horizontalCurve, "Brother's Brother");
//                }
//                if (inputString.equals("BZ")) {
//                    // todo: this uses the vertical curve line for testing
//                    egoDataNode.addRelatedNode(parentDataNode, 2, GraphDataNode.RelationType.none, GraphDataNode.RelationLineType.verticalCurve, "Brother's Sister V");
//                }
//                if (inputString.equals("ZZ")) {
//                    // todo: this uses the vertical curve line for testing
//                    egoDataNode.addRelatedNode(parentDataNode, 2, GraphDataNode.RelationType.none, GraphDataNode.RelationLineType.verticalCurve, "Sister's Sister V");
//                }
        return null;
    }
}
