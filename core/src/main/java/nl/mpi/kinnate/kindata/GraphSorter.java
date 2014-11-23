/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for
 * Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.kindata;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.bind.annotation.XmlElement;
import nl.mpi.kinnate.svg.GraphPanelSize;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : GraphData Created on : Sep 11, 2010, 4:51:36 PM
 *
 * @author Peter Withers
 */
public abstract class GraphSorter {

    static int sortCounter = 0; // for testing only
    @XmlElement(name = "Entity", namespace = "http://mpi.nl/tla/kin")
    public EntityData[] graphDataNodeArray = new EntityData[]{};
    public HashMap<UniqueIdentifier, Point> preferredLocations = new HashMap<UniqueIdentifier, Point>();
    // todo: should these padding vars be stored in the svg, currently they are stored
    public int xPadding = 100; // todo sort out one place for this var
    public int yPadding = 100; // todo sort out one place for this var
//    private boolean requiresRedraw = false;
//    , int hSpacing, int vSpacing
//

    public void setPadding(GraphPanelSize graphPanelSize) {
        xPadding = graphPanelSize.getHorizontalSpacing();
        yPadding = graphPanelSize.getVerticalSpacing();
    }

    public void clearPreferredEntityLocations() {
        preferredLocations = new HashMap<UniqueIdentifier, Point>();
    }

    public void setPreferredEntityLocation(UniqueIdentifier[] egoIdentifierArray, Point defaultLocation) {
        if (preferredLocations == null) {
            preferredLocations = new HashMap<UniqueIdentifier, Point>();
        }
        for (UniqueIdentifier uniqueIdentifier : egoIdentifierArray) {
            preferredLocations.put(uniqueIdentifier, new Point(defaultLocation));
        }
    }

    public Point getDefaultPosition(HashMap<UniqueIdentifier, Point> entityPositions, UniqueIdentifier uniqueIdentifier) {
        if (preferredLocations != null) {
            Point preferredPoint = preferredLocations.get(uniqueIdentifier);
            if (preferredPoint != null) {
                return preferredPoint;
            }
        }
        Rectangle rectangle = getGraphSize(entityPositions);
        Point defaultPosition = new Point(rectangle.x + rectangle.width + xPadding, rectangle.y + rectangle.height + yPadding);
//                            Point defaultPosition = new Point(rectangle.width, rectangle.height);
        return new Point(defaultPosition.x, 0);
//        return new Point(0, 0);
    }

    public void setEntitys(EntityData[] graphDataNodeArrayLocal) {
        graphDataNodeArray = graphDataNodeArrayLocal;
    }

//    public boolean isResizeRequired() {
//        boolean returnBool = requiresRedraw;
//        requiresRedraw = false;
//        return returnBool;
//    }
//    private void placeRelatives(EntityData currentNode, ArrayList<EntityData> intendedSortOrder, HashMap<String, Float[]> entityPositions) {
//        for (EntityRelation entityRelation : currentNode.getVisiblyRelateNodes()) {
//            EntityData relatedEntity = entityRelation.getAlterNode();
//
//        }
//    }
    public Rectangle getGraphSize(HashMap<UniqueIdentifier, Point> entityPositions) {
        // get min positions
        // this should also take into account any graphics such as labels, although the border provided should be adequate, in other situations the page size could be set, in which case maybe an align option would be helpful
        int[] minPostion = null;
        int[] maxPostion = null;
        for (Point currentPosition : entityPositions.values()) {
            if (minPostion == null) {
                minPostion = new int[]{Math.round(currentPosition.x), Math.round(currentPosition.y)};
                maxPostion = new int[]{Math.round(currentPosition.x), Math.round(currentPosition.y)};
            } else {
                minPostion[0] = Math.min(minPostion[0], Math.round(currentPosition.x));
                minPostion[1] = Math.min(minPostion[1], Math.round(currentPosition.y));
                maxPostion[0] = Math.max(maxPostion[0], Math.round(currentPosition.x));
                maxPostion[1] = Math.max(maxPostion[1], Math.round(currentPosition.y));
            }
        }
        if (minPostion == null) {
            // when there are no entities this could be null and must be corrected here
            minPostion = new int[]{0, 0};
            maxPostion = new int[]{0, 0};
        }
        int xOffset = minPostion[0] - xPadding;
        int yOffset = minPostion[1] - yPadding;
        int graphWidth = maxPostion[0] + xPadding - xOffset;
        int graphHeight = maxPostion[1] + yPadding - yOffset;
        return new Rectangle(xOffset, yOffset, graphWidth, graphHeight);
    }

    abstract public void placeAllNodes(HashMap<UniqueIdentifier, Point> entityPositions) throws UnsortablePointsException;

//    // todo: look into http://www.jgraph.com/jgraph.html
//    // todo: and http://books.google.nl/books?id=diqHjRjMhW0C&pg=PA138&lpg=PA138&dq=SVGDOMImplementation+add+namespace&source=bl&ots=IuqzAz7dsz&sig=e5FW_B1bQbhnth6i2rifalv2LuQ&hl=nl&ei=zYpnTYD3E4KVOuPF2YoL&sa=X&oi=book_result&ct=result&resnum=3&ved=0CC0Q6AEwAg#v=onepage&q&f=false
//    // page 139 shows jgraph layout usage
//    // http://www.jgraph.com/doc/jgraph/com/jgraph/layout/hierarchical/JGraphHierarchicalLayout.html
//    // http://www.jgraph.com/doc/jgraph/com/jgraph/layout/JGraphLayout.html
//    // http://www.jgraph.com/doc/jgraph/com/jgraph/layout/JGraphFacade.html
//    // http://www.jgraph.com/doc/jgraph/org/jgraph/graph/GraphModel.html
//    // and maybe http://www.jgraph.com/doc/jgraph/org/jgraph/graph/GraphLayoutCache.html
//    // todo: lookinto:
////                    layouts.put("Hierarchical", new JGraphHierarchicalLayout());
////                layouts.put("Compound", new JGraphCompoundLayout());
////                layouts.put("CompactTree", new JGraphCompactTreeLayout());
////                layouts.put("Tree", new JGraphTreeLayout());
////                layouts.put("RadialTree", new JGraphRadialTreeLayout());
////                layouts.put("Organic", new JGraphOrganicLayout());
////                layouts.put("FastOrganic", new JGraphFastOrganicLayout());
////                layouts.put("SelfOrganizingOrganic", new JGraphSelfOrganizingOrganicLayout());
////                layouts.put("SimpleCircle", new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_CIRCLE));
////                layouts.put("SimpleTilt", new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_TILT));
////                layouts.put("SimpleRandom", new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_RANDOM));
////                layouts.put("Spring", new JGraphSpringLayout());
////                layouts.put("Grid", new SimpleGridLayout());
    public EntityData[] getDataNodes() {
        return graphDataNodeArray;
    }
}
