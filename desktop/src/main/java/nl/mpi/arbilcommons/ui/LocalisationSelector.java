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
package nl.mpi.arbilcommons.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import nl.mpi.flap.plugin.PluginSessionStorage;

/**
 * Created on : Sep 9, 2013, 10:34:40 AM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class LocalisationSelector {

    private static final String SELECTED_LOCALE_KEY = "selectedLocale";
    private static final String SYSTEM_DEFAULT = "<system default>";
    private final PluginSessionStorage sessionStorage;
    // list of known locales which might differ between applications that use this local selection widget
    private final List<Locale> knownLocales;

    public LocalisationSelector(PluginSessionStorage sessionStorage, String[] availableLocales) {
	this.sessionStorage = sessionStorage;
	knownLocales = new ArrayList<Locale>();
	for (String locale : availableLocales) {
	    knownLocales.add(createLocale(locale));
	}
    }

    public LocalisationSelector(PluginSessionStorage sessionStorage, List<Locale> knownLocales) {
	this.sessionStorage = sessionStorage;
	this.knownLocales = knownLocales;
    }

    private Locale getSavedLocale() {
	final String selectedLocaleString = sessionStorage.loadString(SELECTED_LOCALE_KEY);
	if (selectedLocaleString == null || selectedLocaleString.equals(SYSTEM_DEFAULT)) {
	    return null;
	} else {
	    return createLocale(selectedLocaleString);
	}
    }

    private void setSavedLocale(Locale locale) {
	if (locale == null) {
	    sessionStorage.saveString(SELECTED_LOCALE_KEY, SYSTEM_DEFAULT);
	} else {
	    sessionStorage.saveString(SELECTED_LOCALE_KEY, locale.toString());
	}
    }

    public boolean askUser(JFrame jFrame, Icon icon, String please_select_your_preferred_language, String language_Selection, String system_Default) {
	LocaleOption[] possibilities = new LocaleOption[knownLocales.size() + 1];
	possibilities[0] = new LocaleOption(system_Default);
	int localeIndex = 1;
	// loop the locales and add them to posibilities
	for (Locale locale : knownLocales) {
	    possibilities[localeIndex] = new LocaleOption(locale);
	    localeIndex++;
	}
	final Locale savedLocale = getSavedLocale();
	final LocaleOption defaultValue = (savedLocale == null) ? new LocaleOption(system_Default) : new LocaleOption(savedLocale);
	LocaleOption userSelection = (LocaleOption) JOptionPane.showInputDialog(
		jFrame, please_select_your_preferred_language, language_Selection,
		JOptionPane.PLAIN_MESSAGE,
		icon,
		possibilities, defaultValue);

	if ((userSelection != null)) {
	    if (userSelection.getLocale() == null) {
		final boolean modified = getSavedLocale() != null;
		setSavedLocale(null);
		return modified;
	    } else {
		setSavedLocale(userSelection.getLocale());
		return !userSelection.getLocale().equals(savedLocale);
	    }
	}
	return false;
    }

    public boolean hasSavedLocal() {
	final String selectedLocaleString = sessionStorage.loadString(SELECTED_LOCALE_KEY);
	return selectedLocaleString != null && selectedLocaleString.length() > 0;
    }

    public void setLanguageFromSaved() {
	final Locale savedLocale = getSavedLocale();
	if (savedLocale != null) {
	    Locale.setDefault(savedLocale);
	}
    }

    private static Locale createLocale(String localeString) {
	if (localeString.contains("_")) {
	    // language and country codes, e.g. en_US
	    final String[] localeTokens = localeString.split("_");
	    return new Locale(localeTokens[0], localeTokens[1]);
	} else {
	    // only language code
	    return new Locale(localeString);
	}
    }
}
