package nl.mpi.kinnate.kindata;

import javax.xml.bind.annotation.XmlAttribute;
import nl.mpi.kinnate.kindata.DataTypes.RelationLineType;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;

/**
 *  Document   : RelationTypeDefinition
 *  Created on : Jan 2, 2012, 3:29:25 PM
 *  Author     : Peter Withers
 */
public class RelationTypeDefinition {

    @XmlAttribute(name = "name", namespace = "http://mpi.nl/tla/kin")
    private String displayName;
    @XmlAttribute(name = "type", namespace = "http://mpi.nl/tla/kin")
    private RelationType relationType;
    @XmlAttribute(name = "line", namespace = "http://mpi.nl/tla/kin")
    private RelationLineType relationLineType;
    @XmlAttribute(name = "colour", namespace = "http://mpi.nl/tla/kin")
    private String lineColour;
    @XmlAttribute(name = "width", namespace = "http://mpi.nl/tla/kin")
    private int lineWidth;
    @XmlAttribute(name = "stype", namespace = "http://mpi.nl/tla/kin")
    private String lineStye;

    public RelationTypeDefinition() {
    }

    public RelationTypeDefinition(String displayName, RelationType relationType, RelationLineType relationLineType, String lineColour, int lineWidth, String lineStye) {
        this.displayName = displayName;
        this.relationType = relationType;
        this.relationLineType = relationLineType;
        this.lineColour = lineColour;
        this.lineWidth = lineWidth;
        this.lineStye = lineStye;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLineColour() {
        return lineColour;
    }

    public String getLineStye() {
        return lineStye;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public RelationLineType getRelationLineType() {
        return relationLineType;
    }

    public RelationType getRelationType() {
        return relationType;
    }
}
