package nl.mpi.kinnate.ui;

import java.util.ArrayList;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.kinnate.data.KinTreeNode;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : KinTree
 * Created on : Aug 25, 2011, 11:44:11 AM
 * Author : Peter Withers
 */
public class KinTree extends ArbilTree {

    private KinDiagramPanel kinDiagramPanel;
    private GraphPanel graphPanel;
    private ArbilNode[] selectedNodeArray = new ArbilNode[]{};

    public KinTree(KinDiagramPanel kinDiagramPanel, GraphPanel graphPanel) {
        this.kinDiagramPanel = kinDiagramPanel;
        this.graphPanel = graphPanel;
    }

    @Override
    protected void putSelectionIntoPreviewTable() {
        // todo: in arbil the preview table only shows the lead selection, however in the kinship tree it might best to show the entire selection
        selectedNodeArray = getAllSelectedNodes(); //LeadSelectionNode();
        ArrayList<UniqueIdentifier> identifierList = new ArrayList<UniqueIdentifier>();
//        ArrayList<URI> uriList = new ArrayList<URI>();
        graphPanel.metadataPanel.removeAllArbilDataNodeRows();
        graphPanel.metadataPanel.removeAllEditors();
        for (ArbilNode arbilNode : selectedNodeArray) {
            if (arbilNode instanceof ArbilDataNode) {
//                uriList.add(((ArbilDataNode) arbilNode).getURI());
                graphPanel.metadataPanel.addArbilDataNode((ArbilDataNode) arbilNode);
            } else if (arbilNode instanceof KinTreeNode) {
                final KinTreeNode kinTreeNode = (KinTreeNode) arbilNode;
                // set the graph selection
                if (kinTreeNode.entityData != null) {
                    identifierList.add(kinTreeNode.entityData.getUniqueIdentifier());
                    graphPanel.metadataPanel.addEntityDataNode(kinDiagramPanel, ((KinTreeNode) arbilNode).entityData);
                }
            }
        }
        // set the graph selection
        graphPanel.setSelectedIds(identifierList.toArray(new UniqueIdentifier[]{}));
        graphPanel.metadataPanel.updateEditorPane();
        kinDiagramPanel.drawGraph(); //redrawIfKinTermsChanged(); // todo: is this syncronous or done in a thread?
    }

    public ArbilNode[] getSelectedNodeArray() {
        return selectedNodeArray;
    }
}
