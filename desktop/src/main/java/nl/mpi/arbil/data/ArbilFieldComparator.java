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
package nl.mpi.arbil.data;

import java.util.Comparator;

/**
 * @author Peter Withers <peter.withers@mpi.nl>
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilFieldComparator implements Comparator<ArbilField>, java.io.Serializable {
// NOTE: Comparators without state can be Serializable, makes them more useful.

    public int compare(ArbilField firstColumn, ArbilField secondColumn) {
        try {
            int baseIntA = ((ArbilField) firstColumn).getFieldOrder();
            int comparedIntA = ((ArbilField) secondColumn).getFieldOrder();
            int returnValue = baseIntA - comparedIntA;
            if (returnValue == 0) {
                // if the xml node order is the same then also sort on the strings
                String baseStrA = firstColumn.getFieldValue();
                String comparedStrA = secondColumn.getFieldValue();
                returnValue = baseStrA.compareToIgnoreCase(comparedStrA);
            }
            return returnValue;
        } catch (Exception ex) {
            //bugCatcher.logError(ex);
            return 1;
        }
    }
}
