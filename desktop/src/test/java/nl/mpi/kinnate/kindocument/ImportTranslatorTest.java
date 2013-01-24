/**
 * Copyright (C) 2012 The Language Archive
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
package nl.mpi.kinnate.kindocument;

import junit.framework.TestCase;

/**
 *  Document   : ImportTranslatorTest
 *  Created on : Dec 29, 2011, 13:41:44
 * @author petwit
 */
public class ImportTranslatorTest extends TestCase {

    public ImportTranslatorTest(String testName) {
        super(testName);
    }

    /**
     * Test of addTranslationEntry method, of class ImportTranslator.
     */
    public void testTranslationEntry() {
        System.out.println("testTranslationEntry");
        ImportTranslator instance = new ImportTranslator(true);

        assertEquals(instance.translate("FieldName", "A_Value").fieldName, "FieldName");
        assertEquals(instance.translate("Field/Name", "A//Value").fieldName, "Field_Name");
        assertEquals(instance.translate("Field\\Name", "A\\Value").fieldName, "Field_Name");
        assertEquals(instance.translate("%Field&Name@", "%A&Value@").fieldName, "_Field_Name_");
        assertEquals(instance.translate("%%%Field&&&Name@@@", "%%%A&&&Value@@@").fieldName, "_Field_Name_");

        assertEquals(instance.translate("FieldName", "A_Value").fieldValue, "A_Value");
        assertEquals(instance.translate("Field/Name", "A//Value").fieldValue, "A//Value");
        assertEquals(instance.translate("Field\\Name", "A\\Value").fieldValue, "A\\Value");
        assertEquals(instance.translate("%Field&Name@", "%A&Value@").fieldValue, "%A&Value@");
        assertEquals(instance.translate("%%%Field&&&Name@@@", "%%%A&&&Value@@@").fieldValue, "%%%A&&&Value@@@");
    }

    public void testTranslation() {
        System.out.println("testTranslation");
        ImportTranslator instance = new ImportTranslator(true);

        instance.addTranslationEntry("FieldName", "A_Value", "FieldName1", "A_Value1");
        assertEquals(instance.translate("FieldName", "A_Value").fieldName, "FieldName1");
        assertEquals(instance.translate("FieldName", "A_Value").fieldValue, "A_Value1");

        instance.addTranslationEntry("Field/Name", "A//Value", "FieldName2", "A_Value2");
        assertEquals(instance.translate("Field/Name", "A//Value").fieldName, "FieldName2");
        assertEquals(instance.translate("Field/Name", "A//Value").fieldValue, "A_Value2");


        instance.addTranslationEntry("Field\\Name", "A\\Value", "FieldName3", "A_Value3");
        assertEquals(instance.translate("Field\\Name", "A\\Value").fieldName, "FieldName3");
        assertEquals(instance.translate("Field\\Name", "A\\Value").fieldValue, "A_Value3");


        instance.addTranslationEntry("%Field&Name@", "%A&Value@", "FieldName4", "A_Value4");
        assertEquals(instance.translate("%Field&Name@", "%A&Value@").fieldName, "FieldName4");
        assertEquals(instance.translate("%Field&Name@", "%A&Value@").fieldValue, "A_Value4");

        instance.addTranslationEntry("%%%Field&&&Name@@@", "%%%A&&&Value@@@", "FieldName5", "A_Value5");
        assertEquals(instance.translate("%%%Field&&&Name@@@", "%%%A&&&Value@@@").fieldName, "FieldName5");
        assertEquals(instance.translate("%%%Field&&&Name@@@", "%%%A&&&Value@@@").fieldValue, "A_Value5");

        instance.addTranslationEntry("Date_of_Birth", null, "DateOfBirth", null);
        assertEquals(instance.translate("Date_of_Birth", "A_Value").fieldName, "DateOfBirth");
        assertEquals(instance.translate("Date_of_Birth", "A_Value").fieldValue, "A_Value");
    }
}
