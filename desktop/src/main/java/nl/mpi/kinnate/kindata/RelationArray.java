package nl.mpi.kinnate.kindata;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *  Document   : RelationArray
 *  Created on : Aug 2, 2011, 5:15:09 PM
 *  Author     : Peter Withers
 */
@XmlRootElement(name = "Relations", namespace = "http://mpi.nl/tla/kin")
public class RelationArray {

    @XmlElement(name = "Relation", namespace = "http://mpi.nl/tla/kin")
    private EntityRelation[] entityRelationsArray;

    public RelationArray() {
    }

    public RelationArray(EntityRelation[] entityRelationsArray) {
        this.entityRelationsArray = entityRelationsArray;
    }

    public EntityRelation[] getEntityRelationsArray() {
        return entityRelationsArray;
    }
}
