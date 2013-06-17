/**
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.svg;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import javax.swing.ImageIcon;
import nl.mpi.arbil.util.BugCatcherManager;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

/**
 * Document : SymbolGraphic Created on : Aug 25, 2011, 1:27:40 PM
 *
 * @ author Peter Withers
 */
public class SymbolGraphic {

    HashMap<String[], ImageIcon> symbolMapEgo = new HashMap<String[], ImageIcon>();
    HashMap<String[], ImageIcon> symbolMapAlter = new HashMap<String[], ImageIcon>();
    HashMap<String[], ImageIcon> symbolMapEgoAttached = new HashMap<String[], ImageIcon>();
    HashMap<String[], ImageIcon> symbolMapAlterAttached = new HashMap<String[], ImageIcon>();
    private final SVGDocument svgDocument;

    public SymbolGraphic(SVGDocument svgDocument) {
        // the parent diagram is passed here so that symbols from it can be used in the tree nodes
        this.svgDocument = svgDocument;
    }

    class ImageIconTranscoder extends ImageTranscoder {

        private BufferedImage image = null;

        public BufferedImage createImage(int w, int h) {
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            return image;
        }

        public void writeImage(BufferedImage img, TranscoderOutput out) {
        }

        public BufferedImage getImage() {
            return image;
        }
    }

    public ImageIcon getSymbolGraphic(String[] symbolNames, boolean isEgo, boolean isAttached) {
        HashMap<String[], ImageIcon> symbolMap;
        if (isAttached) {
            if (isEgo) {
                symbolMap = symbolMapEgoAttached;
            } else {
                symbolMap = symbolMapAlterAttached;
            }
        } else {
            if (isEgo) {
                symbolMap = symbolMapEgo;
            } else {
                symbolMap = symbolMapAlter;
            }
        }
        if (symbolMap.containsKey(symbolNames)) {
            return symbolMap.get(symbolNames);
        }
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        SVGDocument doc = (SVGDocument) impl.createDocument(svgNS, "svg", null);
        // in order to be in sync with the symbols in the actual document, these symbols are copied from the parent document
        // copy the kin symbols from the users diagram
        Element kinSymbols = svgDocument.getElementById("KinSymbols");
        Node newNode = doc.importNode(kinSymbols, true);
        doc.getDocumentElement().appendChild(newNode);

        int symbolSize = EntitySvg.symbolSize;
        for (String currentSymbol : symbolNames) {
            Element symbolNode;
            symbolNode = doc.createElementNS(svgNS, "use");
            symbolNode.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "#" + currentSymbol); // the xlink: of "xlink:href" is required for some svg viewers to render correctly
            if (isEgo) {
                if (isAttached) {
                    symbolNode.setAttribute("stroke", "black");
                    symbolNode.setAttribute("fill", "black");
                } else {
                    symbolNode.setAttribute("stroke", "grey");
                    symbolNode.setAttribute("fill", "grey");
                }
            } else {
                symbolNode.setAttribute("fill", "none");
                if (isAttached) {
                    symbolNode.setAttribute("stroke", "black");
                } else {
                    symbolNode.setAttribute("stroke", "grey");
                }
            }
            symbolNode.setAttribute("stroke-width", "2");

            Element svgRoot = doc.getDocumentElement();
            svgRoot.appendChild(symbolNode);
        }
        ImageIconTranscoder transcoder = new ImageIconTranscoder();
        TranscodingHints hints = new TranscodingHints();
        hints.put(ImageTranscoder.KEY_WIDTH, (float) symbolSize);
        hints.put(ImageTranscoder.KEY_HEIGHT, (float) symbolSize);
//        hints.put(ImageTranscoder.KEY_MAX_WIDTH, (float) symbolSize);
//        hints.put(ImageTranscoder.KEY_MAX_HEIGHT, (float) symbolSize);
        transcoder.setTranscodingHints(hints);
        try {
            transcoder.transcode(new TranscoderInput(doc), null);
            BufferedImage bufferedImage = transcoder.getImage();
            ImageIcon imageIcon = new ImageIcon(bufferedImage);
            symbolMap.put(symbolNames, imageIcon);
            return imageIcon;
        } catch (TranscoderException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            return null;
        }
    }
}
