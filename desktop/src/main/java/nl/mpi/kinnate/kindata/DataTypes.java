package nl.mpi.kinnate.kindata;

/**
 *  Document   : RelationType
 *  Created on : Mar 25, 2011, 6:16:56 PM
 *  Author     : Peter Withers
 */
public class DataTypes {

    public enum RelationType {

        // todo: replace affiliation, resource, collector, metadata, none  for OTHER..
        // maybe keep the: kinterm, curve1, curve2, none
        // sibling, ancestor, descendant, union, kin_term, custom_type1, custom_type2, none
        sibling, ancestor, descendant, union, kinterm, other //, horizontalCurve, gedcom,/* probably remove the items after this */ custom, affiliation, resource, collector, metadata, none // todo: should metadata relations use the same link type as jpg files?


// modify the kin type definitions to allow multi select for symbols and relation types eg P could be squ or cir and tri
                // and add additional option after divider with <all> that toggles all selections 
                // if other then enable extra column with line type of h or v
                // modify relation type in kintype definitions to ude the custom relation types not the enum
    }

    public static boolean isSanguinLine(String stringRelation) {
        return isSanguinLine(RelationType.valueOf(stringRelation));
    }

    public static boolean isSanguinLine(DataTypes.RelationType directedRelation) {
        return (directedRelation == DataTypes.RelationType.ancestor
                || directedRelation == DataTypes.RelationType.descendant
                || directedRelation == DataTypes.RelationType.sibling
                || directedRelation == DataTypes.RelationType.union);
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
//                    new RelationTypeDefinition("SampeRelationType", "", RelationType.custom, "#999999", 4, "")
                };
    }
}
