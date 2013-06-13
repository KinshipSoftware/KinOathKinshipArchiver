/**
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.ImageIcon;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ContainerNode;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.flap.model.DataNodeType;
import nl.mpi.flap.model.FieldGroup;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.IndexerParameters;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.kindata.ExternalLink;
import nl.mpi.kinnate.svg.DataStoreSvg;
import nl.mpi.kinnate.svg.SymbolGraphic;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : KinTreeNode Created on : Aug 22, 2011, 5:14:05 PM
 *
 * @author Peter Withers
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
    final protected DataStoreSvg dataStoreSvg;

    public KinTreeNode(UniqueIdentifier uniqueIdentifier, EntityData entityData, DataStoreSvg dataStoreSvg, IndexerParameters indexerParameters, MessageDialogHandler dialogHandler, EntityCollection entityCollection, ArbilDataNodeLoader dataNodeLoader) {
        // todo: create new constructor that takes a unique identifer and loads from the database.
        super();
        this.uniqueIdentifier = uniqueIdentifier;
        this.dataStoreSvg = dataStoreSvg;
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
                return "<entity not loaded>";
            } else {
                StringBuilder labelBuilder = new StringBuilder();
                final String[] labelArray = entityData.getLabel();
                if (labelArray != null && labelArray.length > 0) {
                    for (String labelString : labelArray) {
                        labelBuilder.append(labelString);
                        labelBuilder.append(" ");
                    }
                }
                derivedLabelString = labelBuilder.toString();
                if (derivedLabelString.replaceAll("\\s", "").isEmpty()) {
                    derivedLabelString = "<unlabeled entity>";
                }
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
            HashMap<String, HashSet<KinTreeFilteredNode>> metaNodeMap = new HashMap<String, HashSet<KinTreeFilteredNode>>();
            for (EntityRelation entityRelation : entityData.getAllRelations()) {
                final String wraperNodeLabel = entityRelation.getTypeLabel();
                if (!metaNodeMap.containsKey(wraperNodeLabel)) {
                    metaNodeMap.put(wraperNodeLabel, new HashSet<KinTreeFilteredNode>());
                }
                metaNodeMap.get(wraperNodeLabel).add(new KinTreeFilteredNode(entityRelation, dataStoreSvg, indexerParameters, dialogHandler, entityCollection, dataNodeLoader));
            }
            HashSet<ArbilNode> kinTreeMetaNodes = new HashSet<ArbilNode>();
            for (Map.Entry<String, HashSet<KinTreeFilteredNode>> filteredNodeEntry : metaNodeMap.entrySet()) {//values().toArray(new KinTreeFilteredNode[]{})
                kinTreeMetaNodes.add(new FilteredNodeContainer(filteredNodeEntry.getKey(), null, filteredNodeEntry.getValue().toArray(new KinTreeFilteredNode[]{})));
            }
            getLinksMetaNode(kinTreeMetaNodes);
            childNodes = kinTreeMetaNodes.toArray(new ArbilNode[]{});
        }
        return childNodes;
    }

    protected void getLinksMetaNode(HashSet<ArbilNode> kinTreeMetaNodes) {
        if (entityData.externalLinks != null) {
            HashSet<ArbilDataNode> relationList = new HashSet<ArbilDataNode>();
            for (ExternalLink externalLink : entityData.externalLinks) {
                ArbilDataNode linkedArbilDataNode = dataNodeLoader.getArbilDataNode(null, externalLink.getLinkUri());
                relationList.add(linkedArbilDataNode);
            }
            kinTreeMetaNodes.add(new ContainerNode(null, "External Links", null, relationList.toArray(new ArbilDataNode[]{})));
        }
    }

    @Override
    public int getChildCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImageIcon getIcon() {
        if (entityData != null) {
            return symbolGraphic.getSymbolGraphic(entityData.getSymbolNames(dataStoreSvg.defaultSymbol), entityData.isEgo);
        }
        return null;
    }

    @Override
    public String getID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<FieldGroup> getFieldGroups() {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public void setID(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLabel(String label) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLabel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setFieldGroups(List<FieldGroup> fieldGroups) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setChildIds(List<String> idString) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> getChildIds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setType(DataNodeType dataNodeType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DataNodeType getType() {
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
