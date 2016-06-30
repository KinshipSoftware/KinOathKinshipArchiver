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
package nl.mpi.kinnate.svg.relationlines;

import java.util.ArrayList;
import java.util.HashSet;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.kindata.KinPoint;
import nl.mpi.kinnate.kindata.RelationTypeDefinition;
import nl.mpi.kinnate.svg.OldFormatException;
import nl.mpi.kinnate.svg.SvgDiagram;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : RelationRecord Created on : Jun 29, 2012, 2:18:15 PM
 *
 * @author Peter Withers
 */
public class RelationRecord {

//    private final static Logger logger = LoggerFactory.getLogger(RelationRecord.class);
    private String groupName = null;
    public String idString;
    public String lineIdString;
    public SvgDiagram svgDiagram;
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
//    private boolean lineDirectionReversed = false; // sometimes a line is reversed so that any label is not shown upside down, but this must be known when rendering directed lines with line end markers
    final private String dcrType;
    final private String customType;

    public RelationRecord(String lineIdString, DataTypes.RelationType relationType, float vSpacing, KinPoint egoPoint, KinPoint alterPoint, KinPoint averageParentPassed) throws OldFormatException {
        this.dcrType = null;
        this.customType = null;
        lineRecord = setPolylinePointsAttribute(lineIdString, relationType, vSpacing, egoPoint.x, egoPoint.y, alterPoint.x, alterPoint.y, averageParentPassed);
    }

    public RelationRecord(RelationTypeDefinition.CurveLineOrientation curveLineOrientation, float hSpacing, float vSpacing, KinPoint egoPoint, KinPoint alterPoint) {
        this.dcrType = null;
        this.customType = null;
        curveLinePoints = setPathPointsAttribute(curveLineOrientation, hSpacing, vSpacing, egoPoint.x, egoPoint.y, alterPoint.x, alterPoint.y);
    }

    protected RelationRecord(String groupName, SvgDiagram svgDiagram, int relationLineIndex, EntityData leftEntity, EntityData rightEntity, DataTypes.RelationType directedRelation, String dcrType, String customType, int lineWidth, int lineDash, RelationTypeDefinition.CurveLineOrientation curveLineOrientation, String lineColour, String lineLabel, int hSpacing, int vSpacing) {
        this.groupName = groupName;
        this.svgDiagram = svgDiagram;
        this.leftEntity = leftEntity;
        this.rightEntity = rightEntity;
        this.directedRelation = directedRelation;
        this.dcrType = dcrType;
        this.customType = customType;
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

    public boolean pertainsToEntity(ArrayList<UniqueIdentifier> selectedIdentifiers) {
        return (selectedIdentifiers.contains(leftEntity.getUniqueIdentifier()) || selectedIdentifiers.contains(rightEntity.getUniqueIdentifier()));
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean belongsToGroup(HashSet<String> groupSet) {
        if (groupName != null) {
            return groupSet.contains(groupName);
        } else {
            return false;
        }
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
    // start caclulate the average parent position
//    private HashMap<UniqueIdentifier, HashSet<UniqueIdentifier>> parentIdentifiers = new HashMap<UniqueIdentifier, HashSet<UniqueIdentifier>>();

    public KinPoint getAverageParentLocation(HashSet<UniqueIdentifier> parentIdSet) {
        // note that getAverageParentLocation(EntityData entityData) must be called at least once for each entity to poputlate parentIdentifiers
        Integer maxY = null;
        int averageX = 0;
        int parentCount = 0;
//        final HashSet<UniqueIdentifier> parentIdSet = parentIdentifiers.get(entityId);
        if (parentIdSet != null) {
            for (UniqueIdentifier parentIdentifier : parentIdSet) {
                KinPoint parentLoc = svgDiagram.entitySvg.getEntityLocationOffset(parentIdentifier);
                if (maxY == null) {
                    maxY = parentLoc.y;
                } else {
                    maxY = (maxY >= parentLoc.y) ? maxY : parentLoc.y;
                }
                averageX += parentLoc.x;
                parentCount++;
            }
        }
        averageX = averageX / parentCount;
        if (maxY == null) {
            return null;
        } else {
            return new KinPoint(averageX, maxY);
        }
    }

    public KinPoint getAverageParentLocation(EntityData entityData) {
        HashSet<UniqueIdentifier> identifierSet = new HashSet<UniqueIdentifier>();
        for (EntityRelation entityRelation : entityData.getAllRelations()) {
            if (entityRelation.getAlterNode() != null && entityRelation.getAlterNode().isVisible) {
                if (entityRelation.isSameType(DataTypes.RelationType.ancestor, dcrType, customType)) {
                    identifierSet.add(entityRelation.alterUniqueIdentifier);
                }
            }
        }
        if (identifierSet.size() < 2) {
            return null;
        }
//        parentIdentifiers.put(entityData.getUniqueIdentifier(), identifierSet);
        return getAverageParentLocation(identifierSet);
    }
    // end caclulate the average parent position

    public void updatePathPoints(LineLookUpTable lineLookUpTable) throws OldFormatException {
        KinPoint egoSymbolPoint;
        KinPoint alterSymbolPoint;
        KinPoint parentPoint = null;
        // the ancestral relations should already be unidirectional and duplicates should have been removed
        egoSymbolPoint = svgDiagram.entitySvg.getEntityLocationOffset(leftEntity.getUniqueIdentifier());
        alterSymbolPoint = svgDiagram.entitySvg.getEntityLocationOffset(rightEntity.getUniqueIdentifier());
        if (directedRelation != DataTypes.RelationType.sibling && directedRelation != DataTypes.RelationType.union) {
            parentPoint = getAverageParentLocation(leftEntity);
        }

//            relationLineIndex = relationGroupNode.getChildNodes().getLength();
        // set the line end points
//        int[] egoSymbolPoint = graphPanel.dataStoreSvg.graphData.getEntityLocationOffset(currentNode.getUniqueIdentifier());
//        int[] alterSymbolPoint = graphPanel.dataStoreSvg.graphData.getEntityLocationOffset(graphLinkNode.getAlterNode().getUniqueIdentifier());
//        float fromX = (currentNode.getxPos()); // * hSpacing + hSpacing
//        float fromY = (currentNode.getyPos()); // * vSpacing + vSpacing
//        float toX = (graphLinkNode.getAlterNode().getxPos()); // * hSpacing + hSpacing
//        float toY = (graphLinkNode.getAlterNode().getyPos()); // * vSpacing + vSpacing
        int fromX = (egoSymbolPoint.x); // * hSpacing + hSpacing
        int fromY = (egoSymbolPoint.y); // * vSpacing + vSpacing
        int toX = (alterSymbolPoint.x); // * hSpacing + hSpacing
        int toY = (alterSymbolPoint.y); // * vSpacing + vSpacing

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
////           return new LineLookUpTable.LineRecord(lineIdString, initialPointsList.toArray(new KinPoint[]{}));
//                throw new UnsupportedOperationException("lineLookUpTable == null, this is not yet supported");
//            } 
        }

    }

    private LineRecord setPolylinePointsAttribute(String lineIdString, DataTypes.RelationType relationType, float vSpacing, float egoX, float egoY, float alterX, float alterY, KinPoint averageParentPassed) throws OldFormatException {
        //float midY = (egoY + alterY) / 2;
        // todo: Ticket #1064 when an entity is above one that it should be below the line should make a zigzag to indicate it        
        ArrayList<KinPoint> initialPointsList = new ArrayList<KinPoint>();
        KinPoint averageParent = null;
        float midSpacing = vSpacing / 2;
//        float parentSpacing = 10;
        float egoYmid;
        float alterYmid;
        float centerX = (egoX + alterX) / 2;
        switch (relationType) {
            case ancestor:
                if (averageParentPassed == null) {
                    // if no parent location has been provided then just use the current parent
                    averageParent = new KinPoint((int) alterX, (int) alterY);
                } else {
                    // todo: this is filtering out the parent location for non ancestor relations, but it would be more efficient to no get the parent location unless required
                    averageParent = averageParentPassed;
                }
                egoYmid = egoY - midSpacing + 10;
                alterYmid = averageParent.y + 30;
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
                System.out.println("Found descendant in LineRecord");
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
                if (averageParentPassed != null) {
                    throw new UnsupportedOperationException();
                }
                // push the sibling line down a bit
                egoYmid = egoY - midSpacing + 16;
                alterYmid = alterY - midSpacing + 16;
                centerX = (egoY < alterY) ? alterX : egoX;
                centerX = (egoX == alterX) ? centerX - midSpacing : centerX;
                break;
            case union:
                if (averageParentPassed != null) {
                    throw new UnsupportedOperationException();
                }
//                float unionMid = (egoY > alterY) ? egoY : alterY;
                // make the union line lower than the parents but above the mid line
                egoYmid = egoY + 30;
                alterYmid = alterY + 30;
                centerX = (egoY < alterY) ? egoX : alterX;
                centerX = (egoX == alterX) ? centerX - midSpacing : centerX;
                break;
//            case affiliation:
//            case none:
            default:
                egoYmid = egoY + 10;
                alterYmid = alterY + 10;
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

        initialPointsList.add(new KinPoint((int) egoX, (int) egoY));
        initialPointsList.add(new KinPoint((int) egoX, (int) egoYmid));

        if (averageParent != null) {
            // alter is the parent
            float averageParentX = averageParent.x;
//            float minParentY = averageParent.y;
            float centerParentX = (egoX + averageParentX) / 2;
            float lowerParentLineY = averageParent.y + midSpacing + 10;

            if (egoY >= averageParent.y + vSpacing) {
                initialPointsList.add(new KinPoint((int) egoX, (int) lowerParentLineY));
                initialPointsList.add(new KinPoint((int) averageParentX, (int) lowerParentLineY));
                initialPointsList.add(new KinPoint((int) averageParentX, (int) alterYmid));
            } else {
                initialPointsList.add(new KinPoint((int) centerParentX, (int) egoYmid));
                initialPointsList.add(new KinPoint((int) centerParentX, (int) lowerParentLineY));
                initialPointsList.add(new KinPoint((int) averageParentX, (int) lowerParentLineY));
                initialPointsList.add(new KinPoint((int) averageParentX, (int) alterYmid));
            }
        } else {
            initialPointsList.add(new KinPoint((int) centerX, (int) egoYmid));
            initialPointsList.add(new KinPoint((int) centerX, (int) alterYmid));
        }
        if (averageParentPassed == null) {
            initialPointsList.add(new KinPoint((int) alterX, (int) alterYmid));
            initialPointsList.add(new KinPoint((int) alterX, (int) alterY));
        } else {
            // the first and last segments are not moved when they overlap, so we add a final segment here so that the parent point line will be moved when overlapping 
            initialPointsList.add(initialPointsList.get(initialPointsList.size() - 1));
        }

        return new LineRecord(groupName, lineIdString, initialPointsList);
    }
//    /*
//     * When rendering directed lines with line end markers in must be known if the line has been reversed.
//     * Lines might be reversed so that any label is not shown upside down.
//     */
//
//    public boolean isLineDirectionReversed() {
//        return lineDirectionReversed;
//    }

    private String setPathPointsAttribute(RelationTypeDefinition.CurveLineOrientation curveLineOrientation, float hSpacing, float vSpacing, float egoX, float egoY, float alterX, float alterY) {
        float fromBezX;
        float fromBezY;
        float toBezX;
        float toBezY;
        // reversing the line causes havoc with the directed relations so we will need to find a better way to prevent labels rendering upsidedown
//        if ((egoX > alterX && egoY < alterY) || (egoX > alterX && egoY > alterY)) {
//            // prevent the label on the line from rendering upside down
//            float tempX = alterX;
//            float tempY = alterY;
//            alterX = egoX;
//            alterY = egoY;
//            egoX = tempX;
//            egoY = tempY;
//            lineDirectionReversed = true;
//        } else {
//            lineDirectionReversed = false;
//        }
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
            } else if (alterY - egoY < hSpacing / 4) {
                fromBezY = egoY + hSpacing / 4;
                toBezY = alterY + hSpacing / 4;
            } else {
                fromBezY = (alterY - egoY) / 2 + egoY;
                toBezY = (alterY - egoY) / 2 + egoY;
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
            } else if (alterX - egoX < hSpacing / 4) {
                fromBezX = egoX + hSpacing / 4;
                toBezX = alterX + hSpacing / 4;
            } else {
                fromBezX = (alterX - egoX) / 2 + egoX;
                toBezX = (alterX - egoX) / 2 + egoX;
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
