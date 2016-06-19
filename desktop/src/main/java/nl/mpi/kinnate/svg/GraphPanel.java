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
package nl.mpi.kinnate.svg;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.IndexerParameters;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.GraphSorter;
import nl.mpi.kinnate.kindata.UnsortablePointsException;
import nl.mpi.kinnate.kintypestrings.KinTermGroup;
import nl.mpi.kinnate.kintypestrings.KinType;
import nl.mpi.kinnate.ui.GraphPanelContextMenu;
import nl.mpi.kinnate.ui.KinDiagramPanel;
import nl.mpi.kinnate.ui.MetadataPanel;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import nl.mpi.kinoath.graph.DefaultSorter;
import nl.mpi.kinoath.svg.DiagramScrollPanel;
import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.dom.svg.SVGOMPoint;
import org.apache.batik.dom.util.SAXIOException;
import org.apache.batik.swing.JSVGCanvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGLocatable;

/**
 * Document : GraphPanel Created on : Aug 16, 2010, 5:31:33 PM
 *
 * @ author Peter Withers
 */
public class GraphPanel extends JPanel implements SavePanel {

    private final static Logger logger = LoggerFactory.getLogger(GraphPanel.class);
    private final DiagramScrollPanel diagramScrollPanel;
    protected JSVGCanvas svgCanvas;
    public MetadataPanel metadataPanel;
    private boolean requiresSave = false;
    private File svgFile = null;
    protected ArrayList<UniqueIdentifier> selectedGroupId;
    private final SvgDiagram svgDiagram;
    public DataStoreSvg dataStoreSvg;
//    private URI[] egoPathsTemp = null;
    public final SvgUpdateHandler svgUpdateHandler;
    public MouseListenerSvg mouseListenerSvg;
    final private ArbilWindowManager dialogHandler;
    final private ArbilDataNodeLoader dataNodeLoader;
    final private SessionStorage sessionStorage;
//    private EntityCollection entityCollection;
    final KinDiagramPanel kinDiagramPanel;
    private SymbolGraphic symbolGraphic;

    public GraphPanel(KinDiagramPanel kinDiagramPanel, final ArbilWindowManager arbilWindowManager, SessionStorage sessionStorage, ArbilDataNodeLoader dataNodeLoader) {
        this.kinDiagramPanel = kinDiagramPanel;
        this.dialogHandler = arbilWindowManager;
        this.sessionStorage = sessionStorage;
        this.dataNodeLoader = dataNodeLoader;
//        this.entityCollection = entityCollection;
        dataStoreSvg = new DataStoreSvg();
        mouseListenerSvg = new MouseListenerSvg(kinDiagramPanel, this, sessionStorage, dialogHandler);
        svgDiagram = new SvgDiagram(dataStoreSvg, new EntitySvg());
        dataStoreSvg.setDefaults();
        svgUpdateHandler = new SvgUpdateHandler(svgDiagram);
        selectedGroupId = new ArrayList<UniqueIdentifier>();
        this.setLayout(new BorderLayout());
        boolean eventsEnabled = true;
        boolean selectableText = false;
        svgCanvas = new JSVGCanvas(new GraphUserAgent(this, dialogHandler, dataNodeLoader), eventsEnabled, selectableText);
//        svgCanvas.setMySize(new Dimension(600, 400));
        svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
//        drawNodes();
        svgCanvas.setEnableImageZoomInteractor(false);
        svgCanvas.setEnablePanInteractor(false);
        svgCanvas.setEnableRotateInteractor(false);
        svgCanvas.setEnableZoomInteractor(false);
        svgCanvas.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(final MouseWheelEvent e) {
                UpdateManager updateManager = svgCanvas.getUpdateManager();
                if (updateManager != null) {
                    updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                        public void run() {
                            double scale = 1 - e.getUnitsToScroll() / 10.0;
                            double tx = -e.getX() * (scale - 1);
                            double ty = -e.getY() * (scale - 1);
//                                System.out.println("scale: " + scale);
//                                System.out.println("scale: " + svgCanvas.getRenderingTransform().getScaleX());
                            AffineTransform at = new AffineTransform();
                            if (e.isAltDown()) {
                                at.translate(tx, ty);
                                at.scale(scale, scale);
                            } else if (e.isShiftDown()) {
                                at.translate(-tx, 0);
                            } else {
                                at.translate(0, -ty);
                            }
                            at.concatenate(svgCanvas.getRenderingTransform());
//                                System.out.println("new scale: " + at.getScaleX());
                            if (at.getScaleX() > 0.1) {
                                svgCanvas.setRenderingTransform(at);
                            }
                        }
                    });
                }
                e.consume();
                //  show a ToolTip or StatusBar to give hints "Hold modifier + mouse wheel to zoom"
                GraphPanel.this.kinDiagramPanel.setStatusBarText("hint: alt + mouse wheel to zoom; shift + mouse wheel to pan; mouse wheel to scroll.");
            }
        });
//        svgCanvas.setEnableResetTransformInteractor(true);
//        svgCanvas.setDoubleBufferedRendering(true); // todo: look into reducing the noticable aliasing on the canvas

        diagramScrollPanel = new DiagramScrollPanel(svgCanvas);
//        svgCanvas.setBackground(Color.LIGHT_GRAY);
        this.add(BorderLayout.CENTER, diagramScrollPanel);
    }

    public void setEntityCollection(EntityCollection entityCollection) {
        mouseListenerSvg.setEntityCollection(entityCollection);
        svgCanvas.addMouseListener(mouseListenerSvg);
        svgCanvas.addMouseMotionListener(mouseListenerSvg);
        svgCanvas.setComponentPopupMenu(new GraphPanelContextMenu(kinDiagramPanel, this, entityCollection, dialogHandler, dataNodeLoader, sessionStorage));
    }
//    private void zoomDrawing() {
//        AffineTransform scaleTransform = new AffineTransform();
//        scaleTransform.scale(1 - currentZoom / 10.0, 1 - currentZoom / 10.0);
//        System.out.println("currentZoom: " + currentZoom);
////        svgCanvas.setRenderingTransform(scaleTransform);
//        Rectangle canvasBounds = this.getBounds();
//        SVGRect bbox = ((SVGLocatable) svgDiagram.doc.getRootElement()).getBBox();
//        if (bbox != null) {
//            System.out.println("previousZoomedWith: " + bbox.getWidth());
//        }
////        SVGElement rootElement = svgDiagram.doc.getRootElement();
////        if (currentWidth < canvasBounds.width) {
//        float drawingCenter = (currentWidth / 2);
////                float drawingCenter = (bbox.getX() + (bbox.getWidth() / 2));
//        float canvasCenter = (canvasBounds.width / 2);
//        zoomAffineTransform = new AffineTransform();
//        zoomAffineTransform.translate((canvasCenter - drawingCenter), 1);
//        zoomAffineTransform.concatenate(scaleTransform);
//        svgCanvas.setRenderingTransform(zoomAffineTransform);
//    }

    public void setArbilTableModel(MetadataPanel metadataPanel) {
        this.metadataPanel = metadataPanel;
    }

    public void readSvg(URI svgFilePath, boolean savableType) {
        if (savableType) {
            svgFile = new File(svgFilePath);
        } else {
            svgFile = null;
        }
        try {
            svgDiagram.readSvg(svgFilePath.toString());
            svgCanvas.setDocument(((KinDocumentImpl)svgDiagram.doc).getDoc());
            symbolGraphic = new SymbolGraphic(((KinDocumentImpl)svgDiagram.doc).getDoc());
            dataStoreSvg = DataStoreSvg.loadDataFromSvg(((KinDocumentImpl)svgDiagram.doc).getDoc());
            if (dataStoreSvg.indexParameters == null) {
                dataStoreSvg.setDefaults();
            }
            requiresSave = false;
            dataStoreSvg.indexParameters.symbolFieldsFields.setAvailableValues(svgDiagram.entitySvg.listSymbolNames(((KinDocumentImpl)svgDiagram.doc), this.svgDiagram.svgNameSpace));
//            if (dataStoreSvg.graphData == null) {
//                return null;
//            }
        } catch (SAXIOException exception) {
            dialogHandler.addMessageDialogToQueue("Cannot open the diagram: " + exception.getMessage(), "Open Diagram");
        } catch (IOException exception) {
            dialogHandler.addMessageDialogToQueue("Cannot open the diagram: " + exception.getMessage(), "Open Diagram");
        }
//        svgCanvas.setSVGDocument(doc);
        return; // dataStoreSvg.graphData.getDataNodes();
    }

    public void generateDefaultSvg() {
        try {
            svgDiagram.generateDefaultSvg(new DefaultSorter());
            dataStoreSvg.indexParameters.symbolFieldsFields.setAvailableValues(svgDiagram.entitySvg.listSymbolNames(((KinDocumentImpl)svgDiagram.doc), svgDiagram.svgNameSpace));
            svgCanvas.setSVGDocument(((KinDocumentImpl)svgDiagram.doc).getDoc());
            symbolGraphic = new SymbolGraphic(((KinDocumentImpl)svgDiagram.doc).getDoc());
        } catch (IOException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        }
    }

    private void saveSvg(File svgFilePath) throws SaveExeption {
        try {
            svgFile = svgFilePath;
            selectedGroupId.clear();
            svgUpdateHandler.clearHighlights();
            // make sure that any data changes such as the title/description in the kin term groups get updated into the file on save
            dataStoreSvg.storeAllData(((KinDocumentImpl)svgDiagram.doc).getDoc());
            // set up input and output
            DOMSource dOMSource = new DOMSource(((KinDocumentImpl)svgDiagram.doc).getDoc());
            FileOutputStream fileOutputStream = new FileOutputStream(svgFile);
            StreamResult xmlOutput = new StreamResult(fileOutputStream);
            // configure transformer
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(dOMSource, xmlOutput);
            xmlOutput.getOutputStream().close();
            requiresSave = false;
        } catch (IOException exception) {
            logger.warn("failed to save svg", exception);
            throw new SaveExeption(exception.getMessage());
        } catch (IllegalArgumentException exception) {
            logger.warn("failed to save svg", exception);
            throw new SaveExeption(exception.getMessage());
        } catch (TransformerException exception) {
            logger.warn("failed to save svg", exception);
            throw new SaveExeption(exception.getMessage());
        } catch (TransformerFactoryConfigurationError exception) {
            logger.warn("failed to save svg", exception);
            throw new SaveExeption(exception.getMessage());
        }
    }

    public void captureDiagramSvg() throws SaveExeption {
        try {
            if (svgFile == null) {
                logger.warn("Digram must be saved before screenshots can be made.");
                throw new SaveExeption("Digram must be saved before screenshots can be made.");
            }
            // set up input and output
            DOMSource dOMSource = new DOMSource(((KinDocumentImpl)svgDiagram.doc).getDoc());
            final File captureSvgFile = new File(svgFile.getParentFile(), svgFile.getName().substring(0, svgFile.getName().length() - 4) + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".svg");
            FileOutputStream fileOutputStream = new FileOutputStream(captureSvgFile);
            StreamResult xmlOutput = new StreamResult(fileOutputStream);
            // configure transformer
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(dOMSource, xmlOutput);
            xmlOutput.getOutputStream().close();
            requiresSave = false;
        } catch (IOException exception) {
            logger.warn("failed to save svg", exception);
            throw new SaveExeption(exception.getMessage());
        } catch (IllegalArgumentException exception) {
            logger.warn("failed to save svg", exception);
            throw new SaveExeption(exception.getMessage());
        } catch (TransformerException exception) {
            logger.warn("failed to save svg", exception);
            throw new SaveExeption(exception.getMessage());
        } catch (TransformerFactoryConfigurationError exception) {
            logger.warn("failed to save svg", exception);
            throw new SaveExeption(exception.getMessage());
        }
    }

    private void printNodeNames(Node nodeElement) {
        System.out.println(nodeElement.getLocalName());
        System.out.println(nodeElement.getNamespaceURI());
        Node childNode = nodeElement.getFirstChild();
        while (childNode != null) {
            printNodeNames(childNode);
            childNode = childNode.getNextSibling();
        }
    }

    public DiagramSettings getDiagramSettings() {
        return dataStoreSvg;
    }

    public String[] getKinTypeStrigs() {
        return dataStoreSvg.kinTypeStrings;
    }

    public void setKinTypeStrigs(String[] kinTypeStringArray) {
        // strip out any white space, blank lines and remove duplicates
        // this has set has been removed because it creates a discrepancy between what the user types and what is processed
//        HashSet<String> kinTypeStringSet = new HashSet<String>();
//        for (String kinTypeString : kinTypeStringArray) {
//            if (kinTypeString != null && kinTypeString.trim().length() > 0) {
//                kinTypeStringSet.add(kinTypeString.trim());
//            }
//        }
//        dataStoreSvg.kinTypeStrings = kinTypeStringSet.toArray(new String[]{});
        dataStoreSvg.kinTypeStrings = kinTypeStringArray;
    }

    public SvgDiagram getSVGDocument() {
        return svgDiagram;
    }

    public IndexerParameters getIndexParameters() {
        return dataStoreSvg.indexParameters;
    }

    public KinTermGroup[] getkinTermGroups() {
        return dataStoreSvg.kinTermGroups.toArray(new KinTermGroup[0]);
    }

    public KinTermGroup addKinTermGroup() {
        final KinTermGroup kinTermGroup = new KinTermGroup();
        dataStoreSvg.kinTermGroups.add(kinTermGroup);
        return kinTermGroup;
    }

    public void deleteKinTermGroup(KinTermGroup kinTermGroup) {
        dataStoreSvg.kinTermGroups.remove(kinTermGroup);
    }

    public void panToSelected(UniqueIdentifier[] targetIdentifiers) {
        Rectangle selectionSize = null;
        for (UniqueIdentifier currentIdentifier : targetIdentifiers) {
            Point currentPoint = svgDiagram.entitySvg.getEntityLocationOffset(currentIdentifier);
            if (currentPoint != null) {
                if (selectionSize == null) {
                    selectionSize = new Rectangle(currentPoint.x, currentPoint.y, 1, 1);
                } else {
                    selectionSize.add(currentPoint);
                }
            }
        }
        final Rectangle selectionRect = selectionSize;
//        if (selectionRect != null) {
//            System.out.println("selectionRect: " + selectionRect.toString());
//            addTestRect(selectionRect, 0);
//        }
        Rectangle renderRectScreen = svgCanvas.getBounds();
//        System.out.println("getBounds: "+graphPanel.svgCanvas.getBounds().toString());
//        System.out.println("getRenderRect: "+graphPanel.svgCanvas.getRenderRect().toString());
//        System.out.println("getVisibleRect: "+graphPanel.svgCanvas.getVisibleRect().toString());
//        System.out.println("renderRectScreen:" + renderRectScreen.toString());

        Element labelGroup = svgDiagram.doc.getElementById("LabelsGroup");
        final SVGLocatable labelGroupLocatable = (SVGLocatable) labelGroup;
        // todo: should this be moved into the svg thread?
        final Rectangle renderRectDocument = svgUpdateHandler.getRectOnDocument(renderRectScreen, labelGroupLocatable);
//        System.out.println("renderRectDocument: " + renderRectDocument);

//        SVGOMPoint pointOnDocument = getPointOnDocument(new Point(0, 0), labelGroupLocatable);
//        renderRect.translate((int) pointOnDocument.getX(), (int) pointOnDocument.getY());
//        addTestRect(renderRectDocument, 1);
        if (selectionRect != null && selectionRect != null && !renderRectDocument.contains(selectionRect)) {
            UpdateManager updateManager = svgCanvas.getUpdateManager();
            if (updateManager != null) {
                updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                    public void run() {
                        SVGLocatable diagramGroupLocatable = (SVGLocatable) svgDiagram.doc.getElementById("DiagramGroup");
                        final double scaleFactor = diagramGroupLocatable.getScreenCTM().getA();
//                        final double scaleFactor = graphPanel.svgCanvas.getRenderingTransform().getScaleX();
//                        System.out.println("scaleFactor: " + scaleFactor);
                        AffineTransform at = new AffineTransform();
                        final double offsetX = renderRectDocument.getCenterX() - selectionRect.getCenterX();
                        final double offsetY = renderRectDocument.getCenterY() - selectionRect.getCenterY();
//                        System.out.println("offset: " + offsetX + ":" + offsetY);
//                        SVGOMPoint offsetOnScreen = getPointOnDocument(new Point((int) offsetX, (int) offsetY), labelGroupLocatable);
//                        System.out.println("screen offset: " + offsetOnScreen.getX() + " : " + offsetOnScreen.getY());
//                        at.translate(offsetOnScreen.getX(), offsetOnScreen.getY());
                        at.translate((offsetX / 2) * scaleFactor, (offsetY / 2) * scaleFactor);
//                        at.scale(scaleFactor, scaleFactor);
                        at.concatenate(svgCanvas.getRenderingTransform());
                        svgCanvas.setRenderingTransform(at);
                        //... at.concatenate(diagramGroupLocatable.getTransformToElement(null));
                        //... graphPanel.svgCanvas.setRenderingTransform(at);
                    }
                });
            }
        }
    }

    public void addTestRect(final Rectangle testRect, int rectangleID) {
        UpdateManager updateManager = svgCanvas.getUpdateManager();
        final String rectangleName = "SelectionBorder" + rectangleID;
        if (updateManager != null) {
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                public void run() {
                    System.out.println("selectionRect: " + testRect);
                    Element pageBorderNode = svgDiagram.doc.getElementById(rectangleName);
                    if (pageBorderNode == null) {
                        Element labelGroup = svgDiagram.doc.getElementById("LabelsGroup");
                        pageBorderNode = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "rect");
                        pageBorderNode.setAttribute("id", rectangleName);
                        pageBorderNode.setAttribute("fill", "none");
                        pageBorderNode.setAttribute("x", Float.toString(testRect.x - 20));
                        pageBorderNode.setAttribute("y", Float.toString(testRect.y - 20));
                        pageBorderNode.setAttribute("width", Float.toString(testRect.width + 40));
                        pageBorderNode.setAttribute("height", Float.toString(testRect.height + 40));
                        pageBorderNode.setAttribute("stroke-width", "1");
                        pageBorderNode.setAttribute("stroke", "green");
                        labelGroup.appendChild(pageBorderNode);
                    }
                    pageBorderNode.setAttribute("x", Float.toString(testRect.x - 20));
                    pageBorderNode.setAttribute("y", Float.toString(testRect.y - 20));
                    pageBorderNode.setAttribute("width", Float.toString(testRect.width + 40));
                    pageBorderNode.setAttribute("height", Float.toString(testRect.height + 40));
                    System.out.println("pageBorderNode:" + pageBorderNode);
                }
            });
        }
    }

    public void updateMouseDot(final Point currentLocation) {
//        this is only used to test the screen to document transform
        UpdateManager updateManager = svgCanvas.getUpdateManager();
        if (updateManager != null) {
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                public void run() {
                    Element labelGroup = svgDiagram.doc.getElementById("LabelsGroup");
                    Element mouseDotElement = svgDiagram.doc.getElementById("MouseDot");
                    if (mouseDotElement == null) {
                        mouseDotElement = svgDiagram.doc.createElementNS(svgDiagram.svgNameSpace, "circle");
                        mouseDotElement.setAttribute("id", "MouseDot");
                        mouseDotElement.setAttribute("r", "5");
                        mouseDotElement.setAttribute("fill", "blue");
                        mouseDotElement.setAttribute("stroke", "none");
                        labelGroup.appendChild(mouseDotElement);
                    }
                    SVGLocatable labelGroupLocatable = (SVGLocatable) labelGroup;
                    SVGOMPoint pointOnDocument = svgUpdateHandler.getPointOnDocument(currentLocation, labelGroupLocatable);
                    mouseDotElement.setAttribute("cx", Float.toString(pointOnDocument.getX()));
                    mouseDotElement.setAttribute("cy", Float.toString(pointOnDocument.getY()));
                }
            });
        }
    }

    protected void dragCanvas(int updateDragNodeXLocal, int updateDragNodeYLocal) {
        AffineTransform at = new AffineTransform();
        at.translate(updateDragNodeXLocal, updateDragNodeYLocal);
        at.concatenate(svgCanvas.getRenderingTransform());
//        System.out.println("offset: " + at.getTranslateX());
        svgCanvas.setRenderingTransform(at);
    }

    private boolean relationThreadRunning = false;

    protected void updateDragRelation(final int updateDragNodeXLocal, final int updateDragNodeYLocal) {
//        System.out.println("updateDragRelation: " + updateDragNodeXLocal + " : " + updateDragNodeYLocal);
        UpdateManager updateManager = svgCanvas.getUpdateManager();
        synchronized (svgUpdateHandler) {
            if (!relationThreadRunning) {
                relationThreadRunning = true;
                updateManager.getUpdateRunnableQueue().invokeLater(
                        new Runnable() {
                            public void run() {
                                svgUpdateHandler.updateMouseDrag(selectedGroupId, updateDragNodeXLocal, updateDragNodeYLocal);
                                synchronized (svgUpdateHandler) {
                                    relationThreadRunning = false;
                                }
                            }
                        }
                );
            }
        }
    }

    public void removeSelectionRect() {
        UpdateManager updateManager = svgCanvas.getUpdateManager();
        if (updateManager != null) {
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                public void run() {
                    svgUpdateHandler.removeSelectionRectA();
                }
            });
        }
    }

    private boolean threadRunning = false;

    protected void updateDragNode(final int updateDragNodeXLocal, final int updateDragNodeYLocal) {
        UpdateManager updateManager = svgCanvas.getUpdateManager();
        synchronized (svgUpdateHandler) {
            if (!threadRunning) {
                threadRunning = true;
                updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                    public void run() {

                        final Rectangle panelBounds = svgCanvas.getBounds();
                        svgUpdateHandler.updateDragNodeI(selectedGroupId, updateDragNodeXLocal, updateDragNodeYLocal, panelBounds);
                        synchronized (svgUpdateHandler) {
                            threadRunning = false;
                        }
                        setRequiresSave();
                    }
                });
            }
        }
    }

    public void updateCanvasSize(final boolean resetZoom) {
        UpdateManager updateManager = this.svgCanvas.getUpdateManager();
        if (updateManager != null) {
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                public void run() {
                    final Rectangle panelBounds = svgCanvas.getBounds();
                    svgUpdateHandler.updateCanvasSizeI(resetZoom, panelBounds);
                }
            });
        }
    }

    public void addGraphics(final SvgUpdateHandler.GraphicsTypes graphicsType, final Point locationOnScreen) {
        UpdateManager updateManager = svgCanvas.getUpdateManager();
        if (updateManager != null) {
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                public void run() {
                    final Rectangle panelBounds = svgCanvas.getBounds();
                    svgUpdateHandler.addGraphicsI(graphicsType, locationOnScreen, mouseListenerSvg, panelBounds);
                }
            });
        }
    }

    public void deleteGraphics(final UniqueIdentifier uniqueIdentifier) {
        UpdateManager updateManager = svgCanvas.getUpdateManager();
        if (updateManager != null) {
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                public void run() {
                    svgUpdateHandler.deleteGraphicsI(uniqueIdentifier);
                    setRequiresSave();
                }
            });
        }
    }

    protected void drawSelectionRect(final Point startLocation, final Point currentLocation) {
        UpdateManager updateManager = svgCanvas.getUpdateManager();
        if (updateManager != null) {
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                public void run() {
                    svgUpdateHandler.drawSelectionRectI(startLocation, currentLocation);
                }
            });
        }
    }
//    public String[] getEgoUniquiIdentifiersList() {
//        return dataStoreSvg.egoIdentifierSet.toArray(new String[]{});
//    }
//    public String[] getEgoIdList() {
//        return dataStoreSvg.egoIdentifierSet.toArray(new String[]{});
//    }
//    public URI[] getEgoPaths() {
//        if (egoPathsTemp != null) {
//            return egoPathsTemp;
//        }
//        ArrayList<URI> returnPaths = new ArrayList<URI>();
//        for (String egoId : dataStoreSvg.egoIdentifierSet) {
//            try {
//                String entityPath = getPathForElementId(egoId);
////                if (entityPath != null) {
//                returnPaths.add(new URI(entityPath));
////                }
//            } catch (URISyntaxException ex) {
//                GuiHelper.linorgBugCatcher.logError(ex);
//                // todo: warn user with a dialog
//            }
//        }
//        return returnPaths.toArray(new URI[]{});
//    }
//    public void setRequiredEntities(URI[] egoPathArray, String[] egoIdentifierArray) {
////        egoPathsTemp = egoPathArray; // egoPathsTemp is only required if the ego nodes are not already on the graph (otherwise the path can be obtained from the graph elements)
//        dataStoreSvg.requiredEntities = new HashSet<String>(Arrays.asList(egoIdentifierArray));
//    }
//
//    public void addRequiredEntity(URI[] egoPathArray, String[] egoIdentifierArray) {
////        egoPathsTemp = egoPathArray; // egoPathsTemp is only required if the ego nodes are not already on the graph (otherwise the path can be obtained from the graph elements)
//        dataStoreSvg.requiredEntities.addAll(Arrays.asList(egoIdentifierArray));
//    }
//    public void removeEgo(String[] egoIdentifierArray) {
//        dataStoreSvg.egoIdentifierSet.removeAll(Arrays.asList(egoIdentifierArray));
//    }

    protected void updateSvgSelectionHighlights() {
        if (kinDiagramPanel != null) {
            kinDiagramPanel.setStatusBarText(selectedGroupId.size() + " selected of " + kinDiagramPanel.getGraphEntities().length + "");
            String kinTypeStrings = "";
            for (UniqueIdentifier entityID : selectedGroupId) {
                if (kinTypeStrings.length() != 0) {
                    kinTypeStrings = kinTypeStrings + KinType.separator;
                }
                kinTypeStrings = kinTypeStrings + getKinTypeForElementId(entityID);
            }
            if (kinTypeStrings != null) {
                kinDiagramPanel.setSelectedKinTypeSting(kinTypeStrings);
            }
        }
        UpdateManager updateManager = svgCanvas.getUpdateManager();
        if (updateManager != null) { // todo: there may be issues related to the updateManager being null, this should be looked into if symptoms arise.
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                public void run() {
                    svgUpdateHandler.updateSvgSelectionHighlightsI(selectedGroupId, mouseListenerSvg);
                }
            });
        }
    }

    public void setSelectedIds(UniqueIdentifier[] uniqueIdentifiers) {
        selectedGroupId.clear();
        selectedGroupId.addAll(Arrays.asList(uniqueIdentifiers));
        updateSvgSelectionHighlights();
        // pan the diagram so that the selected are in the center
        panToSelected(uniqueIdentifiers);
//        mouseListenerSvg.updateSelectionDisplay();
    }

    public UniqueIdentifier[] getSelectedIds() {
        return selectedGroupId.toArray(new UniqueIdentifier[]{});
    }

    public EntityData getEntityForElementId(UniqueIdentifier uniqueIdentifier) {
        for (EntityData entityData : svgDiagram.graphData.getDataNodes()) {
            if (uniqueIdentifier.equals(entityData.getUniqueIdentifier())) {
                return entityData;
            }
        }
        return null;
    }

    public HashMap<UniqueIdentifier, EntityData> getEntitiesById(UniqueIdentifier[] uniqueIdentifiers) {
        ArrayList<UniqueIdentifier> identifierList = new ArrayList<UniqueIdentifier>(Arrays.asList(uniqueIdentifiers));
        HashMap<UniqueIdentifier, EntityData> returnMap = new HashMap<UniqueIdentifier, EntityData>();
        for (EntityData entityData : svgDiagram.graphData.getDataNodes()) {
            if (identifierList.contains(entityData.getUniqueIdentifier())) {
                returnMap.put(entityData.getUniqueIdentifier(), entityData);
            }
        }
        return returnMap;
    }

//    public boolean selectionContainsEgo() {
//        for (String selectedId : selectedGroupId) {
//            if (dataStoreSvg.egoIdentifierSet.contains(selectedId)) {
//                return true;
//            }
//        }
//        return false;
//    }
//    public String getPathForElementId(UniqueIdentifier elementId) {
////        NamedNodeMap namedNodeMap = svgDiagram.doc.getElementById(elementId).getAttributes();
////        for (int attributeCounter = 0; attributeCounter < namedNodeMap.getLength(); attributeCounter++) {
////            System.out.println(namedNodeMap.item(attributeCounter).getNodeName());
////            System.out.println(namedNodeMap.item(attributeCounter).getNamespaceURI());
////            System.out.println(namedNodeMap.item(attributeCounter).getNodeValue());
////        }
//        Element entityElement = svgDiagram.doc.getElementById(elementId.getAttributeIdentifier());
//        if (entityElement == null) {
//            return null;
//        } else {
//            return entityElement.getAttributeNS(DataStoreSvg.svgDiagram.kinDataNameSpaceLocation, "path");
//        }
//    }
    public String getKinTypeForElementId(UniqueIdentifier elementId) {
        Element entityElement = svgDiagram.doc.getElementById(elementId.getAttributeIdentifier());
        if (entityElement != null) {
            return entityElement.getAttributeNS(SvgDiagram.kinDataNameSpaceLocation, "kintype");
        } else {
            return "";
        }
    }

    public Dimension2D getDiagramSize() {
        return svgCanvas.getSVGDocumentSize();
//    Element svgRoot = svgDiagram.doc.getDocumentElement();
//    String widthString = svgRoot.getAttribute("width");
//    String heightString = svgRoot.getAttribute("height");
//    return new Point(Integer.parseInt(widthString), Integer.parseInt(widthString));
    }

    public void resetZoom() {
        updateCanvasSize(true);
    }

    public void resetZoom(boolean resetZoom) {
        System.out.println("resetZoom: " + resetZoom);
        if (resetZoom) {
            AffineTransform at = new AffineTransform();
            at.scale(1, 1);
            at.setToTranslation(1, 1);
            this.svgCanvas.setRenderingTransform(at);
        }
    }

    public void resetLayout(boolean resetZoom) {
        // this requires that the entity data is loaded by recalculating the diagram at least once
        svgDiagram.entitySvg.discardEntityPositions();
        svgDiagram.graphData.clearPreferredEntityLocations();
        svgDiagram.graphData.setEntitys(svgDiagram.graphData.getDataNodes());
        try {
            svgDiagram.graphData.placeAllNodes(svgDiagram.entitySvg.entityPositions);
            drawNodes(resetZoom);
        } catch (UnsortablePointsException exception) {
            dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Error, the graph is unsortable.");
        }
    }

    public UniqueIdentifier[] getDiagramUniqueIdentifiers() {
        return svgDiagram.entitySvg.entityPositions.keySet().toArray(new UniqueIdentifier[0]);
    }

    public void clearEntityLocations(UniqueIdentifier[] selectedIdentifiers) {
        // all entity locations are now stored as preferred locations when the graph sorter completes
//        // change the entities stored location into a preferred location rather than a fixed location
//        for (UniqueIdentifier uniqueIdentifier : selectedIdentifiers) {
//            final Point entityLocation = svgDiagram.entitySvg.getEntityLocation(uniqueIdentifier);
//            if (entityLocation != null) {
//                dataStoreSvg.graphData.setPreferredEntityLocation(new UniqueIdentifier[]{uniqueIdentifier}, entityLocation);
//            }
//        }
        svgDiagram.entitySvg.clearEntityLocations(selectedIdentifiers);
    }

    public void drawNodes(boolean resetZoom) {
        requiresSave = true;
        selectedGroupId.clear();
        updateEntities(resetZoom);
    }

    public void updateEntities(final boolean resetZoom) {
        UpdateManager updateManager = svgCanvas.getUpdateManager();
        if (updateManager != null) {
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                public void run() {
                    try {
                        final Rectangle panelBounds = svgCanvas.getBounds();
                        svgUpdateHandler.drawEntities(panelBounds);
                        resetZoom(resetZoom);
                    } catch (DOMException exception) {
                        BugCatcherManager.getBugCatcher().logError(exception);
                        dialogHandler.addMessageDialogToQueue(exception.getMessage(), "SVG Error");
                    } catch (OldFormatException exception) {
                        dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Old or erroneous format detected");
                    } catch (UnsortablePointsException exception) {
                        dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Error, the graph is unsortable.");
                    }
                    // todo: this repaint might not resolve all cases of redraw issues
                    svgCanvas.repaint(); // make sure no remnants are left over after the last redraw
                }
            });
        } else {
            try {   // on the first draw there will be on update manager
                final Rectangle panelBounds = svgCanvas.getBounds();
                svgUpdateHandler.drawEntities(panelBounds);
                resetLayout(resetZoom);
            } catch (DOMException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                dialogHandler.addMessageDialogToQueue(exception.getMessage(), "SVG Error");
            } catch (OldFormatException exception) {
                dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Old or erroneous format detected");
            } catch (UnsortablePointsException exception) {
                dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Error, the graph is unsortable.");
            }// todo: this repaint might not resolve all cases of redraw issues
            svgCanvas.repaint(); // make sure no remnants are left over after the last redraw
        }
    }

    public void drawNodes(GraphSorter graphDataLocal, boolean resetZoom) {
        kinDiagramPanel.setStatusBarText(graphDataLocal.getDataNodes().length + " entities shown");
        svgDiagram.graphData = graphDataLocal;
        drawNodes(resetZoom);
        if (graphDataLocal.getDataNodes().length == 0) {
            // if all entities have been removed then reset the zoom so that new nodes are going to been centered
            // todo: it would be better to move the window to cover the drawing area but not change the zoom
//            resetZoom();
        }
    }

    public boolean hasSaveFileName() {
        return svgFile != null;
    }

    public File getFileName() {
        return svgFile;
    }

    public boolean requiresSave() {
        return requiresSave;
    }

    public void setRequiresSave() {
        requiresSave = true;
    }

    public void saveToFile() throws SaveExeption {
        saveSvg(svgFile);
    }

    public void saveToFile(File saveAsFile) throws SaveExeption {
        saveSvg(saveAsFile);
    }

    public void updateGraph() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void doActionCommand(MouseListenerSvg.ActionCode actionCode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void showSettings() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public GraphPanel getGraphPanel() {
        return this;
    }

    public SymbolGraphic getSymbolGraphic() {
        return symbolGraphic;
    }
}
