/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.kinnate.svg;

import nl.mpi.kinnate.kindata.GraphSorter;
import nl.mpi.kinnate.kindata.GraphDataNode;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;
import nl.mpi.arbil.clarin.CmdiComponentBuilder;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.svg.SVGDocument;

/**
 *  Document   : GraphPanel
 *  Created on : Aug 16, 2010, 5:31:33 PM
 *  Author     : Peter Withers
 */
public class GraphPanel1 extends JPanel {

    protected JSVGCanvas svgCanvas = new JSVGCanvas();

    public GraphPanel1() {
        this.setLayout(new BorderLayout());
        drawNodes();
        svgCanvas.setEnableImageZoomInteractor(true);
        svgCanvas.setEnablePanInteractor(true);
        svgCanvas.setEnableRotateInteractor(true);
        svgCanvas.setEnableZoomInteractor(true);
        svgCanvas.setEnableResetTransformInteractor(true);
        svgCanvas.addMouseListener(new MouseInputAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println(e.toString());
                super.mouseClicked(e);
            }
        });
    }

    public void drawNodes() {
        GraphSorter graphData = new GraphSorter();
//        graphData.readData();

        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        SVGDocument doc = (SVGDocument) impl.createDocument(svgNS, "svg", null);
//        Document doc = impl.createDocument(svgNS, "svg", null);
//        SVGDocument doc = svgCanvas.getSVGDocument();
        // Get the root element (the 'svg' elemen¤t).
        Element svgRoot = doc.getDocumentElement();
        // svgRoot.removeAttribute("version");

        int stepNumber = 300;
        int preferedWidth = graphData.gridWidth * stepNumber + stepNumber * 2;

        // Set the width and height attributes on the root 'svg' element.
        svgRoot.setAttributeNS(null, "width", Integer.toString(preferedWidth));
        svgRoot.setAttributeNS(null, "height", Integer.toString(preferedWidth));

        this.setPreferredSize(new Dimension(preferedWidth, preferedWidth));

        svgCanvas.setSVGDocument(doc);
//        svgCanvas.setDocument(doc);
        int counterTest = 0;
        for (GraphDataNode currentNode : graphData.getDataNodes()) {
            counterTest++;
            if (counterTest % 2 > 0) {
                // Create the rectangle.
                Element circle = doc.createElementNS(svgNS, "circle");
                circle.setAttributeNS(null, "cx", Integer.toString(currentNode.getxPos() * stepNumber + stepNumber));
                circle.setAttributeNS(null, "cy", Integer.toString(currentNode.getyPos() * stepNumber + stepNumber));
                circle.setAttributeNS(null, "r", "5");
                circle.setAttributeNS(null, "height", "10");
                circle.setAttributeNS(null, "fill", "red");
                // Attach the rectangle to the root 'svg' element.
                svgRoot.appendChild(circle);
//            <circle id="_16" cx="120.0" cy="155.0" r="50" fill="red" stroke="black" stroke-width="1"/>
//    <polygon id="_17" transform="matrix(0.7457627,0.0,0.0,circle0.6567164,467.339,103.462685)" points="20,10 80,40 40,80" fill="blue" stroke="black" stroke-width="1"/>
            } else {

                // Create the rectangle.
                Element rectangle = doc.createElementNS(svgNS, "rect");
                rectangle.setAttributeNS(null, "x", Integer.toString(currentNode.getxPos() * stepNumber + stepNumber));
                rectangle.setAttributeNS(null, "y", Integer.toString(currentNode.getyPos() * stepNumber + stepNumber));
                rectangle.setAttributeNS(null, "width", "10");
                rectangle.setAttributeNS(null, "height", "10");
                rectangle.setAttributeNS(null, "fill", "red");
                // Attach the rectangle to the root 'svg' element.
                svgRoot.appendChild(rectangle);
            }
            // <text id="_7" x="39.0" y="140.0" fill="black" stroke="black" stroke-width="0" font-size="15">Sample Text</text>
            Element labelText = doc.createElementNS(svgNS, "text");
            labelText.setAttributeNS(null, "x", Integer.toString(currentNode.getxPos() * stepNumber + stepNumber));
            labelText.setAttributeNS(null, "y", Integer.toString(currentNode.getyPos() * stepNumber + stepNumber));
            labelText.setAttributeNS(null, "fill", "black");
            labelText.setAttributeNS(null, "stroke-width", "0");
            labelText.setAttributeNS(null, "font-size", "14");
            //labelText.setNodeValue(currentChild.toString());

            //String textWithUni = "\u0041";
            Text textNode = doc.createTextNode(currentNode.getLabel()[0]);
            labelText.appendChild(textNode);
            svgRoot.appendChild(labelText);

            // draw links
            for (GraphDataNode.EntityRelation graphLinkNode : currentNode.getVisiblyRelateNodes()) {
//                if (graphLinkNode.sourceNode.equals(currentNode)) {
                    System.out.println("link: " + graphLinkNode.getAlterNode().getxPos() + ":" + graphLinkNode.getAlterNode().getyPos());

//                <line id="_15" transform="translate(146.0,112.0)" x1="0" y1="0" x2="100" y2="100" ="black" stroke-width="1"/>
                    Element linkLine = doc.createElementNS(svgNS, "line");
                    linkLine.setAttributeNS(null, "x1", Integer.toString(currentNode.getxPos() * stepNumber + stepNumber));
                    linkLine.setAttributeNS(null, "y1", Integer.toString(currentNode.getyPos() * stepNumber + stepNumber));

                    linkLine.setAttributeNS(null, "x2", Integer.toString(graphLinkNode.getAlterNode().getxPos() * stepNumber + stepNumber));
                    linkLine.setAttributeNS(null, "y2", Integer.toString(graphLinkNode.getAlterNode().getyPos() * stepNumber + stepNumber));
                    linkLine.setAttributeNS(null, "stroke", "black");
                    linkLine.setAttributeNS(null, "stroke-width", "1");
                    // Attach the rectangle to the root 'svg' element.
                    svgRoot.appendChild(linkLine);
//                }
            }
        }
        this.add(BorderLayout.CENTER, svgCanvas);
        new CmdiComponentBuilder().savePrettyFormatting(doc, new File("/Users/petwit/Documents/SharedInVirtualBox/mpi-co-svn-mpi-nl/LAT/Kinnate/trunk/src/main/resources/output1.svg"));
    }
}
