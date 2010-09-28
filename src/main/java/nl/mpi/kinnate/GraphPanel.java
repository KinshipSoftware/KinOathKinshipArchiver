/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.kingraph2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.swing.JPanel;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.arbil.clarin.CmdiComponentBuilder;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import nl.mpi.arbil.data.ImdiLoader;
import nl.mpi.arbil.data.ImdiTreeObject;
import org.apache.batik.swing.gvt.AbstractPanInteractor;
import org.apache.batik.swing.gvt.AbstractZoomInteractor;
import org.apache.batik.swing.gvt.Interactor;
import org.w3c.dom.Text;

/**
 *  Document   : GraphPanel
 *  Created on : Aug 16, 2010, 5:31:33 PM
 *  Author     : Peter Withers
 */
public class GraphPanel extends JPanel {

    protected JSVGCanvas svgCanvas = new JSVGCanvas();

    public GraphPanel() {
        this.setLayout(new BorderLayout());
        //doTest();
        drawNodes();

//        Interactor zoomInteractor = new AbstractZoomInteractor(){
//            public boolean startInteraction(InputEvent ie) {
//                int mods = ie.getModifiers();
//                return ie.getID() == MouseEvent.MOUSE_PRESSED
//                        && (mods & InputEvent.BUTTON3_MASK) != 0;
//            }
//        };
//        Interactor panInteractor = new AbstractPanInteractor() {
//
//            public boolean startInteraction(InputEvent ie) {
//                int mods = ie.getModifiers();
//                return ie.getID() == MouseEvent.MOUSE_PRESSED
//                        && (mods & InputEvent.BUTTON1_MASK) != 0;
//            }
//        };
//        svgCanvas.getInteractors().add(panInteractor);
//        svgCanvas.getInteractors().add(zoomInteractor);

        //Ctrl+LeftButton - Zoom Box
        //Shift+RightButton - Zoom (with instant feedback)
        //Shift+LeftButton - Pan
        //Ctrl+RightButton - Rotate
        //Ctrl+Shift+RightButton - Reset transform (also known as "Original View")
        
        svgCanvas.setEnableImageZoomInteractor(true);
        svgCanvas.setEnablePanInteractor(true);
        svgCanvas.setEnableRotateInteractor(true);
        svgCanvas.setEnableZoomInteractor(true);
        svgCanvas.setEnableResetTransformInteractor(true);
        
    }

    public void drawNodes() {
        String[] treeNodesArray = LinorgSessionStorage.getSingleInstance().loadStringArray("KinGraphTree");
        ArrayList<ImdiTreeObject> tempArray = new ArrayList<ImdiTreeObject>();
        if (treeNodesArray != null) {
            for (String currentNodeString : treeNodesArray) {
                try {
                    tempArray.add(ImdiLoader.getSingleInstance().getImdiObject(null, new URI(currentNodeString)));
                } catch (URISyntaxException exception) {
                    System.err.println(exception.getMessage());
                    exception.printStackTrace();
                }
            }
        }
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        Document doc = impl.createDocument(svgNS, "svg", null);
// Get the root element (the 'svg' element).
        Element svgRoot = doc.getDocumentElement();


//        svgRoot.removeAttribute("version");

        int stepNumber = 300;
       int preferedWidth = (int)Math.sqrt(tempArray.size()) * stepNumber + stepNumber;


        // Set the width and height attributes on the root 'svg' element.
        svgRoot.setAttributeNS(null, "width", Integer.toString(preferedWidth));
        svgRoot.setAttributeNS(null, "height", Integer.toString(preferedWidth));

        this.setPreferredSize(new Dimension(preferedWidth, preferedWidth));

        svgCanvas.setDocument(doc);
        int xPos = stepNumber;
        int yPos = stepNumber;
        for (ImdiTreeObject currentChild : tempArray) {
            currentChild.waitTillLoaded();
            if (!currentChild.isEmptyMetaNode()) {
                // Create the rectangle.
                Element rectangle = doc.createElementNS(svgNS, "rect");
                rectangle.setAttributeNS(null, "x", Integer.toString(xPos));
                rectangle.setAttributeNS(null, "y", Integer.toString(yPos));
                rectangle.setAttributeNS(null, "width", "10");
                rectangle.setAttributeNS(null, "height", "10");
                rectangle.setAttributeNS(null, "fill", "red");
// Attach the rectangle to the root 'svg' element.
                svgRoot.appendChild(rectangle);
                // <text id="_7" x="39.0" y="140.0" fill="black" stroke="black" stroke-width="0" font-size="15">Sample Text</text>
                Element labelText = doc.createElementNS(svgNS, "text");
                labelText.setAttributeNS(null, "x", Integer.toString(xPos));
                labelText.setAttributeNS(null, "y", Integer.toString(yPos));
                labelText.setAttributeNS(null, "fill", "black");
                labelText.setAttributeNS(null, "stroke-width", "0");
                labelText.setAttributeNS(null, "font-size", "14");
                //labelText.setNodeValue(currentChild.toString());

                //String textWithUni = "\u0041";
                Text textNode = doc.createTextNode(currentChild.toString());
                labelText.appendChild(textNode);
                svgRoot.appendChild(labelText);

                xPos += stepNumber;
                if (xPos > preferedWidth) {
                    xPos = stepNumber;
                    yPos += stepNumber;
                }
            }
        }


        this.add(BorderLayout.CENTER, svgCanvas);
        new CmdiComponentBuilder().savePrettyFormatting(doc, new File("/Users/petwit/Documents/SharedInVirtualBox/KinGraph/KinGraph2/src/main/resources/output.svg"));
    }

    public void doTest() {
        ImdiTreeObject imdiNode = null;
        try {
            imdiNode = ImdiLoader.getSingleInstance().getImdiObject(null, new URI("file:/Users/petwit/.arbil/ArbilWorkingFiles/20100817152236/20100817152236.cmdi"));
            //"file:/Users/petwit/.arbil/ArbilWorkingFiles/http/corpus1.mpi.nl/qfs1/media-archive/echo_data/sign_language/SSL/SSL_Poetry/Metadata/SSL_JM_poem_cayak_2.imdi"
            imdiNode.waitTillLoaded();
        } catch (URISyntaxException exception) {
            System.err.println(exception.getMessage());
        }

        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        Document doc = impl.createDocument(svgNS, "svg", null);

// Get the root element (the 'svg' element).
        Element svgRoot = doc.getDocumentElement();

// Set the width and height attributes on the root 'svg' element.
        svgRoot.setAttributeNS(null, "width", "400");
        svgRoot.setAttributeNS(null, "height", "450");

        this.setPreferredSize(new Dimension(450, 400));

        svgRoot.removeAttribute("version");

        int xPos = 10;
        int yPos = 10;
        if (imdiNode != null) {
            for (ImdiTreeObject currentChild : imdiNode.getAllChildren()) {
                if (!currentChild.isEmptyMetaNode()) {
                    // Create the rectangle.
                    Element rectangle = doc.createElementNS(svgNS, "rect");
                    rectangle.setAttributeNS(null, "x", Integer.toString(xPos));
                    rectangle.setAttributeNS(null, "y", Integer.toString(yPos));
                    rectangle.setAttributeNS(null, "width", "100");
                    rectangle.setAttributeNS(null, "height", "50");
                    rectangle.setAttributeNS(null, "fill", "red");
// Attach the rectangle to the root 'svg' element.
                    svgRoot.appendChild(rectangle);
                    // <text id="_7" x="39.0" y="140.0" fill="black" stroke="black" stroke-width="0" font-size="15">Sample Text</text>
                    Element labelText = doc.createElementNS(svgNS, "text");
                    labelText.setAttributeNS(null, "x", Integer.toString(xPos));
                    labelText.setAttributeNS(null, "y", Integer.toString(yPos));
                    labelText.setAttributeNS(null, "fill", "black");
                    labelText.setAttributeNS(null, "stroke-width", "0");
                    labelText.setAttributeNS(null, "font-size", "14");
                    //labelText.setNodeValue(currentChild.toString());

                    //String textWithUni = "\u0041";
                    Text textNode = doc.createTextNode(currentChild.getURI().getFragment());
                    labelText.appendChild(textNode);
                    svgRoot.appendChild(labelText);

                    xPos += 150;
                    if (xPos > 380) {
                        xPos = 10;
                        yPos += 70;
                    }
                }
            }
        }

//// Create the rectangle.
//        Element rectangle = doc.createElementNS(svgNS, "rect");
//        rectangle.setAttributeNS(null, "x", "10");
//        rectangle.setAttributeNS(null, "y", "20");
//        rectangle.setAttributeNS(null, "width", "100");
//        rectangle.setAttributeNS(null, "height", "50");
//        rectangle.setAttributeNS(null, "fill", "red");
//
//// Attach the rectangle to the root 'svg' element.
//        svgRoot.appendChild(rectangle);
//        this.add(BorderLayout.NORTH, new JTextArea(doc.getTextContent()));
//        try {
        svgCanvas.setDocument(doc);
        //svgCanvas.setURI(f.toURL().toString());
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
        this.add(BorderLayout.CENTER, svgCanvas);
        new CmdiComponentBuilder().savePrettyFormatting(doc, new File("/Users/petwit/Documents/SharedInVirtualBox/KinGraph/KinGraph2/src/main/resources/output.svg"));
    }
}
