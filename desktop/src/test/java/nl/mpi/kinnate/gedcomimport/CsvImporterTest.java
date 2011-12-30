package nl.mpi.kinnate.gedcomimport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import junit.framework.TestCase;
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
        runFieldsForLine("one,\"two\nextra\",three\r", new String[]{"one", "two\nextre", "three"});
    }

    private void runFieldsForLine(String csvInputString, String[] expectedResult) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new StringReader(csvInputString));
            ArrayList<String> arrayList = new CsvImporter(new JProgressBar(), new JTextArea(), true, new KinSessionStorage()).getFieldsForLine(bufferedReader);

            assertTrue("Incorrect number of fields found", expectedResult.length == arrayList.size());
            for (int arrayCounter = 0; arrayCounter < expectedResult.length; arrayCounter++) {
                assertEquals(csvInputString, expectedResult[arrayCounter], arrayList.get(arrayCounter));
            }
        } catch (IOException exception) {
            fail(exception.getMessage());
        }

    }
}
