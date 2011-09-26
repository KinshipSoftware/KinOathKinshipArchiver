package nl.mpi.kinnate.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import org.w3c.dom.Element;

/**
 *  Document   : SvgElementEditor
 *  Created on : Aug 17, 2011, 1:17:13 PM
 *  Author     : Peter Withers
 */
public class SvgElementEditor extends JPanel {

    public SvgElementEditor(Element svgElement) {
        this.add(new JLabel("SvgElementEditor"));
        // todo: Ticket #1065 Enable editing of the text and font size of labels in the diagram.
    }
}
