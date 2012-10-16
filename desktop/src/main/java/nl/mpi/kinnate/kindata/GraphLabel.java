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
