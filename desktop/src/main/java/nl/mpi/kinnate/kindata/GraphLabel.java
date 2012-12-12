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
package nl.mpi.kinnate.kindata;

import javax.xml.bind.annotation.XmlAttribute;

/**
 *  Document   : KinTermLabel
 *  Created on : May 25, 2011, 5:09:53 PM
 *  Author     : Peter Withers
 */
public class GraphLabel {

    @XmlAttribute(name = "text", namespace = "http://mpi.nl/tla/kin")
    protected String labelString;
    @XmlAttribute(name = "colour", namespace = "http://mpi.nl/tla/kin")
    protected String colourString = null;

    public GraphLabel() {
    }

    public GraphLabel(String labelStringLocal, String colourStringLocal) {
        labelString = labelStringLocal;
        colourString = colourStringLocal;
    }

    public String getColourString() {
        return colourString;
    }

    public String getLabelString() {
        return labelString;
    }
}
