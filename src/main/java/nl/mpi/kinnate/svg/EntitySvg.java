package nl.mpi.kinnate.svg;

import java.util.ArrayList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGRect;

/**
 *  Document   : EntitySvg
 *  Created on : Mar 9, 2011, 3:20:56 PM
 *  Author     : Peter Withers
 */
public class EntitySvg {

    public void insertSymbols(SVGDocument doc, String svgNameSpace) {
        int symbolSize = 15;
        int strokeWidth = 2;
        Element svgRoot = doc.getDocumentElement();
        Element defsNode = doc.createElementNS(svgNameSpace, "defs");
        defsNode.setAttribute("id", "KinSymbols");
        // add the circle symbol
        Element circleGroup = doc.createElementNS(svgNameSpace, "g");
        circleGroup.setAttribute("id", "circle");
        Element circleNode = doc.createElementNS(svgNameSpace, "circle");
        circleNode.setAttribute("cx", Integer.toString(symbolSize / 2));
        circleNode.setAttribute("cy", Integer.toString(symbolSize / 2));
        circleNode.setAttribute("r", Integer.toString(symbolSize / 2));
        circleNode.setAttribute("height", Integer.toString(symbolSize));
        circleNode.setAttribute("stroke", "black");
        circleNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
        circleGroup.appendChild(circleNode);
        defsNode.appendChild(circleGroup);

        // add the square symbol
        Element squareGroup = doc.createElementNS(svgNameSpace, "g");
        squareGroup.setAttribute("id", "square");
        Element squareNode = doc.createElementNS(svgNameSpace, "rect");
        squareNode.setAttribute("x", "0");
        squareNode.setAttribute("y", "0");
        squareNode.setAttribute("width", Integer.toString(symbolSize));
        squareNode.setAttribute("height", Integer.toString(symbolSize));
        squareNode.setAttribute("stroke", "black");
        squareNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
        squareGroup.appendChild(squareNode);
        defsNode.appendChild(squareGroup);
        svgRoot.appendChild(defsNode);

        // add the resource symbol
        Element resourceGroup = doc.createElementNS(svgNameSpace, "g");
        resourceGroup.setAttribute("id", "square45");
        Element resourceNode = doc.createElementNS(svgNameSpace, "rect");
        resourceNode.setAttribute("x", "0");
        resourceNode.setAttribute("y", "0");
        resourceNode.setAttribute("transform", "rotate(-45 " + Integer.toString(symbolSize / 2) + " " + Integer.toString(symbolSize / 2) + ")");
        resourceNode.setAttribute("width", Integer.toString(symbolSize));
        resourceNode.setAttribute("height", Integer.toString(symbolSize));
        resourceNode.setAttribute("stroke", "black");
        resourceNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
        resourceGroup.appendChild(resourceNode);
        defsNode.appendChild(resourceGroup);
        svgRoot.appendChild(defsNode);

        // add the rhombus symbol
        Element rhombusGroup = doc.createElementNS(svgNameSpace, "g");
        rhombusGroup.setAttribute("id", "rhombus");
        Element rhombusNode = doc.createElementNS(svgNameSpace, "rect");
        rhombusNode.setAttribute("x", "0");
        rhombusNode.setAttribute("y", "0");
        rhombusNode.setAttribute("transform", "scale(1,2), rotate(-45 " + Integer.toString(symbolSize / 2) + " " + Integer.toString(symbolSize / 2) + ")");
        rhombusNode.setAttribute("width", Integer.toString(symbolSize));
        rhombusNode.setAttribute("height", Integer.toString(symbolSize));
        rhombusNode.setAttribute("stroke", "black");
        rhombusNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
        rhombusGroup.appendChild(rhombusNode);
        defsNode.appendChild(rhombusGroup);
        svgRoot.appendChild(defsNode);

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
        int triangleHeight = (int) (Math.sqrt(3) * symbolSize / 2);
        triangleNode.setAttribute("points", (symbolSize / 2) + "," + 0 + " "
                + 0 + "," + triangleHeight
                + " " + symbolSize + "," + triangleHeight);
        triangleNode.setAttribute("stroke", "black");
        triangleNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
        triangleGroup.appendChild(triangleNode);
        defsNode.appendChild(triangleGroup);
        svgRoot.appendChild(defsNode);

        // add the equals symbol
        Element equalsGroup = doc.createElementNS(svgNameSpace, "g");
        equalsGroup.setAttribute("id", "equals");
        Element equalsNode = doc.createElementNS(svgNameSpace, "polyline");
        int offsetAmounta = symbolSize / 2;
        int posXa = 0;
        int posYa = +symbolSize / 2;
        equalsNode.setAttribute("points", (posXa + offsetAmounta * 3) + "," + (posYa + offsetAmounta) + " " + (posXa - offsetAmounta) + "," + (posYa + offsetAmounta) + " " + (posXa - offsetAmounta) + "," + (posYa - offsetAmounta) + " " + (posXa + offsetAmounta * 3) + "," + (posYa - offsetAmounta));
        equalsNode.setAttribute("fill", "white");
        equalsNode.setAttribute("stroke", "black");
        equalsNode.setAttribute("stroke-width", Integer.toString(strokeWidth));
        equalsGroup.appendChild(equalsNode);
        defsNode.appendChild(equalsGroup);
        svgRoot.appendChild(defsNode);

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
    }

    public String[] listSymbolNames(SVGDocument doc) {
        // get the symbol list from the dom
        ArrayList<String> symbolArray = new ArrayList<String>();
        Element kinSymbols = doc.getElementById("KinSymbols");
        if (kinSymbols != null) {
            for (Node kinSymbolNode = kinSymbols.getFirstChild(); kinSymbolNode != null; kinSymbolNode = kinSymbolNode.getNextSibling()) {
                symbolArray.add(kinSymbolNode.getAttributes().getNamedItem("id").getNodeValue());
            }
        }
        return symbolArray.toArray(new String[]{});
    }

    public float[] getEntityLocation(SVGDocument doc, String entityId) {
        Element entitySymbol = doc.getElementById(entityId + "symbol");
//        Element entitySymbol = doc.getElementById(entityId); // the sybol group node
        if (entitySymbol != null) {
            SVGRect bbox = ((SVGLocatable) entitySymbol).getBBox();
            SVGMatrix sVGMatrix = ((SVGLocatable) entitySymbol).getCTM();
//            System.out.println("getA: " + sVGMatrix.getA());
//            System.out.println("getB: " + sVGMatrix.getB());
//            System.out.println("getC: " + sVGMatrix.getC());
//            System.out.println("getD: " + sVGMatrix.getD());
//            System.out.println("getE: " + sVGMatrix.getE());
//            System.out.println("getF: " + sVGMatrix.getF());

//            System.out.println("bbox X: " + bbox.getX());
//            System.out.println("bbox Y: " + bbox.getY());
//            System.out.println("bbox W: " + bbox.getWidth());
//            System.out.println("bbox H: " + bbox.getHeight());
            return new float[]{sVGMatrix.getE() + bbox.getWidth() / 2, sVGMatrix.getF() + bbox.getHeight() / 2};
//            bbox.setX(sVGMatrix.getE());
//            bbox.setY(sVGMatrix.getF());
//            return bbox;
        } else {
            return null;
        }
    }

    public void moveEntity(SVGDocument doc, String entityId, int shiftX, int shiftY) {
        Element entitySymbol = doc.getElementById(entityId);
        if (entitySymbol != null) {
            SVGMatrix sVGMatrix = ((SVGLocatable) entitySymbol).getCTM();
//            sVGMatrix.setE(sVGMatrix.getE() + shiftX);
//            sVGMatrix.setE(sVGMatrix.getF() + shiftY);
            ((Element) entitySymbol).setAttribute("transform", "translate(" + String.valueOf(sVGMatrix.getE() + shiftX) + ", " + sVGMatrix.getF() + ")");
//            ((Element) entitySymbol).setAttribute("transform", "translate(" + String.valueOf(sVGMatrix.getE() + shiftX) + ", " + (sVGMatrix.getF() + shiftY) + ")");
        }
    }
}
