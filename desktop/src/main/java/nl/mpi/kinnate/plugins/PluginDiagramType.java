package nl.mpi.kinnate.plugins;

/**
 *  Document   : PluginDiagramType
 *  Created on : Dec 30, 2011, 2:26:48 PM
 *  Author     : Peter Withers
 */
public interface PluginDiagramType {

    public boolean showKinTerms = false;
    public boolean showArchiveLinker = false;
    public boolean showDiagramTree = false;
    public boolean showEntitySearch = false;
    public boolean showIndexerSettings = false;
    public boolean showKinTypeStrings = false;
    public boolean showExportPanel = false;
    public boolean showMetaData = false;

    public String getMenuLabel();
}
