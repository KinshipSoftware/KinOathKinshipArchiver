package nl.mpi.kinnate.svg;

import nl.mpi.kinnate.ui.GraphPanelContextMenu;
import nl.mpi.kinnate.ui.KinTypeEgoSelectionTestPanel;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.ImdiTableModel;
import nl.mpi.arbil.clarin.CmdiComponentBuilder;
import nl.mpi.arbil.data.ImdiLoader;
import nl.mpi.kinnate.entityindexer.IndexerParameters;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.kintypestrings.KinTerms;
import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.dom.events.DOMMouseEvent;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
public class GraphPanel extends JPanel implements SavePanel {

    private JSVGScrollPane jSVGScrollPane;
    private JSVGCanvas svgCanvas;
    private SVGDocument doc;
    private Element currentDraggedElement;
    private Cursor preDragCursor;
    private HashSet<URI> egoSet = new HashSet<URI>();
    private String[] kinTypeStrings = new String[]{};
    private IndexerParameters indexParameters;
    private KinTerms kinTerms;
    private ImdiTableModel imdiTableModel;
    private GraphData graphData;
    private boolean requiresSave = false;
    private File svgFile = null;
    private GraphPanelSize graphPanelSize;
    private ArrayList<String> selectedGroupElement;
    private String svgNameSpace = SVGDOMImplementation.SVG_NAMESPACE_URI;
    private String kinDataNameSpace = "kin";
    private String kinDataNameSpaceLocation = "http://mpi.nl/tla/kin";

    public GraphPanel(KinTypeEgoSelectionTestPanel egoSelectionPanel) {
        selectedGroupElement = new ArrayList<String>();
        graphPanelSize = new GraphPanelSize();
        indexParameters = new IndexerParameters();
        kinTerms = new KinTerms();
        this.setLayout(new BorderLayout());
        svgCanvas = new JSVGCanvas();
//        svgCanvas.setMySize(new Dimension(600, 400));
        svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
//        drawNodes();
        svgCanvas.setEnableImageZoomInteractor(false);
        svgCanvas.setEnablePanInteractor(false);
        svgCanvas.setEnableRotateInteractor(false);
        svgCanvas.setEnableZoomInteractor(false);
        svgCanvas.addMouseWheelListener(new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent e) {
                AffineTransform at = new AffineTransform();
//                System.out.println("R: " + e.getWheelRotation());
//                System.out.println("A: " + e.getScrollAmount());
//                System.out.println("U: " + e.getUnitsToScroll());
                at.scale(1 + e.getUnitsToScroll() / 10.0, 1 + e.getUnitsToScroll() / 10.0);
//                at.translate(e.getX()/10.0, e.getY()/10.0);
//                System.out.println("x: " + e.getX());
//                System.out.println("y: " + e.getY());

                at.concatenate(svgCanvas.getRenderingTransform());
                svgCanvas.setRenderingTransform(at);
            }
        });
//        svgCanvas.setEnableResetTransformInteractor(true);
//        svgCanvas.setDoubleBufferedRendering(true); // todo: look into reducing the noticable aliasing on the canvas

        MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {

            @Override
            public void mouseDragged(MouseEvent me) {
//                System.out.println("mouseDragged: " + me.toString());
                if (currentDraggedElement != null) {
                    svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    updateDragNode(currentDraggedElement, me.getX(), me.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent me) {
//                System.out.println("mouseReleased: " + me.toString());
                if (currentDraggedElement != null) {
                    svgCanvas.setCursor(preDragCursor);
                    updateDragNode(currentDraggedElement, me.getX(), me.getY());
                    currentDraggedElement = null;
                }
            }
        };
        svgCanvas.addMouseListener(mouseInputAdapter);
        svgCanvas.addMouseMotionListener(mouseInputAdapter);
        jSVGScrollPane = new JSVGScrollPane(svgCanvas);
        this.add(BorderLayout.CENTER, jSVGScrollPane);
        svgCanvas.setComponentPopupMenu(new GraphPanelContextMenu(egoSelectionPanel, this, graphPanelSize));
    }

    public void setImdiTableModel(ImdiTableModel imdiTableModelLocal) {
        imdiTableModel = imdiTableModelLocal;
    }

    public void readSvg(File svgFilePath) {
        svgFile = svgFilePath;
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory documentFactory = new SAXSVGDocumentFactory(parser);
        try {
            doc = (SVGDocument) documentFactory.createDocument(svgFilePath.toURI().toString());
            svgCanvas.setDocument(doc);
            requiresSave = false;
        } catch (IOException ioe) {
            GuiHelper.linorgBugCatcher.logError(ioe);
        }
//        svgCanvas.setURI(svgFilePath.toURI().toString());
        ArrayList<String> egoStringArray = new ArrayList<String>();
        egoSet.clear();
        for (String currentEgoString : getSingleParametersFromDom("EgoList")) {
            try {
                egoSet.add(new URI(currentEgoString));
            } catch (URISyntaxException urise) {
                GuiHelper.linorgBugCatcher.logError(urise);
            }
        }
        kinTypeStrings = getSingleParametersFromDom("KinTypeStrings");
        indexParameters.ancestorFields.setValues(getDoubleParametersFromDom("AncestorFields"));
        indexParameters.decendantFields.setValues(getDoubleParametersFromDom("DecendantFields"));
        indexParameters.labelFields.setValues(getDoubleParametersFromDom("LabelFields"));
        indexParameters.symbolFieldsFields.setValues(getDoubleParametersFromDom("SymbolFieldsFields"));
    }

    private void saveSvg(File svgFilePath) {
        svgFile = svgFilePath;
        new CmdiComponentBuilder().savePrettyFormatting(doc, svgFilePath);
        requiresSave = false;
    }

    private void printNodeNames(Node nodeElement) {
        System.out.println(nodeElement.getLocalName());
        System.out.println(nodeElement.getNamespaceURI());
        Node childNode = nodeElement.getFirstChild();
        while (childNode != null) {
            printNodeNames(childNode);
            childNode = childNode.getNextSibling();
        }
    }

    private String[] getSingleParametersFromDom(String parameterName) {
        ArrayList<String> parameterList = new ArrayList<String>();
        if (doc != null) {
//            printNodeNames(doc);
            try {
                // todo: resolve names space issue
                // todo: try setting the XPath namespaces
                NodeList parameterNodeList = org.apache.xpath.XPathAPI.selectNodeList(doc, "/svg:svg/kin:KinDiagramData/kin:" + parameterName);
                for (int nodeCounter = 0; nodeCounter < parameterNodeList.getLength(); nodeCounter++) {
                    Node parameterNode = parameterNodeList.item(nodeCounter);
                    if (parameterNode != null) {
                        parameterList.add(parameterNode.getAttributes().getNamedItem("value").getNodeValue());
                    }
                }
            } catch (TransformerException transformerException) {
                GuiHelper.linorgBugCatcher.logError(transformerException);
            }
//            // todo: populate the avaiable symbols indexParameters.symbolFieldsFields.setAvailableValues(new String[]{"circle", "triangle", "square", "union"});
        }
        return parameterList.toArray(new String[]{});
    }

    private String[][] getDoubleParametersFromDom(String parameterName) {
        ArrayList<String[]> parameterList = new ArrayList<String[]>();
        if (doc != null) {
            try {
                // todo: resolve names space issue
                NodeList parameterNodeList = org.apache.xpath.XPathAPI.selectNodeList(doc, "/svg/KinDiagramData/" + parameterName);
                for (int nodeCounter = 0; nodeCounter < parameterNodeList.getLength(); nodeCounter++) {
                    Node parameterNode = parameterNodeList.item(nodeCounter);
                    if (parameterNode != null) {
                        parameterList.add(new String[]{parameterNode.getAttributes().getNamedItem("path").getNodeValue(), parameterNode.getAttributes().getNamedItem("value").getNodeValue()});
                    }
                }
            } catch (TransformerException transformerException) {
                GuiHelper.linorgBugCatcher.logError(transformerException);
            }
//            // todo: populate the avaiable symbols indexParameters.symbolFieldsFields.setAvailableValues(new String[]{"circle", "triangle", "square", "union"});
        }
        return parameterList.toArray(new String[][]{});
    }

    public String[] getKinTypeStrigs() {
        return kinTypeStrings;
    }

    public void setKinTypeStrigs(String[] kinTypeStringArray) {
        // strip out any white space, blank lines and remove duplicates
        HashSet<String> kinTypeStringSet = new HashSet<String>();
        for (String kinTypeString : kinTypeStringArray) {
            if (kinTypeString != null && kinTypeString.trim().length() > 0) {
                kinTypeStringSet.add(kinTypeString.trim());
            }
        }
        kinTypeStrings = kinTypeStringSet.toArray(new String[]{});
    }

    public IndexerParameters getIndexParameters() {
        return indexParameters;
    }

    public KinTerms getkinTerms() {
        return kinTerms;
    }

    public URI[] getEgoList() {
        return egoSet.toArray(new URI[]{});
    }

    public void setEgoList(URI[] egoListArray) {
        egoSet = new HashSet<URI>(Arrays.asList(egoListArray));
    }

    public String[] getSelectedPaths() {
        return selectedGroupElement.toArray(new String[]{});
    }

    private void storeParameter(Element dataStoreElement, String parameterName, String[] ParameterValues) {
        for (String currentKinType : ParameterValues) {
            Element dataRecordNode = doc.createElementNS(kinDataNameSpace, parameterName);
//            Element dataRecordNode = doc.createElement(kinDataNameSpace + ":" + parameterName);
            dataRecordNode.setAttributeNS(kinDataNameSpace, "value", currentKinType);
            dataStoreElement.appendChild(dataRecordNode);
        }
    }

    private void storeParameter(Element dataStoreElement, String parameterName, String[][] ParameterValues) {
        for (String[] currentKinType : ParameterValues) {
            Element dataRecordNode = doc.createElementNS(kinDataNameSpace, parameterName);
//            Element dataRecordNode = doc.createElement(kinDataNameSpace + ":" + parameterName);
            if (currentKinType.length == 1) {
                dataRecordNode.setAttributeNS(kinDataNameSpace, "value", currentKinType[0]);
            } else if (currentKinType.length == 2) {
                dataRecordNode.setAttributeNS(kinDataNameSpace, "path", currentKinType[0]);
                dataRecordNode.setAttributeNS(kinDataNameSpace, "value", currentKinType[1]);
            } else {
                // todo: add any other datatypes if required
                throw new UnsupportedOperationException();
            }
            dataStoreElement.appendChild(dataRecordNode);
        }
    }

    public void resetZoom() {
        AffineTransform at = new AffineTransform();
        at.scale(1, 1);
        at.setToTranslation(1, 1);
        svgCanvas.setRenderingTransform(at);
    }

    public void drawNodes() {
        drawNodes(graphData);
    }

    private void updateDragNode(final Element updateDragNodeElement, final int updateDragNodeX, final int updateDragNodeY) {
//        UpdateManager updateManager = svgCanvas.getUpdateManager();
//        updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
//
//            public void run() {
//                if (updateDragNodeElement != null) {
//                    updateDragNodeElement.setAttribute("x", String.valueOf(updateDragNodeX));
//                    updateDragNodeElement.setAttribute("y", String.valueOf(updateDragNodeY));
//                }
//                //                    SVGRect bbox = ((SVGLocatable) currentDraggedElement).getBBox();
////                    System.out.println("bbox X: " + bbox.getX());
////                    System.out.println("bbox Y: " + bbox.getY());
////                    System.out.println("bbox W: " + bbox.getWidth());
////                    System.out.println("bbox H: " + bbox.getHeight());
////                    todo: look into transform issues when dragging ellements eg when the canvas is scaled or panned
////                            SVGLocatable.getTransformToElement()
////                            SVGPoint.matrixTransform()
//            }
//        });
    }

    private void addHighlightToGroup() {
        UpdateManager updateManager = svgCanvas.getUpdateManager();
        updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {

            public void run() {
                if (doc != null) {
                    Element svgRoot = doc.getDocumentElement();
                    for (Node currentChild = svgRoot.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
                        if ("g".equals(currentChild.getLocalName())) {
                            Node idAttrubite = currentChild.getAttributes().getNamedItem("id");
                            if (idAttrubite != null) {
                                String entityPath = idAttrubite.getTextContent();
                                System.out.println("group id (entityPath): " + entityPath);
                                Node existingHighlight = null;
                                // find any existing highlight
                                for (Node subGoupNode = currentChild.getFirstChild(); subGoupNode != null; subGoupNode = subGoupNode.getNextSibling()) {
                                    if ("rect".equals(subGoupNode.getLocalName())) {
                                        Node subGroupIdAttrubite = subGoupNode.getAttributes().getNamedItem("id");
                                        if (subGroupIdAttrubite != null) {
                                            if ("highlight".equals(subGroupIdAttrubite.getTextContent())) {
                                                existingHighlight = subGoupNode;
                                            }
                                        }
                                    }
                                }
                                if (!selectedGroupElement.contains(entityPath)) {
                                    // remove all old highlights
                                    if (existingHighlight != null) {
                                        currentChild.removeChild(existingHighlight);
                                    }
                                    // add the current highlights
                                } else {
                                    if (existingHighlight == null) {
                                        svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                                        SVGRect bbox = ((SVGLocatable) currentChild).getBBox();
                                        System.out.println("bbox X: " + bbox.getX());
                                        System.out.println("bbox Y: " + bbox.getY());
                                        System.out.println("bbox W: " + bbox.getWidth());
                                        System.out.println("bbox H: " + bbox.getHeight());
                                        Element symbolNode = doc.createElementNS(svgNameSpace, "rect");
                                        int paddingDistance = 20;
                                        symbolNode.setAttribute("id", "highlight");
                                        symbolNode.setAttribute("x", Float.toString(bbox.getX() - paddingDistance));
                                        symbolNode.setAttribute("y", Float.toString(bbox.getY() - paddingDistance));
                                        symbolNode.setAttribute("width", Float.toString(bbox.getWidth() + paddingDistance * 2));
                                        symbolNode.setAttribute("height", Float.toString(bbox.getHeight() + paddingDistance * 2));
                                        symbolNode.setAttribute("fill", "none");
                                        symbolNode.setAttribute("stroke-width", "1");
                                        symbolNode.setAttribute("stroke", "blue");
                                        symbolNode.setAttribute("stroke-dasharray", "3");
                                        symbolNode.setAttribute("stroke-dashoffset", "0");
//            symbolNode.setAttribute("id", "Highlight");
//            symbolNode.setAttribute("id", "Highlight");
//            symbolNode.setAttribute("id", "Highlight");
//            symbolNode.setAttribute("style", ":none;fill-opacity:1;fill-rule:nonzero;stroke:#6674ff;stroke-opacity:1;stroke-width:1;stroke-miterlimit:4;"
//                    + "stroke-dasharray:1, 1;stroke-dashoffset:0");
                                        currentChild.appendChild(symbolNode);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private Element createEntitySymbol(GraphDataNode currentNode, int hSpacing, int vSpacing, int symbolSize) {
        Element groupNode = doc.createElementNS(svgNameSpace, "g");
        groupNode.setAttribute("id", currentNode.getEntityPath());
//        counterTest++;
        Element symbolNode;
        String symbolType = currentNode.getSymbolType();
        if ("circle".equals(symbolType)) {
            symbolNode = doc.createElementNS(svgNameSpace, "circle");
            symbolNode.setAttribute("cx", Integer.toString(currentNode.xPos * hSpacing + hSpacing));
            symbolNode.setAttribute("cy", Integer.toString(currentNode.yPos * vSpacing + vSpacing));
            symbolNode.setAttribute("r", Integer.toString(symbolSize / 2));
            symbolNode.setAttribute("height", Integer.toString(symbolSize));
//            <circle id="_16" cx="120.0" cy="155.0" r="50" fill="red" stroke="black" stroke-width="1"/>
//    <polygon id="_17" transform="matrix(0.7457627,0.0,0.0,circle0.6567164,467.339,103.462685)" points="20,10 80,40 40,80" fill="blue" stroke="black" stroke-width="1"/>
        } else if ("square".equals(symbolType)) {
            symbolNode = doc.createElementNS(svgNameSpace, "rect");
            symbolNode.setAttribute("x", Integer.toString(currentNode.xPos * hSpacing + hSpacing - symbolSize / 2));
            symbolNode.setAttribute("y", Integer.toString(currentNode.yPos * vSpacing + vSpacing - symbolSize / 2));
            symbolNode.setAttribute("width", Integer.toString(symbolSize));
            symbolNode.setAttribute("height", Integer.toString(symbolSize));
        } else if ("resource".equals(symbolType)) {
            symbolNode = doc.createElementNS(svgNameSpace, "rect");
            symbolNode.setAttribute("x", Integer.toString(currentNode.xPos * hSpacing + hSpacing - symbolSize / 2));
            symbolNode.setAttribute("y", Integer.toString(currentNode.yPos * vSpacing + vSpacing - symbolSize / 2));
            symbolNode.setAttribute("width", Integer.toString(symbolSize));
            symbolNode.setAttribute("height", Integer.toString(symbolSize));
            symbolNode.setAttribute("transform", "rotate(-45 " + Integer.toString(currentNode.xPos * hSpacing + hSpacing - symbolSize / 2) + " " + Integer.toString(currentNode.yPos * vSpacing + vSpacing - symbolSize / 2) + ")");
            symbolNode.setAttribute("stroke-width", "4");
            symbolNode.setAttribute("fill", "black");
        } else if ("union".equals(symbolType)) {
//                    DOMUtilities.deepCloneDocument(doc, doc.getImplementation());

//                    symbolNode = doc.createElementNS(svgNS, "layer");
//                    Element upperNode = doc.createElementNS(svgNS, "rect");
//                    Element lowerNode = doc.createElementNS(svgNS, "rect");
//                    upperNode.setAttribute("x", Integer.toString(currentNode.xPos * hSpacing + hSpacing - symbolSize / 2));
//                    upperNode.setAttribute("y", Integer.toString(currentNode.yPos * vSpacing + vSpacing - symbolSize / 2));
//                    upperNode.setAttribute("width", Integer.toString(symbolSize));
//                    upperNode.setAttribute("height", Integer.toString(symbolSize / 3));
//                    lowerNode.setAttribute("x", Integer.toString(currentNode.xPos * hSpacing + hSpacing - symbolSize / 2 + (symbolSize / 3) * 2));
//                    lowerNode.setAttribute("y", Integer.toString(currentNode.yPos * vSpacing + vSpacing - symbolSize / 2));
//                    lowerNode.setAttribute("width", Integer.toString(symbolSize));
//                    lowerNode.setAttribute("height", Integer.toString(symbolSize / 3));
//                    lowerNode.appendChild(upperNode);
//                    symbolNode.appendChild(lowerNode);
            symbolNode = doc.createElementNS(svgNameSpace, "polyline");
            int posXa = currentNode.xPos * hSpacing + hSpacing - symbolSize / 2;
            int posYa = currentNode.yPos * vSpacing + vSpacing + symbolSize / 2;
            int offsetAmounta = symbolSize / 2;
            symbolNode.setAttribute("fill", "none");
            symbolNode.setAttribute("points", (posXa + offsetAmounta * 3) + "," + (posYa + offsetAmounta) + " " + (posXa - offsetAmounta) + "," + (posYa + offsetAmounta) + " " + (posXa - offsetAmounta) + "," + (posYa - offsetAmounta) + " " + (posXa + offsetAmounta * 3) + "," + (posYa - offsetAmounta));
        } else if ("triangle".equals(symbolType)) {
            symbolNode = doc.createElementNS(svgNameSpace, "polygon");
            int posXt = currentNode.xPos * hSpacing + hSpacing;
            int posYt = currentNode.yPos * vSpacing + vSpacing;
            int triangleHeight = (int) (Math.sqrt(3) * symbolSize / 2);
            symbolNode.setAttribute("points",
                    (posXt - symbolSize / 2) + "," + (posYt + triangleHeight / 2) + " "
                    + (posXt) + "," + (posYt - +triangleHeight / 2) + " "
                    + (posXt + symbolSize / 2) + "," + (posYt + triangleHeight / 2));
//                case equals:
//                    symbolNode = doc.createElementNS(svgNS, "rect");
//                    symbolNode.setAttribute("x", Integer.toString(currentNode.xPos * stepNumber + stepNumber - symbolSize));
//                    symbolNode.setAttribute("y", Integer.toString(currentNode.yPos * stepNumber + stepNumber));
//                    symbolNode.setAttribute("width", Integer.toString(symbolSize / 2));
//                    symbolNode.setAttribute("height", Integer.toString(symbolSize / 2));
//                    break;
        } else {
            symbolNode = doc.createElementNS(svgNameSpace, "polyline");
            int posX = currentNode.xPos * hSpacing + hSpacing - symbolSize / 2;
            int posY = currentNode.yPos * vSpacing + vSpacing + symbolSize / 2;
            int offsetAmount = symbolSize / 2;
            symbolNode.setAttribute("fill", "none");
            symbolNode.setAttribute("points", (posX - offsetAmount) + "," + (posY - offsetAmount) + " " + (posX + offsetAmount) + "," + (posY + offsetAmount) + " " + (posX) + "," + (posY) + " " + (posX - offsetAmount) + "," + (posY + offsetAmount) + " " + (posX + offsetAmount) + "," + (posY - offsetAmount));
        }
//            if (currentNode.isEgo) {
//                symbolNode.setAttribute("fill", "red");
//            } else {
//                symbolNode.setAttribute("fill", "none");
//            }
        if (currentNode.isEgo) {
            symbolNode.setAttribute("fill", "black");
        } else {
            symbolNode.setAttribute("fill", "white");
        }

        symbolNode.setAttribute("stroke", "black");
        symbolNode.setAttribute("stroke-width", "2");
        groupNode.appendChild(symbolNode);

////////////////////////////// tspan method appears to fail in batik rendering process unless saved and reloaded ////////////////////////////////////////////////
//            Element labelText = doc.createElementNS(svgNS, "text");
////            labelText.setAttribute("x", Integer.toString(currentNode.xPos * hSpacing + hSpacing + symbolSize / 2));
////            labelText.setAttribute("y", Integer.toString(currentNode.yPos * vSpacing + vSpacing - symbolSize / 2));
//            labelText.setAttribute("fill", "black");
//            labelText.setAttribute("fill-opacity", "1");
//            labelText.setAttribute("stroke-width", "0");
//            labelText.setAttribute("font-size", "14px");
////            labelText.setAttribute("text-anchor", "end");
////            labelText.setAttribute("style", "font-size:14px;text-anchor:end;fill:black;fill-opacity:1");
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
            Element labelText = doc.createElementNS(svgNameSpace, "text");
            labelText.setAttribute("x", Integer.toString(currentNode.xPos * hSpacing + hSpacing + symbolSize / 2));
            labelText.setAttribute("y", Integer.toString(currentNode.yPos * vSpacing + vSpacing - symbolSize / 2 + textSpanCounter));
            labelText.setAttribute("fill", "black");
            labelText.setAttribute("stroke-width", "0");
            labelText.setAttribute("font-size", "14");
            Text textNode = doc.createTextNode(currentTextLable);
            labelText.appendChild(textNode);
            textSpanCounter += lineSpacing;
            groupNode.appendChild(labelText);
        }
////////////////////////////// end alternate method ////////////////////////////////////////////////
        ((EventTarget) groupNode).addEventListener("mousedown", new EventListener() {

            public void handleEvent(Event evt) {
                boolean shiftDown = false;
                if (evt instanceof DOMMouseEvent) {
                    shiftDown = ((DOMMouseEvent) evt).getShiftKey();
                }
                System.out.println("mousedown: " + evt.getCurrentTarget());
                currentDraggedElement = ((Element) evt.getCurrentTarget());
                preDragCursor = svgCanvas.getCursor();
                // get the entityPath
                String entityPath = currentDraggedElement.getAttribute("id");
                System.out.println("entityPath: " + entityPath);
                boolean nodeAlreadySelected = selectedGroupElement.contains(entityPath);
                if (!shiftDown) {
                    System.out.println("Clear selection");
                    selectedGroupElement.clear();
                }
                // toggle the highlight
                if (nodeAlreadySelected) {
                    selectedGroupElement.remove(entityPath);
                } else {
                    selectedGroupElement.add(entityPath);
                }
                addHighlightToGroup();
                // update the table selection
                if (imdiTableModel != null) {
                    imdiTableModel.removeAllImdiRows();
                    try {
                        for (String currentSelectedPath : selectedGroupElement) {
                            imdiTableModel.addSingleImdiObject(ImdiLoader.getSingleInstance().getImdiObject(null, new URI(currentSelectedPath)));
                        }
                    } catch (URISyntaxException urise) {
                        GuiHelper.linorgBugCatcher.logError(urise);
                    }
                }
            }
        }, false);
        return groupNode;
    }

    public void drawNodes(GraphData graphDataLocal) {
        requiresSave = true;
        graphData = graphDataLocal;
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        doc = (SVGDocument) impl.createDocument(svgNameSpace, "svg", null);
//        Document doc = impl.createDocument(svgNS, "svg", null);
//        SVGDocument doc = svgCanvas.getSVGDocument();
        // Get the root element (the 'svg' elemen¤t).
        Element svgRoot = doc.getDocumentElement();
        // todo: set up a kinnate namespace so that the ego list and kin type strings can have more permanent storage places
//        int maxTextLength = 0;
//        for (GraphDataNode currentNode : graphData.getDataNodes()) {
//            if (currentNode.getLabel()[0].length() > maxTextLength) {
//                maxTextLength = currentNode.getLabel()[0].length();
//            }
//        }
        int vSpacing = graphPanelSize.getVerticalSpacing(graphData.gridHeight);
        // todo: find the real text size from batik
        // todo: get the user selected canvas size and adjust the hSpacing and vSpacing to fit
//        int hSpacing = maxTextLength * 10 + 100;
        int hSpacing = graphPanelSize.getHorizontalSpacing(graphData.gridWidth);
        int symbolSize = 15;
        int strokeWidth = 2;

//        int preferedWidth = graphData.gridWidth * hSpacing + hSpacing * 2;
//        int preferedHeight = graphData.gridHeight * vSpacing + vSpacing * 2;

        // Set the width and height attributes on the root 'svg' element.
        svgRoot.setAttribute("width", Integer.toString(graphPanelSize.getWidth(graphData.gridWidth, hSpacing)));
        svgRoot.setAttribute("height", Integer.toString(graphPanelSize.getHeight(graphData.gridHeight, vSpacing)));

        this.setPreferredSize(new Dimension(graphPanelSize.getHeight(graphData.gridHeight, vSpacing), graphPanelSize.getWidth(graphData.gridWidth, hSpacing)));

        // create string array to store the selected ego nodes in the dom
        ArrayList<String> egoStringArray = new ArrayList<String>();
        for (URI currentEgoUri : egoSet) {
            egoStringArray.add(currentEgoUri.toASCIIString());
        }
        // store the selected kin type strings and other data in the dom
//        Namespace sNS = Namespace.getNamespace("someNS", "someNamespace");
//        Element element = new Element("SomeElement", sNS);

        Element kinTypesRecordNode = doc.createElementNS(kinDataNameSpace, "KinDiagramData");
//        Element kinTypesRecordNode = doc.createElement(kinDataNameSpace + ":KinDiagramData");
        kinTypesRecordNode.setAttribute("xmlns:" + kinDataNameSpace, kinDataNameSpaceLocation); // todo: this surely is not the only nor the best way to st the namespace
        storeParameter(kinTypesRecordNode, "EgoList", egoStringArray.toArray(new String[]{}));
        storeParameter(kinTypesRecordNode, "KinTypeStrings", kinTypeStrings);
        storeParameter(kinTypesRecordNode, "AncestorFields", indexParameters.ancestorFields.getValues());
        storeParameter(kinTypesRecordNode, "DecendantFields", indexParameters.decendantFields.getValues());
        storeParameter(kinTypesRecordNode, "LabelFields", indexParameters.labelFields.getValues());
        storeParameter(kinTypesRecordNode, "SymbolFieldsFields", indexParameters.symbolFieldsFields.getValues());
        svgRoot.appendChild(kinTypesRecordNode);
        // end store the selected kin type strings and other data in the dom

        svgCanvas.setSVGDocument(doc);
//        svgCanvas.setDocument(doc);
//        int counterTest = 0;
        for (GraphDataNode currentNode : graphData.getDataNodes()) {
            // set up the mouse listners on the group node
//            ((EventTarget) groupNode).addEventListener("mouseover", new EventListener() {
//
//                public void handleEvent(Event evt) {
//                    System.out.println("OnMouseOverCircleAction: " + evt.getCurrentTarget());
//                    if (currentDraggedElement == null) {
//                        ((Element) evt.getCurrentTarget()).setAttribute("fill", "green");
//                    }
//                }
//            }, false);
//            ((EventTarget) groupNode).addEventListener("mouseout", new EventListener() {
//
//                public void handleEvent(Event evt) {
//                    System.out.println("mouseout: " + evt.getCurrentTarget());
//                    if (currentDraggedElement == null) {
//                        ((Element) evt.getCurrentTarget()).setAttribute("fill", "none");
//                    }
//                }
//            }, false);


            // draw links
            for (GraphDataNode.NodeRelation graphLinkNode : currentNode.getNodeRelations()) {
                if (graphLinkNode.sourceNode.equals(currentNode)) {
                    Element groupNode = doc.createElementNS(svgNameSpace, "g");
                    Element defsNode = doc.createElementNS(svgNameSpace, "defs");
                    String lineIdString = currentNode.getEntityPath() + "-" + graphLinkNode.linkedNode.getEntityPath();
//                    todo: groupNode.setAttribute("id", currentNode.getEntityPath()+ ";"+and the other end);

                    // set the line end points
                    int fromX = (currentNode.xPos * hSpacing + hSpacing);
                    int fromY = (currentNode.yPos * vSpacing + vSpacing);
                    int toX = (graphLinkNode.linkedNode.xPos * hSpacing + hSpacing);
                    int toY = (graphLinkNode.linkedNode.yPos * vSpacing + vSpacing);
                    // set the label position
                    int labelX = (fromX + toX) / 2;
                    int labelY = (fromY + toY) / 2;

                    switch (graphLinkNode.relationLineType) {
                        case horizontalCurve:
                        // this case uses the following case
                        case verticalCurve:
                            // todo: groupNode.setAttribute("id", );
//                    System.out.println("link: " + graphLinkNode.linkedNode.xPos + ":" + graphLinkNode.linkedNode.yPos);
//
////                <line id="_15" transform="translate(146.0,112.0)" x1="0" y1="0" x2="100" y2="100" ="black" stroke-width="1"/>
//                    Element linkLine = doc.createElementNS(svgNS, "line");
//                    linkLine.setAttribute("x1", Integer.toString(currentNode.xPos * hSpacing + hSpacing));
//                    linkLine.setAttribute("y1", Integer.toString(currentNode.yPos * vSpacing + vSpacing));
//
//                    linkLine.setAttribute("x2", Integer.toString(graphLinkNode.linkedNode.xPos * hSpacing + hSpacing));
//                    linkLine.setAttribute("y2", Integer.toString(graphLinkNode.linkedNode.yPos * vSpacing + vSpacing));
//                    linkLine.setAttribute("stroke", "black");
//                    linkLine.setAttribute("stroke-width", "1");
//                    // Attach the rectangle to the root 'svg' element.
//                    svgRoot.appendChild(linkLine);
                            System.out.println("link: " + graphLinkNode.linkedNode.xPos + ":" + graphLinkNode.linkedNode.yPos);

//                <line id="_15" transform="translate(146.0,112.0)" x1="0" y1="0" x2="100" y2="100" ="black" stroke-width="1"/>
                            Element linkLine = doc.createElementNS(svgNameSpace, "path");
                            int fromBezX;
                            int fromBezY;
                            int toBezX;
                            int toBezY;
                            if (graphLinkNode.relationLineType == GraphDataNode.RelationLineType.verticalCurve) {
                                fromBezX = fromX;
                                fromBezY = toY;
                                toBezX = toX;
                                toBezY = fromY;
                                if (currentNode.yPos == graphLinkNode.linkedNode.yPos) {
                                    fromBezX = fromX;
                                    fromBezY = toY - vSpacing / 2;
                                    toBezX = toX;
                                    toBezY = fromY - vSpacing / 2;
                                    // set the label postion and lower it a bit
                                    labelY = toBezY + vSpacing / 3;
                                    ;
                                }
                            } else {
                                fromBezX = toX;
                                fromBezY = fromY;
                                toBezX = fromX;
                                toBezY = toY;
                                if (currentNode.xPos == graphLinkNode.linkedNode.xPos) {
                                    fromBezY = fromY;
                                    fromBezX = toX - hSpacing / 2;
                                    toBezY = toY;
                                    toBezX = fromX - hSpacing / 2;
                                    // set the label postion
                                    labelX = toBezX;
                                }
                            }
                            linkLine.setAttribute("d", "M " + fromX + "," + fromY + " C " + fromBezX + "," + fromBezY + " " + toBezX + "," + toBezY + " " + toX + "," + toY);

//                    linkLine.setAttribute("x1", );
//                    linkLine.setAttribute("y1", );
//
//                    linkLine.setAttribute("x2", );
                            linkLine.setAttribute("fill", "none");
                            linkLine.setAttribute("stroke", "blue");
                            linkLine.setAttribute("stroke-width", Integer.toString(strokeWidth));
                            linkLine.setAttribute("id", lineIdString);
                            defsNode.appendChild(linkLine);
                            break;
                        case square:
//                            Element squareLinkLine = doc.createElement("line");
//                            squareLinkLine.setAttribute("x1", Integer.toString(currentNode.xPos * hSpacing + hSpacing));
//                            squareLinkLine.setAttribute("y1", Integer.toString(currentNode.yPos * vSpacing + vSpacing));
//
//                            squareLinkLine.setAttribute("x2", Integer.toString(graphLinkNode.linkedNode.xPos * hSpacing + hSpacing));
//                            squareLinkLine.setAttribute("y2", Integer.toString(graphLinkNode.linkedNode.yPos * vSpacing + vSpacing));
//                            squareLinkLine.setAttribute("stroke", "grey");
//                            squareLinkLine.setAttribute("stroke-width", Integer.toString(strokeWidth));
                            Element squareLinkLine = doc.createElementNS(svgNameSpace, "polyline");
                            int midY = (fromY + toY) / 2;
                            if (toY == fromY) {
                                // make sure that union lines go below the entities and sibling lines go above
                                if (graphLinkNode.relationType == GraphDataNode.RelationType.sibling) {
                                    midY = toY - vSpacing / 2;
                                } else if (graphLinkNode.relationType == GraphDataNode.RelationType.union) {
                                    midY = toY + vSpacing / 2;
                                }
                            }

                            squareLinkLine.setAttribute("points",
                                    fromX + "," + fromY + " "
                                    + fromX + "," + midY + " "
                                    + toX + "," + midY + " "
                                    + toX + "," + toY);

                            squareLinkLine.setAttribute("fill", "none");
                            squareLinkLine.setAttribute("stroke", "grey");
                            squareLinkLine.setAttribute("stroke-width", Integer.toString(strokeWidth));
                            squareLinkLine.setAttribute("id", lineIdString);
                            defsNode.appendChild(squareLinkLine);
                            break;
                    }
                    groupNode.appendChild(defsNode);
                    Element useNode = doc.createElementNS(svgNameSpace, "use");
                    useNode.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "#" + lineIdString); // the xlink: of "xlink:href" is required for some svg viewers to render correctly

//                    useNode.setAttribute("href", "#" + lineIdString);
                    groupNode.appendChild(useNode);

                    // add the relation label
                    if (graphLinkNode.labelString != null) {
                        Element labelText = doc.createElementNS(svgNameSpace, "text");
                        labelText.setAttribute("text-anchor", "middle");
//                        labelText.setAttribute("x", Integer.toString(labelX));
//                        labelText.setAttribute("y", Integer.toString(labelY));
                        labelText.setAttribute("fill", "blue");
                        labelText.setAttribute("stroke-width", "0");
                        labelText.setAttribute("font-size", "14");
//                        labelText.setAttribute("transform", "rotate(45)");                        
                        Element textPath = doc.createElementNS(svgNameSpace, "textPath");
                        textPath.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "#" + lineIdString); // the xlink: of "xlink:href" is required for some svg viewers to render correctly
                        textPath.setAttribute("startOffset", "50%");
//                        textPath.setAttribute("text-anchor", "middle");
                        Text textNode = doc.createTextNode(graphLinkNode.labelString);
                        textPath.appendChild(textNode);
                        labelText.appendChild(textPath);
                        groupNode.appendChild(labelText);
                    }
                    svgRoot.appendChild(groupNode);
                }
            }
        }
        // add the entity symbols on top of the links
        for (GraphDataNode currentNode : graphData.getDataNodes()) {
            svgRoot.appendChild(createEntitySymbol(currentNode, hSpacing, vSpacing, symbolSize));
        }
        //new CmdiComponentBuilder().savePrettyFormatting(doc, new File("/Users/petwit/Documents/SharedInVirtualBox/mpi-co-svn-mpi-nl/LAT/Kinnate/trunk/src/main/resources/output.svg"));
        svgCanvas.revalidate();
        // todo: populate this correctly with the available symbols
        indexParameters.symbolFieldsFields.setAvailableValues(new String[]{"circle", "triangle", "square", "union"});
    }

    public boolean hasSaveFileName() {
        return svgFile != null;
    }

    public boolean requiresSave() {
        return requiresSave;
    }

    public void saveToFile() {
        saveSvg(svgFile);
    }

    public void saveToFile(File saveAsFile) {
        saveSvg(saveAsFile);
    }

    public void updateGraph() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
