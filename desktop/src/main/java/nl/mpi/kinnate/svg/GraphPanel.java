package nl.mpi.kinnate.svg;

import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.ui.GraphPanelContextMenu;
import java.awt.BorderLayout;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilderFactory;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.ui.ArbilTableModel;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.kinnate.KinTermSavePanel;
import nl.mpi.kinnate.entityindexer.IndexerParameters;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.kindata.GraphSorter;
import nl.mpi.kinnate.kintypestrings.KinTermGroup;
import nl.mpi.kinnate.ui.KinTypeEgoSelectionTestPanel;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;
import org.apache.batik.swing.svg.LinkActivationEvent;
import org.apache.batik.swing.svg.LinkActivationListener;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;

/**
 *  Document   : GraphPanel
 *  Created on : Aug 16, 2010, 5:31:33 PM
 *  Author     : Peter Withers
 */
public class GraphPanel extends JPanel implements SavePanel {

    private JSVGScrollPane jSVGScrollPane;
    protected JSVGCanvas svgCanvas;
    protected SVGDocument doc;
    protected ArbilTableModel arbilTableModel;
    private boolean requiresSave = false;
    private File svgFile = null;
    protected GraphPanelSize graphPanelSize;
    protected ArrayList<String> selectedGroupId;
    protected String svgNameSpace = SVGDOMImplementation.SVG_NAMESPACE_URI;
    public DataStoreSvg dataStoreSvg;
    protected EntitySvg entitySvg;
//    private URI[] egoPathsTemp = null;
    public SvgUpdateHandler svgUpdateHandler;
    private int currentZoom = 0;
    private AffineTransform zoomAffineTransform = null;

    public GraphPanel(KinTermSavePanel egoSelectionPanel) {
        dataStoreSvg = new DataStoreSvg();
        entitySvg = new EntitySvg();
        dataStoreSvg.setDefaults();
        svgUpdateHandler = new SvgUpdateHandler(this, egoSelectionPanel);
        selectedGroupId = new ArrayList<String>();
        graphPanelSize = new GraphPanelSize();
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
                currentZoom = currentZoom + e.getUnitsToScroll();
                if (currentZoom > 8) {
                    currentZoom = 8;
                }
                if (currentZoom < -6) {
                    currentZoom = -6;
                }
                double scale = 1 - e.getUnitsToScroll() / 10.0;
                double tx = -e.getX() * (scale - 1);
                double ty = -e.getY() * (scale - 1);
                AffineTransform at = new AffineTransform();
                at.translate(tx, ty);
                at.scale(scale, scale);
                at.concatenate(svgCanvas.getRenderingTransform());
                svgCanvas.setRenderingTransform(at);
//                zoomDrawing();
            }
        });
//        svgCanvas.setEnableResetTransformInteractor(true);
//        svgCanvas.setDoubleBufferedRendering(true); // todo: look into reducing the noticable aliasing on the canvas

        MouseListenerSvg mouseListenerSvg = new MouseListenerSvg(this);
        svgCanvas.addMouseListener(mouseListenerSvg);
        svgCanvas.addMouseMotionListener(mouseListenerSvg);
        jSVGScrollPane = new JSVGScrollPane(svgCanvas);
//        svgCanvas.setBackground(Color.LIGHT_GRAY);
        this.add(BorderLayout.CENTER, jSVGScrollPane);
        if (egoSelectionPanel instanceof KinTypeEgoSelectionTestPanel) {
            svgCanvas.setComponentPopupMenu(new GraphPanelContextMenu((KinTypeEgoSelectionTestPanel) egoSelectionPanel, this, graphPanelSize));
        } else {
            svgCanvas.setComponentPopupMenu(new GraphPanelContextMenu(null, this, graphPanelSize));
        }
        svgCanvas.addLinkActivationListener(new LinkActivationListener() {

            public void linkActivated(LinkActivationEvent lae) {
                // todo: find a better way to block the built in hyper link handler that tries to load the url into the canvas
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
    }

//    private void zoomDrawing() {
//        AffineTransform scaleTransform = new AffineTransform();
//        scaleTransform.scale(1 - currentZoom / 10.0, 1 - currentZoom / 10.0);
//        System.out.println("currentZoom: " + currentZoom);
////        svgCanvas.setRenderingTransform(scaleTransform);
//        Rectangle canvasBounds = this.getBounds();
//        SVGRect bbox = ((SVGLocatable) doc.getRootElement()).getBBox();
//        if (bbox != null) {
//            System.out.println("previousZoomedWith: " + bbox.getWidth());
//        }
////        SVGElement rootElement = doc.getRootElement();
////        if (currentWidth < canvasBounds.width) {
//        float drawingCenter = (currentWidth / 2);
////                float drawingCenter = (bbox.getX() + (bbox.getWidth() / 2));
//        float canvasCenter = (canvasBounds.width / 2);
//        zoomAffineTransform = new AffineTransform();
//        zoomAffineTransform.translate((canvasCenter - drawingCenter), 1);
//        zoomAffineTransform.concatenate(scaleTransform);
//        svgCanvas.setRenderingTransform(zoomAffineTransform);
//    }
    public void setArbilTableModel(ArbilTableModel arbilTableModelLocal) {
        arbilTableModel = arbilTableModelLocal;
    }

    public EntityData[] readSvg(File svgFilePath) {
        svgFile = svgFilePath;
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory documentFactory = new SAXSVGDocumentFactory(parser);
        try {
            doc = (SVGDocument) documentFactory.createDocument(svgFilePath.toURI().toString());
            svgCanvas.setDocument(doc);
            dataStoreSvg = DataStoreSvg.loadDataFromSvg(doc);
            requiresSave = false;
            entitySvg.readEntityPositions(doc.getElementById("EntityGroup"));
            entitySvg.readEntityPositions(doc.getElementById("LabelsGroup"));
        } catch (IOException ioe) {
            GuiHelper.linorgBugCatcher.logError(ioe);
        }
        // set up the mouse listeners that were lost in the save/re-open process
        for (String groupForMouseListener : new String[]{"EntityGroup", "LabelsGroup"}) {
            Element parentElement = doc.getElementById(groupForMouseListener);
            if (parentElement == null) {
                Element requiredGroup = doc.createElementNS(svgNameSpace, "g");
                requiredGroup.setAttribute("id", groupForMouseListener);
                Element svgRoot = doc.getDocumentElement();
                svgRoot.appendChild(requiredGroup);
            } else {
                Node currentNode = parentElement.getFirstChild();
                while (currentNode != null) {
                    ((EventTarget) currentNode).addEventListener("mousedown", new MouseListenerSvg(this), false);
                    currentNode = currentNode.getNextSibling();
                }
            }
        }
        dataStoreSvg.indexParameters.symbolFieldsFields.setAvailableValues(entitySvg.listSymbolNames(doc));
        if (dataStoreSvg.graphData == null) {
            return null;
        }
        svgCanvas.setSVGDocument(doc);
        return dataStoreSvg.graphData.getDataNodes();
    }

    public void generateDefaultSvg() {
        try {
            Element diagramGroup;
            Element relationGroupNode;
            Element entityGroupNode;
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            // set up a kinnate namespace so that the ego list and kin type strings can have more permanent storage places
            // in order to add the extra namespaces to the svg document we use a string and parse it
            // other methods have been tried but this is the most readable and the only one that actually works
            // I think this is mainly due to the way the svg dom would otherwise be constructed
            // others include:
            // doc.getDomConfig()
            // doc.getDocumentElement().setAttributeNS(DataStoreSvg.kinDataNameSpaceLocation, "kin:version", "");
            // doc.getDocumentElement().setAttribute("xmlns:" + DataStoreSvg.kinDataNameSpace, DataStoreSvg.kinDataNameSpaceLocation); // this method of declaring multiple namespaces looks to me to be wrong but it is the only method that does not get stripped out by the transformer on save
            //        Document doc = impl.createDocument(svgNS, "svg", null);
            //        SVGDocument doc = svgCanvas.getSVGDocument();
            String templateXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<svg xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:kin=\"http://mpi.nl/tla/kin\" "
                    + "xmlns=\"http://www.w3.org/2000/svg\" contentScriptType=\"text/ecmascript\" "
                    + " zoomAndPan=\"magnify\" contentStyleType=\"text/css\" "
                    + "preserveAspectRatio=\"xMidYMid meet\" version=\"1.0\"/>";
            // DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
            // doc = (SVGDocument) impl.createDocument(svgNameSpace, "svg", null);
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory documentFactory = new SAXSVGDocumentFactory(parser);
            doc = (SVGDocument) documentFactory.createDocument(svgNameSpace, new StringReader(templateXml));
            entitySvg.insertSymbols(doc, svgNameSpace);
            // add the diagram group to the root element (the 'svg' element)
            diagramGroup = doc.createElementNS(svgNameSpace, "g");
            diagramGroup.setAttribute("id", "DiagramGroup");
            doc.getDocumentElement().appendChild(diagramGroup);
            // add the relation symbols in a group below the relation lines
            relationGroupNode = doc.createElementNS(svgNameSpace, "g");
            relationGroupNode.setAttribute("id", "RelationGroup");
            diagramGroup.appendChild(relationGroupNode);
            // add the entity symbols in a group on top of the relation lines
            entityGroupNode = doc.createElementNS(svgNameSpace, "g");
            entityGroupNode.setAttribute("id", "EntityGroup");
            diagramGroup.appendChild(entityGroupNode);
            // add the labels group on top, also added on svg load if missing
            Element labelsGroup = doc.createElementNS(svgNameSpace, "g");
            labelsGroup.setAttribute("id", "LabelsGroup");
            diagramGroup.appendChild(labelsGroup);
            dataStoreSvg.indexParameters.symbolFieldsFields.setAvailableValues(entitySvg.listSymbolNames(doc));
            svgCanvas.setSVGDocument(doc);
        } catch (IOException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        }
    }

    private void saveSvg(File svgFilePath) {
        svgFile = svgFilePath;
        // todo: make sure the file path ends in .svg lowercase
        // start temp fix
        selectedGroupId.clear();
        svgUpdateHandler.drawEntities(); // todo: look into handling the runnable so that this cannot cause any issues
        // end temp fix
//        drawNodes(); // re draw the nodes so that any data changes such as the title/description in the kin term groups get updated into the file
        // end previous menthod before temp fix
        ArbilComponentBuilder.savePrettyFormatting(doc, svgFilePath);
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
        // this has set has been removed because it creates a discrepancy between what the user types and what is processed
//        HashSet<String> kinTypeStringSet = new HashSet<String>();
//        for (String kinTypeString : kinTypeStringArray) {
//            if (kinTypeString != null && kinTypeString.trim().length() > 0) {
//                kinTypeStringSet.add(kinTypeString.trim());
//            }
//        }
//        dataStoreSvg.kinTypeStrings = kinTypeStringSet.toArray(new String[]{});
        dataStoreSvg.kinTypeStrings = kinTypeStringArray;
    }

    public IndexerParameters getIndexParameters() {
        return dataStoreSvg.indexParameters;
    }

    public KinTermGroup[] getkinTermGroups() {
        return dataStoreSvg.kinTermGroups;
    }

    public void addKinTermGroup() {
        ArrayList<KinTermGroup> kinTermsList = new ArrayList<KinTermGroup>(Arrays.asList(dataStoreSvg.kinTermGroups));
        kinTermsList.add(new KinTermGroup());
        dataStoreSvg.kinTermGroups = kinTermsList.toArray(new KinTermGroup[]{});
    }

//    public String[] getEgoUniquiIdentifiersList() {
//        return dataStoreSvg.egoIdentifierSet.toArray(new String[]{});
//    }
//    public String[] getEgoIdList() {
//        return dataStoreSvg.egoIdentifierSet.toArray(new String[]{});
//    }
//    public URI[] getEgoPaths() {
//        if (egoPathsTemp != null) {
//            return egoPathsTemp;
//        }
//        ArrayList<URI> returnPaths = new ArrayList<URI>();
//        for (String egoId : dataStoreSvg.egoIdentifierSet) {
//            try {
//                String entityPath = getPathForElementId(egoId);
////                if (entityPath != null) {
//                returnPaths.add(new URI(entityPath));
////                }
//            } catch (URISyntaxException ex) {
//                GuiHelper.linorgBugCatcher.logError(ex);
//                // todo: warn user with a dialog
//            }
//        }
//        return returnPaths.toArray(new URI[]{});
//    }
//    public void setRequiredEntities(URI[] egoPathArray, String[] egoIdentifierArray) {
////        egoPathsTemp = egoPathArray; // egoPathsTemp is only required if the ego nodes are not already on the graph (otherwise the path can be obtained from the graph elements)
//        dataStoreSvg.requiredEntities = new HashSet<String>(Arrays.asList(egoIdentifierArray));
//    }
//
//    public void addRequiredEntity(URI[] egoPathArray, String[] egoIdentifierArray) {
////        egoPathsTemp = egoPathArray; // egoPathsTemp is only required if the ego nodes are not already on the graph (otherwise the path can be obtained from the graph elements)
//        dataStoreSvg.requiredEntities.addAll(Arrays.asList(egoIdentifierArray));
//    }
//    public void removeEgo(String[] egoIdentifierArray) {
//        dataStoreSvg.egoIdentifierSet.removeAll(Arrays.asList(egoIdentifierArray));
//    }
    public String[] getSelectedIds() {
        return selectedGroupId.toArray(new String[]{});
    }

//    public boolean selectionContainsEgo() {
//        for (String selectedId : selectedGroupId) {
//            if (dataStoreSvg.egoIdentifierSet.contains(selectedId)) {
//                return true;
//            }
//        }
//        return false;
//    }
    public String getPathForElementId(String elementId) {
//        NamedNodeMap namedNodeMap = doc.getElementById(elementId).getAttributes();
//        for (int attributeCounter = 0; attributeCounter < namedNodeMap.getLength(); attributeCounter++) {
//            System.out.println(namedNodeMap.item(attributeCounter).getNodeName());
//            System.out.println(namedNodeMap.item(attributeCounter).getNamespaceURI());
//            System.out.println(namedNodeMap.item(attributeCounter).getNodeValue());
//        }
        Element entityElement = doc.getElementById(elementId);
        if (entityElement == null) {
            return null;
        } else {
            return entityElement.getAttributeNS(DataStoreSvg.kinDataNameSpaceLocation, "path");
        }
    }

    public String getKinTypeForElementId(String elementId) {
        Element entityElement = doc.getElementById(elementId);
        if (entityElement != null) {
            return entityElement.getAttributeNS(DataStoreSvg.kinDataNameSpaceLocation, "kintype");
        } else {
            return "";
        }
    }
 
    public void resetZoom() {
        // todo: this should be moved to the svg update handler and put into a runnable
        AffineTransform at = new AffineTransform();
        at.scale(1, 1);
        at.setToTranslation(1, 1);
        svgCanvas.setRenderingTransform(at);
    }

    public void resetLayout() {
        entitySvg = new EntitySvg();
        dataStoreSvg.graphData.setEntitys(dataStoreSvg.graphData.getDataNodes());
        dataStoreSvg.graphData.placeAllNodes(this, entitySvg.entityPositions);
        drawNodes();
    }

    public void drawNodes() {
        requiresSave = true;
        selectedGroupId.clear();
        svgUpdateHandler.updateEntities();
    }

    public void drawNodes(GraphSorter graphDataLocal) {
        dataStoreSvg.graphData = graphDataLocal;
        drawNodes();
        if (graphDataLocal.getDataNodes().length == 0) {
            // if all entities have been removed then reset the zoom so that new nodes are going to been centered
            // todo: it would be better to move the window to cover the drawing area but not change the zoom
//            resetZoom();
        }
    }

    public boolean hasSaveFileName() {
        return svgFile != null;
    }

    public File getFileName() {
        return svgFile;
    }

    public boolean requiresSave() {
        return requiresSave;
    }

    public void setRequiresSave() {
        requiresSave = true;
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
