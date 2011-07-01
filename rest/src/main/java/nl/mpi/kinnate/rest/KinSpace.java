package nl.mpi.kinnate.rest;

import java.io.InputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import nl.mpi.kinnate.kinhive.HiveException;
import nl.mpi.kinnate.kinhive.HiveManager;

/**
 *  Document   : KinSpace
 *  Created on : Jun 30, 2011, 11:27:15 AM
 *  Author     : Peter Withers
 */
@Path("/kinspace")
public class KinSpace {

    String listWorkSpacesHref = "<br><a href=/kinoath-rest/kinoath/kinspace/>list workspaces</a>";

    @GET
    @Produces("text/html")
//    @Path("")
    public String listWorkSpaces() {
        StringBuilder resultStringBuilder = new StringBuilder();
        resultStringBuilder.append("<b>Available Workspaces</b><br><p>");
        String[] workspaceDirs = new HiveManager().listWorkspaces();
        if (workspaceDirs == null) {
            resultStringBuilder.append("No workspace directory found");
        } else {
            for (String currentWorkspace : workspaceDirs) {
                resultStringBuilder.append("<a href=/kinoath-rest/kinoath/kinspace/");
                resultStringBuilder.append(currentWorkspace);
                resultStringBuilder.append(">");
                resultStringBuilder.append(currentWorkspace);
                resultStringBuilder.append("</a>");
                resultStringBuilder.append("<br>");
            }
            resultStringBuilder.append("</p><br><a href=/kinoath-rest/kinoath/kinspace/sampleworkspace/create>create a sample workspace (change url to change the workspace name)</a>");
        }
        return resultStringBuilder.toString();
    }

    @GET
    @Produces("text/html")
    @Path("{workspaceName: [a-zA-Z0-9]*}/create")
    public String createWorkspace(@PathParam("workspaceName") String workspaceName) {
        try {
            new HiveManager().createWorkspace(workspaceName);
            return "created workspace<br><a href=/kinoath-rest/kinoath/kinspace/>" + workspaceName + "</a>" + listWorkSpacesHref;
        } catch (HiveException exception) {
            return exception.getMessage() + listWorkSpacesHref;
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
        String[] workspaceFiles = new HiveManager().listWorkspaceFiles(workspaceName);
        if (workspaceFiles == null) {
            resultStringBuilder.append("Workspace not found");
        } else if (workspaceFiles.length < 1) {
            resultStringBuilder.append("no files found<br>");
        } else {
            for (String currentWorkspaceFile : workspaceFiles) {
                resultStringBuilder.append(currentWorkspaceFile);
                resultStringBuilder.append("<br>");
            }
        }
        resultStringBuilder.append("</p>");
        resultStringBuilder.append(listWorkSpacesHref);
        return resultStringBuilder.toString();
    }

    @Path("{workspaceName: [a-zA-Z0-9]*}")
    @PUT
    @Consumes("text/xml")
    public String putXml(@PathParam("workspaceName") String workspaceName, InputStream entityXmlStream) {
        try {
            String kinHivePid = new HiveManager().addToWorkspace(workspaceName, entityXmlStream);
            return "Workspace: " + workspaceName + " Received: " + kinHivePid;
        } catch (HiveException exception) {
            throw new WebApplicationException(404);
        }
    }
}
