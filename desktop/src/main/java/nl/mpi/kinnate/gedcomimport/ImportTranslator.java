/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.kinnate.gedcomimport;

import java.util.HashMap;

/**
 *  Document   : ImportTranslator
 *  Created on : Aug 18, 2011, 11:53:44 AM
 *  Author     : Peter Withers
 */
public class ImportTranslator {

    private HashMap<TranslationElement, TranslationElement> translateTable;

    public class TranslationElement {

        String fieldName;
        String fieldValue;

        public TranslationElement(String fieldName, String fieldValue) {
            this.fieldName = fieldName.replaceAll("\\s", "_");
            this.fieldValue = fieldValue;
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
            if ((this.fieldValue == null) ? (other.fieldValue != null) : !this.fieldValue.equals(other.fieldValue)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 11 * hash + (this.fieldName != null ? this.fieldName.hashCode() : 0);
            hash = 11 * hash + (this.fieldValue != null ? this.fieldValue.hashCode() : 0);
            return hash;
        }
    }

    public ImportTranslator() {
        translateTable = new HashMap<TranslationElement, TranslationElement>();
    }

    public void addTranslationEntry(String fieldName, String fieldValue, String translatedFieldName, String translatedFieldValue) {
        translateTable.put(new TranslationElement(fieldName, fieldValue), new TranslationElement(translatedFieldName, translatedFieldValue));
    }

    public TranslationElement translate(String fieldName, String fieldValue) {
        TranslationElement translatedInput = new TranslationElement(fieldName, fieldValue);
        TranslationElement translatedResult = translateTable.get(translatedInput);
        if (translatedResult != null) {
            return translatedResult;
        } else {
            return translatedInput;
        }
    }
}
