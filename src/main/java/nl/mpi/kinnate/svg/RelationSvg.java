package nl.mpi.kinnate.svg;

import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.svg.SVGDocument;

/**
 *  Document   : RelationSvg
 *  Created on : Mar 9, 2011, 3:21:16 PM
 *  Author     : Peter Withers
 */
public class RelationSvg {

    private void addUseNode(SVGDocument doc, String svgNameSpace, Element targetGroup, String targetDefId) {
        String useNodeId = targetDefId + "use";
        Node useNodeOld = doc.getElementById(useNodeId);
        if (useNodeOld != null) {
            useNodeOld.getParentNode().removeChild(useNodeOld);
        }
        Element useNode = doc.createElementNS(svgNameSpace, "use");
        useNode.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "#" + targetDefId); // the xlink: of "xlink:href" is required for some svg viewers to render correctly
        //                    useNode.setAttribute("href", "#" + lineIdString);
        useNode.setAttribute("id", useNodeId);
        targetGroup.appendChild(useNode);
    }

    private void updateLabelNode(SVGDocument doc, String svgNameSpace, String lineIdString, String targetRelationId) {
        // remove and readd the text on path label so that it updates with the new path
        String labelNodeId = targetRelationId + "label";
        Node useNodeOld = doc.getElementById(labelNodeId);
        if (useNodeOld != null) {
            Node textParentNode = useNodeOld.getParentNode();
            String labelText = useNodeOld.getTextContent();
            useNodeOld.getParentNode().removeChild(useNodeOld);

            Element textPath = doc.createElementNS(svgNameSpace, "textPath");
            textPath.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "#" + lineIdString); // the xlink: of "xlink:href" is required for some svg viewers to render correctly
            textPath.setAttribute("startOffset", "50%");
            textPath.setAttribute("id", labelNodeId);
            Text textNode = doc.createTextNode(labelText);
            textPath.appendChild(textNode);
            textParentNode.appendChild(textPath);
        }
    }

    private void setPolylinePointsAttribute(Element targetNode, GraphDataNode.RelationType relationType, float vSpacing, float egoX, float egoY, float alterX, float alterY) {
        float midY = (egoY + alterY) / 2;
        if (alterY == egoY) {
            // make sure that union lines go below the entities and sibling lines go above
            if (relationType == GraphDataNode.RelationType.sibling) {
                midY = alterY - vSpacing / 2;
            } else if (relationType == GraphDataNode.RelationType.union) {
                midY = alterY + vSpacing / 2;
            }
        }
        targetNode.setAttribute("points",
                egoX + "," + egoY + " "
                + egoX + "," + midY + " "
                + alterX + "," + midY + " "
                + alterX + "," + alterY);
    }

    private void setPathPointsAttribute(Element targetNode, GraphDataNode.RelationType relationType, GraphDataNode.RelationLineType relationLineType, float hSpacing, float vSpacing, float egoX, float egoY, float alterX, float alterY) {
        float fromBezX;
        float fromBezY;
        float toBezX;
        float toBezY;
        if (relationLineType == GraphDataNode.RelationLineType.verticalCurve) {
            fromBezX = egoX;
            fromBezY = alterY;
            toBezX = alterX;
            toBezY = egoY;
            if (egoY == alterY) {
                fromBezX = egoX;
                fromBezY = alterY - vSpacing / 2;
                toBezX = alterX;
                toBezY = egoY - vSpacing / 2;
            }
        } else {
            fromBezX = alterX;
            fromBezY = egoY;
            toBezX = egoX;
            toBezY = alterY;
            // todo: if the nodes are almost in align then this test fails and it should insted check for proximity not equality
            if (egoX == alterX) {
                fromBezY = egoY;
                fromBezX = alterX - hSpacing / 2;
                toBezY = alterY;
                toBezX = egoX - hSpacing / 2;
            }
        }
        targetNode.setAttribute("d", "M " + egoX + "," + egoY + " C " + fromBezX + "," + fromBezY + " " + toBezX + "," + toBezY + " " + alterX + "," + alterY);
    }

    protected void insertRelation(SVGDocument doc, String svgNameSpace, Element relationGroupNode, GraphDataNode currentNode, GraphDataNode.NodeRelation graphLinkNode, int hSpacing, int vSpacing, int strokeWidth) {
        int relationLineIndex = relationGroupNode.getChildNodes().getLength();
        Element groupNode = doc.createElementNS(svgNameSpace, "g");
        groupNode.setAttribute("id", "relation" + relationLineIndex);
        Element defsNode = doc.createElementNS(svgNameSpace, "defs");
        String lineIdString = "relation" + relationLineIndex + "Line";
        new DataStoreSvg().storeRelationParameters(doc, groupNode, graphLinkNode.relationType, graphLinkNode.relationLineType, currentNode.getEntityPath(), graphLinkNode.getAlterNode().getEntityPath());
        // set the line end points
        int fromX = (currentNode.xPos * hSpacing + hSpacing);
        int fromY = (currentNode.yPos * vSpacing + vSpacing);
        int toX = (graphLinkNode.getAlterNode().xPos * hSpacing + hSpacing);
        int toY = (graphLinkNode.getAlterNode().yPos * vSpacing + vSpacing);

        switch (graphLinkNode.relationLineType) {
            case horizontalCurve:
            // this case uses the following case
            case verticalCurve:
                // todo: groupNode.setAttribute("id", );
                //                    System.out.println("link: " + graphLinkNode.linkedNode.xPos + ":" + graphLinkNode.linkedNode.yPos);
                //
                ////                <line id="_15" transform="translate(146.0,112.0)" x1="0" y1="0" x2="100" y2="100" ="black" stroke-width="1"/>
                //                    Element linkLine = doc.createElementNS(svgNS, "line");
                //                    linkLine.setAttribute("x1", Integer.toString(currentNode.xPos * hSpacing + hSpacing));
                //                    linkLine.setAttribute("y1", Integer.toString(currentNode.yPos * vSpacing + vSpacing));
                //
                //                    linkLine.setAttribute("x2", Integer.toString(graphLinkNode.linkedNode.xPos * hSpacing + hSpacing));
                //                    linkLine.setAttribute("y2", Integer.toString(graphLinkNode.linkedNode.yPos * vSpacing + vSpacing));
                //                    linkLine.setAttribute("stroke", "black");
                //                    linkLine.setAttribute("stroke-width", "1");
                //                    // Attach the rectangle to the root 'svg' element.
                //                    svgRoot.appendChild(linkLine);
                //System.out.println("link: " + graphLinkNode.getAlterNode().xPos + ":" + graphLinkNode.getAlterNode().yPos);

                //                <line id="_15" transform="translate(146.0,112.0)" x1="0" y1="0" x2="100" y2="100" ="black" stroke-width="1"/>
                Element linkLine = doc.createElementNS(svgNameSpace, "path");

                setPathPointsAttribute(linkLine, graphLinkNode.relationType, graphLinkNode.relationLineType, hSpacing, vSpacing, fromX, fromY, toX, toY);
                //                    linkLine.setAttribute("x1", );
                //                    linkLine.setAttribute("y1", );
                //
                //                    linkLine.setAttribute("x2", );
                linkLine.setAttribute("fill", "none");
                linkLine.setAttribute("stroke", "blue");
                linkLine.setAttribute("stroke-width", Integer.toString(strokeWidth));
                linkLine.setAttribute("id", lineIdString);
                defsNode.appendChild(linkLine);
                break;
            case square:
                //                            Element squareLinkLine = doc.createElement("line");
                //                            squareLinkLine.setAttribute("x1", Integer.toString(currentNode.xPos * hSpacing + hSpacing));
                //                            squareLinkLine.setAttribute("y1", Integer.toString(currentNode.yPos * vSpacing + vSpacing));
                //
                //                            squareLinkLine.setAttribute("x2", Integer.toString(graphLinkNode.linkedNode.xPos * hSpacing + hSpacing));
                //                            squareLinkLine.setAttribute("y2", Integer.toString(graphLinkNode.linkedNode.yPos * vSpacing + vSpacing));
                //                            squareLinkLine.setAttribute("stroke", "grey");
                //                            squareLinkLine.setAttribute("stroke-width", Integer.toString(strokeWidth));
                Element squareLinkLine = doc.createElementNS(svgNameSpace, "polyline");

                setPolylinePointsAttribute(squareLinkLine, graphLinkNode.relationType, vSpacing, fromX, fromY, toX, toY);

                squareLinkLine.setAttribute("fill", "none");
                squareLinkLine.setAttribute("stroke", "grey");
                squareLinkLine.setAttribute("stroke-width", Integer.toString(strokeWidth));
                squareLinkLine.setAttribute("id", lineIdString);
                defsNode.appendChild(squareLinkLine);
                break;
        }
        groupNode.appendChild(defsNode);

        // insert the node that uses the above definition
        addUseNode(doc, svgNameSpace, groupNode, lineIdString);

        // add the relation label
        if (graphLinkNode.labelString != null) {
            Element labelText = doc.createElementNS(svgNameSpace, "text");
            labelText.setAttribute("text-anchor", "middle");
            //                        labelText.setAttribute("x", Integer.toString(labelX));
            //                        labelText.setAttribute("y", Integer.toString(labelY));
            labelText.setAttribute("fill", "blue");
            labelText.setAttribute("stroke-width", "0");
            labelText.setAttribute("font-size", "14");
            //                        labelText.setAttribute("transform", "rotate(45)");
            Element textPath = doc.createElementNS(svgNameSpace, "textPath");
            textPath.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "#" + lineIdString); // the xlink: of "xlink:href" is required for some svg viewers to render correctly
            textPath.setAttribute("startOffset", "50%");
            textPath.setAttribute("id", "relation" + relationLineIndex + "label");
            Text textNode = doc.createTextNode(graphLinkNode.labelString);
            textPath.appendChild(textNode);
            labelText.appendChild(textPath);
            groupNode.appendChild(labelText);
        }
        relationGroupNode.appendChild(groupNode);
    }

    public void updateRelationLines(SVGDocument doc, ArrayList<String> draggedNodeIds, String svgNameSpace, int hSpacing, int vSpacing) {
        // todo: if an entity is above its ancestor then this must be corrected, if the ancestor data is stored in the relationLine attributes then this would be a good place to correct this
        Element relationGroup = doc.getElementById("RelationGroup");
        for (Node currentChild = relationGroup.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
            if ("g".equals(currentChild.getLocalName())) {
                Node idAttrubite = currentChild.getAttributes().getNamedItem("id");
                System.out.println("idAttrubite: " + idAttrubite.getNodeValue());
                DataStoreSvg.GraphRelationData graphRelationData = new DataStoreSvg().getEntitiesForRelations(currentChild);
                if (graphRelationData != null) {
                    if (draggedNodeIds.contains(graphRelationData.egoNodeId) || draggedNodeIds.contains(graphRelationData.alterNodeId)) {
                        // todo: update the relation lines
                        System.out.println("needs update on: " + idAttrubite.getNodeValue());
                        String lineElementId = idAttrubite.getNodeValue() + "Line";
                        Element relationLineElement = doc.getElementById(lineElementId);
                        System.out.println("type: " + relationLineElement.getLocalName());

                        float[] egoSymbolPoint = new EntitySvg().getEntityLocation(doc, graphRelationData.egoNodeId);
                        float[] alterSymbolPoint = new EntitySvg().getEntityLocation(doc, graphRelationData.alterNodeId);

                        float egoX = egoSymbolPoint[0];
                        float egoY = egoSymbolPoint[1];
                        float alterX = alterSymbolPoint[0];
                        float alterY = alterSymbolPoint[1];

//                        SVGRect egoSymbolRect = new EntitySvg().getEntityLocation(doc, graphRelationData.egoNodeId);
//                        SVGRect alterSymbolRect = new EntitySvg().getEntityLocation(doc, graphRelationData.alterNodeId);
//
//                        float egoX = egoSymbolRect.getX() + egoSymbolRect.getWidth() / 2;
//                        float egoY = egoSymbolRect.getY() + egoSymbolRect.getHeight() / 2;
//                        float alterX = alterSymbolRect.getX() + alterSymbolRect.getWidth() / 2;
//                        float alterY = alterSymbolRect.getY() + alterSymbolRect.getHeight() / 2;

                        if ("polyline".equals(relationLineElement.getLocalName())) {
                            System.out.println("polyline to update: " + lineElementId);
                            setPolylinePointsAttribute(relationLineElement, graphRelationData.relationType, vSpacing, egoX, egoY, alterX, alterY);
                        }
                        if ("path".equals(relationLineElement.getLocalName())) {
                            System.out.println("path to update: " + relationLineElement.getLocalName());
                            setPathPointsAttribute(relationLineElement, graphRelationData.relationType, graphRelationData.relationLineType, hSpacing, vSpacing, egoX, egoY, alterX, alterY);
                        }
                        addUseNode(doc, svgNameSpace, (Element) currentChild, lineElementId);
                        updateLabelNode(doc, svgNameSpace, lineElementId, idAttrubite.getNodeValue());
                    }
                }
            }
        }
    }
//                            new RelationSvg().addTestNode(doc, (Element) relationLineElement.getParentNode().getParentNode(), svgNameSpace);
//    public void addTestNode(SVGDocument doc, Element addTarget, String svgNameSpace) {
//        Element squareNode = doc.createElementNS(svgNameSpace, "rect");
//        squareNode.setAttribute("x", "100");
//        squareNode.setAttribute("y", "100");
//        squareNode.setAttribute("width", "20");
//        squareNode.setAttribute("height", "20");
//        squareNode.setAttribute("fill", "green");
//        squareNode.setAttribute("stroke", "black");
//        squareNode.setAttribute("stroke-width", "2");
//        addTarget.appendChild(squareNode);
//    }
}
