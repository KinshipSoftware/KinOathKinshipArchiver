package nl.mpi.kinnate;

import java.io.File;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.svg.MouseListenerSvg;

/**
 *  Document   : SavePanel
 *  Created on : Feb 17, 2011, 1:37:48 PM
 *  Author     : Peter Withers
 */
public interface SavePanel {

    public boolean requiresSave();

    public void setRequiresSave();

    public boolean hasSaveFileName();

    public File getFileName();

    public void saveToFile();

    public void saveToFile(File saveFile);

    public void updateGraph();

    public void doActionCommand(MouseListenerSvg.ActionCode actionCode);

    public GraphPanel getGraphPanel();
}
