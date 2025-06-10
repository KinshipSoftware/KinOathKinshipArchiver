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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.swing.ImageIcon;
import nl.mpi.flap.model.DataNodeType;
import nl.mpi.flap.model.FieldGroup;
import nl.mpi.flap.model.PluginDataNode;

/**
 * Document : ContainerNode Created on : Mar 28, 2012, 1:04:22 PM
 *
 * @author Peter Withers
 */
public class ContainerNode extends ArbilNode implements Comparable {

    private ArbilNode[] childNodes;
    protected String labelString;
    private ImageIcon imageIcon;
    final private URI nodeUri;

    public ContainerNode(URI nodeUri, String labelString, ImageIcon imageIcon, ArbilNode[] childNodes) {
        this.nodeUri = nodeUri;
        this.childNodes = childNodes;
        this.labelString = labelString;
        this.imageIcon = imageIcon;
    }

    public int compareTo(Object o) {
        return labelString.compareTo(o.toString());
    }

    public void setChildNodes(ArbilNode[] childNodes) {
        this.childNodes = childNodes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ContainerNode other = (ContainerNode) obj;
        if ((this.labelString == null) ? (other.labelString != null) : !this.labelString.equals(other.labelString)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 17;
//        hash = 19 * hash + Arrays.deepHashCode(this.childNodes);
//        hash = 19 * hash + this.childNodes.hashCode();
        hash = 19 * hash + (this.labelString != null ? this.labelString.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return labelString + " (" + childNodes.length + ")";
    }

    @Override
    public ArbilNode[] getChildArray() {
        return childNodes;
    }

    @Override
    public String getLabel() {
        return this.toString();
    }

//    @Override
//    public List<PluginArbilDataNode> getChildArray() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
    @Override
    public DataNodeType getType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImageIcon getIcon() {
        return imageIcon;
    }

    @Override
    public void registerContainer(ArbilDataNodeContainer containerToAdd) {
        for (ArbilNode currentChild : childNodes) {
            currentChild.registerContainer(containerToAdd);
        }
    }

    @Override
    public void removeContainer(ArbilDataNodeContainer containerToRemove) {
        for (ArbilNode currentChild : childNodes) {
            currentChild.removeContainer(containerToRemove);
        }
    }

    @Override
    public ArbilDataNode[] getAllChildren() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getAllChildren(List<ArbilDataNode> allChildren) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getChildCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasCatalogue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasHistory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasLocalResource() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasResource() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isArchivableFile() {
        return false;
    }

    @Override
    public boolean isCatalogue() {
        return false;
    }

    @Override
    public boolean isChildNode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCmdiMetaDataNode() {
        return false;
    }

    @Override
    public boolean isCorpus() {
        return false;
    }

    @Override
    public boolean isDataLoaded() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDataPartiallyLoaded() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public boolean isEmptyMetaNode() {
        return false;
    }

    @Override
    public boolean isFavorite() {
        return false;
    }

    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public boolean isLocal() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isMetaDataNode() {
        return false;
    }

    @Override
    public boolean isResourceSet() {
        return false;
    }

    @Override
    public boolean isSession() {
        return false;
    }

    @Override
    public List<FieldGroup> getFieldGroups() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends PluginDataNode> getChildList() {
        return Arrays.asList(childNodes);
    }
}
