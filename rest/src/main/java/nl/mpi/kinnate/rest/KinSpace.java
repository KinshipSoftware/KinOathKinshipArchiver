package nl.mpi.kinnate.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 *  Document   : KinSpace
 *  Created on : Jun 30, 2011, 11:27:15 AM
 *  Author     : Peter Withers
 */
@Path("/kinspace")
public class KinSpace {

    @GET
    @Produces("text/html")
    @Path("{workspaceName: [a-zA-Z0-9]*}")
    public String getHtml(@PathParam("workspaceName") String workspaceName) {
        return "rabbits for all " + workspaceName;
    }

    @Path("{workspaceName: [a-zA-Z0-9]*}")
    @PUT
    @Consumes("text/xml")
    public String putXml(@PathParam("workspaceName") String workspaceName, InputStream entityXmlStream) {
        StringBuilder putContents = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entityXmlStream, "UTF-8"));
            for (String responseLine = bufferedReader.readLine(); responseLine != null; responseLine = bufferedReader.readLine()) {
                putContents.append(responseLine);
                putContents.append("\n");
            }
            putContents.append("\n");
        } catch (IOException exception) {
            return exception.getMessage();
        }
        return "Workspace: " + workspaceName + " Received: " + putContents;
    }
}
