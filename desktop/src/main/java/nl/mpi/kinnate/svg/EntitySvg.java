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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityDate;
import nl.mpi.kinnate.kindata.ExternalLink;
import nl.mpi.kinnate.kindata.GraphLabel;
import nl.mpi.kinnate.uniqueidentifiers.IdentifierException;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;

/**
 * Document : EntitySvg Created on : Mar 9, 2011, 3:20:56 PM
 *
 * @author Peter Withers
 */
public class EntitySvg {

    protected HashMap<UniqueIdentifier, Point> entityPositions = new HashMap<UniqueIdentifier, Point>();
    static final public int symbolSize = 15;
    static final protected int strokeWidth = 2;
    private MessageDialogHandler dialogHandler;

    public EntitySvg(MessageDialogHandler dialogHandler) {
        this.dialogHandler = dialogHandler;
    }

    public void discardEntityPositions() {
        for (UniqueIdentifier uniqueIdentifier : entityPositions.keySet().toArray(new UniqueIdentifier[0])) {
            if (!uniqueIdentifier.isGraphicsIdentifier()) {
                entityPositions.remove(uniqueIdentifier);
            }
        }
    }

    public void readEntityPositions(Node entityGroup) {
        // read the entity positions from the existing dom
        // this now replaces the position values in the entity data and the entity position is now stored in the svg entity visual not the entity data 
        if (entityGroup != null) {
            for (Node entityNode = entityGroup.getFirstChild(); entityNode != null; entityNode = entityNode.getNextSibling()) {
                try {
                    NamedNodeMap nodeMap = entityNode.getAttributes();
                    if (nodeMap != null) {
                        Node idNode = nodeMap.getNamedItem("id");
                        Node transformNode = nodeMap.getNamedItem("transform");
                        if (idNode != null && transformNode != null) {
                            UniqueIdentifier entityId = new UniqueIdentifier(idNode.getNodeValue());
                            //transform="translate(300.0, 192.5)"
                            // because the svg dom has not been rendered we cannot get any of the screen data, so we must parse the transform tag
                            String transformString = transformNode.getNodeValue();
                            transformString = transformString.replaceAll("\\s", "");
                            transformString = transformString.replace("translate(", "");
                            transformString = transformString.replace(")", "");
                            String[] stringPos = transformString.split(",");
//                        System.out.println("entityId: " + entityId);
//                        System.out.println("transformString: " + transformString);
                            entityPositions.put(entityId, new Point((int) Float.parseFloat(stringPos[0]), (int) Float.parseFloat(stringPos[1]))); // the input data here could be in float format
//                      SVGRect bbox = ((SVGLocatable) entityNode).getBBox();
//                      SVGMatrix sVGMatrix = ((SVGLocatable) entityNode).getCTM();
//                      System.out.println("getE: " + sVGMatrix.getE());
//                      System.out.println("getF: " + sVGMatrix.getF());
////                    System.out.println("bbox X: " + bbox.getX());
////                    System.out.println("bbox Y: " + bbox.getY());
////                    System.out.println("bbox W: " + bbox.getWidth());
////                    System.out.println("bbox H: " + bbox.getHeight());
//                      new float[]{sVGMatrix.getE() + bbox.getWidth() / 2, sVGMatrix.getF() + bbox.getHeight() / 2};
                        }
                    }
                } catch (IdentifierException exception) {
//                    GuiHelper.linorgBugCatcher.logError(exception);
                    dialogHandler.addMessageDialogToQueue("Failed to read an entity position, layout might not be preserved", "Restore Layout");
                }
            }
        }
    }

    private void removeAttributeStrokeBlack(Node symbolNode) {
        // remove all child attributes that set the stroke to black so that existing svg files are updated and the stroke can be changed on composing the diagram
        if (symbolNode instanceof Element) {
            final Element symbolElement = (Element) symbolNode;
            if (symbolElement.getAttribute("stroke").contentEquals("black")) {
                symbolElement.removeAttribute("stroke");
            }
            Node childNode = symbolNode.getFirstChild();
            while (childNode != null) {
                removeAttributeStrokeBlack(childNode);
                childNode = childNode.getNextSibling();
            }
        }
    }

    public Element updateSymbolsElement(SVGDocument doc, String svgNameSpace) {
        Element svgRoot = doc.getDocumentElement();
        Element lineMarkerDefsNode = doc.getElementById("LineMarkerSymbols");
        if (lineMarkerDefsNode == null) {
            lineMarkerDefsNode = doc.createElementNS(svgNameSpace, "defs");
            lineMarkerDefsNode.setAttribute("id", "LineMarkerSymbols");
            svgRoot.appendChild(lineMarkerDefsNode);
        }
        // add the start line marker
        Element startMarker = doc.getElementById("StartMarker");
        if (startMarker == null) {
            startMarker = doc.createElementNS(svgNameSpace, "circle");
            startMarker.setAttribute("id", "StartMarker");
            startMarker.setAttribute("cx", "6");
            startMarker.setAttribute("cy", "6");
            startMarker.setAttribute("r", "5");
            lineMarkerDefsNode.appendChild(startMarker);
        }
        // add the end line marker
        Element endMarker = doc.getElementById("EndMarker");
        if (endMarker == null) {
            endMarker = doc.createElementNS(svgNameSpace, "path");
            endMarker.setAttribute("id", "EndMarker");
            endMarker.setAttribute("d", "M 0 0 L 10 5 L 0 10 z");
            lineMarkerDefsNode.appendChild(endMarker);
        }
        Element kinSymbols = doc.getElementById("KinSymbols");
        if (kinSymbols == null) {
            kinSymbols = doc.createElementNS(svgNameSpace, "defs");
            kinSymbols.setAttribute("id", "KinSymbols");
            svgRoot.appendChild(kinSymbols);
        }

        // add the blank symbol
        Element blankGroup = doc.getElementById("blank");
        if (blankGroup == null) {
            blankGroup = doc.createElementNS(svgNameSpace, "g");
            blankGroup.setAttribute("id", "blank");
            Element blankNode = doc.createElementNS(svgNameSpace, "circle");
            blankNode.setAttribute("cx", Float.toString(symbolSize + strokeWidth / 4f));
            blankNode.setAttribute("cy", Float.toString(symbolSize + strokeWidth / 4f));
            blankNode.setAttribute("r", Integer.toString((symbolSize - strokeWidth / 2)));
//        circleNode.setAttribute("height", Integer.toString(symbolSize - (strokeWidth * 3)));
            blankNode.setAttribute("stroke", "none");
            blankNode.setAttribute("fill", "none");
            blankGroup.appendChild(blankNode);
            kinSymbols.appendChild(blankGroup);
        }
        // add the circle symbol
        Element circleGroup = doc.getElementById("circle");
        if (circleGroup == null) {
            circleGroup = doc.createElementNS(svgNameSpace, "g");
            circleGroup.setAttribute("id", "circle");
            Element circleNode = doc.createElementNS(svgNameSpace, "circle");
            circleNode.setAttribute("cx", Float.toString(symbolSize / 2 + strokeWidth / 4f));
            circleNode.setAttribute("cy", Float.toString(symbolSize / 2 + strokeWidth / 4f));
            circleNode.setAttribute("r", Integer.toString((symbolSize - strokeWidth) / 2));
//        circleNode.setAttribute("height", Integer.toString(symbolSize - (strokeWidth * 3)));
//            circleNode.setAttribute("stroke", "black");
            circleNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
            circleGroup.appendChild(circleNode);
            kinSymbols.appendChild(circleGroup);
        } else {
            removeAttributeStrokeBlack(circleGroup);
        }
        // add the square symbol
        Element squareGroup = doc.getElementById("square");
        if (squareGroup == null) {
            squareGroup = doc.createElementNS(svgNameSpace, "g");
            squareGroup.setAttribute("id", "square");
            Element squareNode = doc.createElementNS(svgNameSpace, "rect");
            squareNode.setAttribute("x", Integer.toString(strokeWidth));
            squareNode.setAttribute("y", Integer.toString(strokeWidth));
            squareNode.setAttribute("width", Integer.toString(symbolSize - strokeWidth * 2));
            squareNode.setAttribute("height", Integer.toString(symbolSize - strokeWidth * 2));
//            squareNode.setAttribute("stroke", "black");
            squareNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
            squareGroup.appendChild(squareNode);
            kinSymbols.appendChild(squareGroup);
        } else {
            removeAttributeStrokeBlack(squareGroup);
        }
        // add the square symbol
        Element squareGroup45 = doc.getElementById("square-45");
        if (squareGroup45 == null) {
            squareGroup45 = doc.createElementNS(svgNameSpace, "g");
            squareGroup45.setAttribute("id", "square-45");
            Element squareNode45 = doc.createElementNS(svgNameSpace, "rect");
            squareNode45.setAttribute("transform", "rotate(-45 " + Integer.toString(symbolSize / 2) + " " + Integer.toString(symbolSize / 2) + ")");
            squareNode45.setAttribute("x", Integer.toString(strokeWidth));
            squareNode45.setAttribute("y", Integer.toString(strokeWidth));
            squareNode45.setAttribute("width", Integer.toString(symbolSize - strokeWidth * 2));
            squareNode45.setAttribute("height", Integer.toString(symbolSize - strokeWidth * 2));
//            squareNode45.setAttribute("stroke", "black");
            squareNode45.setAttribute("stroke-width", Integer.toString(strokeWidth));
            squareGroup45.appendChild(squareNode45);
            kinSymbols.appendChild(squareGroup45);
        } else {
            removeAttributeStrokeBlack(squareGroup45);
        }
        // add the rhombus symbol
        Element rhombusGroup = doc.getElementById("rhombus");
        if (rhombusGroup == null) {
            rhombusGroup = doc.createElementNS(svgNameSpace, "g");
            rhombusGroup.setAttribute("id", "rhombus");
            Element rhombusNode = doc.createElementNS(svgNameSpace, "rect");
            rhombusNode.setAttribute("transform", "translate(0, " + Integer.toString(symbolSize / 3) + "), scale(1,0.5), rotate(-45 " + Integer.toString(symbolSize / 2) + " " + Integer.toString(symbolSize / 2) + ")");
            rhombusNode.setAttribute("x", Integer.toString(strokeWidth));
            rhombusNode.setAttribute("y", Integer.toString(strokeWidth));
            rhombusNode.setAttribute("width", Integer.toString(symbolSize - strokeWidth * 2));
            rhombusNode.setAttribute("height", Integer.toString(symbolSize - strokeWidth * 2));
//            rhombusNode.setAttribute("stroke", "black");
            rhombusNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
            rhombusGroup.appendChild(rhombusNode);
            kinSymbols.appendChild(rhombusGroup);
        } else {
            removeAttributeStrokeBlack(rhombusGroup);
        }
//        // add the rhombus symbol
//        Element rhombusGroup90 = doc.createElementNS(svgNameSpace, "g");
//        rhombusGroup90.setAttribute("id", "rhombus90");
//        Element rhombusNode90 = doc.createElementNS(svgNameSpace, "rect");
//        rhombusNode90.setAttribute("transform", "scale(1,0.5), rotate(45 " + Integer.toString(symbolSize / 2) + " " + Integer.toString(symbolSize / 2) + ")");
//        rhombusNode90.setAttribute("x", Integer.toString(strokeWidth));
//        rhombusNode90.setAttribute("y", Integer.toString(strokeWidth));
//        rhombusNode90.setAttribute("width", Integer.toString(symbolSize - strokeWidth * 2));
//        rhombusNode90.setAttribute("height", Integer.toString(symbolSize - strokeWidth * 2));
//        //rhombusNode90.setAttribute("stroke", "black");
//        rhombusNode90.setAttribute("stroke-width", Integer.toString(strokeWidth));
//        rhombusGroup90.appendChild(rhombusNode90);
//        defsNode.appendChild(rhombusGroup90);

        // add the union symbol
        Element unionGroup = doc.getElementById("union");
        if (unionGroup == null) {
            unionGroup = doc.createElementNS(svgNameSpace, "g");
            unionGroup.setAttribute("id", "union");
            Element upperNode = doc.createElementNS(svgNameSpace, "line");
            Element lowerNode = doc.createElementNS(svgNameSpace, "line");
            upperNode.setAttribute("x1", Integer.toString(0));
            upperNode.setAttribute("y1", Integer.toString((symbolSize / 6)));
            upperNode.setAttribute("x2", Integer.toString(symbolSize));
            upperNode.setAttribute("y2", Integer.toString((symbolSize / 6)));
            upperNode.setAttribute("stroke-width", Integer.toString(symbolSize / 3));
//            upperNode.setAttribute("stroke", "black");
            lowerNode.setAttribute("x1", Integer.toString(0));
            lowerNode.setAttribute("y1", Integer.toString(symbolSize - (symbolSize / 6)));
            lowerNode.setAttribute("x2", Integer.toString(symbolSize));
            lowerNode.setAttribute("y2", Integer.toString(symbolSize - (symbolSize / 6)));
            lowerNode.setAttribute("stroke-width", Integer.toString(symbolSize / 3));
//            lowerNode.setAttribute("stroke", "black");
            // add a background for selecting and draging
            Element backgroundNode = doc.createElementNS(svgNameSpace, "rect");
            backgroundNode.setAttribute("x", "0");
            backgroundNode.setAttribute("y", "0");
            backgroundNode.setAttribute("width", Integer.toString(symbolSize));
            backgroundNode.setAttribute("height", Integer.toString(symbolSize));
            backgroundNode.setAttribute("stroke", "none");
            backgroundNode.setAttribute("fill", "white");
            unionGroup.appendChild(backgroundNode);
            unionGroup.appendChild(upperNode);
            unionGroup.appendChild(lowerNode);
            kinSymbols.appendChild(unionGroup);
        } else {
            removeAttributeStrokeBlack(unionGroup);
        }
        // add the triangle symbol        
        int triangleSize = symbolSize - strokeWidth / 2;
        int triangleHeight = (int) (Math.sqrt(3) * triangleSize / 2);
        Element triangle = doc.getElementById("triangle");
        if (triangle == null) {
            Element triangleGroup = doc.createElementNS(svgNameSpace, "g");
            triangleGroup.setAttribute("id", "triangle");
            Element triangleNode = doc.createElementNS(svgNameSpace, "polygon");
            triangleNode.setAttribute("points", (symbolSize / 2) + "," + strokeWidth / 2 + " "
                    + strokeWidth / 2 + "," + triangleHeight
                    + " " + triangleSize + "," + triangleHeight);
//            triangleNode.setAttribute("stroke", "black");
            triangleNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
            triangleGroup.appendChild(triangleNode);
            kinSymbols.appendChild(triangleGroup);
        } else {
            removeAttributeStrokeBlack(triangle);
        }
        // add the triangle symbol
        Element triangleGroup1 = doc.getElementById("triangle-270");
        if (triangleGroup1 == null) {
            triangleGroup1 = doc.createElementNS(svgNameSpace, "g");
            triangleGroup1.setAttribute("id", "triangle-270");
            Element triangleNode1 = doc.createElementNS(svgNameSpace, "polygon");
            triangleNode1.setAttribute("transform", "rotate(-90 " + Integer.toString(symbolSize / 2) + " " + Integer.toString(symbolSize / 2) + ")");
            triangleNode1.setAttribute("points", (symbolSize / 2) + "," + strokeWidth / 2 + " "
                    + strokeWidth / 2 + "," + triangleHeight
                    + " " + triangleSize + "," + triangleHeight);
//            triangleNode1.setAttribute("stroke", "black");
            triangleNode1.setAttribute("stroke-width", Integer.toString(strokeWidth));
            triangleGroup1.appendChild(triangleNode1);
            kinSymbols.appendChild(triangleGroup1);
        } else {
            removeAttributeStrokeBlack(triangleGroup1);
        }
        // add the triangle symbol
        Element triangleGroup2 = doc.getElementById("triangle-180");
        if (triangleGroup2 == null) {
            triangleGroup2 = doc.createElementNS(svgNameSpace, "g");
            triangleGroup2.setAttribute("id", "triangle-180");
            triangleGroup2.setAttribute("transform", "rotate(180 " + Integer.toString(symbolSize / 2) + " " + Integer.toString(symbolSize / 2) + ")");
            Element triangleNode2 = doc.createElementNS(svgNameSpace, "polygon");
            triangleNode2.setAttribute("points", (symbolSize / 2) + "," + strokeWidth / 2 + " "
                    + strokeWidth / 2 + "," + triangleHeight
                    + " " + triangleSize + "," + triangleHeight);
//            triangleNode2.setAttribute("stroke", "black");
            triangleNode2.setAttribute("stroke-width", Integer.toString(strokeWidth));
            triangleGroup2.appendChild(triangleNode2);
            kinSymbols.appendChild(triangleGroup2);
        } else {
            removeAttributeStrokeBlack(triangleGroup2);
        }
        // add the triangle symbol
        Element triangleGroup3 = doc.getElementById("triangle-90");
        if (triangleGroup3 == null) {
            triangleGroup3 = doc.createElementNS(svgNameSpace, "g");
            triangleGroup3.setAttribute("id", "triangle-90");
            triangleGroup3.setAttribute("transform", "rotate(90 " + Integer.toString(symbolSize / 2) + " " + Integer.toString(symbolSize / 2) + ")");
            Element triangleNode3 = doc.createElementNS(svgNameSpace, "polygon");
            triangleNode3.setAttribute("points", (symbolSize / 2) + "," + strokeWidth / 2 + " "
                    + strokeWidth / 2 + "," + triangleHeight
                    + " " + triangleSize + "," + triangleHeight);
//            triangleNode3.setAttribute("stroke", "black");
            triangleNode3.setAttribute("stroke-width", Integer.toString(strokeWidth));
            triangleGroup3.appendChild(triangleNode3);
            kinSymbols.appendChild(triangleGroup3);
        } else {
            removeAttributeStrokeBlack(triangleGroup3);
        }
//        // add the equals symbol
//        Element equalsGroup = doc.createElementNS(svgNameSpace, "g");
//        equalsGroup.setAttribute("id", "equals");
//        Element equalsNode = doc.createElementNS(svgNameSpace, "polyline");
//        int offsetAmounta = symbolSize / 2;
//        int posXa = 0;
//        int posYa = +symbolSize / 2;
//        equalsNode.setAttribute("points", (posXa + offsetAmounta * 3) + "," + (posYa + offsetAmounta) + " " + (posXa - offsetAmounta) + "," + (posYa + offsetAmounta) + " " + (posXa - offsetAmounta) + "," + (posYa - offsetAmounta) + " " + (posXa + offsetAmounta * 3) + "," + (posYa - offsetAmounta));
//        equalsNode.setAttribute("fill", "white");
//        equalsNode.setAttribute("stroke", "black");
//        equalsNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
//        equalsGroup.appendChild(equalsNode);
//        defsNode.appendChild(equalsGroup);

        // add the error symbol
        Element noneGroup = doc.getElementById("error");
        if (noneGroup == null) {
            noneGroup = doc.createElementNS(svgNameSpace, "g");
            noneGroup.setAttribute("id", "error");
            Element noneNode = doc.createElementNS(svgNameSpace, "polyline");
            int posXnone = symbolSize / 2;
            int posYnone = symbolSize / 2;
            int offsetNoneAmount = symbolSize;
            noneNode.setAttribute("points", (posXnone - offsetNoneAmount) + "," + (posYnone - offsetNoneAmount) + " " + (posXnone + offsetNoneAmount) + "," + (posYnone + offsetNoneAmount) + " " + (posXnone) + "," + (posYnone) + " " + (posXnone - offsetNoneAmount) + "," + (posYnone + offsetNoneAmount)
                    + " " + (posXnone + offsetNoneAmount) + "," + (posYnone - offsetNoneAmount)
                    + " " + (posXnone - offsetNoneAmount) + "," + (posYnone - offsetNoneAmount)
                    + " " + (posXnone - offsetNoneAmount) + "," + (posYnone + offsetNoneAmount)
                    + " " + (posXnone + offsetNoneAmount) + "," + (posYnone + offsetNoneAmount)
                    + " " + (posXnone + offsetNoneAmount) + "," + (posYnone - offsetNoneAmount));
            noneNode.setAttribute("fill", "none");
            noneNode.setAttribute("stroke", "red");
            noneNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
            noneGroup.appendChild(noneNode);
            kinSymbols.appendChild(noneGroup);
        }
        for (String markerColour : new String[]{"black", "orange", "cyan", "purple", "red", "green", "blue"}) {// todo: add a few more colours
            // add the marker symbols
            Element markerGroup = doc.getElementById(markerColour + "marker");
            if (markerGroup == null) {
                markerGroup = doc.createElementNS(svgNameSpace, "g");
                markerGroup.setAttribute("id", markerColour + "marker");
                Element markerNode = doc.createElementNS(svgNameSpace, "circle");
                markerNode.setAttribute("cx", Integer.toString(symbolSize));
                markerNode.setAttribute("cy", "0");
                markerNode.setAttribute("r", Integer.toString((symbolSize) / 4));
//        circleNode.setAttribute("height", Integer.toString(symbolSize - (strokeWidth * 3)));
                markerNode.setAttribute("stroke", "none");
                markerNode.setAttribute("fill", markerColour);
                markerGroup.appendChild(markerNode);
                kinSymbols.appendChild(markerGroup);
            }
            // add the strike through symbol
            Element lineGroup = doc.getElementById(markerColour + "strikethrough");
            if (lineGroup == null) {
                lineGroup = doc.createElementNS(svgNameSpace, "g");
                lineGroup.setAttribute("id", markerColour + "strikethrough");
                Element lineNode = doc.createElementNS(svgNameSpace, "polyline");
                lineNode.setAttribute("points", "0," + (symbolSize) + " " + (symbolSize) + ",0");
                lineNode.setAttribute("fill", "none");
                lineNode.setAttribute("stroke", markerColour);
                lineNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
                lineGroup.appendChild(lineNode);
                kinSymbols.appendChild(lineGroup);
            }
            // add the cross symbol
            Element crossGroup = doc.getElementById(markerColour + "cross");
            if (crossGroup == null) {
                crossGroup = doc.createElementNS(svgNameSpace, "g");
                crossGroup.setAttribute("id", markerColour + "cross");
                Element crossNode = doc.createElementNS(svgNameSpace, "polyline");
                int posX = symbolSize / 2;
                int posY = symbolSize / 2;
                int offsetAmount = symbolSize / 2;
                crossNode.setAttribute("points", (posX - offsetAmount) + "," + (posY - offsetAmount) + " " + (posX + offsetAmount) + "," + (posY + offsetAmount) + " " + (posX) + "," + (posY) + " " + (posX - offsetAmount) + "," + (posY + offsetAmount) + " " + (posX + offsetAmount) + "," + (posY - offsetAmount));
                crossNode.setAttribute("fill", "none");
                crossNode.setAttribute("stroke", markerColour);
                crossNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
                crossGroup.appendChild(crossNode);
                kinSymbols.appendChild(crossGroup);
            }
        }
        return kinSymbols;
    }

    public String[] listSymbolNames(SVGDocument doc, String svgNameSpace) {
        // get the symbol list from the dom
        ArrayList<String> symbolArray = new ArrayList<String>();

        Element kinSymbols = updateSymbolsElement(doc, svgNameSpace);
        for (Node kinSymbolNode = kinSymbols.getFirstChild(); kinSymbolNode != null; kinSymbolNode = kinSymbolNode.getNextSibling()) {
            NamedNodeMap attributesMap = kinSymbolNode.getAttributes();
            if (attributesMap != null) {
                Node idNode = attributesMap.getNamedItem("id");
                if (idNode != null) {
                    symbolArray.add(idNode.getNodeValue());
                }
            }
        }
        return symbolArray.toArray(new String[]{});
    }

    public void clearEntityLocations(UniqueIdentifier[] selectedIdentifiers) {
        for (UniqueIdentifier currentIdentifier : selectedIdentifiers) {
            entityPositions.remove(currentIdentifier);
        }
    }

    public UniqueIdentifier getClosestEntity(float[] locationArray, int maximumDistance, ArrayList<UniqueIdentifier> excludeIdentifiers) {
        double closestDistance = -1;
        UniqueIdentifier closestIdentifier = null;
        for (Entry<UniqueIdentifier, Point> currentEntry : entityPositions.entrySet()) {
            if (!currentEntry.getKey().isGraphicsIdentifier()) {
                if (!excludeIdentifiers.contains(currentEntry.getKey())) {
                    float hDistance = locationArray[0] - currentEntry.getValue().x;
                    float vDistance = locationArray[1] - currentEntry.getValue().y;
                    double entityDistance = Math.sqrt(hDistance * hDistance + vDistance * vDistance);
                    if (closestIdentifier == null) {
                        closestDistance = entityDistance;
                        closestIdentifier = currentEntry.getKey();
                    }
                    if (entityDistance < closestDistance) {
                        closestDistance = entityDistance;
                        closestIdentifier = currentEntry.getKey();
                    }
                }
            }
        }
//        System.out.println("closestDistance: " + closestDistance);
        if (maximumDistance < closestDistance) {
            return null;
        }
        return closestIdentifier;
    }

    public Point[] getAllEntityLocations() {
        return entityPositions.values().toArray(new Point[0]);
    }

    public Point getEntityLocationOffset(UniqueIdentifier entityId) {
//         this offset is added so that the relation lines meet to the center of the symbols, however the symbols should be updated so that they are centered on 0
        Point returnLoc = entityPositions.get(entityId);
        if (returnLoc != null) {
            int xPos = returnLoc.x + (symbolSize / 2);
            int yPos = returnLoc.y + (symbolSize / 2);
            return new Point(xPos, yPos);
        } else {
            return null;
        }
    }

    public List<UniqueIdentifier> getEntitiesWithinRect(Rectangle rectangle) {
        ArrayList<UniqueIdentifier> selectedIdentifiers = new ArrayList<UniqueIdentifier>();
        for (Entry<UniqueIdentifier, Point> entry : entityPositions.entrySet()) {
            if (rectangle.contains(entry.getValue())) {
                selectedIdentifiers.add(entry.getKey());
            }
        }
        return selectedIdentifiers;
    }

    public Point getEntityLocation(UniqueIdentifier entityId) {
        Point returnLoc = entityPositions.get(entityId);
        return returnLoc;
    }
//    public float[] getEntityLocationOffset(SVGDocument doc, String entityId) {
//        Element entitySymbol = doc.getElementById(entityId + "symbol");
////        Element entitySymbol = doc.getElementById(entityId); // the sybol group node
//        if (entitySymbol != null) {
//            SVGRect bbox = ((SVGLocatable) entitySymbol).getBBox();
//            SVGMatrix sVGMatrix = ((SVGLocatable) entitySymbol).getCTM();
////            System.out.println("getA: " + sVGMatrix.getA());
////            System.out.println("getB: " + sVGMatrix.getB());
////            System.out.println("getC: " + sVGMatrix.getC());
////            System.out.println("getD: " + sVGMatrix.getD());
////            System.out.println("getE: " + sVGMatrix.getE());
////            System.out.println("getF: " + sVGMatrix.getF());
//
////            System.out.println("bbox X: " + bbox.getX());
////            System.out.println("bbox Y: " + bbox.getY());
////            System.out.println("bbox W: " + bbox.getWidth());
////            System.out.println("bbox H: " + bbox.getHeight());
//            return new float[]{sVGMatrix.getE() + bbox.getWidth() / 2, sVGMatrix.getF() + bbox.getHeight() / 2};
////            bbox.setX(sVGMatrix.getE());
////            bbox.setY(sVGMatrix.getF());
////            return bbox;
//        } else {
//            return null;
//        }
//    }

    public float[] moveEntity(GraphPanel graphPanel, UniqueIdentifier entityId, float shiftXfloat, float remainderAfterSnapX, float shiftYfloat, float remainderAfterSnapY, boolean snapToGrid, double scaleFactor, boolean allRealtionsSelected) {
//        System.out.println("X: " + shiftXfloat + " Y: " + shiftYfloat + " scale: " + scaleFactor);
        Element entitySymbol = graphPanel.doc.getElementById(entityId.getAttributeIdentifier());
        Element highlightGroup = null;
        if (entityId.isGraphicsIdentifier()) {
            highlightGroup = graphPanel.doc.getElementById("highlight_" + entityId.getAttributeIdentifier());
        }
        double shiftXscaled;
        double shiftYscaled;
        if (entitySymbol != null) {
            boolean allowYshift = true; //entitySymbol.getLocalName().equals("text");
            shiftXscaled = shiftXfloat * scaleFactor;
            shiftYscaled = shiftYfloat * scaleFactor;
            Point entityPosition = entityPositions.get(entityId);
            final float updatedPositionX = entityPosition.x + (float) shiftXscaled + remainderAfterSnapX;
            final float updatedPositionY = entityPosition.y + (float) shiftYscaled + remainderAfterSnapY;
            int snappedPositionX;
            int snappedPositionY = entityPosition.y;
//            if (allowYshift) {
//                snappedPositionY = (int) (updatedPositionY + shiftYscaled);
//            }
            if (snapToGrid) {
                snappedPositionX = Math.round(updatedPositionX / 50) * 50; // limit movement to the grid
                if (allowYshift) {
                    snappedPositionY = Math.round(updatedPositionY / 50) * 50; // limit movement to the grid                        
                }
            } else {
                snappedPositionX = Math.round(updatedPositionX);  // prevent uncorrectable but visible variations in the position of entities to each other
                if (allowYshift) {
                    snappedPositionY = Math.round(updatedPositionY);
                }
            }
            // check if the moved entity is over another, if it is then shift on the x axis
            boolean collisionFound = true;
            while (collisionFound) {
                collisionFound = false;
                for (Point currentEntity : entityPositions.values()) {
                    if (currentEntity != entityPosition) { // if looped over the point is not the point that is being moved
                        if (currentEntity.distance(snappedPositionX, snappedPositionY) < EntitySvg.symbolSize) {
                            collisionFound = true;
                            // keep it simple and only shift left, so that a loop of bouncing left and right cannot occur
                            snappedPositionX = snappedPositionX - EntitySvg.symbolSize;
                        }
//                        // if the Y axis overlaps then check the X axis
//                        if (updatedPositionY > currentEntity.y - EntitySvg.symbolSize && updatedPositionY < currentEntity.y + EntitySvg.symbolSize) {
//                            if (updatedPositionX > currentEntity.x - EntitySvg.symbolSize && updatedPositionX <= currentEntity.x) {
////                                System.out.println("shift left");
//                                collisionFound = true;
//                                snappedPositionX = currentEntity.x - EntitySvg.symbolSize;
//                            } else if (updatedPositionX < currentEntity.x + EntitySvg.symbolSize && updatedPositionX >= currentEntity.x) {
////                                System.out.println("shift right");
//                                collisionFound = true;
//                                snappedPositionX = currentEntity.x + EntitySvg.symbolSize;
//                            }
//                        }
                    }
                }
            }
//            AffineTransform at = new AffineTransform();
//            at.translate(updatedPositionX, updatedPositionY);
////                at.scale(scale, scale);
//            at.concatenate(graphPanel.svgCanvas.getRenderingTransform());
//                svgCanvas.setRenderingTransform(at);

//            System.out.println("updatedPosition after snap: " + updatedPosition);
//                graphPanel.dataStoreSvg.graphData.setEntityLocation(entityId, updatedPositionX, updatedPositionY);
//            entityPositions.put(entityId, new float[]{(float) at.getTranslateX(), (float) at.getTranslateY()});
//            ((Element) entitySymbol).setAttribute("transform", "translate(" + String.valueOf(at.getTranslateX()) + ", " + String.valueOf(at.getTranslateY()) + ")");
            // store the changed location as a preferred location
            final Point updatedLocationPoint = new Point((int) snappedPositionX, (int) snappedPositionY);
            entityPositions.put(entityId, updatedLocationPoint);
            graphPanel.dataStoreSvg.graphData.setPreferredEntityLocation(new UniqueIdentifier[]{entityId}, updatedLocationPoint);
            final String translateString = "translate(" + String.valueOf(updatedLocationPoint.x) + ", " + String.valueOf(updatedLocationPoint.y) + ")";
            ((Element) entitySymbol).setAttribute("transform", translateString);
            if (highlightGroup != null) {
                highlightGroup.setAttribute("transform", translateString);
            }
            remainderAfterSnapX = updatedPositionX - updatedLocationPoint.x;
            remainderAfterSnapY = updatedPositionY - updatedLocationPoint.y;
        }
        return new float[]{remainderAfterSnapX, remainderAfterSnapY};
    }

    private int addTextLabel(GraphPanel graphPanel, Element groupNode, String currentTextLable, String textColour, int textSpanCounter) {
        int lineSpacing = 15;
        Element labelText = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "text");
        labelText.setAttribute("x", Double.toString(symbolSize * 1.5));
        labelText.setAttribute("y", Integer.toString(textSpanCounter));
        labelText.setAttribute("fill", textColour);
        labelText.setAttribute("stroke-width", "0");
        labelText.setAttribute("font-size", "14");
        Text textNode = graphPanel.doc.createTextNode(currentTextLable);
        labelText.appendChild(textNode);
        textSpanCounter += lineSpacing;
        groupNode.appendChild(labelText);
        return textSpanCounter;
    }

    protected Element createEntitySymbol(GraphPanel graphPanel, EntityData currentNode) {
        Element groupNode = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "g");
        groupNode.setAttribute("id", currentNode.getUniqueIdentifier().getAttributeIdentifier());
//        groupNode.setAttributeNS(DataStoreSvg.kinDataNameSpaceLocation, "kin:path", currentNode.getEntityPath());
        // the kin type strings are stored here so that on selection in the graph the add kin term panel can be pre populatedwith the kin type strings of the selection
        groupNode.setAttributeNS(DataStoreSvg.kinDataNameSpaceLocation, "kin:kintype", currentNode.getKinTypeString());
//        counterTest++;
        String[] symbolNames = currentNode.getSymbolNames(graphPanel.dataStoreSvg.defaultSymbol);
        if (symbolNames == null || symbolNames.length == 0) {
            symbolNames = new String[]{"blank"};
        }
        // todo: check that if an entity is already placed in which case do not recreate
        // todo: do not create a new dom each time but reuse it instead, or due to the need to keep things up to date maybe just store an array of entity locations instead
        Point storedPosition = entityPositions.get(currentNode.getUniqueIdentifier());
        if (storedPosition == null) {
            BugCatcherManager.getBugCatcher().logError(new Exception("No storedPosition found for: " + currentNode.getUniqueIdentifier().getAttributeIdentifier()));
            storedPosition = new Point(0, 0);
            // todo: it looks like the stored positon can be null
//            throw new Exception("Entity position should have been set in the graph sorter");
//            // loop through the filled locations and move to the right or left if not empty required
//            // todo: check the related nodes and average their positions then check to see if it is free and insert the node there
//            boolean positionFree = false;
//            float preferedX = currentNode.getxPos();
//            while (!positionFree) {
//                storedPosition = new Float[]{preferedX * hSpacing + hSpacing - symbolSize / 2.0f,
//                            currentNode.getyPos() * vSpacing + vSpacing - symbolSize / 2.0f};
//                if (entityPositions.isEmpty()) {
//                    break;
//                }
//                for (Float[] currentPosition : entityPositions.values()) {
//                    positionFree = !currentPosition[0].equals(storedPosition[0]) || !currentPosition[1].equals(storedPosition[1]);
//                    if (!positionFree) {
//                        break;
//                    }
//                }
//                preferedX++;
//            }
            entityPositions.put(currentNode.getUniqueIdentifier(), storedPosition);
        } else {
////            // prevent the y position being changed
//            storedPosition[1] = currentNode.getyPos() * vSpacing + vSpacing - symbolSize / 2.0f;
        }
        for (String currentSymbol : symbolNames) {
            Element symbolNode;
            symbolNode = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "use");
//            symbolNode.setAttribute("id", currentNode.getUniqueIdentifier().getAttributeIdentifier() + ":symbol:" + currentSymbol);
            symbolNode.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "#" + currentSymbol); // the xlink: of "xlink:href" is required for some svg viewers to render correctly

            // todo: resolve the null pointer on first run with transient nodes (last test on this did not get a null pointer so maybe it is resolved)
            groupNode.setAttribute("transform", "translate(" + Integer.toString(storedPosition.x) + ", " + Integer.toString(storedPosition.y) + ")");
            if (currentNode.isRequired) {
                symbolNode.setAttribute("stroke", "black");
                if (currentNode.isEgo) {
                    symbolNode.setAttribute("fill", "black");
                } else {
                    symbolNode.setAttribute("fill", "white");
                }
            } else {
                // if entity is transient not permanent then use a grey stroke and fill
                symbolNode.setAttribute("stroke", "grey");
                if (currentNode.isEgo) {
                    symbolNode.setAttribute("fill", "grey");
                } else {
                    symbolNode.setAttribute("fill", "white");
                }
            }
            symbolNode.setAttribute("stroke-width", "2");
            groupNode.appendChild(symbolNode);
        }
////////////////////////////// tspan method appears to fail in batik rendering process unless saved and reloaded ////////////////////////////////////////////////
//            Element labelText = doc.createElementNS(svgNS, "text");
////            labelText.setAttribute("x", Integer.toString(currentNode.xPos * hSpacing + hSpacing + symbolSize / 2));
////            labelText.setAttribute("y", Integer.toString(currentNode.yPos * vSpacing + vSpacing - symbolSize / 2));
//            labelText.setAttribute("fill", "black");
//            labelText.setAttribute("fill-opacity", "1");
//            labelText.setAttribute("stroke-width", "0");
//            labelText.setAttribute("font-size", "14px");
////            labelText.setAttribute("text-anchor", "end");
////            labelText.setAttribute("style", "font-size:14px;text-anchor:end;fill:black;fill-opacity:1");
//            //labelText.setNodeValue(currentChild.toString());
//
//            //String textWithUni = "\u0041";
//            int textSpanCounter = 0;
//            int lineSpacing = 10;
//            for (String currentTextLable : currentNode.getLabel()) {
//                Text textNode = doc.createTextNode(currentTextLable);
//                Element tspanElement = doc.createElement("tspan");
//                tspanElement.setAttribute("x", Integer.toString(currentNode.xPos * hSpacing + hSpacing + symbolSize / 2));
//                tspanElement.setAttribute("y", Integer.toString((currentNode.yPos * vSpacing + vSpacing - symbolSize / 2) + textSpanCounter));
////                tspanElement.setAttribute("y", Integer.toString(textSpanCounter * lineSpacing));
//                tspanElement.appendChild(textNode);
//                labelText.appendChild(tspanElement);
//                textSpanCounter += lineSpacing;
//            }
//            groupNode.appendChild(labelText);
////////////////////////////// end tspan method appears to fail in batik rendering process ////////////////////////////////////////////////

////////////////////////////// alternate method ////////////////////////////////////////////////
        ArrayList<String> labelList = new ArrayList<String>();
        if (graphPanel.dataStoreSvg.showIdLabels && currentNode.customIdentifier != null) {
            labelList.add(currentNode.customIdentifier);
        }
        if (graphPanel.dataStoreSvg.showLabels) {
            labelList.addAll(Arrays.asList(currentNode.getLabel()));
        }
        if (graphPanel.dataStoreSvg.showKinTypeLabels) {
            labelList.addAll(Arrays.asList(currentNode.getKinTypeStringArray()));
        }
        // this option has been hidden from the menu because it is not used here anymore, it has been replaced by the kin terms panel option to hide and show
//        if (graphPanel.dataStoreSvg.showKinTermLabels) {
//            labelList.addAll(Arrays.asList(currentNode.getKinTermStrings()));
//        }
        // todo: add the user specified id as a label
        // todo: this method has the draw back that the text is not selectable as a block
        int textSpanCounter = 0;
        if (currentNode.metadataRequiresSave) {
            textSpanCounter = addTextLabel(graphPanel, groupNode, "modified", "red", textSpanCounter);
        }
        for (String currentTextLable : labelList) {
            if (!currentTextLable.isEmpty()) {
                textSpanCounter = addTextLabel(graphPanel, groupNode, currentTextLable, "black", textSpanCounter);
            }
        }
        for (GraphLabel currentTextLable : currentNode.getKinTermStrings()) {
            textSpanCounter = addTextLabel(graphPanel, groupNode, currentTextLable.getLabelString(), currentTextLable.getColourString(), textSpanCounter);
        }
        if (graphPanel.dataStoreSvg.showDateLabels) {
//            try {
            String dateColur = "blue"; // todo: allow this colour to be set by the user
            // add the date of birth/death string
            String dateString = "";
            EntityDate dob = currentNode.getDateOfBirth();
            EntityDate dod = currentNode.getDateOfDeath();
            if (dob != null && !dob.getDateString().isEmpty()) {
                // todo: the date format should probably be user defined rather than assuming that the system prefs are correct
                dateString += dob.getDateString();
                if (!dob.dateIsValid()) {
                    dateColur = "red";
                }
            }
            if (dod != null && !dod.getDateString().isEmpty()) {
                dateString += " - " + dod.getDateString();
                if (!dod.dateIsValid()) {
                    dateColur = "red";
                }
            }
            if (dateString.length() > 0) {
                textSpanCounter = addTextLabel(graphPanel, groupNode, dateString, dateColur, textSpanCounter);
            }
//            } catch (EntityDateException dateException) {
//                textSpanCounter = addTextLabel(graphPanel, groupNode, dateException.getMessage(), "red", textSpanCounter);
//            }
            // end date of birth/death label
        }
        int linkCounter = 0;
        if (graphPanel.dataStoreSvg.showExternalLinks && currentNode.externalLinks != null) {
            // loop through the archive links and optionaly add href tags for each linked archive data <a xlink:href="http://www.mpi.nl/imdi-archive-link" target="_blank"></a>
            Element labelText = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "text");
            labelText.setAttribute("x", Double.toString(symbolSize * 1.5));
            labelText.setAttribute("y", Integer.toString(textSpanCounter));
            labelText.setAttribute("fill", "black");
            labelText.setAttribute("stroke-width", "0");
            labelText.setAttribute("font-size", "14");

            Text textNode = graphPanel.doc.createTextNode("archive ref: ");
            labelText.appendChild(textNode);
            for (ExternalLink linkURI : currentNode.externalLinks) {
                linkCounter++;
                Element labelTagA = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "a");
                String linkUrl;
                if (linkURI.getPidString() != null) {
                    linkUrl = "http://corpus1.mpi.nl/ds/imdi_browser/?openpath=" + linkURI.getPidString();
                } else {
                    linkUrl = linkURI.getLinkUri().toASCIIString();
                }
                labelTagA.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", linkUrl);
//11:19:24 AM Peter: http://corpus1.mpi.nl/ds/imdi_browser/viewcontroller?nodeid=MPI77915%23&action=ViewNode
//11:20:01 AM Guilherme Silva: http://corpus1.mpi.nl/ds/imdi_browser/?openpath=MPI77915%23
                labelTagA.setAttribute("target", "_blank");
                if (linkCounter == 1) {
                    labelTagA.appendChild(graphPanel.doc.createTextNode("" + linkCounter));
                } else {
                    labelTagA.appendChild(graphPanel.doc.createTextNode(", " + linkCounter));
                }
                labelText.appendChild(labelTagA);
            }
            groupNode.appendChild(labelText);
//            textSpanCounter += lineSpacing;
        }
////////////////////////////// end alternate method ////////////////////////////////////////////////
        ((EventTarget) groupNode).addEventListener("mousedown", graphPanel.mouseListenerSvg, false); // todo: use capture (currently false) could be useful for the mouse events
        return groupNode;
    }
}
