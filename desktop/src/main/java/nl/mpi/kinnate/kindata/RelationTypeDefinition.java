package nl.mpi.kinnate.kindata;

import javax.xml.bind.annotation.XmlAttribute;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;

/**
 * Document : RelationTypeDefinition
 * Created on : Jan 2, 2012, 3:29:25 PM
 * Author : Peter Withers
 */
public class RelationTypeDefinition {

    public enum CurveLineOrientation {

        horizontal, vertical
    }
    @XmlAttribute(name = "name", namespace = "http://mpi.nl/tla/kin")
    private String displayName;
    @XmlAttribute(name = "type", namespace = "http://mpi.nl/tla/kin")
    private RelationType relationType;
    @XmlAttribute(name = "dcr", namespace = "http://mpi.nl/tla/kin")
    private String dataCategory;
    @XmlAttribute(name = "colour", namespace = "http://mpi.nl/tla/kin")
    private String lineColour;
    @XmlAttribute(name = "width", namespace = "http://mpi.nl/tla/kin")
    private int lineWidth;
    @XmlAttribute(name = "style", namespace = "http://mpi.nl/tla/kin")
    private String lineStye;
    @XmlAttribute(name = "orientation", namespace = "http://mpi.nl/tla/kin")
    private CurveLineOrientation curveLineOrientation = CurveLineOrientation.horizontal; // used to define the line orientation of curve lines only

    public RelationTypeDefinition() {
    }

    public RelationTypeDefinition(String displayName, String dataCategory, RelationType relationType, String lineColour, int lineWidth, String lineStye) {
        this.displayName = displayName;
        this.relationType = relationType;
        this.dataCategory = dataCategory;
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

    public String getDataCategory() {
        return dataCategory;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public CurveLineOrientation getCurveLineOrientation() {
        return curveLineOrientation;
    }

    public boolean matchesType(EntityRelation entityRelation) {
        if (entityRelation == null) {
            return false;
        }
        if (dataCategory == null) {
            if (entityRelation.dcrType != null) {
                return false;
            }
        } else {
            if (!dataCategory.equals(entityRelation.dcrType)) {
                return false;
            }
        }
        if (relationType == null) {
            if (entityRelation.getRelationType() != null) {
                return false;
            }
        } else {
            if (!relationType.equals(entityRelation.getRelationType())) {
                return false;
            }
        }
        if (displayName == null) {
            if (entityRelation.customType != null) {
                return false;
            }
        } else {
            if (!displayName.equals(entityRelation.customType)) {
                return false;
            }
        }
        return true;
    }
}
