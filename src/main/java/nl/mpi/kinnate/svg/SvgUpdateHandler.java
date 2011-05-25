package nl.mpi.kinnate.svg;

import java.awt.geom.AffineTransform;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.kinnate.KinTermSavePanel;
import org.apache.batik.bridge.UpdateManager;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
                        int dragCounter = 0;
                        for (String entityId : graphPanel.selectedGroupId) {
                            // store the remainder after snap for re use on each update
                            dragRemainders[dragCounter] = graphPanel.entitySvg.moveEntity(graphPanel.doc, entityId, updateDragNodeXInner + dragRemainders[dragCounter][0], updateDragNodeYInner + dragRemainders[dragCounter][1], graphPanel.dataStoreSvg.snapToGrid);
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
                        int vSpacing = graphPanel.graphPanelSize.getVerticalSpacing(graphPanel.dataStoreSvg.graphData.gridHeight);
                        int hSpacing = graphPanel.graphPanelSize.getHorizontalSpacing(graphPanel.dataStoreSvg.graphData.gridWidth);
                        new RelationSvg().updateRelationLines(graphPanel, graphPanel.selectedGroupId, graphPanel.svgNameSpace, hSpacing, vSpacing);
                        //new CmdiComponentBuilder().savePrettyFormatting(doc, new File("/Users/petwit/Documents/SharedInVirtualBox/mpi-co-svn-mpi-nl/LAT/Kinnate/trunk/src/main/resources/output.svg"));
                    }
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

    public void addLabel(final String labelString, final float xPos, final float yPos) {
        UpdateManager updateManager = graphPanel.svgCanvas.getUpdateManager();
        if (updateManager != null) {
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {

                public void run() {
                    Element labelGroup = graphPanel.doc.getElementById("LabelsGroup");
                    Element labelText = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "text");
                    labelText.setAttribute("x", Float.toString(xPos));
                    labelText.setAttribute("y", Float.toString(yPos));
                    labelText.setAttribute("fill", "black");
                    labelText.setAttribute("stroke-width", "0");
                    labelText.setAttribute("font-size", "28");
                    labelText.setAttribute("id", "label" + labelGroup.getChildNodes().getLength());
                    Text textNode = graphPanel.doc.createTextNode(labelString);
                    labelText.appendChild(textNode);
                    // todo: put this into a geometry group and allow for selection and drag
                    labelGroup.appendChild(labelText);
//                    graphPanel.doc.getDocumentElement().appendChild(labelText);
                    ((EventTarget) labelText).addEventListener("mousedown", new MouseListenerSvg(graphPanel), false);
                }
            });
        }
    }
}
