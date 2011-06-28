package nl.mpi.kinnate.svg;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.File;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.kinnate.KinTermSavePanel;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityRelation;
import org.apache.batik.bridge.UpdateManager;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGRect;

/**
 *  Document   : DragHandler
 *  Created on : Mar 31, 2011, 12:52:12 PM
 *  Author     : Peter Withers
 */
public class SvgUpdateHandler {

    private GraphPanel graphPanel;
    private KinTermSavePanel kinTermSavePanel;
    private boolean dragUpdateRequired = false;
    private boolean threadRunning = false;
    private int updateDragNodeX = 0;
    private int updateDragNodeY = 0;
    private float[][] dragRemainders = null;
    private boolean resizeRequired = false;

    protected SvgUpdateHandler(GraphPanel graphPanelLocal, KinTermSavePanel kinTermSavePanelLocal) {
        graphPanel = graphPanelLocal;
        kinTermSavePanel = kinTermSavePanelLocal;
    }

    protected void updateSvgSelectionHighlights() {
        if (kinTermSavePanel != null) {
            String kinTypeStrings = "";
            for (String entityID : graphPanel.selectedGroupId) {
                if (kinTypeStrings.length() != 0) {
                    kinTypeStrings = kinTypeStrings + "|";
                }
                kinTypeStrings = kinTypeStrings + graphPanel.getKinTypeForElementId(entityID);
            }
            if (kinTypeStrings != null) {
                kinTermSavePanel.setSelectedKinTypeSting(kinTypeStrings);
            }
        }
        UpdateManager updateManager = graphPanel.svgCanvas.getUpdateManager();
        if (updateManager != null) { // todo: there may be issues related to the updateManager being null, this should be looked into if symptoms arise.
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {

                public void run() {
                    if (graphPanel.doc != null) {
//                        for (String groupString : new String[]{"EntityGroup", "LabelsGroup"}) {
//                            Element entityGroup = graphPanel.doc.getElementById(groupString);
                        {
                            Element entityGroup = graphPanel.doc.getElementById("EntityGroup");
                            for (Node currentChild = entityGroup.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
                                if ("g".equals(currentChild.getLocalName())) {
                                    Node idAttrubite = currentChild.getAttributes().getNamedItem("id");
                                    if (idAttrubite != null) {
                                        String entityId = idAttrubite.getTextContent();
                                        System.out.println("group id: " + entityId);
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
                                        if (!graphPanel.selectedGroupId.contains(entityId)) {
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
                                                Element symbolNode = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "rect");
                                                int paddingDistance = 20;
                                                symbolNode.setAttribute("id", "highlight");
                                                symbolNode.setAttribute("x", Float.toString(bbox.getX() - paddingDistance));
                                                symbolNode.setAttribute("y", Float.toString(bbox.getY() - paddingDistance));
                                                symbolNode.setAttribute("width", Float.toString(bbox.getWidth() + paddingDistance * 2));
                                                symbolNode.setAttribute("height", Float.toString(bbox.getHeight() + paddingDistance * 2));
                                                symbolNode.setAttribute("fill", "none");
                                                symbolNode.setAttribute("stroke-width", "1");
                                                if (graphPanel.selectedGroupId.indexOf(entityId) == 0) {
                                                    symbolNode.setAttribute("stroke-dasharray", "3");
                                                    symbolNode.setAttribute("stroke-dashoffset", "0");
                                                } else {
                                                    symbolNode.setAttribute("stroke-dasharray", "6");
                                                    symbolNode.setAttribute("stroke-dashoffset", "0");
                                                }
                                                symbolNode.setAttribute("stroke", "blue");
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
                }
            });
        }
    }

    protected void dragCanvas(int updateDragNodeXLocal, int updateDragNodeYLocal) {
        AffineTransform at = new AffineTransform();
        at.translate(updateDragNodeXLocal, updateDragNodeYLocal);
        at.concatenate(graphPanel.svgCanvas.getRenderingTransform());
        graphPanel.svgCanvas.setRenderingTransform(at);
    }

    protected void startDrag() {
        // dragRemainders is used to store the remainder after snap between drag updates
        dragRemainders = null;
    }

    protected void updateDragNode(int updateDragNodeXLocal, int updateDragNodeYLocal) {
        resizeRequired = true;
        UpdateManager updateManager = graphPanel.svgCanvas.getUpdateManager();
        synchronized (SvgUpdateHandler.this) {
            dragUpdateRequired = true;
            updateDragNodeX += updateDragNodeXLocal;
            updateDragNodeY += updateDragNodeYLocal;
            if (!threadRunning) {
                threadRunning = true;
                updateManager.getUpdateRunnableQueue().invokeLater(getRunnable());
            }
        }
    }

    private Runnable getRunnable() {
        return new Runnable() {

            public void run() {
                if (dragRemainders == null) {
                    dragRemainders = new float[graphPanel.selectedGroupId.size()][];
                    for (int dragCounter = 0; dragCounter < dragRemainders.length; dragCounter++) {
                        dragRemainders[dragCounter] = new float[]{0, 0};
                    }
                }
                boolean continueUpdating = true;
                while (continueUpdating) {
                    continueUpdating = false;
                    int updateDragNodeXInner;
                    int updateDragNodeYInner;
                    synchronized (SvgUpdateHandler.this) {
                        dragUpdateRequired = false;
                        updateDragNodeXInner = updateDragNodeX;
                        updateDragNodeYInner = updateDragNodeY;
                        updateDragNodeX = 0;
                        updateDragNodeY = 0;
                    }
//                    System.out.println("updateDragNodeX: " + updateDragNodeXInner);
//                    System.out.println("updateDragNodeY: " + updateDragNodeYInner);
                    if (graphPanel.doc == null || graphPanel.dataStoreSvg.graphData == null) {
                        GuiHelper.linorgBugCatcher.logError(new Exception("graphData or the svg document is null, is this an old file format? try redrawing before draging."));
                    } else {
                        boolean allRealtionsSelected = true;
                        relationLoop:
                        for (EntityData selectedEntity : graphPanel.dataStoreSvg.graphData.getDataNodes()) {
                            if (selectedEntity.isVisible
                                    && graphPanel.selectedGroupId.contains(selectedEntity.getUniqueIdentifier())) {
                                for (EntityRelation entityRelation : selectedEntity.getVisiblyRelateNodes()) {
                                    EntityData relatedEntity = entityRelation.getAlterNode();
                                    if (relatedEntity.isVisible && !graphPanel.selectedGroupId.contains(relatedEntity.getUniqueIdentifier())) {
                                        allRealtionsSelected = false;
                                        break relationLoop;
                                    }
                                }
                            }
                        }
                        int dragCounter = 0;
                        for (String entityId : graphPanel.selectedGroupId) {
                            // store the remainder after snap for re use on each update
                            dragRemainders[dragCounter] = graphPanel.entitySvg.moveEntity(graphPanel, entityId, updateDragNodeXInner + dragRemainders[dragCounter][0], updateDragNodeYInner + dragRemainders[dragCounter][1], graphPanel.dataStoreSvg.snapToGrid, allRealtionsSelected);
                            dragCounter++;
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
////                                    ((Element) currentChild).setAttribute("transform", "translate(" + String.valueOf(updateDragNodeXInner * svgCanvas.getRenderingTransform().getScaleX() - bbox.getX()) + ", " + String.valueOf(updateDragNodeYInner - bbox.getY()) + ")");
//                                    // limit drag to x only
//                                    ((Element) currentChild).setAttribute("transform", "translate(" + String.valueOf(updateDragNodeXInner * svgCanvas.getRenderingTransform().getScaleX() - bbox.getX()) + ", 0)");
////                    updateDragNodeElement.setAttribute("x", String.valueOf(updateDragNodeXInner));
////                    updateDragNodeElement.setAttribute("y", String.valueOf(updateDragNodeYInner));
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
                        int vSpacing = graphPanel.graphPanelSize.getVerticalSpacing(); // graphPanel.dataStoreSvg.graphData.gridHeight);
                        int hSpacing = graphPanel.graphPanelSize.getHorizontalSpacing(); // graphPanel.dataStoreSvg.graphData.gridWidth);
                        new RelationSvg().updateRelationLines(graphPanel, graphPanel.selectedGroupId, graphPanel.svgNameSpace, hSpacing, vSpacing);
                        //new CmdiComponentBuilder().savePrettyFormatting(doc, new File("/Users/petwit/Documents/SharedInVirtualBox/mpi-co-svn-mpi-nl/LAT/Kinnate/trunk/src/main/resources/output.svg"));
                    }
                    // graphPanel.updateCanvasSize(); // updating the canvas size here is too slow so it is moved into the drag ended 
//                    if (graphPanel.dataStoreSvg.graphData.isRedrawRequired()) { // this has been abandoned in favour of preventing dragging past zero
                    // todo: update the position of all nodes
                    // todo: any labels and other non entity graphics must also be taken into account here
//                        for (EntityData selectedEntity : graphPanel.dataStoreSvg.graphData.getDataNodes()) {
//                            if (selectedEntity.isVisible) {
//                                graphPanel.entitySvg.moveEntity(graphPanel, selectedEntity.getUniqueIdentifier(), updateDragNodeXInner + dragRemainders[dragCounter][0], updateDragNodeYInner + dragRemainders[dragCounter][1], graphPanel.dataStoreSvg.snapToGrid, true);
//                            }
//                        }
//                    }
                    synchronized (SvgUpdateHandler.this) {
                        continueUpdating = dragUpdateRequired;
                        if (!continueUpdating) {
                            threadRunning = false;
                        }
                    }
                }
            }
        };
    }

    private void resizeCanvas(Element svgRoot, Element diagramGroupNode) {
        Rectangle graphSize = graphPanel.dataStoreSvg.graphData.getGraphSize(graphPanel.entitySvg.entityPositions);
        // set the diagram offset so that no element is less than zero
        diagramGroupNode.setAttribute("transform", "translate(" + Integer.toString(-graphSize.x) + ", " + Integer.toString(-graphSize.y) + ")");
        // Set the width and height attributes on the root 'svg' element.
        svgRoot.setAttribute("width", Integer.toString(graphSize.width - graphSize.x));
        svgRoot.setAttribute("height", Integer.toString(graphSize.height - graphSize.y));

        // draw hidious green and yellow rectangle for debugging
        Element pageBorderNode = graphPanel.doc.getElementById("PageBorder");
        if (pageBorderNode == null) {
            pageBorderNode = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "rect");
            pageBorderNode.setAttribute("id", "PageBorder");
            pageBorderNode.setAttribute("x", Float.toString(graphSize.x));
            pageBorderNode.setAttribute("y", Float.toString(graphSize.y));
            pageBorderNode.setAttribute("width", Float.toString(graphSize.width - graphSize.x));
            pageBorderNode.setAttribute("height", Float.toString(graphSize.height - graphSize.y));
            pageBorderNode.setAttribute("fill", "none");
            pageBorderNode.setAttribute("stroke-width", "2");
            pageBorderNode.setAttribute("stroke", "grey");
            diagramGroupNode.appendChild(pageBorderNode);
        } else {
            pageBorderNode.setAttribute("x", Float.toString(graphSize.x));
            pageBorderNode.setAttribute("y", Float.toString(graphSize.y));
            pageBorderNode.setAttribute("width", Float.toString(graphSize.width - graphSize.x));
            pageBorderNode.setAttribute("height", Float.toString(graphSize.height - graphSize.y));
        }
        // end draw hidious green rectangle for debugging
    }

    public void updateCanvasSize() {
        UpdateManager updateManager = graphPanel.svgCanvas.getUpdateManager();
        if (updateManager != null) {
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {

                public void run() {
                    if (resizeRequired) {
                        resizeRequired = false;
                        Element svgRoot = graphPanel.doc.getDocumentElement();
                        Element diagramGroupNode = graphPanel.doc.getElementById("DiagramGroup");
                        resizeCanvas(svgRoot, diagramGroupNode);
                    }
                }
            });
        }
    }

    public void addLabel(final String labelString, final float xPos, final float yPos) {
        UpdateManager updateManager = graphPanel.svgCanvas.getUpdateManager();
        if (updateManager != null) {
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {

                public void run() {
                    Rectangle graphSize = graphPanel.dataStoreSvg.graphData.getGraphSize(graphPanel.entitySvg.entityPositions);
                    // todo: handle case where the diagram has not been drawn yet and the graph data and graph size is not available
//                    if (graphSize == null) {
//                        graphSize = new Rectangle(0, 0, 0, 0);
//                    }
                    Element labelGroup = graphPanel.doc.getElementById("LabelsGroup");
                    Element labelText = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "text");
                    String labelIdString = "label" + labelGroup.getChildNodes().getLength();
                    float[] labelPosition = new float[]{graphSize.x + graphSize.width / 2, graphSize.y + graphSize.height / 2};
//                    labelText.setAttribute("x", "0"); // todo: update this to use the mouse click location // xPos
//                    labelText.setAttribute("y", "0"); // yPos
                    labelText.setAttribute("fill", "black");
                    labelText.setAttribute("stroke-width", "0");
                    labelText.setAttribute("font-size", "28");
                    labelText.setAttribute("id", labelIdString);
                    labelText.setAttribute("transform", "translate(" + Float.toString(labelPosition[0]) + ", " + Float.toString(labelPosition[1]) + ")");
//
                    Text textNode = graphPanel.doc.createTextNode(labelString);
                    labelText.appendChild(textNode);
                    // todo: put this into a geometry group and allow for selection and drag
                    labelGroup.appendChild(labelText);
                    graphPanel.entitySvg.entityPositions.put(labelIdString, labelPosition);
//                    graphPanel.doc.getDocumentElement().appendChild(labelText);
                    ((EventTarget) labelText).addEventListener("mousedown", new MouseListenerSvg(graphPanel), false);
                }
            });
        }
    }

    public void updateEntities() {
        UpdateManager updateManager = graphPanel.svgCanvas.getUpdateManager();
        if (updateManager != null) {
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {

                public void run() {
                    drawEntities();
                }
            });
        } else {
            // on the first draw there will be on update manager
            drawEntities();
        }
    }

    public void drawEntities() { // todo: this is public due to the requirements of saving files by users, but this should be done in a more thread safe way.
        graphPanel.dataStoreSvg.graphData.setPadding(graphPanel.graphPanelSize);
        int vSpacing = graphPanel.graphPanelSize.getVerticalSpacing(); //dataStoreSvg.graphData.gridHeight);
        int hSpacing = graphPanel.graphPanelSize.getHorizontalSpacing(); //dataStoreSvg.graphData.gridWidth);
//        currentWidth = graphPanelSize.getWidth(dataStoreSvg.graphData.gridWidth, hSpacing);
//        currentHeight = graphPanelSize.getHeight(dataStoreSvg.graphData.gridHeight, vSpacing);
        try {
            Element svgRoot = graphPanel.doc.getDocumentElement();
            Element diagramGroupNode = graphPanel.doc.getElementById("DiagramGroup");
            if (diagramGroupNode == null) { // make sure the diagram group exists
                diagramGroupNode = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "g");
                diagramGroupNode.setAttribute("id", "DiagramGroup");
                svgRoot.appendChild(diagramGroupNode);
            }
            Element labelsGroup = graphPanel.doc.getElementById("LabelsGroup");
            if (labelsGroup == null) {
                labelsGroup = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "g");
                labelsGroup.setAttribute("id", "LabelsGroup");
                diagramGroupNode.appendChild(labelsGroup);
            } else if (!labelsGroup.getParentNode().equals(diagramGroupNode)) {
                labelsGroup.getParentNode().removeChild(labelsGroup);
                diagramGroupNode.appendChild(labelsGroup);
            }
            Element relationGroupNode;
            Element entityGroupNode;
//            if (doc == null) {
//            } else {
            Node relationGroupNodeOld = graphPanel.doc.getElementById("RelationGroup");
            Node entityGroupNodeOld = graphPanel.doc.getElementById("EntityGroup");
            // remove the old relation lines
            relationGroupNode = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "g");
            relationGroupNode.setAttribute("id", "RelationGroup");
            diagramGroupNode.insertBefore(relationGroupNode, labelsGroup);
            if (relationGroupNodeOld != null) {
                relationGroupNodeOld.getParentNode().removeChild(relationGroupNodeOld);
            }
            // remove the old entity symbols making sure the entities sit above the relations but below the labels
            entityGroupNode = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "g");
            entityGroupNode.setAttribute("id", "EntityGroup");
            diagramGroupNode.insertBefore(entityGroupNode, labelsGroup);
            if (entityGroupNodeOld != null) {
                entityGroupNodeOld.getParentNode().removeChild(entityGroupNodeOld);
            }
            // remove old kin diagram data
            NodeList dataNodes = graphPanel.doc.getElementsByTagNameNS("http://mpi.nl/tla/kin", "KinDiagramData");
            for (int nodeCounter = 0; nodeCounter < dataNodes.getLength(); nodeCounter++) {
                dataNodes.item(nodeCounter).getParentNode().removeChild(dataNodes.item(nodeCounter));
            }
            graphPanel.dataStoreSvg.graphData.placeAllNodes(graphPanel, graphPanel.entitySvg.entityPositions);
            resizeCanvas(svgRoot, diagramGroupNode);

//            entitySvg.removeOldEntities(relationGroupNode);
            // todo: find the real text size from batik
            // store the selected kin type strings and other data in the dom
            graphPanel.dataStoreSvg.storeAllData(graphPanel.doc);
//            new GraphPlacementHandler().placeAllNodes(this, dataStoreSvg.graphData.getDataNodes(), entityGroupNode, hSpacing, vSpacing);
            for (EntityData currentNode : graphPanel.dataStoreSvg.graphData.getDataNodes()) {
                if (currentNode.isVisible) {
                    entityGroupNode.appendChild(graphPanel.entitySvg.createEntitySymbol(graphPanel, currentNode));
                }
            }
            for (EntityData currentNode : graphPanel.dataStoreSvg.graphData.getDataNodes()) {
                if (currentNode.isVisible) {
                    for (EntityRelation graphLinkNode : currentNode.getVisiblyRelateNodes()) {
                        if ((graphPanel.dataStoreSvg.showKinTermLines || graphLinkNode.relationLineType != DataTypes.RelationLineType.kinTermLine)
                                && (graphPanel.dataStoreSvg.showSanguineLines || graphLinkNode.relationLineType != DataTypes.RelationLineType.sanguineLine)) {
                            new RelationSvg().insertRelation(graphPanel, graphPanel.svgNameSpace, relationGroupNode, currentNode, graphLinkNode, hSpacing, vSpacing);
                        }
                    }
                }
            }
            // todo: allow the user to set an entity as the provider of new dat being entered, this selected user can then be added to each field that is updated as the providence for that data. this would be best done in a cascading fashon so that there is a default informant for the entity and if required for sub nodes and fields
//            ArbilComponentBuilder.savePrettyFormatting(graphPanel.doc, new File("/Users/petwit/Documents/SharedInVirtualBox/mpi-co-svn-mpi-nl/LAT/Kinnate/trunk/desktop/src/main/resources/output.svg"));
//        svgCanvas.revalidate();
//            svgUpdateHandler.updateSvgSelectionHighlights(); // todo: does this rsolve the issue after an update that the selection highlight is lost but the selection is still made?
//        zoomDrawing();
//            if (zoomAffineTransform != null) {
//                // re apply the last zoom
//                // todo: asses why this does not work
//                svgCanvas.setRenderingTransform(zoomAffineTransform);
//            };
        } catch (DOMException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        }
        // todo: this repaint might not resolve all cases of redraw issues
        graphPanel.svgCanvas.repaint(); // make sure no remnants are left over after the last redraw
    }
}
