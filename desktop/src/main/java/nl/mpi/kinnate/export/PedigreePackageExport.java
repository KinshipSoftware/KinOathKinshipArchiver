package nl.mpi.kinnate.export;

import java.util.ArrayList;
import java.util.Arrays;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter;
import nl.mpi.kinnate.kintypestrings.ParserHighlight;
import nl.mpi.kinnate.svg.DataStoreSvg;
import nl.mpi.kinnate.ui.KinTypeStringProvider;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : PedigreePackageExport
 * Created on : Jun 27, 2011, 12:06:30 PM
 * Author : Peter Withers
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
        for (EntityRelation entityRelation : entityData.getAllRelations()) {
            if (entityRelation.getRelationType().equals(DataTypes.RelationType.ancestor)) {
                final EntityData alterNode = entityRelation.getAlterNode();
                if (alterNode != null) {
                    for (String symbolName : alterNode.getSymbolNames()) {
                        if (symbolName.equals(symbolType.name())) {
                            return entityRelation.getAlterNode().getUniqueIdentifier();
                        }
                    }
                }
            }
        }
        return orphanId;
    }

    private int getIntegerGender(EntityData entityData) {
        // Gender of individual noted in `id'. Character("male","female","unknown", "terminated") or numeric (1="male", 2="female", 3="unknown", 4="terminated") allowed.
        for (String symbolName : entityData.getSymbolNames()) {
            if (symbolName.equals(EntityData.SymbolType.triangle.name())) {
                return 1;
            }
            if (symbolName.equals(EntityData.SymbolType.circle.name())) {
                return 2;
            }
        }
        return 3;
    }

    public String createCsvContents(EntityData[] entityDataArray) {
        // todo: Ticket #1104 this is producing tab delimited not csv, the format and the suffix should match and both txt and csv should be available
        ArrayList<UniqueIdentifier> allIdArray = new ArrayList<UniqueIdentifier>();
        allIdArray.add(orphanId); // in the pedigree package a non existet entity has the id of 0 so we must keep that free
        StringBuilder stringBuilder = new StringBuilder();

        int labelMaxCount = 0;
        ArrayList<String> symbolHeaders = new ArrayList<String>();
        for (EntityData entityData : entityDataArray) {
            for (String symbolName : entityData.getSymbolNames()) {
                if (!symbolHeaders.contains(symbolName)) {
                    symbolHeaders.add(symbolName);
                }
            }
            int labelCount = entityData.getLabel().length;
            if (labelMaxCount < labelCount) {
                labelMaxCount = labelCount;
            }
        }
        stringBuilder.append("id\tmomid\tdadid\tsex\tego\tdob\tdod");
        for (int labelCounter = 0; labelCounter < labelMaxCount; labelCounter++) {
            stringBuilder.append("\tlabel_");
            stringBuilder.append(labelCounter);
        }
        for (String symbolString : symbolHeaders) {
            stringBuilder.append("\tsymbol_");
            stringBuilder.append(symbolString);
        }
        stringBuilder.append("\tUniqueIdentifier");
        stringBuilder.append("\n"); // todo: add remaining elements: \tstatus\trelations\n");
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
                stringBuilder.append("1");
            } else {
                stringBuilder.append("0");
            }
            stringBuilder.append("\t");
            stringBuilder.append(entityData.getDateOfBirth().getDateString());
            stringBuilder.append("\t");
            stringBuilder.append(entityData.getDateOfDeath().getDateString());
            stringBuilder.append("\t");
            String[] currentLabels = entityData.getLabel();
//            String[] labelArray = new String[labelCounter];
            for (int labelCounter = 0; labelCounter < labelMaxCount; labelCounter++) {
                if (labelCounter < currentLabels.length) {
                    stringBuilder.append(currentLabels[labelCounter]);
                }
                stringBuilder.append("\t");
            }
            ArrayList<String> currentSymbols = new ArrayList<String>(Arrays.asList(entityData.getSymbolNames()));
            for (String symbolString : symbolHeaders) {
                if (currentSymbols.contains(symbolString)) {
                    stringBuilder.append("1");
                } else {
                    stringBuilder.append("0");
                }
                stringBuilder.append("\t");
            }
            stringBuilder.append(entityData.getUniqueIdentifier().getAttributeIdentifier());
            // relations 	A matrix with 3 columns (id1, id2, code) specifying special relationship between pairs of individuals. Codes: 1=Monozygotic twin, 2=Dizygotic twin, 3=Twin of unknown zygosity, 4=Spouse and no children in pedigree
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    static public void main(String[] argsArray) {
        KinTypeStringConverter graphData = new KinTypeStringConverter(new DataStoreSvg());
        // // todo: an addition to KinTypeStringConverter: EmB does not have any parents but it could however just take the parnents of Em: String kinTypes = "EmB|EmZ|EmM|EmF|EmS|EmD";
        //String kinTypes = "EmM|EmF|EmS|EmD";        
        KinTypeStringProvider kinTypeStringProvider = new KinTypeStringProvider() {

            public String[] getCurrentStrings() {
                return "EmB,EmZ,EmM,EmF,EmS,EmD".split(",");
            }

            public int getTotalLength() {
                return getCurrentStrings().length;
            }

            public void highlightKinTypeStrings(ParserHighlight[] parserHighlight, String[] kinTypeStrings) {
//                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        ArrayList<KinTypeStringProvider> kinTypeStringProviders = new ArrayList<KinTypeStringProvider>();
        kinTypeStringProviders.add(kinTypeStringProvider);
        graphData.readKinTypes(kinTypeStringProviders, new DataStoreSvg());
        System.out.println(new PedigreePackageExport().createCsvContents(graphData.getDataNodes()));
    }
}
