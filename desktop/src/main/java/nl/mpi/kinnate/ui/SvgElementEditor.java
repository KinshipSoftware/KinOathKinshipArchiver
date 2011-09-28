package nl.mpi.kinnate.ui;

import java.awt.GridLayout;
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
        this.setLayout(new GridLayout(0, 2));
        this.add(new JLabel("SvgElementEditor"));
        this.add(new JLabel(svgElement.getTagName()));
        this.add(new JLabel("Label Text:"));
        this.add(new JLabel(svgElement.getTextContent()));
        for (String attribiteName : new String[]{"fill", "font-size", "stroke-width"}) {
            this.add(new JLabel(attribiteName));
            this.add(new JLabel(svgElement.getAttribute(attribiteName)));
        }
        // todo: Ticket #1065 Enable editing of the text and font size of labels in the diagram.
    }
}
