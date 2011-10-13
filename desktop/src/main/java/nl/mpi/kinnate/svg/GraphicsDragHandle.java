package nl.mpi.kinnate.svg;

import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import org.w3c.dom.Element;

/**
 *  Document   : GraphicsDragHandle
 *  Created on : Oct 5, 2011, 6:47:21 PM
 *  Author     : Peter Withers
 */
public class GraphicsDragHandle extends RelationDragHandle {

    protected UniqueIdentifier graphicsIdentifier;
    protected Element graphicsElement;
    protected Element highlightElement;
    protected Element highlightRectElement;
    private String xAttribute;
    private String yAttribute;

    public GraphicsDragHandle(Element graphicsElement, Element highlightElement, Element highlightRectElement, float elementStartX, float elementStartY, float mouseStartX, float mouseStartY, double diagramScaleFactor) {
        super(null, elementStartX, elementStartY, mouseStartX, mouseStartY, diagramScaleFactor);
        this.graphicsElement = graphicsElement;
        this.highlightElement = highlightElement;
        this.highlightRectElement = highlightRectElement;
//               this.highlightPadding
        String elementType = graphicsElement.getTagName();
        if (elementType.equals("circle")) {
            xAttribute = "r";
            yAttribute = null;
        } else if (elementType.equals("rect")) {
            xAttribute = "width";
            yAttribute = "height";
        } else if (elementType.equals("ellipse")) {
            xAttribute = "rx";
            yAttribute = "ry";
        }
    }

    protected void updatedElement(float localDragNodeX, float localDragNodeY) {
        // this must be only called from within a svg runnable

        float dragNodeX = getTranslatedX(localDragNodeX);
        float dragNodeY = getTranslatedY(localDragNodeY);

        if (dragNodeX < 1) {
            dragNodeX = 1;
        }
        if (dragNodeY < 1) {
            dragNodeY = 1;
        }
        int highlightRectMultiplier = 2;
        graphicsElement.setAttribute(xAttribute, Float.toString(dragNodeX));
        if (yAttribute != null) {
            highlightRectMultiplier = 1;
            graphicsElement.setAttribute(yAttribute, Float.toString(dragNodeY));
        }
        highlightElement.setAttribute("cx", Float.toString(dragNodeX));
        highlightElement.setAttribute("cy", Float.toString(dragNodeY));
        highlightRectElement.setAttribute("width", Float.toString(dragNodeX * highlightRectMultiplier));
        highlightRectElement.setAttribute("height", Float.toString(dragNodeY * highlightRectMultiplier));
    }
}
