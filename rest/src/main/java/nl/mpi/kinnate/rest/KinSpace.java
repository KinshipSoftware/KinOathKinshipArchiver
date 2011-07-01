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
import nl.mpi.kinnate.kinhive.HiveException;
import nl.mpi.kinnate.kinhive.HiveManager;

/**
 *  Document   : KinSpace
 *  Created on : Jun 30, 2011, 11:27:15 AM
 *  Author     : Peter Withers
 */
@Path("/kinspace")
public class KinSpace {

    @GET
    @Produces("text/html")
    @Path("")
    public String listWorkSpaces() {
        StringBuilder resultStringBuilder = new StringBuilder();
        resultStringBuilder.append("<b>Available Workspaces</b><br><p>");
        for (String currentWorkspace : new HiveManager().listWorkspaces()) {
            resultStringBuilder.append("<a href=/kinoath-rest/kinoath/kinspace/");
            resultStringBuilder.append(currentWorkspace);
            resultStringBuilder.append(">");
            resultStringBuilder.append(currentWorkspace);
            resultStringBuilder.append("</a>");
            resultStringBuilder.append("<br>");
        }
        resultStringBuilder.append("</p><br><a href=/kinoath-rest/kinoath/kinspace/sampleworkspace/create>create a sample workspace (change url to change the workspace name)</a>");
        return resultStringBuilder.toString();
    }

    @GET
    @Produces("text/html")
    @Path("{workspaceName: [a-zA-Z0-9]*}/create")
    public String createWorkspace(@PathParam("workspaceName") String workspaceName) {
        try {
            new HiveManager().createWorkspace(workspaceName);
            return "created workspace<br><a href=/kinoath-rest/kinoath/kinspace/>" + workspaceName + "</a>";
        } catch (HiveException exception) {
            return exception.getMessage();
        }

    }

    @GET
    @Produces("text/html")
    @Path("{workspaceName: [a-zA-Z0-9]*}")
    public String showWorkSpace(@PathParam("workspaceName") String workspaceName) {
        StringBuilder resultStringBuilder = new StringBuilder();
        resultStringBuilder.append("<b>Workspace: ");
        resultStringBuilder.append(workspaceName);
        resultStringBuilder.append("</b><br><p>");
        boolean filesFound = false;
        for (String currentWorkspaceFile : new HiveManager().listWorkspaceFiles(workspaceName)) {
            resultStringBuilder.append(currentWorkspaceFile);
            resultStringBuilder.append("<br>");
            filesFound = true;
        }
        if (!filesFound) {
            resultStringBuilder.append("no files found<br>");
        }
        resultStringBuilder.append("</p><br><a href=/kinoath-rest/kinoath/kinspace/>list workspaces</a>");
        return resultStringBuilder.toString();
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
