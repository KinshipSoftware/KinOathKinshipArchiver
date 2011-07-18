package nl.mpi.kinnate.kindata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *  Document   : GraphDataNode
 *  Created on : Sep 11, 2010, 4:30:41 PM
 *  Author     : Peter Withers
 */
@XmlRootElement(name = "Entity")
public class EntityData {

    public enum SymbolType {
        // symbol terms are used here to try to keep things agnostic

        square, triangle, circle, union, resource, ego, none
    }
    @XmlElement(name = "Identifier")
    private String uniqueIdentifier;
    @XmlElement(name = "Path")
    private String entityPath;
    @XmlElement(name = "KinType")
    private String[] kinTypeArray = new String[]{};
    @XmlElement(name = "KinTerm")
    private GraphLabel[] kinTermArray = new GraphLabel[]{};
    private SymbolType symbolType;
    @XmlElement(name = "Symbol")
    private String symbolTypeString;
    @XmlElement(name = "DateOfBirth")
    private Date dateOfBirth; // todo: use this in the graph sort and offer to show on the graph
    public boolean isEgo = false;
    @XmlElementWrapper(name = "Labels")
    @XmlElement(name = "String")
    private String[] labelStringArray;
    ArrayList<String> tempLabelsList = null;
    @XmlElementWrapper(name = "Relations")
    @XmlElement(name = "Relation")
    private EntityRelation[] relatedNodes;
    @XmlElement(name = "ArchiveLink")
    // todo: this needs to provide both the archive handle (for opening the browser) and the url to open localy stored copy of the file
    public String[] archiveLinkArray = null; //new String[]{"http://corpus1.mpi.nl/ds/imdi_browser/?openpath=hdl%3A1839%2F00-0000-0000-000D-2E72-7", "http://www.google.com", "http://www.mpi.nl"};
//    @XmlElement(name = "ResourceLink")
//    public String[] resourceLinkArray;
    @XmlTransient
    public boolean metadataRequiresSave = false;
    public boolean isVisible = false;
    private EntityRelation[] visiblyRelateNodes = null;
    private EntityRelation[] distinctRelateNodes = null;

    private EntityData() {
    }

    public EntityData(String uniqueIdentifierLocal, String entityPathLocal, String kinTypeStringLocal, String symbolTypeLocal, String[] labelStringLocal, boolean isEgoLocal) {
        uniqueIdentifier = uniqueIdentifierLocal;
        entityPath = entityPathLocal;
        kinTypeArray = new String[]{kinTypeStringLocal};
        symbolType = null;
        symbolTypeString = symbolTypeLocal;
        labelStringArray = labelStringLocal;
        isEgo = isEgoLocal;
    }

    public EntityData(String uniqueIdentifierLocal, String entityPathLocal, String kinTypeStringLocal, SymbolType symbolIndex, String[] labelStringLocal, boolean isEgoLocal) {
        uniqueIdentifier = uniqueIdentifierLocal;
        entityPath = entityPathLocal;
        kinTypeArray = new String[]{kinTypeStringLocal};
        symbolType = symbolIndex;
        labelStringArray = labelStringLocal;
        isEgo = isEgoLocal;
    }

    public String getSymbolType() {
        if (symbolType != null) {
            switch (symbolType) {
                case circle:
                    return "circle";
                case ego:
                    return "square";
                case none:
                    return "none";
                case resource:
                    return "resource";
                case square:
                    return "square";
                case triangle:
                    return "triangle";
                case union:
                    return "union";
            }
        }
        return symbolTypeString;
    }

    public String getEntityPath() {
        return entityPath;
    }

    public void addKinTypeString(String kinTypeString) {
        ArrayList<String> tempList = new ArrayList<String>(Arrays.asList(kinTypeArray));
        if (!tempList.contains(kinTypeString)) {
            tempList.add(kinTypeString);
            kinTypeArray = tempList.toArray(new String[]{});
        }
    }

    public String[] getKinTypeStringArray() {
        return kinTypeArray;
    }

    public String getKinTypeString() {
        String returnString = "";
        if (kinTypeArray.length > 0) {
            for (String kinType : kinTypeArray) {
                returnString = returnString + kinType + "|";
            }
            returnString = returnString.substring(0, returnString.length() - 1);
        }
        return returnString;
    }

    public void addKinTermString(String kinTermString, String colourString) {
        for (GraphLabel currentLabel : kinTermArray) {
            if (currentLabel.getLabelString().equals(kinTermString) && currentLabel.getColourString().equals(colourString)) {
                // prevent duplicates
                return;
            }
        }
        ArrayList<GraphLabel> tempList = new ArrayList<GraphLabel>(Arrays.asList(kinTermArray));
        tempList.add(new GraphLabel(kinTermString, colourString));
        kinTermArray = tempList.toArray(new GraphLabel[]{});
    }

    public GraphLabel[] getKinTermStrings() {
        return kinTermArray;
    }

    public String[] getLabel() {
        if (tempLabelsList != null) {
            return tempLabelsList.toArray(new String[]{});
        } else {
            return labelStringArray;
        }
    }

    public void clearTempLabels() {
        tempLabelsList = null;
    }

    public void appendTempLabel(String labelString) {
        if (tempLabelsList == null) {
            tempLabelsList = new ArrayList<String>(Arrays.asList(labelStringArray));
        }
        if (!tempLabelsList.contains(labelString)) {
            tempLabelsList.add(labelString);
        }
    }

    private void insertSiblingRelations(EntityData parentEntity) {
        // update the sibling relations of the parents other children
        for (EntityRelation entityRelation : parentEntity.getAllRelations()) {
            if (entityRelation.relationType.equals(DataTypes.RelationType.descendant)) {
                if (!entityRelation.getAlterNode().equals(this)) {
                    entityRelation.getAlterNode().addRelatedNode(this, 0, DataTypes.RelationType.sibling, DataTypes.RelationLineType.sanguineLine, null, null);
                    this.addRelatedNode(entityRelation.getAlterNode(), 0, DataTypes.RelationType.sibling, DataTypes.RelationLineType.sanguineLine, null, null);
                }
            }
        }
    }

    public void addRelatedNode(EntityData alterNodeLocal, int generationalDistance, DataTypes.RelationType relationType, DataTypes.RelationLineType relationLineType, String lineColourLocal, String labelString) {
        // note that the test gedcom file has multiple links for a given pair so in might be necessary to filter incoming links on a preferential basis
        EntityRelation nodeRelation = new EntityRelation();
        nodeRelation.setAlterNode(alterNodeLocal);
        nodeRelation.generationalDistance = generationalDistance;
        nodeRelation.relationType = relationType;
        nodeRelation.relationLineType = relationLineType;
        nodeRelation.labelString = labelString;
        nodeRelation.lineColour = lineColourLocal;
        if (relatedNodes != null) {
            // check for existing relations matching the one to be added and prevent duplicates
            for (EntityRelation entityRelation : relatedNodes) {
                if (entityRelation.compareTo(nodeRelation) == 0) {
                    return;
                }
            }
            // add the relation
            ArrayList<EntityRelation> relatedNodesList = new ArrayList<EntityRelation>();
            relatedNodesList.addAll(Arrays.asList(relatedNodes));
            relatedNodesList.add(nodeRelation);
            relatedNodes = relatedNodesList.toArray(new EntityRelation[]{});
        } else {
            relatedNodes = new EntityRelation[]{nodeRelation};
        }
        distinctRelateNodes = null; // if we get here then clear the distinct related node array so that it gets recalculated
        // add this relation to any existing relations
        if (!relationType.equals(DataTypes.RelationType.none)) {
            DataTypes.RelationType opposingRelationType = DataTypes.getOpposingRelationType(relationType);
            alterNodeLocal.addRelatedNode(this, 0, opposingRelationType, DataTypes.RelationLineType.sanguineLine, null, null);
            // if a parent relation is beig added then update the sibling relations of the other children of that parent
            if (relationType.equals(DataTypes.RelationType.ancestor)) {
                this.insertSiblingRelations(alterNodeLocal);
            } else if (relationType.equals(DataTypes.RelationType.descendant)) {
                alterNodeLocal.insertSiblingRelations(this);
            }
            // if a sibling has been added then there is no way to know if any of the parents are common to the other sibings, so we do nothing in this case
        }
    }

    public void clearVisibility() {
        isVisible = false;
        isEgo = false;
        visiblyRelateNodes = null;
        distinctRelateNodes = null;
    }

    public EntityRelation[] getVisiblyRelateNodes() {
        if (visiblyRelateNodes == null) {
            ArrayList<EntityRelation> visiblyRelatedNodes = new ArrayList<EntityRelation>();
            for (EntityRelation nodeRelation : getDistinctRelateNodes()) {
                if (nodeRelation.getAlterNode() != null) {
                    if (nodeRelation.getAlterNode().isVisible) {
                        visiblyRelatedNodes.add(nodeRelation);
                    }
                }
            }
            visiblyRelateNodes = visiblyRelatedNodes.toArray(new EntityRelation[]{});
        }
        return visiblyRelateNodes;
    }

    public EntityRelation[] getAllRelations() {
        if (relatedNodes == null) {
            return new EntityRelation[]{};
        } else {
            return relatedNodes;
        }
    }

    public EntityRelation[] getDistinctRelateNodes() {
        if (distinctRelateNodes == null) {
            ArrayList<String> processedIds = new ArrayList<String>();
            ArrayList<EntityRelation> uniqueNodes = new ArrayList<EntityRelation>();
            if (relatedNodes != null) {
                for (EntityRelation nodeRelation : relatedNodes) {
                    if (!processedIds.contains(nodeRelation.alterUniqueIdentifier)) {
                        uniqueNodes.add(nodeRelation);
                        processedIds.add(nodeRelation.alterUniqueIdentifier);
                    }
                }
            }
            distinctRelateNodes = uniqueNodes.toArray(new EntityRelation[]{});
        }
        return distinctRelateNodes;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }
}
