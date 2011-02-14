package nl.mpi.kinnate;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.MouseInputAdapter;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.ImdiTableModel;
import nl.mpi.arbil.clarin.CmdiComponentBuilder;
import nl.mpi.arbil.data.ImdiLoader;
import nl.mpi.kinnate.EntityIndexer.IndexerParameters;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGRect;

/**
 *  Document   : GraphPanel
 *  Created on : Aug 16, 2010, 5:31:33 PM
 *  Author     : Peter Withers
 */
public class GraphPanel extends JPanel {

    private JScrollPane jScrollPane;
    protected JSVGCanvas svgCanvas;
    private SVGDocument doc;
    private Element currentDraggedElement;
    private Cursor preDragCursor;
    HashSet<URI> egoSet = new HashSet<URI>();
    HashSet<String> kinTypeStringSet = new HashSet<String>();
    private IndexerParameters indexParameters;
    private ImdiTableModel imdiTableModel;

    public GraphPanel() {
        indexParameters = new IndexerParameters();
        this.setLayout(new BorderLayout());
        svgCanvas = new JSVGCanvas();
//        svgCanvas.setMySize(new Dimension(600, 400));
        svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_INTERACTIVE); // JSVGCanvas.ALWAYS_DYNAMIC allows more dynamic updating but introduces concurrency issues
//        drawNodes();
        svgCanvas.setEnableImageZoomInteractor(true);
        svgCanvas.setEnablePanInteractor(true);
        svgCanvas.setEnableRotateInteractor(true);
        svgCanvas.setEnableZoomInteractor(true);
        svgCanvas.setEnableResetTransformInteractor(true);

        MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {

            @Override
            public void mouseDragged(MouseEvent me) {
//                System.out.println("mouseDragged: " + me.toString());
                if (currentDraggedElement != null) {
                    currentDraggedElement.setAttribute("x", String.valueOf(me.getX()));
                    currentDraggedElement.setAttribute("y", String.valueOf(me.getY()));
                    svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    SVGRect bbox = ((SVGLocatable) currentDraggedElement).getBBox();
                    System.out.println("bbox X: " + bbox.getX());
                    System.out.println("bbox Y: " + bbox.getY());
                    System.out.println("bbox W: " + bbox.getWidth());
                    System.out.println("bbox H: " + bbox.getHeight());
//                    todo: look into transform issues when dragging ellements eg when the canvas is scaled or panned
//                            SVGLocatable.getTransformToElement()
//                            SVGPoint.matrixTransform()

                }
            }

            @Override
            public void mouseReleased(MouseEvent me) {
//                System.out.println("mouseReleased: " + me.toString());
                if (currentDraggedElement != null) {
                    currentDraggedElement.setAttribute("x", String.valueOf(me.getX()));
                    currentDraggedElement.setAttribute("y", String.valueOf(me.getY()));
                    currentDraggedElement.setAttribute("fill", "none");
                    currentDraggedElement = null;
                    svgCanvas.setCursor(preDragCursor);
                }
            }
        };
        svgCanvas.addMouseListener(mouseInputAdapter);
        svgCanvas.addMouseMotionListener(mouseInputAdapter);
        jScrollPane = new JScrollPane(svgCanvas);
        this.add(BorderLayout.CENTER, jScrollPane);
    }

    public void setImdiTableModel(ImdiTableModel imdiTableModelLocal) {
        imdiTableModel = imdiTableModelLocal;
    }

    public void readSvg(File svgFilePath) {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory documentFactory = new SAXSVGDocumentFactory(parser);
        try {
            doc = (SVGDocument) documentFactory.createDocument(svgFilePath.toURI().toString());
            svgCanvas.setDocument(doc);
        } catch (IOException ioe) {
            GuiHelper.linorgBugCatcher.logError(ioe);
        }
//        svgCanvas.setURI(svgFilePath.toURI().toString());
        getParametersFromDom();
    }

    public void saveSvg(File svgFilePath) {
        new CmdiComponentBuilder().savePrettyFormatting(doc, svgFilePath);
    }

    private void getParametersFromDom() {
        if (doc != null) {
            Element svgRoot = doc.getDocumentElement();
            for (Node currentChild = svgRoot.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
                if ("desc".equals(currentChild.getLocalName())) {
                    Node idAttrubite = currentChild.getAttributes().getNamedItem("id");
                    if (idAttrubite != null) {
                        System.out.println("Desc idAttrubite: " + idAttrubite.getTextContent());
                        if (idAttrubite.getTextContent().equals("EgoList")) {
                            String[] egoPaths = currentChild.getTextContent().split(",");
                            egoSet = new HashSet<URI>();
                            for (String egoPath : egoPaths) {
                                if (egoPath.length() > 0) {
                                    try {
                                        egoSet.add(new URI(egoPath));
                                    } catch (URISyntaxException urise) {
                                        GuiHelper.linorgBugCatcher.logError(urise);
                                    }
                                }
                            }
                        }
                        if (idAttrubite.getTextContent().equals("KinTypeStrings")) {
                            String[] kinTypeStringArray = currentChild.getTextContent().split(",");
                            kinTypeStringSet = new HashSet<String>();
                            for (String kinTypeString : kinTypeStringArray) {
                                if (kinTypeString.length() > 0) {
                                    kinTypeStringSet.add(kinTypeString);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public String[] getKinTypeStrigs() {
        return kinTypeStringSet.toArray(new String[]{});
    }

    public void setKinTypeStrigs(String[] kinTypeStringArray) {
        kinTypeStringSet = new HashSet<String>();
        for (String kinTypeString : kinTypeStringArray) {
            if (kinTypeString != null && kinTypeString.trim().length() > 0) {
                kinTypeStringSet.add(kinTypeString.trim());
            }
        }
    }

    public IndexerParameters getIndexParameters() {
        return indexParameters;
    }

    public URI[] getEgoList() {
        return egoSet.toArray(new URI[]{});
    }

    public void setEgoList(URI[] egoListArray) {
        egoSet = new HashSet<URI>(Arrays.asList(egoListArray));
    }

    private void storeParameter(Element svgRoot, String parameterName, String[] ParameterValues) {
        Element kinTypesRecordNode = doc.createElement("desc");
        kinTypesRecordNode.setAttributeNS(null, "id", parameterName);
        StringBuilder kinTypeRecordBuilder = new StringBuilder();
        for (String currentKinType : ParameterValues) {
            if (kinTypeRecordBuilder.length() > 0) {
                kinTypeRecordBuilder.append(",");
            }
            kinTypeRecordBuilder.append(currentKinType);
        }
        Text kinTypeTextNode = doc.createTextNode(kinTypeRecordBuilder.toString());
        kinTypesRecordNode.appendChild(kinTypeTextNode);
        svgRoot.appendChild(kinTypesRecordNode);
    }

    public void drawNodes(GraphData graphData) {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        doc = (SVGDocument) impl.createDocument(svgNS, "svg", null);
//        Document doc = impl.createDocument(svgNS, "svg", null);
//        SVGDocument doc = svgCanvas.getSVGDocument();
        // Get the root element (the 'svg' elemen¤t).
        Element svgRoot = doc.getDocumentElement();
        // todo: set up a kinnate namespace so that the ego list and kin type strings can have more permanent storage places
        int maxTextLength = 0;
        for (GraphDataNode currentNode : graphData.getDataNodes()) {
            if (currentNode.getLabel()[0].length() > maxTextLength) {
                maxTextLength = currentNode.getLabel()[0].length();
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

        // store the selected ego nodes in the dom
        Element egoRecordNode = doc.createElementNS(svgNS, "desc");
        egoRecordNode.setAttributeNS(null, "id", "EgoList");
        StringBuilder egoRecordBuilder = new StringBuilder();
        for (URI currentEgoUri : egoSet) {
            if (egoRecordBuilder.length() > 0) {
                egoRecordBuilder.append(",");
            }
            egoRecordBuilder.append(currentEgoUri.toASCIIString());
        }
        Text egoTextNode = doc.createTextNode(egoRecordBuilder.toString());
        egoRecordNode.appendChild(egoTextNode);
        svgRoot.appendChild(egoRecordNode);
        // end store the selected ego nodes in the dom
        // store the selected kin type strings in the dom
        Element kinTypesRecordNode = doc.createElementNS(svgNS, "desc");
        kinTypesRecordNode.setAttributeNS(null, "id", "KinTypeStrings");
        StringBuilder kinTypeRecordBuilder = new StringBuilder();
        for (String currentKinType : kinTypeStringSet) {
            if (kinTypeRecordBuilder.length() > 0) {
                kinTypeRecordBuilder.append(",");
            }
            kinTypeRecordBuilder.append(currentKinType);
        }
        Text kinTypeTextNode = doc.createTextNode(kinTypeRecordBuilder.toString());
        kinTypesRecordNode.appendChild(kinTypeTextNode);
        svgRoot.appendChild(kinTypesRecordNode);
        // end store the selected kin type strings nodes in the dom
        storeParameter(svgRoot, "AncestorFields", indexParameters.ancestorFields);
        storeParameter(svgRoot, "DecendantFields", indexParameters.decendantFields);
        storeParameter(svgRoot, "LabelFields", indexParameters.labelFields);
        storeParameter(svgRoot, "SymbolFieldsFields", indexParameters.symbolFieldsFields);

        svgCanvas.setSVGDocument(doc);
//        svgCanvas.setDocument(doc);
        int counterTest = 0;
        for (GraphDataNode currentNode : graphData.getDataNodes()) {
            Element groupNode = doc.createElementNS(svgNS, "g");
            groupNode.setAttributeNS(null, "id", currentNode.getEntityPath());
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
                case resource:
                    symbolNode = doc.createElementNS(svgNS, "rect");
                    symbolNode.setAttributeNS(null, "x", Integer.toString(currentNode.xPos * hSpacing + hSpacing - symbolSize / 2));
                    symbolNode.setAttributeNS(null, "y", Integer.toString(currentNode.yPos * vSpacing + vSpacing - symbolSize / 2));
                    symbolNode.setAttributeNS(null, "width", Integer.toString(symbolSize));
                    symbolNode.setAttributeNS(null, "height", Integer.toString(symbolSize));
                    symbolNode.setAttributeNS(null, "transform", "rotate(-45 " + Integer.toString(currentNode.xPos * hSpacing + hSpacing - symbolSize / 2) + " " + Integer.toString(currentNode.yPos * vSpacing + vSpacing - symbolSize / 2) + ")");
                    symbolNode.setAttributeNS(null, "stroke-width", "4");
                    symbolNode.setAttributeNS(null, "fill", "black");
                    break;
                case union:
//                    DOMUtilities.deepCloneDocument(doc, doc.getImplementation());

//                    symbolNode = doc.createElementNS(svgNS, "layer");
//                    Element upperNode = doc.createElementNS(svgNS, "rect");
//                    Element lowerNode = doc.createElementNS(svgNS, "rect");
//                    upperNode.setAttributeNS(null, "x", Integer.toString(currentNode.xPos * hSpacing + hSpacing - symbolSize / 2));
//                    upperNode.setAttributeNS(null, "y", Integer.toString(currentNode.yPos * vSpacing + vSpacing - symbolSize / 2));
//                    upperNode.setAttributeNS(null, "width", Integer.toString(symbolSize));
//                    upperNode.setAttributeNS(null, "height", Integer.toString(symbolSize / 3));
//                    lowerNode.setAttributeNS(null, "x", Integer.toString(currentNode.xPos * hSpacing + hSpacing - symbolSize / 2 + (symbolSize / 3) * 2));
//                    lowerNode.setAttributeNS(null, "y", Integer.toString(currentNode.yPos * vSpacing + vSpacing - symbolSize / 2));
//                    lowerNode.setAttributeNS(null, "width", Integer.toString(symbolSize));
//                    lowerNode.setAttributeNS(null, "height", Integer.toString(symbolSize / 3));
//                    lowerNode.appendChild(upperNode);
//                    symbolNode.appendChild(lowerNode);
                    symbolNode = doc.createElementNS(svgNS, "polyline");
                    int posXa = currentNode.xPos * hSpacing + hSpacing - symbolSize / 2;
                    int posYa = currentNode.yPos * vSpacing + vSpacing + symbolSize / 2;
                    int offsetAmounta = symbolSize / 2;
                    symbolNode.setAttributeNS(null, "fill", "none");
                    symbolNode.setAttributeNS(null, "points", (posXa + offsetAmounta * 3) + "," + (posYa + offsetAmounta) + " " + (posXa - offsetAmounta) + "," + (posYa + offsetAmounta) + " " + (posXa - offsetAmounta) + "," + (posYa - offsetAmounta) + " " + (posXa + offsetAmounta * 3) + "," + (posYa - offsetAmounta));

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
            groupNode.appendChild(symbolNode);

////////////////////////////// tspan method appears to fail in batik rendering process unless saved and reloaded ////////////////////////////////////////////////
//            Element labelText = doc.createElementNS(svgNS, "text");
////            labelText.setAttributeNS(null, "x", Integer.toString(currentNode.xPos * hSpacing + hSpacing + symbolSize / 2));
////            labelText.setAttributeNS(null, "y", Integer.toString(currentNode.yPos * vSpacing + vSpacing - symbolSize / 2));
//            labelText.setAttributeNS(null, "fill", "black");
//            labelText.setAttributeNS(null, "fill-opacity", "1");
//            labelText.setAttributeNS(null, "stroke-width", "0");
//            labelText.setAttributeNS(null, "font-size", "14px");
////            labelText.setAttributeNS(null, "text-anchor", "end");
////            labelText.setAttributeNS(null, "style", "font-size:14px;text-anchor:end;fill:black;fill-opacity:1");
//            //labelText.setNodeValue(currentChild.toString());
//
//            //String textWithUni = "\u0041";
//            int textSpanCounter = 0;
//            int lineSpacing = 10;
//            for (String currentTextLable : currentNode.getLabel()) {
//                Text textNode = doc.createTextNode(currentTextLable);
//                Element tspanElement = doc.createElement("tspan");
//                tspanElement.setAttribute("x", Integer.toString(currentNode.xPos * hSpacing + hSpacing + symbolSize / 2));
//                tspanElement.setAttribute("y", Integer.toString((currentNode.yPos * vSpacing + vSpacing - symbolSize / 2) + textSpanCounter));
////                tspanElement.setAttribute("y", Integer.toString(textSpanCounter * lineSpacing));
//                tspanElement.appendChild(textNode);
//                labelText.appendChild(tspanElement);
//                textSpanCounter += lineSpacing;
//            }
//            groupNode.appendChild(labelText);
////////////////////////////// end tspan method appears to fail in batik rendering process ////////////////////////////////////////////////

////////////////////////////// alternate method ////////////////////////////////////////////////
            // todo: this method has the draw back that the text is not selectable as a block
            int textSpanCounter = 0;
            int lineSpacing = 15;
            for (String currentTextLable : currentNode.getLabel()) {
                Element labelText = doc.createElementNS(svgNS, "text");
                labelText.setAttributeNS(null, "x", Integer.toString(currentNode.xPos * hSpacing + hSpacing + symbolSize / 2));
                labelText.setAttributeNS(null, "y", Integer.toString(currentNode.yPos * vSpacing + vSpacing - symbolSize / 2 + textSpanCounter));
                labelText.setAttributeNS(null, "fill", "black");
                labelText.setAttributeNS(null, "stroke-width", "0");
                labelText.setAttributeNS(null, "font-size", "14");
                Text textNode = doc.createTextNode(currentTextLable);
                labelText.appendChild(textNode);
                textSpanCounter += lineSpacing;
                groupNode.appendChild(labelText);
            }
////////////////////////////// end alternate method ////////////////////////////////////////////////
            svgRoot.appendChild(groupNode);
            // set up the mouse listners on the group node
            ((EventTarget) groupNode).addEventListener("mouseover", new EventListener() {

                public void handleEvent(Event evt) {
                    System.out.println("OnMouseOverCircleAction: " + evt.getCurrentTarget());
                    if (currentDraggedElement == null) {
                        ((Element) evt.getCurrentTarget()).setAttribute("fill", "green");
                    }
                }
            }, false);
            ((EventTarget) groupNode).addEventListener("mouseout", new EventListener() {

                public void handleEvent(Event evt) {
                    System.out.println("mouseout: " + evt.getCurrentTarget());
                    if (currentDraggedElement == null) {
                        ((Element) evt.getCurrentTarget()).setAttribute("fill", "none");
                    }
                }
            }, false);
            ((EventTarget) groupNode).addEventListener("mousedown", new EventListener() {

                public void handleEvent(Event evt) {
                    System.out.println("mousedrag: " + evt.getCurrentTarget());
                    currentDraggedElement = ((Element) evt.getCurrentTarget());
                    preDragCursor = svgCanvas.getCursor();
                    ((Element) evt.getCurrentTarget()).setAttribute("fill", "red");
                    // get the entityPath
                    String entityPath = currentDraggedElement.getAttribute("id");
                    System.out.println("entityPath: " + entityPath);
                    if (imdiTableModel != null) {
                        imdiTableModel.removeAllImdiRows();
                        try {
                            imdiTableModel.addSingleImdiObject(ImdiLoader.getSingleInstance().getImdiObject(null, new URI(entityPath)));
                        } catch (URISyntaxException urise) {
                            GuiHelper.linorgBugCatcher.logError(urise);
                        }
                    }
                }
            }, false);

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
        //new CmdiComponentBuilder().savePrettyFormatting(doc, new File("/Users/petwit/Documents/SharedInVirtualBox/mpi-co-svn-mpi-nl/LAT/Kinnate/trunk/src/main/resources/output.svg"));
        svgCanvas.revalidate();
    }
}
