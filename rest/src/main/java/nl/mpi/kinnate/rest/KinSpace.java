/**
 * Copyright (C) 2013 The Language Archive
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
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
            return "created workspace<br><a href=/kinoath-rest/kinoath/kinspace/" + workspaceName + ">view " + workspaceName + "</a>" + listWorkSpacesHref;
        } catch (HiveException exception) {
            return exception.getMessage() + listWorkSpacesHref;
        }

    }

    @GET
    @Produces("text/html")
    @Path("{workspaceName: [a-zA-Z0-9]*}/{pidString: [a-zA-Z0-9]*}")
    public String showWorkSpace(@PathParam("workspaceName") String workspaceName, @PathParam("pidString") String pidString) {
        return workspaceName + " : " + pidString;
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
            resultStringBuilder.append("<table>");
            for (String currentWorkspaceFile : workspaceFiles) {
                String pidString = currentWorkspaceFile.replaceFirst("\\.kmdi$", "");
                resultStringBuilder.append("<tr><td>");
                resultStringBuilder.append(pidString);
                resultStringBuilder.append("<br>");
                resultStringBuilder.append("</td><td>");
                resultStringBuilder.append("<a href=/kinoath-rest/kinoath/kinspace/");
                resultStringBuilder.append(workspaceName);
                resultStringBuilder.append("/");
                resultStringBuilder.append(pidString);
                resultStringBuilder.append(">view</a>");
                resultStringBuilder.append("</td><td>");
                resultStringBuilder.append("<a href=/kinoath-rest/kinoath/kinspace/");
                resultStringBuilder.append(workspaceName);
                resultStringBuilder.append("/");
                resultStringBuilder.append(pidString);
                resultStringBuilder.append("/commit>commit</a>");
                resultStringBuilder.append("</td></tr>");
            }
            resultStringBuilder.append("</table>");
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
            return "Pid for uploaded file: " + kinHivePid;
        } catch (HiveException exception) {
            throw new WebApplicationException(404);
        }
    }
}
