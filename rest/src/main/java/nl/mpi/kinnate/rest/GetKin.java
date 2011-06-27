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
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
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
    @GET
    @Produces("text/html")
    @Path("/csv")
    public String getXml(@QueryParam("kts") List<String> kintypeStrings) {
        KinTypeStringConverter graphData = new KinTypeStringConverter();
//        String kinTypeStrings = "EmB|EmZ|EmM|EmF|EmS|EmD";
        graphData.readKinTypes(kintypeStrings.toArray(new String[]{}), /*graphPanel.getkinTermGroups()*/ new KinTermGroup[]{}, new DataStoreSvg(), new ParserHighlight[kintypeStrings.size()]);
//                graphPanel.drawNodes(graphData);
        StringBuilder stringBuilder = new StringBuilder();
        for (EntityData entityData : graphData.getDataNodes()) {
            stringBuilder.append(entityData.getUniqueIdentifier());
            stringBuilder.append(",");
            stringBuilder.append(entityData.getSymbolType());
            stringBuilder.append(",");
            for (String labelString : entityData.getLabel()) {
                stringBuilder.append(labelString);
                stringBuilder.append(",");
            }
            for (String labelString : entityData.getKinTypeStringArray()) {
                stringBuilder.append(labelString);
                stringBuilder.append(",");
            }
            stringBuilder.append("<br>");
        }
        return "<html><body>" + kintypeStrings + "<p>" + stringBuilder + "</p></body></html>";
//        return "<html><body><p>" + kintypeStrings + "</p></body></html>"; //entityStorage.getName()
    }

    @PUT
    @Consumes("text/plain")
    public void putXml(String content) {
//        entityStorage.setName(content);
    }
}
