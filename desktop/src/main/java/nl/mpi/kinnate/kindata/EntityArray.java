package nl.mpi.kinnate.kindata;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *  Document   : RelationArray
 *  Created on : Aug 2, 2011, 5:15:09 PM
 *  Author     : Peter Withers
 */
@XmlRootElement(name = "Entities", namespace = "http://mpi.nl/tla/kin")
public class EntityArray {

    @XmlElement(name = "Entity", namespace = "http://mpi.nl/tla/kin")
    private EntityData[] entityDataArray;

    public EntityArray() {
    }

    public EntityData[] getEntityDataArray() {
        return entityDataArray;
    }
}
