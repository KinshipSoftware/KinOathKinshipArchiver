package nl.mpi.kinnate.kindata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

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
    private String[] kinTypeArray = new String[]{};
    private String[] kinTermArray = new String[]{};
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
    protected int xPos;
    protected int yPos;
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

    public int getxPos() {
        return xPos;
    }

    public void setxPos(int xPos) {
        this.xPos = xPos;
    }

    public int getyPos() {
        return yPos;
    }

//    public void setyPos(int yPos) {
//        // todo: y position cannot be set in the default layout of vertical generations
//        // this.yPos = yPos;
//    }
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
        for (String kinType : kinTypeArray) {
            returnString = returnString + kinType + "|";
        }
        returnString = returnString.substring(0, returnString.length() - 1);
        return returnString;
    }

    public void addKinTermString(String kinTermString) {
        ArrayList<String> tempList = new ArrayList<String>(Arrays.asList(kinTermArray));
        tempList.add(kinTermString);
        kinTermArray = tempList.toArray(new String[]{});
    }

    public String[] getKinTermStrings() {
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
            ArrayList<EntityRelation> relatedNodesList = new ArrayList<EntityRelation>();
            relatedNodesList.addAll(Arrays.asList(relatedNodes));
            relatedNodesList.add(nodeRelation);
            relatedNodes = relatedNodesList.toArray(new EntityRelation[]{});
        } else {
            relatedNodes = new EntityRelation[]{nodeRelation};
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
