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
package nl.mpi.kinnate.entityindexer;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Document : IndexerParam Created on : Apr 13, 2011, 3:24:44 PM
 *
 * @author Peter Withers
 */
public class IndexerParam {

    @XmlTransient
    IndexerParameters indexerParameters;
    @XmlElement(name = "IndexerParam", namespace = "http://mpi.nl/tla/kin")
    private ArrayList<ParameterElement> parametersList;
    @XmlTransient
    private String[] availableValuesArray = null;
    @XmlAttribute(name = "DefaultFormat", namespace = "http://mpi.nl/tla/kin")
    private String defaultValueFormat = "%s";

    public IndexerParam() {
    }

    public IndexerParam(String[][] valuesArrayLocal, String defaultValueFormat) {
        parametersList = new ArrayList<ParameterElement>();
        this.defaultValueFormat = defaultValueFormat;
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

    public void setChangedFlag() {
        indexerParameters.valuesChanged = true;
    }

    public void removeValue(ParameterElement parameterElementRemove) {
//        ArrayList<ParameterElement> tempParametersList = new ArrayList<ParameterElement>();
//        for (ParameterElement currentEntry : parametersList) {
//            if (!currentEntry.getXpathString().equals(pathToRemove)) {
//                tempParametersList.add(currentEntry);
//            }
//        }
//        parametersList = tempParametersList;
        parametersList.remove(parameterElementRemove);
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

    public String getDefaultValueFormat() {
        return defaultValueFormat;
    }
}
