package nl.mpi.kinnate.ui;

import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.ui.ArbilTree;

/**
 *  Document   : KinTree
 *  Created on : Aug 25, 2011, 11:44:11 AM
 *  Author     : Peter Withers
 */
public class KinTree extends ArbilTree {

    public KinTree() {
        super.init();
    }

    @Override
    protected void putSelectionIntoPreviewTable() {
        ArbilNode arbilNode = getLeadSelectionNode();
        if (arbilNode instanceof ArbilDataNode) {
            // todo: clear the graph selection
            super.putSelectionIntoPreviewTable();
        } else {
            // todo: set the graph selection
        }
    }
}
