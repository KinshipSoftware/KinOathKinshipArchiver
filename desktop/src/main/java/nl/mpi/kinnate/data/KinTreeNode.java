package nl.mpi.kinnate.data;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;
import javax.swing.ImageIcon;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ContainerNode;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.IndexerParameters;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.svg.SymbolGraphic;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : KinTreeNode
 * Created on : Aug 22, 2011, 5:14:05 PM
 * Author : Peter Withers
 */
public class KinTreeNode extends ArbilNode implements Comparable {

    private UniqueIdentifier uniqueIdentifier;
    protected EntityData entityData = null;
    protected IndexerParameters indexerParameters;
    protected ArbilNode[] childNodes = null;
    static private SymbolGraphic symbolGraphic = null;
    protected EntityCollection entityCollection;
    protected MessageDialogHandler dialogHandler;
    protected ArbilDataNodeLoader dataNodeLoader;
    private String derivedLabelString = null;

    public KinTreeNode(UniqueIdentifier uniqueIdentifier, EntityData entityData, IndexerParameters indexerParameters, MessageDialogHandler dialogHandler, EntityCollection entityCollection, ArbilDataNodeLoader dataNodeLoader) {
        // todo: create new constructor that takes a unique identifer and loads from the database.
        super();
        this.uniqueIdentifier = uniqueIdentifier;
        this.indexerParameters = indexerParameters;
        this.entityData = entityData;
        this.entityCollection = entityCollection;
        this.dialogHandler = dialogHandler;
        this.dataNodeLoader = dataNodeLoader;
        if (symbolGraphic == null) {
            symbolGraphic = new SymbolGraphic(dialogHandler);
        }
    }

//    public void setEntityData(EntityData entityData) {
//        // todo: this does not cause the tree to update so it is redundent 
//        this.entityData = entityData;
//        derivedLabelString = null;
//        symbolGraphic = null;
//        // todo: clear or set the child entity data 
//        //childNodes
//    }

    public EntityData getEntityData() {
        return entityData;
    }

    public UniqueIdentifier getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    @Override
    public String toString() {
        if (derivedLabelString == null) {
            if (entityData == null) {
                return "(entity not loaded)";
            } else {
                StringBuilder labelBuilder = new StringBuilder();
                final String[] labelArray = entityData.getLabel();
                if (labelArray != null && labelArray.length > 0) {
                    for (String labelString : labelArray) {
                        labelBuilder.append(labelString);
                        labelBuilder.append(" ");
                    }
                } else {
                    labelBuilder.append("              ");
                }
                derivedLabelString = labelBuilder.toString();
            }
        }
        return derivedLabelString;
    }

    @Override
    public ArbilDataNode[] getAllChildren() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getAllChildren(Vector<ArbilDataNode> allChildren) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ArbilNode[] getChildArray() {
        if (childNodes == null) {
            // add the related entities grouped into metanodes by relation type and within each group the subsequent nodes are filtered by the type of relation.
            HashMap<DataTypes.RelationType, HashSet<KinTreeFilteredNode>> metaNodeMap = new HashMap<DataTypes.RelationType, HashSet<KinTreeFilteredNode>>();
            for (EntityRelation entityRelation : entityData.getAllRelations()) {
                if (!metaNodeMap.containsKey(entityRelation.getRelationType())) {
                    metaNodeMap.put(entityRelation.getRelationType(), new HashSet<KinTreeFilteredNode>());
                }
                metaNodeMap.get(entityRelation.getRelationType()).add(new KinTreeFilteredNode(entityRelation, indexerParameters, dialogHandler, entityCollection, dataNodeLoader));
            }
            HashSet<ArbilNode> kinTreeMetaNodes = new HashSet<ArbilNode>();
            for (Map.Entry<DataTypes.RelationType, HashSet<KinTreeFilteredNode>> filteredNodeEntry : metaNodeMap.entrySet()) {//values().toArray(new KinTreeFilteredNode[]{})
                kinTreeMetaNodes.add(new FilteredNodeContainer(filteredNodeEntry.getKey().name(), null, filteredNodeEntry.getValue().toArray(new KinTreeFilteredNode[]{})));
            }
            getLinksMetaNode(kinTreeMetaNodes);
            childNodes = kinTreeMetaNodes.toArray(new ArbilNode[]{});
        }
        return childNodes;
    }

    protected void getLinksMetaNode(HashSet<ArbilNode> kinTreeMetaNodes) {
        if (entityData.archiveLinkArray != null) {
            HashSet<ArbilDataNode> relationList = new HashSet<ArbilDataNode>();
            for (URI archiveLink : entityData.archiveLinkArray) {
                ArbilDataNode linkedArbilDataNode = dataNodeLoader.getArbilDataNode(null, archiveLink);
                relationList.add(linkedArbilDataNode);
            }
            kinTreeMetaNodes.add(new ContainerNode("External Links", null, relationList.toArray(new ArbilDataNode[]{})));
        }
    }

    @Override
    public int getChildCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImageIcon getIcon() {
        if (entityData != null) {
            return symbolGraphic.getSymbolGraphic(entityData.getSymbolNames(), entityData.isEgo);
        }
        return null;
    }

    @Override
    public boolean hasCatalogue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasHistory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasLocalResource() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasResource() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isArchivableFile() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCatalogue() {
        return false;
    }

    @Override
    public boolean isChildNode() {
        return false;
    }

    @Override
    public boolean isCmdiMetaDataNode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCorpus() {
        return false;
    }

    @Override
    public boolean isDataLoaded() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isEditable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isEmptyMetaNode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isFavorite() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isLoading() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDataPartiallyLoaded() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isLocal() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isMetaDataNode() {
        return false;
    }

    @Override
    public boolean isResourceSet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSession() {
        return false;
    }

    public int compareTo(Object o) {
        if (o instanceof KinTreeNode) {
            int compResult = this.toString().compareTo(o.toString());
            if (compResult == 0) {
                // todo: compare by age if the labels match
//                compResult = entityData
            }
            return compResult;
        } else {
            // put kin nodes in front of other nodes
            return 1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KinTreeNode other = (KinTreeNode) obj;
        // we compare the entity data instance because this is the only way to update the arbil tree
        // todo: this does not break the graph selection process but check for other places where equals might be used
        return this.entityData == other.entityData;

////        return this.hashCode() == other.hashCode();
//        if (entityData == null || other.entityData == null) {
//            // todo: it would be good for this to never be null, or at least to aways have the UniqueIdentifier to compare
//            return false;
//        }
//        if (this.getUniqueIdentifier() != other.getUniqueIdentifier() && (this.getUniqueIdentifier() == null || !this.getUniqueIdentifier().equals(other.getUniqueIdentifier()))) {
//            return false;
//        }
//        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = 37 * hash + (this.uniqueIdentifier != null ? this.uniqueIdentifier.hashCode() : 0);
        return hash;
    }
}
