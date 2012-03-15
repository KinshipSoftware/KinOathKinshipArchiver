package nl.mpi.kinnate.ui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;

/**
 *  Document   : DocumentNewMenu
 *  Created on : Sep 27, 2011, 10:33:29 AM
 *  Author     : Peter Withers
 */
public class DocumentNewMenu extends JMenu implements ActionListener {

    AbstractDiagramManager diagramWindowManager;

    public enum DocumentType {

        Simple("Standard Diagram (database driven)"), // todo: should this be database diagram, data driven
        Freeform("Freeform Diagram"),
        KinTerms("Kin Terms Diagram"),
        Query("Query Diagram"),
        EntitySearch("Entity Search"),
        ArchiveLinker("Archive Data Linker"),
        CustomQuery("Custom Data Formats");
        private String displayName;

        private DocumentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public DocumentNewMenu(AbstractDiagramManager diagramWindowManager) {
        this.diagramWindowManager = diagramWindowManager;
        for (DocumentType documentType : DocumentType.values()) {
            JMenuItem menuItem = new JMenuItem(documentType.getDisplayName());
            menuItem.setActionCommand(documentType.name());
            menuItem.addActionListener(this);
            this.add(menuItem);
        }
    }

    public void actionPerformed(ActionEvent e) {
        DocumentType documentType = DocumentType.valueOf(e.getActionCommand());
        diagramWindowManager.newDiagram(documentType);
    }
}
