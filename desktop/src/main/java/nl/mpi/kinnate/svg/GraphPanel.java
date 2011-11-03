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
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilderFactory;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.kinnate.entityindexer.IndexerParameters;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.kindata.GraphSorter;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import nl.mpi.kinnate.kintypestrings.KinTermGroup;
import nl.mpi.kinnate.ui.KinDiagramPanel;
import nl.mpi.kinnate.ui.MetadataPanel;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;
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
    public MetadataPanel metadataPanel;
    private boolean requiresSave = false;
    private File svgFile = null;
    protected GraphPanelSize graphPanelSize;
    protected LineLookUpTable lineLookUpTable;
    protected ArrayList<UniqueIdentifier> selectedGroupId;
    protected String svgNameSpace = SVGDOMImplementation.SVG_NAMESPACE_URI;
    public DataStoreSvg dataStoreSvg;
    protected EntitySvg entitySvg;
//    private URI[] egoPathsTemp = null;
    public SvgUpdateHandler svgUpdateHandler;
    public MouseListenerSvg mouseListenerSvg;

    public GraphPanel(KinDiagramPanel kinDiagramPanel) {
        dataStoreSvg = new DataStoreSvg();
        entitySvg = new EntitySvg();
        dataStoreSvg.setDefaults();
        svgUpdateHandler = new SvgUpdateHandler(this, kinDiagramPanel);
        selectedGroupId = new ArrayList<UniqueIdentifier>();
        graphPanelSize = new GraphPanelSize();
        this.setLayout(new BorderLayout());
        svgCanvas = new JSVGCanvas(new GraphUserAgent(this), true, true);
//        svgCanvas.setMySize(new Dimension(600, 400));
        svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
//        drawNodes();
        svgCanvas.setEnableImageZoomInteractor(false);
        svgCanvas.setEnablePanInteractor(false);
        svgCanvas.setEnableRotateInteractor(false);
        svgCanvas.setEnableZoomInteractor(false);
        svgCanvas.addMouseWheelListener(new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent e) {
                double scale = 1 - e.getUnitsToScroll() / 10.0;
                double tx = -e.getX() * (scale - 1);
                double ty = -e.getY() * (scale - 1);
//                System.out.println("scale: " + scale);
//                System.out.println("scale: " + svgCanvas.getRenderingTransform().getScaleX());
                if (scale > 1 || svgCanvas.getRenderingTransform().getScaleX() > 0.01) {
                    AffineTransform at = new AffineTransform();
                    at.translate(tx, ty);
                    at.scale(scale, scale);
                    at.concatenate(svgCanvas.getRenderingTransform());
                    svgCanvas.setRenderingTransform(at);
                }
            }
        });
//        svgCanvas.setEnableResetTransformInteractor(true);
//        svgCanvas.setDoubleBufferedRendering(true); // todo: look into reducing the noticable aliasing on the canvas

        mouseListenerSvg = new MouseListenerSvg(kinDiagramPanel, this);
        svgCanvas.addMouseListener(mouseListenerSvg);
        svgCanvas.addMouseMotionListener(mouseListenerSvg);
        jSVGScrollPane = new JSVGScrollPane(svgCanvas);
//        svgCanvas.setBackground(Color.LIGHT_GRAY);
        this.add(BorderLayout.CENTER, jSVGScrollPane);
        svgCanvas.setComponentPopupMenu(new GraphPanelContextMenu(kinDiagramPanel, this, graphPanelSize));
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
    public void setArbilTableModel(MetadataPanel metadataPanel) {
        this.metadataPanel = metadataPanel;
    }

    public EntityData[] readSvg(URI svgFilePath, boolean savableType) {
        if (savableType) {
            svgFile = new File(svgFilePath);
        } else {
            svgFile = null;
        }
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory documentFactory = new SAXSVGDocumentFactory(parser);
        try {
            doc = (SVGDocument) documentFactory.createDocument(svgFilePath.toString());
            svgCanvas.setDocument(doc);
            dataStoreSvg = DataStoreSvg.loadDataFromSvg(doc);
            if (dataStoreSvg.indexParameters == null) {
                dataStoreSvg.setDefaults();
            }
            requiresSave = false;
            entitySvg.readEntityPositions(doc.getElementById("EntityGroup"));
            entitySvg.readEntityPositions(doc.getElementById("LabelsGroup"));
            entitySvg.readEntityPositions(doc.getElementById("GraphicsGroup"));
        } catch (IOException ioe) {
            GuiHelper.linorgBugCatcher.logError(ioe);
        }
        configureDiagramGroups();
        dataStoreSvg.indexParameters.symbolFieldsFields.setAvailableValues(entitySvg.listSymbolNames(doc, this.svgNameSpace));
        if (dataStoreSvg.graphData == null) {
            return null;
        }
        svgCanvas.setSVGDocument(doc);
        return dataStoreSvg.graphData.getDataNodes();
    }

    private void configureDiagramGroups() {
        Element svgRoot = doc.getDocumentElement();
        // make sure the diagram group exisits
        Element diagramGroup = doc.getElementById("DiagramGroup");
        if (diagramGroup == null) {
            diagramGroup = doc.createElementNS(svgNameSpace, "g");
            diagramGroup.setAttribute("id", "DiagramGroup");
            // add the diagram group to the root element (the 'svg' element)
            svgRoot.appendChild(diagramGroup);
        }
        Element previousElement = null;
        // add the graphics group below the entities and relations
        // add the relation symbols in a group below the relation lines
        // add the entity symbols in a group on top of the relation lines
        // add the labels group on top, also added on svg load if missing
        for (String groupForMouseListener : new String[]{"LabelsGroup", "EntityGroup", "RelationGroup", "GraphicsGroup"}) {
            // add any groups that are required and add them in the required order
            Element parentElement = doc.getElementById(groupForMouseListener);
            if (parentElement == null) {
                parentElement = doc.createElementNS(svgNameSpace, "g");
                parentElement.setAttribute("id", groupForMouseListener);
                diagramGroup.insertBefore(parentElement, previousElement);
            } else {
                diagramGroup.insertBefore(parentElement, previousElement); // insert the node to make sure that it is in the diagram group and not in any other location
                // set up the mouse listeners that were lost in the save/re-open process
                if (!groupForMouseListener.equals("RelationGroup")) {
                    // do not add mouse listeners to the relation group
                    Node currentNode = parentElement.getFirstChild();
                    while (currentNode != null) {
                        ((EventTarget) currentNode).addEventListener("mousedown", mouseListenerSvg, false);
                        currentNode = currentNode.getNextSibling();
                    }
                }
            }
            previousElement = parentElement;
        }
    }

    public void generateDefaultSvg() {
        try {
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
            configureDiagramGroups();
            dataStoreSvg.indexParameters.symbolFieldsFields.setAvailableValues(entitySvg.listSymbolNames(doc, this.svgNameSpace));
            svgCanvas.setSVGDocument(doc);
            dataStoreSvg.graphData = new GraphSorter();
        } catch (IOException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        }
    }

    private void saveSvg(File svgFilePath) {
        svgFile = svgFilePath;
        selectedGroupId.clear();
        svgUpdateHandler.clearHighlights();
        // todo: make sure that any data changes such as the title/description in the kin term groups get updated into the file on save
        ArbilComponentBuilder.savePrettyFormatting(doc, svgFile);
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

    public KinTermGroup addKinTermGroup() {
        ArrayList<KinTermGroup> kinTermsList = new ArrayList<KinTermGroup>(Arrays.asList(dataStoreSvg.kinTermGroups));
        final KinTermGroup kinTermGroup = new KinTermGroup();
        kinTermsList.add(kinTermGroup);
        dataStoreSvg.kinTermGroups = kinTermsList.toArray(new KinTermGroup[]{});
        return kinTermGroup;
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
    public void setSelectedIds(UniqueIdentifier[] uniqueIdentifiers) {
        selectedGroupId.clear();
        selectedGroupId.addAll(Arrays.asList(uniqueIdentifiers));
        svgUpdateHandler.updateSvgSelectionHighlights();
//        mouseListenerSvg.updateSelectionDisplay();
    }

    public UniqueIdentifier[] getSelectedIds() {
        return selectedGroupId.toArray(new UniqueIdentifier[]{});
    }

    public EntityData getEntityForElementId(UniqueIdentifier uniqueIdentifier) {
        for (EntityData entityData : dataStoreSvg.graphData.getDataNodes()) {
            if (uniqueIdentifier.equals(entityData.getUniqueIdentifier())) {
                return entityData;
            }
        }
        return null;
    }

    public HashMap<UniqueIdentifier, EntityData> getEntitiesById(UniqueIdentifier[] uniqueIdentifiers) {
        ArrayList<UniqueIdentifier> identifierList = new ArrayList<UniqueIdentifier>(Arrays.asList(uniqueIdentifiers));
        HashMap<UniqueIdentifier, EntityData> returnMap = new HashMap<UniqueIdentifier, EntityData>();
        for (EntityData entityData : dataStoreSvg.graphData.getDataNodes()) {
            if (identifierList.contains(entityData.getUniqueIdentifier())) {
                returnMap.put(entityData.getUniqueIdentifier(), entityData);
            }
        }
        return returnMap;
    }

//    public boolean selectionContainsEgo() {
//        for (String selectedId : selectedGroupId) {
//            if (dataStoreSvg.egoIdentifierSet.contains(selectedId)) {
//                return true;
//            }
//        }
//        return false;
//    }
    public String getPathForElementId(UniqueIdentifier elementId) {
//        NamedNodeMap namedNodeMap = doc.getElementById(elementId).getAttributes();
//        for (int attributeCounter = 0; attributeCounter < namedNodeMap.getLength(); attributeCounter++) {
//            System.out.println(namedNodeMap.item(attributeCounter).getNodeName());
//            System.out.println(namedNodeMap.item(attributeCounter).getNamespaceURI());
//            System.out.println(namedNodeMap.item(attributeCounter).getNodeValue());
//        }
        Element entityElement = doc.getElementById(elementId.getAttributeIdentifier());
        if (entityElement == null) {
            return null;
        } else {
            return entityElement.getAttributeNS(DataStoreSvg.kinDataNameSpaceLocation, "path");
        }
    }

    public String getKinTypeForElementId(UniqueIdentifier elementId) {
        Element entityElement = doc.getElementById(elementId.getAttributeIdentifier());
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
        dataStoreSvg.graphData.placeAllNodes(entitySvg.entityPositions);
        drawNodes();
    }

    public void clearEntityLocations(UniqueIdentifier[] selectedIdentifiers) {
        entitySvg.clearEntityLocations(selectedIdentifiers);
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
