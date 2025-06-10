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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.ArrayList;
import nl.mpi.arbil.util.BugCatcherManager;
import org.apache.commons.digester.Digester;

/**
 * CmdiResourceLinkReader.java <br> Created on March 12, 2010, 17:04:03
 *
 * @author Peter.Withers@mpi.nl
 */
public class CmdiComponentLinkReader {

    private URI parentUri;
    public ArrayList<CmdiResourceLink> cmdiResourceLinkArray = null;
    public ArrayList<ResourceRelation> cmdiResourceRelationArray = null;

    public static void main(String args[]) {
	CmdiComponentLinkReader cmdiComponentLinkReader = new CmdiComponentLinkReader();
	try {//http://www.clarin.eu/cmd/example/example-md-instance.xml
	    cmdiComponentLinkReader.readLinks(new URI("http://www.clarin.eu/cmd/example/example-md-instance.cmdi"));
	} catch (URISyntaxException exception) {
	    System.err.println(exception.getMessage());
	}
    }

    public final static class CmdiResourceLink {

	public final String resourceProxyId;
	public final String resourceType;
	public final String resourceRef;
	private final URI parentUri;
	private int referencingNodes;

	/**
	 *
	 * @param parentUri URI of parent document
	 * @param proxyId id of proxy (id attribute of ResourceProxy element)
	 * @param type proxy type (generally 'Resource' or 'Metadata')
	 * @param ref reference to the actual resource location
	 */
	public CmdiResourceLink(URI parentUri, String proxyId, String type, String ref) {
	    //TODO: Store resolved parent URI as soon as available (at the point of digestion), use this in getResolvedLinkUri

	    resourceProxyId = proxyId;
	    resourceType = type;
	    resourceRef = ref;

	    this.parentUri = parentUri;
	}

	/**
	 * To be called whenever a reference of this resource link is found
	 */
	public synchronized void addReferencingNode() {
	    referencingNodes++;
	}

	/**
	 * To be called whenever a reference of this resource link is removed
	 * (without reloading)
	 */
	public synchronized void removeReferencingNode() {
	    referencingNodes--;
	}

	/**
	 *
	 * @return Number of references to the resource registered
	 */
	public synchronized int getReferencingNodesCount() {
	    return referencingNodes;
	}

	/**
	 *
	 * @return the URI of the link, can be unresolved
	 * @see #getResolvedLinkUri()
	 * @throws URISyntaxException if reference in this link is not a valid
	 * URI
	 */
	public URI getLinkUri() throws URISyntaxException {
	    if (resourceRef != null && resourceRef.length() > 0) {
		return new URI(resourceRef);
	    } else {
		throw new URISyntaxException(resourceRef, "resourceRef is null or zero length");
	    }
	}

	/**
	 *
	 * @return the URI of the link resolved against the URI of the
	 * containing document. Null if either reference or parent URI not set.
	 * @throws URISyntaxException if reference in this link is not a valid
	 * URI
	 */
	public URI getResolvedLinkUri() throws URISyntaxException {
	    if (parentUri != null) {
		return parentUri.resolve(getLinkUri());
	    } else {
		throw new URISyntaxException(resourceRef, "resourceRef is null");
	    }
	}

	@Override
	public String toString() {
	    return String.format("%s: %s -> [%s]%s", resourceProxyId, parentUri, resourceType, resourceRef);
	}
    }

    public static class ResourceRelation {

	public ResourceRelation(String type, String resource1, String resource2) {
	    relationType = type;
	    res1 = resource1;
	    res2 = resource2;
	}
	public final String relationType;
	public final String res1;
	public final String res2;
    }

    public CmdiComponentLinkReader() {
    }

    public URI getLinkUrlString(String resourceId) {
	CmdiResourceLink cmdiResourceLink = getResourceLink(resourceId);
	if (cmdiResourceLink != null) {
	    try {
		return cmdiResourceLink.getLinkUri();
	    } catch (URISyntaxException urise) {
		BugCatcherManager.getBugCatcher().logError(urise);
	    }
	}
	return null;
    }

    public CmdiResourceLink getResourceLink(String resourceId) {
	for (CmdiResourceLink cmdiResourceLink : cmdiResourceLinkArray) {
	    if (cmdiResourceLink.resourceProxyId.equals(resourceId)) {
		return cmdiResourceLink;
	    }
	}
	return null;
    }

    /**
     *
     * @param resourceRef Resource reference (i.e. content of ResourceRef)
     * @return Id of proxy with the given resource reference. Null if no such
     * ref can be found
     */
    public String getProxyId(String resourceRef) {
	for (CmdiResourceLink resourceLink : cmdiResourceLinkArray) {
	    try {
		if (resourceLink.resourceRef.equals(resourceRef)
			|| resourceLink.getResolvedLinkUri().toString().equals(resourceRef)) {
		    return resourceLink.resourceProxyId;
		}
	    } catch (URISyntaxException ex) {
		// Detected illegal URI in resource link. Can't do much at this point, continue with others
		BugCatcherManager.getBugCatcher().logError(ex);
	    }
	}
	return null;
    }

    public ArrayList<CmdiResourceLink> readLinks(URI targetCmdiNode) {
	this.parentUri = targetCmdiNode;
//        ArrayList<URI> returnUriList = new ArrayList<URI>();
	try {
	    Digester digester = new Digester();
	    digester.push(this);
	    digester.addCallMethod("CMD/Resources/ResourceProxyList/ResourceProxy", "addResourceProxy", 3);
	    digester.addCallParam("CMD/Resources/ResourceProxyList/ResourceProxy", 0, "id");
	    digester.addCallParam("CMD/Resources/ResourceProxyList/ResourceProxy/ResourceType", 1);
	    digester.addCallParam("CMD/Resources/ResourceProxyList/ResourceProxy/ResourceRef", 2);

	    digester.addCallMethod("CMD/Resources/ResourceRelationList/ResourceRelation", "addResourceRelation", 3);
	    digester.addCallParam("CMD/Resources/ResourceRelationList/ResourceRelation/RelationType", 0);
	    digester.addCallParam("CMD/Resources/ResourceRelationList/ResourceRelation/Res1", 1, "ref");
	    digester.addCallParam("CMD/Resources/ResourceRelationList/ResourceRelation/Res2", 2, "ref");

	    cmdiResourceLinkArray = new ArrayList<CmdiResourceLink>();
	    cmdiResourceRelationArray = new ArrayList<ResourceRelation>();
	    // we open the stream here so we can set follow redirects
	    URLConnection uRLConnection = targetCmdiNode.toURL().openConnection();
	    if (uRLConnection instanceof HttpURLConnection) {
		((HttpURLConnection) uRLConnection).setInstanceFollowRedirects(true);
	    }
	    final InputStream inputStream = uRLConnection.getInputStream();
	    digester.parse(inputStream);
	} catch (Exception e) {
	    BugCatcherManager.getBugCatcher().logError(e);
	}
	return cmdiResourceLinkArray;
    }

    public void addResourceProxy(
	    String resourceProxyId,
	    String resourceType,
	    String resourceRef) {
	CmdiResourceLink cmdiProfile = new CmdiResourceLink(parentUri, resourceProxyId, resourceType, resourceRef);

	cmdiResourceLinkArray.add(cmdiProfile);
    }

    public void addResourceRelation(
	    String RelationType,
	    String Res1,
	    String Res2) {
	ResourceRelation resourceRelation = new ResourceRelation(RelationType, Res1, Res2);

	cmdiResourceRelationArray.add(resourceRelation);
    }
}
