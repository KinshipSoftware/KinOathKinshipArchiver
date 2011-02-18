package nl.mpi.kinnate;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *  Document   : GraphPanelContextMenu
 *  Created on : Feb 18, 2011, 11:51:00 AM
 *  Author     : Peter Withers
 */
public class GraphPanelContextMenu extends JPopupMenu {

    GraphPanel graphPanel;
    GraphPanelSize graphPanelSize;

    public GraphPanelContextMenu(GraphPanel graphPanelLocal, GraphPanelSize graphPanelSizeLocal) {
        graphPanel = graphPanelLocal;
        graphPanelSize = graphPanelSizeLocal;
        JMenu diagramSizeMenuItem = new JMenu("Diagram Size");
        for (String currentString : graphPanelSize.getPreferredSizes()) {
            JMenuItem currentMenuItem = new JMenuItem(currentString);
            currentMenuItem.setActionCommand(currentString);
            currentMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setGraphPanelSize(evt.getActionCommand());
                }
            });
            diagramSizeMenuItem.add(currentMenuItem);
        }
        this.add(diagramSizeMenuItem);
    }

    private void setGraphPanelSize(String sizeString) {
        graphPanelSize.setSize(sizeString);
        graphPanel.drawNodes();
    }
}
