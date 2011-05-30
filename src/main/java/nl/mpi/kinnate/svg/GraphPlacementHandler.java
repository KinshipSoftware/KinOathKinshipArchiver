package nl.mpi.kinnate.svg;

import nl.mpi.kinnate.kindata.EntityData;
import org.w3c.dom.Element;

/**
 *  Document   : GraphPlacementHandler
 *  Created on : May 30, 2011, 4:43:45 PM
 *  Author     : Peter Withers
 */
public class GraphPlacementHandler {

    public void placeAllNodes(GraphPanel graphPanel, EntityData[] allEntitys, Element entityGroupNode, int hSpacing, int vSpacing) {
        for (EntityData currentNode : allEntitys) {
            if (currentNode.isVisible) {
                entityGroupNode.appendChild(graphPanel.entitySvg.createEntitySymbol(graphPanel, currentNode, hSpacing, vSpacing));
            }
        }
    }
}
