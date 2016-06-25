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
 * @since Jun 22, 2016 19:57:16 PM (creation date)
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class KinPoint {

    public int x;
    public int y;

    public KinPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public KinPoint(KinPoint kinPoint) {
        this.x = kinPoint.x;
        this.y = kinPoint.y;
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

    public double distance(double otherX, double otherY) {
        otherX -= x;
        otherY -= y;
        return Math.sqrt(otherX * otherX + otherY * otherY);
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setLocation(KinPoint kinPoint) {
        this.x = kinPoint.x;
        this.y = kinPoint.y;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + this.x;
        hash = 19 * hash + this.y;
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
        final KinPoint other = (KinPoint) obj;
        if (this.x != other.x) {
            return false;
        }
        return this.y == other.y;
    }
}
