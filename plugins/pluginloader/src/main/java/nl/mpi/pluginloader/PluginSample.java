package nl.mpi.pluginloader;

import javax.swing.event.TableModelListener;

/**
 *  Document   : PluginSample
 *  Created on : Dec 22, 2011, 3:58:34 PM
 *  Author     : Peter Withers
 */
//@MetaInfServices
//@Service
public class PluginSample implements KinOathPlugin, PluginSettings {

    DiagramConnector diagramConnector;

    public String getName() {
        return "Sample Plugin Name";
    }

    public String getDescription() {
        return "Sample Plugin Description String";
    }

    public String getVersionNumber() {
        // this is the version of the plugin itself and is separate from the version numbers of the interfaces
        return "0.0.1";
    }

    public void setDiagramConnector(DiagramConnector diagramConnector) {
        this.diagramConnector = diagramConnector;
    }

    ////////////////////////////////////////////////////////////////////
    public void addTableModelListener(TableModelListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Class<?> getColumnClass(int columnIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getColumnCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getColumnName(int columnIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getRowCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeTableModelListener(TableModelListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
