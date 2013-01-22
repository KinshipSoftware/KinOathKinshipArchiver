/**
 * Copyright (C) 2012 The Language Archive
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
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.event.MouseInputAdapter;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.RelationTypeDefinition;
import nl.mpi.kinnate.kindocument.RelationLinker;
import nl.mpi.kinnate.ui.KinDiagramPanel;
import nl.mpi.kinnate.ui.SvgElementEditor;
import nl.mpi.kinnate.uniqueidentifiers.IdentifierException;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import org.apache.batik.dom.events.DOMMouseEvent;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGMatrix;

/**
 * Document : MouseListenerSvg Created on : Mar 9, 2011, 3:21:53 PM Author :
 * Peter Withers
 */
public class MouseListenerSvg extends MouseInputAdapter implements EventListener {

    private Cursor preDragCursor;
    private KinDiagramPanel kinDiagramPanel;
    private GraphPanel graphPanel;
    private Point startDragPoint = null;
    private boolean mouseActionOnNode = false;
    private boolean mouseActionIsPopupTrigger = false;
    private boolean mouseActionIsDrag = false;
    private UniqueIdentifier entityToToggle = null;
    private HashMap<UniqueIdentifier, SvgElementEditor> shownGraphicsEditors;
    private MessageDialogHandler dialogHandler;
    final private SessionStorage sessionStorage;
    private EntityCollection entityCollection;

    public enum ActionCode {

        selectAll, selectRelated, expandSelection, deselectAll
    }

    public MouseListenerSvg(KinDiagramPanel kinDiagramPanel, GraphPanel graphPanel, SessionStorage sessionStorage, MessageDialogHandler dialogHandler, EntityCollection entityCollection) {
        this.kinDiagramPanel = kinDiagramPanel;
        this.graphPanel = graphPanel;
        this.dialogHandler = dialogHandler;
        this.sessionStorage = sessionStorage;
        this.entityCollection = entityCollection;
        shownGraphicsEditors = new HashMap<UniqueIdentifier, SvgElementEditor>();
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        // todo: this shold probably be put into the svg canvas thread
        if (graphPanel.svgUpdateHandler.relationDragHandle != null) {
            graphPanel.svgUpdateHandler.updateDragRelation(me.getPoint().x, me.getPoint().y);
        } else {
            if (startDragPoint != null) {
//            System.out.println("mouseDragged: " + me.toString());
                if (graphPanel.selectedGroupId.size() > 0) {
                    checkSelectionClearRequired(me);
                }
                if (graphPanel.selectedGroupId.size() > 0) {
                    graphPanel.svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    // limit the drag to the distance draged not the location
                    graphPanel.svgUpdateHandler.updateDragNode(me.getPoint().x - startDragPoint.x, me.getPoint().y - startDragPoint.y);
                } else {
                    graphPanel.svgUpdateHandler.dragCanvas(me.getPoint().x - startDragPoint.x, me.getPoint().y - startDragPoint.y);
                }
                mouseActionIsDrag = true;
            } else {
                graphPanel.svgUpdateHandler.startDrag();
            }
            startDragPoint = me.getPoint();
        }
    }

    private void checkSelectionClearRequired(MouseEvent me) {
        boolean shiftDown = me.isShiftDown();
        if (!shiftDown && /* !mouseActionIsDrag && */ !mouseActionIsPopupTrigger && !mouseActionOnNode && me.getButton() == MouseEvent.BUTTON1) { // todo: button1 could cause issues for left handed people with swapped mouse buttons
            System.out.println("Clear selection");
            graphPanel.selectedGroupId.clear();
            updateSelectionDisplay();
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        if (graphPanel.dataStoreSvg.graphData == null) {
//        if (!kinDiagramPanel.verifyDiagramDataLoaded()) {
            return;
        }
//        System.out.println("mouseReleased: " + me.toString());
        graphPanel.svgCanvas.setCursor(preDragCursor);
        if (mouseActionIsDrag) {
            graphPanel.svgUpdateHandler.updateCanvasSize(false);
        }
        startDragPoint = null;
        if (!mouseActionIsDrag && entityToToggle != null) {
            // toggle the highlight
            graphPanel.selectedGroupId.remove(entityToToggle);
            entityToToggle = null;
            updateSelectionDisplay();
        }
        checkSelectionClearRequired(me);
        mouseActionOnNode = false;
        if (graphPanel.svgUpdateHandler.relationDragHandle != null) {
            new Thread(new Runnable() {
                public void run() {

                    System.out.println("Showing progrtess bar");
                    if (graphPanel.svgUpdateHandler.relationDragHandle.targetIdentifier != null) {
                        kinDiagramPanel.showProgressBar();
                        try {
                            // if a relation has been set by this drag action then it is created here.
                            final RelationType relationType = DataTypes.getOpposingRelationType(graphPanel.svgUpdateHandler.relationDragHandle.getRelationType());
                            UniqueIdentifier[] changedIdentifiers = new RelationLinker(sessionStorage, dialogHandler, entityCollection).linkEntities(graphPanel.svgUpdateHandler.relationDragHandle.targetIdentifier, graphPanel.getSelectedIds(), relationType, graphPanel.svgUpdateHandler.relationDragHandle.getDataCategory(), graphPanel.svgUpdateHandler.relationDragHandle.getDisplayName());
                            kinDiagramPanel.entityRelationsChanged(changedIdentifiers);
                        } catch (ImportException exception) {
                            dialogHandler.addMessageDialogToQueue("Failed to create relation: " + exception.getMessage(), "Drag Relation");
                        }
                        kinDiagramPanel.clearProgressBar();
                    }
                    graphPanel.svgUpdateHandler.relationDragHandle = null;
                    updateSelectionDisplay();
                }
            }).start();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseActionIsDrag = false;
        mouseActionIsPopupTrigger = e.isPopupTrigger();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
//      This is for testing the screen to document transform
//        graphPanel.svgUpdateHandler.updateMouseDot(e.getPoint());
//        final Point entityPointOnDocument = graphPanel.svgUpdateHandler.getEntityPointOnDocument(new Point(e.getX(), e.getY()));
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
        if (evt instanceof DOMMouseEvent) {
            shiftDown = ((DOMMouseEvent) evt).getShiftKey();
        }
        System.out.println("dom mouse event: " + evt.getCurrentTarget());
        Element currentDraggedElement = ((Element) evt.getCurrentTarget());
        preDragCursor = graphPanel.svgCanvas.getCursor();
        final String handleTypeString = currentDraggedElement.getAttribute("handletype");
        final String targetIdString = currentDraggedElement.getAttribute("target");
        if ((targetIdString != null && targetIdString.length() > 0) || (handleTypeString != null && handleTypeString.length() > 0)) {
            if (evt instanceof DOMMouseEvent) {
                // the entity group is no longer offset so we no longer need to subtract the entity group position here
                SVGMatrix draggedElementScreenMatrix = ((SVGLocatable) currentDraggedElement).getScreenCTM();
                SVGMatrix draggedElementMatrix = graphPanel.doc.getRootElement().getTransformToElement((SVGElement) currentDraggedElement);
                float scaleFactor = draggedElementScreenMatrix.inverse().getA(); // the drawing is proportional so only using X is adequate here
                float xTranslate = draggedElementMatrix.getE();
                float yTranslate = draggedElementMatrix.getF();
//                AffineTransform affineTransform = graphPanel.svgCanvas.getRenderingTransform();
                if (targetIdString != null && targetIdString.length() > 0) {
                    graphPanel.svgUpdateHandler.relationDragHandle =
                            new GraphicsDragHandle(
                            graphPanel.doc.getElementById(targetIdString),
                            currentDraggedElement,
                            (Element) currentDraggedElement.getParentNode().getFirstChild(), // this assumes that the rect is the first element in the highlight
                            Float.valueOf(currentDraggedElement.getAttribute("cx")),
                            Float.valueOf(currentDraggedElement.getAttribute("cy")),
                            ((DOMMouseEvent) evt).getClientX(),
                            ((DOMMouseEvent) evt).getClientY(),
                            scaleFactor);
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
                    graphPanel.svgUpdateHandler.relationDragHandle =
                            new RelationDragHandle(
                            customTypeDefinition,
                            relationType,
                            Float.valueOf(currentDraggedElement.getAttribute("cx")) - xTranslate,
                            Float.valueOf(currentDraggedElement.getAttribute("cy")) - yTranslate,
                            ((DOMMouseEvent) evt).getClientX(),
                            ((DOMMouseEvent) evt).getClientY(),
                            scaleFactor);
                }
            }
        } else {
            final String attributeString = currentDraggedElement.getAttribute("id");
            try {
                UniqueIdentifier entityIdentifier = new UniqueIdentifier(attributeString);
                System.out.println("entityPath: " + entityIdentifier.getAttributeIdentifier());
                boolean nodeAlreadySelected = graphPanel.selectedGroupId.contains(entityIdentifier);
                if (!shiftDown && !nodeAlreadySelected) {
                    System.out.println("Clear selection");
                    graphPanel.selectedGroupId.clear();
                    graphPanel.selectedGroupId.add(entityIdentifier);
                } else {
                    // toggle the highlight
                    if (shiftDown && nodeAlreadySelected) {
                        // postpone until after a drag action can be tested for and only deselect if not draged
                        entityToToggle = entityIdentifier;
                        // graphPanel.selectedGroupId.remove(entityIdentifier);
                    } else if (!nodeAlreadySelected) {
                        graphPanel.selectedGroupId.add(entityIdentifier);
                    }
                }
                updateSelectionDisplay();
            } catch (IdentifierException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                dialogHandler.addMessageDialogToQueue("Failed to read selection identifier, selection might not be correct", "Selection Highlight");
            }
        }
    }

    protected void updateSelectionDisplay() {
        graphPanel.svgUpdateHandler.updateSvgSelectionHighlights();
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
                        Element graphicsElement = graphPanel.doc.getElementById(currentSelectedId.getAttributeIdentifier());
                        SvgElementEditor elementEditor = new SvgElementEditor(graphPanel.svgCanvas.getUpdateManager(), graphicsElement);
                        graphPanel.metadataPanel.addTab("Graphics Editor", elementEditor);
//                            graphPanel.editorHidePane.setSelectedComponent(elementEditor);
                        shownGraphicsEditors.put(currentSelectedId, elementEditor);
                    }
                } else if (!currentSelectedId.isTransientIdentifier()) {
                    EntityData currentSelectedEntity = graphPanel.getEntityForElementId(currentSelectedId);
                    if (currentSelectedEntity != null) {
                        selectedEntities.add(currentSelectedEntity);
                        graphPanel.metadataPanel.addEntityDataNode(kinDiagramPanel, currentSelectedEntity);
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

    public void performMenuAction(ActionCode commandCode) {
        System.out.println("commandCode: " + commandCode.name());
        boolean addRecursively = true;
        switch (commandCode) {
            case selectAll:
                graphPanel.selectedGroupId.clear();
                for (EntityData currentEntity : graphPanel.dataStoreSvg.graphData.getDataNodes()) {
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
                for (EntityData currentEntity : graphPanel.dataStoreSvg.graphData.getDataNodes()) {
                    if (currentEntity.isVisible) {
                        // todo: continue here
                        if (graphPanel.selectedGroupId.contains(currentEntity.getUniqueIdentifier())) {
                            expandSelectionByRelations(graphPanel.dataStoreSvg.graphData.getDataNodes().length, currentEntity, selectedIds, addRecursively);
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
