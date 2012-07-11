package nl.mpi.kinnate.svg.relationlines;

import java.awt.Point;
import java.util.ArrayList;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.RelationTypeDefinition;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.svg.OldFormatException;

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
    public LineRecord lineRecord; // todo: remove this 
    private String curveLinePoints = null; // todo: remove this 

    public RelationRecord(String lineIdString, DataTypes.RelationType relationType, float vSpacing, float egoX, float egoY, float alterX, float alterY, float[] averageParentPassed) throws OldFormatException {
        lineRecord = setPolylinePointsAttribute(lineIdString, relationType, vSpacing, egoX, egoY, alterX, alterY, averageParentPassed);
    }

    public RelationRecord(RelationTypeDefinition.CurveLineOrientation curveLineOrientation, float hSpacing, float vSpacing, float egoX, float egoY, float alterX, float alterY) {
        curveLinePoints = setPathPointsAttribute(curveLineOrientation, hSpacing, vSpacing, egoX, egoY, alterX, alterY);
    }

    protected RelationRecord(GraphPanel graphPanel, int relationLineIndex, EntityData leftEntity, EntityData rightEntity, DataTypes.RelationType directedRelation, int lineWidth, int lineDash, RelationTypeDefinition.CurveLineOrientation curveLineOrientation, String lineColour, String lineLabel, int hSpacing, int vSpacing) throws OldFormatException {
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
        idString = "relation" + relationLineIndex;
        lineIdString = "relation" + relationLineIndex + "Line";
    }

    public String getPathPointsString() {
        String returnValue;
        if (curveLinePoints != null) {
            returnValue = curveLinePoints;
        } else {
            returnValue = lineRecord.getPointsAttribute();
        }
//        System.out.println("getPathPointsString: " + returnValue);
        return returnValue;
    }

    public void updatePathPoints(LineLookUpTable lineLookUpTable) throws OldFormatException {
        float[] egoSymbolPoint;
        float[] alterSymbolPoint;
        float[] parentPoint;
        // the ancestral relations should already be unidirectional and duplicates should have been removed
        egoSymbolPoint = graphPanel.entitySvg.getEntityLocation(leftEntity.getUniqueIdentifier());
        alterSymbolPoint = graphPanel.entitySvg.getEntityLocation(rightEntity.getUniqueIdentifier());
        parentPoint = graphPanel.entitySvg.getAverageParentLocation(leftEntity);

//            relationLineIndex = relationGroupNode.getChildNodes().getLength();

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
            lineRecord = setPolylinePointsAttribute(lineIdString, directedRelation, vSpacing, fromX, fromY, toX, toY, parentPoint);
            lineLookUpTable.addRecord(this.lineRecord);

//          todo: check for cases when lineLookUpTable is null 
//            if (lineLookUpTable != null) {
//                // this version is used when the relations are drawn on the diagram
//                // or when an entity is dragged before the diagram is redrawn in the case of a reloaded from disk diagram (this case is sub optimal in that on first load the loops will not be drawn)
//                return new LineRecord(lineIdString, initialPointsList);
//            } else {
//                // this version is used when the relation drag handles are used
////           return new LineLookUpTable.LineRecord(lineIdString, initialPointsList.toArray(new Point[]{}));
//                throw new UnsupportedOperationException("lineLookUpTable == null, this is not yet supported");
//            } 
        }

    }

    private LineRecord setPolylinePointsAttribute(String lineIdString, DataTypes.RelationType relationType, float vSpacing, float egoX, float egoY, float alterX, float alterY, float[] averageParentPassed) throws OldFormatException {
        //float midY = (egoY + alterY) / 2;
        // todo: Ticket #1064 when an entity is above one that it should be below the line should make a zigzag to indicate it        
        ArrayList<Point> initialPointsList = new ArrayList<Point>();
        float[] averageParent = null;
        float midSpacing = vSpacing / 2;
//        float parentSpacing = 10;
        float egoYmid;
        float alterYmid;
        float centerX = (egoX + alterX) / 2;
        switch (relationType) {
            case ancestor:
                if (averageParentPassed == null) {
                    // if no parent location has been provided then just use the current parent
                    averageParent = new float[]{alterX, alterY};
                } else {
                    // todo: this is filtering out the parent location for non ancestor relations, but it would be more efficient to no get the parent location unless required
                    averageParent = averageParentPassed;
                }
                egoYmid = egoY - midSpacing;
                alterYmid = averageParent[1] + 30;
//                alterYmid = alterY + midSpacing;
//                egoYmid = alterYmid + 30;
                centerX = (egoYmid < alterYmid) ? centerX : egoX;
                centerX = (egoY < alterY && egoX == alterX) ? centerX - midSpacing : centerX;
                break;
//                float tempX = egoX;
//                float tempY = egoY;
//                egoX = alterX;
//                egoY = alterY;
//                alterX = tempX;
//                alterY = tempY;
            case descendant:
                throw new OldFormatException("This diagram needs to be updated, select recalculate diagram from the edit menu before continuing.");
//                targetNode.getParentNode().getParentNode().getParentNode().getParentNode().removeChild(targetNode.getParentNode().getParentNode().getParentNode());
//                throw new UnsupportedOperationException("in order to simplify section, the ancestor relations should be swapped so that ego is the parent");
//                return;
//                throw new UnsupportedOperationException("in order to simplify section, the ancestor relations should be swapped so that ego is the parent");
//                egoYmid = egoY + midSpacing;
//                alterYmid = alterY - midSpacing;
//                centerX = (egoYmid < alterYmid) ? alterX : centerX;
//                centerX = (egoY > alterY && egoX == alterX) ? centerX - midSpacing : centerX;
//                break;
            case sibling:
                egoYmid = egoY - midSpacing;
                alterYmid = alterY - midSpacing;
                centerX = (egoY < alterY) ? alterX : egoX;
                centerX = (egoX == alterX) ? centerX - midSpacing : centerX;
                break;
            case union:
//                float unionMid = (egoY > alterY) ? egoY : alterY;
                egoYmid = egoY + 30;
                alterYmid = alterY + 30;
                centerX = (egoY < alterY) ? egoX : alterX;
                centerX = (egoX == alterX) ? centerX - midSpacing : centerX;
                break;
//            case affiliation:
//            case none:
            default:
                egoYmid = egoY;
                alterYmid = alterY;
                break;
        }
//        if (alterY == egoY) {
//            // make sure that union lines go below the entities and sibling lines go above
//            if (relationType == DataTypes.RelationType.sibling) {
//                midY = alterY - vSpacing / 2;
//            } else if (relationType == DataTypes.RelationType.union) {
//                midY = alterY + vSpacing / 2;
//            }
//        }

        initialPointsList.add(new Point((int) egoX, (int) egoY));
        initialPointsList.add(new Point((int) egoX, (int) egoYmid));

        if (averageParent != null) {
            float averageParentX = averageParent[0];
//            float minParentY = averageParent[1];
            initialPointsList.add(new Point((int) averageParentX, (int) egoYmid));
            initialPointsList.add(new Point((int) averageParentX, (int) alterYmid));
        } else {
            initialPointsList.add(new Point((int) centerX, (int) egoYmid));
            initialPointsList.add(new Point((int) centerX, (int) alterYmid));
        }
        initialPointsList.add(new Point((int) alterX, (int) alterYmid));
        initialPointsList.add(new Point((int) alterX, (int) alterY));

        return new LineRecord(lineIdString, initialPointsList);
    }

    private String setPathPointsAttribute(RelationTypeDefinition.CurveLineOrientation curveLineOrientation, float hSpacing, float vSpacing, float egoX, float egoY, float alterX, float alterY) {
        float fromBezX;
        float fromBezY;
        float toBezX;
        float toBezY;
        if ((egoX > alterX && egoY < alterY) || (egoX > alterX && egoY > alterY)) {
            // prevent the label on the line from rendering upside down
            float tempX = alterX;
            float tempY = alterY;
            alterX = egoX;
            alterY = egoY;
            egoX = tempX;
            egoY = tempY;
        }
        // todo: if this line is too straight then add a curve by tweeking the handles
        if (curveLineOrientation == RelationTypeDefinition.CurveLineOrientation.vertical) {
            fromBezX = egoX;
            toBezX = alterX;
            if (egoY > alterY) {
                if (egoY - alterY < hSpacing / 4) {
                    fromBezY = egoY - hSpacing / 4;
                    toBezY = alterY - hSpacing / 4;
                } else {
                    fromBezY = (egoY - alterY) / 2 + alterY;
                    toBezY = (egoY - alterY) / 2 + alterY;
                }
            } else {
                if (alterY - egoY < hSpacing / 4) {
                    fromBezY = egoY + hSpacing / 4;
                    toBezY = alterY + hSpacing / 4;
                } else {
                    fromBezY = (alterY - egoY) / 2 + egoY;
                    toBezY = (alterY - egoY) / 2 + egoY;
                }
            }
//            System.out.println("egoY: " + egoY + " alterY: " + alterY);
            final float distanceX = Math.abs(egoX - alterX);
//            System.out.println("distanceY: " + distanceY);
            if (distanceX < hSpacing / 4) {
//                System.out.println("needs curve added");
                boolean quadrantType = egoY > alterY == egoY > alterY; // top left and bottom right need to be handled differently from top right and bottom left                
//                System.out.println("quadrantType: " + quadrantType);
                final float curveToAdd = hSpacing / 8;
                if (quadrantType) {
                    fromBezX -= curveToAdd;
                    toBezX += curveToAdd;
                } else {
                    fromBezX += curveToAdd;
                    toBezX -= curveToAdd;
                }
            }
        } else {
            fromBezY = egoY;
            toBezY = alterY;
            if (egoX > alterX) {
                if (egoX - alterX < hSpacing / 4) {
                    fromBezX = egoX - hSpacing / 4;
                    toBezX = alterX - hSpacing / 4;
                } else {
                    fromBezX = (egoX - alterX) / 2 + alterX;
                    toBezX = (egoX - alterX) / 2 + alterX;
                }
            } else {
                if (alterX - egoX < hSpacing / 4) {
                    fromBezX = egoX + hSpacing / 4;
                    toBezX = alterX + hSpacing / 4;
                } else {
                    fromBezX = (alterX - egoX) / 2 + egoX;
                    toBezX = (alterX - egoX) / 2 + egoX;
                }
            }
//            System.out.println("egoY: " + egoY + " alterY: " + alterY);
            final float distanceY = Math.abs(egoY - alterY);
//            System.out.println("distanceY: " + distanceY);
            if (distanceY < hSpacing / 4) {
//                System.out.println("needs curve added");
                boolean quadrantType = egoX > alterX == egoY > alterY; // top left and bottom right need to be handled differently from top right and bottom left                
//                System.out.println("quadrantType: " + quadrantType);
                final float curveToAdd = hSpacing / 8;
                if (quadrantType) {
                    fromBezY -= curveToAdd;
                    toBezY += curveToAdd;
                } else {
                    fromBezY += curveToAdd;
                    toBezY -= curveToAdd;
                }
            }
        }
        return "M " + egoX + "," + egoY + " C " + fromBezX + "," + fromBezY + " " + toBezX + "," + toBezY + " " + alterX + "," + alterY;
    }
}