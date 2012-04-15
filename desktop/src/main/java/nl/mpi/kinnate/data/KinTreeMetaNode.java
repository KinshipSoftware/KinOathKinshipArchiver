package nl.mpi.kinnate.data;

import java.util.Arrays;
import java.util.Vector;
import javax.swing.ImageIcon;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.kinnate.svg.SymbolGraphic;

/**
 * Document : KinTreeMetaNode
 * Created on : Mar 28, 2012, 1:04:22 PM
 * Author : Peter Withers
 */
@Deprecated
public class KinTreeMetaNode extends ArbilNode implements Comparable {

    private ArbilNode[] childNodes;
    private String labelString;
    protected SymbolGraphic symbolGraphic;

    public KinTreeMetaNode(ArbilNode[] childNodes, String labelString, int notused, SymbolGraphic symbolGraphic) {
        this.childNodes = childNodes;
        this.labelString = labelString;
        this.symbolGraphic = symbolGraphic;
    }

    public int compareTo(Object o) {
        return labelString.compareTo(o.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KinTreeMetaNode other = (KinTreeMetaNode) obj;
        return this.hashCode() == other.hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 17;
//        hash = 19 * hash + Arrays.deepHashCode(this.childNodes);
        hash = 19 * hash + (this.labelString != null ? this.labelString.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return labelString + " (" + childNodes.length + ")";
    }

    @Override
    public ArbilDataNode[] getAllChildren() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getAllChildren(Vector<ArbilDataNode> allChildren) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ArbilNode[] getChildArray() {
        return childNodes;
    }

    @Override
    public int getChildCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImageIcon getIcon() {
        return null;  //symbolGraphic.getSymbolGraphic(new String[]{"error"}, false);
    }

    @Override
    public boolean hasCatalogue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasHistory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasLocalResource() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasResource() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isArchivableFile() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCatalogue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isChildNode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCmdiMetaDataNode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCorpus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDataLoaded() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDirectory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isEditable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isEmptyMetaNode() {
//        return true;
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isFavorite() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isLoading() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isLocal() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isMetaDataNode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isResourceSet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSession() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
