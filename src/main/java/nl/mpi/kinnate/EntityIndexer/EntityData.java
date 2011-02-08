/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.kinnate.EntityIndexer;

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

    protected String getEntityField(String fieldName) {
        for (String[] currentField : entityFields) {
            if (currentField[0].equals(fieldName)) {
                return currentField[1];
            }
        }
        return null;
    }
}
