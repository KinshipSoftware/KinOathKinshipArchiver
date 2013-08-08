/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.kintypestrings;

import nl.mpi.kinnate.kindata.EntityData;

/**
 *  Document   : KinTermCalculator
 *  Created on : Oct 28, 2011, 3:17:13 PM
 *  Author     : Peter Withers
 */
public class KinTermCalculator {

    public void insertKinTerms(EntityData[] graphNodeArray, KinTermGroup[] kinTermsArray) {
// todo: finish migrating the kin terms code here
//        for (EntityData grapgNode : graphNodeArray) {
//            if (grapgNode.isEgo) {
//                for (KinTermGroup kinTermGroup : kinTermsArray) {
//                        if (kinTermGroup.graphShow) {
//                    for (KinTerm kinTerm : kinTermGroup.getKinTerms()) {
//                            // todo: replace this with a loop over egoDataNodeList and then calculate the actual kin type strings for each one rather than the user entered ones used here
//                            // todo: this should probably be done after all nodes have been created so that subsequent relations are taken into account when calculating the kin terms
////                        for (String kinTermLabel : kinTermGroup.getTermLabel(fullKinTypeString)) {
////                            // todo: this could be running too many times, maybe check for efficiency
////                            // todo maybe move this to the last point and search for the kin rather than looping for each
////                            currentGraphDataNode.addKinTermString(kinTermLabel, kinTermGroup.graphColour);
////                            egoDataNode.addRelatedNode(currentGraphDataNode, DataTypes.RelationType.none, DataTypes.RelationLineType.kinTermLine, kinTermGroup.graphColour, kinTermLabel);
////                        }
//                        }
//                    }
//                }
//            }
//        }

    }
}
