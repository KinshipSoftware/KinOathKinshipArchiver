package nl.mpi.kinnate.svg;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.apache.batik.dom.events.DOMMouseEvent;
import javax.swing.event.MouseInputAdapter;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityRelation;
import org.w3c.dom.Element;

/**
 *  Document   : MouseListenerSvg
 *  Created on : Mar 9, 2011, 3:21:53 PM
 *  Author     : Peter Withers
 */
public class MouseListenerSvg extends MouseInputAdapter implements EventListener {

    private Cursor preDragCursor;
    private GraphPanel graphPanel;
    private Point startDragPoint = null;
    static private boolean mouseActionOnNode = false;
    static private boolean mouseActionIsPopupTrigger = false;
    static private boolean mouseActionIsDrag = false;
    static private String entityToToggle = null;

    public enum ActionCode {

        selectAll, selectRelated, expandSelection, deselectAll
    }

    public MouseListenerSvg(GraphPanel graphPanelLocal) {
        graphPanel = graphPanelLocal;
    }

    @Override
    public void mouseDragged(MouseEvent me) {
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

    private void checkSelectionClearRequired(MouseEvent me) {
        boolean shiftDown = me.isShiftDown();
        if (!shiftDown && /* !mouseActionIsDrag &&  */ !mouseActionIsPopupTrigger && !mouseActionOnNode && me.getButton() == MouseEvent.BUTTON1) { // todo: button1 could cause issues for left handed people with swapped mouse buttons
            System.out.println("Clear selection");
            graphPanel.selectedGroupId.clear();
            updateSelectionDisplay();
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
//        System.out.println("mouseReleased: " + me.toString());
        graphPanel.svgCanvas.setCursor(preDragCursor);
        if (mouseActionIsDrag) {
            graphPanel.svgUpdateHandler.updateCanvasSize();
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
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseActionIsDrag = false;
        mouseActionIsPopupTrigger = e.isPopupTrigger();
    }

    @Override
    public void handleEvent(Event evt) {
        mouseActionOnNode = true;
        boolean shiftDown = false;
        if (evt instanceof DOMMouseEvent) {
            shiftDown = ((DOMMouseEvent) evt).getShiftKey();
        }
        System.out.println("dom mouse event: " + evt.getCurrentTarget());
        Element currentDraggedElement = ((Element) evt.getCurrentTarget());
        preDragCursor = graphPanel.svgCanvas.getCursor();
        // get the entityPath
        // todo: change selected elements to use the ID not the path, but this change will affect th way the imdi path is obtained to show the table
        String entityIdentifier = currentDraggedElement.getAttribute("id");
        System.out.println("entityPath: " + entityIdentifier);
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
    }

    private void updateSelectionDisplay() {
        graphPanel.svgUpdateHandler.updateSvgSelectionHighlights();
        // update the table selection
        if (graphPanel.arbilTableModel != null) {
            graphPanel.arbilTableModel.removeAllArbilDataNodeRows();
            try {
                for (String currentSelectedId : graphPanel.selectedGroupId) {
                    String currentSelectedPath = graphPanel.getPathForElementId(currentSelectedId);
                    if (currentSelectedPath != null) {
                        graphPanel.arbilTableModel.addArbilDataNode(new URI(currentSelectedPath));
                    }
                }
            } catch (URISyntaxException urise) {
                GuiHelper.linorgBugCatcher.logError(urise);
            }
        }
    }

    private void addRelations(int maxCount, EntityData currentEntity, HashSet<String> selectedIds) {
        if (maxCount <= selectedIds.size()) {
            return;
        }
        for (EntityRelation entityRelation : currentEntity.getVisiblyRelateNodes()) {
            EntityData alterNode = entityRelation.getAlterNode();
            if (alterNode.isVisible && !selectedIds.contains(alterNode.getUniqueIdentifier())) {
                selectedIds.add(alterNode.getUniqueIdentifier());
                addRelations(maxCount, alterNode, selectedIds);
            }
        }
    }

    public void performMenuAction(ActionCode commandCode) {
        System.out.println("commandCode: " + commandCode.name());
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
            case selectRelated:
                HashSet<String> selectedIds = new HashSet<String>(graphPanel.selectedGroupId);
                for (EntityData currentEntity : graphPanel.dataStoreSvg.graphData.getDataNodes()) {
                    if (currentEntity.isVisible) {
                        // todo: continue here
                        if (graphPanel.selectedGroupId.contains(currentEntity.getUniqueIdentifier())) {
                            addRelations(graphPanel.dataStoreSvg.graphData.getDataNodes().length, currentEntity, selectedIds);
                        }
                    }
                }
                graphPanel.selectedGroupId.clear();
                graphPanel.selectedGroupId.addAll(selectedIds);
                break;
            case expandSelection:
                // todo: continue here...
                break;
            case deselectAll:
                graphPanel.selectedGroupId.clear();
                break;
        }
        updateSelectionDisplay();
    }
}
