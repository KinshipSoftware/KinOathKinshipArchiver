package nl.mpi.kinnate.plugins.metadatasearch.db;

import java.net.URI;
import java.net.URISyntaxException;
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
    @XmlElement(name = "FileUriPath")
    private String fileUriPathArray[] = null;
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

    public URI[] getUri() throws URISyntaxException {
        ArrayList<URI> uriList = new ArrayList<URI>();
        if (fileUriPathArray != null) {
            for (String fileUriPath : fileUriPathArray) {
                if (!fileUriPath.equals("/")) {
                    String imdiApiPathPreNumber = fileUriPath.replaceAll("/\"[^\"]*\":", ".").replaceAll("\\[1]", "");//.replaceAll("\\[", "(").replaceAll("\\]", ")");;
                    String imdiApiPath = "";
                    for (String pathPart : imdiApiPathPreNumber.split("\\[")) {
                        String[] innerPathParts = pathPart.split("\\]");
                        if (innerPathParts.length == 1) {
                            imdiApiPath = imdiApiPath + innerPathParts[0];
                        } else if (innerPathParts.length == 2) {
                            imdiApiPath = imdiApiPath + "(" + (Integer.decode(innerPathParts[0]) - 1) + ")" + innerPathParts[1];
                        } else {
                            throw new UnsupportedOperationException();
                        }
                    }
                    uriList.add(new URI(fileUri.getScheme(), fileUri.getUserInfo(), fileUri.getHost(), fileUri.getPort(), fileUri.getPath(), fileUri.getQuery(), imdiApiPath));
                }
            }
            if (uriList.isEmpty()) {
                uriList.add(fileUri);
            }
        }

        return uriList.toArray(
                new URI[0]);
    }
}
