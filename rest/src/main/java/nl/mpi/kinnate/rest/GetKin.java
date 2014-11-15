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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import nl.mpi.kinnate.export.PedigreePackageExport;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kintypestrings.KinType;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter;
import nl.mpi.kinnate.kintypestrings.ParserHighlight;
import nl.mpi.kinnate.ui.KinTypeStringProvider;

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
    @Produces("text/html")
    @Path("/view") // todo: Ticket #1103 view fails
    public String getHtml(@QueryParam("kts") List<String> kintypeStrings) {
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
