package nl.mpi.kinnate.kindata;

/**
 *  Document   : RelationType
 *  Created on : Mar 25, 2011, 6:16:56 PM
 *  Author     : Peter Withers
 */
public class DataTypes {

    @Deprecated
    public enum RelationLineType {

        sanguineLine, kinTermLine, verticalCurve, none
    }

    public enum RelationType {

        // todo: replace affiliation, resource, collector, metadata, none  for OTHER
        sibling, ancestor, descendant, union, custom, affiliation, resource, collector, metadata, none // todo: should metadata relations use the same link type as jpg files?
    }

    public static RelationType getOpposingRelationType(RelationType relationType) {
        switch (relationType) {
            case ancestor:
                return DataTypes.RelationType.descendant;
            case descendant:
                return DataTypes.RelationType.ancestor;
        }
        return relationType;
    }

    public RelationTypeDefinition[] getReferenceRelations() {
        return new RelationTypeDefinition[]{
                    new RelationTypeDefinition("SampeRelationType", "", RelationType.custom, "#999999", 4, "")
                };
    }
}
