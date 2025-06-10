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

import java.util.Collections;
import java.util.List;
import nl.mpi.arbil.clarin.profiles.CmdiTemplate;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.userstorage.SessionStorage;

/**
 * Document : CmdiDocumentationLanguages
 * Created on : Jul 6, 2010, 4:05:46 PM
 *
 * @author Peter Withers
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CmdiDocumentationLanguages extends DocumentationLanguages {

    private final SessionStorage sessionStorage;
    private static final String CMDI_LANGUAGE_VOCABULARY_URL_KEY = "CmdiLanguageVocabularyUrl";
    private static final String CMDI_LANGUAGE_VOCABULARY_PATH_KEY = "CmdiLanguageVocabularyPath";
    private static final String SELECTED_LANGUAGES_KEY = "selectedLanguagesCmdi";
    public static final String CMDI_VOCABULARY_URL = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438110/xsd";
    public static final String CMDI_VOCABULARY_PATH = ".CMD.Components.ISO639.iso-639-3-code";
    private static String cmdiLanguageVocabularyUrl = null;
    private static String cmdiLanguageVocabularyPath = null;
    private List<ArbilVocabularyItem> languages;
    private List<ArbilVocabularyItem> sortedLanguages;

    public synchronized String getLanguageVocabularyUrl() {
	if (cmdiLanguageVocabularyUrl == null) {
	    cmdiLanguageVocabularyUrl = sessionStorage.loadString(CMDI_LANGUAGE_VOCABULARY_URL_KEY);
	    if (cmdiLanguageVocabularyUrl == null) {
		cmdiLanguageVocabularyUrl = CMDI_VOCABULARY_URL;
		sessionStorage.saveString(CMDI_LANGUAGE_VOCABULARY_URL_KEY, cmdiLanguageVocabularyUrl);
	    }
	}
	return cmdiLanguageVocabularyUrl;
    }

    public synchronized String getLanguageVocabularyPath() {
	if (cmdiLanguageVocabularyPath == null) {
	    cmdiLanguageVocabularyPath = sessionStorage.loadString(CMDI_LANGUAGE_VOCABULARY_PATH_KEY);
	    if (cmdiLanguageVocabularyPath == null) {
		cmdiLanguageVocabularyPath = CMDI_VOCABULARY_PATH;
		sessionStorage.saveString(CMDI_LANGUAGE_VOCABULARY_PATH_KEY, cmdiLanguageVocabularyPath);
	    }
	}
	return cmdiLanguageVocabularyPath;
    }

    public CmdiDocumentationLanguages(final SessionStorage sessionStorage) {
	super(SELECTED_LANGUAGES_KEY, sessionStorage);
	this.sessionStorage = sessionStorage;
    }

    public synchronized List<ArbilVocabularyItem> getAllLanguages() {
	// Assumption is that language list doesn't change
	if (languages == null) {
	    languages = getLanguages();
	}
	return languages;
    }

    private synchronized List<ArbilVocabularyItem> getLanguages() {
	CmdiTemplate profile = (CmdiTemplate) ArbilTemplateManager.getSingleInstance().getCmdiTemplate(getLanguageVocabularyUrl());
	if (profile != null) {
	    ArbilVocabulary vocab = profile.getFieldVocabulary(getLanguageVocabularyPath());
	    if (vocab != null) {
		return vocab.getVocabularyItems();
	    }
	}
	return null;
    }

    public synchronized List<ArbilVocabularyItem> getSortedLanguageListSubset() {
	// No subset for CMDI yet, selection from dialog only applies to IMDI
	if (sortedLanguages == null) {
	    // TODO: Subset here as soon as feature (language selection dialog for CMDI) available
	    sortedLanguages = getAllLanguages();
	    if (sortedLanguages != null) {
		Collections.sort(sortedLanguages);
	    }
	}
	return sortedLanguages;
    }
}
