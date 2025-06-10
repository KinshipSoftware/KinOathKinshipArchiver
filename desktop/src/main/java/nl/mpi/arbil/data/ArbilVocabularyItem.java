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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.data;

import java.io.Serializable;

public class ArbilVocabularyItem implements Comparable, Serializable {

    public final String itemDisplayName;
    public final String itemCode;
    public final String followUpVocabulary;
    public String descriptionString;

    public ArbilVocabularyItem(String itemDisplayName, String itemCode, String followUpVocabulary) {
	this.itemDisplayName = itemDisplayName;
	this.itemCode = itemCode;
	this.followUpVocabulary = followUpVocabulary;
    }

    /**
     *
     * @return Value for vocabulary item: itemCode if it is set, otherwise display name
     */
    public String getValue() {
	if (itemCode != null) {
	    return itemCode;
	} else {
	    return itemDisplayName;
	}
    }

    public String getDisplayValue() {
	if (itemDisplayName != null) {
	    return itemDisplayName;
	} else {
	    return itemCode;
	}
    }

    public boolean hasItemCode() {
	return itemCode != null;
    }

    @Override
    public String toString() {
	return getDisplayValue();
    }

    public int compareTo(Object otherObject) {
	return this.toString().compareTo(otherObject.toString());
    }
}
