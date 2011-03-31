package nl.mpi.kinnate.svg;

import org.apache.batik.bridge.UpdateManager;

/**
 *  Document   : DragHandler
 *  Created on : Mar 31, 2011, 12:52:12 PM
 *  Author     : Peter Withers
 */
public class SvgUpdateHandler {

    GraphPanel graphPanel;

    protected SvgUpdateHandler(GraphPanel graphPanelLocal) {
        graphPanel = graphPanelLocal;
    }

    protected void updateDragNode(final int updateDragNodeX, final int updateDragNodeY) {
        UpdateManager updateManager = graphPanel.svgCanvas.getUpdateManager();
        updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {

            public void run() {
                System.out.println("updateDragNodeX: " + updateDragNodeX);
                System.out.println("updateDragNodeY: " + updateDragNodeY);
                if (graphPanel.doc != null) {
                    for (String entityId : graphPanel.selectedGroupId) {
                        new EntitySvg().moveEntity(graphPanel.doc, entityId, updateDragNodeX, updateDragNodeY);
                    }
//                    Element entityGroup = doc.getElementById("EntityGroup");
//                    for (Node currentChild = entityGroup.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
//                        if ("g".equals(currentChild.getLocalName())) {
//                            Node idAttrubite = currentChild.getAttributes().getNamedItem("id");
//                            if (idAttrubite != null) {
//                                String entityPath = idAttrubite.getTextContent();
//                                if (selectedGroupElement.contains(entityPath)) {
//                                    SVGRect bbox = ((SVGLocatable) currentChild).getBBox();
////                    ((SVGLocatable) currentDraggedElement).g
//                                    // drageboth x and y
////                                    ((Element) currentChild).setAttribute("transform", "translate(" + String.valueOf(updateDragNodeX * svgCanvas.getRenderingTransform().getScaleX() - bbox.getX()) + ", " + String.valueOf(updateDragNodeY - bbox.getY()) + ")");
//                                    // limit drag to x only
//                                    ((Element) currentChild).setAttribute("transform", "translate(" + String.valueOf(updateDragNodeX * svgCanvas.getRenderingTransform().getScaleX() - bbox.getX()) + ", 0)");
////                    updateDragNodeElement.setAttribute("x", String.valueOf(updateDragNodeX));
////                    updateDragNodeElement.setAttribute("y", String.valueOf(updateDragNodeY));
//                                    //                    SVGRect bbox = ((SVGLocatable) currentDraggedElement).getBBox();
////                    System.out.println("bbox X: " + bbox.getX());
////                    System.out.println("bbox Y: " + bbox.getY());
////                    System.out.println("bbox W: " + bbox.getWidth());
////                    System.out.println("bbox H: " + bbox.getHeight());
////                    todo: look into transform issues when dragging ellements eg when the canvas is scaled or panned
////                            SVGLocatable.getTransformToElement()
////                            SVGPoint.matrixTransform()
//                                }
//                            }
//                        }
//                    }
                    int vSpacing = graphPanel.graphPanelSize.getVerticalSpacing(graphPanel.graphData.gridHeight);
                    int hSpacing = graphPanel.graphPanelSize.getHorizontalSpacing(graphPanel.graphData.gridWidth);
                    new RelationSvg().updateRelationLines(graphPanel.doc, graphPanel.selectedGroupId, graphPanel.svgNameSpace, hSpacing, vSpacing);
                    //new CmdiComponentBuilder().savePrettyFormatting(doc, new File("/Users/petwit/Documents/SharedInVirtualBox/mpi-co-svn-mpi-nl/LAT/Kinnate/trunk/src/main/resources/output.svg"));
                }
            }
        });
    }
}
