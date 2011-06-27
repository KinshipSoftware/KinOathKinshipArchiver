package nl.mpi.kinnate.export;

import java.util.ArrayList;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityRelation;

/**
 *  Document   : PedigreePackageExport
 *  Created on : Jun 27, 2011, 12:06:30 PM
 *  Author     : Peter Withers
 */
public class PedigreePackageExport {

    private String getSimpleId(ArrayList<String> allIdArray, String entityIdentifier) {
//        String entityIdentifier = entityData.getUniqueIdentifier();
        if (!allIdArray.contains(entityIdentifier)) {
            allIdArray.add(entityIdentifier);
        }
        return Integer.toString(allIdArray.indexOf(entityIdentifier));
    }

    private String getFirstMatchingParent(EntityData entityData, EntityData.SymbolType symbolType) {
        for (EntityRelation entityRelation : entityData.getDistinctRelateNodes()) {
            if (entityRelation.relationType.equals(DataTypes.RelationType.ancestor)) {
                if (entityRelation.getAlterNode().getSymbolType().equals(symbolType.name())) {
                    return entityRelation.getAlterNode().getUniqueIdentifier();
                }
            }
        }
        return "orphan";
    }

    private int getIntegerGender(EntityData entityData) {
        String symbolName = entityData.getSymbolType();
        if (symbolName.equals(EntityData.SymbolType.triangle.name())) {
            return 2;
        }
        if (symbolName.equals(EntityData.SymbolType.circle.name())) {
            return 1;
        }
        return 0;
    }

    public String createCsvContents(EntityData[] entityDataArray) {
        ArrayList<String> allIdArray = new ArrayList<String>();
        allIdArray.add("orphan"); // in the pedigree package a non existet entity has the id of 0 so we must keep that free
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("pid	id	momid	dadid	sex	affected");
        String pidString = "1";
        for (EntityData entityData : entityDataArray) {
            // pid
            stringBuilder.append(pidString);
            stringBuilder.append("\t");
            // id
            stringBuilder.append(getSimpleId(allIdArray, entityData.getUniqueIdentifier()));
            stringBuilder.append("\t");
            // momid
            stringBuilder.append(getSimpleId(allIdArray, getFirstMatchingParent(entityData, EntityData.SymbolType.circle)));
            stringBuilder.append("\t");
            // dadid
            stringBuilder.append(getSimpleId(allIdArray, getFirstMatchingParent(entityData, EntityData.SymbolType.square)));
            stringBuilder.append("\t");
            // sex
            stringBuilder.append(getIntegerGender(entityData));
            stringBuilder.append("\t");
            // affected
            // todo: this could use an xquery on the xml data of the entity or the imdi etc.
            if (entityData.isEgo) {
                stringBuilder.append("1");
            } else {
                stringBuilder.append("0");
            }
        }
        return stringBuilder.toString();
    }
}
