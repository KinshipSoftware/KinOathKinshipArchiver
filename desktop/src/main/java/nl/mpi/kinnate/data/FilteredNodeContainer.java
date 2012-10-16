package nl.mpi.kinnate.data;

import javax.swing.ImageIcon;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ContainerNode;

/**
 * Document : FilteredNodeContainer
 * Created on : Apr 26, 2012, 4:39:32 PM
 * Author : Peter Withers
 */
public class FilteredNodeContainer extends ContainerNode {

    public FilteredNodeContainer(String labelString, ImageIcon imageIcon, KinTreeFilteredNode[] childNodes) {
        super(labelString, imageIcon, childNodes);
    }

    @Override
    public ArbilNode[] getChildArray() {
        final ArbilNode[] childArray = super.getChildArray();
        new Thread() {

            @Override
            public void run() {
                for (ArbilNode childNode : childArray) {
                    ((KinTreeFilteredNode) childNode).loadEntityIfNotLoaded();
                }
            }
        }.start();
        return childArray;
    }
}
