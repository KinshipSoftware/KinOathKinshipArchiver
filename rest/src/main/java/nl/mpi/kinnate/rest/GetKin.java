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
package nl.mpi.kinnate.rest;

/**
 *  Document   : GetKin
 *  Created on : Jun 21, 2011, 11:55:37 AM
 *  Author     : Peter Withers
 */
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import nl.mpi.kinnate.export.PedigreePackageExport;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.GraphSorter;
import nl.mpi.kinnate.kindata.RelationTypeDefinition;
import nl.mpi.kinnate.kintypestrings.KinType;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter;
import nl.mpi.kinnate.kintypestrings.ParserHighlight;
import nl.mpi.kinnate.svg.DiagramSettings;
import nl.mpi.kinnate.svg.EntitySvg;
import nl.mpi.kinnate.svg.OldFormatException;
import nl.mpi.kinnate.svg.SvgDiagram;
import nl.mpi.kinnate.svg.SvgUpdateHandler;
import nl.mpi.kinnate.ui.KinTypeStringProvider;
import org.w3c.dom.DOMException;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

//@Stateless
@Path("/getkin")
public class GetKin {

    private static final String RHOMBUS = "rhombus";

//    @EJB
//    private EntityStorageBean entityStorage;
//    @Path("/kintypes/{kintypeStrings: [a-zA-Z0-9]}")
    private EntityData[] getEntityNodes(final List<String> kintypeStrings) {
        KinTypeStringConverter graphData = new KinTypeStringConverter(RHOMBUS, KinType.getReferenceKinTypes());
        HashSet<String> kinTypeAllStrings = new HashSet<String>();
        // loop each and split any pipe chars | into lines
        for (String currentKinType : kintypeStrings) {
            kinTypeAllStrings.addAll(Arrays.asList(currentKinType.split("\\|")));
        }
        final ArrayList<KinTypeStringProvider> arrayList = new ArrayList<KinTypeStringProvider>();
        arrayList.add(new KinTypeStringProvider() {

            @Override
            public String[] getCurrentStrings() {
                return kintypeStrings.toArray(new String[kintypeStrings.size()]);
            }

            @Override
            public int getTotalLength() {
                return kintypeStrings.size();
            }

            @Override
            public void highlightKinTypeStrings(ParserHighlight[] parserHighlight, String[] kinTypeStrings) {
            }
        });
        graphData.readKinTypes(arrayList);
        return graphData.getDataNodes();
    }

    @GET
    @Produces("text/xml")
    @Path("/svg")
    public Document getSVG(@QueryParam("kts") List<String> kintypeStrings) throws IOException, DOMException, OldFormatException, GraphSorter.UnsortablePointsException {
        EntityData[] entiryData = getEntityNodes(kintypeStrings);
//        for (String kts : kintypeStrings) {
//            System.out.println("kts" + kts);
//        }
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
        svgDiagram.generateDefaultSvg(eventListener);
        final SvgUpdateHandler svgUpdateHandler = new SvgUpdateHandler(svgDiagram);
        svgDiagram.graphData.setEntitys(entiryData);
        svgUpdateHandler.drawEntities(new Rectangle(800, 600));
//        printNodeNames(svgDiagram.getDoc().getRootElement());
        return svgDiagram.getDoc();
    }

//    private void printNodeNames(Node nodeElement) {
//        System.out.println(nodeElement.getLocalName());
//        System.out.println(nodeElement.getNamespaceURI());
//        Node childNode = nodeElement.getFirstChild();
//        while (childNode != null) {
//            printNodeNames(childNode);
//            childNode = childNode.getNextSibling();
//        }
//    }

    @GET
    @Produces("text/html")
    @Path("/view") // todo: Ticket #1103 view fails
    public String getHtml(@QueryParam("kts") List<String> kintypeStrings) {
//        for (String kts : kintypeStrings) {
//            System.out.println("kts" + kts);
//        }
        StringBuilder stringBuilder = new StringBuilder();
        EntityData[] entiryData = getEntityNodes(kintypeStrings);
        stringBuilder.append("<table>");
        for (EntityData entityData : entiryData) {
            stringBuilder.append("<tr><td>");
            stringBuilder.append(entityData.getUniqueIdentifier().getAttributeIdentifier());
            stringBuilder.append("</td><td>");
            for (String symbol : entityData.getSymbolNames(RHOMBUS)) {
                stringBuilder.append(symbol);
            }
            stringBuilder.append("</td><td>");
            for (String labelString : entityData.getLabel()) {
                stringBuilder.append(labelString);
                stringBuilder.append("<br>");
            }
            stringBuilder.append("</td><td>");
            for (String labelString : entityData.getKinTypeStringArray()) {
                stringBuilder.append(labelString);
                stringBuilder.append("<br>");
            }
            stringBuilder.append("</td></tr>");
        }
        stringBuilder.append("</table>");
        stringBuilder.append("<pre>");
        PedigreePackageExport packageExport = new PedigreePackageExport(RHOMBUS);
        stringBuilder.append(packageExport.createCsvContents(entiryData));
        stringBuilder.append("</pre>");
        return "<html><body>" + kintypeStrings + "<p>" + stringBuilder + "</p></body></html>";
    }

    @GET
    @Produces("text/csv")
    @Path("/csv")
    public String getXml(@QueryParam("kts") List<String> kintypeStrings) {
        PedigreePackageExport packageExport = new PedigreePackageExport(RHOMBUS);
        return packageExport.createCsvContents(getEntityNodes(kintypeStrings));
    }
    // todo: Ticket #1088 Enable kin type string queries to be done via the rest interface (currently only transient kin type strings are enabled).
//    @Path("/kinput")
//    @Path("/kinstruct")
//    @Path("/kinhive")
//    @Path("/kinspace")
}
