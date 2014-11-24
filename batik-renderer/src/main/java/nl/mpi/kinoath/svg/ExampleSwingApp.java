/*
 * Copyright (C) 2014 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.kinoath.svg;

import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.JFrame;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityData.SymbolType;
import nl.mpi.kinnate.kindata.RelationTypeDefinition;
import nl.mpi.kinnate.kindata.UnsortablePointsException;
import nl.mpi.kinnate.svg.DiagramSettings;
import nl.mpi.kinnate.svg.EntitySvg;
import nl.mpi.kinnate.svg.OldFormatException;
import nl.mpi.kinnate.svg.SvgDiagram;
import nl.mpi.kinnate.svg.SvgUpdateHandler;
import nl.mpi.kinoath.graph.DefaultSorter;
import org.apache.batik.swing.JSVGCanvas;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.svg.SVGDocument;

/**
 * @since Nov 24, 2014 7:55:16 PM (creation date)
 * @author Peter Withers
 */
public class ExampleSwingApp {

    private final DiagramScrollPanel diagramScrollPanel;
    protected final JSVGCanvas svgCanvas = new JSVGCanvas();
    private static final String RHOMBUS = "rhombus";

    public ExampleSwingApp() throws DOMException, IOException, OldFormatException, UnsortablePointsException, AbstractMethodError {
        svgCanvas.setDocument(getSVG());
        this.diagramScrollPanel = new DiagramScrollPanel(svgCanvas);
        svgCanvas.setEnableImageZoomInteractor(true);
        svgCanvas.setEnablePanInteractor(true);
        svgCanvas.setEnableRotateInteractor(true);
        svgCanvas.setEnableZoomInteractor(true);
    }

    public DiagramScrollPanel getDiagramScrollPanel() {
        return diagramScrollPanel;
    }

    public JSVGCanvas getSvgCanvas() {
        return svgCanvas;
    }

    private EntityData[] getEntityNodes() {
        final EntityData entityData1 = new EntityData(1, new String[]{"A"}, SymbolType.circle, false, null, null);
        final EntityData entityData2 = new EntityData(2, new String[]{"B"}, SymbolType.square, false, null, null);
        final EntityData entityData3 = new EntityData(3, new String[]{"C"}, SymbolType.triangle, false, null, null);
        entityData1.addRelatedNode(entityData3, DataTypes.RelationType.ancestor, null, null, null, null);
        entityData1.addRelatedNode(entityData2, DataTypes.RelationType.ancestor, null, null, null, null);
//        entityData1.isVisible = true;
        return new EntityData[]{entityData1, entityData2, entityData3};
    }

    public Document getSVG() throws IOException, DOMException, OldFormatException, UnsortablePointsException {
        EntityData[] entiryData = getEntityNodes();
        final EventListener eventListener = new EventListener() {

            @Override
            public void handleEvent(Event evt) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        final EntitySvg entitySvg = new EntitySvg(eventListener);
        SvgDiagram svgDiagram = new SvgDiagram(new DiagramSettings() {

            @Override
            public String defaultSymbol() {
                return RHOMBUS;
            }

            @Override
            public boolean showIdLabels() {
                return true;
            }

            @Override
            public boolean showLabels() {
                return true;
            }

            @Override
            public boolean showKinTypeLabels() {
                return true;
            }

            @Override
            public boolean showDateLabels() {
                return true;
            }

            @Override
            public boolean showExternalLinks() {
                return true;
            }

            @Override
            public boolean highlightRelationLines() {
                return true;
            }

            @Override
            public boolean snapToGrid() {
                return true;
            }

            @Override
            public boolean showDiagramBorder() {
                return true;
            }

            @Override
            public boolean showSanguineLines() {
                return true;
            }

            @Override
            public boolean showKinTermLines() {
                return true;
            }

            @Override
            public RelationTypeDefinition[] getRelationTypeDefinitions() {
                return new DataTypes().getReferenceRelations();
            }

            @Override
            public void storeAllData(SVGDocument doc) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        }, entitySvg);
        svgDiagram.generateDefaultSvg(eventListener, new DefaultSorter());
        final SvgUpdateHandler svgUpdateHandler = new SvgUpdateHandler(svgDiagram);
        svgDiagram.graphData.setEntitys(entiryData);
        svgUpdateHandler.drawEntities(new Rectangle(800, 600));
//        printNodeNames(svgDiagram.getDoc().getRootElement());
        return svgDiagram.getDoc();
    }

    public static void main(String[] args) throws AbstractMethodError, DOMException, HeadlessException, IOException, OldFormatException, UnsortablePointsException {
        // Create a new JFrame.
        JFrame jFrame = new JFrame("KinOath Swing Example: shift+l=pan, shift+r=zoom, ctrl+l=box, ctrl+r=rotate");
        final ExampleSwingApp exampleSwingApp = new ExampleSwingApp();

        // Add components to the frame.
        jFrame.getContentPane().add(exampleSwingApp.getDiagramScrollPanel());

        // Display the frame.
        jFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        jFrame.setSize(800, 400);
        jFrame.setVisible(true);
    }
}
