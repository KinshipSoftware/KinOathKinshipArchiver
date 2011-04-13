package nl.mpi.kinnate.entityindexer;

import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.bind.annotation.XmlElement;

/**
 *  Document   : IndexerParam
 *  Created on : Apr 13, 2011, 3:24:44 PM
 *  Author     : Peter Withers
 */
public class IndexerParam {

    IndexerParameters indexerParameters;
    @XmlElement(name = "kin:PathEntry")
    private String[][] valuesArray;
    private String[] availableValuesArray = null;

    public IndexerParam() {
    }

    public IndexerParam(IndexerParameters indexerParametersLocal, String[][] valuesArrayLocal) {
        indexerParameters = indexerParametersLocal;
        valuesArray = valuesArrayLocal;
    }

    public void setValues(String[][] valuesArrayLocal) {
        valuesArray = valuesArrayLocal;
//        IndexerParameters.this.relevantEntityData = null;
        // cause the entity index and entity collection to update based on the new indexer values
        indexerParameters.valuesChanged = true;
    }

    public void setValue(String parameterString, String valueString) {
        ArrayList<String[]> tempArrayList = new ArrayList<String[]>(Arrays.asList(valuesArray));
        for (String[] currentEntry : valuesArray) {
            if (currentEntry[0].equals(parameterString)) {
                currentEntry[1] = valueString;
            }
        }
        setValues(tempArrayList.toArray(new String[][]{}));
        // cause the entity index and entity collection to update based on the new indexer values
        indexerParameters.valuesChanged = true;
    }

    public void removeValue(String valueToRemove) {
        ArrayList<String[]> tempArrayList = new ArrayList<String[]>(Arrays.asList(valuesArray));
        for (String[] currentEntry : valuesArray) {
            if (currentEntry[0].equals(valueToRemove)) {
                tempArrayList.remove(currentEntry); // todo: will array list remove use the value of the array or the pointer to the array?????
            }
        }
        setValues(tempArrayList.toArray(new String[][]{}));
        // cause the entity index and entity collection to update based on the new indexer values
        indexerParameters.valuesChanged = true;
    }

    public String[][] getValues() {
        return valuesArray;
    }

    public void setAvailableValues(String[] availableValuesArrayLocal) {
        availableValuesArray = availableValuesArrayLocal;
    }

    public String[] getAvailableValues() {
        return availableValuesArray;
    }
}
