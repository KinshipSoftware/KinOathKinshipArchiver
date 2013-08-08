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
package nl.mpi.kinnate;

/**
 *  Document   : SVGApplication
 *  Created on : Aug 17, 2010, 12:47:23 PM
 *  Author     : Peter Withers
 */
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.gvt.Interactor;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;

public class SVGApplication {

    public static void main(String[] args) {
        // Create a new JFrame.
        JFrame f = new JFrame("Batik");
        SVGApplication app = new SVGApplication(f);

        // Add components to the frame.
        f.getContentPane().add(app.createComponents());

        // Display the frame.
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        f.setSize(400, 400);
        f.setVisible(true);
    }

    // The frame.
    protected JFrame frame;

    // The "Load" button, which displays up a file chooser upon clicking.
    protected JButton button = new JButton("Load...");

    // The status label.
    protected JLabel label = new JLabel();

    // The SVG canvas.
    protected JSVGCanvas svgCanvas = new JSVGCanvas();

    public SVGApplication(JFrame f) {
        frame = f;
    }

    public JComponent createComponents() {
        // Create a panel and add the button, status label and the SVG canvas.
        final JPanel panel = new JPanel(new BorderLayout());

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(button);
        p.add(label);

        panel.add("North", p);
        panel.add("Center", svgCanvas);

        // Set the button action.
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
//                JFileChooser fc = new JFileChooser(".");
//                int choice = fc.showOpenDialog(panel);
//                if (choice == JFileChooser.APPROVE_OPTION) {
//                    File f = fc.getSelectedFile();
//                    try {
                        svgCanvas.setURI("http://upload.wikimedia.org/wikipedia/commons/6/6b/Bitmap_VS_SVG.svg"/*f.toURL().toString()*/);
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    }
//                }
            }
        });

        // Set the JSVGCanvas listeners.
        svgCanvas.addSVGDocumentLoaderListener(new SVGDocumentLoaderAdapter() {
            public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
                label.setText("Document Loading...");
            }
            public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
                label.setText("Document Loaded.");
            }
        });

        svgCanvas.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
            public void gvtBuildStarted(GVTTreeBuilderEvent e) {
                label.setText("Build Started...");
            }
            public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
                label.setText("Build Done.");
                frame.pack();
            }
        });

        svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
            public void gvtRenderingPrepare(GVTTreeRendererEvent e) {
                label.setText("Rendering Started...");
            }
            public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
                label.setText("");
            }
        });
        Interactor testInteractor = new Interactor(){

            public boolean endInteraction() {
                System.out.println("endInteraction");
//                throw new UnsupportedOperationException("Not supported yet.");
                return true;
            }

            public boolean startInteraction(InputEvent ie) {
                System.out.println("startInteraction:" + ie.toString());
//                throw new UnsupportedOperationException("Not supported yet.");
                return true;
            }

            public void keyPressed(KeyEvent ke) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void keyReleased(KeyEvent ke) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void keyTyped(KeyEvent ke) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void mouseClicked(MouseEvent me) {
//                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void mouseEntered(MouseEvent me) {
//                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void mouseExited(MouseEvent me) {
//                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void mousePressed(MouseEvent me) {
//                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void mouseReleased(MouseEvent me) {
                System.out.println("mouseReleased:" + me.toString());
//                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void mouseDragged(MouseEvent me) {
                System.out.println("mouseDragged:" + me.toString());
//                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void mouseMoved(MouseEvent me) {
//                throw new UnsupportedOperationException("Not supported yet.");
            }

//
            
        };
        svgCanvas.getInteractors().add(testInteractor);

        return panel;
    }
}