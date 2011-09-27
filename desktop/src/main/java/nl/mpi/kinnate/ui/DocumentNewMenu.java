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

        KinTypeString,
        KinTerms,
        Query,
        ArchiveLinker,
        EntitySearch,
        CustomQuery,
        Simple
    }

    public DocumentNewMenu(JTabbedPane targetPane) {
        this.targetPane = targetPane;
        for (DocumentType documentType : DocumentType.values()) {
            JMenuItem menuItem = new JMenuItem(documentType.name());
            menuItem.setActionCommand(documentType.name());
            menuItem.addActionListener(this);
            this.add(menuItem);
        }
    }

    public void actionPerformed(ActionEvent e) {
        KinDiagramPanel egoSelectionTestPanel = new KinDiagramPanel(DocumentType.valueOf(e.getActionCommand()));
        targetPane.add("Unsaved Diagram", egoSelectionTestPanel);
        targetPane.setSelectedComponent(egoSelectionTestPanel);
    }
}
