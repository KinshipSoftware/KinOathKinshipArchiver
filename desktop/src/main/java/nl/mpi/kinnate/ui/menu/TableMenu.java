/*
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.kinnate.ui.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.kindocument.EntityDocument;
import nl.mpi.kinnate.kindocument.ImportTranslator;

/**
 * Created on : Feb 6, 2013, 3:16:43 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class TableMenu extends JPopupMenu implements ActionListener {

    final private SessionStorage sessionStorage;
    final private MessageDialogHandler dialogHandler;
    private final ArbilField[] arbilFields;
    private final ArbilDataNode[] arbilDataNodes;
    private final EntityCollection entityCollection;
    final String deleteCommand = "delete";
    final String addCustomCommand = "addcustom";
    final String addCommand = "addknown";

    public TableMenu(SessionStorage sessionStorage, MessageDialogHandler dialogHandler, EntityCollection entityCollection, ArbilDataNode[] arbilDataNodes, ArbilField[] arbilFields) {
//        System.out.println("cellContents:" + cellContents.getClass());
        this.sessionStorage = sessionStorage;
        this.dialogHandler = dialogHandler;
        this.entityCollection = entityCollection;
        this.arbilDataNodes = arbilDataNodes;
        this.arbilFields = arbilFields;
        if (arbilDataNodes != null && arbilDataNodes.length > 0) {
            getAddMenu();
        }
        if (arbilFields != null && arbilFields.length > 0) {
            this.add(getDeleteMenuItem(arbilFields));
        }
    }

    private void getAddMenu() {
        int addedCounter = 0;
        JMenu addMenu = new JMenu("Add");
        for (String fieldName : entityCollection.getAllFieldNames()) {
            if (addedCounter > 20) {
                this.add(addMenu);
                addMenu = new JMenu("Add (" + fieldName.substring(0, 1) + ")");
                addedCounter = 0;
            }
            final JMenuItem addMenuItem = new JMenuItem(fieldName);
            addMenuItem.setActionCommand(addCommand + fieldName);
            addMenuItem.addActionListener(this);
            addMenu.add(addMenuItem);
            addedCounter++;
        }
        this.add(addMenu);
        final JMenuItem addCustomMenuItem = new JMenuItem("Add <custom field>");
        addCustomMenuItem.setActionCommand(addCustomCommand);
        addCustomMenuItem.addActionListener(this);
        this.add(addCustomMenuItem);
    }

    private JMenuItem getDeleteMenuItem(ArbilField[] arbilFields) {
        String deleteFieldLabel;
        if (arbilFields.length == 1) {
            deleteFieldLabel = "Delete Field \"" + arbilFields[0].getTranslateFieldName() + "\"";
        } else {
            deleteFieldLabel = "Delete  " + arbilFields.length + " Field(s)";
        }
        final JMenuItem deleteMenuItem = new JMenuItem(deleteFieldLabel);
        deleteMenuItem.setActionCommand(deleteCommand);
        deleteMenuItem.addActionListener(this);
        return deleteMenuItem;
    }

    @Override
    public void show(Component cmpnt, int i, int i1) {
        if ((arbilDataNodes != null && arbilDataNodes.length > 0) || (arbilFields != null && arbilFields.length > 0)) {
            // do not show when there are no menu items 
            super.show(cmpnt, i, i1);
        }
    }

    public void actionPerformed(ActionEvent ae) {
        final String actionCommand = ae.getActionCommand();
        try {
            if (actionCommand.equals(deleteCommand)) {
//                EntityDocument entityDocument = new EntityDocument(arbilDataNode.getURI(), new ImportTranslator(true), sessionStorage);
////                entityDocument.
//
//                entityDocument.saveDocument();
            } else if (actionCommand.startsWith(addCommand)) {
                for (ArbilDataNode arbilDataNode : arbilDataNodes) {
                    EntityDocument entityDocument = new EntityDocument(arbilDataNode.getURI(), new ImportTranslator(true), sessionStorage);
                    entityDocument.insertValue(actionCommand.substring(addCommand.length()), "");
                    entityDocument.saveDocument();
                    arbilDataNode.reloadNode();
                }
            } else if (actionCommand.equals(addCustomCommand)) {
//                String userInput = dialogHandler.
            }
        } catch (ImportException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Add/Remove Fields");
        }
    }
}
