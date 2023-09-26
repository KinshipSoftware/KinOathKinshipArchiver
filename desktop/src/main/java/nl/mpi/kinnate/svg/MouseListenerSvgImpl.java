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

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.dom.KinDocumentImpl;
import nl.mpi.kinnate.dom.KinElementImpl;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.GraphSorter;
import nl.mpi.kinnate.kindata.KinPoint;
import nl.mpi.kinnate.kindata.KinRectangle;
import nl.mpi.kinnate.kindata.RelationTypeDefinition;
import nl.mpi.kinnate.kindocument.RelationLinker;
import nl.mpi.kinnate.ui.KinDiagramPanel;
import nl.mpi.kinnate.ui.SvgElementEditor;
import nl.mpi.kinnate.uniqueidentifiers.IdentifierException;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import org.apache.batik.dom.events.DOMMouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGMatrix;

/**
 * Document : MouseListenerSvgImpl Created on : Mar 9, 2011, 3:21:53 PM 
 * Author   : Peter Withers
 */
public class MouseListenerSvgImpl extends MouseInputAdapter implements EventListener, MouseListenerSvg {

    private final static Logger logger = LoggerFactory.getLogger(GraphPanel.class);
    private Cursor preDragCursor;
    private final KinDiagramPanel kinDiagramPanel;
    private final GraphPanel graphPanel;
    private KinPoint startDragPoint = null;
    private KinPoint startRectangleSelectPoint = null;
    private boolean mouseActionOnNode = false;
    private boolean mouseActionIsPopupTrigger = false;
    private boolean mouseActionIsDrag = false;
    private UniqueIdentifier entityToToggle = null;
    private final HashMap<UniqueIdentifier, SvgElementEditor> shownGraphicsEditors;
    private final MessageDialogHandler dialogHandler;
    final private SessionStorage sessionStorage;
    private EntityCollection entityCollection;

    public MouseListenerSvgImpl(KinDiagramPanel kinDiagramPanel, GraphPanel graphPanel, SessionStorage sessionStorage, MessageDialogHandler dialogHandler) {
        this.kinDiagramPanel = kinDiagramPanel;
        this.graphPanel = graphPanel;
        this.dialogHandler = dialogHandler;
        this.sessionStorage = sessionStorage;
        shownGraphicsEditors = new HashMap<UniqueIdentifier, SvgElementEditor>();
    }

    public void setEntityCollection(EntityCollection entityCollection) {
        this.entityCollection = entityCollection;
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        mouseDragged(new KinPoint(me.getPoint().x, me.getPoint().y), SwingUtilities.isMiddleMouseButton(me), SwingUtilities.isLeftMouseButton(me), me.isShiftDown());
    }

    @Override
    public void mouseDragged(final KinPoint kinPoint, final Boolean isMiddleMouseButton, final Boolean isLeftMouseButton, final Boolean shiftDown) {
        // todo: this shold probably be put into the svg canvas thread
        if (graphPanel.svgUpdateHandler.dragHandlesShowing()) {
            graphPanel.updateDragRelation(kinPoint.x, kinPoint.y);
        } else {
            try {
                if (startDragPoint != null) {
//            System.out.println("mouseDragged: " + me.toString());
                    if (isMiddleMouseButton) {
                        graphPanel.dragCanvas(kinPoint.x - startDragPoint.x, kinPoint.y - startDragPoint.y);
                    } else if (isLeftMouseButton) {
                        // we check and clear the selection here on the drag because on mouse down it is not known if an svg element was the target of the click
                        checkSelectionClearRequired(isLeftMouseButton, shiftDown);
                        if (!mouseActionOnNode) {
                            // draw selection rectangle                        
                            if (startRectangleSelectPoint == null) {
                                startRectangleSelectPoint = new KinPoint(kinPoint.x, kinPoint.y);
                            }
                            graphPanel.drawSelectionRect(new KinPoint(startRectangleSelectPoint.x, startRectangleSelectPoint.y), kinPoint);
                        } else if (graphPanel.selectedGroupId.size() > 0) {
                            graphPanel.svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                            // limit the drag to the distance draged not the location
                            graphPanel.updateDragNode(kinPoint.x - startDragPoint.x, kinPoint.y - startDragPoint.y);
                        }
                    }
                    mouseActionIsDrag = true;
                } else {
                    graphPanel.svgUpdateHandler.startDrag(graphPanel.selectedGroupId);
                }
                startDragPoint = new KinPoint(kinPoint.x, kinPoint.y);
            } catch (KinElementException exception) {
                logger.warn("Error, modifying the SVG.", exception);
            }
        }
    }

    private void checkSelectionClearRequired(final boolean isLeftMouseButton, final boolean shiftDown) throws KinElementException {
        if (!shiftDown && /* !mouseActionIsDrag && */ !mouseActionIsPopupTrigger && !mouseActionOnNode
                && isLeftMouseButton) { // todo: button1 could cause issues for left handed people with swapped mouse buttons
            System.out.println("Clear selection");
            graphPanel.selectedGroupId.clear();
            updateSelectionDisplay();
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        mouseReleased(new KinPoint(me.getPoint().x, me.getPoint().y), SwingUtilities.isLeftMouseButton(me), me.isShiftDown());
    }

    @Override
    public void mouseReleased(final KinPoint kinPoint, Boolean isLeftMouseButton, Boolean shiftDown) {
        if (graphPanel.getSVGDocument().graphData == null) {
//        if (!kinDiagramPanel.verifyDiagramDataLoaded()) {
            return;
        }
//        System.out.println("mouseReleased: " + me.toString());
        graphPanel.svgCanvas.setCursor(preDragCursor);
        if (mouseActionIsDrag) {
            graphPanel.updateCanvasSize(false);
        }
        startDragPoint = null;
        try {
            if (!mouseActionIsDrag && entityToToggle != null && !graphPanel.svgUpdateHandler.dragHandlesShowing()) {
                // toggle the highlight
                graphPanel.selectedGroupId.remove(entityToToggle);
                entityToToggle = null;
                updateSelectionDisplay();
            }
            checkSelectionClearRequired(isLeftMouseButton, shiftDown);
            if (startRectangleSelectPoint != null) {
                startRectangleSelectPoint = null;
                graphPanel.removeSelectionRect();
                KinRectangle dragSelectionRectOnDocument = graphPanel.svgUpdateHandler.getSelectionRect();
                // update the entity selection based on the drag selection rectangle
                graphPanel.selectedGroupId.addAll(graphPanel.getSVGDocument().entitySvg.getEntitiesWithinRect(dragSelectionRectOnDocument));
                updateSelectionDisplay();
            }
            mouseActionOnNode = false;
            if (graphPanel.svgUpdateHandler.dragHandlesShowing()) {
                // act on the realation drag
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            if (graphPanel.svgUpdateHandler.dropTargetDefined()) {
                                kinDiagramPanel.showProgressBar();
                                try {
                                    // if a relation has been set by this drag action then it is created here.
                                    final RelationType relationType = DataTypes.getOpposingRelationType(graphPanel.svgUpdateHandler.getRelationDragHandle().getRelationType());
                                    UniqueIdentifier[] changedIdentifiers = new RelationLinker(sessionStorage, dialogHandler, entityCollection).linkEntities(graphPanel.svgUpdateHandler.getRelationDragHandle().targetIdentifier, graphPanel.getSelectedIds(), relationType, graphPanel.svgUpdateHandler.getRelationDragHandle().getDataCategory(), graphPanel.svgUpdateHandler.getRelationDragHandle().getDisplayName());
                                    kinDiagramPanel.entityRelationsChanged(changedIdentifiers);
                                } catch (ImportException exception) {
                                    dialogHandler.addMessageDialogToQueue("Failed to create relation: " + exception.getMessage(), "Drag Relation");
                                }
                                kinDiagramPanel.clearProgressBar();
                            } else if (graphPanel.svgUpdateHandler.dragHandlesShowing() && !graphPanel.svgUpdateHandler.dropTargetDefined()) {
                                // show add entity
                                graphPanel.svgUpdateHandler.showAddEntityBox(kinPoint.x, kinPoint.y);
                            } else {
                                graphPanel.svgUpdateHandler.setRelationDragHandle(null);
                                updateSelectionDisplay();
                            }
                        } catch (KinElementException exception) {
                            logger.warn("Error, modifying the SVG.", exception);
                        }
                    }
                }).start();
            }
        } catch (KinElementException exception) {
            logger.warn("Error, modifying the SVG.", exception);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mousePressed(e.isPopupTrigger());
    }

    @Override
    public void mousePressed(final Boolean isPopupTrigger) {
        if (!kinDiagramPanel.verifyDiagramDataLoaded()) {
            return;
        }
        mouseActionIsDrag = false;
        mouseActionIsPopupTrigger = isPopupTrigger;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
//      This is for testing the screen to document transform
//        graphPanel.svgUpdateHandler.updateMouseDot(e.getPoint());
//        final KinPoint entityPointOnDocument = graphPanel.svgUpdateHandler.getEntityPointOnDocument(new KinPoint(e.getX(), e.getY()));
//        kinDiagramPanel.setStatusBarText("mouseMoved:" + entityPointOnDocument.getX() + ":" + entityPointOnDocument.getY());
//        System.out.println("mouseMoved:" + e.getX() + ":" + e.getY());
    }

    @Override
    public void handleEvent(Event evt) {
        if (!kinDiagramPanel.verifyDiagramDataLoaded()) {
            return;
        }
        mouseActionOnNode = true;
        boolean shiftDown = false;
//        boolean mouseDownButton1 = false;
        if (evt instanceof DOMMouseEvent) {
            shiftDown = ((DOMMouseEvent) evt).getShiftKey();
            // todo: it seems that DOMMouseEvent does not map to MouseEvent.BUTTON1, so it is unknown how this will behave on various platforms
//            mouseDownButton1 = ((DOMMouseEvent) evt).getButton() == MouseEvent.BUTTON1; 
//            System.out.println("button: "+((DOMMouseEvent) evt).getButton());
        }
        System.out.println("dom mouse event: " + evt.getCurrentTarget());
        Element currentDraggedElement = ((Element) evt.getCurrentTarget());
        preDragCursor = graphPanel.svgCanvas.getCursor();
        try {
            final String handleTypeString = currentDraggedElement.getAttribute("handletype");
            final String targetIdString = currentDraggedElement.getAttribute("target");
            if ((targetIdString != null && targetIdString.length() > 0) || (handleTypeString != null && handleTypeString.length() > 0)) {
                if (evt instanceof DOMMouseEvent) {
                    // the entity group is no longer offset so we no longer need to subtract the entity group position here
                    SVGMatrix draggedElementScreenMatrix = ((SVGLocatable) currentDraggedElement).getScreenCTM();
                    SVGMatrix draggedElementMatrix = ((KinDocumentImpl) graphPanel.getSVGDocument().doc).getDoc().getRootElement().getTransformToElement((SVGElement) currentDraggedElement);
                    float scaleFactor = draggedElementScreenMatrix.inverse().getA(); // the drawing is proportional so only using X is adequate here
                    float xTranslate = draggedElementMatrix.getE();
                    float yTranslate = draggedElementMatrix.getF();
//                AffineTransform affineTransform = graphPanel.svgCanvas.getRenderingTransform();
                    if (targetIdString != null && targetIdString.length() > 0) {
                        graphPanel.svgUpdateHandler.setRelationDragHandle(
                                new GraphicsDragHandle(
                                        graphPanel.getSVGDocument().doc.getElementById(targetIdString),
                                        new KinElementImpl(currentDraggedElement),
                                        new KinElementImpl(currentDraggedElement.getParentNode().getFirstChild()), // this assumes that the rect is the first element in the highlight
                                        Float.valueOf(currentDraggedElement.getAttribute("cx")),
                                        Float.valueOf(currentDraggedElement.getAttribute("cy")),
                                        ((DOMMouseEvent) evt).getClientX(),
                                        ((DOMMouseEvent) evt).getClientY(),
                                        scaleFactor));
                    } else {
                        RelationTypeDefinition customTypeDefinition = null;
                        DataTypes.RelationType relationType = null;
                        if (handleTypeString.startsWith("custom:")) {
                            String[] handleParts = handleTypeString.split(":");
                            relationType = DataTypes.RelationType.valueOf(handleParts[1]);
                            int typeHashCode = Integer.parseInt(handleParts[2]);
                            for (RelationTypeDefinition currentDefinition : graphPanel.dataStoreSvg.getRelationTypeDefinitions()) {
                                if (currentDefinition.hashCode() == typeHashCode) {
                                    customTypeDefinition = currentDefinition;
                                    break;
                                }
                            }
                        } else {
                            relationType = DataTypes.RelationType.valueOf(handleTypeString);
                        }
                        graphPanel.svgUpdateHandler.setRelationDragHandle(
                                new RelationDragHandle(
                                        customTypeDefinition,
                                        relationType,
                                        Float.valueOf(currentDraggedElement.getAttribute("cx")) - xTranslate,
                                        Float.valueOf(currentDraggedElement.getAttribute("cy")) - yTranslate,
                                        ((DOMMouseEvent) evt).getClientX(),
                                        ((DOMMouseEvent) evt).getClientY(),
                                        scaleFactor));
                    }
                }
            } else /* if (mouseDownButton1) */ {
                final String attributeString = currentDraggedElement.getAttribute("id");
                try {
                    UniqueIdentifier entityIdentifier = new UniqueIdentifier(attributeString);
                    System.out.println("entityPath: " + entityIdentifier.getAttributeIdentifier());
                    boolean nodeAlreadySelected = graphPanel.selectedGroupId.contains(entityIdentifier);
                    if (!shiftDown && !nodeAlreadySelected) {
                        System.out.println("Clear selection");
                        graphPanel.selectedGroupId.clear();
                        graphPanel.selectedGroupId.add(entityIdentifier);
                    } else // toggle the highlight
                     if (shiftDown && nodeAlreadySelected) {
                            // postpone until after a drag action can be tested for and only deselect if not draged
                            entityToToggle = entityIdentifier;
                            // graphPanel.selectedGroupId.remove(entityIdentifier);
                        } else if (!nodeAlreadySelected) {
                            graphPanel.selectedGroupId.add(entityIdentifier);
                        }
                    updateSelectionDisplay();
                } catch (IdentifierException exception) {
                    BugCatcherManager.getBugCatcher().logError(exception);
                    dialogHandler.addMessageDialogToQueue("Failed to read selection identifier, selection might not be correct", "Selection Highlight");
                }
            }
        } catch (KinElementException exception) {
            logger.warn("Error, modifying the SVG.", exception);
        }
    }

    protected void updateSelectionDisplay() throws KinElementException {
        graphPanel.updateSvgSelectionHighlights();
        // update the table selection
        // todo: #1099	Labels should show the blue highlight
        if (graphPanel.metadataPanel != null) {
            graphPanel.metadataPanel.removeAllArbilDataNodeRows();
            ArrayList<UniqueIdentifier> remainingEditors = new ArrayList<UniqueIdentifier>(shownGraphicsEditors.keySet());
            ArrayList<EntityData> selectedEntities = new ArrayList<EntityData>();
            for (UniqueIdentifier currentSelectedId : graphPanel.selectedGroupId) {
                remainingEditors.remove(currentSelectedId);
                if (currentSelectedId.isGraphicsIdentifier()) {
                    if (!shownGraphicsEditors.containsKey(currentSelectedId)) {
                        KinElement graphicsElement = graphPanel.getSVGDocument().doc.getElementById(currentSelectedId.getAttributeIdentifier());
                        SvgElementEditor elementEditor = new SvgElementEditor(graphPanel.svgCanvas.getUpdateManager(), graphicsElement);
                        graphPanel.metadataPanel.addTab("Graphics Editor", elementEditor);
//                            graphPanel.editorHidePane.setSelectedComponent(elementEditor);
                        shownGraphicsEditors.put(currentSelectedId, elementEditor);
                    }
                } else if (!currentSelectedId.isTransientIdentifier()) {
                    EntityData currentSelectedEntity = graphPanel.getEntityForElementId(currentSelectedId);
                    if (currentSelectedEntity != null) {
                        selectedEntities.add(currentSelectedEntity);
                        graphPanel.metadataPanel.addEntityDataNode(currentSelectedEntity);
                    }
                }
            }
            for (UniqueIdentifier remainingIdentifier : remainingEditors) {
                // remove the unused editors
                graphPanel.metadataPanel.removeTab(shownGraphicsEditors.get(remainingIdentifier));
                shownGraphicsEditors.remove(remainingIdentifier);
            }
            graphPanel.metadataPanel.updateEditorPane();
//            graphPanel.metadataPanel.setDateEditorEntities(selectedEntities);
        }
    }

    private void expandSelectionByRelations(int maxCount, EntityData currentEntity, HashSet<UniqueIdentifier> selectedIds, boolean addRecursively) {
        if (maxCount <= selectedIds.size()) {
            return;
        }
        for (EntityData alterNode : currentEntity.getVisiblyRelated()) {
            if (alterNode.isVisible && !selectedIds.contains(alterNode.getUniqueIdentifier())) {
                selectedIds.add(alterNode.getUniqueIdentifier());
                if (addRecursively) {
                    expandSelectionByRelations(maxCount, alterNode, selectedIds, addRecursively);
                }
            }
        }
    }

    public void performMenuAction(ActionCode commandCode, GraphSorter graphData) throws KinElementException {
        System.out.println("commandCode: " + commandCode.name());
        boolean addRecursively = true;
        switch (commandCode) {
            case selectAll:
                graphPanel.selectedGroupId.clear();
                for (EntityData currentEntity : graphData.getDataNodes()) {
                    if (currentEntity.isVisible) {
                        if (!graphPanel.selectedGroupId.contains(currentEntity.getUniqueIdentifier())) {
                            graphPanel.selectedGroupId.add(currentEntity.getUniqueIdentifier());
                        }
                    }
                }
                break;
            case expandSelection:
                addRecursively = false;
            case selectRelated:
                HashSet<UniqueIdentifier> selectedIds = new HashSet<UniqueIdentifier>(graphPanel.selectedGroupId);
                for (EntityData currentEntity : graphData.getDataNodes()) {
                    if (currentEntity.isVisible) {
                        // todo: continue here
                        if (graphPanel.selectedGroupId.contains(currentEntity.getUniqueIdentifier())) {
                            expandSelectionByRelations(graphData.getDataNodes().length, currentEntity, selectedIds, addRecursively);
                        }
                    }
                }
                graphPanel.selectedGroupId.clear();
                graphPanel.selectedGroupId.addAll(selectedIds);
                break;
            case deselectAll:
                graphPanel.selectedGroupId.clear();
                break;
        }
        updateSelectionDisplay();
    }
}