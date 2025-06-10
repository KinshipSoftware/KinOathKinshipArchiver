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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nl.mpi.arbil.userstorage.SessionStorage;

/**
 * Document : ImdiDocumentationLanguages
 * Created on : Jul 6, 2010, 4:05:46 PM
 *
 * @author Peter Withers
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ImdiDocumentationLanguages extends DocumentationLanguages implements ArbilVocabularyFilter {

    private SessionStorage sessionStorage;
    private static final String IMDI_LANGUAGE_VOCABULARY_URL_KEY = "LanguageVocabularyUrl";
    private static final String SELECTED_LANGUAGES_KEY = "selectedLanguages";
    private static final String OLD_MPI_LANGUAGE_VOCABULARY_URL = "http://www.mpi.nl/IMDI/Schema/ISO639-2Languages.xml";
    private static final String MPI_LANGUAGE_VOCABULARY_URL = "http://www.mpi.nl/IMDI/Schema/MPI-Languages.xml";
    private static String imdiLanguageVocabularyUrl = null;

    public synchronized String getLanguageVocabularyUrlForImdi() {
	if (imdiLanguageVocabularyUrl == null) {
	    imdiLanguageVocabularyUrl = sessionStorage.loadString(IMDI_LANGUAGE_VOCABULARY_URL_KEY);
	    if (imdiLanguageVocabularyUrl == null || imdiLanguageVocabularyUrl.equals(OLD_MPI_LANGUAGE_VOCABULARY_URL)) {
		imdiLanguageVocabularyUrl = MPI_LANGUAGE_VOCABULARY_URL;
		sessionStorage.saveString(IMDI_LANGUAGE_VOCABULARY_URL_KEY, imdiLanguageVocabularyUrl);
	    }
	}
	return imdiLanguageVocabularyUrl;
    }

    public ImdiDocumentationLanguages(SessionStorage sessionStorage) {
	super(SELECTED_LANGUAGES_KEY, sessionStorage);
	this.sessionStorage = sessionStorage;
    }

    public synchronized List<ArbilVocabularyItem> getAllLanguages() {
	return IMDIVocabularies.getSingleInstance().getVocabulary(null, getLanguageVocabularyUrlForImdi()).getVocabularyItemsUnfiltered();
    }

    public List<ArbilVocabularyItem> getSortedLanguageListSubset() {
	//TODO: Do this sorting only when required (i.e. cache sorted list)
	List<ArbilVocabularyItem> languages = getLanguageListSubset(getAllLanguages());
	Collections.sort(languages);
	return languages;
    }

    public List<ArbilVocabularyItem> filterVocabularyItems(List<ArbilVocabularyItem> items) {
	List<ArbilVocabularyItem> vocabClone = new ArrayList<ArbilVocabularyItem>(items);
	vocabClone.retainAll(getSortedLanguageListSubset());
	return vocabClone;
    }
}
