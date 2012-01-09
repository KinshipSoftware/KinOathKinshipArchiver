package nl.mpi.kinnate.svg;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import javax.swing.ImageIcon;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

/**
 *  Document   : SymbolGraphic
 *  Created on : Aug 25, 2011, 1:27:40 PM
 *  Author     : Peter Withers
 */
public class SymbolGraphic {

    HashMap<String, ImageIcon> symbolMapEgo = new HashMap<String, ImageIcon>();
    HashMap<String, ImageIcon> symbolMapAlter = new HashMap<String, ImageIcon>();
    private MessageDialogHandler dialogHandler;
    private BugCatcher bugCatcher;

    public SymbolGraphic(MessageDialogHandler dialogHandler, BugCatcher bugCatcher) {
        this.dialogHandler = dialogHandler;
        this.bugCatcher = bugCatcher;
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

    public ImageIcon getSymbolGraphic(String symbolType, boolean isEgo) {
        HashMap<String, ImageIcon> symbolMap;
        if (isEgo) {
            symbolMap = symbolMapEgo;
        } else {
            symbolMap = symbolMapAlter;
        }
        if (symbolMap.containsKey(symbolType)) {
            return symbolMap.get(symbolType);
        }
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        SVGDocument doc = (SVGDocument) impl.createDocument(svgNS, "svg", null);

        int symbolSize = new EntitySvg(dialogHandler, bugCatcher).insertSymbols(doc, svgNS);

        Element symbolNode;
        symbolNode = doc.createElementNS(svgNS, "use");
        symbolNode.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "#" + symbolType); // the xlink: of "xlink:href" is required for some svg viewers to render correctly
        if (isEgo) {
            symbolNode.setAttribute("fill", "black");
        } else {
            symbolNode.setAttribute("fill", "none");
        }
        symbolNode.setAttribute("stroke", "black");
        symbolNode.setAttribute("stroke-width", "2");

        Element svgRoot = doc.getDocumentElement();
        svgRoot.appendChild(symbolNode);

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
            symbolMap.put(symbolType, imageIcon);
            return imageIcon;
        } catch (TranscoderException exception) {
            bugCatcher.logError(exception);
            return null;
        }
    }
}
