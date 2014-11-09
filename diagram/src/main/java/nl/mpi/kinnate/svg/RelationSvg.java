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

import java.util.ArrayList;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.svg.relationlines.RelationRecord;
import nl.mpi.kinnate.svg.relationlines.RelationRecordTable;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

/**
 * Created on : Mar 9, 2011, 3:21:16 PM
 *
 * @author Peter Withers
 */
public class RelationSvg {

    private void addUseNode(SVGDocument doc, String svgNameSpace, Element targetGroup, String targetDefId) {
        String useNodeId = targetDefId + "use";
        // todo: look into this ConcurrentModificationException
        /*
         ava.util.ConcurrentModificationException
         at java.util.AbstractList$Itr.checkForComodification(AbstractList.java:372)
         at java.util.AbstractList$Itr.remove(AbstractList.java:357)
         at org.apache.batik.dom.AbstractDocument.getChildElementById(AbstractDocument.java:485)
         at org.apache.batik.dom.AbstractDocument.getElementById(AbstractDocument.java:451)
         at nl.mpi.kinnate.svg.RelationSvg.addUseNode(RelationSvg.java:28)
         at nl.mpi.kinnate.svg.RelationSvg.updateRelationLines(RelationSvg.java:204)
         at nl.mpi.kinnate.svg.SvgUpdateHandler$6.run(SvgUpdateHandler.java:740)
         at org.apache.batik.util.RunnableQueue.run(RunnableQueue.java:237)
         at java.lang.Thread.run(Thread.java:680)
         */
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

//    private void updateLabelNode(SVGDocument doc, String svgNameSpace, String lineIdString, String targetRelationId) {
//        // remove and readd the text on path label so that it updates with the new path
//        String labelNodeId = targetRelationId + "label";
//        Node useNodeOld = doc.getElementById(labelNodeId);
//        if (useNodeOld != null) {
//            Node textParentNode = useNodeOld.getParentNode();
//            String labelText = useNodeOld.getTextContent();
//            useNodeOld.getParentNode().removeChild(useNodeOld);
//
//            Element textPath = doc.createElementNS(svgNameSpace, "textPath");
//            textPath.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "#" + lineIdString); // the xlink: of "xlink:href" is required for some svg viewers to render correctly
//            textPath.setAttribute("startOffset", "50%");
//            textPath.setAttribute("id", labelNodeId);
//            Text textNode = doc.createTextNode(labelText);
//            textPath.appendChild(textNode);
//            textParentNode.appendChild(textPath);
//        }
//    }
//    private Float getCommonParentMaxY(EntitySvg entitySvg, EntityData currentNode, EntityRelation graphLinkNode) {
//        if (graphLinkNode.relationType == DataTypes.RelationType.sibling) {
//            Float maxY = null;
//            ArrayList<Float> commonParentY = new ArrayList<Float>();
//            for (EntityRelation altersRelation : graphLinkNode.getAlterNode().getDistinctRelateNodes()) {
//                if (altersRelation.relationType == DataTypes.RelationType.ancestor) {
//                    for (EntityRelation egosRelation : currentNode.getDistinctRelateNodes()) {
//                        if (egosRelation.relationType == DataTypes.RelationType.ancestor) {
//                            if (altersRelation.alterUniqueIdentifier.equals(egosRelation.alterUniqueIdentifier)) {
//                                float parentY = entitySvg.getEntityLocation(egosRelation.alterUniqueIdentifier)[1];
//                                maxY = parentY > maxY ? parentY : maxY;
//                            }
//                        }
//                    }
//                }
//            }
//            return maxY;
//        } else {
//            return null;
//        }
//
//    }
    public void createRelationElements(SvgDiagram svgDiagram, RelationRecordTable relationRecords, Element relationGroupNode) throws OldFormatException {
        relationRecords.adjustLines(svgDiagram);

        for (RelationRecord relationRecord : relationRecords.getAllRecords()) {
            Element groupNode = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "g");
            groupNode.setAttribute("id", relationRecord.idString);
            Element defsNode = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "defs");
//            new DataStoreSvg().storeRelationParameters(graphPanel.doc, groupNode, relationRecord.directedRelation, relationRecord.curveLineOrientation, relationRecord.leftEntity.getUniqueIdentifier(), relationRecord.rightEntity.getUniqueIdentifier());
            boolean addedRelationLine = false;
            Element linkLine;
            linkLine = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "path");
            linkLine.setAttribute("d", relationRecord.getPathPointsString());
            if (relationRecord.lineDash > 0) {
                linkLine.setAttribute("stroke-dasharray", Integer.toString(relationRecord.lineDash));
                linkLine.setAttribute("stroke-dashoffset", "0");
            }
            linkLine.setAttribute("fill", "none");
            if (relationRecord.lineColour != null) {
                linkLine.setAttribute("stroke", relationRecord.lineColour);
            } else {
                linkLine.setAttribute("stroke", "grey"); // todo: get the line colour from the settings
            }
            linkLine.setAttribute("stroke-width", Integer.toString(relationRecord.lineWidth));
            linkLine.setAttribute("id", relationRecord.lineIdString);
            // begin line markers code
            if (relationRecord.directedRelation.equals(DataTypes.RelationType.directedout)) {
                int lineMarkerOffset = EntitySvg.symbolSize * 2 - relationRecord.lineWidth * 9;
                if (lineMarkerOffset < 0) {
                    lineMarkerOffset = 0;
                }
//                System.out.println("lineMarkerOffset: " + lineMarkerOffset);
                // add the start line marker
                Element startMarker = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "marker");
                final String startMarkerId = relationRecord.lineIdString + "StartMarker";
                startMarker.setAttribute("id", startMarkerId);
                startMarker.setAttribute("fill", linkLine.getAttribute("stroke"));
                startMarker.setAttribute("orient", "auto");
                startMarker.setAttribute("viewBox", "0 0 12 12");
                startMarker.setAttribute("refX", Integer.toString(-lineMarkerOffset));
                startMarker.setAttribute("refY", "6");
                startMarker.setAttribute("markerWidth", "3");
                startMarker.setAttribute("markerHeight", "3");
                Element startMarkerNode = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "use");
                startMarkerNode.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "#" + "StartMarker");
                startMarker.appendChild(startMarkerNode);
                defsNode.appendChild(startMarker);
                // add the end line marker
                Element endMarker = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "marker");
                final String endMarkerId = relationRecord.lineIdString + "EndMarker";
                endMarker.setAttribute("id", endMarkerId);
                endMarker.setAttribute("fill", linkLine.getAttribute("stroke"));
                endMarker.setAttribute("viewBox", "0 0 10 10");
                endMarker.setAttribute("refX", Integer.toString(lineMarkerOffset + 10));
                endMarker.setAttribute("refY", "5");
                endMarker.setAttribute("markerUnits", "strokeWidth");
                endMarker.setAttribute("markerWidth", "4");
                endMarker.setAttribute("markerHeight", "3");
                endMarker.setAttribute("orient", "auto");
                Element endMarkerNode = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "use");
                endMarkerNode.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "#" + "EndMarker");
                endMarker.appendChild(endMarkerNode);
                defsNode.appendChild(endMarker);
//                if (relationRecord.isLineDirectionReversed()) {
//                    linkLine.setAttribute("marker-end", "url(#" + startMarkerId + ")");
//                    linkLine.setAttribute("marker-start", "url(#" + endMarkerId + ")");
//                } else {
                linkLine.setAttribute("marker-start", "url(#" + startMarkerId + ")");
                linkLine.setAttribute("marker-end", "url(#" + endMarkerId + ")");
//                }
            }
//            linkLine.setAttribute("marker-mid", "url(#StartMarker)");
            if (relationRecord.directedRelation.equals(DataTypes.RelationType.directedin)) {
                throw new UnsupportedOperationException("Directed relations should be drawn in one direction.");
            }
            // end line markers code
            defsNode.appendChild(linkLine);
            addedRelationLine = true;

            groupNode.appendChild(defsNode);

            if (addedRelationLine) {
                // insert the node that uses the above definition
                addUseNode(svgDiagram.doc, svgDiagram.svgNameSpace, groupNode, relationRecord.lineIdString);
                // add the relation label
                // todo: the label on relation lines code is suppresed at the moment because it seems that there is a name space issue preventing the line labels to show, as exemplified by saving and reloading the document which will then show the labels correctly.
//                if (relationRecord.lineLabel != null) {
//                    Element labelText = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "text");
//                    labelText.setAttributeNS(graphPanel.svgNameSpace, "text-anchor", "middle");
//                    if (relationRecord.lineColour != null) {
//                        labelText.setAttributeNS(graphPanel.svgNameSpace, "fill", relationRecord.lineColour);
//                    } else {
//                        labelText.setAttributeNS(graphPanel.svgNameSpace, "fill", "blue");
//                    }
//                    labelText.setAttributeNS(graphPanel.svgNameSpace, "stroke-width", "0");
//                    labelText.setAttributeNS(graphPanel.svgNameSpace, "font-size", "14");
////                  labelText.setAttribute("transform", "rotate(45)");
////                // todo: resolve issues with the USE node for the text
//                    Element textPath = graphPanel.doc.createElementNS(graphPanel.svgNameSpace, "textPath");
////                    textPath.setAttributeNS("http://www.w3.rg/1999/xlink", "xlink:href", "#" + relationRecord.lineIdString);
////                  //  textPath.setAttributeNS("http://www.w3.rg/1999/xlink", "xlink:actuate", "onRequest");
//                    textPath.setAttributeNS(graphPanel.svgNameSpace, "startOffset", "50%");
//                    textPath.setTextContent(relationRecord.lineLabel);
//                    labelText.appendChild(textPath);
//                    groupNode.appendChild(labelText);
//                }
            }
            relationGroupNode.appendChild(groupNode);
        }
    }

    public void updateRelationLines(SvgDiagram svgDiagram, RelationRecordTable relationRecords, ArrayList<UniqueIdentifier> draggedNodeIds, int hSpacing, int vSpacing) throws OldFormatException {
        if (relationRecords == null) {
            // no relations therefore nothing to do here
            return;
        }
        relationRecords.adjustLines(svgDiagram);
//        graphPanel.lineLookUpTable.addLoops();
        // todo: if an entity is above its ancestor then this must be corrected, if the ancestor data is stored in the relationLine attributes then this would be a good place to correct this
        Element relationGroup = svgDiagram.doc.getElementById("RelationGroup");
//        createRelationElements(graphPanel, relationRecords, lineLookUpTable, relationGroup);
        for (Node currentChild = relationGroup.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
            if ("g".equals(currentChild.getLocalName())) {
                Node idAttrubite = currentChild.getAttributes().getNamedItem("id");
                //System.out.println("idAttrubite: " + idAttrubite.getNodeValue());
//                try {
//                    DataStoreSvg.GraphRelationData graphRelationData = new DataStoreSvg().getEntitiesForRelations(currentChild);
//                    if (graphRelationData != null) {
                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                // we update all the relation lines here, rather than cacluating which co parent (parentPoint) lines need updating when the current parent is moved //
                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                        if (draggedNodeIds.contains(graphRelationData.egoNodeId) || draggedNodeIds.contains(graphRelationData.alterNodeId)) {
                // todo: update the relation lines
                //System.out.println("needs update on: " + idAttrubite.getNodeValue());
                String lineElementId = idAttrubite.getNodeValue() + "Line";
                Element relationLineElement = svgDiagram.doc.getElementById(lineElementId);
                if (relationLineElement != null) {
                    //System.out.println("type: " + relationLineElement.getLocalName());
//                            float[] egoSymbolPoint;
//                            float[] alterSymbolPoint;
//                            float[] parentPoint;
//                        int[] egoSymbolPoint = graphPanel.dataStoreSvg.graphData.getEntityLocation(graphRelationData.egoNodeId);
//                        int[] alterSymbolPoint = graphPanel.dataStoreSvg.graphData.getEntityLocation(graphRelationData.alterNodeId);
//                            DataTypes.RelationType directedRelation = graphRelationData.relationType;
                    // the relation lines are already directed so there is no need to make then unidirectional here
//                            egoSymbolPoint = graphPanel.entitySvg.getEntityLocation(graphRelationData.egoNodeId);
//                            alterSymbolPoint = graphPanel.entitySvg.getEntityLocation(graphRelationData.alterNodeId);
//                            parentPoint = graphPanel.entitySvg.getAverageParentLocation(graphRelationData.egoNodeId);

//                            float egoX = egoSymbolPoint[0];
//                            float egoY = egoSymbolPoint[1];
//                            float alterX = alterSymbolPoint[0];
//                            float alterY = alterSymbolPoint[1];
//                        SVGRect egoSymbolRect = new EntitySvg().getEntityLocation(doc, graphRelationData.egoNodeId);
//                        SVGRect alterSymbolRect = new EntitySvg().getEntityLocation(doc, graphRelationData.alterNodeId);
//
//                        float egoX = egoSymbolRect.getX() + egoSymbolRect.getWidth() / 2;
//                        float egoY = egoSymbolRect.getY() + egoSymbolRect.getHeight() / 2;
//                        float alterX = alterSymbolRect.getX() + alterSymbolRect.getWidth() / 2;
//                        float alterY = alterSymbolRect.getY() + alterSymbolRect.getHeight() / 2;
                    if ("polyline".equals(relationLineElement.getLocalName())) {
                        //System.out.println("polyline to update: " + lineElementId);
                        // todo: we need to be getting the record from the lineLookUpTable not creating a new one
                        RelationRecord relationRecord = relationRecords.getRecord(lineElementId);
//                                    RelationRecord relationRecord = new RelationRecord(/* graphPanel.lineLookUpTable, */lineElementId, directedRelation, vSpacing, egoX, egoY, alterX, alterY, parentPoint);
                        relationLineElement.setAttribute("points", relationRecord.getPathPointsString());
                    }
                    if ("path".equals(relationLineElement.getLocalName())) {
                        //System.out.println("path to update: " + relationLineElement.getLocalName());
                        RelationRecord relationRecord = relationRecords.getRecord(lineElementId); //new RelationRecord(graphRelationData.curveLineOrientation, hSpacing, vSpacing, egoX, egoY, alterX, alterY);
                        relationLineElement.setAttribute("d", relationRecord.getPathPointsString());
                    }
                    addUseNode(svgDiagram.doc, svgDiagram.svgNameSpace, (Element) currentChild, lineElementId);
//                    updateLabelNode(graphPanel.doc, graphPanel.svgNameSpace, lineElementId, idAttrubite.getNodeValue());
                }
//                    }
//                } catch (IdentifierException exception) {
////                    GuiHelper.linorgBugCatcher.logError(exception);
//                    dialogHandler.addMessageDialogToQueue("Failed to read related entities, sanguine lines might be incorrect", "Update Sanguine Lines");
//                }
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
