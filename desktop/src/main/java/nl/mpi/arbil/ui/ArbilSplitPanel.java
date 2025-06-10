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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.ui.menu.ImagePreviewContextMenu;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.TreeHelper;

/**
 * Document : ArbilSplitPanel
 * Created on :
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilSplitPanel extends JPanel implements ArbilWindowComponent {

    private final SessionStorage sessionStorage;
    private final TreeHelper treeHelper;
    private final ArbilDragDrop dragDrop;
    public final ArbilTable arbilTable;
    private JList imagePreview;
    private JScrollPane tableScrollPane;
    private JScrollPane listScroller;
    private JSplitPane splitPane;
    private JLabel hiddenColumnsLabel;
    private FindReplacePanel findReplacePanel = null;
    private boolean showSearchPanel = false;
    private JPanel tableOuterPanel;
    boolean selectionChangeInProcess = false; // this is to stop looping selection changes

    public ArbilSplitPanel(SessionStorage sessionStorage, TreeHelper treeHelper, ArbilDragDrop dragDrop, ArbilTable arbilTable) {
	this.sessionStorage = sessionStorage;
	this.treeHelper = treeHelper;
	this.dragDrop = dragDrop;
	this.arbilTable = arbilTable;

	this.setLayout(new BorderLayout());
	splitPane = new JSplitPane();
	hiddenColumnsLabel = new JLabel();
	tableScrollPane = new JScrollPane(arbilTable);
	tableScrollPane.addComponentListener(new ComponentListener() {
	    public void componentResized(ComponentEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			ArbilSplitPanel.this.arbilTable.setColumnWidths();
		    }
		});
	    }

	    public void componentMoved(ComponentEvent e) {
	    }

	    public void componentShown(ComponentEvent e) {
	    }

	    public void componentHidden(ComponentEvent e) {
	    }
	});
	tableOuterPanel = new JPanel(new BorderLayout());
	tableOuterPanel.add(tableScrollPane, BorderLayout.CENTER);
	tableOuterPanel.add(hiddenColumnsLabel, BorderLayout.SOUTH);
	arbilTable.getArbilTableModel().setHiddenColumnsLabel(hiddenColumnsLabel);
	imagePreview = new JList(arbilTable.getArbilTableModel().getListModel(this));
	imagePreview.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	imagePreview.setLayoutOrientation(JList.HORIZONTAL_WRAP);
	imagePreview.setVisibleRowCount(-1);

	listScroller = new JScrollPane(imagePreview);
	listScroller.setPreferredSize(new Dimension(250, 80));

	ImageBoxRenderer renderer = new ImageBoxRenderer();
	imagePreview.setCellRenderer(renderer);
	splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
	splitPane.setDividerSize(5);

	imagePreview.addMouseListener(new java.awt.event.MouseAdapter() {
	    @Override
	    public void mousePressed(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
		    new ImagePreviewContextMenu(imagePreview).show(evt.getX(), evt.getY());
		}
	    }

	    @Override
	    public void mouseReleased(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
		    new ImagePreviewContextMenu(imagePreview).show(evt.getX(), evt.getY());
		}
	    }
	});

	imagePreview.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	imagePreview.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting() && !selectionChangeInProcess) { // while this is not thread safe this should only be called by the swing thread via the gui or as a consequence of the enclosed selection changes
		    selectionChangeInProcess = true;
		    if (e.getSource() instanceof JList) {
//                        logger.debug("JList");
			final ArbilTable arbilTable = ArbilSplitPanel.this.arbilTable;
			arbilTable.clearSelection();
			int minSelectedRow = -1;
			int maxSelectedRow = -1;
			for (Object selectedRow : ((JList) e.getSource()).getSelectedValues()) {
			    arbilTable.setColumnSelectionAllowed(false);
			    arbilTable.setRowSelectionAllowed(true);
			    for (int rowCount = 0; rowCount < arbilTable.getRowCount(); rowCount++) {
				if (arbilTable.getTableCellContentAt(rowCount, 0).equals(selectedRow)) {
				    arbilTable.addRowSelectionInterval(rowCount, rowCount);
				    if (maxSelectedRow == -1 || maxSelectedRow < rowCount) {
					maxSelectedRow = rowCount;
				    }
				    if (minSelectedRow == -1 || minSelectedRow > rowCount) {
					minSelectedRow = rowCount;
				    }
				}
			    }
//                            logger.debug("selectedRow:" + selectedRow);
			    if (maxSelectedRow != -1) {
				arbilTable.scrollRectToVisible(arbilTable.getCellRect(minSelectedRow, 0, true));
			    }
			}
			if (ArbilSplitPanel.this.sessionStorage.loadBoolean("trackTableSelection", false)) {
			    ArbilSplitPanel.this.treeHelper.jumpToSelectionInTree(true, (ArbilDataNode) ((JList) e.getSource()).getSelectedValue());
			}
		    }
		    selectionChangeInProcess = false;
		}
	    }
	});
	arbilTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting() && !selectionChangeInProcess) { // while this is not thread safe this should only be called by the swing thread via the gui or as a consequence of the enclosed selection changes
		    selectionChangeInProcess = true;
		    imagePreview.clearSelection();
		    int minSelectedRow = -1;
		    int maxSelectedRow = -1;
		    for (Object selectedRow : ArbilSplitPanel.this.arbilTable.getSelectedRowsFromTable()) {
//                        logger.debug("selectedRow:" + selectedRow);
			for (int rowCount = 0; rowCount < imagePreview.getModel().getSize(); rowCount++) {
//                            logger.debug("JList:" + fileList.getModel().getElementAt(rowCount));
			    if (imagePreview.getModel().getElementAt(rowCount).equals(selectedRow)) {
				imagePreview.addSelectionInterval(rowCount, rowCount);
//                                logger.debug("add selection");
				if (maxSelectedRow == -1 || maxSelectedRow < rowCount) {
				    maxSelectedRow = rowCount;
				}
				if (minSelectedRow == -1 || minSelectedRow > rowCount) {
				    minSelectedRow = rowCount;
				}
			    }
			}
		    }
		    if (maxSelectedRow != -1) {
			imagePreview.scrollRectToVisible(imagePreview.getCellBounds(minSelectedRow, maxSelectedRow));
		    }
		    if (ArbilSplitPanel.this.sessionStorage.loadBoolean("trackTableSelection", false)) {
			ArbilSplitPanel.this.treeHelper.jumpToSelectionInTree(true, ArbilSplitPanel.this.arbilTable.getDataNodeForSelection());
		    }
		    selectionChangeInProcess = false;
		}
	    }
	});
    }

    public void showSearchPane() {
	if (findReplacePanel == null) {
	    findReplacePanel = new FindReplacePanel(this);
	}
	if (!showSearchPanel) {
	    tableOuterPanel.remove(hiddenColumnsLabel);
	    tableOuterPanel.add(findReplacePanel, BorderLayout.SOUTH);
	} else {
	    tableOuterPanel.remove(findReplacePanel);
	    tableOuterPanel.add(hiddenColumnsLabel, BorderLayout.SOUTH);
	}
	showSearchPanel = !showSearchPanel;
	this.revalidate();
	this.repaint();
	if (showSearchPanel) {
	    findReplacePanel.requestFocusOnSearchField();
	}
    }

    public void setSplitDisplay() {
	this.removeAll();
	if (imagePreview.getModel().getSize() == 0) {
	    this.add(tableOuterPanel);
	} else {
	    splitPane.setTopComponent(tableOuterPanel);
//            splitPane.setTopComponent(tableScrollPane);
	    splitPane.setBottomComponent(listScroller);
	    if (dragDrop != null) {
		dragDrop.addDrag(imagePreview);
		dragDrop.setTransferHandlerOnComponent(tableScrollPane);
	    }
	    this.add(splitPane);
	    this.doLayout();
	    splitPane.setDividerLocation(0.5);
	}
	if (dragDrop != null) {
	    dragDrop.addDrag(arbilTable);
	    dragDrop.setTransferHandlerOnComponent(this);
	}
	this.doLayout();
    }

    @Override
    public void doLayout() {
//        imdiTable.doLayout();
	super.doLayout();
    }

    public void addFocusListener(JInternalFrame internalFrame) {
	internalFrame.addInternalFrameListener(new InternalFrameAdapter() {
	    @Override
	    public void internalFrameDeactivated(InternalFrameEvent e) {
		TableCellEditor tableCellEditor = arbilTable.getCellEditor();
		if (tableCellEditor != null) {
		    tableCellEditor.stopCellEditing();
		}
		super.internalFrameDeactivated(e);
	    }
	});
    }

    public void arbilWindowClosed() {
	arbilTable.getArbilTableModel().removeAllArbilDataNodeRows();
    }
}
