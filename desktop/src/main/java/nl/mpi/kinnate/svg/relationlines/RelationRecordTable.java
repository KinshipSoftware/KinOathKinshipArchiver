package nl.mpi.kinnate.svg.relationlines;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.kindata.RelationTypeDefinition;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.svg.OldFormatException;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : RelationRecordTable
 * Created on : Jun 29, 2012, 7:11:48 PM
 * Author : Peter Withers
 */
public class RelationRecordTable {

    HashMap<String, RelationRecord> recordStore = new HashMap<String, RelationRecord>();
    LineLookUpTable lineLookUpTable;
    ArrayList<String> doneRelations = new ArrayList<String>();

    public void addRecord(GraphPanel graphPanel, EntityData entityData, EntityRelation entityRelation, int hSpacing, int vSpacing, int lineWidth) throws OldFormatException {
        // make directed and exclude any lines that are already done
        DataTypes.RelationType directedRelation = entityRelation.getRelationType();
        EntityData leftEntity;
        EntityData rightEntity;
        if (entityRelation.getRelationType() == DataTypes.RelationType.descendant) {
            // make sure the ancestral relations are unidirectional
            directedRelation = DataTypes.RelationType.ancestor;
            leftEntity = entityRelation.getAlterNode();
            rightEntity = entityData;
        } else if (entityRelation.getRelationType() == DataTypes.RelationType.ancestor) {
            // make sure the ancestral relations are unidirectional
            leftEntity = entityData;
            rightEntity = entityRelation.getAlterNode();
        } else if (entityData.getUniqueIdentifier().getQueryIdentifier().compareTo(entityRelation.getAlterNode().getUniqueIdentifier().getQueryIdentifier()) > 0) {
            // make sure all other relations are directed by the string sort order so that they can be made unique
            leftEntity = entityRelation.getAlterNode();
            rightEntity = entityData;
        } else {
            // make sure all other relations are directed by the string sort order so that they can be made unique
            leftEntity = entityData;
            rightEntity = entityRelation.getAlterNode();
        }
        String compoundIdentifier;
        if (directedRelation == DataTypes.RelationType.ancestor) {
            compoundIdentifier = "commonparent:" + leftEntity.getUniqueIdentifier().getQueryIdentifier() + directedRelation.name() + ":" + entityRelation.dcrType + ":" + entityRelation.customType;
        } else {
            compoundIdentifier = leftEntity.getUniqueIdentifier().getQueryIdentifier() + rightEntity.getUniqueIdentifier().getQueryIdentifier() + directedRelation.name() + ":" + entityRelation.dcrType + ":" + entityRelation.customType;
        }
        // make sure each equivalent relation is drawn only once
        if (!doneRelations.contains(compoundIdentifier)) {
            boolean skipSiblingRelation = false;
            String groupId = getGroupId(entityData, entityRelation);
            if (entityRelation.getRelationType() == DataTypes.RelationType.sibling) {
                String siblingGroupId = getGroupId(entityData, entityRelation);
                // do not draw lines for siblings if the common parent is visible because the ancestor lines will take the place of the sibling lines
                skipSiblingRelation = groupId.equals(siblingGroupId);
            }
            if (!skipSiblingRelation) {
                doneRelations.add(compoundIdentifier);
                String lineColour = entityRelation.lineColour;
                RelationTypeDefinition.CurveLineOrientation curveLineOrientation = RelationTypeDefinition.CurveLineOrientation.horizontal;
                int lineDash = 0;
                if (lineColour == null) {
                    for (RelationTypeDefinition relationTypeDefinition : graphPanel.dataStoreSvg.getRelationTypeDefinitions()) {
                        if (relationTypeDefinition.matchesType(entityRelation)) {
                            lineColour = relationTypeDefinition.getLineColour();
                            lineWidth = relationTypeDefinition.getLineWidth();
                            curveLineOrientation = relationTypeDefinition.getCurveLineOrientation();
                            lineDash = relationTypeDefinition.getLineDash();
                            break;
                        }
                    }
                }
                RelationRecord relationRecord = new RelationRecord(groupId, graphPanel, this.size(), leftEntity, rightEntity, directedRelation, lineWidth, lineDash, curveLineOrientation, lineColour, entityRelation.labelString, hSpacing, vSpacing);
                recordStore.put(relationRecord.lineIdString, relationRecord);
            }
        }
    }

    public String getGroupId(EntityData currentNode, EntityRelation graphLinkNode) {
//        System.out.println("ego: " + graphLinkNode.getRelationType() + " : " + currentNode.getLabel()[0].toString());
        if (!DataTypes.isSanguinLine(graphLinkNode.getRelationType())) {
            // group ids do not apply to non sangune relations
            return null;
        }
        ArrayList<String> parentIdList = new ArrayList<String>(); // we use a string here so that it can be sorted consistently, the array list is used because any number of parents could exist
        if (graphLinkNode.getRelationType() == DataTypes.RelationType.union) {
            // get the common parent id based on the union
            // todo: could this cause issues when there are three or more parents to one child?
            parentIdList.add(currentNode.getUniqueIdentifier().getAttributeIdentifier());
//            System.out.println("P1: " + currentNode.getLabel()[0]);
            if (!parentIdList.contains(graphLinkNode.alterUniqueIdentifier.getAttributeIdentifier())) {
                parentIdList.add(graphLinkNode.alterUniqueIdentifier.getAttributeIdentifier());
//                System.out.println("P2: " + graphLinkNode.getAlterNode().getLabel()[0]);
            }
        } else {
            // generate the id based on the ancestors of the entity
            for (EntityRelation egosRelation : currentNode.getAllRelations()) {
                if (egosRelation.getRelationType() == DataTypes.RelationType.ancestor) {
                    if (egosRelation.getAlterNode() != null && egosRelation.getAlterNode().isVisible) {
                        if (!parentIdList.contains(egosRelation.alterUniqueIdentifier.getAttributeIdentifier())) {
                            parentIdList.add(egosRelation.alterUniqueIdentifier.getAttributeIdentifier());
//                            System.out.println("P3: " + egosRelation.getAlterNode().getLabel()[0]);
                        }
                    }
                }
            }
        }
        if (parentIdList.isEmpty()) {
            return null;
        } else {
            Collections.sort(parentIdList);
//            System.out.println("getGroupId: " + parentIdList.toString());
            return parentIdList.toString();
        }
    }

    public RelationRecord getRecord(String idString) {
        return recordStore.get(idString);
    }

    public Collection<RelationRecord> getAllRecords() {
        return recordStore.values();
    }

    public ArrayList<RelationRecord> getRecordsForSelection(ArrayList<UniqueIdentifier> selectedIdentifiers) {
        ArrayList<RelationRecord> returnRecords = new ArrayList<RelationRecord>();
        for (RelationRecord relationRecord : recordStore.values()) {
            if (relationRecord.pertainsToEntity(selectedIdentifiers)) {
                returnRecords.add(relationRecord);
            }
        }
        return returnRecords;
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
