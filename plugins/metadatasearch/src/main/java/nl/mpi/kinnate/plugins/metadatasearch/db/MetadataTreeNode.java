package nl.mpi.kinnate.plugins.metadatasearch.db;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.tree.TreeNode;
import javax.xml.bind.annotation.XmlElement;
import nl.mpi.arbil.data.ArbilDataNode;

/**
 * Document : MetadataTreeNode <br> Created on Sep 6, 2012, 3:52:56 PM <br>
 *
 * @author Peter Withers <br>
 */
public class MetadataTreeNode extends AbstractDbTreeNode {

    @XmlElement(name = "FileUri")
    private URI fileUri = null;
    @XmlElement(name = "FileUriPath")
    private String fileUriPath = null;

    public TreeNode getChildAt(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getChildCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TreeNode getParent() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getIndex(TreeNode tn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean getAllowsChildren() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isLeaf() {
        return true;
    }

    public Enumeration children() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private URI[] getUri() throws URISyntaxException {
        ArrayList<URI> uriList = new ArrayList<URI>();
        if (fileUriPath != null) {
//            for (String fileUriPath : fileUriPathArray) {
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
//            }
            if (uriList.isEmpty()) {
                uriList.add(fileUri);
            }
        }
        return uriList.toArray(new URI[0]);
    }

    @Override
    public String toString() {
        if (fileUri != null) {
            return fileUri.toString();
        }
        return "            ";
    }

    public ArbilDataNode getArbilNode() {
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
        return arbilDataNodeLoader.getArbilDataNode(MetadataTreeNode.this, fileUri);
    }
}
