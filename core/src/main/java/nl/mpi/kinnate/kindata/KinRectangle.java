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
package nl.mpi.kinnate.kindata;

/**
 * @since Jun 22, 2016 19:04:36 PM (creation date)
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class KinRectangle {

    public int x;
    public int y;
    public int width;
    public int height;

    public KinRectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public KinRectangle(int width, int height) {
        this(0, 0, width, height);
    }

    public boolean contains(KinPoint kinPoint) {
        if (x > kinPoint.x) {
            return false;
        }
        if (x + width < kinPoint.x) {
            return false;
        }
        if (y > kinPoint.y) {
            return false;
        }
        return y + height >= kinPoint.y;
    }

    public boolean contains(KinRectangle kinRectangle) {
        if (x > kinRectangle.x) {
            return false;
        }
        if (x + width < kinRectangle.x + kinRectangle.width) {
            return false;
        }
        if (y > kinRectangle.y) {
            return false;
        }
        return y + height >= kinRectangle.y + kinRectangle.height;
    }

    public void add(KinPoint kinPoint) {
        int minX = Math.min(x, kinPoint.x);
        int minY = Math.min(y, kinPoint.y);
        int maxX = Math.max(x + width, kinPoint.x);
        int maxY = Math.max(y + height, kinPoint.y);
        x = minX;
        y = minY;
        width = maxX - minX;
        height = maxY - minY;
    }

    public double getCenterX() {
        return (x + width) / 2.0;
    }

    public double getCenterY() {
        return (y + height) / 2.0;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + this.x;
        hash = 89 * hash + this.y;
        hash = 89 * hash + this.width;
        hash = 89 * hash + this.height;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KinRectangle other = (KinRectangle) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        if (this.width != other.width) {
            return false;
        }
        return this.height == other.height;
    }
}
