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

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;

/**
 * @since Jun 17, 2016 18:46:13 PM (creation date)
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class KinDocumentImpl implements KinDocument {

    SVGDocument doc;
    final private EventListener mouseListenerSvg = null;

    public void readDocument(String uri, String templateXml) throws IOException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);

        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory documentFactory = new SAXSVGDocumentFactory(parser);
        doc = (SVGDocument) documentFactory.createDocument(uri, new StringReader(templateXml));
    }

    public void createDocument(String uri) throws IOException {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory documentFactory = new SAXSVGDocumentFactory(parser);
        doc = (SVGDocument) documentFactory.createDocument(uri);
    }

    public KinElement getElementById(String elementId) {
        final Element elementById = doc.getElementById(elementId);
        return (elementById == null) ? null : new KinElementImpl(elementById);
    }

    public KinElement getDocumentElement() {
        final Element documentElement = doc.getDocumentElement();
        return (documentElement == null) ? null : new KinElementImpl(documentElement);
    }

    public KinElement createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
        return new KinElementImpl(doc.createElementNS(namespaceURI, qualifiedName));
    }

    public KinElement createTextNode(String data) {
        return new KinElementImpl(doc.createTextNode(data));
    }

    public void addEventListener(KinElement targetNode) {
        ((EventTarget) ((KinElementImpl) targetNode).getNode()).addEventListener("mousedown", mouseListenerSvg, false); // todo: use capture (currently false) could be useful for the mouse events    }
    }

    public SVGDocument getDoc() {
        return doc;
    }
}
