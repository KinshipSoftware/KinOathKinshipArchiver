package nl.mpi.kinnate.plugins;

/**
 *  Document   : KinOathPlugin
 *  Created on : Dec 20, 2011, 2:49:57 PM
 *  Author     : Peter Withers
 */
public interface KinOathPlugin {

    public String getName();

    public String getVersionNumber();

    public String getDescription();

    public void setDiagramConnector(DiagramConnector diagramConnector);
}
