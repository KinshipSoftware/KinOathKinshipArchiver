package nl.mpi.kinnate.kindata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 *  Document   : GraphData
 *  Created on : Sep 11, 2010, 4:51:36 PM
 *  Author     : Peter Withers
 */
public class GraphSorter {

    protected GraphDataNode[] graphDataNodeArray = new GraphDataNode[]{};
    public int gridWidth;
    public int gridHeight;

    public void setEgoNodes(GraphDataNode[] graphDataNodeArrayLocal) {
        graphDataNodeArray = graphDataNodeArrayLocal;
        sanguineSort();
        printLocations();
    }

    // todo: look into http://www.jgraph.com/jgraph.html
    // todo: and http://books.google.nl/books?id=diqHjRjMhW0C&pg=PA138&lpg=PA138&dq=SVGDOMImplementation+add+namespace&source=bl&ots=IuqzAz7dsz&sig=e5FW_B1bQbhnth6i2rifalv2LuQ&hl=nl&ei=zYpnTYD3E4KVOuPF2YoL&sa=X&oi=book_result&ct=result&resnum=3&ved=0CC0Q6AEwAg#v=onepage&q&f=false
    // page 139 shows jgraph layout usage
    // http://www.jgraph.com/doc/jgraph/com/jgraph/layout/hierarchical/JGraphHierarchicalLayout.html
    // http://www.jgraph.com/doc/jgraph/com/jgraph/layout/JGraphLayout.html
    // http://www.jgraph.com/doc/jgraph/com/jgraph/layout/JGraphFacade.html
    // http://www.jgraph.com/doc/jgraph/org/jgraph/graph/GraphModel.html
    // and maybe http://www.jgraph.com/doc/jgraph/org/jgraph/graph/GraphLayoutCache.html
    // todo: lookinto:
//                    layouts.put("Hierarchical", new JGraphHierarchicalLayout());
//                layouts.put("Compound", new JGraphCompoundLayout());
//                layouts.put("CompactTree", new JGraphCompactTreeLayout());
//                layouts.put("Tree", new JGraphTreeLayout());
//                layouts.put("RadialTree", new JGraphRadialTreeLayout());
//                layouts.put("Organic", new JGraphOrganicLayout());
//                layouts.put("FastOrganic", new JGraphFastOrganicLayout());
//                layouts.put("SelfOrganizingOrganic", new JGraphSelfOrganizingOrganicLayout());
//                layouts.put("SimpleCircle", new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_CIRCLE));
//                layouts.put("SimpleTilt", new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_TILT));
//                layouts.put("SimpleRandom", new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_RANDOM));
//                layouts.put("Spring", new JGraphSpringLayout());
//                layouts.put("Grid", new SimpleGridLayout());
    private void sanguineSubnodeSort(ArrayList<HashSet<GraphDataNode>> generationRows, HashSet<GraphDataNode> currentColumns, ArrayList<GraphDataNode> inputNodes, GraphDataNode currentNode) {
        int currentRowIndex = generationRows.indexOf(currentColumns);
        HashSet<GraphDataNode> ancestorColumns;
        HashSet<GraphDataNode> descendentColumns;
        if (currentRowIndex < generationRows.size() - 1) {
            descendentColumns = generationRows.get(currentRowIndex + 1);
        } else {
            descendentColumns = new HashSet<GraphDataNode>();
            generationRows.add(currentRowIndex + 1, descendentColumns);
        }
        if (currentRowIndex > 0) {
            ancestorColumns = generationRows.get(currentRowIndex - 1);
        } else {
            ancestorColumns = new HashSet<GraphDataNode>();
            generationRows.add(currentRowIndex, ancestorColumns);
        }
        for (GraphDataNode.EntityRelation relatedNode : currentNode.getVisiblyRelateNodes()) { // todo: here we are soriting only visible nodes, sorting invisible nodes as well might cause issues or might help the layout and this must be tested
            // todo: what happens here if there are multiple relations specified?
            if (inputNodes.contains(relatedNode.getAlterNode())) {
                HashSet<GraphDataNode> targetColumns;
                switch (relatedNode.relationType) {
                    case ancestor:
                        targetColumns = ancestorColumns;
                        break;
                    case sibling:
                        targetColumns = currentColumns;
                        break;
                    case descendant:
                        targetColumns = descendentColumns;
                        break;
                    case union:
                        targetColumns = currentColumns;
                        break;
                    case none:
                        // this would be a kin term or other so skip when sorting
                        targetColumns = null;
                        break;
                    default:
                        targetColumns = null;
                }
                if (targetColumns != null) {
                    inputNodes.remove(relatedNode.getAlterNode());
                    targetColumns.add(relatedNode.getAlterNode());
                    System.out.println("sorted: " + relatedNode.getAlterNode().getLabel() + " : " + relatedNode.relationType + " of " + currentNode.getLabel());
                    sanguineSubnodeSort(generationRows, targetColumns, inputNodes, relatedNode.getAlterNode());
                }
            }
        }
    }

    protected void sanguineSort() {
        System.out.println("calculateLocations");
        // create an array of rows
        ArrayList<HashSet<GraphDataNode>> generationRows = new ArrayList<HashSet<GraphDataNode>>();
        ArrayList<GraphDataNode> inputNodes = new ArrayList<GraphDataNode>();
        inputNodes.addAll(Arrays.asList(graphDataNodeArray));
        // put an array of columns into the current row
        HashSet<GraphDataNode> currentColumns = new HashSet<GraphDataNode>();
        generationRows.add(currentColumns);

        while (inputNodes.size() > 0) {
            GraphDataNode currentNode = inputNodes.remove(0);
            System.out.println("add as root node: " + currentNode.getLabel());
            currentColumns.add(currentNode);
            sanguineSubnodeSort(generationRows, currentColumns, inputNodes, currentNode);
        }
        gridWidth = 0;
        int yPos = 0;
        for (HashSet<GraphDataNode> currentRow : generationRows) {
            System.out.println("row: : " + yPos);
            if (currentRow.isEmpty()) {
                System.out.println("Skipping empty row");
            } else {
                int xPos = 0;
                if (gridWidth < currentRow.size()) {
                    gridWidth = currentRow.size();
                }
                for (GraphDataNode graphDataNode : currentRow) {
                    System.out.println("updating: " + xPos + " : " + yPos + " : " + graphDataNode.getLabel());
                    graphDataNode.yPos = yPos;
                    graphDataNode.xPos = xPos;
                    xPos++;
                }
                yPos++;
            }
        }
        System.out.println("gridWidth: " + gridWidth);
        gridHeight = yPos;
        sortByLinkDistance();
        sortByLinkDistance();
    }

    private void sortByLinkDistance() {
        GraphDataNode[][] graphGrid = new GraphDataNode[gridHeight][gridWidth];
        for (GraphDataNode graphDataNode : graphDataNodeArray) {
            graphGrid[graphDataNode.yPos][graphDataNode.xPos] = graphDataNode;
        }
        for (GraphDataNode graphDataNode : graphDataNodeArray) {
            int relationCounter = 0;
            int totalPositionCounter = 0;
            for (GraphDataNode.EntityRelation graphLinkNode : graphDataNode.getVisiblyRelateNodes()) {
                relationCounter++;
                totalPositionCounter += graphLinkNode.getAlterNode().xPos;
                //totalPositionCounter += Math.abs(graphLinkNode.linkedNode.xPos - graphLinkNode.sourceNode.xPos);
//                totalPositionCounter += Math.abs(graphLinkNode.linkedNode.xPos - graphLinkNode.sourceNode.xPos);
                System.out.println("link: " + graphLinkNode.getAlterNode().xPos + ":" + graphLinkNode.getAlterNode().xPos);
                System.out.println("totalPositionCounter: " + totalPositionCounter);
            }
            if (relationCounter > 0) {
                int averagePosition = totalPositionCounter / relationCounter;
                while (averagePosition < gridWidth - 1 && graphGrid[graphDataNode.yPos][averagePosition] != null) {
                    averagePosition++;
                }
                while (averagePosition > 0 && graphGrid[graphDataNode.yPos][averagePosition] != null) {
                    averagePosition--;
                }
                if (graphGrid[graphDataNode.yPos][averagePosition] == null) {
                    graphGrid[graphDataNode.yPos][graphDataNode.xPos] = null;
                    graphDataNode.xPos = averagePosition; // todo: swap what ever is aready there
                    graphGrid[graphDataNode.yPos][graphDataNode.xPos] = graphDataNode;
                }
                System.out.println("averagePosition: " + averagePosition);
            }
        }
    }

    public GraphDataNode[] getDataNodes() {
        return graphDataNodeArray;
    }

    private void printLocations() {
        System.out.println("printLocations");
        for (GraphDataNode graphDataNode : graphDataNodeArray) {
            System.out.println("node: " + graphDataNode.xPos + ":" + graphDataNode.yPos);
            for (GraphDataNode.EntityRelation graphLinkNode : graphDataNode.getVisiblyRelateNodes()) {
                System.out.println("link: " + graphLinkNode.getAlterNode().xPos + ":" + graphLinkNode.getAlterNode().yPos);
            }
        }
    }
}
