package nl.mpi.kinnate.ui;

import java.net.URI;
import java.net.URISyntaxException;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.ui.ArbilTableModel;
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
        ArbilNode arbilNode = getLeadSelectionNode();
        if (arbilNode instanceof ArbilDataNode) {
            // clear the graph selection
            graphPanel.setSelectedIds(new UniqueIdentifier[]{});
            super.putSelectionIntoPreviewTable();
        } else if (arbilNode instanceof KinTreeNode) {
            final KinTreeNode kinTreeNode = (KinTreeNode) arbilNode;
            // set the graph selection
            if (kinTreeNode.entityData != null) {
                graphPanel.setSelectedIds(new UniqueIdentifier[]{kinTreeNode.entityData.getUniqueIdentifier()});
                final ArbilTableModel previewTableModel = customPreviewTable.getArbilTableModel();
                try {
                    final ArbilDataNode arbilDataNode = ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, new URI(((KinTreeNode) arbilNode).entityData.getEntityPath()));
                    if (!(previewTableModel.getArbilDataNodeCount() == 1 && previewTableModel.containsArbilDataNode(arbilDataNode))) {
                        previewTableModel.removeAllArbilDataNodeRows();
                        previewTableModel.addSingleArbilDataNode(arbilDataNode);
                    }
                } catch (URISyntaxException urise) {
                    GuiHelper.linorgBugCatcher.logError(urise);
                }
            }
        }
    }
}
