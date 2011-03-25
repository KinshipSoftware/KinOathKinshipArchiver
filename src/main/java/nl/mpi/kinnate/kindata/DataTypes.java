package nl.mpi.kinnate.kindata;

/**
 *  Document   : RelationType
 *  Created on : Mar 25, 2011, 6:16:56 PM
 *  Author     : Peter Withers
 */
public class DataTypes {
    public enum RelationLineType {

        square, horizontalCurve, verticalCurve, none
    }

    public enum RelationType {
        // the term sibling is too specific and needs to encompas anything on the same generation such as union

        sibling, ancestor, descendant, union, none
    }

    public static RelationType getOpposingRelationType(RelationType relationType) {
        switch (relationType) {
            case ancestor:
                return DataTypes.RelationType.descendant;
            case descendant:
                return DataTypes.RelationType.ancestor;
            case sibling:
                return DataTypes.RelationType.sibling;
            case union:
                return DataTypes.RelationType.union;
        }
        return DataTypes.RelationType.sibling;
    }
}
