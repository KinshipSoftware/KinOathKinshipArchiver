package nl.mpi.kinnate.plugins.metadatasearch.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import javax.swing.tree.TreeNode;
import javax.xml.bind.annotation.XmlElement;

/**
 * Document : DbTreeNode <br> Created on Aug 23, 2012, 4:42:23 PM <br>
 *
 * @author Peter Withers <br>
 */
public class DbTreeNode extends AbstractDbTreeNode {

    @XmlElement(name = "TreeNode")
    private DbTreeNode[] childTreeNode = new DbTreeNode[0];
    @XmlElement(name = "MetadataTreeNode")
    private MetadataTreeNode[] childMetadataTreeNode = new MetadataTreeNode[0];
    @XmlElement(name = "DisplayString")
    private String displayString = null;
    private int maxChildrenToShow = 100;

    public DbTreeNode() {
    }

    public DbTreeNode(String displayString) {
        this.displayString = displayString;
    }

//    public DbTreeNode[] getChildTreeNode() {
//        return childTreeNode;
//    }
    public TreeNode getChildAt(int i) {
        final AbstractDbTreeNode selectedChild = getChildList().get(i);
        selectedChild.setParentDbTreeNode(this, defaultTreeModel, arbilDataNodeLoader);
        return selectedChild;
    }

    public int getChildCount() {
        if (canShowAllChildren()) {
            return childTreeNode.length + childMetadataTreeNode.length;
        } else {
            return 1;
        }
    }

    private boolean canShowAllChildren() {
        return childTreeNode.length + childMetadataTreeNode.length < maxChildrenToShow;
    }

    public TreeNode getParent() {
        return parentDbTreeNode;
    }

    public int getIndex(TreeNode tn) {
        return getChildList().indexOf(tn);
    }

    public boolean getAllowsChildren() {
        return getChildCount() > 0;
    }

    public boolean isLeaf() {
        return getChildCount() == 0;
    }

    private ArrayList<AbstractDbTreeNode> getChildList() {
        ArrayList<AbstractDbTreeNode> childList = new ArrayList<AbstractDbTreeNode>();
        if (!canShowAllChildren()) {
            // todo: this should be done in the db query not here
//            childList.clear();
            childList.add(new DbTreeNode("<more than " + maxChildrenToShow + " results, please add more facets>"));
        } else {
            for (DbTreeNode childNode : childTreeNode) {
                childNode.setParentDbTreeNode(this, defaultTreeModel, arbilDataNodeLoader);
                childList.add(childNode);
            }
            for (MetadataTreeNode childNode : childMetadataTreeNode) {
                // todo: sort the metadata child nodes
                childNode.setParentDbTreeNode(this, defaultTreeModel, arbilDataNodeLoader);
                childList.add(childNode);
            }
        }
        return childList;
    }

    public Enumeration children() {
        ArrayList<AbstractDbTreeNode> childList = getChildList();
        return Collections.enumeration(childList);
    }

    @Override
    public String toString() {
        if (displayString != null) {
            return displayString;
        } else {
            return "            ";
        }
    }
}
