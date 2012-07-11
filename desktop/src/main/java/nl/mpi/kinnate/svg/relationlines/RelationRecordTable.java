package nl.mpi.kinnate.svg.relationlines;

import java.util.Collection;
import java.util.HashMap;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.RelationTypeDefinition;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.svg.OldFormatException;

/**
 * Document : RelationRecordTable
 * Created on : Jun 29, 2012, 7:11:48 PM
 * Author : Peter Withers
 */
public class RelationRecordTable {

    HashMap<String, RelationRecord> recordStore = new HashMap<String, RelationRecord>();
    LineLookUpTable lineLookUpTable;

    public void addRecord(GraphPanel graphPanel, int relationLineIndex, EntityData leftEntity, EntityData rightEntity, DataTypes.RelationType directedRelation, int lineWidth, int lineDash, RelationTypeDefinition.CurveLineOrientation curveLineOrientation, String lineColour, String lineLabel, int hSpacing, int vSpacing) throws OldFormatException {
        RelationRecord relationRecord = new RelationRecord(graphPanel, relationLineIndex, leftEntity, rightEntity, directedRelation, lineWidth, lineDash, curveLineOrientation, lineColour, lineLabel, hSpacing, vSpacing);
        recordStore.put(relationRecord.lineIdString, relationRecord);
    }

    public RelationRecord getRecord(String idString) {
        return recordStore.get(idString);
    }

    public Collection<RelationRecord> getAllRecords() {
        return recordStore.values();
    }

    public int size() {
        return recordStore.size();
    }

    public void adjustLines() throws OldFormatException {
        lineLookUpTable = new LineLookUpTable();
        for (RelationRecord relationRecord : recordStore.values()) {
            relationRecord.updatePathPoints(lineLookUpTable);
        }
        lineLookUpTable.separateOverlappingLines();
        lineLookUpTable.addLoops();
    }
}
