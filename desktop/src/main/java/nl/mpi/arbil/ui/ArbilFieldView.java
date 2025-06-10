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

import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that defines a collection of columns that should be shown or hidden
 * in a table view
 * Document   : ArbilFieldView
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ArbilFieldView implements Serializable, Cloneable {
    private final static Logger logger = LoggerFactory.getLogger(ArbilFieldView.class);

    private static final long serialVersionUID = 3L;
    private Vector hiddenColumns = new Vector();
    private Vector showOnlyColumns = new Vector();
    private Vector knownColumns = new Vector();
    private Vector alwaysShowColumns = new Vector();
    private HashMap<String, Integer> columnWidths = new HashMap<String, Integer>();

    public void showState() {
	logger.debug("knownColumns: {}", knownColumns);
	logger.debug("hiddenColumns: {}", hiddenColumns);
	logger.debug("showOnlyColumns: {}", showOnlyColumns);
	logger.debug("alwaysShowColumns: {}", alwaysShowColumns);
	logger.debug("columnWidths: {}", columnWidths);
    }

    public void setAlwaysShowColumns(Vector alwaysShowColumns) {
	this.alwaysShowColumns = alwaysShowColumns;
    }

    public Enumeration getAlwaysShowColumns() {
	return this.alwaysShowColumns.elements();
    }

    public void setHiddenColumns(Vector hiddenColumns) {
	this.hiddenColumns = hiddenColumns;
    }

    private void setKnownColumns(Vector knownColumns) {
	this.knownColumns = knownColumns;
    }

    public void setShowOnlyColumns(Vector showOnlyColumns) {
	this.showOnlyColumns = showOnlyColumns;
    }

    @Override
    public ArbilFieldView clone() {
	ArbilFieldView returnFieldView = new ArbilFieldView();
	returnFieldView.setAlwaysShowColumns((Vector) alwaysShowColumns.clone());
	returnFieldView.setHiddenColumns((Vector) hiddenColumns.clone());
	returnFieldView.setKnownColumns((Vector) knownColumns.clone());
	returnFieldView.setShowOnlyColumns((Vector) showOnlyColumns.clone());
	returnFieldView.columnWidths = (HashMap<String, Integer>) columnWidths.clone();
	return returnFieldView;
    }

    public void addKnownColumn(String columnName) {
	if (!knownColumns.contains(columnName)) {
	    knownColumns.add(columnName);
	}
    }

    public void setShowOnlyColumns(String[] columnsToShow) {
	showOnlyColumns.clear();
	for (int columnCounter = 0; columnCounter < columnsToShow.length; columnCounter++) {
	    showOnlyColumns.add(columnsToShow[columnCounter]);
	}
    }

    public void addAlwaysShowColumn(String columnName) {
	logger.debug("addAlwaysShowColumn");
	alwaysShowColumns.add(columnName);
	showState();
    }

    public void removeAlwaysShowColumn(String columnName) {
	logger.debug("removeAlwaysShowColumn");
	alwaysShowColumns.remove(columnName);
	showState();
    }

    public void addShowOnlyColumn(String columnName) {
	logger.debug("addShowOnlyColumn");
	showOnlyColumns.add(columnName);
	showState();
    }

    public void removeShowOnlyColumn(String columnName) {
	logger.debug("removeShowOnlyColumn");
	showOnlyColumns.remove(columnName);
	showState();
    }

    public void addHiddenColumn(String columnName) {
	logger.debug("addHiddenColumn");
	hiddenColumns.add(columnName);
	showOnlyColumns.remove(columnName);
	showState();
    }

    public void removeHiddenColumn(String columnName) {
	logger.debug("removeHiddenColumn");
	hiddenColumns.remove(columnName);
	showState();
    }

    public boolean viewShowsColumn(String currentColumnString) {
	boolean showColumn = true;
//    hiddenColumns, showOnlyColumns, knownColumns, alwaysShowColumns
	if (showOnlyColumns.size() > 0) {
	    // set to true if it is in the show only list
	    showColumn = showOnlyColumns.contains(currentColumnString);
	} else { // if (showColumn) { // this else makes the selection exclusive 
	    // set to false if in the hidden list
	    showColumn = !hiddenColumns.contains(currentColumnString);
	}
	if (!showColumn) {
	    // set to true if in the always show list
	    showColumn = alwaysShowColumns.contains(currentColumnString);
	}
	return showColumn;
    }

    public Enumeration getKnownColumns() {
	Collections.sort(knownColumns);
	return (knownColumns).elements();
    }

    public boolean isShowOnlyColumn(String columnString) {
	return showOnlyColumns.contains(columnString);
    }

    public boolean isHiddenColumn(String columnString) {
	return hiddenColumns.contains(columnString);
    }

    public boolean isAlwaysShowColumn(String columnString) {
	return alwaysShowColumns.contains(columnString);
    }

    /**
     * Sets the preferred column width for the specified column
     * @param columnString Name of column to set width for
     * @param width Preferred width for specified column. Null to remove preference
     */
    public void setColumnWidth(String columnString, Integer width) {
	if (width != null) {
	    columnWidths.put(columnString, width);
	} else if (hasColumnWidthForColumn(columnString)) {
	    columnWidths.remove(columnString);
	}
    }

    public boolean hasColumnWidthForColumn(String columnString) {
	return columnWidths.containsKey(columnString);
    }

    /**
     * @param columnString Name of the column to get the width for
     * @return Preferred width of column, if set. Otherwise null;
     */
    public Integer getColumnWidth(String columnString) {
	if (columnWidths.containsKey(columnString)) {
	    return columnWidths.get(columnString);
	} else {
	    return null;
	}
    }

    public void resetColumnWidths() {
	columnWidths.clear();
    }

    public void storeColumnWidths(TableColumnModel columnModel) {
	for (int i = 0; i < columnModel.getColumnCount(); i++) {
	    TableColumn column = columnModel.getColumn(i);
	    if (column.getHeaderValue() instanceof String) {
		setColumnWidth((String) column.getHeaderValue(), column.getWidth());
	    }
	}
    }
}
