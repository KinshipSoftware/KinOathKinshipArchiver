/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for
 * Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.svg;

import java.util.ArrayList;
import java.util.HashSet;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.kindata.KinPoint;
import nl.mpi.kinnate.kindata.KinRectangle;
import nl.mpi.kinnate.kindata.RelationTypeDefinition;
import nl.mpi.kinnate.kindata.UnsortablePointsException;
import nl.mpi.kinnate.svg.relationlines.RelationRecord;
import nl.mpi.kinnate.svg.relationlines.RelationRecordTable;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import org.apache.batik.dom.svg.SVGOMPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGRect;

/**
 * Document : DragHandler Created on : Mar 31, 2011, 12:52:12 PM
 *
 * @author Peter Withers
 */
public class SvgUpdateHandler {

    private final static Logger logger = LoggerFactory.getLogger(SvgUpdateHandler.class);
    private SvgDiagram svgDiagram;
    private boolean dragUpdateRequired = false;
    private int updateDragNodeX = 0;
    private int updateDragNodeY = 0;
    private int updateDragRelationX = 0;
    private int updateDragRelationY = 0;
    private float[][] dragRemainders = null;
    private float dragScale;
    private boolean resizeRequired = false;
    protected RelationDragHandle relationDragHandle = null;
    private HashSet<UniqueIdentifier> highlightedIdentifiers = new HashSet<UniqueIdentifier>();
    public RelationRecordTable relationRecords;
    private int paddingDistance = 20;
    private KinRectangle dragSelectionRectOnDocument = null;

    public enum GraphicsTypes {

        Label, Circle, Square, Polyline, Ellipse
//                        *  Rectangle <rect>
//                        * Circle <circle>
//                        * Ellipse <ellipse>
//                        * Line <line>
//                        * Polyline <polyline>
//                        * Polygon <polygon>
//                        * Path <path>
    }
//    GraphPanel graphPanel;
//public SvgUpdateHandler(GraphPanel graphPanel) {
//        this.svgDiagram = graphPanel.getSVGDocument();
//        this.graphPanel = graphPanel;

    public SvgUpdateHandler(SvgDiagram svgDiagram) {
        this.svgDiagram = svgDiagram;
    }

    public void clearHighlights() throws KinElementException {
        removeRelationHighLights();
        for (UniqueIdentifier currentIdentifier : highlightedIdentifiers.toArray(new UniqueIdentifier[]{})) {
            // remove old highlights
            KinElement existingHighlight = svgDiagram.doc.getElementById("highlight_" + currentIdentifier.getAttributeIdentifier());
            if (existingHighlight != null) {
                existingHighlight.getParentNode().removeChild(existingHighlight);
            }
            highlightedIdentifiers.remove(currentIdentifier);
        }
    }

    private void removeRelationHighLights() throws KinElementException {
        // this must be only called from within a svg runnable
        KinElement relationOldHighlightGroup = svgDiagram.doc.getElementById("RelationHighlightGroup");
        if (relationOldHighlightGroup != null) {
            // remove the relation highlight group
            relationOldHighlightGroup.getParentNode().removeChild(relationOldHighlightGroup);
        }
    }

    private void removeEntityHighLights() throws KinElementException {
        for (KinElement entityHighlightGroup = svgDiagram.doc.getElementById("highlight"); entityHighlightGroup != null; entityHighlightGroup = svgDiagram.doc.getElementById("highlight")) {
            entityHighlightGroup.getParentNode().removeChild(entityHighlightGroup);
        }
    }

    private void createRelationLineHighlights(KinElement entityGroup, ArrayList<UniqueIdentifier> selectedGroupId) throws KinElementException {
        // this is used to draw the highlighted relations for the selected entities
        // this must be only called from within a svg runnable
        removeRelationHighLights();
        if (relationRecords != null) {
            if (svgDiagram.getDiagramSettings().highlightRelationLines()) {
                // add highlights for relation lines
                KinElement relationHighlightGroup = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "g");
                relationHighlightGroup.setAttribute("id", "RelationHighlightGroup");
                entityGroup.getParentNode().insertBefore(relationHighlightGroup, entityGroup);
                // create new relation lines for each highlight in a separate group so that they can all be removed after the drag
                for (RelationRecord relationRecord : relationRecords.getRecordsForSelection(selectedGroupId)) {
                    final String lineWidth = Integer.toString(relationRecord.lineWidth);
                    final String pathPointsString = relationRecord.getPathPointsString();
                    // add a white background
                    KinElement highlightBackgroundLine = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "path");
                    highlightBackgroundLine.setAttribute("stroke-width", lineWidth);
                    highlightBackgroundLine.setAttribute("fill", "none");
                    highlightBackgroundLine.setAttribute("d", pathPointsString);
                    highlightBackgroundLine.setAttribute("stroke", "white");
                    relationHighlightGroup.appendChild(highlightBackgroundLine);
                    // add a blue dotted line
                    KinElement highlightLine = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "path");
                    highlightLine.setAttribute("stroke-width", lineWidth);
                    highlightLine.setAttribute("fill", "none");
                    highlightLine.setAttribute("d", pathPointsString);
                    highlightLine.setAttribute("stroke", "blue");
                    highlightLine.setAttribute("stroke-dasharray", "3");
                    highlightLine.setAttribute("stroke-dashoffset", "0");
                    relationHighlightGroup.appendChild(highlightLine);
                }
            }
        }
    }

    private void updateDragRelationLines(KinElement entityGroup, float localDragNodeX, float localDragNodeY, ArrayList<UniqueIdentifier> selectedGroupId) throws KinElementException {
        // this is used to draw the lines for the drag handles when the user is creating relations
        // this must be only called from within a svg runnable
        RelationDragHandle localRelationDragHandle = relationDragHandle;
        if (localRelationDragHandle != null) {
            if (localRelationDragHandle instanceof GraphicsDragHandle) {
                ((GraphicsDragHandle) localRelationDragHandle).updatedElement(localDragNodeX, localDragNodeY, paddingDistance);
            } else {
                // add highlights for relation lines that would be created by the user action
                float dragNodeX = localRelationDragHandle.getTranslatedX(localDragNodeX);
                float dragNodeY = localRelationDragHandle.getTranslatedY(localDragNodeY);
                boolean entityConnection = false;
                localRelationDragHandle.targetIdentifier = svgDiagram.entitySvg.getClosestEntity(new float[]{dragNodeX, dragNodeY}, 30, selectedGroupId);
                if (localRelationDragHandle.targetIdentifier != null) {
                    KinPoint closestEntityPoint = svgDiagram.entitySvg.getEntityLocationOffset(localRelationDragHandle.targetIdentifier);
                    dragNodeX = closestEntityPoint.x;
                    dragNodeY = closestEntityPoint.y;
                    entityConnection = true;
                }

                KinElement relationHighlightGroup = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "g");
                relationHighlightGroup.setAttribute("id", "RelationHighlightGroup");
                entityGroup.getParentNode().insertBefore(relationHighlightGroup, entityGroup);
                float vSpacing = svgDiagram.graphPanelSize.getVerticalSpacing();
                float hSpacing = svgDiagram.graphPanelSize.getHorizontalSpacing();
                for (UniqueIdentifier uniqueIdentifier : selectedGroupId) {
                    String dragLineElementId = "dragLine-" + uniqueIdentifier.getAttributeIdentifier();
                    KinPoint egoSymbolPoint;// = svgDiagram.entitySvg.getEntityLocationOffset(uniqueIdentifier);
                    KinPoint parentPoint = null; // = svgDiagram.entitySvg.getAverageParentLocation(uniqueIdentifier);
                    KinPoint dragPoint;
//
                    DataTypes.RelationType directedRelation = localRelationDragHandle.getRelationType();
                    if (directedRelation == DataTypes.RelationType.descendant) { // make sure the ancestral relations are unidirectional
                        egoSymbolPoint = new KinPoint((int) dragNodeX, (int) dragNodeY);
                        dragPoint = svgDiagram.entitySvg.getEntityLocationOffset(uniqueIdentifier);
//                        parentPoint = dragPoint;
                        directedRelation = DataTypes.RelationType.ancestor;
                    } else {
                        egoSymbolPoint = svgDiagram.entitySvg.getEntityLocationOffset(uniqueIdentifier);
                        dragPoint = new KinPoint((int) dragNodeX, (int) dragNodeY);
                    }
                    // try creating a use node for the highlight (these use nodes do not get updated when a node is dragged and the colour attribute is ignored)
//                                            KinElement useNode = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "use");
//                                            useNode.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "#" + polyLineElement.getAttribute("id"));
//                                            useNode.setAttributeNS(null, "stroke", "blue");
//                                            relationHighlightGroup.appendChild(useNode);

                    // try creating a new node based on the original lines attributes (these lines do not get updated when a node is dragged)
                    // as a comprimise these highlighs can be removed when a node is dragged
                    String svgLineType = "path"; // (DataTypes.isSanguinLine(directedRelation)) ? "polyline" : "path";
                    // add a white background
                    KinElement highlightBackgroundLine = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, svgLineType);
                    highlightBackgroundLine.setAttribute("stroke-width", Integer.toString(EntitySvg.strokeWidth));
                    highlightBackgroundLine.setAttribute("fill", "none");
//            highlightBackgroundLine.setAttribute("points", polyLineElement.getAttribute("points"));
                    highlightBackgroundLine.setAttribute("stroke", "white");
                    RelationRecord relationRecord;
                    if (DataTypes.isSanguinLine(directedRelation)) {
                        relationRecord = new RelationRecord(dragLineElementId, directedRelation, vSpacing, egoSymbolPoint, dragPoint, parentPoint);
                    } else {
                        relationRecord = new RelationRecord(localRelationDragHandle.getCurveLineOrientation(), hSpacing, vSpacing, egoSymbolPoint, dragPoint);
                    }
                    highlightBackgroundLine.setAttribute("d", relationRecord.getPathPointsString());
                    relationHighlightGroup.appendChild(highlightBackgroundLine);
                    // add a blue dotted line
                    KinElement highlightLine = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, svgLineType);
                    highlightLine.setAttribute("stroke-width", Integer.toString(EntitySvg.strokeWidth));
                    highlightLine.setAttribute("fill", "none");
//            highlightLine.setAttribute("points", highlightBackgroundLine.getAttribute("points"));
                    highlightLine.setAttribute("d", relationRecord.getPathPointsString());
                    highlightLine.setAttribute("stroke", localRelationDragHandle.getRelationColour());
                    highlightLine.setAttribute("stroke-dasharray", "3");
                    highlightLine.setAttribute("stroke-dashoffset", "0");
                    relationHighlightGroup.appendChild(highlightLine);
                }
                KinElement symbolNode = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "circle");
                symbolNode.setAttribute("cx", Float.toString(dragNodeX));
                symbolNode.setAttribute("cy", Float.toString(dragNodeY));
                if (entityConnection) {
                    symbolNode.setAttribute("r", "20");
                    symbolNode.setAttribute("fill", "none");
                    symbolNode.setAttribute("stroke", localRelationDragHandle.getRelationColour());
                    symbolNode.setAttribute("stroke-width", Integer.toString(EntitySvg.strokeWidth));
                } else {
                    symbolNode.setAttribute("r", "5");
                    symbolNode.setAttribute("fill", localRelationDragHandle.getRelationColour());
                    symbolNode.setAttribute("stroke", "none");
                }
                relationHighlightGroup.appendChild(symbolNode);
            }
        }
//        graphPanel.lineLookUpTable.addLoops();
//        ArbilComponentBuilder.savePrettyFormatting(svgDiagram.doc, new File("/Users/petwit/Documents/SharedInVirtualBox/mpi-co-svn-mpi-nl/LAT/Kinnate/trunk/desktop/src/main/resources/output.svg"));
    }

    protected void addRelationDragHandles(RelationTypeDefinition[] relationTypeDefinitions, KinElement highlightGroupNode, SVGRect bbox, int paddingDistance, EventListener mouseListenerSvg) throws KinElementException {
        // add the standard relation types
        for (DataTypes.RelationType relationType : new DataTypes.RelationType[]{DataTypes.RelationType.ancestor, DataTypes.RelationType.descendant, DataTypes.RelationType.union, DataTypes.RelationType.sibling}) {
            KinElement symbolNode = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "circle");
            switch (relationType) {
                case ancestor:
                    symbolNode.setAttribute("cy", Float.toString(bbox.getY() - paddingDistance));
                    symbolNode.setAttribute("cx", Float.toString(bbox.getX() + bbox.getWidth() / 2));
                    break;
                case descendant:
                    symbolNode.setAttribute("cy", Float.toString(bbox.getY() + bbox.getHeight() + paddingDistance));
                    symbolNode.setAttribute("cx", Float.toString(bbox.getX() + bbox.getWidth() / 2));
                    break;
                case union:
                    symbolNode.setAttribute("cy", Float.toString(bbox.getY() + bbox.getHeight()));
                    symbolNode.setAttribute("cx", Float.toString(bbox.getX() - paddingDistance));
                    break;
                case sibling:
                    symbolNode.setAttribute("cy", Float.toString(bbox.getY()));
                    symbolNode.setAttribute("cx", Float.toString(bbox.getX() - paddingDistance));
                    break;
            }
            symbolNode.setAttribute("r", "5");
            symbolNode.setAttribute("handletype", relationType.name());
            symbolNode.setAttribute("fill", "blue");
            symbolNode.setAttribute("stroke", "none");
            ((EventTarget) ((KinElementImpl) symbolNode).getNode()).addEventListener("mousedown", mouseListenerSvg, false);
            highlightGroupNode.appendChild(symbolNode);
        }
        // add the custom relation types
        float currentCX = bbox.getX() + bbox.getWidth() + paddingDistance;
        float minCY = bbox.getY() - paddingDistance;
        float currentCY = minCY;
        float stepC = 12; //(bbox.getHeight() + paddingDistance) / relationTypeDefinitions.length;
        float maxCY = bbox.getHeight() + paddingDistance;
        for (RelationTypeDefinition typeDefinition : relationTypeDefinitions) {
            // if typeDefinition is null than the type any has been selected, but no drag handle can be given for the type any
            if (typeDefinition != null) {
                for (DataTypes.RelationType relationType : typeDefinition.getRelationType()) {
                    // use a constant spacing between the drag handle dots and to start a new column when each line is full
                    KinElement symbolNode = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "circle");
                    symbolNode.setAttribute("cx", Float.toString(currentCX));
                    symbolNode.setAttribute("cy", Float.toString(currentCY));
                    currentCY += stepC;
                    if (currentCY >= maxCY) {
                        currentCY = minCY;
                        currentCX += stepC;
                    }
                    symbolNode.setAttribute("r", "5");
                    symbolNode.setAttribute("handletype", "custom:" + relationType + ":" + typeDefinition.hashCode());
                    symbolNode.setAttribute("fill", typeDefinition.getLineColour());
                    symbolNode.setAttribute("stroke", "none");
                    ((EventTarget) ((KinElementImpl) symbolNode).getNode()).addEventListener("mousedown", mouseListenerSvg, false);
                    highlightGroupNode.appendChild(symbolNode);
                }
            }
        }
    }

    protected void addGraphicsDragHandles(KinElement highlightGroupNode, UniqueIdentifier targetIdentifier, SVGRect bbox, int paddingDistance, EventListener mouseListenerSvg) throws KinElementException {
        KinElement symbolNode = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "circle");
        symbolNode.setAttribute("cx", Float.toString(bbox.getX() + bbox.getWidth() + paddingDistance));
        symbolNode.setAttribute("cy", Float.toString(bbox.getY() + bbox.getHeight() + paddingDistance));
        symbolNode.setAttribute("r", "5");
        symbolNode.setAttribute("target", targetIdentifier.getAttributeIdentifier());
        symbolNode.setAttribute("fill", "blue");
        symbolNode.setAttribute("stroke", "none");
        ((EventTarget) ((KinElementImpl) symbolNode).getNode()).addEventListener("mousedown", mouseListenerSvg, false);
        highlightGroupNode.appendChild(symbolNode);
    }

    public KinPoint getEntityPointOnDocument(final KinPoint screenLocation) {
        KinElement etityGroup = svgDiagram.doc.getElementById("EntityGroup");
        SVGOMPoint pointOnDocument = getPointOnDocument(screenLocation, etityGroup);
        return new KinPoint((int) pointOnDocument.getX(), (int) pointOnDocument.getY()); // we discard the float precision because the diagram does not need that level of resolution 
    }

    public SVGOMPoint getPointOnScreen(final KinPoint documentLocation, KinElement targetGroupElement) {
        SVGOMPoint pointOnDocument = new SVGOMPoint(documentLocation.x, documentLocation.y);
        SVGMatrix mat = ((SVGLocatable) ((KinElementImpl) targetGroupElement).getNode()).getScreenCTM();  // this gives us the element to screen transform
        return (SVGOMPoint) pointOnDocument.matrixTransform(mat);
    }

    public SVGOMPoint getPointOnDocument(final KinPoint screenLocation, KinElement targetGroupElement) {
        SVGOMPoint pointOnScreen = new SVGOMPoint(screenLocation.x, screenLocation.y);
        SVGMatrix mat = ((SVGLocatable) ((KinElementImpl) targetGroupElement).getNode()).getScreenCTM();  // this gives us the element to screen transform
        mat = mat.inverse();                                // this converts that into the screen to element transform
        return (SVGOMPoint) pointOnScreen.matrixTransform(mat);
    }

    public KinRectangle getRectOnDocument(final KinRectangle screenRectangle, KinElement targetGroupElement) {
        SVGOMPoint pointOnScreen = new SVGOMPoint(screenRectangle.x, screenRectangle.y);
        SVGOMPoint sizeOnScreen = new SVGOMPoint(screenRectangle.width, screenRectangle.height);
        SVGMatrix mat = ((SVGLocatable) ((KinElementImpl) targetGroupElement).getNode()).getScreenCTM();  // this gives us the element to screen transform
        // todo: mat can be null
        mat = mat.inverse();                                // this converts that into the screen to element transform
        SVGPoint pointOnDocument = pointOnScreen.matrixTransform(mat);
        // the diagram keeps the x and y scale equal so we can just use getA here
        SVGPoint sizeOnDocument = new SVGOMPoint(sizeOnScreen.getX() * mat.getA(), sizeOnScreen.getY() * mat.getA());
        System.out.println("sizeOnScreen: " + sizeOnScreen);
        System.out.println("sizeOnDocument: " + sizeOnDocument);
        return new KinRectangle((int) pointOnDocument.getX(), (int) pointOnDocument.getY(), (int) sizeOnDocument.getX(), (int) sizeOnDocument.getY());
    }

    protected void updateSvgSelectionHighlightsI(ArrayList<UniqueIdentifier> selectedGroupId, EventListener mouseListenerSvg) throws KinElementException {
        if (svgDiagram.doc != null) {
//                        for (String groupString : new String[]{"EntityGroup", "LabelsGroup"}) {
//                            KinElement entityGroup = svgDiagram.doc.getElementById(groupString);
            {
                boolean isLeadSelection = true;
                for (UniqueIdentifier currentIdentifier : highlightedIdentifiers.toArray(new UniqueIdentifier[]{})) {
                    // remove old highlights but leave existing selections
                    if (!selectedGroupId.contains(currentIdentifier)) {
                        KinElement existingHighlight = svgDiagram.doc.getElementById("highlight_" + currentIdentifier.getAttributeIdentifier());
                        if (existingHighlight != null) {
                            existingHighlight.getParentNode().removeChild(existingHighlight);
                        }
                        highlightedIdentifiers.remove(currentIdentifier);
                    }
                }
                for (UniqueIdentifier uniqueIdentifier : selectedGroupId.toArray(new UniqueIdentifier[0])) {
                    KinElement selectedGroup = svgDiagram.doc.getElementById(uniqueIdentifier.getAttributeIdentifier());
                    KinElement existingHighlight = svgDiagram.doc.getElementById("highlight_" + uniqueIdentifier.getAttributeIdentifier());
//                            for (Node currentChild = entityGroup.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
//                                if ("g".equals(currentChild.getLocalName())) {
//                                    Node idAttrubite = currentChild.getAttributes().getNamedItem("id");
//                                    if (idAttrubite != null) {
//                                        UniqueIdentifier entityId = new UniqueIdentifier(idAttrubite.getTextContent());
//                                        System.out.println("group id: " + entityId.getAttributeIdentifier());
//                                        Node existingHighlight = null;
                    // find any existing highlight
//                                        for (Node subGoupNode = currentChild.getFirstChild(); subGoupNode != null; subGoupNode = subGoupNode.getNextSibling()) {
//                                            if ("g".equals(subGoupNode.getLocalName())) {
//                                                Node subGroupIdAttrubite = subGoupNode.getAttributes().getNamedItem("id");
//                                                if (subGroupIdAttrubite != null) {
//                                                    if ("highlight".equals(subGroupIdAttrubite.getTextContent())) {
//                                                        existingHighlight = subGoupNode;
//                                                    }
//                                                }
//                                            }
//                                        }
//                                        if (!graphPanel.selectedGroupId.contains(entityId)) {
                    // remove all old highlights
//                                            if (existingHighlight != null) {
//                                                currentChild.removeChild(existingHighlight);
//                                            }
                    // add the current highlights
//                                        } else {
                    if (existingHighlight == null && selectedGroup != null) {
//                                        svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                        SVGRect bbox = ((SVGLocatable) ((KinElementImpl) selectedGroup).getNode()).getBBox();
                        KinElement highlightGroupNode = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "g");
                        ((EventTarget) ((KinElementImpl) highlightGroupNode).getNode()).addEventListener("mousedown", mouseListenerSvg, false);
                        highlightGroupNode.setAttribute("id", "highlight_" + uniqueIdentifier.getAttributeIdentifier());
                        KinElement symbolNode = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "rect");
                        symbolNode.setAttribute("x", Float.toString(bbox.getX() - paddingDistance));
                        symbolNode.setAttribute("y", Float.toString(bbox.getY() - paddingDistance));
                        symbolNode.setAttribute("width", Float.toString(bbox.getWidth() + paddingDistance * 2));
                        symbolNode.setAttribute("height", Float.toString(bbox.getHeight() + paddingDistance * 2));
                        symbolNode.setAttribute("fill", "#999999"); // provide a fill so that the mouse selection extends to the bounding box, but but make it transparent
                        symbolNode.setAttribute("fill-opacity", "0");
                        symbolNode.setAttribute("stroke-width", "1");
                        //if (graphPanel.selectedGroupId.indexOf(entityId) == 0) {
                        if (isLeadSelection) {
                            symbolNode.setAttribute("stroke-dasharray", "3");
                            symbolNode.setAttribute("stroke-dashoffset", "0");
                        } else {
                            symbolNode.setAttribute("stroke-dasharray", "6");
                            symbolNode.setAttribute("stroke-dashoffset", "0");
                        }
                        symbolNode.setAttribute("stroke", "blue");
//                                                if (graphPanel.dataStoreSvg.highlightRelationLines) {
//                                                    // add highlights for relation lines
//                                                    KinElement relationsGroup = svgDiagram.doc.getElementById("RelationGroup");
//                                                    for (Node currentRelation = relationsGroup.getFirstChild(); currentRelation != null; currentRelation = currentRelation.getNextSibling()) {
//                                                        Node dataElement = currentRelation.getFirstChild();
//                                                        NamedNodeMap dataAttributes = dataElement.getAttributes();
//                                                        if (dataAttributes.getNamedItemNS(DataStoreSvg.kinDataNameSpace, "lineType").getNodeValue().equals("sanguineLine")) {
//
////
//                                                            if (entityId.equals(dataAttributes.getNamedItemNS(DataStoreSvg.kinDataNameSpace, "ego").getNodeValue()) || entityId.equals(dataAttributes.getNamedItemNS(DataStoreSvg.kinDataNameSpace, "alter").getNodeValue())) {
//                                                                KinElement  polyLineElement = (KinElement ) dataElement.getNextSibling().getFirstChild();
//
//
//                                                                KinElement  useNode = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "use");
//                                                                useNode.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "#" + polyLineElement.getAttribute("id"));
//                                                                useNode.setAttributeNS(null, "stroke", "blue");
//                                                                highlightGroupNode.appendChild(useNode);
//                                                            }
//                                                        }
//                                                    }
//                                                }
                        // make sure the rect is added before the drag handles, otherwise the rect can block the mouse actions
                        highlightGroupNode.appendChild(symbolNode);
                        if (!uniqueIdentifier.isTransientIdentifier() && !uniqueIdentifier.isGraphicsIdentifier()) {
                            addRelationDragHandles(svgDiagram.getDiagramSettings().getRelationTypeDefinitions(), highlightGroupNode, bbox, paddingDistance, mouseListenerSvg);
                        } else if (uniqueIdentifier.isGraphicsIdentifier()) {
                            if (!"text".equals(selectedGroup.getLocalName())) {
                                // add a drag handle for all graphics but not text nodes
                                addGraphicsDragHandles(highlightGroupNode, uniqueIdentifier, bbox, paddingDistance, mouseListenerSvg);
                            }
                        }
                        if ("g".equals(selectedGroup.getLocalName())) {
                            selectedGroup.appendChild(highlightGroupNode);
                        } else {
                            highlightGroupNode.setAttribute("transform", selectedGroup.getAttribute("transform"));
                            selectedGroup.getParentNode().appendChild(highlightGroupNode);
                        }
                        highlightedIdentifiers.add(uniqueIdentifier);
//                                            }
//                                        }
                    }
                    isLeadSelection = false;
                }
                createRelationLineHighlights(svgDiagram.doc.getElementById("EntityGroup"), selectedGroupId);
            }
        }
        // Em:1:FMDH:1:
//                    ArbilComponentBuilder.savePrettyFormatting(svgDiagram.doc, new File("/Users/petwit/Documents/SharedInVirtualBox/mpi-co-svn-mpi-nl/LAT/Kinnate/trunk/desktop/src/main/resources/output.svg"));
    }

    public void updateMouseDrag(final ArrayList<UniqueIdentifier> selectedGroupId, int updateDragNodeXLocal, int updateDragNodeYLocal) throws KinElementException {
        this.updateDragRelationX = updateDragNodeXLocal;
        this.updateDragRelationY = updateDragNodeYLocal;
        KinElement entityGroup = svgDiagram.doc.getElementById("EntityGroup");
        updateDragNodeXLocal = 0;
        updateDragNodeYLocal = 0;
        while (updateDragNodeXLocal != updateDragRelationX && updateDragNodeYLocal != updateDragRelationY) {
            synchronized (SvgUpdateHandler.this) {
                updateDragNodeXLocal = updateDragRelationX;
                updateDragNodeYLocal = updateDragRelationY;
            }
            removeRelationHighLights();
            removeEntityHighLights();
            updateDragRelationLines(entityGroup, updateDragNodeXLocal, updateDragNodeYLocal, selectedGroupId);
        }
    }

    protected void startDrag(ArrayList<UniqueIdentifier> selectedGroupId) {
        // dragRemainders is used to store the remainder after snap between drag updates
        // reset all remainders
        float[][] tempRemainders = new float[selectedGroupId.size()][];
        for (int dragCounter = 0; dragCounter < tempRemainders.length; dragCounter++) {
            tempRemainders[dragCounter] = new float[]{0, 0};
        }
        synchronized (SvgUpdateHandler.this) {
            dragRemainders = tempRemainders;
//            KinElement  entityGroup = svgDiagram.doc.getElementById("EntityGroup");
            SVGMatrix draggedElementScreenMatrix = ((SVGLocatable) ((KinElementImpl) svgDiagram.doc.getDocumentElement()).getNode()).getScreenCTM().inverse();
            dragScale = draggedElementScreenMatrix.getA(); // the drawing is proportional so only using X is adequate here         
        }
    }

    protected void removeSelectionRectA() throws KinElementException {
        KinElement labelGroup = svgDiagram.doc.getElementById("LabelsGroup");
        KinElement selectionBorderNode = svgDiagram.doc.getElementById("drag_select_highlight");
        labelGroup.removeChild(selectionBorderNode);
    }

    public KinRectangle getSelectionRect() {
        return dragSelectionRectOnDocument;
    }

    protected void drawSelectionRectI(final KinPoint startLocation, final KinPoint currentLocation) throws KinElementException {
        KinElement labelGroup = svgDiagram.doc.getElementById("LabelsGroup");
        SVGOMPoint startOnDocument = getPointOnDocument(startLocation, labelGroup);
        SVGOMPoint currentOnDocument = getPointOnDocument(currentLocation, labelGroup);
        KinElement selectionBorderNode = svgDiagram.doc.getElementById("drag_select_highlight");
        float highlightX = startOnDocument.getX();
        float highlightY = startOnDocument.getY();
        float highlightWidth = currentOnDocument.getX();
        float highlightHeight = currentOnDocument.getY();
        if (highlightX > highlightWidth) {
            float tempValue = highlightWidth;
            highlightWidth = highlightX;
            highlightX = tempValue;
        }
        if (highlightY > highlightHeight) {
            float tempValue = highlightHeight;
            highlightHeight = highlightY;
            highlightY = tempValue;
        }
        highlightHeight = highlightHeight - highlightY;
        highlightWidth = highlightWidth - highlightX;
        dragSelectionRectOnDocument = new KinRectangle((int) highlightX, (int) highlightY, (int) highlightWidth, (int) highlightHeight);
        if (selectionBorderNode == null) {
            selectionBorderNode = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "rect");
            selectionBorderNode.setAttribute("id", "drag_select_highlight");
            selectionBorderNode.setAttribute("fill", "none");
            selectionBorderNode.setAttribute("x", Float.toString(highlightX));
            selectionBorderNode.setAttribute("y", Float.toString(highlightY));
            selectionBorderNode.setAttribute("width", Float.toString(highlightWidth));
            selectionBorderNode.setAttribute("height", Float.toString(highlightHeight));
            selectionBorderNode.setAttribute("stroke-width", "1");
            selectionBorderNode.setAttribute("stroke", "blue");
//                        selectionBorderNode.setAttribute("stroke-dasharray", "6");
//                        selectionBorderNode.setAttribute("stroke-dashoffset", "0");
            labelGroup.appendChild(selectionBorderNode);
        }
        selectionBorderNode.setAttribute("x", Float.toString(highlightX));
        selectionBorderNode.setAttribute("y", Float.toString(highlightY));
        selectionBorderNode.setAttribute("width", Float.toString(highlightWidth));
        selectionBorderNode.setAttribute("height", Float.toString(highlightHeight));
//                    System.out.println("pageBorderNode:" + selectionBorderNode);
    }

    protected void updateDragNodeI(ArrayList<UniqueIdentifier> selectedGroupId, int updateDragNodeXLocal, int updateDragNodeYLocal, final KinRectangle panelBounds) throws KinElementException {
        resizeRequired = true;
        dragUpdateRequired = true;
        updateDragNodeX += updateDragNodeXLocal;
        updateDragNodeY += updateDragNodeYLocal;
//                KinElement  relationOldHighlightGroup = svgDiagram.doc.getElementById("RelationHighlightGroup");
//                if (relationOldHighlightGroup != null) {
//                    // remove the relation highlight group because lines will be out of date when the entities are moved
//                    relationOldHighlightGroup.getParentNode().removeChild(relationOldHighlightGroup);
//                }
        KinRectangle initialGraphRect = svgDiagram.graphData.getGraphSize(svgDiagram.entitySvg.entityPositions);
        KinElement entityGroup = svgDiagram.doc.getElementById("EntityGroup");
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
            if (svgDiagram.doc == null || svgDiagram.graphData == null) {
                logger.error("graphData or the svg document is null, is this an old file format? try redrawing before draging.");
            } else {
//                        if (relationDragHandleType != null) {
//                            // drag relation handles
////                            updateSanguineHighlights(entityGroup);
//                            removeHighLights();
//                            updateDragRelationLines(entityGroup, updateDragNodeXInner, updateDragNodeYInner);
//                        } else {
                // drag the entities
                boolean allRealtionsSelected = true;
                relationLoop:
                for (EntityData selectedEntity : svgDiagram.graphData.getDataNodes()) {
                    if (selectedEntity.isVisible
                            && selectedGroupId.contains(selectedEntity.getUniqueIdentifier())) {
                        for (EntityData relatedEntity : selectedEntity.getVisiblyRelated()) {
                            if (relatedEntity.isVisible && !selectedGroupId.contains(relatedEntity.getUniqueIdentifier())) {
                                allRealtionsSelected = false;
                                break relationLoop;
                            }
                        }
                    }
                }
                int dragCounter = 0;
                // todo: concurent modification issue here in graphPanel.selectedGroupId when debugging
                for (UniqueIdentifier entityId : selectedGroupId) {
                    // store the remainder after snap for re use on each update
                    synchronized (SvgUpdateHandler.this) {
                        if (dragRemainders.length > dragCounter) {
//                                        System.out.println("drag remainder: " + updateDragNodeXInner + " : " + dragRemainders[dragCounter][0]);
                            dragRemainders[dragCounter] = svgDiagram.entitySvg.moveEntity(svgDiagram, entityId, updateDragNodeXInner, dragRemainders[dragCounter][0], updateDragNodeYInner, dragRemainders[dragCounter][1], svgDiagram.getDiagramSettings().snapToGrid(), dragScale, allRealtionsSelected);
                        }
                    }
                    dragCounter++;
                }
//                    KinElement  entityGroup = doc.getElementById("EntityGroup");
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
                int vSpacing = svgDiagram.graphPanelSize.getVerticalSpacing(); // graphPanel.dataStoreSvg.graphData.gridHeight);
                int hSpacing = svgDiagram.graphPanelSize.getHorizontalSpacing(); // graphPanel.dataStoreSvg.graphData.gridWidth);
                new RelationSvg().updateRelationLines(svgDiagram, relationRecords, selectedGroupId, hSpacing, vSpacing);
                createRelationLineHighlights(entityGroup, selectedGroupId);
                final KinRectangle currentGraphRect = svgDiagram.graphData.getGraphSize(svgDiagram.entitySvg.entityPositions);
                if (!initialGraphRect.contains(currentGraphRect)) {
                    KinElement svgRoot = svgDiagram.doc.getDocumentElement();
                    KinElement diagramGroupNode = svgDiagram.doc.getElementById("DiagramGroup");
                    resizeCanvas(svgRoot, diagramGroupNode, panelBounds);
                }
                //new CmdiComponentBuilder().savePrettyFormatting(doc, new File("/Users/petwit/Documents/SharedInVirtualBox/mpi-co-svn-mpi-nl/LAT/Kinnate/trunk/src/main/resources/output.svg"));
            }
            // graphPanel.updateCanvasSize(); // updating the canvas size here is too slow so it is moved into the drag ended 
//                    if (graphPanel.dataStoreSvg.graphData.isRedrawRequired()) { // this has been abandoned in favour of preventing dragging past zero
            // todo: update the position of all nodes
            // todo: any labels and other non entity graphics must also be taken into account here
//                        for (EntityData selectedEntity : graphPanel.dataStoreSvg.graphData.getDataNodes()) {
//                            if (selectedEntity.isVisible) {
//                                svgDiagram.entitySvg.moveEntity(graphPanel, selectedEntity.getUniqueIdentifier(), updateDragNodeXInner + dragRemainders[dragCounter][0], updateDragNodeYInner + dragRemainders[dragCounter][1], graphPanel.dataStoreSvg.snapToGrid, true);
//                            }
//                        }
//                    }
            synchronized (SvgUpdateHandler.this) {
                continueUpdating = dragUpdateRequired;
            }
        }
    }

    private void resizeCanvas(KinElement svgRoot, KinElement diagramGroupNode, final KinRectangle panelBounds) throws KinElementException {
//        svgRoot.setAttribute("width", "100%");
//        svgRoot.setAttribute("height", "100%");
//        diagramGroupNode.setAttribute("transform", null);       
        KinRectangle graphSize = svgDiagram.graphData.getGraphSize(svgDiagram.entitySvg.entityPositions);
        // set the diagram offset so that no element is less than zero
//        diagramGroupNode.setAttribute("transform", "translate(" + Integer.toString(-graphSize.x) + ", " + Integer.toString(-graphSize.y) + ")");
        diagramGroupNode.removeAttribute("transform");
//        System.out.println("graphSize: " + graphSize.x + " : " + graphSize.y + " : " + graphSize.width + " : " + graphSize.height);
//        SVGRect bbox = ((SVGLocatable) svgRoot).getBBox();
//        System.out.println("bbox X: " + bbox.getX());
//        System.out.println("bbox Y: " + bbox.getY());
//        System.out.println("bbox W: " + bbox.getWidth());
//        System.out.println("bbox H: " + bbox.getHeight());

//        graphSize.x = graphSize.x + (int) bbox.getX();
//        graphSize.y = graphSize.y + (int) bbox.getY();
//        svgRoot.setAttribute("viewBox", Float.toString(bbox.getX()) + " " + Float.toString(bbox.getY()) + " " + Float.toString(bbox.getWidth()) + " " + Float.toString(bbox.getHeight()));
//        viewBox="0.0 0.0 1024.0 768.0"
        // Set the width and height attributes on the root 'svg' element.
        System.out.println("graphSize: " + graphSize);
//        svgRoot.setAttribute("width", Integer.toString(graphSize.width - graphSize.x));
//        svgRoot.setAttribute("height", Integer.toString(graphSize.height - graphSize.y));

        svgRoot.removeAttribute("width");
        svgRoot.removeAttribute("height");
        final int maxAutoScale = 2;
        final int horizontalGap = panelBounds.width / maxAutoScale - graphSize.width;
        final int verticalGap = panelBounds.height / maxAutoScale - graphSize.height;
        int emptyBorder = (horizontalGap < verticalGap) ? horizontalGap : verticalGap;
        if (emptyBorder < 0) {
            emptyBorder = 0;
        }
        svgRoot.setAttribute("viewBox", (graphSize.x - 5 - emptyBorder) + " " + (graphSize.y - 5 - emptyBorder) + " " + (graphSize.width + 10 + emptyBorder * 2) + " " + (graphSize.height + 10 + emptyBorder * 2));
        if (svgDiagram.getDiagramSettings().showDiagramBorder()) {
            // draw a grey rectangle to show the diagram bounds
            KinElement pageBorderNode = svgDiagram.doc.getElementById("PageBorder");
            if (pageBorderNode == null) {
                pageBorderNode = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "rect");
                pageBorderNode.setAttribute("id", "PageBorder");
                pageBorderNode.setAttribute("x", Float.toString(graphSize.x + 2 - emptyBorder));
                pageBorderNode.setAttribute("y", Float.toString(graphSize.y + 2 - emptyBorder));
                pageBorderNode.setAttribute("width", Float.toString(graphSize.width - 4 + emptyBorder * 2));
                pageBorderNode.setAttribute("height", Float.toString(graphSize.height - 4 + emptyBorder * 2));
                pageBorderNode.setAttribute("fill", "none");
                pageBorderNode.setAttribute("stroke-width", "2");
                pageBorderNode.setAttribute("stroke", "grey");
                diagramGroupNode.appendChild(pageBorderNode);
            } else {
                pageBorderNode.setAttribute("x", Float.toString(graphSize.x + 2 - emptyBorder));
                pageBorderNode.setAttribute("y", Float.toString(graphSize.y + 2 - emptyBorder));
                pageBorderNode.setAttribute("width", Float.toString(graphSize.width - 4 + emptyBorder * 2));
                pageBorderNode.setAttribute("height", Float.toString(graphSize.height - 4 + emptyBorder * 2));
            }
            // end draw a grey rectangle to show the diagram bounds
        } else {
            KinElement pageBorderNode = svgDiagram.doc.getElementById("PageBorder");
            if (pageBorderNode != null) {
                pageBorderNode.getParentNode().removeChild(pageBorderNode);
            }
        }
    }

    public void updateCanvasSizeI(final boolean required, final KinRectangle panelBounds) throws KinElementException {
        if (resizeRequired || required) { // todo: check the use of resizeRequired here
            this.resizeRequired = false;
            KinElement svgRoot = svgDiagram.doc.getDocumentElement();
            KinElement diagramGroupNode = svgDiagram.doc.getElementById("DiagramGroup");
            resizeCanvas(svgRoot, diagramGroupNode, panelBounds);
        }
    }

    public void deleteGraphicsI(UniqueIdentifier uniqueIdentifier) throws KinElementException {
        final KinElement graphicsElement = svgDiagram.doc.getElementById(uniqueIdentifier.getAttributeIdentifier());
        final KinElement existingHighlight = svgDiagram.doc.getElementById("highlight_" + uniqueIdentifier.getAttributeIdentifier());
        final KinElement parentElement = graphicsElement.getParentNode();
        svgDiagram.entitySvg.entityPositions.remove(uniqueIdentifier);
        parentElement.removeChild(graphicsElement);
        if (existingHighlight != null) {
            existingHighlight.getParentNode().removeChild(existingHighlight);
        }
    }

    public void addGraphicsI(final GraphicsTypes graphicsType, final KinPoint locationOnScreen, EventListener mouseListenerSvg, final KinRectangle panelBounds) throws KinElementException {
        KinElement labelText;
        switch (graphicsType) {
            case Circle:
                labelText = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "circle");
                labelText.setAttribute("r", "100");
                labelText.setAttribute("fill", "#ffffff");
                labelText.setAttribute("stroke", "#000000");
                labelText.setAttribute("stroke-width", "2");
                break;
            case Ellipse:
                labelText = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "ellipse");
                labelText.setAttribute("rx", "100");
                labelText.setAttribute("ry", "100");
                labelText.setAttribute("fill", "#ffffff");
                labelText.setAttribute("stroke", "#000000");
                labelText.setAttribute("stroke-width", "2");
                break;
            case Label:
                labelText = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "text");
                labelText.setAttribute("fill", "#000000");
                labelText.setAttribute("stroke-width", "0");
                labelText.setAttribute("font-size", "28");
                KinElement textNode = svgDiagram.doc.createTextNode("Label");
                labelText.appendChild(textNode);
                break;
            case Polyline:
                labelText = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "polyline");
                break;
            case Square:
                labelText = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "rect");
                labelText.setAttribute("width", "100");
                labelText.setAttribute("height", "100");
                labelText.setAttribute("fill", "#ffffff");
                labelText.setAttribute("stroke", "#000000");
                labelText.setAttribute("stroke-width", "2");
                break;
            default:
                return;
        }
//                        *  Rectangle <rect>
//                        * Circle <circle>
//                        * Ellipse <ellipse>
//                        * Line <line>
//                        * Polyline <polyline>
//                        * Polygon <polygon>
//                        * Path <path>

        UniqueIdentifier labelId = new UniqueIdentifier(UniqueIdentifier.IdentifierType.gid);
        labelText.setAttribute("id", labelId.getAttributeIdentifier());
        // put this into the geometry group or the label group depending on its type so that labels sit above entitis and graphics sit below entities
        KinElement targetGroup;
        if (graphicsType.equals(GraphicsTypes.Label)) {
            targetGroup = svgDiagram.doc.getElementById("LabelsGroup");
        } else {
            targetGroup = svgDiagram.doc.getElementById("GraphicsGroup");
        }
        SVGOMPoint pointOnDocument = getPointOnDocument(locationOnScreen, targetGroup);
        KinPoint labelPosition = new KinPoint((int) pointOnDocument.getX(), (int) pointOnDocument.getY()); // we discard the float precision because the diagram does not need that level of resolution 
        final String transformAttribute = "translate(" + Integer.toString(labelPosition.x) + ", " + Integer.toString(labelPosition.y) + ")";
        System.out.println("transformAttribute:" + transformAttribute);
        labelText.setAttribute("transform", transformAttribute);
        targetGroup.appendChild(labelText);
        svgDiagram.entitySvg.entityPositions.put(labelId, new KinPoint(labelPosition));
        ((EventTarget) ((KinElementImpl) labelText).getNode()).addEventListener("mousedown", mouseListenerSvg, false);
        resizeCanvas(svgDiagram.doc.getDocumentElement(), svgDiagram.doc.getElementById("DiagramGroup"), panelBounds);
    }

    public void drawEntities(final KinRectangle panelBounds) throws DOMException, OldFormatException, UnsortablePointsException, KinElementException { // todo: this is public due to the requirements of saving files by users, but this should be done in a more thread safe way.
        svgDiagram.graphData.setPadding(svgDiagram.graphPanelSize);
        relationRecords = new RelationRecordTable();
        int vSpacing = svgDiagram.graphPanelSize.getVerticalSpacing(); //dataStoreSvg.graphData.gridHeight);
        int hSpacing = svgDiagram.graphPanelSize.getHorizontalSpacing(); //dataStoreSvg.graphData.gridWidth);
//        currentWidth = graphPanelSize.getWidth(dataStoreSvg.graphData.gridWidth, hSpacing);
//        currentHeight = graphPanelSize.getHeight(dataStoreSvg.graphData.gridHeight, vSpacing);
        removeRelationHighLights();
        KinElement svgRoot = svgDiagram.doc.getDocumentElement();
        KinElement diagramGroupNode = svgDiagram.doc.getElementById("DiagramGroup");
        if (diagramGroupNode == null) { // make sure the diagram group exists
            diagramGroupNode = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "g");
            diagramGroupNode.setAttribute("id", "DiagramGroup");
            svgRoot.appendChild(diagramGroupNode);
        }
        KinElement labelsGroup = svgDiagram.doc.getElementById("LabelsGroup");
        if (labelsGroup == null) {
            labelsGroup = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "g");
            labelsGroup.setAttribute("id", "LabelsGroup");
            diagramGroupNode.appendChild(labelsGroup);
        } else if (!labelsGroup.getParentNode().equals(diagramGroupNode)) {
            labelsGroup.getParentNode().removeChild(labelsGroup);
            diagramGroupNode.appendChild(labelsGroup);
        }
        KinElement relationGroupNode;
        KinElement entityGroupNode;
//            if (doc == null) {
//            } else {
        KinElement relationGroupNodeOld = svgDiagram.doc.getElementById("RelationGroup");
        KinElement entityGroupNodeOld = svgDiagram.doc.getElementById("EntityGroup");
        // remove the old relation lines
        relationGroupNode = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "g");
        relationGroupNode.setAttribute("id", "RelationGroup");
        diagramGroupNode.insertBefore(relationGroupNode, labelsGroup);
        if (relationGroupNodeOld != null) {
            relationGroupNodeOld.getParentNode().removeChild(relationGroupNodeOld);
        }
        // remove the old entity symbols making sure the entities sit above the relations but below the labels
        entityGroupNode = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "g");
        entityGroupNode.setAttribute("id", "EntityGroup");
        diagramGroupNode.insertBefore(entityGroupNode, labelsGroup);
        if (entityGroupNodeOld != null) {
            entityGroupNodeOld.getParentNode().removeChild(entityGroupNodeOld);
        }
        svgDiagram.graphData.placeAllNodes(svgDiagram.entitySvg.entityPositions);
        resizeCanvas(svgRoot, diagramGroupNode, panelBounds);

//            entitySvg.removeOldEntities(relationGroupNode);
        // todo: find the real text size from batik
        // store the selected kin type strings and other data in the dom
        svgDiagram.getDiagramSettings().storeAllData(svgDiagram.doc);
//            new GraphPlacementHandler().placeAllNodes(this, dataStoreSvg.graphData.getDataNodes(), entityGroupNode, hSpacing, vSpacing);
        for (EntityData currentNode : svgDiagram.graphData.getDataNodes()) {
            if (currentNode.isVisible) {
                entityGroupNode.appendChild(svgDiagram.entitySvg.createEntitySymbol(svgDiagram, currentNode));
            }
        }
        for (EntityData currentNode : svgDiagram.graphData.getDataNodes()) {
            if (currentNode.isVisible) {
                for (EntityRelation graphLinkNode : currentNode.getAllRelations()) {
                    if ((svgDiagram.getDiagramSettings().showKinTermLines() || graphLinkNode.getRelationType() != DataTypes.RelationType.kinterm)
                            && (svgDiagram.getDiagramSettings().showSanguineLines() || !DataTypes.isSanguinLine(graphLinkNode.getRelationType()))
                            && (graphLinkNode.getAlterNode() != null && graphLinkNode.getAlterNode().isVisible)) {
                        relationRecords.addRecord(svgDiagram.getDiagramSettings(), svgDiagram, currentNode, graphLinkNode, hSpacing, vSpacing, EntitySvg.strokeWidth);
                    }
                }
            }
        }
        new RelationSvg().createRelationElements(svgDiagram, relationRecords, relationGroupNode);
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
    }
}
