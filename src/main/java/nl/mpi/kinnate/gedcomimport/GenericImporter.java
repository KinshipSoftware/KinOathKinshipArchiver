package nl.mpi.kinnate.gedcomimport;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

/**
 *  Document   : GenericImporter
 *  Created on : May 30, 2011, 10:31:15 AM
 *  Author     : Peter Withers
 */
public interface GenericImporter {

    public HashMap<String, ArrayList<String>> getCreatedNodeIds();

    public void setProgressBar(JProgressBar progressBarLocal);

    public URI[] importTestFile(JTextArea importTextArea, File testFile);

    public URI[] importTestFile(JTextArea importTextArea, String testFileString);
}
