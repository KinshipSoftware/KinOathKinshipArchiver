package nl.mpi.kinnate.svg.relationlines;

import java.awt.Point;
import java.util.HashSet;

/**
 * Document : LineLookUpTable
 * Created on : Sep 30, 2011, 3:24:55 PM
 * Author : Peter Withers
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

    private boolean intersects(Point[] horizontalLine, Point[] verticalLine) {
        boolean startsBefore = horizontalLine[0].x <= verticalLine[0].x;
        boolean endsBefore = horizontalLine[1].x <= verticalLine[0].x;
        boolean startsAbove = verticalLine[0].y <= horizontalLine[0].y;
        boolean endsAbove = verticalLine[1].y <= horizontalLine[0].y;
        return (startsBefore != endsBefore && startsAbove != endsAbove);
    }

    private boolean overlaps(Point[] lineA, Point[] lineB) {
        boolean verticalMatch = lineA[0].x == lineB[0].x && lineA[0].x == lineB[1].x && lineA[0].x == lineA[1].x;
        boolean horizontalMatch = lineA[0].y == lineB[0].y && lineA[0].y == lineB[1].y && lineA[0].y == lineA[1].y;
        // horizontalMatch
        int startA = lineA[0].x;
        int startB = lineB[0].x;
        int endA = lineA[1].x;
        int endB = lineB[1].x;
        if (verticalMatch) {
            // verticalMatch
            startA = lineA[0].y;
            startB = lineB[0].y;
            endA = lineA[1].y;
            endB = lineB[1].y;
        }
        boolean zeroLength = startA == endA || startB == endB;
        if (zeroLength) {
            // zero length lines don't count as a match
            return false;
        }
        if (horizontalMatch || verticalMatch) {
            // is lineA within lineB
            // is lineB within lineA
            if (startA < startB && startA > endB) {
//                System.out.print(horizontalMatch + "||" + verticalMatch);
//                System.out.print("x");
                return true;
            } else if (startA > startB && startA < endB) {
//                System.out.print(horizontalMatch + "||" + verticalMatch);
//                System.out.print("y");
                return true;
            } else if (startB < startA && startB > endA) {
//                System.out.print(horizontalMatch + "||" + verticalMatch);
//                System.out.print("z");
                return true;
            } else if (startB > startA && startB < endA) {
//                System.out.print(horizontalMatch + "||" + verticalMatch);
//                System.out.print("w");
                return true;
            }
        }
        return false;
    }

    public void separateOverlappingLines() {
        LineRecord[] lineRecordArray = lineRecords.toArray(new LineRecord[0]);
        for (int lineRecordCount = 0; lineRecordCount < lineRecordArray.length; lineRecordCount++) {
//            System.out.print(lineRecordCount + ": ");
            LineRecord lineRecordOuter = lineRecordArray[lineRecordCount];
            for (int currentIndexA = lineRecordOuter.getLastSegment(); currentIndexA > -1; currentIndexA--) {
                Point[] currentSegmentA = lineRecordOuter.getSegment(currentIndexA);
//                System.out.print("[a" + currentIndexA + "]");
                for (int lineRecordInnerCount = lineRecordCount + 1; lineRecordInnerCount < lineRecordArray.length; lineRecordInnerCount++) {
                    if (lineRecordCount != lineRecordInnerCount) {
                        LineRecord lineRecordInner = lineRecordArray[lineRecordInnerCount];
                        for (int currentIndexB = 0; currentIndexB <= lineRecordInner.getLastSegment(); currentIndexB++) {
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
                currentHorizontal = lineRecordForLoops.getPrevHorizontal(currentHorizontal);
            }
            lineRecordForLoops.sortLoops();
        }
    }

//    private Point[] getIntersections(LineRecord localLineRecord) {
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
