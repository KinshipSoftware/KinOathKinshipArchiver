package nl.mpi.kinnate.ui.menu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;

/**
 * Document : DocumentNewMenu Created on : Sep 27, 2011, 10:33:29 AM
 *
 * @author Peter Withers
 */
public class DocumentNewMenu extends JMenu implements ActionListener {

    private final AbstractDiagramManager diagramWindowManager;
    private final Component parentComponent;

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

    public DocumentNewMenu(AbstractDiagramManager diagramWindowManager, Component parentComponent) {
        this.diagramWindowManager = diagramWindowManager;
        this.parentComponent = parentComponent;
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
        diagramWindowManager.newDiagram(documentType, windowRectangle);
    }
}
