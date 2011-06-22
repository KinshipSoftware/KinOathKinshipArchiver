package nl.mpi.kinnate.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import javax.swing.JButton;

/**
 *  Document   : RotatedLabel
 *  Created on : May 19, 2011, 2:04:28 PM
 *  Author     : Peter Withers
 */
public class RotatedButton extends JButton {

    public enum Orientation {

        top, right, bottom, left
    }
    Orientation orientation;

    public RotatedButton(String labelText, Orientation orientationLocal) {
        super(labelText);
        orientation = orientationLocal;
    }

    @Override
    public int getHeight() {
        return getSize().height;
    }

    @Override
    public int getWidth() {
        return getSize().width;
    }

    @Override
    public Dimension getSize() {
//        return super.size();
        Dimension superSize = super.getSize();
        switch (orientation) {
            case left:
            case right:
                return new Dimension(superSize.height, superSize.width);
            default:
                return super.getSize();
        }
    }

//    @Override
//    public Dimension size() {
//        Dimension superSize = super.size();
//        //        Dimension superSize = super.getSize();
//        switch (orientation) {
//            case left:
//            case right:
//                return new Dimension(superSize.height, superSize.width);
//            default:
//                return super.getSize();
//        }
//    }

    @Override
    public void paint(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        switch (orientation) {
            case left:
            case right:
                graphics2D.translate(0, super.getSize().width);
                graphics2D.transform(AffineTransform.getQuadrantRotateInstance(-1));
            default:
        }
        super.paintComponent(graphics2D);
    }
}
