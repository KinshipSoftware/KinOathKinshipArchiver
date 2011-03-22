package nl.mpi.kinnate.svg;

import nl.mpi.kinnate.ui.GraphPanelContextMenu;
import nl.mpi.kinnate.ui.KinTypeEgoSelectionTestPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import javax.swing.JPanel;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.ImdiTableModel;
import nl.mpi.arbil.clarin.CmdiComponentBuilder;
import nl.mpi.kinnate.entityindexer.IndexerParameters;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.kintypestrings.KinTerms;
import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
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
    protected JSVGCanvas svgCanvas;
    private SVGDocument doc;
    private KinTerms kinTerms;
    protected ImdiTableModel imdiTableModel;
    private GraphData graphData;
    private boolean requiresSave = false;
    private File svgFile = null;
    private GraphPanelSize graphPanelSize;
    protected ArrayList<String> selectedGroupElement;
    private String svgNameSpace = SVGDOMImplementation.SVG_NAMESPACE_URI;
    private DataStoreSvg dataStoreSvg;

    public GraphPanel(KinTypeEgoSelectionTestPanel egoSelectionPanel) {
        dataStoreSvg = new DataStoreSvg();
        selectedGroupElement = new ArrayList<String>();
        graphPanelSize = new GraphPanelSize();
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

        MouseListenerSvg mouseListenerSvg = new MouseListenerSvg(this);
        svgCanvas.addMouseListener(mouseListenerSvg);
        svgCanvas.addMouseMotionListener(mouseListenerSvg);
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

        dataStoreSvg.loadDataFromSvg(doc);
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

    public String[] getKinTypeStrigs() {
        return dataStoreSvg.kinTypeStrings;
    }

    public void setKinTypeStrigs(String[] kinTypeStringArray) {
        // strip out any white space, blank lines and remove duplicates
        HashSet<String> kinTypeStringSet = new HashSet<String>();
        for (String kinTypeString : kinTypeStringArray) {
            if (kinTypeString != null && kinTypeString.trim().length() > 0) {
                kinTypeStringSet.add(kinTypeString.trim());
            }
        }
        dataStoreSvg.kinTypeStrings = kinTypeStringSet.toArray(new String[]{});
    }

    public IndexerParameters getIndexParameters() {
        return dataStoreSvg.indexParameters;
    }

    public KinTerms getkinTerms() {
        return kinTerms;
    }

    public String[] getEgoUniquiIdentifiersList() {
        return dataStoreSvg.egoIdentifierSet.toArray(new String[]{});
    }

    public URI[] getEgoList() {
        return dataStoreSvg.egoSet.toArray(new URI[]{});
    }

    public void setEgoList(URI[] egoListArray, String[] egoIdentifierArray) {
        dataStoreSvg.egoSet = new HashSet<URI>(Arrays.asList(egoListArray));
        dataStoreSvg.egoIdentifierSet = new HashSet<String>(Arrays.asList(egoIdentifierArray));
    }

    public String[] getSelectedPaths() {
        return selectedGroupElement.toArray(new String[]{});
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

    protected void updateDragNode(final int updateDragNodeX, final int updateDragNodeY) {
        UpdateManager updateManager = svgCanvas.getUpdateManager();
        updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {

            public void run() {
                System.out.println("updateDragNodeX: " + updateDragNodeX);
                System.out.println("updateDragNodeY: " + updateDragNodeY);
                if (doc != null) {
                    for (String entityId : selectedGroupElement) {
                        new EntitySvg().moveEntity(doc, entityId, updateDragNodeX, updateDragNodeY);
                    }
//                    Element entityGroup = doc.getElementById("EntityGroup");
//                    for (Node currentChild = entityGroup.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
//                        if ("g".equals(currentChild.getLocalName())) {
//                            Node idAttrubite = currentChild.getAttributes().getNamedItem("id");
//                            if (idAttrubite != null) {
//                                String entityPath = idAttrubite.getTextContent();
//                                if (selectedGroupElement.contains(entityPath)) {
//                                    SVGRect bbox = ((SVGLocatable) currentChild).getBBox();
////                    ((SVGLocatable) currentDraggedElement).g
//                                    // drageboth x and y
////                                    ((Element) currentChild).setAttribute("transform", "translate(" + String.valueOf(updateDragNodeX * svgCanvas.getRenderingTransform().getScaleX() - bbox.getX()) + ", " + String.valueOf(updateDragNodeY - bbox.getY()) + ")");
//                                    // limit drag to x only
//                                    ((Element) currentChild).setAttribute("transform", "translate(" + String.valueOf(updateDragNodeX * svgCanvas.getRenderingTransform().getScaleX() - bbox.getX()) + ", 0)");
////                    updateDragNodeElement.setAttribute("x", String.valueOf(updateDragNodeX));
////                    updateDragNodeElement.setAttribute("y", String.valueOf(updateDragNodeY));
//                                    //                    SVGRect bbox = ((SVGLocatable) currentDraggedElement).getBBox();
////                    System.out.println("bbox X: " + bbox.getX());
////                    System.out.println("bbox Y: " + bbox.getY());
////                    System.out.println("bbox W: " + bbox.getWidth());
////                    System.out.println("bbox H: " + bbox.getHeight());
////                    todo: look into transform issues when dragging ellements eg when the canvas is scaled or panned
////                            SVGLocatable.getTransformToElement()
////                            SVGPoint.matrixTransform()
//                                }
//                            }
//                        }
//                    }
                    int vSpacing = graphPanelSize.getVerticalSpacing(graphData.gridHeight);
                    int hSpacing = graphPanelSize.getHorizontalSpacing(graphData.gridWidth);
                    new RelationSvg().updateRelationLines(doc, selectedGroupElement, svgNameSpace, hSpacing, vSpacing);
                    //new CmdiComponentBuilder().savePrettyFormatting(doc, new File("/Users/petwit/Documents/SharedInVirtualBox/mpi-co-svn-mpi-nl/LAT/Kinnate/trunk/src/main/resources/output.svg"));
                }
            }
        });
    }

    protected void addHighlightToGroup() {
        UpdateManager updateManager = svgCanvas.getUpdateManager();
        updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {

            public void run() {
                if (doc != null) {
                    Element entityGroup = doc.getElementById("EntityGroup");
                    for (Node currentChild = entityGroup.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
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
//                                        svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                                        SVGRect bbox = ((SVGLocatable) currentChild).getBBox();
//                                        System.out.println("bbox X: " + bbox.getX());
//                                        System.out.println("bbox Y: " + bbox.getY());
//                                        System.out.println("bbox W: " + bbox.getWidth());
//                                        System.out.println("bbox H: " + bbox.getHeight());
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
        if (symbolType == null) {
            symbolType = "cross";
        }
        // todo: check that if an entity is already placed in which case do not recreate
        // todo: do not create a new dom each time but reuse it instead, or due to the need to keep things up to date maybe just store an array of entity locations instead
        symbolNode = doc.createElementNS(svgNameSpace, "use");
        symbolNode.setAttribute("id", currentNode.getEntityPath() + "symbol");
        symbolNode.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "#" + symbolType); // the xlink: of "xlink:href" is required for some svg viewers to render correctly
        groupNode.setAttribute("transform", "translate(" + Integer.toString(currentNode.xPos * hSpacing + hSpacing - symbolSize / 2) + ", " + Integer.toString(currentNode.yPos * vSpacing + vSpacing - symbolSize / 2) + ")");

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
            labelText.setAttribute("x", Double.toString(symbolSize * 1.5));
            labelText.setAttribute("y", Integer.toString(textSpanCounter));
            labelText.setAttribute("fill", "black");
            labelText.setAttribute("stroke-width", "0");
            labelText.setAttribute("font-size", "14");
            Text textNode = doc.createTextNode(currentTextLable);
            labelText.appendChild(textNode);
            textSpanCounter += lineSpacing;
            groupNode.appendChild(labelText);
        }
////////////////////////////// end alternate method ////////////////////////////////////////////////
        ((EventTarget) groupNode).addEventListener("mousedown", new MouseListenerSvg(this), false);
        return groupNode;
    }

    public void drawNodes(GraphData graphDataLocal) {
        requiresSave = true;
        graphData = graphDataLocal;
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        doc = (SVGDocument) impl.createDocument(svgNameSpace, "svg", null);
        new EntitySvg().insertSymbols(doc, svgNameSpace);
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

        // store the selected kin type strings and other data in the dom
        dataStoreSvg.storeAllData(doc);

        svgCanvas.setSVGDocument(doc);
//        svgCanvas.setDocument(doc);
//        int counterTest = 0;

        // add the relation symbols in a group below the relation lines
        Element relationGroupNode = doc.createElementNS(svgNameSpace, "g");
        relationGroupNode.setAttribute("id", "RelationGroup");
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


            for (GraphDataNode.NodeRelation graphLinkNode : currentNode.getNodeRelations()) {
                if (graphLinkNode.sourceNode.equals(currentNode)) {
                    new RelationSvg().insertRelation(doc, svgNameSpace, relationGroupNode, currentNode, graphLinkNode, hSpacing, vSpacing, strokeWidth);
                }
            }
        }
        svgRoot.appendChild(relationGroupNode);
        // add the entity symbols in a group on top of the relation lines
        Element entityGroupNode = doc.createElementNS(svgNameSpace, "g");
        entityGroupNode.setAttribute("id", "EntityGroup");
        for (GraphDataNode currentNode : graphData.getDataNodes()) {
            entityGroupNode.appendChild(createEntitySymbol(currentNode, hSpacing, vSpacing, symbolSize));
        }
        svgRoot.appendChild(entityGroupNode);
        //new CmdiComponentBuilder().savePrettyFormatting(doc, new File("/Users/petwit/Documents/SharedInVirtualBox/mpi-co-svn-mpi-nl/LAT/Kinnate/trunk/src/main/resources/output.svg"));
//        svgCanvas.revalidate();
        // todo: populate this correctly with the available symbols
        dataStoreSvg.indexParameters.symbolFieldsFields.setAvailableValues(new EntitySvg().listSymbolNames(doc));
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
