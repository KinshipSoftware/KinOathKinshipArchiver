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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import nl.mpi.arbil.data.ArbilDataNode;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface TableController {

    /**
     * Use when cell value is field place holder, meaning that the node does not
     * contain the selected field and may not be able to.
     */
    void addFieldFromPlaceholder(final ArbilTable table, final int selectedFieldIndex, ArbilFieldPlaceHolder placeholder);

    void checkPopup(MouseEvent evt, boolean checkSelection);

    void copySelectedCellToColumn(ArbilTable table);

    /**
     * Attempts to delete the field represented by the specified column name from all nodes visible as rows in the table.
     * A check whether deletion of the field is allowed is done for each row/node; if one fails, a message gets presented and the entire
     * operation gets aborted
     *
     * @param table table to get nodes to delete from
     * @param columnName name of the column/field to remove
     * @return whether deletion was carried out
     */
    boolean deleteColumnFieldFromAllNodes(ArbilTable table, String columnName);

    void deleteNodes(ArbilTable table);

    /**
     * Deletes the fields selected in the provided table from their parent nodes
     *
     * @param table table to get selection from
     * @return whether deletion was carried out
     */
    boolean deleteSelectedFields(ArbilTable table);

    MouseListener getTableHeaderMouseListener();

    MouseListener getTableMouseListener();

    void highlightMatchingRows(ArbilTable table);

    void initKeyMapping(ArbilTable table);

    void jumpToSelectionInTree(ArbilTable table);

    void openNodesInNewTable(ArbilDataNode[] nodes, String fieldName, ArbilDataNode registeredOwner);

    void saveCurrentColumnView(ArbilTable table);

    void showColumnViewsEditor(ArbilTable table);

    void showContextForSelectedNodes(ArbilTable table);

    void showRowChildData(ArbilTableModel tableModel);

    void startLongFieldEditorForSelectedFields(ArbilTable table);

    void viewSelectedTableRows(ArbilTable table);

    void copySelectedTableRowsToClipBoard(ArbilTable table);

    void pasteIntoSelectedTableRowsFromClipBoard(ArbilTable table);
    
}
