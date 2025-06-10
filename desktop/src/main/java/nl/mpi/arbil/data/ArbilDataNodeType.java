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
package nl.mpi.arbil.data;

import nl.mpi.flap.model.DataNodeType;

/**
 * Created on : Feb 18, 2013, 3:37:27 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class ArbilDataNodeType extends DataNodeType {

//    enum ImdiTypes {
//
//        imdicorpus,
//        imdisession,
//        imdicatalogue,
//        imdiresource,
//        rootnode,
//        containernode,
//        childnode,
//        directory
//    }

    static public DataNodeType getTypeForNode(ArbilDataNode arbilDataNode) {
        final DataNodeType dataNodeType = new DataNodeType();
//        if (arbilDataNode.isCmdiMetaDataNode()) {
//            final ArbilTemplate template = arbilDataNode.getNodeTemplate();
//            if (template instanceof CmdiTemplate) {
//                final CmdiTemplate cmdiTemplate = (CmdiTemplate) template;
//                dataNodeType.setName(cmdiTemplate.getTemplateName());
////                dataNodeType.setID(cmdiTemplate.getTe());
//            }
//        } else {
//            if (arbilDataNode.isCatalogue()) {
//                dataNodeType.setName(ImdiTypes.imdicatalogue.name());
//            } else if (arbilDataNode.isChildNode()) {
//                dataNodeType.setName(ImdiTypes.childnode.name());
//            } else if (arbilDataNode.isContainerNode()) {
//                dataNodeType.setName(ImdiTypes.containernode.name());
//            } else if (arbilDataNode.isCorpus()) {
//                dataNodeType.setName(ImdiTypes.imdicorpus.name());
//            } else if (arbilDataNode.isDirectory()) {
//                dataNodeType.setName(ImdiTypes.directory.name());
//            } else if (arbilDataNode.isSession()) {
//                dataNodeType.setName(ImdiTypes.imdisession.name());
//            } else if (arbilDataNode.hasResource()) {
//                dataNodeType.setName(ImdiTypes.imdiresource.name());
//            }
////        rootnode
//        }
//        final CmdiTemplate cmdiTemplate = (CmdiTemplate) template;
        dataNodeType.setName(arbilDataNode.getNodeTemplate().getTemplateName());
        return dataNodeType;
    }
}
