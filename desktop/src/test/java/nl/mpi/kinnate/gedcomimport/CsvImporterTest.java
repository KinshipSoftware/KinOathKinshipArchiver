package nl.mpi.kinnate.gedcomimport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import junit.framework.TestCase;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.kinnate.KinOathVersion;
import nl.mpi.kinnate.userstorage.KinSessionStorage;

/**
 *  Document   : CsvImporterTest
 *  Created on : Dec 30, 2011, 10:11:44
 * @author petwit
 */
public class CsvImporterTest extends TestCase {

    /**
     * Test of getFieldsForLine method, of class CsvImporter.
     */
    public void testFieldsForLine() {
        runFieldsForLine("one,two,three", new String[]{"one", "two", "three"});
        runFieldsForLine("one\ttwo\tthree", new String[]{"one", "two", "three"});
        runFieldsForLine("one,two,three\n", new String[]{"one", "two", "three"});
        runFieldsForLine("one,two,three\r", new String[]{"one", "two", "three"});
        runFieldsForLine("one,\"two\",three\r", new String[]{"one", "two", "three"});
        runFieldsForLine("one,\"two\",three\r", new String[]{"one", "two", "three"});
        runFieldsForLine("one,\"two\nextra\",three\r", new String[]{"one", "two\nextra", "three"});
        runFieldsForLine("one,\"two,extra\",three\r", new String[]{"one", "two,extra", "three"});
        runFieldsForLine("one,\"two\textra\",three\r", new String[]{"one", "two\textra", "three"});
        runFieldsForLine("one\t\"two,extra\"\tthree\r", new String[]{"one", "two,extra", "three"});
        runFieldsForLine("one\t\"two\textra\"\tthree\r", new String[]{"one", "two\textra", "three"});
    }

    private void runFieldsForLine(String csvInputString, String[] expectedResult) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new StringReader(csvInputString));
            final ApplicationVersionManager applicationVersionManager = new ApplicationVersionManager(new KinOathVersion());
            final KinSessionStorage kinSessionStorage = new KinSessionStorage(applicationVersionManager);
            final ArbilBugCatcher bugCatcher = new ArbilBugCatcher(kinSessionStorage, new ApplicationVersionManager(new KinOathVersion()));
            ArrayList<String> arrayList = new CsvImporter(new JProgressBar(), new JTextArea(), true, new KinSessionStorage(applicationVersionManager)).getFieldsForLine(bufferedReader);

            assertTrue("Incorrect number of fields found", expectedResult.length == arrayList.size());
            for (int arrayCounter = 0; arrayCounter < expectedResult.length; arrayCounter++) {
                assertEquals(csvInputString, expectedResult[arrayCounter], arrayList.get(arrayCounter));
            }
        } catch (IOException exception) {
            fail(exception.getMessage());
        }

    }
}
