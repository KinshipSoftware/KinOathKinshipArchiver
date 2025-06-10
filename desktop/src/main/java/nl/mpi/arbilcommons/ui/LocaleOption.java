/*
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbilcommons.ui;

import java.util.Locale;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class LocaleOption {
    private final Locale locale;
    private final String displayName;

    public LocaleOption(Locale locale) {
	this.locale = locale;
	if (locale.getCountry().length() == 0) {
	    this.displayName = locale.getDisplayLanguage(locale);
	} else {
	    this.displayName = String.format("%s (%s)", locale.getDisplayLanguage(locale), locale.getDisplayCountry(locale));
	}
    }

    public LocaleOption(String displayName) {
	this.locale = null;
	this.displayName = displayName;
    }

    public Locale getLocale() {
	return locale;
    }

    @Override
    public String toString() {
	return displayName;
    }

    @Override
    public int hashCode() {
	int hash = 5;
	hash = 37 * hash + (this.locale != null ? this.locale.hashCode() : 0);
	return hash;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final LocaleOption other = (LocaleOption) obj;
	if (other.getLocale() == null) {
	    return locale == null;
	}
	return other.getLocale().equals(locale);
    }

}
