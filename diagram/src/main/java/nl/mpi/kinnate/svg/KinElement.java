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

/**
 * @since Jun 22, 2016 19:41:29 PM (creation date)
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public interface KinElement {

    public void setAttribute(String name, String value) throws KinElementException;

    public KinElement appendChild(KinElement newChild) throws KinElementException;

    public KinElement removeChild(KinElement oldChild) throws KinElementException;

    public KinElement insertBefore(KinElement newChild, KinElement refChild) throws KinElementException;

    public KinElement getFirstChild();

    public KinElement getNextSibling();

    public KinElement getParentNode();

    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws KinElementException;

    public String getAttributeNS(String namespaceURI, String localName) throws KinElementException;

    public String getAttribute(String attributeName) throws KinElementException;

    public String getLocalName();

    public String getTagName() throws KinElementException;

    public void setTextContent(String textContent) throws KinElementException;

    public String getTextContent() throws KinElementException;

    public void removeAttribute(String name) throws KinElementException;

    public boolean isElement();
}
