package nl.mpi.kinnate;

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
public class GraphPanel extends JPanel {

    protected JSVGCanvas svgCanvas = new JSVGCanvas();

    public GraphPanel() {
        this.setLayout(new BorderLayout());
//        drawNodes();
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
        this.add(BorderLayout.CENTER, svgCanvas);
    }

    public void drawNodes(GraphData graphData) {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        SVGDocument doc = (SVGDocument) impl.createDocument(svgNS, "svg", null);
//        Document doc = impl.createDocument(svgNS, "svg", null);
//        SVGDocument doc = svgCanvas.getSVGDocument();
        // Get the root element (the 'svg' elemen¤t).
        Element svgRoot = doc.getDocumentElement();
        // svgRoot.removeAttribute("version");
        int maxTextLength = 0;
        for (GraphDataNode currentNode : graphData.getDataNodes()) {
            if (currentNode.getLabel().length() > maxTextLength) {
                maxTextLength = currentNode.getLabel().length();
            }
        }
        int vSpacing = 100;
        // todo: find the real text size from batik
        int hSpacing = maxTextLength * 10 + 100;
        int symbolSize = 10;
        int strokeWidth = 1;

        int preferedWidth = graphData.gridWidth * hSpacing + hSpacing * 2;
        int preferedHeight = graphData.gridHeight * vSpacing + vSpacing * 2;

        // Set the width and height attributes on the root 'svg' element.
        svgRoot.setAttributeNS(null, "width", Integer.toString(preferedWidth));
        svgRoot.setAttributeNS(null, "height", Integer.toString(preferedHeight));

        this.setPreferredSize(new Dimension(preferedWidth, preferedWidth));

        svgCanvas.setSVGDocument(doc);
//        svgCanvas.setDocument(doc);
        int counterTest = 0;
        for (GraphDataNode currentNode : graphData.getDataNodes()) {
            counterTest++;
            Element symbolNode;
            switch (currentNode.symbolType) {
                case circle:
                    symbolNode = doc.createElementNS(svgNS, "circle");
                    symbolNode.setAttributeNS(null, "cx", Integer.toString(currentNode.xPos * hSpacing + hSpacing));
                    symbolNode.setAttributeNS(null, "cy", Integer.toString(currentNode.yPos * vSpacing + vSpacing));
                    symbolNode.setAttributeNS(null, "r", Integer.toString(symbolSize / 2));
                    symbolNode.setAttributeNS(null, "height", Integer.toString(symbolSize));
//            <circle id="_16" cx="120.0" cy="155.0" r="50" fill="red" stroke="black" stroke-width="1"/>
//    <polygon id="_17" transform="matrix(0.7457627,0.0,0.0,circle0.6567164,467.339,103.462685)" points="20,10 80,40 40,80" fill="blue" stroke="black" stroke-width="1"/>
                    break;
                case square:
                    symbolNode = doc.createElementNS(svgNS, "rect");
                    symbolNode.setAttributeNS(null, "x", Integer.toString(currentNode.xPos * hSpacing + hSpacing - symbolSize / 2));
                    symbolNode.setAttributeNS(null, "y", Integer.toString(currentNode.yPos * vSpacing + vSpacing - symbolSize / 2));
                    symbolNode.setAttributeNS(null, "width", Integer.toString(symbolSize));
                    symbolNode.setAttributeNS(null, "height", Integer.toString(symbolSize));
                    break;
                case triangle:
                    symbolNode = doc.createElementNS(svgNS, "polygon");
                    int posXt = currentNode.xPos * hSpacing + hSpacing;
                    int posYt = currentNode.yPos * vSpacing + vSpacing;
                    int triangleHeight = (int) (Math.sqrt(3) * symbolSize / 2);
                    symbolNode.setAttributeNS(null, "points",
                            (posXt - symbolSize / 2) + "," + (posYt + triangleHeight / 2) + " "
                            + (posXt) + "," + (posYt - +triangleHeight / 2) + " "
                            + (posXt + symbolSize / 2) + "," + (posYt + triangleHeight / 2));
                    break;
//                case equals:
//                    symbolNode = doc.createElementNS(svgNS, "rect");
//                    symbolNode.setAttributeNS(null, "x", Integer.toString(currentNode.xPos * stepNumber + stepNumber - symbolSize));
//                    symbolNode.setAttributeNS(null, "y", Integer.toString(currentNode.yPos * stepNumber + stepNumber));
//                    symbolNode.setAttributeNS(null, "width", Integer.toString(symbolSize / 2));
//                    symbolNode.setAttributeNS(null, "height", Integer.toString(symbolSize / 2));
//                    break;
                default:
                    symbolNode = doc.createElementNS(svgNS, "polyline");
                    int posX = currentNode.xPos * hSpacing + hSpacing - symbolSize / 2;
                    int posY = currentNode.yPos * vSpacing + vSpacing + symbolSize / 2;
                    int offsetAmount = symbolSize / 2;
                    symbolNode.setAttributeNS(null, "fill", "none");
                    symbolNode.setAttributeNS(null, "points", (posX - offsetAmount) + "," + (posY - offsetAmount) + " " + (posX + offsetAmount) + "," + (posY + offsetAmount) + " " + (posX) + "," + (posY) + " " + (posX - offsetAmount) + "," + (posY + offsetAmount) + " " + (posX + offsetAmount) + "," + (posY - offsetAmount));
            }
            if (currentNode.isEgo) {
                symbolNode.setAttributeNS(null, "fill", "red");
            } else {
                symbolNode.setAttributeNS(null, "fill", "none");
            }

            symbolNode.setAttributeNS(null, "stroke", "black");
            symbolNode.setAttributeNS(null, "stroke-width", "2");
            // Attach the rectangle to the root 'svg' element.
            svgRoot.appendChild(symbolNode);
            // <text id="_7" x="39.0" y="140.0" fill="black" stroke="black" stroke-width="0" font-size="15">Sample Text</text>
            Element labelText = doc.createElementNS(svgNS, "text");
            labelText.setAttributeNS(null, "x", Integer.toString(currentNode.xPos * hSpacing + hSpacing + symbolSize / 2));
            labelText.setAttributeNS(null, "y", Integer.toString(currentNode.yPos * vSpacing + vSpacing - symbolSize / 2));
            labelText.setAttributeNS(null, "fill", "black");
            labelText.setAttributeNS(null, "stroke-width", "0");
            labelText.setAttributeNS(null, "font-size", "14");
            //labelText.setNodeValue(currentChild.toString());

            //String textWithUni = "\u0041";
            Text textNode = doc.createTextNode(currentNode.getLabel());
            labelText.appendChild(textNode);
            svgRoot.appendChild(labelText);

            // draw links
            for (GraphDataNode.NodeRelation graphLinkNode : currentNode.getNodeRelations()) {
                if (graphLinkNode.sourceNode.equals(currentNode)) {
                    System.out.println("link: " + graphLinkNode.linkedNode.xPos + ":" + graphLinkNode.linkedNode.yPos);

//                <line id="_15" transform="translate(146.0,112.0)" x1="0" y1="0" x2="100" y2="100" ="black" stroke-width="1"/>
                    Element linkLine = doc.createElementNS(svgNS, "line");
                    linkLine.setAttributeNS(null, "x1", Integer.toString(currentNode.xPos * hSpacing + hSpacing));
                    linkLine.setAttributeNS(null, "y1", Integer.toString(currentNode.yPos * vSpacing + vSpacing));

                    linkLine.setAttributeNS(null, "x2", Integer.toString(graphLinkNode.linkedNode.xPos * hSpacing + hSpacing));
                    linkLine.setAttributeNS(null, "y2", Integer.toString(graphLinkNode.linkedNode.yPos * vSpacing + vSpacing));
                    linkLine.setAttributeNS(null, "stroke", "black");
                    linkLine.setAttributeNS(null, "stroke-width", "1");
                    // Attach the rectangle to the root 'svg' element.
                    svgRoot.appendChild(linkLine);
                }
            }
        }
        new CmdiComponentBuilder().savePrettyFormatting(doc, new File("/Users/petwit/Documents/SharedInVirtualBox/mpi-co-svn-mpi-nl/LAT/Kinnate/trunk/src/main/resources/output.svg"));
    }
}
