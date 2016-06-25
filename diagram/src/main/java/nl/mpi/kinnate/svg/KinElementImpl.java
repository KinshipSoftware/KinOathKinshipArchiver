/*
 * Copyright (C) 2016 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.kinnate.svg;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * @since Jun 22, 2016 19:43:36 PM (creation date)
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class KinElementImpl implements KinElement {

    private final Node node;

    public KinElementImpl(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public void setAttribute(String name, String value) throws KinElementException {
        if (node instanceof Element) {
            ((Element) node).setAttribute(name, value);
        } else {
            throw new KinElementException("no valid on this node");
        }
    }

    public KinElement appendChild(KinElement kinElement) throws KinElementException {
        final Node appendChild = node.appendChild(((KinElementImpl) kinElement).getNode());
        return (appendChild == null) ? null : kinElement;
    }

    public KinElement insertBefore(KinElement newChild, KinElement refChild) throws KinElementException {
        final Node insertBefore = node.insertBefore(((KinElementImpl) newChild).getNode(), ((KinElementImpl) refChild).getNode());
        return (insertBefore == null) ? null : newChild;
    }

    public KinElement getFirstChild() {
        final Node firstChild = node.getFirstChild();
        return (firstChild == null) ? null : new KinElementImpl(firstChild);
    }

    public KinElement getNextSibling() {
        final Node nextSibling = node.getNextSibling();
        return (nextSibling == null) ? null : new KinElementImpl(nextSibling);
    }

    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws KinElementException {
        if (node instanceof Element) {
            ((Element) node).setAttributeNS(namespaceURI, qualifiedName, value);
        } else {
            throw new KinElementException("no valid on this node");
        }
    }

    public String getAttributeNS(String namespaceURI, String localName) throws KinElementException {
        if (node instanceof Element) {
            return ((Element) node).getAttributeNS(namespaceURI, localName);
        } else {
            throw new KinElementException("no valid on this node");
        }
    }

    public String getAttribute(String attributeName) throws KinElementException {
        if (node instanceof Element) {
            return ((Element) node).getAttribute(attributeName);
        } else {
            NamedNodeMap nodeMap = node.getAttributes();
            if (nodeMap != null) {
                Node idNode = nodeMap.getNamedItem(attributeName);
                if (idNode != null) {
                    return idNode.getNodeValue();
                }
            }
            return null;
        }
    }

    public KinElement getParentNode() {
        final Node parentNode = node.getParentNode();
        return (parentNode == null) ? null : new KinElementImpl(parentNode);
    }

    public KinElement removeChild(KinElement oldChild) throws KinElementException {
        final Node removedChild = node.removeChild(((KinElementImpl) oldChild).getNode());
        return (removedChild == null) ? null : oldChild;
    }

    public String getLocalName() {
        return node.getLocalName();
    }

    public void removeAttribute(String name) throws KinElementException {
        if (node instanceof Element) {
            ((Element) node).removeAttribute(name);
        } else {
            throw new KinElementException("no valid on this node");
        }
    }

    public String getTagName() throws KinElementException {
        if (node instanceof Element) {
            return ((Element) node).getTagName();
        } else {
            throw new KinElementException("no valid on this node");
        }
    }

    public void setTextContent(String textContent) throws KinElementException {
        node.setTextContent(textContent);
    }

    public String getTextContent() throws KinElementException {
        return node.getTextContent();
    }

    public boolean isElement() {
        return (node instanceof Element);
    }
}
