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

import java.util.HashSet;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.entityindexer.IndexerParameters;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.svg.DataStoreSvg;
import nl.mpi.kinnate.svg.SymbolGraphic;

/**
 * Document : KinTreeFilteredNode Created on : Mar 28, 2012, 1:30:23 PM
 *
 * @author Peter Withers
 */
public class KinTreeFilteredNode extends KinTreeNode {

    DataTypes.RelationType subnodeFilter;
    EntityRelation entityRelation;

    public KinTreeFilteredNode(SymbolGraphic symbolGraphic, EntityRelation entityRelation, DataStoreSvg dataStoreSvg, IndexerParameters indexerParameters, MessageDialogHandler dialogHandler, EntityCollection entityCollection, ArbilDataNodeLoader dataNodeLoader) {
        super(symbolGraphic, entityRelation.alterUniqueIdentifier, entityRelation.getAlterNode(), dataStoreSvg, indexerParameters, dialogHandler, entityCollection, dataNodeLoader);
        this.subnodeFilter = entityRelation.getRelationType(); // subnode filter will be used to filter the child nodes
        this.entityRelation = entityRelation;
    }

    protected void loadEntityIfNotLoaded() {
        if (entityData == null) {
            // todo: should these enties be cached? or will the entire tree be discarded on redraw?
            // todo change this to return a node imediately and the node can then load itself and then request a tree resort
            entityData = entityRelation.getAlterNode();
            if (entityData == null) {
                try {
                    entityData = entityCollection.getEntity(entityRelation.alterUniqueIdentifier, indexerParameters);
                } catch (EntityServiceException exception) {
                    entityData = new EntityData(entityRelation.alterUniqueIdentifier, new String[]{"Error loading the entity data", "view log for details"});
                }
                entityRelation.setAlterNode(entityData);
            }
        }
    }

    @Override
    public ArbilNode[] getChildArray() {
        if (childNodes == null) {
            if (subnodeFilter != DataTypes.RelationType.ancestor && subnodeFilter != DataTypes.RelationType.descendant) {
                // anything other than directional relations need not be shown beneath this filtered node
                // siblings need not show sub nodes of their siblings
                // relation type other need not be shown 
                return new ArbilNode[]{};
            }
            HashSet<ArbilNode> relationList = new HashSet<ArbilNode>();
            for (EntityRelation entityRelation : entityData.getAllRelations()) {
                if (subnodeFilter == entityRelation.getRelationType()) {
//                EntityData alterEntity = entityRelation.getAlterNode();
//                if (alterEntity == null) {
//                    alterEntity = entityCollection.getEntity(entityRelation.alterUniqueIdentifier, indexerParameters);
//                    entityRelation.setAlterNode(alterEntity);
//                }
                    relationList.add(new KinTreeFilteredNode(symbolGraphic, entityRelation, dataStoreSvg, indexerParameters, dialogHandler, entityCollection, dataNodeLoader));
                }
            }
            getLinksMetaNode(relationList);
            childNodes = relationList.toArray(new ArbilNode[]{});
            new Thread() {
                @Override
                public void run() {
                    for (ArbilNode childNode : childNodes) {
                        ((KinTreeFilteredNode) childNode).loadEntityIfNotLoaded();
                    }
                }
            }.start();
        }
        return childNodes;
    }
}
