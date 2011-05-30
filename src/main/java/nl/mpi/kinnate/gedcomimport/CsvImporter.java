package nl.mpi.kinnate.gedcomimport;

import java.io.InputStreamReader;
import java.net.URI;
import javax.swing.JTextArea;

/**
 *  Document   : CsvImporter
 *  Created on : May 30, 2011, 10:29:24 AM
 *  Author     : Peter Withers
 */
public class CsvImporter extends EntityImporter implements GenericImporter {

    public CsvImporter(boolean overwriteExistingLocal) {
        super(overwriteExistingLocal);
    }

    @Override
    public boolean canImport(String inputFileString) {
        return (inputFileString.toLowerCase().endsWith(".csv"));
    }

    @Override
    public URI[] importFile(JTextArea importTextArea, InputStreamReader inputStreamReader) {
        return null;
    }
}
