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
package nl.mpi.arbilcommons.journal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import nl.mpi.arbilcommons.model.HistoryField;
import nl.mpi.flap.plugin.JournalWatcherPlugin;
import nl.mpi.flap.plugin.PluginDialogHandler;
import nl.mpi.flap.plugin.PluginException;
import nl.mpi.flap.plugin.PluginJournal;
import nl.mpi.flap.plugin.PluginSessionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document : ArbilJournal Created on :
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilJournal implements PluginJournal {

    private final static Logger logger = LoggerFactory.getLogger(ArbilJournal.class);
    private static PluginDialogHandler messageDialogHandler;
    private final HashSet<JournalWatcherPlugin> jounalWatchers;

    public static void setMessageDialogHandler(PluginDialogHandler handler) {
	messageDialogHandler = handler;
    }
    private static PluginSessionStorage sessionStorage;

    public static void setSessionStorage(PluginSessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }

    private File getJournalFile() {
	return new File(sessionStorage.getProjectDirectory(), "ChangeJournal.log");
    }

    public enum UndoType {

	Value, LanguageId, KeyName
    }

    private static class HistoryItem {

	HistoryField targetField;
	String oldValue;
	String newValue;
	UndoType undoType;
    }

    private ArbilJournal() {
	jounalWatchers = new HashSet<JournalWatcherPlugin>();
    }
    static private ArbilJournal singleInstance = null;

    static synchronized public ArbilJournal getSingleInstance() {
	if (singleInstance == null) {
	    singleInstance = new ArbilJournal();
	}
	return singleInstance;
    }
    ArrayList<HistoryItem> fieldChangeHistory;
    int currentFieldChangeHistoryItem = 0;

    public synchronized void recordFieldChange(HistoryField targetField, String oldValue, String newValue, UndoType undoType) {
	if (fieldChangeHistory == null) {
	    fieldChangeHistory = new ArrayList<HistoryItem>();
	    currentFieldChangeHistoryItem = 0;
	}
	if (currentFieldChangeHistoryItem < fieldChangeHistory.size()) {
	    fieldChangeHistory = new ArrayList(fieldChangeHistory.subList(0, currentFieldChangeHistoryItem));
	}
	HistoryItem historyItem = new HistoryItem();
	historyItem.targetField = targetField;
	historyItem.oldValue = oldValue;
	historyItem.newValue = newValue;
	historyItem.undoType = undoType;
	fieldChangeHistory.add(historyItem);
	currentFieldChangeHistoryItem++;
    }

    public boolean canUndo() {
	//fieldChangeHistory.size();
	return currentFieldChangeHistoryItem > 0;
    }

    public boolean canRedo() {
	return fieldChangeHistory != null && currentFieldChangeHistoryItem < fieldChangeHistory.size();
    }

    public void undoFromFieldChangeHistory() {
	if (canUndo()) {
	    HistoryItem changeHistoryItem = fieldChangeHistory.get(--currentFieldChangeHistoryItem);
	    HistoryItem reversedHistoryItem = new HistoryItem();
	    reversedHistoryItem.newValue = changeHistoryItem.oldValue;
	    reversedHistoryItem.oldValue = changeHistoryItem.newValue;
	    reversedHistoryItem.targetField = changeHistoryItem.targetField;
	    reversedHistoryItem.undoType = changeHistoryItem.undoType;
	    makeChangeFromHistoryItem(reversedHistoryItem);
	}
    }

    public void redoFromFieldChangeHistory() {
	if (canRedo()) {
	    HistoryItem changeHistoryItem = fieldChangeHistory.get(currentFieldChangeHistoryItem++);
	    makeChangeFromHistoryItem(changeHistoryItem);
	}
    }

    public void clearFieldChangeHistory() {
	fieldChangeHistory = null;
	currentFieldChangeHistoryItem = 0;
    }

    private void makeChangeFromHistoryItem(HistoryItem historyItem) {
	String currentValue = null;
	switch (historyItem.undoType) {
	    case KeyName:
		currentValue = historyItem.targetField.getKeyName();
		break;
	    case LanguageId:
		currentValue = historyItem.targetField.getLanguageId();
		break;
	    case Value:
		currentValue = historyItem.targetField.getFieldValue();
		break;
	}
	if (currentValue != null && !currentValue.equals(historyItem.oldValue)) {
	    messageDialogHandler.addMessageDialogToQueue("The field value is out of sync with the history item", "Undo/Redo");
	    logger.error("ChangeFromHistory old value does not match current value");
	} else {
	    switch (historyItem.undoType) {
		case KeyName:
		    historyItem.targetField.setKeyName(historyItem.newValue, true, true);
		    break;
		case LanguageId:
		    historyItem.targetField.setLanguageId(historyItem.newValue, true, true);
		    break;
		case Value:
		    historyItem.targetField.setFieldValue(historyItem.newValue, true, true);
		    break;
	    }
	}
    }

    // this is also use to record an import event
    public boolean saveJournalEntry(String imdiUrl, String imdiNodePath, String oldValue, String newValue, String eventType) {
	boolean returnValue = false;
	FileWriter journalFile = null;
	try {
	    journalFile = new FileWriter(getJournalFile(), true);
	    logger.debug("Journal: " + imdiUrl + "," + imdiNodePath + "," + oldValue + "," + newValue);
	    journalFile.append("\"" + imdiUrl + "#" + imdiNodePath + "\",\"" + oldValue + "\",\"" + newValue + "\",\"" + eventType + "\"\n");
	    journalFile.close();
	    journalFile = null;
	    returnValue = true;
	    if ("save".equals(eventType)) {
		wakeJounalWatchers(getJournalFile().length());
	    }
	} catch (IOException ex) {
	    returnValue = false;
	    logger.error("failed to write to the journal", ex);
	    System.err.println("failed to write to the journal: " + ex.getMessage());
	} finally {
	    if (journalFile != null) {
		try {
		    journalFile.close();
		} catch (IOException ioe) {
		    logger.error("Failed to close the journal", ioe);
		}
	    }
	}
	return (returnValue);
    }

    synchronized public long getChangedFiles(long lastChangeIndex, Set<String> changedURIs) throws PluginException {
	try {
	    final File journalFile = getJournalFile();
	    final long journalLength = journalFile.length();
	    if (journalLength > lastChangeIndex) {
		final FileReader fileReader = new FileReader(journalFile);
		LineNumberReader lineNumberReader = new LineNumberReader(fileReader);
		lineNumberReader.skip(lastChangeIndex);
		String readLine;
		while (null != (readLine = lineNumberReader.readLine())) {
		    if (readLine.endsWith(",\"save\"")) {
			// extract the URI
			// todo: note that old versions of Arbil often ommit the # as follows:
//                        "file:/Users/petwit2/.arbil/ArbilWorkingFiles/20121221145203/20121221145203.cmdi.CMD.Components.AnnotationTool.applicationType","","dfdfsdf","save"
//                        "file:/Users/petwit2/.arbil/ArbilWorkingFiles/20121207173739/20121207173739/20121207182709.imdi.METATRANSCRIPT.Session.MDGroup.Location.Continent","Unspecified","Australia","save"
			String urlString = readLine.substring(1);
			urlString = urlString.split("#", 2)[0];
			changedURIs.add(urlString);
		    }
		}
	    }
	    return journalLength;
	} catch (FileNotFoundException exception) {
	    throw new PluginException("Failed to read the journal file: " + exception.getMessage());
	} catch (IOException exception) {
	    throw new PluginException("Failed to read the journal file: " + exception.getMessage());
	}
    }

    private void wakeJounalWatchers(final long journalLength) {
	new Thread(new Runnable() {
	    public void run() {
		for (JournalWatcherPlugin jounalWatcher : jounalWatchers) {
		    try {
			jounalWatcher.journalEvent(journalLength);
		    } catch (PluginException exception) {
			messageDialogHandler.addMessageDialogToQueue("Journal watcher plugin failed and has been removed:\n" + exception.getMessage(), "Wake Jounal Watchers");
			jounalWatchers.remove(jounalWatcher);
		    }
		}
	    }
	}, "JounalWatcherPlugins").start();
    }

    public void addJounalWatcher(JournalWatcherPlugin jounalWatcher) {
	jounalWatchers.add(jounalWatcher);
    }
}
