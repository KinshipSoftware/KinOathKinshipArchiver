package nl.mpi.kinnate.gedcomimport;

import java.util.ArrayList;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.kinnate.kindocument.ImportTranslator;

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
    protected ImportTranslator getImportTranslator() {
        ImportTranslator importTranslator = new ImportTranslator(true);
        // todo: add the translator values if required
//        importTranslator.addTranslationEntry("SEX", "F", "Gender", "Female");
//        importTranslator.addTranslationEntry("SEX", "M", "Gender", "Male");
//        importTranslator.addTranslationEntry("NAME", null, "Name", null);
//        importTranslator.addTranslationEntry("chro", null, "Chromosome", null);
        return importTranslator;
    }

    @Override
    protected ImportLineStructure getImportLineStructure(String lineString, ArrayList<String> gedcomLevelStrings) throws ImportException {
        return new TipLineStructure(lineString, gedcomLevelStrings);
    }

    @Override
    public boolean canImport(String inputFileString) {
        return (inputFileString.toLowerCase().endsWith(".tip"));
    }
}
