package nl.mpi.kinnate.data;

import java.util.HashSet;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.IndexerParameters;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityRelation;

/**
 * Document : KinTreeFilteredNode
 * Created on : Mar 28, 2012, 1:30:23 PM
 * Author : Peter Withers
 */
public class KinTreeFilteredNode extends KinTreeNode {

    DataTypes.RelationType subnodeFilter;
    EntityRelation entityRelation;

    public KinTreeFilteredNode(EntityRelation entityRelation, IndexerParameters indexerParameters, MessageDialogHandler dialogHandler, EntityCollection entityCollection, ArbilDataNodeLoader dataNodeLoader) {
        super(entityRelation.alterUniqueIdentifier, entityRelation.getAlterNode(), indexerParameters, dialogHandler, entityCollection, dataNodeLoader);
        this.subnodeFilter = entityRelation.getRelationType(); // subnode filter will be used to filter the child nodes
        this.entityRelation = entityRelation;
    }

    protected void loadEntityIfNotLoaded() {
        if (entityData == null) {
            // todo: should these enties be cached? or will the entire tree be discarded on redraw?
            // todo change this to return a node imediately and the node can then load itself and then request a tree resort
            entityData = entityRelation.getAlterNode();
            if (entityData == null) {
                entityData = entityCollection.getEntity(entityRelation.alterUniqueIdentifier, indexerParameters);
                entityRelation.setAlterNode(entityData);
            }
        }
    }

    @Override
    public ArbilNode[] getChildArray() {
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
                relationList.add(new KinTreeFilteredNode(entityRelation, indexerParameters, dialogHandler, entityCollection, dataNodeLoader));
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
        return childNodes;
    }
}
