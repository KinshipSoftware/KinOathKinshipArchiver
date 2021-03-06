/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.gedcomimport;

import java.util.ArrayList;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.kinnate.kindocument.ImportTranslator;
import nl.mpi.kinnate.projects.ProjectRecord;

/**
 * Created on : Jul 25, 2012, 5:53:00 PM
 *
 * @author Peter Withers
 */
public class TipImporter extends GedcomImporter {

    public TipImporter(JProgressBar progressBarLocal, JTextArea importTextAreaLocal, boolean overwriteExistingLocal, SessionStorage sessionStorage, ProjectRecord projectRecord) {
        super(progressBarLocal, importTextAreaLocal, overwriteExistingLocal, sessionStorage, projectRecord);
    }

    @Override
    protected ImportTranslator getImportTranslator() {
        ImportTranslator importTranslator = new ImportTranslator(true);
        importTranslator.addTranslationEntry("BIRT_Date", null, "DateOfBirth", null);
        importTranslator.addTranslationEntry("DEAT_Date", null, "DateOfDeath", null);
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
