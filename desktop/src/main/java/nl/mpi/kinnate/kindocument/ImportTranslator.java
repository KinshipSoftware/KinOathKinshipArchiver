package nl.mpi.kinnate.kindocument;

import java.util.HashMap;

/**
 *  Document   : ImportTranslator
 *  Created on : Aug 18, 2011, 11:53:44 AM
 *  Author     : Peter Withers
 */
public class ImportTranslator {

    private HashMap<TranslationElement, TranslationElement> translateTable;
    boolean ignoreCase;

    public class TranslationElement {

        String fieldName;
        String fieldValue;

        public TranslationElement(String fieldName, String fieldValue) {
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
        if (translatedResult != null) {
            if (translatedResult.fieldValue == null) {
                // if no field value has been set then just pass the input value back but change the field name as specified
                return new TranslationElement(translatedResult.fieldName, fieldValue);
            } else {
                return translatedResult;
            }
        } else {
            return translatedInput;
        }
    }
}
