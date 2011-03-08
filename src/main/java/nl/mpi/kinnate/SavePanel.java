package nl.mpi.kinnate;

import java.io.File;

/**
 *  Document   : SavePanel
 *  Created on : Feb 17, 2011, 1:37:48 PM
 *  Author     : Peter Withers
 */
public interface SavePanel {

    public boolean requiresSave();

    public boolean hasSaveFileName();

    public void saveToFile();

    public void saveToFile(File saveFile);

    public void updateGraph();
}
