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
package nl.mpi.arbil.data.metadatafile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import nl.mpi.arbil.ArbilMetadataException;

/**
 * Document : MetadataUtils
 * Created on : May 22, 2010, 7:56:46 AM
 * Author : Peter Withers
 */
public interface MetadataUtils {

    public boolean addCorpusLink(URI nodeURI, URI linkURI[]);

    public boolean removeCorpusLink(URI nodeURI, URI linkURI[]);

    public boolean moveMetadataFile(URI sourceURI, File destinationFile, boolean updateLinks);

    public void copyMetadataFile(URI sourceURI, File destinationFile, URI[][] linksToUpdate, boolean updateLinks) throws IOException, ArbilMetadataException;

    public MetadataLinkSet getCorpusLinks(URI nodeURI) throws ArbilMetadataException;

    public class MetadataLinkSet {

	private final List<URI> resourceLinks;
	private final List<URI> metadataLinks;

	public MetadataLinkSet(List<URI> resourceLinks, List<URI> metadataLinks) {
	    this.resourceLinks = resourceLinks;
	    this.metadataLinks = metadataLinks;
	}

	public List<URI> getResourceLinks() {
	    return resourceLinks;
	}

	public List<URI> getMetadataLinks() {
	    return metadataLinks;
	}
    }
}
