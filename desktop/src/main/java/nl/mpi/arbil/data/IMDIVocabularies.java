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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.ResourceBundle;
import javax.swing.ProgressMonitor;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.DownloadAbortFlag;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Document : ArbilVocabularies
 * Created on :
 *
 * @author Peter.Withers@mpi.nl
 */
public class IMDIVocabularies {

    private final static Logger logger = LoggerFactory.getLogger(IMDIVocabularies.class);
    private final ResourceBundle services = java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Services");

    private static MessageDialogHandler messageDialogHandler;
    /**
     * Update IMDI vocabularies from server every two weeks
     */
    public static final int VOCABULARY_UPDATE_FREQUENCY = 14;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }
    private static WindowManager windowManager;

    public static void setWindowManager(WindowManager windowManagerInstance) {
	windowManager = windowManagerInstance;
    }
    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private Hashtable<String, ArbilVocabulary> vocabulariesTable = new Hashtable<String, ArbilVocabulary>();
    // IMDIVocab has been abandoned, the mpi.vocabs.IMDIVocab class is too scary
    // every time IMDIVocab in the API is called (correction its in a static stansa ARRRGGG) it downloads the mpi homepage before anything else
    // mpi.vocabs.IMDIVocab cv = mpi.vocabs.IMDIVocab.get(vocabularyLocation);
    // cv.cslist2array(vocabularyLocation)
    // the output of the following looks similar however they differ slightly and only a few have comments
//        logger.debug("CVentries: "+cv.getCVentries());
//        logger.debug("NameEntries: "+cv.getNameEntries());
//        logger.debug("ValueEntries: "+cv.getValueEntries()); // this one is has comments
//        logger.debug("VocabHashtable: "+cv.getVocabHashtable());
//        logger.debug("Vocabs: "+cv.getVocabs());
//        logger.debug("DescriptionEntries: "+cv.getDescriptionEntries());
//        logger.debug("Entries: "+cv.getEntries());      
//    }
    static private IMDIVocabularies singleInstance = null;

    static synchronized public IMDIVocabularies getSingleInstance() {
	if (singleInstance == null) {
	    singleInstance = new IMDIVocabularies();
	}
	return singleInstance;
    }

    private IMDIVocabularies() {
    }

    public boolean vocabularyContains(String vocabularyLocation, String valueString) {
	if (!vocabulariesTable.containsKey(vocabularyLocation)) {
	    parseRemoteFile(vocabularyLocation);
	}
	ArbilVocabulary tempVocab = vocabulariesTable.get(vocabularyLocation);
	if (tempVocab != null) {
	    return (null != tempVocab.findVocabularyItem(valueString));
	} else {
	    return false;
	}
    }

    public void redownloadCurrentlyLoadedVocabularies() {
	new Thread() {
	    @Override
	    public void run() {
		int succeededCount = 0;
		int failedCount = 0;
		ProgressMonitor progressMonitor = windowManager.newProgressMonitor(services.getString("DOWNLOADING CURRENTLY LOADED VOCABULARIES"), "", 0, vocabulariesTable.size());
		for (String currentUrl : vocabulariesTable.keySet()) {
		    if (sessionStorage.replaceCacheCopy(currentUrl)) {
			succeededCount++;
		    } else {
			failedCount++;
		    }
		    if (progressMonitor.isCanceled()) {
			progressMonitor.close();
			break;
		    }
		    progressMonitor.setNote(MessageFormat.format(services.getString("DOWNLOADED: {0} FAILED: {1}"), succeededCount, failedCount));
		    progressMonitor.setProgress(succeededCount);
		}
		progressMonitor.close();
		messageDialogHandler.addMessageDialogToQueue(MessageFormat.format(services.getString("DOWNLOADED {0} OUT OF THE {1} VOCABULARIES CURRENTLY IN USE YOU WILL NEED TO RESTART THE APPLICATION FOR THE NEW VOCABULARIES TO TAKE EFFECT"), succeededCount, vocabulariesTable.size()),java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Services").getString("RE-DOWNLOAD CURRENT VOCABULARIES"));
	    }
	}.start();
    }

    public ArbilVocabulary getVocabulary(ArbilField originatingArbilField, String vocabularyLocation) {
	// todo the MPI-Languages vocabularies should use the DocumentationLanguages().getLanguageListSubset(); class that provides a sub set list of languages
	if (originatingArbilField != null) {
	    if (vocabularyLocation == null) {// || vocabularyLocation.length() == 0) {
		return null;
	    }
	    String fieldPath = originatingArbilField.getGenericFullXmlPath();
	    // testing code for checking the language fields have the required triggers
	    if (vocabularyLocation.endsWith("MPI-Languages.xml")) {
		boolean foundTrigger = false;
		for (String[] currentTrigger : originatingArbilField.getParentDataNode().getNodeTemplate().getFieldTriggersArray()) {
		    if (fieldPath.equals(currentTrigger[0])) {
			foundTrigger = true;
		    }
		}
		if (!foundTrigger) {
		    if (!fieldPath.equals(".METATRANSCRIPT.Session.Resources.LexiconResource(x).MetaLanguages.Language")) {
			logger.error("Missing Field Trigger for: {} in {}", fieldPath, originatingArbilField.getParentDataNode().getUrlString());
		    }
		}
	    }
	    ///////////////////////////////
	    // look for genre / sub genre redirects in the template
	    String vocabularyRedirectField = null;
	    for (String[] currentRedirect : originatingArbilField.getParentDataNode().getNodeTemplate().getGenreSubgenreArray()) {
		if (fieldPath.equals(currentRedirect[0])) {
		    vocabularyRedirectField = currentRedirect[1];
		}
	    }
	    if (vocabularyRedirectField != null) {
		// todo: check that genre /subgenre are being linked correctly
		ArbilField[] tempField = originatingArbilField.getSiblingField(vocabularyRedirectField);
		if (tempField != null) {
		    String redirectFieldString = tempField[0].getFieldValue();
		    // TODO: this may need to put the (\d) back into the (x) as is done for the FieldChangeTriggers
		    ArbilVocabulary tempVocabulary = tempField[0].getVocabulary();
		    ArbilVocabularyItem redirectFieldVocabItem = tempVocabulary.findVocabularyItem(redirectFieldString);
		    if (redirectFieldVocabItem != null && redirectFieldVocabItem.followUpVocabulary != null) {
			String correctedUrl = tempVocabulary.resolveFollowUpUrl(redirectFieldVocabItem.followUpVocabulary);
			// change the requested vocabulary string to the redirected value
			vocabularyLocation = correctedUrl;
		    }
		}
	    }
	}
	///////////////////////////////
	if (vocabularyLocation == null || vocabularyLocation.length() == 0) {
	    return null;
	} else {
	    if (!vocabulariesTable.containsKey(vocabularyLocation)) {
		parseRemoteFile(vocabularyLocation);
		setLanguageFilter(vocabulariesTable.get(vocabularyLocation), originatingArbilField, vocabularyLocation);
	    }
	    return vocabulariesTable.get(vocabularyLocation);
	}
    }

    private void setLanguageFilter(final ArbilVocabulary vocabulary, final ArbilField originatingArbilField, final String vocabularyLocation) {
	if (vocabulary != null && originatingArbilField != null) {
	    DocumentationLanguages documentationLanguages = originatingArbilField.getParentDataNode().getNodeTemplate().getDocumentationLanguages();
	    if (documentationLanguages instanceof ImdiDocumentationLanguages) {
		if (vocabularyLocation.equals(((ImdiDocumentationLanguages) documentationLanguages).getLanguageVocabularyUrlForImdi())) {
		    vocabulary.setFilter((ImdiDocumentationLanguages) documentationLanguages);
		}
	    }
	}
    }

    synchronized public void parseRemoteFile(String vocabRemoteUrl) {
	if (vocabRemoteUrl != null && !vocabulariesTable.containsKey(vocabRemoteUrl)) {
	    File cachedFile = sessionStorage.updateCache(vocabRemoteUrl, VOCABULARY_UPDATE_FREQUENCY, false);
	    if (!cachedFile.exists()) {
		String backupPath = "/nl/mpi/arbil/resources/IMDI/FallBack/" + cachedFile.getName();
		URL backUp = this.getClass().getResource(backupPath);
		if (backUp != null) {
		    sessionStorage.saveRemoteResource(backUp, cachedFile, null, true, false, new DownloadAbortFlag(), null);
		}
	    }
	    ArbilVocabulary vocabulary = new ArbilVocabulary(vocabRemoteUrl);
	    vocabulariesTable.put(vocabRemoteUrl, vocabulary);
	    try {
		final SAXParserFactory saxParserFactory = javax.xml.parsers.SAXParserFactory.newInstance();
		final SAXParser saxParser = saxParserFactory.newSAXParser();
		final XMLReader xmlReader = saxParser.getXMLReader();
		xmlReader.setFeature("http://xml.org/sax/features/validation", false);
		xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
		///////////////////////////////////////////////////////////////////////
		// non utf-8 version
//                xmlReader.setContentHandler(new SaxVocabularyHandler(vocabulary));
//                xmlReader.parse(cachedFile.getCanonicalPath());
		///////////////////////////////////////////////////////////////////////
		// utf-8 version
		// todo: here we could test if the file is utf-8 and log an error if it is not
		InputStream inputStream = new FileInputStream(cachedFile);
		Reader reader = new InputStreamReader(inputStream, "UTF-8");
		InputSource inputSource = new InputSource(reader);
		inputSource.setEncoding("UTF-8");
		saxParser.parse(inputSource, new SaxVocabularyHandler(vocabulary));
		///////////////////////////////////////////////////////////////////////
	    } catch (Exception ex) {
		messageDialogHandler.addMessageDialogToQueue(MessageFormat.format(services.getString("A CONTROLLED VOCABULARY COULD NOT BE READ SOME FIELDS MAY NOT SHOW ALL OPTIONS"), vocabRemoteUrl),java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Services").getString("LOAD CONTROLLED VOCABULARY"));
		logger.error("A controlled vocabulary could not be read: {}", vocabRemoteUrl, ex);
	    }
	}
    }

    private static class SaxVocabularyHandler extends org.xml.sax.helpers.DefaultHandler {

	private ArbilVocabulary collectedVocab;
	private ArbilVocabularyItem currentVocabItem = null;

	public SaxVocabularyHandler(ArbilVocabulary vocabList) {
	    super();
	    collectedVocab = vocabList;
	}

	@Override
	public void characters(char[] charArray, int start, int length) {
	    if (currentVocabItem != null) {
		StringBuilder nodeContents = new StringBuilder();
		for (int charCounter = start; charCounter < start + length; charCounter++) {
		    nodeContents.append(charArray[charCounter]);
		}
		currentVocabItem.descriptionString = nodeContents.toString();
	    }
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
	    currentVocabItem = null;
	}

	@Override
	public void startElement(String uri, String name, String qName, org.xml.sax.Attributes atts) {
//            this VocabularyRedirect code has been replaced by the templates
//            if (name.equals("VocabularyRedirect")) { // or should this be Redirect
//                // when getting the list check attribute in the field X for the vocab location
//                collectedVocab.vocabularyRedirectField = atts.getValue("SourceFieldName");
//                logger.debug("VocabularyRedirect: " + collectedVocab.vocabularyRedirectField);
//            }
	    if (name.equals("Entry")) {
		String vocabName = atts.getValue("Value");
		String vocabCode = atts.getValue("Code");
		String followUpVocab = atts.getValue("FollowUp");
		if (vocabName != null) {
		    currentVocabItem = new ArbilVocabularyItem(vocabName, vocabCode, followUpVocab);
		    collectedVocab.getVocabularyItemsUnfiltered().add(currentVocabItem);
		} else {
		    logger.error("Vocabulary item has no name in {}", collectedVocab.getVocabularyUrl());
		}
	    }
	}
    }
}
