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
package nl.mpi.arbil.ui;

import java.io.Serializable;
import java.net.URI;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.DataNodeLoader;

/**
 * Used as blank in horizontal tables (grid view) in cells that represent
 * fields that are not present in the node represented by their row, i.e. the
 * 'grey fields'.
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilFieldPlaceHolder implements Serializable {

    private String fieldName;
    private transient ArbilDataNode arbilDataNode;
    private URI arbilDataNodeURI = null;
    private static DataNodeLoader dataNodeLoader;
 
    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
	dataNodeLoader = dataNodeLoaderInstance;
    }

    public ArbilFieldPlaceHolder(String fieldName, ArbilDataNode dataNode) {
	this.fieldName = fieldName;
	this.arbilDataNode = dataNode;
	if (dataNode != null) {
	    this.arbilDataNodeURI = arbilDataNode.getURI();
	}
    }

    public String getFieldName() {
	return fieldName;
    }

    public ArbilDataNode getArbilDataNode() {
	if (arbilDataNode == null && arbilDataNodeURI != null) {
	    arbilDataNode = dataNodeLoader.getArbilDataNode(null, arbilDataNodeURI);
	}
	return arbilDataNode;
    }

    @Override
    public String toString() {
	return "";
    }
}
