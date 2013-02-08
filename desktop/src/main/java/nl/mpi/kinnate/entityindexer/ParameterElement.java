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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Document : ParameterElement Created on : Apr 14, 2011, 11:23:20 AM
 *
 * @author Peter Withers
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

    @XmlTransient
    public String getSelectedValue() {
        return selectedValue;
    }

    @XmlTransient
    public String getXpathString() {
        return xpathString;
    }

    public void setXpathString(String xpathString) {
        this.xpathString = xpathString;
    }

    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
    }
}
