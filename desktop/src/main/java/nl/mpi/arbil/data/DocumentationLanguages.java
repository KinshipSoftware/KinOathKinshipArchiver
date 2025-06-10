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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import nl.mpi.arbil.userstorage.SessionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Peter Withers
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class DocumentationLanguages {

    private final static Logger logger = LoggerFactory.getLogger(DocumentationLanguages.class);
    private final String selectedLanguagesKey;
    private final SessionStorage sessionStorage;
    private final static String ALL_LANGUAGES = "_ALL_LANGUAGES_";

    public DocumentationLanguages(String selectedLanguagesKey, SessionStorage sessionStorage) {
	this.selectedLanguagesKey = selectedLanguagesKey;
	this.sessionStorage = sessionStorage;
    }

    private List<String> getDefaultLanguages() {
	final List<ArbilVocabularyItem> imdiLanguages = getAllLanguages();
	List<String> selectedLanguages = new ArrayList<String>(imdiLanguages.size());
	for (ArbilVocabularyItem currentTemplate : imdiLanguages) {
	    selectedLanguages.add(currentTemplate.itemDisplayName);
	}
	return selectedLanguages;
    }

    public abstract List<ArbilVocabularyItem> getAllLanguages();

    protected synchronized List<ArbilVocabularyItem> getLanguageListSubset(List<ArbilVocabularyItem> allLanguages) {
	final List<ArbilVocabularyItem> languageListSubset = new ArrayList<ArbilVocabularyItem>();
	if (allLanguages != null) {
	    final List<String> selectedLanguages = getSelectedLanguagesOrDefault();
	    for (ArbilVocabularyItem currentVocabItem : allLanguages) {
		if (selectedLanguages.contains(currentVocabItem.itemDisplayName)) {
		    languageListSubset.add(currentVocabItem);
		}
	    }
	}
	return languageListSubset;
    }

    /**
     *
     * @return Sorted selection of documentation languages
     */
    public abstract List<ArbilVocabularyItem> getSortedLanguageListSubset();

    /**
     *
     * @return a newly instantiated <em>mutable</em> list of the stored selected languages, null if nothing was stored
     */
    private List<String> getSelectedLanguages() {
	try {
	    final String[] storedLanguages = sessionStorage.loadStringArray(selectedLanguagesKey);
	    if (storedLanguages != null) {
		if (storedLanguages.length == 1 && storedLanguages[0].equals(ALL_LANGUAGES)) {
		    // wildcard for all available languages
		    return getDefaultLanguages();
		}
		return new ArrayList<String>(Arrays.asList(storedLanguages));
	    }
	} catch (Exception e) {
	    // Most likely an IOException
	    logger.error("Error while trying to read language file, falling back to defaults", e);
	}
	return null;
    }

    public synchronized List<String> getSelectedLanguagesOrDefault() {
	final List<String> storedLanguages = getSelectedLanguages();
	if (storedLanguages == null) {
	    return getDefaultLanguages();
	} else {
	    return storedLanguages;
	}
    }

    public synchronized void addselectedLanguage(String templateString) {
	final List<String> selectedLanguages = getSelectedLanguages();
	if (selectedLanguages == null) {
	    saveSelectedLanguages(Collections.singletonList(templateString));
	} else {
	    selectedLanguages.add(templateString);
	    saveSelectedLanguages(selectedLanguages);
	}
    }

    public synchronized void removeselectedLanguages(String templateString) {
	final List<String> selectedLanguages = getSelectedLanguagesOrDefault();
	while (selectedLanguages.contains(templateString)) {
	    selectedLanguages.remove(templateString);
	}
	saveSelectedLanguages(selectedLanguages);
    }

    protected void saveSelectedLanguages(List<String> selectedLanguages) {
	try {
	    if (selectedLanguages.containsAll(getDefaultLanguages())) {
		// Store wildcard for all languages
		sessionStorage.saveStringArray(selectedLanguagesKey, new String[]{ALL_LANGUAGES});
	    } else {
		sessionStorage.saveStringArray(selectedLanguagesKey, selectedLanguages.toArray(new String[]{}));
	    }
	} catch (IOException ex) {
	    logger.error("Could not store language selection", ex);
	}
    }
}
