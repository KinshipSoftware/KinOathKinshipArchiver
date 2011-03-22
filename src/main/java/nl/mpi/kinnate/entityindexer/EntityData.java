package nl.mpi.kinnate.entityindexer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *  Document   : EntityData
 *  Created on : Feb 8, 2011, 6:02:53 PM
 *  Author     : Peter Withers
 */
public class EntityData {

    private HashMap<String /* url to the related entiry */, ArrayList<String[] /* relevant entity data (link vs entity is clear from the data path)
            eg: [link.famc, link.kinterm:uncle,entity.gender:m, entity.age:60, entity.birth.year:1960]
             */>> relationData = new HashMap<String, ArrayList<String[]>>();
    private ArrayList<String[] /* relevant entity data (link vs entity is clear from the data path)
            eg: [link.famc, link.kinterm:uncle,entity.gender:m, entity.age:60, entity.birth.year:1960]
             */> entityFields = new ArrayList<String[]>();
    private String uniqueIdentifier;

    public EntityData(String uniqueIdentifierLocal) {
        uniqueIdentifier = uniqueIdentifierLocal;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    protected String getEntityField(String fieldName) {
        for (String[] currentField : entityFields) {
            if (currentField[0].equals(fieldName)) {
                return currentField[1];
            }
        }
        return null;
    }

    protected void addRelation(String linkPath) {
        addRelationData(linkPath, null, null);
    }

    protected void addRelationData(String linkPath, String dataPath, String dataValue) {
        ArrayList<String[]> dataArray;
        if (!relationData.containsKey(linkPath)) {
            dataArray = new ArrayList<String[]>();
            relationData.put(linkPath, dataArray);
        } else {
            dataArray = relationData.get(linkPath);
        }
        dataArray.add(new String[]{dataPath, dataValue});
    }

    protected void addEntityData(String dataPath, String dataValue) {
        entityFields.add(new String[]{dataPath, dataValue});
    }

    protected String[][] getEntityFields() {
        return entityFields.toArray(new String[][]{});
    }

    protected String[] getRelationPaths() {
        return relationData.keySet().toArray(new String[]{});
    }

    protected String[][] getRelationData(String realtionPath) {
        if (relationData.containsKey(realtionPath)) {
            return relationData.get(realtionPath).toArray(new String[][]{});
        } else {
            return null;
        }
    }
}
