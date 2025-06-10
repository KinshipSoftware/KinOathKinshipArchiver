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
package nl.mpi.arbil.clarin;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on : Sep 24, 2013, 2:08:11 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class HandleUtils {

    private final static Logger logger = LoggerFactory.getLogger(HandleUtils.class);
    public static final String HANDLE_SERVER_URI = "http://hdl.handle.net/";
    public final static int REDIRECT_TIMEOUT = 30 * 1000;

    /**
     *
     * @return the URI of the link, can be unresolved
     * @see #getResolvedLinkUri()
     * @throws URISyntaxException if reference in this link is not a valid URI
     */
    public URI resolveHandle(String resourceRef) throws URISyntaxException {
	if (resourceRef != null && resourceRef.length() > 0) {
	    if (resourceRef.startsWith("hdl://")) { // IS THIS VALID? TG 4/10/2011
		return new URI(resourceRef.replace("hdl://", HANDLE_SERVER_URI));
	    } else if (resourceRef.startsWith("hdl:")) {
		return new URI(resourceRef.replace("hdl:", HANDLE_SERVER_URI));
	    } else {
		return new URI(resourceRef);
	    }
	}
	return null;
    }

    public URI resolveHandle(URI resourceURI) {
	if ("hdl".equals(resourceURI.getScheme())) {
	    return URI.create(HANDLE_SERVER_URI + resourceURI.toString().replaceFirst("^hdl:", ""));
	}
	return resourceURI;
    }

    public URI followRedirect(final URI handleURI) {
	if (handleURI.getScheme().startsWith("http")) {
	    try {
		final URLConnection uRLConnection = resolveHandle(handleURI).toURL().openConnection();
		if (uRLConnection instanceof HttpURLConnection) {
		    logger.trace("Requesting {} to find possible redirect", handleURI);
		    ((HttpURLConnection) uRLConnection).setInstanceFollowRedirects(true);
		    ((HttpURLConnection) uRLConnection).setReadTimeout(REDIRECT_TIMEOUT);
		    uRLConnection.getInputStream().close();
		    final URL resolvedUrl = uRLConnection.getURL();
		    final URI resolvedUri = resolvedUrl.toURI();
		    if (logger.isDebugEnabled() && !resolvedUri.equals(handleURI)) {
			logger.debug("Redirected: {} -> {}", handleURI, resolvedUri);
		    }
		    return resolvedUri;
		}
	    } catch (URISyntaxException ex) {
		logger.warn("Could not convert URL to URI for {}: {}", handleURI, ex.getMessage());
		logger.info("Could not convert URL to URI", ex);
	    } catch(ConnectException ex){
		logger.warn("Connection to {} could not be established, could not check for a redirect (timeout is {}ms}): {}", handleURI, REDIRECT_TIMEOUT, ex.getMessage());
		logger.info("Connection could not be established while looking for redirect", ex);
	    } catch (IOException ex) {
		logger.warn("Could not follow redirects for {}: {}", handleURI, ex.getMessage());
		logger.info("Could not follow redirects", ex);		
	    }
	}
	return handleURI;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
	new HandleUtils().followRedirect(URI.create("http://hdl.handle.net/1839/00-0000-0000-0001-53A6-F@format=cmdi"));
    }
}
