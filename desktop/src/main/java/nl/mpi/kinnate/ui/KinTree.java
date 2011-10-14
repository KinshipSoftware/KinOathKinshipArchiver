package nl.mpi.kinnate.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.kinnate.data.KinTreeNode;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 *  Document   : KinTree
 *  Created on : Aug 25, 2011, 11:44:11 AM
 *  Author     : Peter Withers
 */
public class KinTree extends ArbilTree {

    GraphPanel graphPanel;

    public KinTree(GraphPanel graphPanel) {
        this.graphPanel = graphPanel;
    }

    @Override
    protected void putSelectionIntoPreviewTable() {
        // todo: in arbil the preview table only shows the lead selection, however in the kinship tree it might best to show the entire selection
        ArbilNode[] arbilNodeArray = getAllSelectedNodes(); //LeadSelectionNode();
        ArrayList<UniqueIdentifier> identifierList = new ArrayList<UniqueIdentifier>();
//        ArrayList<URI> uriList = new ArrayList<URI>();
        graphPanel.metadataPanel.removeAllArbilDataNodeRows();
        for (ArbilNode arbilNode : arbilNodeArray) {
            if (arbilNode instanceof ArbilDataNode) {
//                uriList.add(((ArbilDataNode) arbilNode).getURI());
                graphPanel.metadataPanel.addSingleArbilDataNode((ArbilDataNode) arbilNode);
            } else if (arbilNode instanceof KinTreeNode) {
                final KinTreeNode kinTreeNode = (KinTreeNode) arbilNode;
                // set the graph selection
                if (kinTreeNode.entityData != null) {
                    identifierList.add(kinTreeNode.entityData.getUniqueIdentifier());
                    try {
                        final ArbilDataNode arbilDataNode = ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, new URI(((KinTreeNode) arbilNode).entityData.getEntityPath()));
                        graphPanel.metadataPanel.addSingleArbilDataNode(arbilDataNode);
                    } catch (URISyntaxException urise) {
                        GuiHelper.linorgBugCatcher.logError(urise);
                    }
                }
            }
        }
        // set the graph selection
        graphPanel.setSelectedIds(identifierList.toArray(new UniqueIdentifier[]{}));
        graphPanel.metadataPanel.updateEditorPane();
    }
}
