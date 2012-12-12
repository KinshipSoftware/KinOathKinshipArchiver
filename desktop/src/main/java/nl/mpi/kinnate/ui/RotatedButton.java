/**
 * Copyright (C) 2012 The Language Archive
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
