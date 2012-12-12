/**
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.ui;

import java.util.ArrayList;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ContainerNode;
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
    private boolean updateGraphOnSelectionChange = false;

    public KinTree(KinDiagramPanel kinDiagramPanel, GraphPanel graphPanel, ContainerNode rootNode) {
        this.kinDiagramPanel = kinDiagramPanel;
        this.graphPanel = graphPanel;
        this.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(rootNode), true));
        this.setRootVisible(false);
        this.setShowsRootHandles(true);
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
//                if (kinTreeNode.getEntityData() != null) { // todo: why do we care if the entity data is null or not? I think kinTreeNode object has changed and the null test is no longer required
                identifierList.add(kinTreeNode.getUniqueIdentifier());
                graphPanel.metadataPanel.addEntityDataNode(kinDiagramPanel, ((KinTreeNode) arbilNode).getEntityData());
//                }
            }
        }
        // set the graph selection
        graphPanel.metadataPanel.updateEditorPane();
        if (updateGraphOnSelectionChange) {
            kinDiagramPanel.drawGraph(identifierList.toArray(new UniqueIdentifier[]{}));
        } else {
            graphPanel.setSelectedIds(identifierList.toArray(new UniqueIdentifier[]{}));
        }
    }

    public void setUpdateGraphOnSelectionChange(boolean updateGraphOnSelectionChange) {
        this.updateGraphOnSelectionChange = updateGraphOnSelectionChange;
    }

    public ArbilNode[] getSelectedNodeArray() {
        return selectedNodeArray;
    }
}
