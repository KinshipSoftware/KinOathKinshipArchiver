package nl.mpi.kinnate.entityindexer;

import javax.xml.bind.annotation.XmlAttribute;

/**
 *  Document   : ParameterElement
 *  Created on : Apr 14, 2011, 11:23:20 AM
 *  Author     : Peter Withers
 */
public class ParameterElement {

    @XmlAttribute(name = "XPath", namespace = "http://mpi.nl/tla/kin")
    String xpathString = null;
    @XmlAttribute(name = "Value", namespace = "http://mpi.nl/tla/kin")
    String selectedValue = null;

    public ParameterElement() {
    }

    public ParameterElement(String xpathStringLocal, String selectedValueLocal) {
        xpathString = xpathStringLocal;
        selectedValue = selectedValueLocal;
    }

    public ParameterElement(String[] valuesArrayLocal) {
        xpathString = valuesArrayLocal[0];
        if (valuesArrayLocal.length > 1) {
            selectedValue = valuesArrayLocal[1];
        }
    }

    public boolean hasSelectedValue() {
        return selectedValue != null;
    }

    public String getSelectedValue() {
        return selectedValue;
    }

    public String getXpathString() {
        return xpathString;
    }
}
