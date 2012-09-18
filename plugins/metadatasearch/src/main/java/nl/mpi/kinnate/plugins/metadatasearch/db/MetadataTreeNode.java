package nl.mpi.kinnate.plugins.metadatasearch.db;

import java.net.URI;
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
    private ArbilDataNode arbilDataNode = null;
    private ArbilDataNode arbilDomDataNode = null;
    private String imdiApiPath = null;
    private String labelString = null;

    public TreeNode getChildAt(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getChildCount() {
        // todo: metadta nodes will be able to be expanded to show the entire arbil node and children
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
            // this version seems to work for 6.7.1 version of basex
            String imdiApiPathPreNumber = fileUriPath.replaceAll("/\"[^\"]*\":", ".").replaceAll("\\[1]", "");//.replaceAll("\\[", "(").replaceAll("\\]", ")");;
            // this version seems to work for 7.3.1 version of basex
            imdiApiPathPreNumber = imdiApiPathPreNumber.replaceAll("/Q[^\\}]*\\}", ".").replaceAll("\\[1]", "");
//            String imdiApiPath = "";
//            for (String pathPart : imdiApiPathPreNumber.split("\\[")) {
//                String[] innerPathParts = pathPart.split("\\]");
//                if (innerPathParts.length == 1) {
//                    imdiApiPath = imdiApiPath + innerPathParts[0];
//                } else if (innerPathParts.length == 2) {
//                    imdiApiPath = imdiApiPath + "(" + (Integer.decode(innerPathParts[0]) - 1) + ")" + innerPathParts[1];
//                } else {
//                    throw new UnsupportedOperationException();
//                }
//            }
            String imdiApiPathReturnValue = imdiApiPathPreNumber.replace("[", "(").replace("]", ")");
            return imdiApiPathReturnValue;
//                uriList.add(new URI(fileUri.getScheme(), fileUri.getUserInfo(), fileUri.getHost(), fileUri.getPort(), fileUri.getPath(), fileUri.getQuery(), imdiApiPath));
        }
//            }
    }

    @Override
    public String toString() {
        if (labelString != null) {
            return labelString;
        } else {
            return "            ";
        }
    }

    public ImageIcon getIcon() {
        return getArbilNode().getIcon();
    }

    public ArbilDataNode getArbilNode() {
        boolean nodeNeedsUpdating = false;
//        System.out.println("getArbilNode-fileUri: " + fileUri);
        if (arbilDomDataNode == null) {
            URI fileDbNameUri = fileUri;
            if (fileUri.getScheme() == null) {
                final String[] splitPath = fileUri.getPath().split("/", 2);
                String databaseName = splitPath[0];
                String uriPath = splitPath[1];
                URI projectUri = arbilDatabase.getDatabaseProjectDirectory(databaseName).toURI();
                fileDbNameUri = projectUri.resolve(uriPath);
            }
            arbilDomDataNode = (ArbilDataNode) arbilDataNodeLoader.getPluginArbilDataNode(MetadataTreeNode.this, fileDbNameUri);
//            arbilDataNode.registerContainer(this);
            nodeNeedsUpdating = true;
        }
        labelString = arbilDomDataNode.toString();
        arbilDataNode = arbilDomDataNode;
        if (!arbilDomDataNode.isLoading()) { // && imdiApiPath == null
            imdiApiPath = getImdiApiPath();
            boolean matchingChildFound = true;
            while (matchingChildFound) {
                matchingChildFound = false;
                String lastMatchedImdiApiPath = "";
                for (ArbilDataNode arbilChildDataNode : arbilDataNode.getChildArray()) {
                    String fragmentString = arbilChildDataNode.getURI().getFragment();
                    String currentImdiApiPath;
                    if (fragmentString != null) {
                        currentImdiApiPath = fragmentString.replace("(1)", "");
                    } else {
                        currentImdiApiPath = "";
                    }
                    if (imdiApiPath.startsWith(currentImdiApiPath) && lastMatchedImdiApiPath.length() < currentImdiApiPath.length()) {
                        lastMatchedImdiApiPath = currentImdiApiPath;
                        arbilDataNode = arbilChildDataNode;
                        labelString = labelString + " / " + arbilChildDataNode.toString();
                        matchingChildFound = true;
                    }
                }
            }
            System.out.println("labelString: " + labelString);
        }
        if (nodeNeedsUpdating) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // todo: should we really be triggering this here???? should not the change to the root node have done this
                    defaultTreeModel.nodeChanged(MetadataTreeNode.this);
//                    defaultTreeModel.nodeStructureChanged(MetadataTreeNode.this);
                }
            });
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
        // update the label string and the target data node
        //System.out.println("dataNodeIconCleared");
        getArbilNode();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //System.out.println("run");
                defaultTreeModel.nodeChanged(MetadataTreeNode.this);
//                if (parentDbTreeNode != null) {
//                    // update the parent structure so that any sorting of the child nodes can be done
//                    defaultTreeModel.nodeStructureChanged(parentDbTreeNode);
//                    //System.out.println("parent update");
//                } else {
//                    defaultTreeModel.nodeStructureChanged(MetadataTreeNode.this);
//                    //System.out.println("self update");
//                }
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
