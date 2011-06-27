package nl.mpi.kinnate.rest;

/**
 *  Document   : GetKin
 *  Created on : Jun 21, 2011, 11:55:37 AM
 *  Author     : Peter Withers
 */
//import javax.ejb.EJB;
//import javax.ejb.Stateless;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import nl.mpi.kinnate.export.PedigreePackageExport;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kintypestrings.KinTermGroup;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter;
import nl.mpi.kinnate.kintypestrings.ParserHighlight;
import nl.mpi.kinnate.svg.DataStoreSvg;

//@Stateless
@Path("/getkin")
public class GetKin {

//    @EJB
//    private EntityStorageBean entityStorage;
//    @Path("/kintypes/{kintypeStrings: [a-zA-Z0-9]}")
    private EntityData[] getEntityNodes(List<String> kintypeStrings) {
        KinTypeStringConverter graphData = new KinTypeStringConverter();
        graphData.readKinTypes(kintypeStrings.toArray(new String[]{}), /*graphPanel.getkinTermGroups()*/ new KinTermGroup[]{}, new DataStoreSvg(), new ParserHighlight[kintypeStrings.size()]);
        return graphData.getDataNodes();
    }

    @GET
    @Produces("text/html")
    @Path("/view")
    public String getHtml(@QueryParam("kts") List<String> kintypeStrings) {
        StringBuilder stringBuilder = new StringBuilder();
        EntityData[] entiryData = getEntityNodes(kintypeStrings);
        stringBuilder.append("<table>");
        for (EntityData entityData : entiryData) {
            stringBuilder.append("<tr><td>");
            stringBuilder.append(entityData.getUniqueIdentifier());
            stringBuilder.append("</td><td>");
            stringBuilder.append(entityData.getSymbolType());
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
        PedigreePackageExport packageExport = new PedigreePackageExport();
        stringBuilder.append(packageExport.createCsvContents(entiryData));
        stringBuilder.append("</pre>");
        return "<html><body>" + kintypeStrings + "<p>" + stringBuilder + "</p></body></html>";
    }

    @GET
    @Produces("text/csv")
    @Path("/csv")
    public String getXml(@QueryParam("kts") List<String> kintypeStrings) {
        PedigreePackageExport packageExport = new PedigreePackageExport();
        return packageExport.createCsvContents(getEntityNodes(kintypeStrings));
    }
}
