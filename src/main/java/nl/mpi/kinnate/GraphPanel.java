package nl.mpi.kinnate;

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
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.ImdiTableModel;
import nl.mpi.arbil.clarin.CmdiComponentBuilder;
import nl.mpi.arbil.data.ImdiLoader;
import nl.mpi.kinnate.EntityIndexer.IndexerParameters;
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
    private ImdiTableModel imdiTableModel;
    private GraphData graphData;
    private boolean requiresSave = false;
    private File svgFile = null;
    private GraphPanelSize graphPanelSize;
    private ArrayList<String> selectedGroupElement;

    public GraphPanel(KinTypeEgoSelectionTestPanel egoSelectionPanel) {
        selectedGroupElement = new ArrayList<String>();
        graphPanelSize = new GraphPanelSize();
        indexParameters = new IndexerParameters();
        this.setLayout(new BorderLayout());
        svgCanvas = new JSVGCanvas();
//        svgCanvas.setMySize(new Dimension(600, 400));
        svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC); // JSVGCanvas.ALWAYS_DYNAMIC allows more dynamic updating but introduces concurrency issues
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
        getParametersFromDom();
    }

    private void saveSvg(File svgFilePath) {
        svgFile = svgFilePath;
        new CmdiComponentBuilder().savePrettyFormatting(doc, svgFilePath);
        requiresSave = false;
    }

    private String[] readArrayFromEntity(Node currentChild) {
        return currentChild.getTextContent().split(",");
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
//                        if (idAttrubite.getTextContent().equals("KinTypeStrings")) {
//                            String[] kinTypeStringArray = currentChild.getTextContent().split(",");
//                            kinTypeStringSet = new HashSet<String>();
//                            for (String kinTypeString : kinTypeStringArray) {
//                                if (kinTypeString.length() > 0) {
//                                    kinTypeStringSet.add(kinTypeString);
//                                }
//                            }
//                        }
                        if (idAttrubite.getTextContent().equals("KinTypeStrings")) {
                            kinTypeStrings = readArrayFromEntity(currentChild);
                        }
                        if (idAttrubite.getTextContent().equals("AncestorFields")) {
                            indexParameters.ancestorFields.setValues(readArrayFromEntity(currentChild));
                        }
                        if (idAttrubite.getTextContent().equals("DecendantFields")) {
                            indexParameters.decendantFields.setValues(readArrayFromEntity(currentChild));
                        }
                        if (idAttrubite.getTextContent().equals("LabelFields")) {
                            indexParameters.labelFields.setValues(readArrayFromEntity(currentChild));
                        }
                        if (idAttrubite.getTextContent().equals("SymbolFieldsFields")) {
                            indexParameters.symbolFieldsFields.setValues(readArrayFromEntity(currentChild));
                        }
                    }
                }
            }
        }
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

    public void resetZoom() {
        AffineTransform at = new AffineTransform();
        at.scale(1, 1);
        at.setToTranslation(1, 1);
        svgCanvas.setRenderingTransform(at);
    }

    public void drawNodes() {
        drawNodes(graphData);
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
                                        Element symbolNode = doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "rect");
                                        int paddingDistance = 20;
                                        symbolNode.setAttributeNS(null, "id", "highlight");
                                        symbolNode.setAttributeNS(null, "x", Float.toString(bbox.getX() - paddingDistance));
                                        symbolNode.setAttributeNS(null, "y", Float.toString(bbox.getY() - paddingDistance));
                                        symbolNode.setAttributeNS(null, "width", Float.toString(bbox.getWidth() + paddingDistance * 2));
                                        symbolNode.setAttributeNS(null, "height", Float.toString(bbox.getHeight() + paddingDistance * 2));
                                        symbolNode.setAttributeNS(null, "fill", "none");
                                        symbolNode.setAttributeNS(null, "stroke-width", "1");
                                        symbolNode.setAttributeNS(null, "stroke", "blue");
                                        symbolNode.setAttributeNS(null, "stroke-dasharray", "3");
                                        symbolNode.setAttributeNS(null, "stroke-dashoffset", "0");
//            symbolNode.setAttributeNS(null, "id", "Highlight");
//            symbolNode.setAttributeNS(null, "id", "Highlight");
//            symbolNode.setAttributeNS(null, "id", "Highlight");
//            symbolNode.setAttributeNS(null, "style", ":none;fill-opacity:1;fill-rule:nonzero;stroke:#6674ff;stroke-opacity:1;stroke-width:1;stroke-miterlimit:4;"
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

    public void drawNodes(GraphData graphDataLocal) {
        requiresSave = true;
        graphData = graphDataLocal;
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        doc = (SVGDocument) impl.createDocument(svgNS, "svg", null);
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
        int symbolSize = 10;
        int strokeWidth = 1;

//        int preferedWidth = graphData.gridWidth * hSpacing + hSpacing * 2;
//        int preferedHeight = graphData.gridHeight * vSpacing + vSpacing * 2;

        // Set the width and height attributes on the root 'svg' element.
        svgRoot.setAttributeNS(null, "width", Integer.toString(graphPanelSize.getWidth(graphData.gridWidth, hSpacing)));
        svgRoot.setAttributeNS(null, "height", Integer.toString(graphPanelSize.getHeight(graphData.gridHeight, vSpacing)));

        this.setPreferredSize(new Dimension(graphPanelSize.getHeight(graphData.gridHeight, vSpacing), graphPanelSize.getWidth(graphData.gridWidth, hSpacing)));

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
        storeParameter(svgRoot, "KinTypeStrings", kinTypeStrings);
        // end store the selected kin type strings nodes in the dom
        storeParameter(svgRoot, "AncestorFields", indexParameters.ancestorFields.getValues());
        storeParameter(svgRoot, "DecendantFields", indexParameters.decendantFields.getValues());
        storeParameter(svgRoot, "LabelFields", indexParameters.labelFields.getValues());
        storeParameter(svgRoot, "SymbolFieldsFields", indexParameters.symbolFieldsFields.getValues());

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
            ((EventTarget) groupNode).addEventListener("mousedown", new EventListener() {

                public void handleEvent(Event evt) {
                    boolean shiftDown = false;
                    if (evt instanceof DOMMouseEvent) {
                        shiftDown = ((DOMMouseEvent) evt).getShiftKey();
                    }
                    System.out.println("mousedown: " + evt.getCurrentTarget());
                    currentDraggedElement = ((Element) evt.getCurrentTarget());
                    if (!shiftDown) {
                        System.out.println("Clear selection");
                        selectedGroupElement.clear();
                    }
                    preDragCursor = svgCanvas.getCursor();
                    // get the entityPath
                    String entityPath = currentDraggedElement.getAttribute("id");
                    System.out.println("entityPath: " + entityPath);
                    // set the highlight
                    selectedGroupElement.add(entityPath);
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

            // draw links
            for (GraphDataNode.NodeRelation graphLinkNode : currentNode.getNodeRelations()) {
                if (graphLinkNode.sourceNode.equals(currentNode)) {
//                    System.out.println("link: " + graphLinkNode.linkedNode.xPos + ":" + graphLinkNode.linkedNode.yPos);
//
////                <line id="_15" transform="translate(146.0,112.0)" x1="0" y1="0" x2="100" y2="100" ="black" stroke-width="1"/>
//                    Element linkLine = doc.createElementNS(svgNS, "line");
//                    linkLine.setAttributeNS(null, "x1", Integer.toString(currentNode.xPos * hSpacing + hSpacing));
//                    linkLine.setAttributeNS(null, "y1", Integer.toString(currentNode.yPos * vSpacing + vSpacing));
//
//                    linkLine.setAttributeNS(null, "x2", Integer.toString(graphLinkNode.linkedNode.xPos * hSpacing + hSpacing));
//                    linkLine.setAttributeNS(null, "y2", Integer.toString(graphLinkNode.linkedNode.yPos * vSpacing + vSpacing));
//                    linkLine.setAttributeNS(null, "stroke", "black");
//                    linkLine.setAttributeNS(null, "stroke-width", "1");
//                    // Attach the rectangle to the root 'svg' element.
//                    svgRoot.appendChild(linkLine);
                    System.out.println("link: " + graphLinkNode.linkedNode.xPos + ":" + graphLinkNode.linkedNode.yPos);

//                <line id="_15" transform="translate(146.0,112.0)" x1="0" y1="0" x2="100" y2="100" ="black" stroke-width="1"/>
                    Element linkLine = doc.createElementNS(svgNS, "path");
                    String fromX = Integer.toString(currentNode.xPos * hSpacing + hSpacing);
                    String fromY = Integer.toString(currentNode.yPos * vSpacing + vSpacing);
                    String toX = Integer.toString(graphLinkNode.linkedNode.xPos * hSpacing + hSpacing);
                    String toY = Integer.toString(graphLinkNode.linkedNode.yPos * vSpacing + vSpacing);
                    String fromBezX = fromX;
                    String fromBezY = toY;
                    String toBezX = toX;
                    String toBezY = fromY;
                    linkLine.setAttributeNS(null, "d", "M " + fromX + "," + fromY + " C " + fromBezX + "," + fromBezY + " " + toBezX + "," + toBezY + " " + toX + "," + toY);

//                    linkLine.setAttributeNS(null, "x1", );
//                    linkLine.setAttributeNS(null, "y1", );
//
//                    linkLine.setAttributeNS(null, "x2", );
                    linkLine.setAttributeNS(null, "fill", "none");
                    linkLine.setAttributeNS(null, "stroke", "grey");
                    linkLine.setAttributeNS(null, "stroke-width", "2");
                    // Attach the rectangle to the root 'svg' element.
                    svgRoot.appendChild(linkLine);
                }
            }
        }
        //new CmdiComponentBuilder().savePrettyFormatting(doc, new File("/Users/petwit/Documents/SharedInVirtualBox/mpi-co-svn-mpi-nl/LAT/Kinnate/trunk/src/main/resources/output.svg"));
        svgCanvas.revalidate();
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
}
