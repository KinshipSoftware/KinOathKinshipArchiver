package nl.mpi.kinnate.svg.relationlines;

import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.RelationTypeDefinition;
import nl.mpi.kinnate.svg.GraphPanel;

/**
 * Document : RelationRecord
 * Created on : Jun 29, 2012, 2:18:15 PM
 * Author : Peter Withers
 */
public class RelationRecord {

    public String idString;
    public String lineIdString;
    public GraphPanel graphPanel;
    public EntityData leftEntity;
    public EntityData rightEntity;
    public DataTypes.RelationType directedRelation;
    public int lineWidth;
    public int lineDash;
    public RelationTypeDefinition.CurveLineOrientation curveLineOrientation;
    public String lineColour;
    public String lineLabel;
    public int hSpacing;
    public int vSpacing;
    public int relationLineIndex;
    public LineRecord lineRecord;
    public String curveLinePoints = null;

    public RelationRecord(GraphPanel graphPanel, int relationLineIndex, EntityData leftEntity, EntityData rightEntity, DataTypes.RelationType directedRelation, int lineWidth, int lineDash, RelationTypeDefinition.CurveLineOrientation curveLineOrientation, String lineColour, String lineLabel, int hSpacing, int vSpacing) {
        this.graphPanel = graphPanel;
        this.leftEntity = leftEntity;
        this.rightEntity = rightEntity;
        this.directedRelation = directedRelation;
        this.lineWidth = lineWidth;
        this.lineDash = lineDash;
        this.curveLineOrientation = curveLineOrientation;
        this.lineColour = lineColour;
        this.lineLabel = lineLabel;
        this.hSpacing = hSpacing;
        this.vSpacing = vSpacing;
        this.relationLineIndex = relationLineIndex;

        float[] egoSymbolPoint;
        float[] alterSymbolPoint;
        float[] parentPoint;
        // the ancestral relations should already be unidirectional and duplicates should have been removed
        egoSymbolPoint = graphPanel.entitySvg.getEntityLocation(leftEntity.getUniqueIdentifier());
        alterSymbolPoint = graphPanel.entitySvg.getEntityLocation(rightEntity.getUniqueIdentifier());
        parentPoint = graphPanel.entitySvg.getAverageParentLocation(leftEntity);

//            relationLineIndex = relationGroupNode.getChildNodes().getLength();
        idString = "relation" + relationLineIndex;
        lineIdString = "relation" + relationLineIndex + "Line";

        // set the line end points
//        int[] egoSymbolPoint = graphPanel.dataStoreSvg.graphData.getEntityLocation(currentNode.getUniqueIdentifier());
//        int[] alterSymbolPoint = graphPanel.dataStoreSvg.graphData.getEntityLocation(graphLinkNode.getAlterNode().getUniqueIdentifier());
//        float fromX = (currentNode.getxPos()); // * hSpacing + hSpacing
//        float fromY = (currentNode.getyPos()); // * vSpacing + vSpacing
//        float toX = (graphLinkNode.getAlterNode().getxPos()); // * hSpacing + hSpacing
//        float toY = (graphLinkNode.getAlterNode().getyPos()); // * vSpacing + vSpacing
        float fromX = (egoSymbolPoint[0]); // * hSpacing + hSpacing
        float fromY = (egoSymbolPoint[1]); // * vSpacing + vSpacing
        float toX = (alterSymbolPoint[0]); // * hSpacing + hSpacing
        float toY = (alterSymbolPoint[1]); // * vSpacing + vSpacing

        if (!DataTypes.isSanguinLine(directedRelation)) {
//            case kinTermLine:
            // this case uses the following case
//            case verticalCurve:
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
            curveLinePoints = setPathPointsAttribute(curveLineOrientation, hSpacing, vSpacing, fromX, fromY, toX, toY);
        } else {
//            case sanguineLine:
            //                            Element squareLinkLine = doc.createElement("line");
            //                            squareLinkLine.setAttribute("x1", Integer.toString(currentNode.xPos * hSpacing + hSpacing));
            //                            squareLinkLine.setAttribute("y1", Integer.toString(currentNode.yPos * vSpacing + vSpacing));
            //
            //                            squareLinkLine.setAttribute("x2", Integer.toString(graphLinkNode.linkedNode.xPos * hSpacing + hSpacing));
            //                            squareLinkLine.setAttribute("y2", Integer.toString(graphLinkNode.linkedNode.yPos * vSpacing + vSpacing));
            //                            squareLinkLine.setAttribute("stroke", "grey");
            //                            squareLinkLine.setAttribute("stroke-width", Integer.toString(strokeWidth));
            setPolylinePointsAttribute(graphPanel.lineLookUpTable, lineIdString, directedRelation, vSpacing, fromX, fromY, toX, toY, parentPoint);
        }

    }
