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
package nl.mpi.arbil.util;

import java.util.Comparator;

/**
 * Compares two arrays by comparing their first elements
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArrayComparator<T> implements Comparator<T[]> {

    private final Comparator<T> itemComparator;
    private final int compareIndex;

    public ArrayComparator(Comparator<T> itemComparator, int compareIndex) {
        this.itemComparator = itemComparator;
        this.compareIndex = compareIndex;
    }

    public int compare(T[] o1, T[] o2) {
        // Check if both arrays are long enough
        if (o1.length >= compareIndex && o2.length >= compareIndex) {
            // Call comparator for array elements
            return itemComparator.compare(o1[compareIndex], o2[compareIndex]);
        } else {
            return 0;
        }
    }
}
