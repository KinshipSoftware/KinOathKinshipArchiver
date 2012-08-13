package nl.mpi.kinnate.plugins;

/**
 *  Document   : KinOathPlugin
 *  Created on : Dec 20, 2011, 2:49:57 PM
 *  Author     : Peter Withers
 */
public interface KinOathPlugin {

    public int versionMajor = 0;
    public int versionMinor = 0;
    public int versionRevision = 1;

    public String getName();

    public String getVersionNumber();

    public String getDescription();

    public void setDiagramConnector(DiagramConnector diagramConnector);
}
