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

/**
 * ArbilTableCell for ArbilDataNodes. Serializable due to transient ArbilDataNode field.
 * Re-attaches using node URI
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDataNodeTableCell implements ArbilTableCell<ArbilDataNode> {

    private transient ArbilDataNode dataNode;
    private URI contentUri;
    
    private static DataNodeLoader dataNodeLoader;
    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance){
	dataNodeLoader = dataNodeLoaderInstance;
    }

    public ArbilDataNodeTableCell(ArbilDataNode dataNode) {
	setContent(dataNode);
	if (dataNode != null) {
	    contentUri = dataNode.getURI();
	}
    }

    /**
     * @return the content
     */
    @Override
    public ArbilDataNode getContent() {
	if (dataNode == null && contentUri != null) {
	    dataNode = dataNodeLoader.getArbilDataNode(null, contentUri);
	}
	return dataNode;
    }

    /**
     * @param content the content to set
     */
    @Override
    public final void setContent(ArbilDataNode content) {
	this.dataNode = content;
	contentUri = content != null ? content.getURI() : null;
    }

    @Override
    public String toString() {
	return dataNode.toString();
    }
}
