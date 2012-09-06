package nl.mpi.kinnate.plugins.metadatasearch.ui;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import nl.mpi.kinnate.plugins.metadatasearch.db.MetadataTreeNode;

/**
 * Document : SearchTreeCellRenderer <br> Created on Sep 6, 2012, 4:46:55 PM
 * <br>
 *
 * @author Peter Withers <br>
 */
public class SearchTreeCellRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        final DefaultTreeCellRenderer treeCellRendererComponent = (DefaultTreeCellRenderer) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (value instanceof MetadataTreeNode) {
            final ImageIcon nodeIcon = ((MetadataTreeNode) value).getIcon();
            treeCellRendererComponent.setIcon(nodeIcon);
        }
        return treeCellRendererComponent;
    }
}
