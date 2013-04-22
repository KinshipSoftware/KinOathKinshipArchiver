/**
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.ui.menu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;

/**
 * Document : DocumentNewMenu Created on : Sep 27, 2011, 10:33:29 AM
 *
 * @author Peter Withers
 */
public class DocumentNewMenu extends JMenu implements ActionListener {

    private final AbstractDiagramManager diagramWindowManager;
    private final Component parentComponent;
    private final MessageDialogHandler dialogHandler;

    public enum DocumentType {

        Simple("Standard Diagram (database driven)"),
        Freeform("Freeform Diagram (transient)"),
        KinTerms("Kin Terms Diagram"),
        Query("Query Diagram"),
        //EntitySearch("Entity Search"),
        ArchiveLinker("Archive Data Linker");//,
//        CustomQuery("Custom Data Formats");
        private String displayName;

        private DocumentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public DocumentNewMenu(AbstractDiagramManager diagramWindowManager, Component parentComponent, MessageDialogHandler dialogHandler) {
        this.diagramWindowManager = diagramWindowManager;
        this.parentComponent = parentComponent;
        this.dialogHandler = dialogHandler;
        for (DocumentType documentType : DocumentType.values()) {
            JMenuItem menuItem = new JMenuItem(documentType.getDisplayName());
            menuItem.setActionCommand(documentType.name());
            menuItem.addActionListener(this);
            this.add(menuItem);
        }
    }

    public void actionPerformed(ActionEvent e) {
        DocumentType documentType = DocumentType.valueOf(e.getActionCommand());
        final Dimension parentSize = parentComponent.getSize();
        final Point parentLocation = parentComponent.getLocation();
        int offset = 10;
        final Rectangle windowRectangle = new Rectangle(parentLocation.x + offset, parentLocation.y + offset, parentSize.width - offset, parentSize.height - offset);
        try {
            diagramWindowManager.newDiagram(documentType, windowRectangle);
        } catch (EntityServiceException entityServiceException) {
            dialogHandler.addMessageDialogToQueue("Failed to open diagram: " + entityServiceException.getMessage(), "Open Diagram Error");
        }
    }
}
