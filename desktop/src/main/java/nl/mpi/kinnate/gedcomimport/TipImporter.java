package nl.mpi.kinnate.gedcomimport;

import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import nl.mpi.arbil.userstorage.SessionStorage;

/**
 * Document : TipImporter
 * Created on : Jul 25, 2012, 5:53:00 PM
 * Author : Peter Withers
 */
public class TipImporter extends GedcomImporter {

    public TipImporter(JProgressBar progressBarLocal, JTextArea importTextAreaLocal, boolean overwriteExistingLocal, SessionStorage sessionStorage) {
        super(progressBarLocal, importTextAreaLocal, overwriteExistingLocal, sessionStorage);
    }

    @Override
    public boolean canImport(String inputFileString) {
        return (inputFileString.toLowerCase().endsWith(".tip"));
    }
}
