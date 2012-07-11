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
        if (horizontalMatch || verticalMatch) {
            // is lineA within lineB
            // is lineB within lineA
            if (startA < startB && startA > endB) {
                return true;
            } else if (startA > startB && startA < endB) {
                return true;
            } else if (startB < startA && startB > endA) {
                return true;
            } else if (startB > startA && startB < endA) {
                return true;
            }
        }
        return false;
    }

    public void separateOverlappingLines() {
        for (LineRecord lineRecordForLoops : lineRecords) {
            for (int currentIndexA = lineRecordForLoops.getLastSegment(); currentIndexA > -1; currentIndexA--) {
                Point[] currentSegmentA = lineRecordForLoops.getSegment(currentIndexA);
//                System.out.println("currentHorizontal: " + currentHorizontal);
                for (LineRecord lineRecord : lineRecords) {
                    if (lineRecord != lineRecordForLoops) {
                        for (int currentIndexB = 0; currentIndexB <= lineRecord.getLastSegment(); currentIndexB++) {
                            Point[] otherHorizontalLine = lineRecord.getSegment(currentIndexB);
                            if (overlaps(currentSegmentA, otherHorizontalLine)) {
                                lineRecordForLoops.moveAside(currentIndexA, 6);
                            }
                        }
                    }
                }

            }
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
