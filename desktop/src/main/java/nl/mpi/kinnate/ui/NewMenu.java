package nl.mpi.kinnate.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 *  Document   : NewMenu
 *  Created on : Sep 27, 2011, 10:33:29 AM
 *  Author     : Peter Withers
 */
public class NewMenu extends JMenu implements ActionListener {

    enum DocumentTypes {

        KinTypeString,
        Query,
        ArchiveLinker,
        EntitySearch,
        CustomQuery
    }

    public void addMenuItems() {
        for (DocumentTypes documentType : DocumentTypes.values()) {
            JMenuItem menuItem = new JMenuItem(documentType.name());
            menuItem.setActionCommand(documentType.name());
            menuItem.addActionListener(this);
            this.add(menuItem);
        }
    }

    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
