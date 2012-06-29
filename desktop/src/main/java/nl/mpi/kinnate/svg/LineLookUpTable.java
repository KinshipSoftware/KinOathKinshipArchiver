package nl.mpi.kinnate.svg;

import java.awt.Point;
import java.util.ArrayList;
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

    private enum Orientation {

        horizontal, vertical
    };

    protected class LineRecord {

        public LineRecord(String lineIdString, ArrayList<Point> pointsList) {
            this.lineIdSring = lineIdString;
            this.pointsList = pointsList;
        }
        private String lineIdSring;
        private ArrayList<Point> pointsList;

//        private Point getIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
//            double denominator = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
//            if (denominator == 0.0) { // Lines are parallel.
//                return null;
//            }
//            double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denominator;
//            double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denominator;
//            if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f) {
//                // Get the intersection point.
//                return new Point((int) (x1 + ua * (x2 - x1)), (int) (y1 + ua * (y2 - y1)));
//            }
//            return null;
//        }
        // sanguine lines are either horizontal or vertical but never diagonal so this makes the following calculations simpler
        protected int getFirstHorizontal() {
            return getNextHorizontal(-1);
        }

        protected int getNextHorizontal(int current) {
            return getNext(current, Orientation.horizontal);
        }

        protected int getFirstVertical() {
            return getNextHorizontal(-1);
        }

        protected int getNextVertical(int current) {
            return getNext(current, Orientation.vertical);
        }

        private int getNext(int current, Orientation orientation) {
            for (int currentIndex = current + 1; currentIndex < pointsList.size() - 1; currentIndex++) {
                Point startPoint = pointsList.get(currentIndex);
                Point endPoint = pointsList.get(currentIndex + 1);
                switch (orientation) {
                    case horizontal:
                        if (startPoint.y == endPoint.y) {
                            return currentIndex;
                        }
                        break;
                    case vertical:
                        if (startPoint.x == endPoint.x) {
                            return currentIndex;
                        }
                        break;
                }
            }
            return -1;
        }

        protected Point getIntersection(LineRecord lineRecord) {
            return null; //Point((lineRecord.startPoint.x + lineRecord.endPoint.x) / 2, (lineRecord.startPoint.y + lineRecord.endPoint.y) / 2);
            // todo: get the actual intersections and insert loops
            // todo: in RelationSVG on first load the lineLookUpTable is null and loops will not be drawn
        }

        private void insertLoop(int linePart) {
            // todo: this is test needs to be extended to place the loops in the correct locations and to produce pretty curved loops
            Point startPoint = this.pointsList.get(linePart);
            Point endPoint = this.pointsList.get(linePart + 1);
            int centerX = (startPoint.x + endPoint.x) / 2;
            int centerY = (startPoint.y + endPoint.y) / 2;
            int startOffset = -5;
            int endOffset = +5;
            int loopHeight = -10;
            if (startPoint.x == endPoint.x) {
                // horizontal lines
                if (startPoint.y < endPoint.y) {
                    startOffset = +5;
                    endOffset = -5;
                }
                this.pointsList.add(linePart + 1, new Point(centerX, centerY + startOffset));
                this.pointsList.add(linePart + 1, new Point(centerX + loopHeight, centerY + startOffset));
                this.pointsList.add(linePart + 1, new Point(centerX + loopHeight, centerY + endOffset));
                this.pointsList.add(linePart + 1, new Point(centerX, centerY + endOffset));
            } else {
                // vertical lines
                if (startPoint.x < endPoint.x) {
                    startOffset = +5;
                    endOffset = -5;
                }
                this.pointsList.add(linePart + 1, new Point(centerX + startOffset, centerY));
                this.pointsList.add(linePart + 1, new Point(centerX + startOffset, centerY + loopHeight));
                this.pointsList.add(linePart + 1, new Point(centerX + endOffset, centerY + loopHeight));
                this.pointsList.add(linePart + 1, new Point(centerX + endOffset, centerY));
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LineRecord other = (LineRecord) obj;
            if ((this.lineIdSring == null) ? (other.lineIdSring != null) : !this.lineIdSring.equals(other.lineIdSring)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + (this.lineIdSring != null ? this.lineIdSring.hashCode() : 0);
            return hash;
        }
    }

    public void getIntersectsEntity() {
    }

    public void getOverlapsOtherLine() {
    }

    private Point[] getIntersections(LineRecord localLineRecord) {
        HashSet<Point> intersectionPoints = new HashSet<Point>();
        for (LineRecord lineRecord : lineRecords) {
            int currentHorizontal = lineRecord.getFirstHorizontal();
            while (currentHorizontal > -1) {
                System.out.println("currentHorizontal: " + currentHorizontal);
                currentHorizontal = lineRecord.getNextHorizontal(currentHorizontal);
            }

            Point intersectionPoint = localLineRecord.getIntersection(lineRecord);
            if (lineRecord != null) {
                intersectionPoints.add(intersectionPoint);
            }
        }
        return intersectionPoints.toArray(new Point[]{});
    }

    public Point[] adjustLineToObstructions(String lineIdString, ArrayList<Point> pointsList) {
        LineRecord localLineRecord = new LineRecord(lineIdString, pointsList);
        getIntersections(localLineRecord);
        //localLineRecord.insertLoop(3);
        lineRecords.add(localLineRecord);
        return localLineRecord.pointsList.toArray(new Point[]{});
    }
}
