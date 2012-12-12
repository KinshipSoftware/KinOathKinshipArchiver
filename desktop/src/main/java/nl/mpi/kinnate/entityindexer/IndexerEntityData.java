/**
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.entityindexer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *  Document   : EntityData
 *  Created on : Feb 8, 2011, 6:02:53 PM
 *  Author     : Peter Withers
 */
public class IndexerEntityData {

    private HashMap<String /* url to the related entiry */, ArrayList<String[] /* relevant entity data (link vs entity is clear from the data path)
            eg: [link.famc, link.kinterm:uncle,entity.gender:m, entity.age:60, entity.birth.year:1960]
             */>> relationData = new HashMap<String, ArrayList<String[]>>();
    private ArrayList<String[] /* relevant entity data (link vs entity is clear from the data path)
            eg: [link.famc, link.kinterm:uncle,entity.gender:m, entity.age:60, entity.birth.year:1960]
             */> entityFields = new ArrayList<String[]>();
    private String uniqueIdentifier;

    public IndexerEntityData(String uniqueIdentifierLocal) {
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
