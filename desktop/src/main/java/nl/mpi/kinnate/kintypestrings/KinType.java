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
public class KinType {

    private KinType() {
    }

    private KinType(String codeStringLocal, DataTypes.RelationType relationTypeLocal, EntityData.SymbolType symbolTypeLocal, String displayStringLocal) {
        codeString = codeStringLocal;
        relationType = relationTypeLocal;
        symbolType = symbolTypeLocal;
        displayString = displayStringLocal;
    }
    @XmlAttribute(name = "code", namespace = "http://mpi.nl/tla/kin")
    protected String codeString = null;
    @XmlAttribute(name = "type", namespace = "http://mpi.nl/tla/kin")
    protected DataTypes.RelationType relationType = null;
    @XmlAttribute(name = "symbol", namespace = "http://mpi.nl/tla/kin")
    protected EntityData.SymbolType symbolType = null;
    @XmlAttribute(name = "name", namespace = "http://mpi.nl/tla/kin")
    protected String displayString = null;

    public String getCodeString() {
        return codeString;
    }

    public String getDisplayString() {
        return displayString;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public SymbolType getSymbolType() {
        return symbolType;
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
        if (relationType != null && !relationType.equals(entityRelation.relationType)) {
            return false;
        }
        if (symbolType == null || symbolType == EntityData.SymbolType.square) {
            return true; // it is better to return all the relations regardless of symbol in this case
            // if the symbol is square then either circle or triangle are matches (square is matched later)
//            if (EntityData.SymbolType.circle.name().equals(entityRelation.getAlterNode().getSymbolType())) {
//                return true;
//            }
//            if (EntityData.SymbolType.triangle.name().equals(entityRelation.getAlterNode().getSymbolType())) {
//                return true;
//            }
        }
        if (!symbolType.name().equals(entityRelation.getAlterNode().getSymbolType())) {
            return false;
        }
        return true;
    }

    public boolean matchesEgoEntity(EntityData entityData, String kinTypeModifier) {
        // todo make use of the kin type modifier or remove it if it proves irelevant
        if (!entityData.isEgo || !this.isEgoType()) {
            return false;
        }
        if (symbolType == EntityData.SymbolType.square) {
            return true; // it is better to return all the relations regardless of symbol in this case
            // if the symbol is square then either circle or triangle are matches (square is matched later)
//            if (EntityData.SymbolType.circle.name().equals(entityData.getSymbolType())) {
//                return true;
//            }
//            if (EntityData.SymbolType.triangle.name().equals(entityData.getSymbolType())) {
//                return true;
//            }
        }
        if (!symbolType.name().equals(entityData.getSymbolType())) {
            return false;
        }
        return true;
    }

    public static KinType[] getReferenceKinTypes() {
        return referenceKinTypes;
    }
    protected static KinType[] referenceKinTypes = new KinType[]{
        // other types
        // todo: the gendered ego kin types Em and Ef are probably not correct and should be verified
        new KinType("Ef", DataTypes.RelationType.none, EntityData.SymbolType.circle, "Ego Female"),
        new KinType("Em", DataTypes.RelationType.none, EntityData.SymbolType.triangle, "Ego Male"),
        // type 1
        new KinType("Fa", DataTypes.RelationType.ancestor, EntityData.SymbolType.triangle, "Father"),
        new KinType("Mo", DataTypes.RelationType.ancestor, EntityData.SymbolType.circle, "Mother"),
        new KinType("Br", DataTypes.RelationType.sibling, EntityData.SymbolType.triangle, "Brother"),
        new KinType("Si", DataTypes.RelationType.sibling, EntityData.SymbolType.circle, "Sister"),
        new KinType("So", DataTypes.RelationType.descendant, EntityData.SymbolType.triangle, "Son"),
        new KinType("Da", DataTypes.RelationType.descendant, EntityData.SymbolType.circle, "Daughter"),
        new KinType("Hu", DataTypes.RelationType.union, EntityData.SymbolType.triangle, "Husband"),
        new KinType("Wi", DataTypes.RelationType.union, EntityData.SymbolType.circle, "Wife"),
        new KinType("Pa", DataTypes.RelationType.ancestor, EntityData.SymbolType.square, "Parent"),
        new KinType("Sb", DataTypes.RelationType.sibling, EntityData.SymbolType.square, "Sibling"), //todo: are Sp and Sb correct?
        new KinType("Sp", DataTypes.RelationType.union, EntityData.SymbolType.square, "Spouse"),
        new KinType("Ch", DataTypes.RelationType.descendant, EntityData.SymbolType.square, "Child"),
        // type 2
        new KinType("F", DataTypes.RelationType.ancestor, EntityData.SymbolType.triangle, "Father"),
        new KinType("M", DataTypes.RelationType.ancestor, EntityData.SymbolType.circle, "Mother"),
        new KinType("B", DataTypes.RelationType.sibling, EntityData.SymbolType.triangle, "Brother"),
        new KinType("Z", DataTypes.RelationType.sibling, EntityData.SymbolType.circle, "Sister"),
        new KinType("S", DataTypes.RelationType.descendant, EntityData.SymbolType.triangle, "Son"),
        new KinType("D", DataTypes.RelationType.descendant, EntityData.SymbolType.circle, "Daughter"),
        new KinType("H", DataTypes.RelationType.union, EntityData.SymbolType.triangle, "Husband"),
        new KinType("W", DataTypes.RelationType.union, EntityData.SymbolType.circle, "Wife"),
        new KinType("P", DataTypes.RelationType.ancestor, EntityData.SymbolType.square, "Parent"),
        new KinType("G", DataTypes.RelationType.sibling, EntityData.SymbolType.square, "Sibling"),
        new KinType("E", DataTypes.RelationType.none, EntityData.SymbolType.square, "Ego"),
        new KinType("C", DataTypes.RelationType.descendant, EntityData.SymbolType.square, "Child"),
        //        new KinType("X", DataTypes.RelationType.none, EntityData.SymbolType.none) // X is intended to indicate unknown or no type, for instance this is used after import to add all nodes to the graph

        // non ego types to be used to start a kin type string but cannot be used except at the beginning
        new KinType("m", DataTypes.RelationType.none, EntityData.SymbolType.triangle, "Male"),
        new KinType("f", DataTypes.RelationType.none, EntityData.SymbolType.circle, "Female"),
        new KinType("x", DataTypes.RelationType.none, EntityData.SymbolType.square, "Undefined"),
        new KinType("*", null, null, "Any Relation"),};
}
