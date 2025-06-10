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
package nl.mpi.arbil.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilNodeSearchColumnComboBox extends JComboBox {

    private List<String> options;
    private TypeAheadComboBoxEditor typeAheadEditor;
    private final static String[] defaultOptions = new String[]{"Name", "Title", "Description", "Genre", "Subject", "Task"};
    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }

    public ArbilNodeSearchColumnComboBox() {
	this("", "");
    }

    public ArbilNodeSearchColumnComboBox(String initialValue, String originalValue) {
	super();

	try {
	    String[] optionsArray = sessionStorage.loadStringArray("searchFieldOptions");
	    if (optionsArray != null) {
		options = new ArrayList(Arrays.asList(optionsArray));
	    }
	} catch (IOException ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
	if (options == null) {
	    options = new ArrayList<String>(Arrays.asList(defaultOptions));
	}

	for (String option : options) {
	    addItem(option);
	}

	typeAheadEditor = new ArbilNodeSearchColumnComboBoxEditor(initialValue, originalValue);
	setEditor(typeAheadEditor);
    }

    /**
     * Adds option to the current list. To permanently add, call static addOptions method
     * @param option 
     * @see addOptions()
     */
    public synchronized void addOption(String option) {
	if (!options.contains(option)) {
	    options.add(option);
	    addItem(option);
	}
    }

    public static synchronized void addOptions(Collection<String> options) {
	try {
	    Collection<String> existingOptions;
	    String[] optionsArray = sessionStorage.loadStringArray("searchFieldOptions");
	    if (optionsArray != null) {
		existingOptions = Arrays.asList(optionsArray);
	    } else {
		existingOptions = new ArrayList<String>(Arrays.asList(defaultOptions));
	    }
	    ArrayList<String> newOptions = new ArrayList<String>(existingOptions);
	    for (String option : options) {
		if (option != null && option.length() > 0 && !existingOptions.contains(option)) {
		    newOptions.add(option);
		}
	    }
	    sessionStorage.saveStringArray("searchFieldOptions", newOptions.toArray(new String[]{}));
	} catch (IOException ex) {
	    BugCatcherManager.getBugCatcher().logError("Could not save search options", ex);
	}
    }

    public static synchronized void removeOptions(Collection<String> options) {
	try {
	    Collection<String> existingOptions;
	    String[] optionsArray = sessionStorage.loadStringArray("searchFieldOptions");
	    if (optionsArray != null) {
		existingOptions = Arrays.asList(optionsArray);
	    } else {
		existingOptions = new ArrayList<String>(Arrays.asList(defaultOptions));
	    }
	    ArrayList<String> newOptions = new ArrayList<String>(existingOptions);
	    for (String option : options) {
		if (option != null && existingOptions.contains(option)) {
		    newOptions.remove(option);
		}
	    }
	    sessionStorage.saveStringArray("searchFieldOptions", newOptions.toArray(new String[]{}));
	} catch (IOException ex) {
	    BugCatcherManager.getBugCatcher().logError("Could not save search options", ex);
	}
    }

    public String getText() {
	return typeAheadEditor.getCurrentValue();
    }

    public void setText(String text) {
	typeAheadEditor.setItem(text);
    }

    public JTextField getTextField() {
	return typeAheadEditor.getTextField();
    }

    private class ArbilNodeSearchColumnComboBoxEditor extends TypeAheadComboBoxEditor {

	public ArbilNodeSearchColumnComboBoxEditor(String value, String originalValue) {
	    super(new JTextField(value), value, originalValue, ArbilNodeSearchColumnComboBox.this);

	    init();
	}

	@Override
	protected String getItemAt(int index) {
	    return options.get(index);
	}

	@Override
	protected int getItemsCount() {
	    return options.size();
	}

	@Override
	protected boolean isList() {
	    return false;
	}

	@Override
	protected boolean isOpen() {
	    return true;
	}

	@Override
	protected boolean isItemsDeletable() {
	    return true;
	}

	@Override
	protected synchronized boolean deleteItem(Object item) {
	    if (item instanceof String) {
		if (options.remove((String) item)) {
		    ArbilNodeSearchColumnComboBox.removeOptions(Collections.singleton((String) item));
		    removeItem(item);
		    return true;
		}
	    }
	    return false;
	}
    }
}
