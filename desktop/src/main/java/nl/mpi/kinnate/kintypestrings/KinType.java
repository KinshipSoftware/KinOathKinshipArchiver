package nl.mpi.kinnate.kintypestrings;

import javax.xml.bind.annotation.XmlAttribute;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityData.SymbolType;
import nl.mpi.kinnate.kindata.EntityRelation;

/**
 *  Document   : KinType
 *  Created on : Jun 28, 2011, 11:31:35 AM
 *  Author     : Peter Withers
 */
public class KinType implements Comparable<KinType> {

    private KinType() {
    }

    public KinType(String codeStringLocal, DataTypes.RelationType[] relationTypes, EntityData.SymbolType[] symbolTypes, String displayStringLocal) {
        codeString = codeStringLocal;
        this.relationTypes = relationTypes;
        this.symbolTypes = symbolTypes;
        displayString = displayStringLocal;
    }
    @XmlAttribute(name = "code", namespace = "http://mpi.nl/tla/kin")
    protected String codeString = null;
    @XmlAttribute(name = "type", namespace = "http://mpi.nl/tla/kin")
    private DataTypes.RelationType[] relationTypes = null;
    @XmlAttribute(name = "symbol", namespace = "http://mpi.nl/tla/kin")
    private EntityData.SymbolType[] symbolTypes = null;
    @XmlAttribute(name = "name", namespace = "http://mpi.nl/tla/kin")
    protected String displayString = null;

    public String getCodeString() {
        return codeString;
    }

    public String getDisplayString() {
        return displayString;
    }

    public RelationType[] getRelationTypes() {
        return relationTypes;
    }

    public boolean hasNoRelationTypes() {
        // null means any relation type so this must not be null but a length of zero to indicate no relations
        return relationTypes != null && relationTypes.length == 0;
    }

    public SymbolType[] getSymbolTypes() {
        return symbolTypes;
    }

    public boolean isEgoType() {
        // todo: this could be better handled by adding a boolean: isego to each KinType
        return codeString.contains("E");
    }

    public boolean matchesRelation(EntityRelation entityRelation, String kinTypeModifier) {
        // todo: make use of the kin type modifier
        if (entityRelation.getAlterNode().isEgo != this.isEgoType()) {
            return false;
        }
        boolean relationMatchFound = false;
        if (relationTypes == null) {
            relationMatchFound = true; // null will match all relation types
        } else {
            for (DataTypes.RelationType relationType : relationTypes) {
                if (relationType.equals(entityRelation.getRelationType())) {
                    relationMatchFound = true;
                }
            }
        }
        boolean symbolMatchFound = false;
        if (symbolTypes == null) {
            symbolMatchFound = true; // null will match all symbol types
        } else {
            for (EntityData.SymbolType symbolType : symbolTypes) {
                // square was the wildcard symbol but now a null symbol array is used and since it is not null we compare all symbols in the array
                if (symbolType.name().equals(entityRelation.getAlterNode().getSymbolType())) {
                    symbolMatchFound = true;
                }
            }
        }
        if (!relationMatchFound || !symbolMatchFound) {
            return false;
        }
        // compare the birth order
        if (kinTypeModifier != null && !kinTypeModifier.isEmpty()) {
            int relationOrder = entityRelation.getRelationOrder();
            if (kinTypeModifier.equals("-")) {
                if (relationOrder >= 0) {
                    return false;
                }
            } else if (kinTypeModifier.equals("+")) {
                if (relationOrder <= 0) {
                    return false;
                }
            } else { // handle integer syntax ie EMD+3 for the third daughter
                int requiredOrder = Integer.parseInt(kinTypeModifier.replaceFirst("^\\+", ""));
                return (relationOrder == requiredOrder);
            }
        }
        return true;
    }

    public boolean matchesEgonessAndSymbol(EntityData entityData, String kinTypeModifier) {
        // todo: make use of the kin type modifier or remove it if it proves irelevant
        if (!entityData.isEgo || !this.isEgoType()) {
            return false;
        }
        if (symbolTypes == null) {
            return true; // null will match all symbols
        }
        // square used to be the wildcard symbol but now a null symbol array is used and since we know it is not null we compare all symbols in the array
        for (EntityData.SymbolType symbolType : symbolTypes) {
            if (symbolType.name().equals(entityData.getSymbolType())) {
                return true;
            }
        }
        return false;
    }

    public int compareTo(KinType o) {
        if (o == null) {
            return -1;
        }
        if (codeString.length() > o.codeString.length()) {
            return -1;
        }
        return codeString.compareToIgnoreCase(o.codeString);
    }

    public static KinType[] getReferenceKinTypes() {
        return referenceKinTypes;
    }
    private static KinType[] referenceKinTypes = new KinType[]{
        // other types
        // todo: the gendered ego kin types Em and Ef are probably not correct and should be verified
        new KinType("Ef", new DataTypes.RelationType[]{}, new EntityData.SymbolType[]{EntityData.SymbolType.circle}, "Ego Female"),
        new KinType("Em", new DataTypes.RelationType[]{}, new EntityData.SymbolType[]{EntityData.SymbolType.triangle}, "Ego Male"),
        // type 1
        new KinType("Fa", new DataTypes.RelationType[]{DataTypes.RelationType.ancestor}, new EntityData.SymbolType[]{EntityData.SymbolType.triangle}, "Father"),
        new KinType("Mo", new DataTypes.RelationType[]{DataTypes.RelationType.ancestor}, new EntityData.SymbolType[]{EntityData.SymbolType.circle}, "Mother"),
        new KinType("Br", new DataTypes.RelationType[]{DataTypes.RelationType.sibling}, new EntityData.SymbolType[]{EntityData.SymbolType.triangle}, "Brother"),
        new KinType("Si", new DataTypes.RelationType[]{DataTypes.RelationType.sibling}, new EntityData.SymbolType[]{EntityData.SymbolType.circle}, "Sister"),
        new KinType("So", new DataTypes.RelationType[]{DataTypes.RelationType.descendant}, new EntityData.SymbolType[]{EntityData.SymbolType.triangle}, "Son"),
        new KinType("Da", new DataTypes.RelationType[]{DataTypes.RelationType.descendant}, new EntityData.SymbolType[]{EntityData.SymbolType.circle}, "Daughter"),
        new KinType("Hu", new DataTypes.RelationType[]{DataTypes.RelationType.union}, new EntityData.SymbolType[]{EntityData.SymbolType.triangle}, "Husband"),
        new KinType("Wi", new DataTypes.RelationType[]{DataTypes.RelationType.union}, new EntityData.SymbolType[]{EntityData.SymbolType.circle}, "Wife"),
        new KinType("Pa", new DataTypes.RelationType[]{DataTypes.RelationType.ancestor}, new EntityData.SymbolType[]{EntityData.SymbolType.triangle, EntityData.SymbolType.circle}, "Parent"),
        new KinType("Sb", new DataTypes.RelationType[]{DataTypes.RelationType.sibling}, new EntityData.SymbolType[]{EntityData.SymbolType.square}, "Sibling"), //todo: are Sp and Sb correct?
        new KinType("Sp", new DataTypes.RelationType[]{DataTypes.RelationType.union}, new EntityData.SymbolType[]{EntityData.SymbolType.square}, "Spouse"),
        new KinType("Ch", new DataTypes.RelationType[]{DataTypes.RelationType.descendant,}, new EntityData.SymbolType[]{EntityData.SymbolType.square}, "Child"),
        // type 2
        new KinType("F", new DataTypes.RelationType[]{DataTypes.RelationType.ancestor}, new EntityData.SymbolType[]{EntityData.SymbolType.triangle}, "Father"),
        new KinType("M", new DataTypes.RelationType[]{DataTypes.RelationType.ancestor}, new EntityData.SymbolType[]{EntityData.SymbolType.circle}, "Mother"),
        new KinType("B", new DataTypes.RelationType[]{DataTypes.RelationType.sibling}, new EntityData.SymbolType[]{EntityData.SymbolType.triangle}, "Brother"),
        new KinType("Z", new DataTypes.RelationType[]{DataTypes.RelationType.sibling}, new EntityData.SymbolType[]{EntityData.SymbolType.circle}, "Sister"),
        new KinType("S", new DataTypes.RelationType[]{DataTypes.RelationType.descendant}, new EntityData.SymbolType[]{EntityData.SymbolType.triangle}, "Son"),
        new KinType("D", new DataTypes.RelationType[]{DataTypes.RelationType.descendant}, new EntityData.SymbolType[]{EntityData.SymbolType.circle}, "Daughter"),
        new KinType("H", new DataTypes.RelationType[]{DataTypes.RelationType.union}, new EntityData.SymbolType[]{EntityData.SymbolType.triangle}, "Husband"),
        new KinType("W", new DataTypes.RelationType[]{DataTypes.RelationType.union}, new EntityData.SymbolType[]{EntityData.SymbolType.circle}, "Wife"),
        new KinType("P", new DataTypes.RelationType[]{DataTypes.RelationType.ancestor}, new EntityData.SymbolType[]{EntityData.SymbolType.triangle, EntityData.SymbolType.circle}, "Parent"),
        new KinType("G", new DataTypes.RelationType[]{DataTypes.RelationType.sibling}, new EntityData.SymbolType[]{EntityData.SymbolType.square}, "Sibling"),
        new KinType("E", new DataTypes.RelationType[]{}, new EntityData.SymbolType[]{EntityData.SymbolType.square}, "Ego"),
        new KinType("C", new DataTypes.RelationType[]{DataTypes.RelationType.descendant}, new EntityData.SymbolType[]{EntityData.SymbolType.square}, "Child"),
        //        new KinType("X", DataTypes.RelationType.none, EntityData.SymbolType.none) // X is intended to indicate unknown or no type, for instance this is used after import to add all nodes to the graph

        // non ego types to be used to start a kin type string but cannot be used except at the beginning
        new KinType("m", new DataTypes.RelationType[]{}, new EntityData.SymbolType[]{EntityData.SymbolType.triangle}, "Male"),
        new KinType("f", new DataTypes.RelationType[]{}, new EntityData.SymbolType[]{EntityData.SymbolType.circle}, "Female"),
        new KinType("x", new DataTypes.RelationType[]{}, new EntityData.SymbolType[]{EntityData.SymbolType.square}, "Undefined"),
        new KinType("*", new DataTypes.RelationType[]{DataTypes.RelationType.ancestor, DataTypes.RelationType.descendant, DataTypes.RelationType.union, DataTypes.RelationType.sibling}, null, "Any Relation"),};
}
