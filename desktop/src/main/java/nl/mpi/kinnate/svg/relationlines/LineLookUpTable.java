package nl.mpi.kinnate.svg.relationlines;

import java.awt.Point;
import java.util.HashSet;
import nl.mpi.kinnate.svg.EntitySvg;

/**
 * Document : LineLookUpTable Created on : Sep 30, 2011, 3:24:55 PM
 *
 * @author Peter Withers
 */
public class LineLookUpTable {

    // this hashset keeps one line record for line part for each pair of entities, the line segments might be updated when an entity is dragged
    // todo: there will probably be multiple line parts for each pari of entities: start segment, end segment, main line and maybe some zig zag bits, even if these zig zag bits are not ued they probably should always be there for simplicity
    HashSet<LineRecord> lineRecords = new HashSet<LineRecord>();

    protected LineLookUpTable() {
    }

    public void getIntersectsEntity() {
    }

    public void getOverlapsOtherLine() {
    }

    protected Point[] getLineDirected(Point[] undirectedLine) {
        // this method is not intended to handle diagonal lines
        Point[] lineDirected;
        if (undirectedLine[0].y == undirectedLine[1].y) {
            if (undirectedLine[0].x <= undirectedLine[1].x) {
                lineDirected = undirectedLine;
            } else {
                lineDirected = new Point[]{undirectedLine[1], undirectedLine[0]};
            }
        } else if (undirectedLine[0].y <= undirectedLine[1].y) {
            lineDirected = undirectedLine;
        } else {
            lineDirected = new Point[]{undirectedLine[1], undirectedLine[0]};
        }
        return lineDirected;
    }

    protected boolean intersectsPoint(Point entityPoint, Point[] relationLine) {
        System.out.println("entityPoint:" + entityPoint);
        System.out.println("segment: " + relationLine[0] + relationLine[1]);
        boolean startsBefore = relationLine[0].x <= entityPoint.x + EntitySvg.symbolSize;
        boolean endsBefore = relationLine[1].x <= entityPoint.x;
        boolean startsAbove = relationLine[0].y <= entityPoint.y + EntitySvg.symbolSize;
        boolean endsAbove = relationLine[1].y <= entityPoint.y;
        final boolean intersectsResult = startsBefore != endsBefore && startsAbove != endsAbove;
        System.out.println("startsBefore: " + startsBefore);
        System.out.println("endsBefore: " + endsBefore);
        System.out.println("intersectsResult: " + intersectsResult);
        return (intersectsResult);
    }

    protected boolean intersects(Point[] horizontalLine, Point[] verticalLine) {
        Point[] horizontalLineDirected = getLineDirected(horizontalLine);
        boolean startsBefore = horizontalLineDirected[0].x < verticalLine[0].x;
        boolean endsBefore = horizontalLineDirected[1].x <= verticalLine[0].x;
        boolean startsAbove = verticalLine[0].y < horizontalLineDirected[0].y;
        boolean endsAbove = verticalLine[1].y < horizontalLineDirected[0].y;
        final boolean intersectsResult = startsBefore != endsBefore && startsAbove != endsAbove;
        return (intersectsResult);
    }

    protected boolean overlaps(Point[] lineA, Point[] lineB) {
        Point[] lineDirectedA = getLineDirected(lineA);
        Point[] lineDirectedB = getLineDirected(lineB);
        boolean verticalMatch = lineDirectedA[0].x == lineDirectedB[0].x && lineDirectedA[0].x == lineDirectedB[1].x && lineDirectedA[0].x == lineDirectedA[1].x;
        boolean horizontalMatch = lineDirectedA[0].y == lineDirectedB[0].y && lineDirectedA[0].y == lineDirectedB[1].y && lineDirectedA[0].y == lineDirectedA[1].y;
        // horizontalMatch
        int startA = lineDirectedA[0].x;
        int startB = lineDirectedB[0].x;
        int endA = lineDirectedA[1].x;
        int endB = lineDirectedB[1].x;
        if (verticalMatch) {
            // verticalMatch
            startA = lineDirectedA[0].y;
            startB = lineDirectedB[0].y;
            endA = lineDirectedA[1].y;
            endB = lineDirectedB[1].y;
        }
        boolean zeroLength = startA == endA || startB == endB;
        if (zeroLength) {
            // zero length lines don't count as a match
            return false;
        }
        if (horizontalMatch || verticalMatch) {
            // is lineA within lineB
            // is lineB within lineA
            if (startA <= startB && startA > endB) {
//                System.out.print(horizontalMatch + "||" + verticalMatch);
//                System.out.println(" x");
                return true;
            } else if (startA >= startB && startA < endB) {
//                System.out.print(horizontalMatch + "||" + verticalMatch);
//                System.out.println(" y");
                return true;
            } else if (startB <= startA && startB > endA) {
//                System.out.print(horizontalMatch + "||" + verticalMatch);
//                System.out.println(" z");
                return true;
            } else if (startB >= startA && startB < endA) {
//                System.out.print(horizontalMatch + "||" + verticalMatch);
//                System.out.println(" w");
                return true;
            }
        }
        return false;
    }
    boolean excludeFirstLastSegments = true;

    public void separateLinesOverlappingEntities(Point[] allEntityLocations) {
        int offset = 0;
        if (excludeFirstLastSegments) {
            offset = 1;
        }
        LineRecord[] lineRecordArray = lineRecords.toArray(new LineRecord[0]);
        for (int lineRecordCount = 0; lineRecordCount < lineRecordArray.length; lineRecordCount++) {
//            System.out.print("lineRecordCount: "+lineRecordCount);
            LineRecord lineRecordOuter = lineRecordArray[lineRecordCount];
            for (int currentIndexA = 0 + offset; currentIndexA <= lineRecordOuter.getLastSegment() - offset; currentIndexA++) {
                Point[] currentSegmentA = lineRecordOuter.getSegment(currentIndexA);
                System.out.print("[" + lineRecordCount + ":" + currentIndexA + "]");
                for (Point entityLocation : allEntityLocations) {
                    System.out.println("entityLocation:" + entityLocation);
                    if (intersectsPoint(entityLocation, currentSegmentA)) {
                        System.out.print(" intersects,");
//                    System.out.print("[" + entityLocation + "]");
                        lineRecordOuter.moveAside(currentIndexA, 6);
                    } else {
//                        System.out.print(",");
                    }
                }
            }
            System.out.println("");
        }
    }

    public void separateOverlappingLines() {
        int offset = 0;
        if (excludeFirstLastSegments) {
            offset = 1;
        }
        LineRecord[] lineRecordArray = lineRecords.toArray(new LineRecord[0]);
        for (int lineRecordCount = 0; lineRecordCount < lineRecordArray.length; lineRecordCount++) {
//            System.out.print(lineRecordCount + ": ");
            LineRecord lineRecordOuter = lineRecordArray[lineRecordCount];
            for (int currentIndexA = 0 + offset; currentIndexA <= lineRecordOuter.getLastSegment() - offset; currentIndexA++) {
                Point[] currentSegmentA = lineRecordOuter.getSegment(currentIndexA);
//                System.out.print("[a" + currentIndexA + "]");
                for (int lineRecordInnerCount = lineRecordCount + 1; lineRecordInnerCount < lineRecordArray.length; lineRecordInnerCount++) {
                    if (lineRecordCount != lineRecordInnerCount) {
                        LineRecord lineRecordInner = lineRecordArray[lineRecordInnerCount];
                        if (lineRecordOuter.sharesSameGroup(lineRecordInner)) {
                            // todo: hide lines that are overlapped by lines in the same group
                        } else {
                            for (int currentIndexB = 0 + offset; currentIndexB <= lineRecordInner.getLastSegment() - offset; currentIndexB++) {
                                Point[] otherHorizontalLine = lineRecordInner.getSegment(currentIndexB);
//                            System.out.print("[b" + currentIndexB + "]");
                                if (overlaps(currentSegmentA, otherHorizontalLine)) {
//                                System.out.print(" overlaps,");
                                    lineRecordInner.moveAside(currentIndexB, 6);
                                } else {
//                                System.out.print(",");
                                }
                            }
                        }
                    }
                }
            }
//            System.out.println("");
        }
    }

    public void addLoops() {
        for (LineRecord lineRecordForLoops : lineRecords) {
            int currentHorizontal = lineRecordForLoops.getLastHorizontal();
            while (currentHorizontal > -1) {
                Point[] currentHorizontalLine = lineRecordForLoops.getSegment(currentHorizontal);
//                System.out.println("currentHorizontal: " + currentHorizontal);
                for (LineRecord lineRecord : lineRecords) {
                    if (lineRecord != lineRecordForLoops) {
                        if (!lineRecord.sharesSameGroup(lineRecordForLoops)) {
                            int currentVertical = lineRecord.getFirstVertical();
//                        System.out.println("currentVertical: " + currentVertical);
                            while (currentVertical > -1) {
                                Point[] currentVerticalLine = lineRecord.getSegment(currentVertical);
                                if (intersects(currentHorizontalLine, currentVerticalLine)) {
                                    boolean isLeftHand = currentHorizontalLine[0].x > currentHorizontalLine[1].x;
                                    lineRecordForLoops.insertLoop(currentHorizontal, currentVerticalLine[0].x, isLeftHand);
                                }
                                currentVertical = lineRecord.getNextVertical(currentVertical);
                            }
                        }
                    }
                }
                currentHorizontal = lineRecordForLoops.getPrevHorizontal(currentHorizontal);
            }
            lineRecordForLoops.sortLoops();
        }
    }

//    protected Point[] getIntersections(LineRecord localLineRecord) {
//        HashSet<Point> intersectionPoints = new HashSet<Point>();
//        for (LineRecord lineRecord : lineRecords) {
//            int currentHorizontal = lineRecord.getFirstHorizontal();
//            while (currentHorizontal > -1) {
//                System.out.println("currentHorizontal: " + currentHorizontal);
//                currentHorizontal = lineRecord.getNextHorizontal(currentHorizontal);
//            }
//
//            Point intersectionPoint = localLineRecord.getIntersection(lineRecord);
//            if (lineRecord != null) {
//                intersectionPoints.add(intersectionPoint);
//            }
//        }
//        return intersectionPoints.toArray(new Point[]{});
//    }
    public void addRecord(LineRecord lineRecord) {
        lineRecords.add(lineRecord);
    }
//    public LineRecord adjustLineToObstructions(String lineIdString, ArrayList<Point> pointsList) {
//        LineRecord lineRecord = new LineRecord(lineIdString, pointsList);
////        getIntersections(localLineRecord);
////        //localLineRecord.insertLoop(3);
//        lineRecords.add(lineRecord);
//        return lineRecord;
//    }
}
