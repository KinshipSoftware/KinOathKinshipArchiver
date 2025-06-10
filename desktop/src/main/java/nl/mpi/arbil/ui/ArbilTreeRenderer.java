/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.ui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;

/**
 * Document : ArbilTreeRenderer
 * Created on :
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTreeRenderer extends DefaultTreeCellRenderer {

    @Override
    synchronized public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultTreeCellRenderer returnComponent = this;
        returnComponent.setTextNonSelectionColor(tree.getForeground());
        returnComponent.setBackgroundNonSelectionColor(tree.getBackground());
//        returnComponent.setIcon(null); // setting the icon to null to avoid a null pointer later in the set icon where it tries to testthe icon width/hight
//        returnComponent.setText(null);
        String valueString = "";
        Icon nodeIcon = null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (node.getUserObject() instanceof ArbilNode) {
            ArbilNode arbilNode = (ArbilNode) node.getUserObject();
            // create the object with parameters so the jvm has a chance to reused objects in memory
//            returnComponent = new JLabel(arbilNode.toString(), arbilNode.getIcon(), JLabel.LEFT);
            valueString = (arbilNode.toString());
            nodeIcon = (arbilNode.getIcon());
            returnComponent.setHorizontalAlignment(JLabel.LEFT);
            if (arbilNode instanceof ArbilDataNode) {
                if (((ArbilDataNode) arbilNode).isContainerNode()) {
                    returnComponent.setTextNonSelectionColor(Color.DARK_GRAY);
                    valueString = ("<u>" + arbilNode.toString() + "</u>");
                }
                if (/* !sel && */((ArbilDataNode) arbilNode).getNeedsSaveToDisk(true)) {
                    returnComponent.setTextNonSelectionColor(Color.BLUE);
                }
                if (/* !sel && */((ArbilDataNode) arbilNode).hasSchemaError) {
                    returnComponent.setTextNonSelectionColor(Color.RED);
                }
            }
        } else if (node.getUserObject() instanceof JLabel) {
            // create the object with parameters so the jvm has a chance to reused objects in memory
            valueString = (((JLabel) node.getUserObject()).getText());
            nodeIcon = ((JLabel) node.getUserObject()).getIcon();
        }
//        returnComponent.setBackgroundSelectionColor(tree.getBackground().darker());
        returnComponent.setClosedIcon(nodeIcon);
        returnComponent.setOpenIcon(nodeIcon);
        returnComponent.setLeafIcon(nodeIcon);
        return super.getTreeCellRendererComponent(tree, "<html>" + valueString + "</html>", selected, expanded, leaf, row, hasFocus);
    }
}
