package nl.mpi.kinnate.export;

import java.util.ArrayList;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.kintypestrings.KinTermGroup;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter;
import nl.mpi.kinnate.kintypestrings.ParserHighlight;
import nl.mpi.kinnate.svg.DataStoreSvg;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 *  Document   : PedigreePackageExport
 *  Created on : Jun 27, 2011, 12:06:30 PM
 *  Author     : Peter Withers
 */
public class PedigreePackageExport {

    private static UniqueIdentifier orphanId = new UniqueIdentifier("orphan", UniqueIdentifier.IdentifierType.tid);

    private String getSimpleId(ArrayList<UniqueIdentifier> allIdArray, UniqueIdentifier entityIdentifier) {
//        String entityIdentifier = entityData.getUniqueIdentifier();
        if (!allIdArray.contains(entityIdentifier)) {
            allIdArray.add(entityIdentifier);
        }
        return Integer.toString(allIdArray.indexOf(entityIdentifier));
    }

    private UniqueIdentifier getFirstMatchingParent(EntityData entityData, EntityData.SymbolType symbolType) {
        for (EntityRelation entityRelation : entityData.getDistinctRelateNodes()) {
            if (entityRelation.relationType.equals(DataTypes.RelationType.ancestor)) {
                if (entityRelation.getAlterNode().getSymbolType().equals(symbolType.name())) {
                    return entityRelation.getAlterNode().getUniqueIdentifier();
                }
            }
        }
        return orphanId;
    }

    private int getIntegerGender(EntityData entityData) {
        // Gender of individual noted in `id'. Character("male","female","unknown", "terminated") or numeric (1="male", 2="female", 3="unknown", 4="terminated") allowed.
        String symbolName = entityData.getSymbolType();
        if (symbolName.equals(EntityData.SymbolType.triangle.name())) {
            return 1;
        }
        if (symbolName.equals(EntityData.SymbolType.circle.name())) {
            return 2;
        }
        return 3;
    }

    public String createCsvContents(EntityData[] entityDataArray) {
        ArrayList<UniqueIdentifier> allIdArray = new ArrayList<UniqueIdentifier>();
        allIdArray.add(orphanId); // in the pedigree package a non existet entity has the id of 0 so we must keep that free
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("id\tmomid\tdadid\tsex\taffected\n"); // todo: add remaining elements: \tstatus\trelations\n");
        for (EntityData entityData : entityDataArray) {
            // prime the IDs so that the sequence matches the line sequence
            getSimpleId(allIdArray, entityData.getUniqueIdentifier());
        }
        for (EntityData entityData : entityDataArray) {
            // id
            stringBuilder.append(getSimpleId(allIdArray, entityData.getUniqueIdentifier()));
            stringBuilder.append("\t");
            // momid
            stringBuilder.append(getSimpleId(allIdArray, getFirstMatchingParent(entityData, EntityData.SymbolType.circle)));
            stringBuilder.append("\t");
            // dadid
            stringBuilder.append(getSimpleId(allIdArray, getFirstMatchingParent(entityData, EntityData.SymbolType.triangle)));
            stringBuilder.append("\t");
            // sex
            stringBuilder.append(getIntegerGender(entityData));
            stringBuilder.append("\t");
            // affected
            // One variable, or a matrix, indicating affection status. Assumed that 1="unaffected", 2="affected", NA or 0 = "unknown".
            // todo: this could use an xquery on the xml data of the entity or the imdi etc.
            // more info: http://hosho.ees.hokudai.ac.jp/~kubo/Rdoc/library/kinship/html/pedigree.html
            if (entityData.isEgo) {
                stringBuilder.append("2");
            } else {
                stringBuilder.append("1");
            }
//            stringBuilder.append("\t");
            // status 	Status (0="censored", 1="dead")
//            stringBuilder.append("\t");
            // relations 	A matrix with 3 columns (id1, id2, code) specifying special relationship between pairs of individuals. Codes: 1=Monozygotic twin, 2=Dizygotic twin, 3=Twin of unknown zygosity, 4=Spouse and no children in pedigree
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    static public void main(String[] argsArray) {
        KinTypeStringConverter graphData = new KinTypeStringConverter();
        // // todo: an addition to KinTypeStringConverter: EmB does not have any parents but it could however just take the parnents of Em: String kinTypes = "EmB|EmZ|EmM|EmF|EmS|EmD";
        //String kinTypes = "EmM|EmF|EmS|EmD";
        String kinTypes = "EmB|EmZ|EmM|EmF|EmS|EmD";
        String[] kinTypeStrings = kinTypes.split("\\|");
        graphData.readKinTypes(kinTypeStrings, /*graphPanel.getkinTermGroups()*/ new KinTermGroup[]{}, new DataStoreSvg(), new ParserHighlight[kinTypeStrings.length]);
        System.out.println(new PedigreePackageExport().createCsvContents(graphData.getDataNodes()));
    }
}
