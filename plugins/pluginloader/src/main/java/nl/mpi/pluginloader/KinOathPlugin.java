package nl.mpi.pluginloader;

import javax.swing.JPanel;

/**
 * Document : KinOathPlugin
 * Created on : Dec 20, 2011, 2:49:57 PM
 * Author : Peter Withers
 */
public interface KinOathPlugin {

    public String getName();

    public int getMajorVersionNumber();

    public int getMinorVersionNumber();

    public int getBuildVersionNumber();

    public String getDescription();

    public JPanel getUiPanel();
//    public void setDiagramConnector(DiagramConnector diagramConnector);
}
