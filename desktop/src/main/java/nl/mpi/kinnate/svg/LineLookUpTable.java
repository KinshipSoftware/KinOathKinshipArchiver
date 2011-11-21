package nl.mpi.kinnate.svg;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *  Document   : LineLookUpTable
 *  Created on : Sep 30, 2011, 3:24:55 PM
 *  Author     : Peter Withers
 */
public class LineLookUpTable {

    // this hashset keeps one line record for line part for each pair of entities, the line segments might be updated when an entity is dragged
    // todo: there will probably be multiple line parts for each pari of entities: start segment, end segment, main line and maybe some zig zag bits, even if these zig zag bits are not ued they probably should always be there for simplicity
    HashSet<LineRecord> lineRecords = new HashSet<LineRecord>();

    protected class LineRecord {

        public LineRecord(String lineIdString, ArrayList<Point> pointsList) {
            this.lineIdSring = lineIdString;
            this.pointsList = pointsList;
        }
        private String lineIdSring;
        private ArrayList<Point> pointsList;

        protected Point getIntersection(LineRecord lineRecord) {
            return null; //Point((lineRecord.startPoint.x + lineRecord.endPoint.x) / 2, (lineRecord.startPoint.y + lineRecord.endPoint.y) / 2);
            // todo: get the actual intersections and insert loops
            // todo: in RelationSVG on first load the lineLookUpTable is null and loops will not be drawn
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

    public Point[] getIntersections(LineRecord localLineRecord) {
        HashSet<Point> intersectionPoints = new HashSet<Point>();
        for (LineRecord lineRecord : lineRecords) {
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
        //localLineRecord.pointsList.set(3, new Point(0, 0));
        lineRecords.add(localLineRecord);
        return localLineRecord.pointsList.toArray(new Point[]{});
    }
}
