/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.svg;

import org.w3c.dom.Element;

/**
 * Created on : Oct 5, 2011, 6:47:21 PM
 *
 * @author Peter Withers
 */
public class GraphicsDragHandle extends RelationDragHandle {

//     protected UniqueIdentifier graphicsIdentifier;
    protected KinElement graphicsElement;
    protected KinElement highlightElement;
    protected KinElement highlightRectElement;
    private String xAttribute;
    private String yAttribute;
    private boolean isCenteredElement = false;

    public GraphicsDragHandle(KinElement graphicsElement, KinElement highlightElement, KinElement highlightRectElement, float elementStartX, float elementStartY, float mouseStartX, float mouseStartY, double diagramScaleFactor) throws KinElementException {
        super(null, null, elementStartX, elementStartY, mouseStartX, mouseStartY, diagramScaleFactor);
        this.graphicsElement = graphicsElement;
        this.highlightElement = highlightElement;
        this.highlightRectElement = highlightRectElement;

        String elementType = graphicsElement.getTagName();
        if (elementType.equals("circle")) {
            isCenteredElement = true;
            xAttribute = "r";
            yAttribute = null;
        } else if (elementType.equals("rect")) {
            xAttribute = "width";
            yAttribute = "height";
        } else if (elementType.equals("ellipse")) {
            isCenteredElement = true;
            xAttribute = "rx";
            yAttribute = "ry";
        }
    }

    protected void updatedElement(float localDragNodeX, float localDragNodeY, int paddingDistance) throws KinElementException {
        // this must be only called from within a svg runnable

        float dragNodeX = getTranslatedX(localDragNodeX);
        float dragNodeY = getTranslatedY(localDragNodeY);

        int minSize = 3;
        if (dragNodeX < paddingDistance + minSize) {
            dragNodeX = paddingDistance + minSize;
        }
        if (dragNodeY < paddingDistance + minSize) {
            dragNodeY = paddingDistance + minSize;
        }
        graphicsElement.setAttribute(xAttribute, Float.toString(dragNodeX - paddingDistance));
        if (yAttribute != null) {
            graphicsElement.setAttribute(yAttribute, Float.toString(dragNodeY - paddingDistance));
        } else {
            dragNodeY = dragNodeX;
        }
        if (isCenteredElement) {
            highlightRectElement.setAttribute("x", Float.toString(-(dragNodeX)));
            highlightRectElement.setAttribute("y", Float.toString(-(dragNodeY)));

            highlightRectElement.setAttribute("width", Float.toString(dragNodeX * 2) + paddingDistance);
            highlightRectElement.setAttribute("height", Float.toString(dragNodeY * 2) + paddingDistance);
        } else {
            highlightRectElement.setAttribute("width", Float.toString((dragNodeX + paddingDistance)));
            highlightRectElement.setAttribute("height", Float.toString((dragNodeY + paddingDistance)));
        }
        highlightElement.setAttribute("cx", Float.toString(dragNodeX));
        highlightElement.setAttribute("cy", Float.toString(dragNodeY));
    }
}
