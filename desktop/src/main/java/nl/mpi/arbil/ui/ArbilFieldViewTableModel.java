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

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.ResourceBundle;
import javax.swing.table.DefaultTableModel;
import nl.mpi.arbil.util.MessageDialogHandler;

/**
 * Document : ArbilFieldViewTableModel
 * This table model is used to edit the field view for a given imdi table model
 * Split from ImdiFieldViews on : Dec 16, 2008, 10:27:30 AM
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilFieldViewTableModel extends DefaultTableModel {

    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");
    private ArbilTableModel imdiTableModel;
    private static MessageDialogHandler dialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler dialogHandlerInstance) {
	dialogHandler = dialogHandlerInstance;
    }

    public ArbilFieldViewTableModel(ArbilTableModel localImdiTableModel) {
	imdiTableModel = localImdiTableModel;
	//logger.debug("setting to: " + viewLabel);
	// "Column Description",  is not relevant here because this is a list of column names not imdi fields
	setColumnIdentifiers(new String[]{widgets.getString("FIELD_VIEW_TABLE_COLUMN NAME"), widgets.getString("FIELD_VIEW_TABLE_SHOW ONLY"), widgets.getString("FIELD_VIEW_TABLE_HIDE"), widgets.getString("FIELD_VIEW_TABLE_COLUMN WIDTH")}); //, "Always Display"
	// we want a table model even if it has no rows
	ArbilFieldView currentView = imdiTableModel.getFieldView();
	if (currentView != null) {
	    // loop the known columns
	    for (Enumeration knownColumnNames = ((ArbilFieldView) currentView).getKnownColumns(); knownColumnNames.hasMoreElements();) {
		String currentFieldName = knownColumnNames.nextElement().toString();
		this.addRow(new Object[]{currentFieldName,
		    //GuiHelper.imdiSchema.getHelpForField(currentFieldName),
		    // set the show only fields
		    ((ArbilFieldView) currentView).isShowOnlyColumn(currentFieldName),
		    // set the hidden fields
		    ((ArbilFieldView) currentView).isHiddenColumn(currentFieldName),
		    // set width
		    widthToString(((ArbilFieldView) currentView).getColumnWidth(currentFieldName))
		//,
		// set alays show fields
		//((LinorgFieldView) currentView).isAlwaysShowColumn(currentFieldName)
		});
	    }
	}
    }
    Class[] types = new Class[]{
	java.lang.String.class, /*java.lang.String.class,*/ java.lang.Boolean.class, java.lang.Boolean.class, java.lang.String.class
    };
    private int showOnlyEnabledCount = -1;
    public static final int FIELD_NAME_COLUMN = 0;
    public static final int SHOW_ONLY_COLUMN = 1;
    public static final int HIDE_COLUMN = 2;
    public static final int WIDTH_COLUMN = 3;

    @Override
    public Class getColumnClass(int columnIndex) {
	return types[columnIndex];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
//        logger.debug("isCellEditable showOnlyEnabledCount: " + showOnlyEnabledCount);
	if (column == HIDE_COLUMN) {
	    if (showOnlyEnabledCount < 0) { // count the show only selection
		showOnlyEnabledCount = 0;
		for (int rowCounter = 0; rowCounter < getRowCount(); rowCounter++) {
		    //if (((Boolean) getValueAt(rowCounter, SHOW_ONLY_COLUMN)) == true) {
		    if (getValueAt(rowCounter, SHOW_ONLY_COLUMN).equals(true)) {
			showOnlyEnabledCount++;
		    }
		}

	    }
	    return showOnlyEnabledCount == 0;
//                    return (getValueAt(row, SHOW_ONLY_COLUMN).equals(true) || showOnlyEnabledCount == 0);
	}
	return (column == SHOW_ONLY_COLUMN || column == HIDE_COLUMN || column == WIDTH_COLUMN);
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
	super.setValueAt(aValue, row, column);
	// clear the show only count to retrigger the checking process
	showOnlyEnabledCount = -1;
	boolean booleanState = Boolean.TRUE.equals(aValue);
	String targetColumnName = getValueAt(row, FIELD_NAME_COLUMN).toString();
	switch (column) {
	    case SHOW_ONLY_COLUMN:
		if (booleanState) {
		    imdiTableModel.getFieldView().addShowOnlyColumn(targetColumnName);
		} else {
		    imdiTableModel.getFieldView().removeShowOnlyColumn(targetColumnName);
		}
		break;
	    case HIDE_COLUMN:
		if (booleanState) {
		    imdiTableModel.getFieldView().addHiddenColumn(targetColumnName);
		} else {
		    imdiTableModel.getFieldView().removeHiddenColumn(targetColumnName);
		}
		break;
	    case WIDTH_COLUMN:
		if (aValue != null) {
		    if (aValue instanceof String) {
			String intString = ((String) aValue).trim();
			if (intString.length() > 0) {
			    try {
				Integer width = Integer.parseInt((String) aValue);
				imdiTableModel.setPreferredColumnWidth(targetColumnName, width);
			    } catch (NumberFormatException ex) {
				dialogHandler.addMessageDialogToQueue(
					MessageFormat.format(widgets.getString("FIELD_VIEW_TABLE_INVALID WIDTH FOR COLUMN {0}"), targetColumnName),
					widgets.getString("COLUMN WIDTH INVALID"));
			    }
			    break;
			}
		    }
		}
		// Empty or invalid column width
		imdiTableModel.setPreferredColumnWidth(targetColumnName, null);
		break;
//            case 4:
//                if (booleanState) {
//                    imdiTableModel.getFieldView().addAlwaysShowColumn(targetColumnName);
//                } else {
//                    imdiTableModel.getFieldView().removeAlwaysShowColumn(targetColumnName);
//                }
//                break;
	}
	imdiTableModel.requestReloadTableData();
	fireTableStructureChanged();
    }//returnTableModel.setRowCount(0);

    private String widthToString(Integer width) {
	if (width == null) {
	    return "";
	} else {
	    return String.valueOf(width);
	}
    }
}
