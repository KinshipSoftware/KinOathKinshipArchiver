/**
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.kindata;

/**
 * Created on : Mar 25, 2011, 6:16:56 PM
 *
 * @author Peter Withers
 */
public class DataTypes {

    public enum RelationType {

        // todo: replace affiliation, resource, collector, metadata, none  for OTHER..
        // maybe keep the: kinterm, curve1, curve2, none
        // sibling, ancestor, descendant, union, kin_term, custom_type1, custom_type2, none
        sibling, ancestor, descendant, union, kinterm, other, undirected, directedin, directedout
        //, horizontalCurve, gedcom,/* probably remove the items after this */ custom, affiliation, resource, collector, metadata, none // todo: should metadata relations use the same link type as jpg files?
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
            case directedin:
                return DataTypes.RelationType.directedout;
            case directedout:
                return DataTypes.RelationType.directedin;
        }
        return relationType;
    }

    public RelationTypeDefinition[] getReferenceRelations() {
        return new RelationTypeDefinition[]{};
    }
}
