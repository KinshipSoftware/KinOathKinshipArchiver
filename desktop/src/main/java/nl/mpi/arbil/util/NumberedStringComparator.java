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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class NumberedStringComparator implements Comparator {

    private final static Pattern PATTERN = Pattern.compile("[0-9]+");

    public abstract int compare(Object object1, Object object2);

    /**
     * 
     * @param string1 First string of pair
     * @param string2 Second string of pair
     * @return 
     */
    protected Integer compareNumberedStrings(String string1, String string2) {
	// If both strings contain an integer, check if prefix is identical, then compare on basis of int values
	final Matcher match1 = getPattern().matcher(string1);
	if (match1.find()) {
	    // See if prefix matches same area in string 2
	    if (string1.regionMatches(0, string2, 0, match1.start())) {
		// See if string 2 ends in integer as well, and check whether prefixes match completely
		final Matcher match2 = getPattern().matcher(string2);
		if (match2.find() && match1.start() == match2.start()) {
		    Integer comparison;
		    if (match1.end() == match2.end()) {
			// Matches are of same length, so we can do an ordinary string comparison 
			// (I suppose this is cheaper than parsing integers)
			comparison = match1.group().compareToIgnoreCase(match2.group());
		    } else {
			// Get integer values and compare
			try {
			    comparison = Integer.parseInt(match1.group()) - Integer.parseInt(match2.group());
			} catch (NumberFormatException ex) {
			    // something gone wrong with the regex, revert to caller's method of comparison
			    return null;
			}
		    }
		    if (comparison.equals(0) // Numerical parts are equal?
			    && string1.length() > match1.end() // There's more for both strings?
			    && string2.length() > match2.end()) {
			// Compare remaining parts
			return compareSub(string1.substring(match1.end()), string2.substring(match2.end()));
		    } else {
			return comparison;
		    }
		}
	    }
	}
	return null;
    }

    /**
     * Called to compare remainder of string with equal numerical part. Will try to do numerical compare on
     * remainder; if this is not possible, reverts to string comparison.
     * @param string1 Remainder of first string
     * @param string2 Remainder of second string
     * @return Comparison value
     */
    protected Integer compareSub(String string1, String string2) {
	Integer numberComp = compareNumberedStrings(string1, string2);
	if (numberComp == null) {
	    return Integer.valueOf(string1.compareToIgnoreCase(string2));
	} else {
	    return numberComp;
	}
    }

    protected Pattern getPattern() {
	return PATTERN;
    }
}
