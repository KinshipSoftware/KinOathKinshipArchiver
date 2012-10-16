package nl.mpi.kinnate.entityindexer;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *  Document   : IndexerParam
 *  Created on : Apr 13, 2011, 3:24:44 PM
 *  Author     : Peter Withers
 */
public class IndexerParam {

    @XmlTransient
    IndexerParameters indexerParameters;
    @XmlElement(name = "IndexerParam", namespace = "http://mpi.nl/tla/kin")
    private ArrayList<ParameterElement> parametersList;
    @XmlTransient
    private String[] availableValuesArray = null;

    public IndexerParam() {
    }

    public IndexerParam(String[][] valuesArrayLocal) {
        parametersList = new ArrayList<ParameterElement>();
        for (String[] currentValue : valuesArrayLocal) {
            parametersList.add(new ParameterElement(currentValue));
        }
    }

    public void setParent(IndexerParameters indexerParametersLocal) {
        indexerParameters = indexerParametersLocal;
    }

    public void setValues(String[][] valuesArrayLocal) {
        parametersList.clear();
        for (String[] currentValue : valuesArrayLocal) {
            parametersList.add(new ParameterElement(currentValue[0], currentValue[1]));
        }
//        IndexerParameters.this.relevantEntityData = null;
        // cause the entity index and entity collection to update based on the new indexer values
        indexerParameters.valuesChanged = true;
    }

    public void setValue(String parameterString, String valueString) {
        if (valueString != null && valueString.length() == 0) {
            valueString = availableValuesArray[0];
        }
        // cause the entity index and entity collection to update based on the new indexer values
        indexerParameters.valuesChanged = true;
        for (ParameterElement currentEntry : parametersList) {
            if (currentEntry.xpathString.equals(parameterString)) {
                currentEntry.selectedValue = valueString;
                return;
            }
        }
        // if the value has not been found then add it
        parametersList.add(new ParameterElement(parameterString, valueString));
    }

    public void removeValue(String pathToRemove) {
        ArrayList<ParameterElement> tempParametersList = new ArrayList<ParameterElement>();
        for (ParameterElement currentEntry : parametersList) {
            if (!currentEntry.getXpathString().equals(pathToRemove)) {
                tempParametersList.add(currentEntry);
            }
        }
        parametersList = tempParametersList;
        // cause the entity index and entity collection to update based on the new indexer values
        indexerParameters.valuesChanged = true;
    }

    @XmlTransient
    public ParameterElement[] getValues() {
        return parametersList.toArray(new ParameterElement[]{});
    }

    @XmlTransient
    public void setAvailableValues(String[] availableValuesArrayLocal) {
        availableValuesArray = availableValuesArrayLocal;
    }

    public String[] getAvailableValues() {
        return availableValuesArray;
    }
}
