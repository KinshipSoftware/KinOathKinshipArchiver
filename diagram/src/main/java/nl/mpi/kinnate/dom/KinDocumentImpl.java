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
package nl.mpi.kinnate.dom;

import java.io.IOException;
import java.io.StringReader;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import nl.mpi.kinnate.kindata.KinPoint;
import nl.mpi.kinnate.kindata.KinRectangle;
import nl.mpi.kinnate.svg.KinDocument;
import nl.mpi.kinnate.svg.KinElement;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGOMPoint;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGRect;

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

    public KinPoint getPointOnDocument(final KinPoint screenLocation, KinElement targetGroupElement) {
        SVGOMPoint pointOnScreen = new SVGOMPoint(screenLocation.x, screenLocation.y);
        SVGMatrix mat = ((SVGLocatable) ((KinElementImpl) targetGroupElement).getNode()).getScreenCTM();  // this gives us the element to screen transform
        mat = mat.inverse();                                // this converts that into the screen to element transform
        SVGOMPoint pointOnDocument = (SVGOMPoint) pointOnScreen.matrixTransform(mat);
        return new KinPoint((int) pointOnDocument.getX(), (int) pointOnDocument.getY()); // we discard the float precision because the diagram does not need that level of resolution 
    }

    public KinPoint getPointOnScreen(final KinPoint documentLocation, KinElement targetGroupElement) {
        SVGOMPoint pointOnDocument = new SVGOMPoint(documentLocation.x, documentLocation.y);
        SVGMatrix mat = ((SVGLocatable) ((KinElementImpl) targetGroupElement).getNode()).getScreenCTM();  // this gives us the element to screen transform
        SVGOMPoint point = (SVGOMPoint) pointOnDocument.matrixTransform(mat);
        return new KinPoint((int) point.getX(), (int) point.getY());
    }

    public KinRectangle getRectOnDocument(final KinRectangle screenRectangle, KinElement targetGroupElement) {
        SVGOMPoint pointOnScreen = new SVGOMPoint(screenRectangle.x, screenRectangle.y);
        SVGOMPoint sizeOnScreen = new SVGOMPoint(screenRectangle.width, screenRectangle.height);
        SVGMatrix mat = ((SVGLocatable) ((KinElementImpl) targetGroupElement).getNode()).getScreenCTM();  // this gives us the element to screen transform
        // todo: mat can be null
        mat = mat.inverse();                                // this converts that into the screen to element transform
        SVGPoint pointOnDocument = pointOnScreen.matrixTransform(mat);
        // the diagram keeps the x and y scale equal so we can just use getA here
        SVGPoint sizeOnDocument = new SVGOMPoint(sizeOnScreen.getX() * mat.getA(), sizeOnScreen.getY() * mat.getA());
        System.out.println("sizeOnScreen: " + sizeOnScreen);
        System.out.println("sizeOnDocument: " + sizeOnDocument);
        return new KinRectangle((int) pointOnDocument.getX(), (int) pointOnDocument.getY(), (int) sizeOnDocument.getX(), (int) sizeOnDocument.getY());
    }

    public KinRectangle getBoundingBox(KinElement selectedGroup) {
        final SVGRect bBox = ((SVGLocatable) ((KinElementImpl) selectedGroup).getNode()).getBBox();
        return new KinRectangle((int) bBox.getX(), (int) bBox.getY(), (int) bBox.getWidth(), (int) bBox.getHeight());
    }

    public float getDragScale(KinElement kinElement) {
//            KinElement  entityGroup = svgDiagram.doc.getElementById("EntityGroup");
        SVGMatrix draggedElementScreenMatrix = ((SVGLocatable) ((KinElementImpl) kinElement).getNode()).getScreenCTM().inverse();
        float dragScale = draggedElementScreenMatrix.getA(); // the drawing is proportional so only using X is adequate here         
        return dragScale;
    }

    public String getUUID() {
        return UUID.randomUUID().toString();
    }
}
