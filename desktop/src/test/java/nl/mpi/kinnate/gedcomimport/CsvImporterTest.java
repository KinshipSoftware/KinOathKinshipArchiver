/**
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
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
import nl.mpi.kinnate.projects.ProjectManager;
import nl.mpi.kinnate.userstorage.KinSessionStorage;

/**
 * Document : CsvImporterTest Created on : Dec 30, 2011, 10:11:44
 *
 * @author petwit
 */
public class CsvImporterTest extends TestCase {

    /**
     * Test of getFieldsForLine method, of class CsvImporter.
     */
    public void testFieldsForLine() {
        runFieldsForLine("one,two,three", new String[]{"one", "two", "three"}, ',');
        runFieldsForLine("one\ttwo\tthree", new String[]{"one", "two", "three"}, '\t');
        runFieldsForLine("one,two,three\n", new String[]{"one", "two", "three"}, ',');
        runFieldsForLine("one,two,three\r", new String[]{"one", "two", "three"}, ',');
        runFieldsForLine("one,\"two\",three\r", new String[]{"one", "two", "three"}, ',');
        runFieldsForLine("one,\"two\",three\r", new String[]{"one", "two", "three"}, ',');
        runFieldsForLine("one,\"two\nextra\",three\r", new String[]{"one", "two\nextra", "three"}, ',');
        runFieldsForLine("one,\"two,extra\",three\r", new String[]{"one", "two,extra", "three"}, ',');
        runFieldsForLine("one,\"two\textra\",three\r", new String[]{"one", "two\textra", "three"}, ',');
        runFieldsForLine("one\t\"two,extra\"\tthree\r", new String[]{"one", "two,extra", "three"}, '\t');
        runFieldsForLine("one\t\"two\textra\"\tthree\r", new String[]{"one", "two\textra", "three"}, '\t');
        runFieldsForLine("* comments \n* more comments\r\r\n\n\rone\t\"two\textra\"\tthree\r", new String[]{"one", "two\textra", "three"}, '\t');
    }

    private void runFieldsForLine(String csvInputString, String[] expectedResult, char fieldSeparator) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new StringReader(csvInputString));
            final ApplicationVersionManager applicationVersionManager = new ApplicationVersionManager(new KinOathVersion());
            final KinSessionStorage kinSessionStorage = new KinSessionStorage(applicationVersionManager);
            final ArbilBugCatcher bugCatcher = new ArbilBugCatcher(kinSessionStorage, new ApplicationVersionManager(new KinOathVersion()));
            ArrayList<String> arrayList = new CsvImporter(new JProgressBar(), new JTextArea(), true, new KinSessionStorage(applicationVersionManager), new ProjectManager().getDefaultProject(kinSessionStorage)).getFieldsForLineExcludingComments(bufferedReader, fieldSeparator);

            assertTrue("Incorrect number of fields found", expectedResult.length == arrayList.size());
            for (int arrayCounter = 0; arrayCounter < expectedResult.length; arrayCounter++) {
                assertEquals(csvInputString, expectedResult[arrayCounter], arrayList.get(arrayCounter));
            }
        } catch (IOException exception) {
            fail(exception.getMessage());
        }

    }
}
