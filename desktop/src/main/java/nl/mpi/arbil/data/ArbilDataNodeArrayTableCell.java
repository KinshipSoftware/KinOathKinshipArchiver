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
package nl.mpi.arbil.data;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * ArbilTableCell for arrays of ArbilDataNodes. Serializable due to transient ArbilDataNode field.
 * Re-attaches using node URI
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDataNodeArrayTableCell implements ArbilTableCell<ArbilDataNode[]> {

    private transient ArbilDataNode[] dataNodes;
    private ArrayList<URI> contentUris;
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
	dataNodeLoader = dataNodeLoaderInstance;
    }

    public ArbilDataNodeArrayTableCell(ArbilDataNode[] dataNode) {
	setContent(dataNode);
	setURIs();
    }

    /**
     * @return the content
     */
    @Override
    public ArbilDataNode[] getContent() {
	if (dataNodes == null && contentUris != null) {
	    loadNodes();
	}
	return dataNodes;
    }

    /**
     * @param content the content to set
     */
    @Override
    public final void setContent(ArbilDataNode[] content) {
	this.dataNodes = content;
	setURIs();
    }

    private void setURIs() {
	if (dataNodes == null) {
	    contentUris = null;
	} else {
	    contentUris = new ArrayList<URI>(dataNodes.length);
	    for (ArbilDataNode node : dataNodes) {
		contentUris.add(node != null ? node.getURI() : null);
	    }
	}
    }

    private void loadNodes() {
	if (contentUris == null) {
	    dataNodes = null;
	} else {
	    dataNodes = new ArbilDataNode[contentUris.size()];
	    for (int i = 0; i < contentUris.size(); i++) {
		dataNodes[i] = dataNodeLoader.getArbilDataNode(null, contentUris.get(i));
	    }
	}
    }

    @Override
    public String toString() {
	StringBuilder cellText = new StringBuilder();
	Arrays.sort(getContent(), new Comparator() {

	    public int compare(Object o1, Object o2) {
		String value1 = o1.toString();
		String value2 = o2.toString();
		return value1.compareToIgnoreCase(value2);
	    }
	});
	boolean hasAddedValues = false;
	for (ArbilDataNode currentArbilDataNode : getContent()) {
	    if (hasAddedValues) {
		cellText.append(','); // before each non-first value
	    }
	    cellText.append('[');
	    cellText.append(currentArbilDataNode.toString());
	    cellText.append(']');
	    hasAddedValues = true;
	}
	return cellText.toString();
    }
}
