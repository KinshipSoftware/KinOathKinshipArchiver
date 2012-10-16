package nl.mpi.kinnate.kindata;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import nl.mpi.kinnate.kintypestrings.KinType;
import nl.mpi.kinnate.kintypestrings.LabelStringsParser;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : GraphDataNode
 * Created on : Sep 11, 2010, 4:30:41 PM
 * Author : Peter Withers
 */
@XmlRootElement(name = "Entity", namespace = "http://mpi.nl/tla/kin")
public class EntityData {

    public enum SymbolType {
        // symbol terms are used here to try to keep things agnostic

        square, triangle, circle, union, resource, none, error
    }
    @XmlElement(name = "Identifier", namespace = "http://mpi.nl/tla/kin")
    private UniqueIdentifier uniqueIdentifier;
    @XmlElement(name = "CustomIdentifier", namespace = "http://mpi.nl/tla/kin")
    public String customIdentifier;
    @XmlElement(name = "Path", namespace = "http://mpi.nl/tla/kin")
    private String entityPath;
    @XmlElement(name = "KinType", namespace = "http://mpi.nl/tla/kin")
    private String[] kinTypeArray = new String[]{};
    @XmlElement(name = "KinTerm", namespace = "http://mpi.nl/tla/kin")
    private GraphLabel[] kinTermArray = new GraphLabel[]{};
    @XmlElement(name = "Symbol", namespace = "http://mpi.nl/tla/kin")
    private String[] symbolNames = new String[]{};
    @XmlElement(name = "DateOfBirth", namespace = "http://mpi.nl/tla/kin")
    private EntityDate dateOfBirth = null; // todo:. use this in the graph sort
    @XmlElement(name = "DateOfDeath", namespace = "http://mpi.nl/tla/kin")
    private EntityDate dateOfDeath = null;
    @XmlElement(name = "Ego", namespace = "http://mpi.nl/tla/kin") // required for populating the tree when first loading a saved svg
    public boolean isEgo = false;
    @XmlElement(name = "Visible", namespace = "http://mpi.nl/tla/kin") // required for populating the tree when first loading a saved svg
    public boolean isVisible = false;
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
    private EntityData[] visiblyRelateNodes = null;
    @XmlTransient
    private EntityData[] distinctRelateNodes = null;

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
    public EntityData(LabelStringsParser labelStringsParser, EntityData parentEntity, String kinTypeStringLocal, SymbolType symbolIndex, boolean isEgoLocal) {
        // this is used to enable transient entities to have the same identifier on each redraw and on loading a saved document, otherwise the entity positions on the graph get lost
        uniqueIdentifier = labelStringsParser.getUniqueIdentifier(parentEntity, kinTypeStringLocal, symbolIndex);
        entityPath = null;
        kinTypeArray = new String[]{kinTypeStringLocal};
        symbolNames = new String[]{symbolIndex.name()};
        labelStringArray = labelStringsParser.labelsStrings;
        isEgo = isEgoLocal;
        dateOfBirth = labelStringsParser.dateOfBirth;
        dateOfDeath = labelStringsParser.dateOfDeath;
        customIdentifier = labelStringsParser.uidString;
    }

    public EntityData(UniqueIdentifier uniqueIdentifierLocal, String[] errorMessage) {
        // this is used only to return error messages from a query that fails to get an entity and to prevent that query being hit again
        uniqueIdentifier = uniqueIdentifierLocal;
        entityPath = null;
        symbolNames = new String[]{SymbolType.error.name()};
        labelStringArray = errorMessage;
        isEgo = false;
    }

    // begin code used for importing gedcom and other file types
    public EntityData(UniqueIdentifier uniqueIdentifierLocal) {
        uniqueIdentifier = uniqueIdentifierLocal;
        entityPath = null;
        symbolNames = new String[]{};
        labelStringArray = null;
        isEgo = false;
    }

//    public void setDateOfBirth(String dateOfBirth) throws EntityDateException {
//        this.dateOfBirth = new EntityDate(dateOfBirth);
//    }
//
//    public void setDateOfDeath(String dateOfDeath) throws EntityDateException {
//        this.dateOfDeath = new EntityDate(dateOfDeath);
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

    public String[] getSymbolNames() {
        return symbolNames;
    }

    public String getFirstSymbolName() {
        String firstSymbolName = null;
        if (symbolNames != null && symbolNames.length > 0) {
            // get the first symbol only
            firstSymbolName = symbolNames[0];
        }
        return firstSymbolName;
    }

    @XmlTransient
    public EntityDate getDateOfBirth() {
        return dateOfBirth;
    }

    @XmlTransient
    public EntityDate getDateOfDeath() {
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
                returnString = returnString + kinType + KinType.separator;
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

    private void insertUnionRelations(EntityData childEntity, String dcrType, String customType) {
        // update the union relations of the parents other children
        for (EntityRelation entityRelation : childEntity.getAllRelations()) {
            if (entityRelation.getRelationType().equals(DataTypes.RelationType.ancestor)) {
                if (!entityRelation.getAlterNode().equals(this)) {
                    entityRelation.getAlterNode().addRelatedNode(this, DataTypes.RelationType.union, null, null, dcrType, customType);
                    this.addRelatedNode(entityRelation.getAlterNode(), DataTypes.RelationType.union, null, null, dcrType, customType);
                }
            }
        }
    }

    private void insertSiblingRelations(EntityData parentEntity, String dcrType, String customType) {
        // update the sibling relations of the parents other children
        for (EntityRelation entityRelation : parentEntity.getAllRelations()) {
            // todo: Ticket #1062  there is an issue here when you add a child node to a parent that when you add a second child node the alter node is null "getAlterNode()", maybe it is time to put all the nodes into a hash or create some kind of loader (maybe mbased on the ArbilLoader). This would also beable to service the loading branches of the tree.
            if (entityRelation.getRelationType().equals(DataTypes.RelationType.descendant)) {
                if (!entityRelation.getAlterNode().equals(this)) {
                    entityRelation.getAlterNode().addRelatedNode(this, DataTypes.RelationType.sibling, null, null, dcrType, customType);
                    this.addRelatedNode(entityRelation.getAlterNode(), DataTypes.RelationType.sibling, null, null, dcrType, customType);
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

    public EntityRelation addRelatedNode(EntityData alterNodeLocal, /* int generationalDistance, */ DataTypes.RelationType relationType, String lineColour, String labelString, String dcrType, String customType) {
        // note that the test gedcom file has multiple links for a given pair so in might be necessary to filter incoming links on a preferential basis
        EntityRelation nodeRelation = new EntityRelation(dcrType, customType, lineColour, relationType, labelString);
        nodeRelation.setAlterNode(alterNodeLocal);
        if (relatedNodes != null) {
            // check for existing relations matching the one to be added and prevent duplicates
            for (EntityRelation entityRelation : relatedNodes) {
                if (entityRelation.equals(nodeRelation)) {
                    return entityRelation;
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
//        if (!relationType.equals(DataTypes.RelationType.none)) {
        DataTypes.RelationType opposingRelationType = DataTypes.getOpposingRelationType(relationType);
        alterNodeLocal.addRelatedNode(this, opposingRelationType, null, null, dcrType, customType);
        // if a child relation is beig added then update the union relations of the other parents of that child
        // if a parent relation is beig added then update the sibling relations of the other children of that parent
        if (relationType.equals(DataTypes.RelationType.ancestor)) {
            this.insertSiblingRelations(alterNodeLocal, dcrType, customType);
            alterNodeLocal.insertUnionRelations(this, dcrType, customType);
        } else if (relationType.equals(DataTypes.RelationType.descendant)) {
            alterNodeLocal.insertSiblingRelations(this, dcrType, customType);
            this.insertUnionRelations(alterNodeLocal, dcrType, customType);
        }
        // if a sibling has been added then there is no way to know if any of the parents are common to the other sibings, so we do nothing in this case
//        }
        return nodeRelation;
    }

    public void clearVisibility() {
        isVisible = false;
        isEgo = false;
        visiblyRelateNodes = null;
        distinctRelateNodes = null;
    }

    public EntityData[] getVisiblyRelated() {
        if (visiblyRelateNodes == null) {
            ArrayList<EntityData> visiblyRelatedNodes = new ArrayList<EntityData>();
            for (EntityData entityData : getDistinctRelateNodes()) {
                if (entityData != null) {
                    if (entityData.isVisible) {
                        visiblyRelatedNodes.add(entityData);
                    }
                }
            }
            visiblyRelateNodes = visiblyRelatedNodes.toArray(new EntityData[]{});
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

    public EntityData[] getDistinctRelateNodes() {
        if (distinctRelateNodes == null) {
            ArrayList<UniqueIdentifier> processedIds = new ArrayList<UniqueIdentifier>();
            ArrayList<EntityData> uniqueNodes = new ArrayList<EntityData>();
            if (relatedNodes != null) {
                for (EntityRelation nodeRelation : relatedNodes) {
//                    if (!onlySanguine || DataTypes.isSanguinLine(nodeRelation.relationType)) {
                    if (!processedIds.contains(nodeRelation.alterUniqueIdentifier)) {
                        uniqueNodes.add(nodeRelation.getAlterNode());
                        processedIds.add(nodeRelation.alterUniqueIdentifier);
                    }
//                    }
                }
            }
            distinctRelateNodes = uniqueNodes.toArray(new EntityData[]{});
        }
        return distinctRelateNodes;
    }

    public EntityRelation[] getRelatedNodesToBeLoaded() {
        ArrayList<EntityRelation> entityRelationsToLoad = new ArrayList<EntityRelation>();
        for (EntityRelation relatedNode : getAllRelations()) {
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
