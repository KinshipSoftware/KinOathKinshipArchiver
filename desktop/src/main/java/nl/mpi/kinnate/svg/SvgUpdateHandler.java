package nl.mpi.kinnate.svg;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashSet;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.KinTermSavePanel;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.kindata.RelationTypeDefinition;
import nl.mpi.kinnate.kindata.RelationTypeDefinition.CurveLineOrientation;
import nl.mpi.kinnate.kintypestrings.KinType;
import nl.mpi.kinnate.svg.relationlines.RelationRecord;
import nl.mpi.kinnate.svg.relationlines.RelationRecordTable;
import nl.mpi.kinnate.uniqueidentifiers.IdentifierException;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import org.apache.batik.bridge.UpdateManager;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGRect;

/**
 * Document : DragHandler
 * Created on : Mar 31, 2011, 12:52:12 PM
 * Author : Peter Withers
 */
public class SvgUpdateHandler {

    private GraphPanel graphPanel;
    private KinTermSavePanel kinTermSavePanel;
    private MessageDialogHandler dialogHandler;
    private boolean dragUpdateRequired = false;
    private boolean threadRunning = false;
    private boolean relationThreadRunning = false;
    private int updateDragNodeX = 0;
    private int updateDragNodeY = 0;
    private int updateDragRelationX = 0;
    private int updateDragRelationY = 0;
    private float[][] dragRemainders = null;
    private boolean resizeRequired = false;
    protected RelationDragHandle relationDragHandle = null;
    private HashSet<UniqueIdentifier> highlightedIdentifiers = new HashSet<UniqueIdentifier>();
    public RelationRecordTable relationRecords;
    private boolean oldFormatWarningShown = false;

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

    public SvgUpdateHandler(GraphPanel graphPanel, KinTermSavePanel kinTermSavePanel, MessageDialogHandler dialogHandler) {
        this.graphPanel = graphPanel;
        this.kinTermSavePanel = kinTermSavePanel;
        this.dialogHandler = dialogHandler;
    }

    public void clearHighlights() {
        removeRelationHighLights();
        for (UniqueIdentifier currentIdentifier : highlightedIdentifiers.toArray(new UniqueIdentifier[]{})) {
            // remove old highlights
            Element existingHighlight = graphPanel.doc.getElementById("highlight_" + currentIdentifier.getAttributeIdentifier());
            if (existingHighlight != null) {
                existingHighlight.getParentNode().removeChild(existingHighlight);
            }
            highlightedIdentifiers.remove(currentIdentifier);
        }
    }

    private void removeRelationHighLights() {
        // this must be only called from within a svg runnable
        Element relationOldHighlightGroup = graphPanel.doc.getElementById("RelationHighlightGroup");
        if (relationOldHighlightGroup != null) {
            // remove the relation highlight group
            relationOldHighlightGroup.getParentNode().removeChild(relationOldHighlightGroup);
        }
    }

    private void removeEntityHighLights() {
        for (Element entityHighlightGroup = graphPanel.doc.getElementById("highlight"); entityHighlightGroup != null; entityHighlightGroup = graphPanel.doc.getElementById("highlight")) {
            entityHighlightGroup.getParentNode().removeChild(entityHighlightGroup);
        }
    }

    private void createRelationLineHighlights(Element entityGroup) {
        // this is used to draw the highlighted relations for the selected entities
        // this must be only called from within a svg runnable
        removeRelationHighLights();
        if (graphPanel.dataStoreSvg.highlightRelationLines) {
            // add highlights for relation lines
            Element relationHighlightGroup = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "g");
            relationHighlightGroup.setAttribute("id", "RelationHighlightGroup");
            entityGroup.getParentNode().insertBefore(relationHighlightGroup, entityGroup);
            // create new relation lines for each highlight in a separate group so that they can all be removed after the drag
            for (RelationRecord relationRecord : relationRecords.getRecordsForSelection(graphPanel.selectedGroupId)) {
                final String lineWidth = Integer.toString(relationRecord.lineWidth);
                final String pathPointsString = relationRecord.getPathPointsString();
                // add a white background
                Element highlightBackgroundLine = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "path");
                highlightBackgroundLine.setAttribute("stroke-width", lineWidth);
                highlightBackgroundLine.setAttribute("fill", "none");
                highlightBackgroundLine.setAttribute("d", pathPointsString);
                highlightBackgroundLine.setAttribute("stroke", "white");
                relationHighlightGroup.appendChild(highlightBackgroundLine);
                // add a blue dotted line
                Element highlightLine = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "path");
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

    private void updateDragRelationLines(Element entityGroup, float localDragNodeX, float localDragNodeY) {
        // this is used to draw the lines for the drag handles when the user is creating relations
        // this must be only called from within a svg runnable
        RelationDragHandle localRelationDragHandle = relationDragHandle;
        if (localRelationDragHandle != null) {
            if (localRelationDragHandle instanceof GraphicsDragHandle) {
                ((GraphicsDragHandle) localRelationDragHandle).updatedElement(localDragNodeX, localDragNodeY);
            } else {
                // add highlights for relation lines that would be created by the user action
                float dragNodeX = localRelationDragHandle.getTranslatedX(localDragNodeX);
                float dragNodeY = localRelationDragHandle.getTranslatedY(localDragNodeY);

                localRelationDragHandle.targetIdentifier = graphPanel.entitySvg.getClosestEntity(new float[]{dragNodeX, dragNodeY}, 30, graphPanel.selectedGroupId);
                if (localRelationDragHandle.targetIdentifier != null) {
                    float[] closestEntityPoint = graphPanel.entitySvg.getEntityLocation(localRelationDragHandle.targetIdentifier);
                    dragNodeX = closestEntityPoint[0];
                    dragNodeY = closestEntityPoint[1];
                }

                Element relationHighlightGroup = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "g");
                relationHighlightGroup.setAttribute("id", "RelationHighlightGroup");
                entityGroup.getParentNode().insertBefore(relationHighlightGroup, entityGroup);
                float vSpacing = graphPanel.graphPanelSize.getVerticalSpacing();
                float hSpacing = graphPanel.graphPanelSize.getHorizontalSpacing();
                for (UniqueIdentifier uniqueIdentifier : graphPanel.selectedGroupId) {
                    String dragLineElementId = "dragLine-" + uniqueIdentifier.getAttributeIdentifier();
                    float[] egoSymbolPoint;// = graphPanel.entitySvg.getEntityLocation(uniqueIdentifier);
                    float[] parentPoint; // = graphPanel.entitySvg.getAverageParentLocation(uniqueIdentifier);
                    float[] dragPoint;
//
                    DataTypes.RelationType directedRelation = localRelationDragHandle.getRelationType();
                    if (directedRelation == DataTypes.RelationType.descendant) { // make sure the ancestral relations are unidirectional
                        egoSymbolPoint = new float[]{dragNodeX, dragNodeY};
                        dragPoint = graphPanel.entitySvg.getEntityLocation(uniqueIdentifier);
                        parentPoint = dragPoint;
                        directedRelation = DataTypes.RelationType.ancestor;
                    } else {
                        egoSymbolPoint = graphPanel.entitySvg.getEntityLocation(uniqueIdentifier);
                        dragPoint = new float[]{dragNodeX, dragNodeY};
                        parentPoint = dragPoint;
                    }
                    // try creating a use node for the highlight (these use nodes do not get updated when a node is dragged and the colour attribute is ignored)
//                                            Element useNode = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "use");
//                                            useNode.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "#" + polyLineElement.getAttribute("id"));
//                                            useNode.setAttributeNS(null, "stroke", "blue");
//                                            relationHighlightGroup.appendChild(useNode);

                    // try creating a new node based on the original lines attributes (these lines do not get updated when a node is dragged)
                    // as a comprimise these highlighs can be removed when a node is dragged
                    String svgLineType = "path"; // (DataTypes.isSanguinLine(directedRelation)) ? "polyline" : "path";
                    // add a white background
                    Element highlightBackgroundLine = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, svgLineType);
                    highlightBackgroundLine.setAttribute("stroke-width", Integer.toString(EntitySvg.strokeWidth));
                    highlightBackgroundLine.setAttribute("fill", "none");
//            highlightBackgroundLine.setAttribute("points", polyLineElement.getAttribute("points"));
                    highlightBackgroundLine.setAttribute("stroke", "white");
                    try {
                        RelationRecord relationRecord;
                        if (DataTypes.isSanguinLine(directedRelation)) {
                            relationRecord = new RelationRecord(dragLineElementId, directedRelation, vSpacing, egoSymbolPoint[0], egoSymbolPoint[1], dragPoint[0], dragPoint[1], parentPoint);
                        } else {
                            relationRecord = new RelationRecord(localRelationDragHandle.getCurveLineOrientation(), hSpacing, vSpacing, egoSymbolPoint[0], egoSymbolPoint[1], dragPoint[0], dragPoint[1]);
                        }
                        highlightBackgroundLine.setAttribute("d", relationRecord.getPathPointsString());
                        relationHighlightGroup.appendChild(highlightBackgroundLine);
                        // add a blue dotted line
                        Element highlightLine = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, svgLineType);
                        highlightLine.setAttribute("stroke-width", Integer.toString(EntitySvg.strokeWidth));
                        highlightLine.setAttribute("fill", "none");
//            highlightLine.setAttribute("points", highlightBackgroundLine.getAttribute("points"));
                        highlightLine.setAttribute("d", relationRecord.getPathPointsString());
                        highlightLine.setAttribute("stroke", localRelationDragHandle.getRelationColour());
                        highlightLine.setAttribute("stroke-dasharray", "3");
                        highlightLine.setAttribute("stroke-dashoffset", "0");
                        relationHighlightGroup.appendChild(highlightLine);
                    } catch (OldFormatException exception) {
                        if (!oldFormatWarningShown) {
                            dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Old or erroneous format detected");
                            oldFormatWarningShown = true;
                        }
                    }
                }
                Element symbolNode = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "circle");
                symbolNode.setAttribute("cx", Float.toString(dragNodeX));
                symbolNode.setAttribute("cy", Float.toString(dragNodeY));
                symbolNode.setAttribute("r", "5");
                symbolNode.setAttribute("fill", localRelationDragHandle.getRelationColour());
                symbolNode.setAttribute("stroke", "none");
                relationHighlightGroup.appendChild(symbolNode);
            }
        }
//        graphPanel.lineLookUpTable.addLoops();
//        ArbilComponentBuilder.savePrettyFormatting(graphPanel.doc, new File("/Users/petwit/Documents/SharedInVirtualBox/mpi-co-svn-mpi-nl/LAT/Kinnate/trunk/desktop/src/main/resources/output.svg"));
    }

    protected void addRelationDragHandles(RelationTypeDefinition[] relationTypeDefinitions, Element highlightGroupNode, SVGRect bbox, int paddingDistance) {
        // add the standard relation types
        for (DataTypes.RelationType relationType : new DataTypes.RelationType[]{DataTypes.RelationType.ancestor, DataTypes.RelationType.descendant, DataTypes.RelationType.union, DataTypes.RelationType.sibling}) {
            Element symbolNode = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "circle");
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
            ((EventTarget) symbolNode).addEventListener("mousedown", graphPanel.mouseListenerSvg, false);
            highlightGroupNode.appendChild(symbolNode);
        }
        // add the custom relation types
        float currentCX = bbox.getX() + bbox.getWidth() + paddingDistance;
        float minCY = bbox.getY() - paddingDistance;
        float currentCY = minCY;
        float stepC = 12; //(bbox.getHeight() + paddingDistance) / relationTypeDefinitions.length;
        float maxCY = bbox.getHeight() + paddingDistance;
        for (RelationTypeDefinition typeDefinition : relationTypeDefinitions) {
            for (DataTypes.RelationType relationType : typeDefinition.getRelationType()) {
                // use a constant spacing between the drag handle dots and to start a new column when each line is full
                Element symbolNode = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "circle");
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
                ((EventTarget) symbolNode).addEventListener("mousedown", graphPanel.mouseListenerSvg, false);
                highlightGroupNode.appendChild(symbolNode);
            }
        }
    }

    protected void addGraphicsDragHandles(Element highlightGroupNode, UniqueIdentifier targetIdentifier, SVGRect bbox, int paddingDistance) {
        Element symbolNode = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "circle");
        symbolNode.setAttribute("cx", Float.toString(bbox.getX() + bbox.getWidth() + paddingDistance));
        symbolNode.setAttribute("cy", Float.toString(bbox.getY() + bbox.getHeight() + paddingDistance));
        symbolNode.setAttribute("r", "5");
        symbolNode.setAttribute("target", targetIdentifier.getAttributeIdentifier());
        symbolNode.setAttribute("fill", "blue");
        symbolNode.setAttribute("stroke", "none");
        ((EventTarget) symbolNode).addEventListener("mousedown", graphPanel.mouseListenerSvg, false);
        highlightGroupNode.appendChild(symbolNode);
    }

    protected void updateSvgSelectionHighlights() {
        if (kinTermSavePanel != null) {
            String kinTypeStrings = "";
            for (UniqueIdentifier entityID : graphPanel.selectedGroupId) {
                if (kinTypeStrings.length() != 0) {
                    kinTypeStrings = kinTypeStrings + KinType.separator;
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
                            boolean isLeadSelection = true;
                            for (UniqueIdentifier currentIdentifier : highlightedIdentifiers.toArray(new UniqueIdentifier[]{})) {
                                // remove old highlights but leave existing selections
                                if (!graphPanel.selectedGroupId.contains(currentIdentifier)) {
                                    Element existingHighlight = graphPanel.doc.getElementById("highlight_" + currentIdentifier.getAttributeIdentifier());
                                    if (existingHighlight != null) {
                                        existingHighlight.getParentNode().removeChild(existingHighlight);
                                    }
                                    highlightedIdentifiers.remove(currentIdentifier);
                                }
                            }
                            for (UniqueIdentifier uniqueIdentifier : graphPanel.selectedGroupId.toArray(new UniqueIdentifier[0])) {
                                Element selectedGroup = graphPanel.doc.getElementById(uniqueIdentifier.getAttributeIdentifier());
                                Element existingHighlight = graphPanel.doc.getElementById("highlight_" + uniqueIdentifier.getAttributeIdentifier());
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
                                    SVGRect bbox = ((SVGLocatable) selectedGroup).getBBox();
//                                        System.out.println("bbox X: " + bbox.getX());
//                                        System.out.println("bbox Y: " + bbox.getY());
//                                        System.out.println("bbox W: " + bbox.getWidth());
//                                        System.out.println("bbox H: " + bbox.getHeight());
                                    Element highlightGroupNode = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "g");
                                    ((EventTarget) highlightGroupNode).addEventListener("mousedown", graphPanel.mouseListenerSvg, false);
                                    highlightGroupNode.setAttribute("id", "highlight_" + uniqueIdentifier.getAttributeIdentifier());
                                    Element symbolNode = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "rect");
                                    int paddingDistance = 20;
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
//                                                    Element relationsGroup = graphPanel.doc.getElementById("RelationGroup");
//                                                    for (Node currentRelation = relationsGroup.getFirstChild(); currentRelation != null; currentRelation = currentRelation.getNextSibling()) {
//                                                        Node dataElement = currentRelation.getFirstChild();
//                                                        NamedNodeMap dataAttributes = dataElement.getAttributes();
//                                                        if (dataAttributes.getNamedItemNS(DataStoreSvg.kinDataNameSpace, "lineType").getNodeValue().equals("sanguineLine")) {
//
////
//                                                            if (entityId.equals(dataAttributes.getNamedItemNS(DataStoreSvg.kinDataNameSpace, "ego").getNodeValue()) || entityId.equals(dataAttributes.getNamedItemNS(DataStoreSvg.kinDataNameSpace, "alter").getNodeValue())) {
//                                                                Element polyLineElement = (Element) dataElement.getNextSibling().getFirstChild();
//
//
//                                                                Element useNode = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "use");
//                                                                useNode.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "#" + polyLineElement.getAttribute("id"));
//                                                                useNode.setAttributeNS(null, "stroke", "blue");
//                                                                highlightGroupNode.appendChild(useNode);
//                                                            }
//                                                        }
//                                                    }
//                                                }
                                    // make sure the rect is added before the drag handles, otherwise the rect can block the mouse actions
                                    highlightGroupNode.appendChild(symbolNode);
                                    if (((Element) selectedGroup).getAttributeNS(DataStoreSvg.kinDataNameSpaceLocation, "path").length() > 0) {
                                        addRelationDragHandles(graphPanel.dataStoreSvg.getRelationTypeDefinitions(), highlightGroupNode, bbox, paddingDistance);
                                    } else {
                                        if (uniqueIdentifier.isGraphicsIdentifier()) {
                                            if (!"text".equals(selectedGroup.getLocalName())) {
                                                // add a drag handle for all graphics but not text nodes
                                                addGraphicsDragHandles(highlightGroupNode, uniqueIdentifier, bbox, paddingDistance);
                                            }
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
                            createRelationLineHighlights(graphPanel.doc.getElementById("EntityGroup"));
                        }
                    }
                    // Em:1:FMDH:1:
//                    ArbilComponentBuilder.savePrettyFormatting(graphPanel.doc, new File("/Users/petwit/Documents/SharedInVirtualBox/mpi-co-svn-mpi-nl/LAT/Kinnate/trunk/desktop/src/main/resources/output.svg"));
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

    protected void updateDragRelation(int updateDragNodeXLocal, int updateDragNodeYLocal) {
//        System.out.println("updateDragRelation: " + updateDragNodeXLocal + " : " + updateDragNodeYLocal);
        UpdateManager updateManager = graphPanel.svgCanvas.getUpdateManager();
        synchronized (SvgUpdateHandler.this) {
            updateDragRelationX = updateDragNodeXLocal;
            updateDragRelationY = updateDragNodeYLocal;
            if (!relationThreadRunning) {

                relationThreadRunning = true;
                updateManager.getUpdateRunnableQueue().invokeLater(getRelationRunnable());
            }
        }
    }

    private Runnable getRelationRunnable() {
        return new Runnable() {

            public void run() {
                Element entityGroup = graphPanel.doc.getElementById("EntityGroup");
                int updateDragNodeXLocal = 0;
                int updateDragNodeYLocal = 0;
                while (updateDragNodeXLocal != updateDragRelationX && updateDragNodeYLocal != updateDragRelationY) {
                    synchronized (SvgUpdateHandler.this) {
                        updateDragNodeXLocal = updateDragRelationX;
                        updateDragNodeYLocal = updateDragRelationY;
                    }
                    removeRelationHighLights();
                    removeEntityHighLights();
                    updateDragRelationLines(entityGroup, updateDragNodeXLocal, updateDragNodeYLocal);
                }
                synchronized (SvgUpdateHandler.this) {
                    relationThreadRunning = false;
                }
            }
        };
    }

    protected void startDrag() {
        // dragRemainders is used to store the remainder after snap between drag updates
        // reset all remainders
        float[][] tempRemainders = new float[graphPanel.selectedGroupId.size()][];
        for (int dragCounter = 0; dragCounter < tempRemainders.length; dragCounter++) {
            tempRemainders[dragCounter] = new float[]{0, 0};
        }
        synchronized (SvgUpdateHandler.this) {
            dragRemainders = tempRemainders;
        }
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
//                Element relationOldHighlightGroup = graphPanel.doc.getElementById("RelationHighlightGroup");
//                if (relationOldHighlightGroup != null) {
//                    // remove the relation highlight group because lines will be out of date when the entities are moved
//                    relationOldHighlightGroup.getParentNode().removeChild(relationOldHighlightGroup);
//                }
                Element entityGroup = graphPanel.doc.getElementById("EntityGroup");
                try {
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
                            BugCatcherManager.getBugCatcher().logError(new Exception("graphData or the svg document is null, is this an old file format? try redrawing before draging."));
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
                            for (EntityData selectedEntity : graphPanel.dataStoreSvg.graphData.getDataNodes()) {
                                if (selectedEntity.isVisible
                                        && graphPanel.selectedGroupId.contains(selectedEntity.getUniqueIdentifier())) {
                                    for (EntityData relatedEntity : selectedEntity.getVisiblyRelated()) {
                                        if (relatedEntity.isVisible && !graphPanel.selectedGroupId.contains(relatedEntity.getUniqueIdentifier())) {
                                            allRealtionsSelected = false;
                                            break relationLoop;
                                        }
                                    }
                                }
                            }
                            int dragCounter = 0;
                            for (UniqueIdentifier entityId : graphPanel.selectedGroupId) {
                                // store the remainder after snap for re use on each update
                                synchronized (SvgUpdateHandler.this) {
                                    if (dragRemainders.length > dragCounter) {
                                        dragRemainders[dragCounter] = graphPanel.entitySvg.moveEntity(graphPanel, entityId, updateDragNodeXInner + dragRemainders[dragCounter][0], updateDragNodeYInner + dragRemainders[dragCounter][1], graphPanel.dataStoreSvg.snapToGrid, allRealtionsSelected);
                                    }
                                }
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
                            new RelationSvg(dialogHandler).updateRelationLines(graphPanel, relationRecords, graphPanel.selectedGroupId, hSpacing, vSpacing);
                            createRelationLineHighlights(entityGroup);
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
                } catch (OldFormatException exception) {
                    synchronized (SvgUpdateHandler.this) {
                        threadRunning = false;
                    }
                    if (!oldFormatWarningShown) {
                        dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Old or erroneous format detected");
                        oldFormatWarningShown = true;
                    }
                }
                graphPanel.setRequiresSave();
            }
        };
    }

    private void resizeCanvas(Element svgRoot, Element diagramGroupNode) {
        Rectangle graphSize = graphPanel.dataStoreSvg.graphData.getGraphSize(graphPanel.entitySvg.entityPositions);
        // set the diagram offset so that no element is less than zero
        diagramGroupNode.setAttribute("transform", "translate(" + Integer.toString(-graphSize.x) + ", " + Integer.toString(-graphSize.y) + ")");

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
        svgRoot.setAttribute("width", Integer.toString(graphSize.width - graphSize.x));
        svgRoot.setAttribute("height", Integer.toString(graphSize.height - graphSize.y));

        if (graphPanel.dataStoreSvg.showDiagramBorder) {
            // draw a grey rectangle to show the diagram bounds
            Element pageBorderNode = graphPanel.doc.getElementById("PageBorder");
            if (pageBorderNode == null) {
                pageBorderNode = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "rect");
                pageBorderNode.setAttribute("id", "PageBorder");
                pageBorderNode.setAttribute("x", Float.toString(graphSize.x + 2));
                pageBorderNode.setAttribute("y", Float.toString(graphSize.y + 2));
                pageBorderNode.setAttribute("width", Float.toString(graphSize.width - graphSize.x - 4));
                pageBorderNode.setAttribute("height", Float.toString(graphSize.height - graphSize.y - 4));
                pageBorderNode.setAttribute("fill", "none");
                pageBorderNode.setAttribute("stroke-width", "2");
                pageBorderNode.setAttribute("stroke", "grey");
                diagramGroupNode.appendChild(pageBorderNode);
            } else {
                pageBorderNode.setAttribute("x", Float.toString(graphSize.x + 2));
                pageBorderNode.setAttribute("y", Float.toString(graphSize.y + 2));
                pageBorderNode.setAttribute("width", Float.toString(graphSize.width - graphSize.x - 4));
                pageBorderNode.setAttribute("height", Float.toString(graphSize.height - graphSize.y - 4));
            }
            // end draw a grey rectangle to show the diagram bounds
        } else {
            Element pageBorderNode = graphPanel.doc.getElementById("PageBorder");
            if (pageBorderNode != null) {
                pageBorderNode.getParentNode().removeChild(pageBorderNode);
            }
        }
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

    public void deleteGraphics(UniqueIdentifier uniqueIdentifier) {
        final Element graphicsElement = graphPanel.doc.getElementById(uniqueIdentifier.getAttributeIdentifier());
        final Node parentElement = graphicsElement.getParentNode();
        UpdateManager updateManager = graphPanel.svgCanvas.getUpdateManager();
        if (updateManager != null) {
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {

                public void run() {
                    parentElement.removeChild(graphicsElement);
                    graphPanel.setRequiresSave();
                }
            });
        }
    }

    public void addGraphics(final GraphicsTypes graphicsType, final float xPos, final float yPos) {
        UpdateManager updateManager = graphPanel.svgCanvas.getUpdateManager();
        if (updateManager != null) {
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {

                public void run() {
                    Rectangle graphSize;
//                    if (graphPanel.dataStoreSvg.graphData == null) {
                    // handle case where the diagram has not been drawn yet and the graph data and graph size is not available
//                        graphSize = new Rectangle(0, 0, 0, 0);
//                    } else {
                    graphSize = graphPanel.dataStoreSvg.graphData.getGraphSize(graphPanel.entitySvg.entityPositions);
//                    }
                    Element labelText;
                    switch (graphicsType) {
                        case Circle:
                            labelText = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "circle");
                            labelText.setAttribute("r", "100");
                            labelText.setAttribute("fill", "#ffffff");
                            labelText.setAttribute("stroke", "#000000");
                            labelText.setAttribute("stroke-width", "2");
                            break;
                        case Ellipse:
                            labelText = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "ellipse");
                            labelText.setAttribute("rx", "100");
                            labelText.setAttribute("ry", "100");
                            labelText.setAttribute("fill", "#ffffff");
                            labelText.setAttribute("stroke", "#000000");
                            labelText.setAttribute("stroke-width", "2");
                            break;
                        case Label:
                            labelText = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "text");
                            labelText.setAttribute("fill", "#000000");
                            labelText.setAttribute("stroke-width", "0");
                            labelText.setAttribute("font-size", "28");
                            Text textNode = graphPanel.doc.createTextNode("Label");
                            labelText.appendChild(textNode);
                            break;
                        case Polyline:
                            labelText = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "polyline");
                            break;
                        case Square:
                            labelText = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "rect");
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
//                    String labelIdString = "label" + labelGroup.getChildNodes().getLength();
                    float[] labelPosition = new float[]{graphSize.x + graphSize.width / 2, graphSize.y + graphSize.height / 2};
//                    labelText.setAttribute("x", "0"); // todo: update this to use the mouse click location // xPos
//                    labelText.setAttribute("y", "0"); // yPos
                    labelText.setAttribute("id", labelId.getAttributeIdentifier());
                    labelText.setAttribute("transform", "translate(" + Float.toString(labelPosition[0]) + ", " + Float.toString(labelPosition[1]) + ")");
//
                    // put this into the geometry group or the label group depending on its type so that labels sit above entitis and graphics sit below entities
                    if (graphicsType.equals(GraphicsTypes.Label)) {
                        Element labelGroup = graphPanel.doc.getElementById("LabelsGroup");
                        labelGroup.appendChild(labelText);
                    } else {
                        Element graphicsGroup = graphPanel.doc.getElementById("GraphicsGroup");
                        graphicsGroup.appendChild(labelText);
                    }
                    graphPanel.entitySvg.entityPositions.put(labelId, labelPosition);
//                    graphPanel.doc.getDocumentElement().appendChild(labelText);
                    ((EventTarget) labelText).addEventListener("mousedown", graphPanel.mouseListenerSvg, false);
                    resizeCanvas(graphPanel.doc.getDocumentElement(), graphPanel.doc.getElementById("DiagramGroup"));
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
        relationRecords = new RelationRecordTable();
        int vSpacing = graphPanel.graphPanelSize.getVerticalSpacing(); //dataStoreSvg.graphData.gridHeight);
        int hSpacing = graphPanel.graphPanelSize.getHorizontalSpacing(); //dataStoreSvg.graphData.gridWidth);
//        currentWidth = graphPanelSize.getWidth(dataStoreSvg.graphData.gridWidth, hSpacing);
//        currentHeight = graphPanelSize.getHeight(dataStoreSvg.graphData.gridHeight, vSpacing);
        try {
            removeRelationHighLights();
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
            graphPanel.dataStoreSvg.graphData.placeAllNodes(graphPanel.entitySvg.entityPositions);
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
                    for (EntityRelation graphLinkNode : currentNode.getAllRelations()) {
                        if ((graphPanel.dataStoreSvg.showKinTermLines || graphLinkNode.getRelationType() != DataTypes.RelationType.kinterm)
                                && (graphPanel.dataStoreSvg.showSanguineLines || !DataTypes.isSanguinLine(graphLinkNode.getRelationType()))
                                && (graphLinkNode.getAlterNode() != null && graphLinkNode.getAlterNode().isVisible)) {
                            try {
                                relationRecords.addRecord(graphPanel, currentNode, graphLinkNode, hSpacing, vSpacing, EntitySvg.strokeWidth);
                            } catch (OldFormatException exception) {
                                if (!oldFormatWarningShown) {
                                    dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Old or erroneous format detected");
                                    oldFormatWarningShown = true;
                                }
                            }
                        }
                    }
                }
            }
            new RelationSvg(dialogHandler).createRelationElements(graphPanel, relationRecords, relationGroupNode);
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
            BugCatcherManager.getBugCatcher().logError(exception);
            dialogHandler.addMessageDialogToQueue(exception.getMessage(), "SVG Error");
        } catch (OldFormatException exception) {
            dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Old or erroneous format detected");
        }
        // todo: this repaint might not resolve all cases of redraw issues
        graphPanel.svgCanvas.repaint(); // make sure no remnants are left over after the last redraw
    }
}
