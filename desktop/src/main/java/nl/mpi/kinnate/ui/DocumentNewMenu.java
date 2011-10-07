package nl.mpi.kinnate.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

/**
 *  Document   : DocumentNewMenu
 *  Created on : Sep 27, 2011, 10:33:29 AM
 *  Author     : Peter Withers
 */
public class DocumentNewMenu extends JMenu implements ActionListener {

    JTabbedPane targetPane;

    public enum DocumentType {

        Freeform("Freeform Diagram"),
        KinTerms("Kin Terms Diagram"),
        Query("Kin Type String Query"),
        ArchiveLinker("Archive Data Linker"),
        EntitySearch("Entity Search"),
        CustomQuery("Custom Metadata"),
        Simple("Standard");
        private String displayName;

        private DocumentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public DocumentNewMenu(JTabbedPane targetPane) {
        this.targetPane = targetPane;
        for (DocumentType documentType : DocumentType.values()) {
            JMenuItem menuItem = new JMenuItem(documentType.getDisplayName());
            menuItem.setActionCommand(documentType.name());
            menuItem.addActionListener(this);
            this.add(menuItem);
        }
    }

    public void actionPerformed(ActionEvent e) {
        DocumentType documentType = DocumentType.valueOf(e.getActionCommand());
        KinDiagramPanel egoSelectionTestPanel = new KinDiagramPanel(documentType);
        targetPane.add("Unsaved " + documentType.getDisplayName() + " Diagram", egoSelectionTestPanel);
        targetPane.setSelectedComponent(egoSelectionTestPanel);
    }
}
