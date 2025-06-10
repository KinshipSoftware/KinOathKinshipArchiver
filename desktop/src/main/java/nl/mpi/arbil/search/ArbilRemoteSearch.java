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
package nl.mpi.arbil.search;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import org.apache.xpath.XPathAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Logic for carrying out a search on a remote corpus
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilRemoteSearch {

    private final static Logger logger = LoggerFactory.getLogger(ArbilRemoteSearch.class);
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");
    private static MessageDialogHandler dialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler dialogHandlerInstance) {
	dialogHandler = dialogHandlerInstance;
    }
    protected String lastSearchString = null;
    protected ArbilDataNode[] lastSearchNodes = null;
    protected URI[] searchResults = null;

    public static boolean isEmptyQuery(String queryText) {
	return RemoteServerSearchTerm.valueFieldMessage.equals(queryText) || "".equals(queryText);
    }

    public URI[] getServerSearchResults(final String queryText, ArbilDataNode[] searchNodes) {
	if (queryText == null || isEmptyQuery(queryText)) {
	    return new URI[]{};
	} else {
	    if (queryText.equals(lastSearchString) && Arrays.equals(searchNodes, lastSearchNodes)) {
		logger.debug("remote search term unchanged, returning last server response");
		return searchResults;
	    } else {
		ArrayList<URI> foundNodes = new ArrayList<URI>();
		lastSearchString = queryText;
		lastSearchNodes = searchNodes.clone();
		for (String resultString : performSearch(lastSearchString, searchNodes)) {
		    try {
			foundNodes.add(new URI(resultString));
		    } catch (URISyntaxException exception) {
			BugCatcherManager.getBugCatcher().logError(exception);
		    }
		}
		searchResults = foundNodes.toArray(new URI[]{});
		return searchResults;
	    }
	}
    }

    protected String[] performSearch(String searchString, ArbilDataNode[] arbilDataNodeArray) {
	ArrayList<String> returnArray = new ArrayList<String>();
	int maxResultNumber = 1000;
	try {
	    String fullQueryString = constructSearchQuery(arbilDataNodeArray, searchString, maxResultNumber);
	    logger.debug("QueryString: {}", fullQueryString);
	    Document resultsDocument = getSearchResults(fullQueryString);
	    NodeList domIdNodeList = XPathAPI.selectNodeList(resultsDocument, RemoteServerSearchTerm.IMDI_RESULT_URL_XPATH);
	    for (int nodeCounter = 0; nodeCounter < domIdNodeList.getLength(); nodeCounter++) {
		Node urlNode = domIdNodeList.item(nodeCounter);
		if (urlNode != null) {
		    logger.debug(urlNode.getTextContent());
		    returnArray.add(urlNode.getTextContent());
		}
	    }
	} catch (DOMException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	} catch (IOException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	} catch (ParserConfigurationException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	} catch (SAXException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	} catch (TransformerException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	}
	if (returnArray.size() >= maxResultNumber) {
	    dialogHandler.addMessageDialogToQueue(MessageFormat.format(widgets.getString("FOUND MORE RESULTS THAN CAN BE DISPLAYED, ONLY SHOWING THE FIRST {0} RESULTS"), maxResultNumber), widgets.getString("REMOTE SEARCH"));
	}
	return returnArray.toArray(new String[]{});
    }

    private static String constructSearchQuery(ArbilDataNode[] arbilDataNodeArray, String searchString, int maxResultNumber) {
	String encodedQuery;
	try {
	    encodedQuery = URLEncoder.encode(searchString, "UTF-8");
	} catch (UnsupportedEncodingException ex) {
	    throw new RuntimeException(ex);
	}

	String fullQueryString = RemoteServerSearchTerm.IMDI_SEARCH_BASE;
	fullQueryString += "&num=" + maxResultNumber;
	fullQueryString += "&query=" + encodedQuery;
	fullQueryString += "&type=simple";
	fullQueryString += "&includeUrl=true";
	for (ArbilDataNode arbilDataNode : arbilDataNodeArray) {
	    if (arbilDataNode.archiveHandle != null) {
		fullQueryString += "&nodeid=" + arbilDataNode.archiveHandle;
	    } else {
		dialogHandler.addMessageDialogToQueue(MessageFormat.format(widgets.getString("CANNOT SEARCH {0} BECAUSE IT DOES NOT HAVE AN ARCHIVE HANDLE"), arbilDataNode), widgets.getString("REMOTE SEARCH"));
	    }
	}
	// to search a branch we need the node id and to get that we need to have the handle and that might not exist, also to do any of that we would need to use an xmlrpc and include the lamusapi jar file to all versions of the application, so we will just search the entire archive since that takes about the same time to return the results
	//fullQueryString += "&nodeid=" + nodeidString; //MPI77915%23
	// &nodeid=MPI556280%23&nodeid=MPI84114%23&nodeid=MPI77915%23
	fullQueryString += "&returnType=xml";
	return fullQueryString;
    }

    private Document getSearchResults(String fullQueryString) throws SAXException, ParserConfigurationException, IOException {
	DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	documentBuilderFactory.setValidating(false);
	documentBuilderFactory.setNamespaceAware(true);
	DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
	Document resultsDocument = documentBuilder.parse(fullQueryString);
	return resultsDocument;
    }
}
