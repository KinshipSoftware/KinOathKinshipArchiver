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
package nl.mpi.kinnate.ui.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.ResourceBundle;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.kindocument.EntityDocument;
import nl.mpi.kinnate.kindocument.ImportTranslator;
import nl.mpi.kinnate.ui.KinDiagramPanel;

/**
 * Created on : Feb 6, 2013, 3:16:43 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class TableMenu extends JPopupMenu implements ActionListener {
    private static final ResourceBundle menus = ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Menus");

    final private SessionStorage sessionStorage;
    final private MessageDialogHandler dialogHandler;
    private final ArbilField[] arbilFields;
    private final ArbilDataNode[] arbilDataNodes;
    private final EntityCollection entityCollection;
    private final KinDiagramPanel kinDiagramPanel;
    final String deleteCommand = "delete";
    final String addCustomCommand = "addcustom";
    final String addCommand = "addknown";

    public TableMenu(SessionStorage sessionStorage, MessageDialogHandler dialogHandler, EntityCollection entityCollection, KinDiagramPanel kinDiagramPanel, ArbilDataNode[] arbilDataNodes, ArbilField[] arbilFields) {
//        System.out.println("cellContents:" + cellContents.getClass());
        this.sessionStorage = sessionStorage;
        this.dialogHandler = dialogHandler;
        this.entityCollection = entityCollection;
        this.kinDiagramPanel = kinDiagramPanel;
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
        JMenu addMenu = new JMenu(menus.getString("ADD"));
        try {
            for (String fieldName : entityCollection.getAllFieldNames()) {
                if (addedCounter > 20) {
                    this.add(addMenu);
                    addMenu = new JMenu(java.text.MessageFormat.format(menus.getString("ADD ({0})"), new Object[] {fieldName.substring(0, 1)}));
                    addedCounter = 0;
                }
                final JMenuItem addMenuItem = new JMenuItem(fieldName);
                addMenuItem.setActionCommand(addCommand + fieldName);
                addMenuItem.addActionListener(this);
                addMenu.add(addMenuItem);
                addedCounter++;
            }
        } catch (EntityServiceException exception) {
            dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Get All Field Names");
        }
        this.add(addMenu);
        final JMenuItem addCustomMenuItem = new JMenuItem(menus.getString("ADD <CUSTOM FIELD>"));
        addCustomMenuItem.setActionCommand(addCustomCommand);
        addCustomMenuItem.addActionListener(this);
        this.add(addCustomMenuItem);
    }

    private JMenuItem getDeleteMenuItem(ArbilField[] arbilFields) {
        String deleteFieldLabel;
        if (arbilFields.length == 1) {
            deleteFieldLabel = java.text.MessageFormat.format(menus.getString("DELETE FIELD \"{0}\""), new Object[] {arbilFields[0].getTranslateFieldName()});
        } else {
            deleteFieldLabel = java.text.MessageFormat.format(menus.getString("DELETE  {0} FIELD(S)"), new Object[] {arbilFields.length});
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
        new Thread() {
            @Override
            public void run() {
                // node type will be used to determine the schema used from the diagram options
                kinDiagramPanel.showProgressBar();
                try {
                    if (actionCommand.equals(deleteCommand)) {
                        performDeleteFields();
                    } else if (actionCommand.startsWith(addCommand)) {
                        performAddField(actionCommand.substring(addCommand.length()));

                    } else if (actionCommand.equals(addCustomCommand)) {
                        String userInput = null;
                        do {
                            userInput = JOptionPane.showInputDialog(TableMenu.this, menus.getString("ONLY ALPHANUMERIC CHARACTERS ARE RECOMMENDED"), menus.getString("ADD CUSTOM FIELD"), JOptionPane.PLAIN_MESSAGE);
                        } while (userInput != null && (userInput.length() < 1 /*|| userInput.matches(".*[: \t].*") */));
                        if (userInput != null) {
                            performAddField(userInput);
                        }
                    }
                } catch (ImportException exception) {
                    BugCatcherManager.getBugCatcher().logError(exception);
                    dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Add/Remove Fields");
                }
                try {
                    saveAllDocuments();
                } catch (ImportException exception) {
                    BugCatcherManager.getBugCatcher().logError(exception);
                    dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Add/Remove Fields");
                }
                kinDiagramPanel.clearProgressBar();
            }
        }.start();
    }

    private void performDeleteFields() throws ImportException {
        for (ArbilField arbilField : arbilFields) {
            EntityDocument entityDocument = getEntityDocument(arbilField.getParentDataNode());
            // the EntityDocument does not handle nested nodes at this stage and users cannot create nested nodes with the possible exception of some imports, it would be best to remove the sub nodes capability from the import since this is not supported elsewhere in the application.
            String tagName = arbilField.getFullXmlPath().replaceAll("\\(\\d*?\\)$", "").substring(".Kinnate.CustomData.".length());
            entityDocument.removeValue(tagName, arbilField.getFieldValue());
        }
    }

    private void performAddField(String validatedFieldName) throws ImportException {
        for (ArbilDataNode arbilDataNode : arbilDataNodes) {
            EntityDocument entityDocument = getEntityDocument(arbilDataNode);
            entityDocument.insertValue(validatedFieldName, "");
        }
    }
    HashMap<ArbilDataNode, EntityDocument> documentMap = new HashMap<ArbilDataNode, EntityDocument>();

    private EntityDocument getEntityDocument(ArbilDataNode arbilDataNode) throws ImportException {
        if (!documentMap.containsKey(arbilDataNode)) {
            documentMap.put(arbilDataNode, new EntityDocument(arbilDataNode.getURI(), new ImportTranslator(true), sessionStorage, entityCollection.getProjectRecord()));
        }
        return documentMap.get(arbilDataNode);
    }

    private void saveAllDocuments() throws ImportException {
        try {
            for (EntityDocument entityDocument : documentMap.values()) {
                entityDocument.saveDocument();
                entityCollection.updateDatabase(entityDocument.getFile().toURI(), entityDocument.getUniqueIdentifier());
            }
        } catch (EntityServiceException exception) {
            dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Update Database");
        }
        for (ArbilDataNode arbilDataNode : documentMap.keySet()) {
            arbilDataNode.reloadNode();
        }
    }
}
