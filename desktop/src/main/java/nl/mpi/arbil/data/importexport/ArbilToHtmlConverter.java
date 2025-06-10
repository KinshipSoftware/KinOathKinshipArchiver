/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbil.data.importexport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import org.apache.xml.utils.DefaultErrorHandler;

/**
 * Document : ArbilToHtmlConverter
 * Created on : Apr 15, 2010, 1:33:02 PM
 * Author : Peter Withers
 */
public class ArbilToHtmlConverter {

    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }

    public URI exportImdiToHtml(ArbilDataNode[] inputNodeArray) {
	File destinationDirectory = messageDialogHandler.showEmptyExportDirectoryDialogue("Export HTML");
	if (destinationDirectory == null) {
	    return null;
	} else {
	    copyDependancies(destinationDirectory, false);
	    for (ArbilDataNode currentNode : inputNodeArray) {
		File destinationFile = new File(destinationDirectory, currentNode.toString() + ".html");
		int fileCounter = 1;
		while (destinationFile.exists()) {
		    destinationFile = new File(destinationDirectory, currentNode.toString() + "(" + fileCounter + ").html");
		}
		try {
		    transformNodeToHtml(currentNode, destinationFile);
		} catch (Exception exception) {
		    messageDialogHandler.addMessageDialogToQueue("Cannot convert data", "HTML Export");
		    BugCatcherManager.getBugCatcher().logError(exception);
		}
	    }
	    return destinationDirectory.toURI();
	}
    }

    public File convertToHtml(ArbilDataNode inputNode) throws IOException, TransformerException {
	File tempHtmlFile;
	tempHtmlFile = File.createTempFile("tmp", ".html");
	tempHtmlFile.deleteOnExit();
	copyDependancies(tempHtmlFile.getParentFile(), true);
	transformNodeToHtml(inputNode, tempHtmlFile);
	return tempHtmlFile;
    }

    private void transformNodeToHtml(ArbilDataNode inputNode, File destinationFile) throws IOException, TransformerException {
	// 1. Instantiate a TransformerFactory.
	javax.xml.transform.TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();
	// 2. Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
	URL xslUrl = this.getClass().getResource("/nl/mpi/arbil/resources/xsl/imdi-viewer.xsl");
	// look in the current template for a custom xsl
	File xslFile = null;
	xslFile = new File(inputNode.getNodeTemplate().getTemplateDirectory(), "format.xsl");
	if (xslFile != null && xslFile.exists()) {
	    xslUrl = xslFile.toURL();
	}
	final StreamSource streamSource = new javax.xml.transform.stream.StreamSource(xslUrl.toString());
	javax.xml.transform.Transformer transformer = tFactory.newTransformer(streamSource);
	final StringWriter errorStringWriter = new StringWriter();
	final DefaultErrorHandler errorHandler = new DefaultErrorHandler(new PrintWriter(errorStringWriter));
	transformer.setErrorListener(errorHandler);
	// 3. Use the Transformer to transform an XML Source and send the output to a Result object.
	try {
	    transformer.transform(new javax.xml.transform.stream.StreamSource(inputNode.getURI().toString()), new javax.xml.transform.stream.StreamResult(new java.io.FileOutputStream(destinationFile.getCanonicalPath())));
	} catch (TransformerException tEx) {
	    BugCatcherManager.getBugCatcher().logError("Transformer error messages: " + errorStringWriter.toString(), null);
	    throw tEx;
	}
    }

    private void copyDependancies(File destinationDirectory, boolean deleteOnExit) {
	// copy any dependent files from the jar
	String[] dependentFiles = {"imdi-viewer-open.gif", "imdi-viewer-closed.gif", "imdi-viewer.js", "imdi-viewer.css"};
//"additTooltip.js", "additPopup.js", , "additTooltip.css"
	for (String dependantFileString : dependentFiles) {
	    File tempDependantFile = new File(destinationDirectory, dependantFileString);
	    if (deleteOnExit) {
		tempDependantFile.deleteOnExit();
	    }
	    try {
		FileOutputStream outFile = new FileOutputStream(tempDependantFile);
		//InputStream inputStream = this.getClass().getResourceAsStream("html/imdi-viewer/" + dependantFileString);
		InputStream inputStream = this.getClass().getResourceAsStream("/nl/mpi/arbil/resources/xsl/" + dependantFileString);
		if (inputStream == null) {
		    BugCatcherManager.getBugCatcher().logError("Missing file in jar: " + dependantFileString, null);
		} else {
		    int bufferLength = 1024 * 4;
		    byte[] buffer = new byte[bufferLength]; // make htis 1024*4 or something and read chunks not the whole file
		    int bytesread = 0;
		    while (bytesread >= 0) {
			bytesread = inputStream.read(buffer);
			if (bytesread == -1) {
			    break;
			}
			outFile.write(buffer, 0, bytesread);
		    }
		    inputStream.close();
		}
		outFile.close();
	    } catch (IOException iOException) {
		messageDialogHandler.addMessageDialogToQueue("Cannot copy requisite file", "HTML Export");
		BugCatcherManager.getBugCatcher().logError(iOException);
	    }
	}
    }
}
