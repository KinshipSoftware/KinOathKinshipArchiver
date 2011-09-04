package nl.mpi.kinnate.kindata;

import java.net.URI;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import nl.mpi.kinnate.kintypestrings.LabelStringsParser;

/**
 *  Document   : GraphDataNode
 *  Created on : Sep 11, 2010, 4:30:41 PM
 *  Author     : Peter Withers
 */
@XmlRootElement(name = "Entity", namespace = "http://mpi.nl/tla/kin")
public class EntityData {

    public enum SymbolType {
        // symbol terms are used here to try to keep things agnostic

        square, triangle, circle, union, resource, none, error
    }
    @XmlElement(name = "Identifier", namespace = "http://mpi.nl/tla/kin")
    private UniqueIdentifier uniqueIdentifier;
    @XmlElement(name = "Path", namespace = "http://mpi.nl/tla/kin")
    private String entityPath;
    @XmlElement(name = "KinType", namespace = "http://mpi.nl/tla/kin")
    private String[] kinTypeArray = new String[]{};
    @XmlElement(name = "KinTerm", namespace = "http://mpi.nl/tla/kin")
    private GraphLabel[] kinTermArray = new GraphLabel[]{};
    private SymbolType symbolType;
    @XmlElement(name = "Symbol", namespace = "http://mpi.nl/tla/kin")
    private String symbolTypeString;
    @XmlElement(name = "DateOfBirth", namespace = "http://mpi.nl/tla/kin")
    private Date dateOfBirth; // todo: use this in the graph sort and offer to show on the graph
    @XmlElement(name = "DateOfDeath", namespace = "http://mpi.nl/tla/kin")
    private Date dateOfDeath; // todo: use this in the graph to draw a line through or similar
    @XmlTransient
    public boolean isEgo = false;
//    @XmlElementWrapper(name = "Labels", namespace="http://mpi.nl/tla/kin")
    @XmlElement(name = "Label", namespace = "http://mpi.nl/tla/kin")
    private String[] labelStringArray = new String[]{};
    @XmlTransient
    ArrayList<String> tempLabelsList = null;
    @XmlElementWrapper(name = "Relations", namespace = "http://mpi.nl/tla/kin")
    @XmlElement(name = "Relation", namespace = "http://mpi.nl/tla/kin")
    private EntityRelation[] relatedNodes;
    @XmlElement(name = "ArchiveLink", namespace = "http://mpi.nl/tla/kin")
    // todo: this needs to provide both the archive handle (for opening the browser) and the url to open localy stored copy of the file
    //@Deprecated // todo: should this be separate from the relations? can it even work that way? replace this archive link with a relation of type resource: but then again a resource link such as a jpg cannot be a relaion it is metadata only, maybe this is also the case for collector?
    public URI[] archiveLinkArray = null; //new String[]{"http://corpus1.mpi.nl/ds/imdi_browser/?openpath=hdl%3A1839%2F00-0000-0000-000D-2E72-7", "http://www.google.com", "http://www.mpi.nl"};
//    @XmlElement(name = "ResourceLink")
//    public String[] resourceLinkArray;
    @XmlTransient
    public boolean metadataRequiresSave = false;
    @XmlTransient
    public boolean isVisible = false;
    @XmlTransient
    private EntityRelation[] visiblyRelateNodes = null;
    @XmlTransient
    private EntityRelation[] distinctRelateNodes = null;

    private EntityData() {
    }

//    public EntityData(String entityPathLocal, String kinTypeStringLocal, String symbolTypeLocal, String[] labelStringLocal, boolean isEgoLocal) {
//        uniqueIdentifier = new UniqueIdentifier(UniqueIdentifier.IdentifierType.tid);
//        entityPath = entityPathLocal;
//        kinTypeArray = new String[]{kinTypeStringLocal};
//        symbolType = null;
//        symbolTypeString = symbolTypeLocal;
//        labelStringArray = labelStringLocal;
//        isEgo = isEgoLocal;
//    }
//    public EntityData(String entityPathLocal, String kinTypeStringLocal, SymbolType symbolIndex, String[] labelStringLocal, boolean isEgoLocal) {
//        uniqueIdentifier = new UniqueIdentifier(UniqueIdentifier.IdentifierType.tid);
//        entityPath = entityPathLocal;
//        kinTypeArray = new String[]{kinTypeStringLocal};
//        symbolType = symbolIndex;
//        labelStringArray = labelStringLocal;
//        isEgo = isEgoLocal;
//    }
    public EntityData(LabelStringsParser labelStringsParser, String kinTypeStringLocal, SymbolType symbolIndex, boolean isEgoLocal) {
        // this is used to enable transient entities to have the same identifier on each redraw and on loading a saved document, otherwise the entity positions on the graph get lost
        uniqueIdentifier = labelStringsParser.uniqueIdentifier;
        entityPath = null;
        kinTypeArray = new String[]{kinTypeStringLocal};
        symbolType = symbolIndex;
        labelStringArray = labelStringsParser.labelsStrings;
        isEgo = isEgoLocal;
        dateOfBirth = labelStringsParser.dateOfBirth;
        dateOfDeath = labelStringsParser.dateOfDeath;
    }

    public EntityData(UniqueIdentifier uniqueIdentifierLocal, String[] errorMessage) {
        // this is used only to return error messages from a query that fails to get an entity and to prevent that query being hit again
        uniqueIdentifier = uniqueIdentifierLocal;
        entityPath = null;
        symbolType = SymbolType.error;
        symbolTypeString = null;
        labelStringArray = errorMessage;
        isEgo = false;
    }

    // begin code used for importing gedcom and other file types
    public EntityData(UniqueIdentifier uniqueIdentifierLocal) {
        uniqueIdentifier = uniqueIdentifierLocal;
        entityPath = null;
        symbolType = null;
        symbolTypeString = null;
        labelStringArray = null;
        isEgo = false;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setDateOfDeath(Date dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
    }

//    public void setSymbolType(SymbolType symbolTypeLocal) {
//        this.symbolTypeString = symbolTypeLocal.name();
//    }
    // end code used for importing gedcom and other file types
    public void addArchiveLink(URI resourceUri) {
        ArrayList<URI> linksList;
        if (archiveLinkArray != null) {
            linksList = new ArrayList<URI>(Arrays.asList(archiveLinkArray));
        } else {
            linksList = new ArrayList<URI>();
        }
        linksList.add(resourceUri);
        archiveLinkArray = linksList.toArray(new URI[]{});
    }

    public String getSymbolType() {
        if (symbolType != null) {
            return symbolType.name();
        }
        return symbolTypeString;
    }

    @XmlTransient
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    @XmlTransient
    public Date getDateOfDeath() {
        return dateOfDeath;
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
                    entityRelation.getAlterNode().addRelatedNode(this, DataTypes.RelationType.sibling, DataTypes.RelationLineType.sanguineLine, null, null);
                    this.addRelatedNode(entityRelation.getAlterNode(), DataTypes.RelationType.sibling, DataTypes.RelationLineType.sanguineLine, null, null);
                }
            }
        }
    }

    public void removeRelationsWithNode(EntityData alterNodeLocal) {
        ArrayList<EntityRelation> uniqueNodes = new ArrayList<EntityRelation>();
        if (relatedNodes != null) {
            for (EntityRelation nodeRelation : relatedNodes) {
                if (!alterNodeLocal.uniqueIdentifier.equals(nodeRelation.alterUniqueIdentifier)) {
                    uniqueNodes.add(nodeRelation);
                }
            }
        }
        relatedNodes = uniqueNodes.toArray(new EntityRelation[]{});
        distinctRelateNodes = null;
    }

    public void addRelatedNode(EntityData alterNodeLocal, /*int generationalDistance,*/ DataTypes.RelationType relationType, DataTypes.RelationLineType relationLineType, String lineColourLocal, String labelString) {
        // note that the test gedcom file has multiple links for a given pair so in might be necessary to filter incoming links on a preferential basis
        EntityRelation nodeRelation = new EntityRelation();
        nodeRelation.setAlterNode(alterNodeLocal);
//        nodeRelation.generationalDistance = generationalDistance;
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
            alterNodeLocal.addRelatedNode(this, opposingRelationType, relationLineType, null, null);
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
            ArrayList<UniqueIdentifier> processedIds = new ArrayList<UniqueIdentifier>();
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

    public EntityRelation[] getRelatedNodesToBeLoaded() {
        ArrayList<EntityRelation> entityRelationsToLoad = new ArrayList<EntityRelation>();
        for (EntityRelation relatedNode : getDistinctRelateNodes()) {
            if (relatedNode.getAlterNode() == null) {
                entityRelationsToLoad.add(relatedNode);
            }
        }
        return entityRelationsToLoad.toArray(new EntityRelation[]{});
    }

    public UniqueIdentifier getUniqueIdentifier() {
        return uniqueIdentifier;
    }
}
