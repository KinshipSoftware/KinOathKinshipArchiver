package nl.mpi.kinnate.svg.relationlines;

import java.util.Collection;
import java.util.HashMap;

/**
 * Document : RelationRecordTable
 * Created on : Jun 29, 2012, 7:11:48 PM
 * Author : Peter Withers
 */
public class RelationRecordTable {

    HashMap<String, RelationRecord> recordStore = new HashMap<String, RelationRecord>();

    public void addRecord(RelationRecord relationRecord) {
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
}
