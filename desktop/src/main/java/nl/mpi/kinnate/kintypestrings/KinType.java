package nl.mpi.kinnate.kintypestrings;

import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityRelation;

/**
 *  Document   : KinType
 *  Created on : Jun 28, 2011, 11:31:35 AM
 *  Author     : Peter Withers
 */
public class KinType {

    private KinType(String codeStringLocal, DataTypes.RelationType relationTypeLocal, EntityData.SymbolType symbolTypeLocal) {
        codeString = codeStringLocal;
        relationType = relationTypeLocal;
        symbolType = symbolTypeLocal;
    }
    protected String codeString;
    protected DataTypes.RelationType relationType;
    protected EntityData.SymbolType symbolType;

    public String getCodeString() {
        return codeString;
    }

    public boolean isEgoType() {
        // todo: this could be better handled by adding a boolean: isego to each KinType
        return codeString.contains("E");
    }

    protected boolean matchesRelation(EntityRelation entityRelation, String kinTypeModifier) {
        // todo make use of the kin type modifier
        if (entityRelation.getAlterNode().isEgo != this.isEgoType()) {
            return false;
        }
        if (!relationType.equals(entityRelation.relationType)) {
            return false;
        }
        if (!symbolType.name().equals(entityRelation.getAlterNode().getSymbolType())) {
            return false;
        }
        return true;
    }

    protected boolean matchesEgoEntity(EntityData entityData, String kinTypeModifier) {
        // todo make use of the kin type modifier or remove it if it proves irelevant
        if (!entityData.isEgo || !this.isEgoType()) {
            return false;
        }
        if (!symbolType.name().equals(entityData.getSymbolType())) {
            return false;
        }
        return true;
    }
    protected static KinType[] referenceKinTypes = new KinType[]{
        // other types
        // todo: the gendered ego kin types Em and Ef are probably not correct and should be verified
        new KinType("Ef", DataTypes.RelationType.none, EntityData.SymbolType.circle),
        new KinType("Em", DataTypes.RelationType.none, EntityData.SymbolType.triangle),
        // type 1
        new KinType("Fa", DataTypes.RelationType.ancestor, EntityData.SymbolType.triangle),
        new KinType("Mo", DataTypes.RelationType.ancestor, EntityData.SymbolType.circle),
        new KinType("Br", DataTypes.RelationType.sibling, EntityData.SymbolType.triangle),
        new KinType("Si", DataTypes.RelationType.sibling, EntityData.SymbolType.circle),
        new KinType("So", DataTypes.RelationType.descendant, EntityData.SymbolType.triangle),
        new KinType("Da", DataTypes.RelationType.descendant, EntityData.SymbolType.circle),
        new KinType("Hu", DataTypes.RelationType.union, EntityData.SymbolType.triangle),
        new KinType("Wi", DataTypes.RelationType.union, EntityData.SymbolType.circle),
        new KinType("Pa", DataTypes.RelationType.ancestor, EntityData.SymbolType.square),
        new KinType("Sb", DataTypes.RelationType.sibling, EntityData.SymbolType.square), //todo: are Sp and Sb correct?
        new KinType("Sp", DataTypes.RelationType.sibling, EntityData.SymbolType.square),
        new KinType("Ch", DataTypes.RelationType.descendant, EntityData.SymbolType.square),
        // type 2
        new KinType("F", DataTypes.RelationType.ancestor, EntityData.SymbolType.triangle),
        new KinType("M", DataTypes.RelationType.ancestor, EntityData.SymbolType.circle),
        new KinType("B", DataTypes.RelationType.sibling, EntityData.SymbolType.triangle),
        new KinType("Z", DataTypes.RelationType.sibling, EntityData.SymbolType.circle),
        new KinType("S", DataTypes.RelationType.descendant, EntityData.SymbolType.triangle),
        new KinType("D", DataTypes.RelationType.descendant, EntityData.SymbolType.circle),
        new KinType("H", DataTypes.RelationType.union, EntityData.SymbolType.triangle),
        new KinType("W", DataTypes.RelationType.union, EntityData.SymbolType.circle),
        new KinType("P", DataTypes.RelationType.ancestor, EntityData.SymbolType.square),
        new KinType("G", DataTypes.RelationType.sibling, EntityData.SymbolType.square),
        new KinType("E", DataTypes.RelationType.none, EntityData.SymbolType.square),
        new KinType("C", DataTypes.RelationType.descendant, EntityData.SymbolType.square),
        //        new KinType("X", DataTypes.RelationType.none, EntityData.SymbolType.none) // X is intended to indicate unknown or no type, for instance this is used after import to add all nodes to the graph

        // non ego types to be used to start a kin type string but cannot be used except at the beginning
        new KinType("m", DataTypes.RelationType.none, EntityData.SymbolType.triangle),
        new KinType("f", DataTypes.RelationType.none, EntityData.SymbolType.circle),
        new KinType("x", DataTypes.RelationType.none, EntityData.SymbolType.square)
    };
}
