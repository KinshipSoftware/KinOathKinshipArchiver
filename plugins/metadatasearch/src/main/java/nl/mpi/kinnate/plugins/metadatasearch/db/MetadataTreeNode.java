package nl.mpi.kinnate.plugins.metadatasearch.db;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import javax.xml.bind.annotation.XmlElement;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.data.ArbilNode;

/**
 * Document : MetadataTreeNode <br> Created on Sep 6, 2012, 3:52:56 PM <br>
 *
 * @author Peter Withers <br>
 */
public class MetadataTreeNode extends AbstractDbTreeNode implements ArbilDataNodeContainer {

    @XmlElement(name = "FileUri")
    private URI fileUri = null;
    @XmlElement(name = "FileUriPath")
    private String fileUriPath = null;
    ArbilDataNode arbilDataNode = null;
    private String imdiApiPath = null;

    public TreeNode getChildAt(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getChildCount() {
        return 0;
    }

    public TreeNode getParent() {
        return parentDbTreeNode;
    }

    public int getIndex(TreeNode tn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean getAllowsChildren() {
        return false;
    }

    public boolean isLeaf() {
        return true;
    }

    public Enumeration children() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private String getImdiApiPath() {
//            for (String fileUriPath : fileUriPathArray) {
        if (fileUriPath.equals("/")) {
            return "";
        } else {
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
            return imdiApiPath;
//                uriList.add(new URI(fileUri.getScheme(), fileUri.getUserInfo(), fileUri.getHost(), fileUri.getPort(), fileUri.getPath(), fileUri.getQuery(), imdiApiPath));
        }
//            }
    }

    @Override
    public String toString() {
        return getArbilNode().toString();
//        if (fileUri != null) {
//            return fileUri.toString();
//        }
//        return "            ";
    }

    public ImageIcon getIcon() {
        return getArbilNode().getIcon();
    }

    public ArbilDataNode getArbilNode() {
//        System.out.println("getArbilNode-fileUri: " + fileUri);
        if (arbilDataNode == null) {
            arbilDataNode = arbilDataNodeLoader.getArbilDataNode(MetadataTreeNode.this, fileUri);
//            arbilDataNode.registerContainer(this);
        }
        if (!arbilDataNode.isLoading() && imdiApiPath == null) {
            imdiApiPath = getImdiApiPath();
            boolean matchingChildFound = true;
            while (matchingChildFound) {
                matchingChildFound = false;
                for (ArbilDataNode arbilChildDataNode : arbilDataNode.getChildArray()) {
                    if (imdiApiPath.startsWith(arbilChildDataNode.getURI().getFragment())) {
                        arbilDataNode = arbilChildDataNode;
                        matchingChildFound = true;
                    }
                }
            }
        }
        return arbilDataNode;
//        try {
//            for (URI nodeUri : ((MetadataTreeNode) treePath.getLastPathComponent()).getUri()) {
//                System.out.println("nodeUri: " + nodeUri);
//                if (nodeUri != null) {
//                    arbilDataNodeList.add(arbilDataNodeLoader.getArbilDataNode(FacetedTreePanel.this, nodeUri));
//                }
//            }
//        } catch (URISyntaxException exception) {
//            dialogHandler.addMessageDialogToQueue("Failed to get the URI for the tree selection.", "Show Tree Selection");
//        }
    }

    public void dataNodeRemoved(ArbilNode dataNode) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void dataNodeIconCleared(ArbilNode dataNode) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                defaultTreeModel.nodeChanged(MetadataTreeNode.this);
                defaultTreeModel.nodeStructureChanged(MetadataTreeNode.this);
            }
        });
    }

    public void dataNodeChildAdded(ArbilNode destination, ArbilNode newChildNode) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isFullyLoadedNodeRequired() {
        return true;
    }
}
