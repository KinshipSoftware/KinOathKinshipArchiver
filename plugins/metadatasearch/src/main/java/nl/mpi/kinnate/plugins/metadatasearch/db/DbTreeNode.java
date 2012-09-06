package nl.mpi.kinnate.plugins.metadatasearch.db;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    @XmlElement(name = "FileUri")
    private URI fileUri = null;
    private DbTreeNode parentDbTreeNode;

    public DbTreeNode[] getChildTreeNode() {
        return childTreeNode;
    }

    public void setParentDbTreeNode(DbTreeNode parentDbTreeNode) {
        this.parentDbTreeNode = parentDbTreeNode;
    }

    public TreeNode getChildAt(int i) {
        childTreeNode[i].setParentDbTreeNode(this);
        return childTreeNode[i];
    }

    public int getChildCount() {
        return childTreeNode.length;
    }

    public TreeNode getParent() {
        return parentDbTreeNode;
    }

    public int getIndex(TreeNode tn) {
        return Arrays.binarySearch(childTreeNode, tn);
    }

    public boolean getAllowsChildren() {
        return childTreeNode != null && childTreeNode.length > 0;
    }

    public boolean isLeaf() {
        return childTreeNode == null || childTreeNode.length == 0;
    }

    public Enumeration children() {
        ArrayList<DbTreeNode> childList = new ArrayList<DbTreeNode>();
        for (DbTreeNode childNode : childTreeNode) {
            childNode.setParentDbTreeNode(this);
            childList.add(childNode);
        }
        return Collections.enumeration(childList);
    }

    @Override
    public String toString() {
        if (displayString != null) {
            return displayString;
        } else if (fileUri != null) {
            return fileUri.toString();
        }
        return "            ";
    }

    public URI getUri() {
        return fileUri;
    }
}
