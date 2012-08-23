package nl.mpi.kinnate.plugins.metadatasearch.db;

import java.util.Arrays;
import java.util.Enumeration;
import javax.swing.tree.TreeNode;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Document : DbTreeNode <br> Created on Aug 23, 2012, 4:42:23 PM <br>
 *
 * @author Peter Withers <br>
 */
@XmlRootElement(name = "MetadataFileType")
public class DbTreeNode implements TreeNode {

    @XmlElement(name = "TreeNode")
    private DbTreeNode[] childTreeNode = new DbTreeNode[0];
    @XmlElement(name = "DisplayString")
    private String displayString = null;

    public DbTreeNode[] getChildTreeNode() {
        return childTreeNode;
    }

    public TreeNode getChildAt(int i) {
        return childTreeNode[i];
    }

    public int getChildCount() {
        return childTreeNode.length;
    }

    public TreeNode getParent() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getIndex(TreeNode tn) {
        return Arrays.binarySearch(childTreeNode, tn);
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public boolean isLeaf() {
        return false;
    }

    public Enumeration children() {
        throw new UnsupportedOperationException("Not supported yet.");
//        return Arrays.asList(childTreeNode);
    }

    @Override
    public String toString() {
        return displayString;
    }
}
