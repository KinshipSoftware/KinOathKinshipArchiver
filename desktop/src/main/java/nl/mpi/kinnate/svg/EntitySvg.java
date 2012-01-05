package nl.mpi.kinnate.svg;

import java.awt.geom.AffineTransform;
import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.kindata.GraphLabel;
import nl.mpi.kinnate.uniqueidentifiers.IdentifierException;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.events.EventTarget;

/**
 *  Document   : EntitySvg
 *  Created on : Mar 9, 2011, 3:20:56 PM
 *  Author     : Peter Withers
 */
public class EntitySvg {

    protected HashMap<UniqueIdentifier, float[]> entityPositions = new HashMap<UniqueIdentifier, float[]>();
    private HashMap<UniqueIdentifier, HashSet<UniqueIdentifier>> parentIdentifiers = new HashMap<UniqueIdentifier, HashSet<UniqueIdentifier>>();
    private int symbolSize = 15;
    static protected int strokeWidth = 2;
    private MessageDialogHandler dialogHandler;
    private BugCatcher bugCatcher;

    public EntitySvg(MessageDialogHandler dialogHandler, BugCatcher bugCatcher) {
        this.dialogHandler = dialogHandler;
        this.bugCatcher = bugCatcher;
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
                            entityPositions.put(entityId, new float[]{Float.parseFloat(stringPos[0]), Float.parseFloat(stringPos[1])});
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

    public int insertSymbols(SVGDocument doc, String svgNameSpace) {
        Element svgRoot = doc.getDocumentElement();
        Element defsNode = doc.createElementNS(svgNameSpace, "defs");
        defsNode.setAttribute("id", "KinSymbols");
        // add the circle symbol
        Element circleGroup = doc.createElementNS(svgNameSpace, "g");
        circleGroup.setAttribute("id", "circle");
        Element circleNode = doc.createElementNS(svgNameSpace, "circle");
        circleNode.setAttribute("cx", Integer.toString(symbolSize / 2));
        circleNode.setAttribute("cy", Integer.toString(symbolSize / 2));
        circleNode.setAttribute("r", Integer.toString((symbolSize - strokeWidth) / 2));
//        circleNode.setAttribute("height", Integer.toString(symbolSize - (strokeWidth * 3)));
        circleNode.setAttribute("stroke", "black");
        circleNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
        circleGroup.appendChild(circleNode);
        defsNode.appendChild(circleGroup);

        // add the square symbol
        Element squareGroup = doc.createElementNS(svgNameSpace, "g");
        squareGroup.setAttribute("id", "square");
        Element squareNode = doc.createElementNS(svgNameSpace, "rect");
        squareNode.setAttribute("x", Integer.toString(strokeWidth));
        squareNode.setAttribute("y", Integer.toString(strokeWidth));
        squareNode.setAttribute("width", Integer.toString(symbolSize - strokeWidth * 2));
        squareNode.setAttribute("height", Integer.toString(symbolSize - strokeWidth * 2));
        squareNode.setAttribute("stroke", "black");
        squareNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
        squareGroup.appendChild(squareNode);
        defsNode.appendChild(squareGroup);
        svgRoot.appendChild(defsNode);

        // add the square symbol
        Element squareGroup45 = doc.createElementNS(svgNameSpace, "g");
        squareGroup45.setAttribute("id", "square-45");
        Element squareNode45 = doc.createElementNS(svgNameSpace, "rect");
        squareNode45.setAttribute("transform", "rotate(-45 " + Integer.toString(symbolSize / 2) + " " + Integer.toString(symbolSize / 2) + ")");
        squareNode45.setAttribute("x", Integer.toString(strokeWidth));
        squareNode45.setAttribute("y", Integer.toString(strokeWidth));
        squareNode45.setAttribute("width", Integer.toString(symbolSize - strokeWidth * 2));
        squareNode45.setAttribute("height", Integer.toString(symbolSize - strokeWidth * 2));
        squareNode45.setAttribute("stroke", "black");
        squareNode45.setAttribute("stroke-width", Integer.toString(strokeWidth));
        squareGroup45.appendChild(squareNode45);
        defsNode.appendChild(squareGroup45);
        svgRoot.appendChild(defsNode);

        // add the rhombus symbol
        Element rhombusGroup = doc.createElementNS(svgNameSpace, "g");
        rhombusGroup.setAttribute("id", "rhombus");
        Element rhombusNode = doc.createElementNS(svgNameSpace, "rect");
        rhombusNode.setAttribute("transform", "scale(1,0.5), rotate(-45 " + Integer.toString(symbolSize / 2) + " " + Integer.toString(symbolSize / 2) + ")");
        rhombusNode.setAttribute("x", Integer.toString(strokeWidth));
        rhombusNode.setAttribute("y", Integer.toString(strokeWidth));
        rhombusNode.setAttribute("width", Integer.toString(symbolSize - strokeWidth * 2));
        rhombusNode.setAttribute("height", Integer.toString(symbolSize - strokeWidth * 2));
        rhombusNode.setAttribute("stroke", "black");
        rhombusNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
        rhombusGroup.appendChild(rhombusNode);
        defsNode.appendChild(rhombusGroup);
        svgRoot.appendChild(defsNode);

//        // add the rhombus symbol
//        Element rhombusGroup90 = doc.createElementNS(svgNameSpace, "g");
//        rhombusGroup90.setAttribute("id", "rhombus90");
//        Element rhombusNode90 = doc.createElementNS(svgNameSpace, "rect");
//        rhombusNode90.setAttribute("transform", "scale(1,0.5), rotate(45 " + Integer.toString(symbolSize / 2) + " " + Integer.toString(symbolSize / 2) + ")");
//        rhombusNode90.setAttribute("x", Integer.toString(strokeWidth));
//        rhombusNode90.setAttribute("y", Integer.toString(strokeWidth));
//        rhombusNode90.setAttribute("width", Integer.toString(symbolSize - strokeWidth * 2));
//        rhombusNode90.setAttribute("height", Integer.toString(symbolSize - strokeWidth * 2));
//        rhombusNode90.setAttribute("stroke", "black");
//        rhombusNode90.setAttribute("stroke-width", Integer.toString(strokeWidth));
//        rhombusGroup90.appendChild(rhombusNode90);
//        defsNode.appendChild(rhombusGroup90);
//        svgRoot.appendChild(defsNode);

        // add the union symbol
        Element unionGroup = doc.createElementNS(svgNameSpace, "g");
        unionGroup.setAttribute("id", "union");
        Element upperNode = doc.createElementNS(svgNameSpace, "line");
        Element lowerNode = doc.createElementNS(svgNameSpace, "line");
        upperNode.setAttribute("x1", Integer.toString(0));
        upperNode.setAttribute("y1", Integer.toString((symbolSize / 6)));
        upperNode.setAttribute("x2", Integer.toString(symbolSize));
        upperNode.setAttribute("y2", Integer.toString((symbolSize / 6)));
        upperNode.setAttribute("stroke-width", Integer.toString(symbolSize / 3));
        upperNode.setAttribute("stroke", "black");
        lowerNode.setAttribute("x1", Integer.toString(0));
        lowerNode.setAttribute("y1", Integer.toString(symbolSize - (symbolSize / 6)));
        lowerNode.setAttribute("x2", Integer.toString(symbolSize));
        lowerNode.setAttribute("y2", Integer.toString(symbolSize - (symbolSize / 6)));
        lowerNode.setAttribute("stroke-width", Integer.toString(symbolSize / 3));
        lowerNode.setAttribute("stroke", "black");
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
        defsNode.appendChild(unionGroup);
        svgRoot.appendChild(defsNode);

        // add the triangle symbol
        Element triangleGroup = doc.createElementNS(svgNameSpace, "g");
        triangleGroup.setAttribute("id", "triangle");
        Element triangleNode = doc.createElementNS(svgNameSpace, "polygon");
        int triangleSize = symbolSize - strokeWidth / 2;
        int triangleHeight = (int) (Math.sqrt(3) * triangleSize / 2);
        triangleNode.setAttribute("points", (symbolSize / 2) + "," + strokeWidth / 2 + " "
                + strokeWidth / 2 + "," + triangleHeight
                + " " + triangleSize + "," + triangleHeight);
        triangleNode.setAttribute("stroke", "black");
        triangleNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
        triangleGroup.appendChild(triangleNode);
        defsNode.appendChild(triangleGroup);
        svgRoot.appendChild(defsNode);

        // add the triangle symbol
        Element triangleGroup1 = doc.createElementNS(svgNameSpace, "g");
        triangleGroup1.setAttribute("id", "triangle-270");
        Element triangleNode1 = doc.createElementNS(svgNameSpace, "polygon");
        triangleNode1.setAttribute("transform", "rotate(-90 " + Integer.toString(symbolSize / 2) + " " + Integer.toString(symbolSize / 2) + ")");
        triangleNode1.setAttribute("points", (symbolSize / 2) + "," + strokeWidth / 2 + " "
                + strokeWidth / 2 + "," + triangleHeight
                + " " + triangleSize + "," + triangleHeight);
        triangleNode1.setAttribute("stroke", "black");
        triangleNode1.setAttribute("stroke-width", Integer.toString(strokeWidth));
        triangleGroup1.appendChild(triangleNode1);
        defsNode.appendChild(triangleGroup1);
        svgRoot.appendChild(defsNode);

        // add the triangle symbol
        Element triangleGroup2 = doc.createElementNS(svgNameSpace, "g");
        triangleGroup2.setAttribute("id", "triangle-180");
        triangleGroup2.setAttribute("transform", "rotate(180 " + Integer.toString(symbolSize / 2) + " " + Integer.toString(symbolSize / 2) + ")");
        Element triangleNode2 = doc.createElementNS(svgNameSpace, "polygon");
        triangleNode2.setAttribute("points", (symbolSize / 2) + "," + strokeWidth / 2 + " "
                + strokeWidth / 2 + "," + triangleHeight
                + " " + triangleSize + "," + triangleHeight);
        triangleNode2.setAttribute("stroke", "black");
        triangleNode2.setAttribute("stroke-width", Integer.toString(strokeWidth));
        triangleGroup2.appendChild(triangleNode2);
        defsNode.appendChild(triangleGroup2);
        svgRoot.appendChild(defsNode);

        // add the triangle symbol
        Element triangleGroup3 = doc.createElementNS(svgNameSpace, "g");
        triangleGroup3.setAttribute("id", "triangle-90");
        triangleGroup3.setAttribute("transform", "rotate(90 " + Integer.toString(symbolSize / 2) + " " + Integer.toString(symbolSize / 2) + ")");
        Element triangleNode3 = doc.createElementNS(svgNameSpace, "polygon");
        triangleNode3.setAttribute("points", (symbolSize / 2) + "," + strokeWidth / 2 + " "
                + strokeWidth / 2 + "," + triangleHeight
                + " " + triangleSize + "," + triangleHeight);
        triangleNode3.setAttribute("stroke", "black");
        triangleNode3.setAttribute("stroke-width", Integer.toString(strokeWidth));
        triangleGroup3.appendChild(triangleNode3);
        defsNode.appendChild(triangleGroup3);
        svgRoot.appendChild(defsNode);

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
//        svgRoot.appendChild(defsNode);

        // add the cross symbol
        Element crossGroup = doc.createElementNS(svgNameSpace, "g");
        crossGroup.setAttribute("id", "cross");
        Element crossNode = doc.createElementNS(svgNameSpace, "polyline");
        int posX = symbolSize / 2;
        int posY = symbolSize / 2;
        int offsetAmount = symbolSize / 2;
        crossNode.setAttribute("points", (posX - offsetAmount) + "," + (posY - offsetAmount) + " " + (posX + offsetAmount) + "," + (posY + offsetAmount) + " " + (posX) + "," + (posY) + " " + (posX - offsetAmount) + "," + (posY + offsetAmount) + " " + (posX + offsetAmount) + "," + (posY - offsetAmount));
        crossNode.setAttribute("fill", "none");
        crossNode.setAttribute("stroke", "black");
        crossNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
        crossGroup.appendChild(crossNode);
        defsNode.appendChild(crossGroup);
        svgRoot.appendChild(defsNode);

        // add the error symbol
        Element noneGroup = doc.createElementNS(svgNameSpace, "g");
        noneGroup.setAttribute("id", "error");
        Element noneNode = doc.createElementNS(svgNameSpace, "polyline");
        int posXnone = symbolSize / 2;
        int posYnone = symbolSize / 2;
        int offsetNoneAmount = symbolSize / 2;
        noneNode.setAttribute("points", (posXnone - offsetNoneAmount) + "," + (posYnone - offsetNoneAmount) + " " + (posXnone + offsetNoneAmount) + "," + (posYnone + offsetNoneAmount) + " " + (posXnone) + "," + (posYnone) + " " + (posXnone - offsetNoneAmount) + "," + (posYnone + offsetNoneAmount) + " " + (posXnone + offsetNoneAmount) + "," + (posYnone - offsetNoneAmount));
        noneNode.setAttribute("fill", "none");
        noneNode.setAttribute("stroke", "red");
        noneNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
        noneGroup.appendChild(noneNode);
        defsNode.appendChild(noneGroup);
        svgRoot.appendChild(defsNode);
        return symbolSize;
    }

    public String[] listSymbolNames(SVGDocument doc, String svgNameSpace) {
        // get the symbol list from the dom
        ArrayList<String> symbolArray = new ArrayList<String>();
        Element kinSymbols = doc.getElementById("KinSymbols");
        if (kinSymbols == null) {
            insertSymbols(doc, svgNameSpace);
            kinSymbols = doc.getElementById("KinSymbols");
        }
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
        for (Entry<UniqueIdentifier, float[]> currentEntry : entityPositions.entrySet()) {
            if (!excludeIdentifiers.contains(currentEntry.getKey())) {
                float hDistance = locationArray[0] - currentEntry.getValue()[0];
                float vDistance = locationArray[1] - currentEntry.getValue()[1];
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
//        System.out.println("closestDistance: " + closestDistance);
        if (maximumDistance < closestDistance) {
            return null;
        }
        return closestIdentifier;
    }

    public float[] getEntityLocation(UniqueIdentifier entityId) {
        float[] returnLoc = entityPositions.get(entityId);
        float xPos = returnLoc[0] + (symbolSize / 2);
        float yPos = returnLoc[1] + (symbolSize / 2);
        return new float[]{xPos, yPos};
    }

    public float[] getAverageParentLocation(UniqueIdentifier entityId) {
        // note that getAverageParentLocation(EntityData entityData) must be called at least once for each entity to poputlate parentIdentifiers
        Float maxY = null;
        float averageX = 0;
        int parentCount = 0;
        final HashSet<UniqueIdentifier> parentIdSet = parentIdentifiers.get(entityId);
        if (parentIdSet != null) {
            for (UniqueIdentifier parentIdentifier : parentIdSet) {
                float[] parentLoc = getEntityLocation(parentIdentifier);
                if (maxY == null) {
                    maxY = parentLoc[1];
                } else {
                    maxY = (maxY >= parentLoc[1]) ? maxY : parentLoc[1];
                }
                averageX += parentLoc[0];
                parentCount++;
            }
        }
        averageX = averageX / parentCount;
        if (maxY == null) {
            return null;
        } else {
            return new float[]{averageX, maxY};
        }
    }

    public float[] getAverageParentLocation(EntityData entityData) {
        HashSet<UniqueIdentifier> identifierSet = new HashSet<UniqueIdentifier>();
        for (EntityRelation entityRelation : entityData.getAllRelations()) {
            if (entityRelation.relationType == RelationType.ancestor) {
                identifierSet.add(entityRelation.alterUniqueIdentifier);
            }
        }
        parentIdentifiers.put(entityData.getUniqueIdentifier(), identifierSet);
        return getAverageParentLocation(entityData.getUniqueIdentifier());
    }

//    public float[] getEntityLocation(SVGDocument doc, String entityId) {
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
    public float[] moveEntity(GraphPanel graphPanel, UniqueIdentifier entityId, float shiftXfloat, float shiftYfloat, boolean snapToGrid, boolean allRealtionsSelected) {
        Element entitySymbol = graphPanel.doc.getElementById(entityId.getAttributeIdentifier());
        Element highlightGroup = null;
        if (entityId.isGraphicsIdentifier()) {
            highlightGroup = graphPanel.doc.getElementById("highlight_" + entityId.getAttributeIdentifier());
        }
        float remainderAfterSnapX = 0;
        float remainderAfterSnapY = 0;
        double scaleFactor = 1;
        double shiftXscaled;
        double shiftYscaled;
        if (entitySymbol != null) {
            boolean allowYshift = entitySymbol.getLocalName().equals("text");
            if (allRealtionsSelected) {
                // if all the visible relations are selected then allow y shift
                allowYshift = true;
            }
            // todo: Ticket #1064 when the zig zag lines are done the y shift can be allowed
            allowYshift = true;
            AffineTransform affineTransform = graphPanel.svgCanvas.getRenderingTransform();
            scaleFactor = affineTransform.getScaleX(); // the drawing should be proportional so only using X is adequate here
            shiftXscaled = shiftXfloat / scaleFactor;
            shiftYscaled = shiftYfloat / scaleFactor;
////            sVGMatrix.setE(sVGMatrix.getE() + shiftX);
////            sVGMatrix.setE(sVGMatrix.getF() + shiftY);
////            System.out.println("shiftX: " + shiftX);
//            float updatedPositionX = sVGMatrix.getE() + shiftX;
//            float updatedPositionY = sVGMatrix.getF();

            float[] entityPosition = entityPositions.get(entityId);
            float updatedPositionX = (float) (entityPosition[0] + shiftXscaled);
            float updatedPositionY = entityPosition[1];

            if (allowYshift) {
                updatedPositionY = (float) (updatedPositionY + shiftYscaled);
            }
//            System.out.println("updatedPosition: " + updatedPositionX + " : " + updatedPositionY + " : " + shiftX + " : " + shiftY);
            if (snapToGrid) {
                double updatedSnapPositionX = Math.round(updatedPositionX / 50) * 50; // limit movement to the grid
                updatedPositionX = (float) updatedSnapPositionX;
                if (allowYshift) {
                    float updatedSnapPositionY = Math.round(updatedPositionY / 50) * 50; // limit movement to the grid                    
                    updatedPositionY = updatedSnapPositionY;
                }
            } else {
                updatedPositionX = (int) updatedPositionX;  // prevent uncorrectable but visible variations in the position of entities to each other
                if (allowYshift) {
                    updatedPositionY = (int) updatedPositionY;
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
            entityPositions.put(entityId, new float[]{updatedPositionX, updatedPositionY});
            final String translateString = "translate(" + String.valueOf(updatedPositionX) + ", " + String.valueOf(updatedPositionY) + ")";
            ((Element) entitySymbol).setAttribute("transform", translateString);
            if (highlightGroup != null) {
                highlightGroup.setAttribute("transform", translateString);
            }
            float distanceXmoved = ((float) ((updatedPositionX - entityPosition[0]) * scaleFactor));
            float distanceYmoved = ((float) ((updatedPositionY - entityPosition[1]) * scaleFactor));
            remainderAfterSnapX = shiftXfloat - distanceXmoved;
            remainderAfterSnapY = shiftYfloat - distanceYmoved;
//            ((Element) entitySymbol).setAttribute("transform", "translate(" + String.valueOf(sVGMatrix.getE() + shiftX) + ", " + (sVGMatrix.getF() + shiftY) + ")");
        }
//        System.out.println("remainderAfterSnap: " + remainderAfterSnap);
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
        groupNode.setAttributeNS(DataStoreSvg.kinDataNameSpaceLocation, "kin:path", currentNode.getEntityPath());
        // the kin type strings are stored here so that on selection in the graph the add kin term panel can be pre populatedwith the kin type strings of the selection
        groupNode.setAttributeNS(DataStoreSvg.kinDataNameSpaceLocation, "kin:kintype", currentNode.getKinTypeString());
//        counterTest++;
        Element symbolNode;
        String symbolType = currentNode.getSymbolType();
        if (symbolType == null || symbolType.length() == 0) {
            symbolType = "error";
        }
        // todo: check that if an entity is already placed in which case do not recreate
        // todo: do not create a new dom each time but reuse it instead, or due to the need to keep things up to date maybe just store an array of entity locations instead
        symbolNode = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "use");
        symbolNode.setAttribute("id", currentNode.getUniqueIdentifier().getAttributeIdentifier() + ":symbol");
        symbolNode.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "#" + symbolType); // the xlink: of "xlink:href" is required for some svg viewers to render correctly
        float[] storedPosition = entityPositions.get(currentNode.getUniqueIdentifier());
        if (storedPosition == null) {
            bugCatcher.logError(new Exception("No storedPosition found for: " + currentNode.getUniqueIdentifier().getAttributeIdentifier()));
            storedPosition = new float[]{0, 0};
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
        // todo: resolve the null pointer on first run with transient nodes (last test on this did not get a null pointer so maybe it is resolved)
        groupNode.setAttribute("transform", "translate(" + Float.toString(storedPosition[0]) + ", " + Float.toString(storedPosition[1]) + ")");
        if (currentNode.isEgo) {
            symbolNode.setAttribute("fill", "black");
        } else {
            symbolNode.setAttribute("fill", "white");
        }

        symbolNode.setAttribute("stroke", "black");
        symbolNode.setAttribute("stroke-width", "2");
        groupNode.appendChild(symbolNode);

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
            textSpanCounter = addTextLabel(graphPanel, groupNode, currentTextLable, "black", textSpanCounter);
        }
        for (GraphLabel currentTextLable : currentNode.getKinTermStrings()) {
            textSpanCounter = addTextLabel(graphPanel, groupNode, currentTextLable.getLabelString(), currentTextLable.getColourString(), textSpanCounter);
        }
        if (graphPanel.dataStoreSvg.showDateLabels) {
            // add the date of birth/death string
            String dateString = "";
            Date dob = currentNode.getDateOfBirth();
            Date dod = currentNode.getDateOfDeath();
            if (dob != null) {
                // todo: the date format should probably be user defined rather than assuming that the system prefs are correct
                dateString += DateFormat.getDateInstance().format(dob);
            }
            if (dod != null) {
                dateString += " - " + DateFormat.getDateInstance().format(dod);
            }
            if (dateString.length() > 0) {
                textSpanCounter = addTextLabel(graphPanel, groupNode, dateString, "black", textSpanCounter);
            }
            // end date of birth/death label
        }
        int linkCounter = 0;
        if (graphPanel.dataStoreSvg.showArchiveLinks && currentNode.archiveLinkArray != null) {
            // loop through the archive links and optionaly add href tags for each linked archive data <a xlink:href="http://www.mpi.nl/imdi-archive-link" target="_blank"></a>
            Element labelText = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "text");
            labelText.setAttribute("x", Double.toString(symbolSize * 1.5));
            labelText.setAttribute("y", Integer.toString(textSpanCounter));
            labelText.setAttribute("fill", "black");
            labelText.setAttribute("stroke-width", "0");
            labelText.setAttribute("font-size", "14");

            Text textNode = graphPanel.doc.createTextNode("archive ref: ");
            labelText.appendChild(textNode);
            for (URI linkURI : currentNode.archiveLinkArray) {
                linkCounter++;
                Element labelTagA = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "a");

                labelTagA.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", linkURI.toASCIIString());
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
