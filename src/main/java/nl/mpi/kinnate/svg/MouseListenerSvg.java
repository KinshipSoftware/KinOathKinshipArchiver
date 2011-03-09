package nl.mpi.kinnate.svg;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URISyntaxException;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.apache.batik.dom.events.DOMMouseEvent;
import javax.swing.event.MouseInputAdapter;
import nl.mpi.arbil.GuiHelper;
import org.w3c.dom.Element;
import nl.mpi.arbil.data.ImdiLoader;

/**
 *  Document   : MouseListenerSvg
 *  Created on : Mar 9, 2011, 3:21:53 PM
 *  Author     : Peter Withers
 */
public class MouseListenerSvg extends MouseInputAdapter implements EventListener {

    private Element currentDraggedElement;
    private Cursor preDragCursor;
    private GraphPanel graphPanel;

    public MouseListenerSvg(GraphPanel graphPanelLocal) {
        graphPanel = graphPanelLocal;
    }

    @Override
    public void mouseDragged(MouseEvent me) {
//                System.out.println("mouseDragged: " + me.toString());
        if (currentDraggedElement != null) {
            graphPanel.svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            graphPanel.updateDragNode(currentDraggedElement, me.getX(), me.getY());
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
//                System.out.println("mouseReleased: " + me.toString());
        if (currentDraggedElement != null) {
            graphPanel.svgCanvas.setCursor(preDragCursor);
            graphPanel.updateDragNode(currentDraggedElement, me.getX(), me.getY());
            currentDraggedElement = null;
        }
    }

    @Override
    public void handleEvent(Event evt) {
        boolean shiftDown = false;
        if (evt instanceof DOMMouseEvent) {
            shiftDown = ((DOMMouseEvent) evt).getShiftKey();
        }
        System.out.println("mousedown: " + evt.getCurrentTarget());
        currentDraggedElement = ((Element) evt.getCurrentTarget());
        preDragCursor = graphPanel.svgCanvas.getCursor();
        // get the entityPath
        String entityPath = currentDraggedElement.getAttribute("id");
        System.out.println("entityPath: " + entityPath);
        boolean nodeAlreadySelected = graphPanel.selectedGroupElement.contains(entityPath);
        if (!shiftDown) {
            System.out.println("Clear selection");
            graphPanel.selectedGroupElement.clear();
        }
        // toggle the highlight
        if (nodeAlreadySelected) {
            graphPanel.selectedGroupElement.remove(entityPath);
        } else {
            graphPanel.selectedGroupElement.add(entityPath);
        }
        graphPanel.addHighlightToGroup();
//                if (existingHighlight == null) {
//                                        svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
//                }
        // update the table selection
        if (graphPanel.imdiTableModel != null) {
            graphPanel.imdiTableModel.removeAllImdiRows();
            try {
                for (String currentSelectedPath : graphPanel.selectedGroupElement) {
                    graphPanel.imdiTableModel.addSingleImdiObject(ImdiLoader.getSingleInstance().getImdiObject(null, new URI(currentSelectedPath)));
                }
            } catch (URISyntaxException urise) {
                GuiHelper.linorgBugCatcher.logError(urise);
            }
        }
    }
}
