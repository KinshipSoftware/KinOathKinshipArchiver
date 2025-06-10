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

import nl.mpi.arbil.util.NumberedStringComparator;
import java.io.Serializable;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * ArbilTreeNodeSorter.java
 * Created on Aug 11, 2009, 11:08:48 AM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilNodeSorter extends NumberedStringComparator implements Serializable {

    public int compare(Object object1, Object object2) {
	Object userObject1;
	Object userObject2;
	if (object1 instanceof DefaultMutableTreeNode && object2 instanceof DefaultMutableTreeNode) {
	    userObject1 = ((DefaultMutableTreeNode) object1).getUserObject();
	    userObject2 = ((DefaultMutableTreeNode) object2).getUserObject();
	} else if (object1 instanceof ArbilNode && object2 instanceof ArbilNode) {
	    userObject1 = object1;
	    userObject2 = object2;
	} else {
	    throw new IllegalArgumentException("not a DefaultMutableTreeNode object");
	}
	if (userObject1 instanceof ArbilNode && userObject2 instanceof ArbilNode) {
	    final int typeIndex1 = getTypeIndex((ArbilNode) userObject1);
	    final int typeIndex2 = getTypeIndex((ArbilNode) userObject2);
	    // sort by catalogue then corpus then session etc. then by the text order
	    if (typeIndex1 == typeIndex2) {
		return compareSameTypeNodes((ArbilNode) userObject1, (ArbilNode) userObject2);
	    } else {
		return typeIndex1 - typeIndex2;
	    }
	} else {
	    //return userObject1.toString().compareToIgnoreCase(object2.toString());
	    throw new IllegalArgumentException("not a ArbilNode object: " + object1.toString() + " : " + object2.toString());
	}
    }

    private int compareSameTypeNodes(final ArbilNode userObject1, final ArbilNode userObject2) {
	final String string1 = userObject1.toString();
	final String string2 = userObject2.toString();

	Integer resultInt = compareNumberedStrings(string1, string2);
	if (resultInt == null) {
	    resultInt = string1.compareToIgnoreCase(string2);
	    if (resultInt == 0) {
		if (userObject1 instanceof ArbilDataNode && userObject2 instanceof ArbilDataNode) { // make sure that to objects dont get mistaken to be the same just because the string lebels are the same
		    resultInt = ((ArbilDataNode) userObject1).getUrlString().compareToIgnoreCase(((ArbilDataNode) userObject2).getUrlString());
		} else {
		    resultInt = userObject1.hashCode() - userObject2.hashCode();
		}
	    }
	}
	return resultInt;
    }

    protected int getTypeIndex(ArbilNode targetNode) {
	if (targetNode instanceof ArbilDataNode) {
	    if (((ArbilDataNode) targetNode).isInfoLink) {
		return 100;
	    }
	}
	if (targetNode.isDataLoaded() // caution: this sort can cause the tree to collapse when nodes reload because the nodes will be removed if not in order
		|| targetNode instanceof ArbilDataNode && ((ArbilDataNode) targetNode).isNodeTextDetermined()) { // If we know the node's name, we already know where it will (most likely) end up
	    if (targetNode.isCorpus()) {
		return 300;
	    } else if (targetNode.isCatalogue()) {
		return 400;
	    } else if (targetNode.isSession()) {
		return 500;
	    } else if (targetNode.isChildNode()) {
		return 200 + getChildNodeTypeIndex(targetNode);
	    } else if (targetNode.isMetaDataNode()) {
		// in the case of ArbilNodes 
		return 600;
	    } else if (targetNode.isDirectory()) {
		return 700;
	    } else {
		return 800;
	    }
	} else {
	    // put the loading nodes at the end to help the tree sorting and rendering process
	    return 1000;
	}
    }

    protected int getChildNodeTypeIndex(ArbilNode targetDataNode) {
	return 0;
    }
}
