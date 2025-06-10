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
import java.util.ResourceBundle;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import nl.mpi.arbil.util.BugCatcherManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table for editing ArbilFieldViews
 *
 * Document   : ArbilFieldViewTable
 * Used to edit the field view of an arbil table
 * Created on : Jan 5, 2009, 12:40:37 PM
 * @author Peter.Withers@mpi.nl
 * @see ArbilFieldView
 */
public class ArbilFieldViewTable extends JTable {
    private final static Logger logger = LoggerFactory.getLogger(ArbilFieldViewTable.class);
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");

    public ArbilFieldViewTable(ArbilTableModel imdiTableModel) {
	TableModel tableModel = new ArbilFieldViewTableModel(imdiTableModel);
	setModel(tableModel);
	this.addMouseListener(new java.awt.event.MouseAdapter() {

	    @Override
	    public void mousePressed(MouseEvent evt) {
		logger.debug("mousePressed");
		checkPopup(evt);
	    }

	    @Override
	    public void mouseReleased(MouseEvent evt) {
		logger.debug("mouseReleased");
		checkPopup(evt);
	    }

//            @Override
//            public void mouseClicked(java.awt.event.MouseEvent evt) {
//                logger.debug("mouseClicked");
//                checkPopup(evt);
//            }
	    private void checkPopup(java.awt.event.MouseEvent evt) {
		if (evt.isPopupTrigger() /* evt.getButton() == MouseEvent.BUTTON3 || evt.isMetaDown()*/) {
		    // set the clicked cell selected
		    java.awt.Point p = evt.getPoint();
		    int clickedColumn = columnAtPoint(p);
		    logger.debug("clickedColumn: {}", clickedColumn);
		    if (clickedColumn == 2 || clickedColumn == 3) {
			JPopupMenu viewPopupMenu = new javax.swing.JPopupMenu();
			viewPopupMenu.setName("viewPopupMenu");

			JMenuItem selectedAllMenuItem = new javax.swing.JMenuItem();
			selectedAllMenuItem.setText("Selected All");
			viewPopupMenu.setInvoker(evt.getComponent());
			selectedAllMenuItem.setActionCommand("" + clickedColumn);
			selectedAllMenuItem.addActionListener(new java.awt.event.ActionListener() {

			    public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
				    int targetColumn = Integer.parseInt(evt.getActionCommand());
				    ArbilFieldViewTableModel fieldViewTableModel = (ArbilFieldViewTableModel) ((JTable) ((JPopupMenu) ((JMenuItem) evt.getSource()).getComponent().getParent()).getInvoker()).getModel();
				    logger.trace("targetColumn: {}:{}", targetColumn, evt.getActionCommand());
				    for (int rowCounter = 0; rowCounter < fieldViewTableModel.getRowCount(); rowCounter++) {
					fieldViewTableModel.setValueAt(true, rowCounter, targetColumn);
				    }
				} catch (Exception ex) {
				    BugCatcherManager.getBugCatcher().logError(ex);
				}
			    }
			});
			viewPopupMenu.add(selectedAllMenuItem);

			JMenuItem selectetNodeMenuItem = new javax.swing.JMenuItem();
			selectetNodeMenuItem.setText("Selected None");
			selectetNodeMenuItem.setActionCommand("" + clickedColumn);
			selectetNodeMenuItem.addActionListener(new java.awt.event.ActionListener() {

			    public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
				    int targetColumn = Integer.parseInt(evt.getActionCommand());
				    ArbilFieldViewTableModel fieldViewTableModel = (ArbilFieldViewTableModel) ((JTable) ((JPopupMenu) ((JMenuItem) evt.getSource()).getComponent().getParent()).getInvoker()).getModel();
				    logger.trace("targetColumn: {}:{}", targetColumn, evt.getActionCommand());
				    for (int rowCounter = 0; rowCounter < fieldViewTableModel.getRowCount(); rowCounter++) {
					fieldViewTableModel.setValueAt(false, rowCounter, targetColumn);
				    }
				} catch (Exception ex) {
				    BugCatcherManager.getBugCatcher().logError(ex);
				}
			    }
			});
			viewPopupMenu.add(selectetNodeMenuItem);

			viewPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
		    }
		}
	    }
	});
    }

    @Override
    //Implement table cell tool tips.
    public String getToolTipText(MouseEvent e) {
	java.awt.Point p = e.getPoint();
	switch (columnAtPoint(p)) {
	    case ArbilFieldViewTableModel.SHOW_ONLY_COLUMN:
		return widgets.getString("FIELD_VIEW_TABLE_SHOW ONLY CHECKED FIELDS (HIDES ALL OTHERS AND OVERRIDES HIDE FIELDS)");
	    case ArbilFieldViewTableModel.HIDE_COLUMN:
		return widgets.getString("FIELD_VIEW_TABLE_HIDE CHECKED FIELDS (ONLY ACTIVE WHEN NO 'SHOW ONLY' SELECTION IS MADE)");
	}
	return null;
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
	TableCellRenderer tableCellRenderer = super.getCellRenderer(row, column);
	if (tableCellRenderer instanceof JCheckBox) {
	    ((JCheckBox) tableCellRenderer).setEnabled(getModel().isCellEditable(row, column));
	}
	return tableCellRenderer;
    }
}
