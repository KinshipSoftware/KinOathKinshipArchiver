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
package nl.mpi.kinnate.kindocument;

import java.util.HashMap;

/**
 * Document : ImportTranslator Created on : Aug 18, 2011, 11:53:44 AM
 *
 * @author Peter Withers
 */
public class ImportTranslator {
    
    private HashMap<TranslationElement, TranslationElement> translateTable;
    boolean ignoreCase;
    
    public class TranslationElement {
        
        String fieldName;
        String fieldValue;
        
        public TranslationElement(String fieldName, String fieldValue) {
            if (fieldName.matches("^[0-9].*")) {
                fieldName = "_" + fieldName;
            }
            this.fieldName = fieldName.replaceAll("[^a-zA-Z0-9]+", "_");
            this.fieldValue = fieldValue;
        }
        
        private String checkCase(String inputString) {
            if (ignoreCase) {
                return inputString.toLowerCase();
            } else {
                return inputString;
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TranslationElement other = (TranslationElement) obj;
            if ((this.fieldName == null) ? (other.fieldName != null) : !this.fieldName.equals(other.fieldName)) {
                return false;
            }
            if ((this.fieldValue == null) ? (other.fieldValue != null) : !checkCase(this.fieldValue).equals(checkCase(other.fieldValue))) {
                return false;
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            int hash = 5;
            hash = 11 * hash + (this.fieldName != null ? this.fieldName.hashCode() : 0);
            hash = 11 * hash + (this.fieldValue != null ? checkCase(this.fieldValue).hashCode() : 0);
            return hash;
        }
    }
    
    public ImportTranslator(boolean ignoreCase) {
        translateTable = new HashMap<TranslationElement, TranslationElement>();
        this.ignoreCase = ignoreCase;
    }
    
    public void addTranslationEntry(String fieldName, String fieldValue, String translatedFieldName, String translatedFieldValue) {
        translateTable.put(new TranslationElement(fieldName, fieldValue), new TranslationElement(translatedFieldName, translatedFieldValue));
    }
    
    public TranslationElement translate(String fieldName, String fieldValue) {
        TranslationElement translatedInput = new TranslationElement(fieldName, fieldValue);
        TranslationElement translatedResult = translateTable.get(translatedInput);
        if (translatedResult == null) {
            // look for translation entries that act only on the field name
            translatedInput = new TranslationElement(fieldName, null);
            translatedResult = translateTable.get(translatedInput);
        }
        if (translatedResult != null) {
            if (translatedResult.fieldValue == null) {
                // if no field value has been set then just pass the input value back but change the field name as specified
                return new TranslationElement(translatedResult.fieldName, fieldValue);
            } else {
                return translatedResult;
            }
        } else {
            return new TranslationElement(fieldName, fieldValue);
        }
    }
}
