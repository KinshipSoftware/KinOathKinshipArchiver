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
package nl.mpi.arbil.ui.fieldeditors;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilVocabularyItem;
import nl.mpi.arbil.util.BugCatcherManager;

/**
 * Document : LanguageIdBox
 * Created on : Sep 14, 2010, 3:58:33 PM
 * Author : Peter Withers
 */
public class LanguageIdBox extends JComboBox {

    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");
    public final static int languageSelectWidth = 100;
    private final static String defaultLanguageDropDownValue = widgets.getString("<SELECT>");
    private final static String initializingDropDownValue = widgets.getString("INITIALIZING...");
    private final ArbilField cellField;

    /**
     * Constucts a language id box for a field. After construction, the box will be disabled and containg a single "initializing..." item.
     * Call {@link #init() } to make it ready for use.
     *
     * @param cellField
     * @param parentCellRect
     * @see #init()
     */
    public LanguageIdBox(ArbilField cellField, Rectangle parentCellRect) {
	this.cellField = cellField;
	this.setEditable(false);
	if (parentCellRect != null) {
	    this.setPreferredSize(new Dimension(languageSelectWidth, parentCellRect.height));
	}

	addItem(initializingDropDownValue);
	// Disable until initialized
	setEnabled(false);
    }

    /**
     * Adds all items to the box and sets the selection according to the field model. When finished, enables the box.
     * <strong>Call only once</strong>.
     */
    public void init() {

	new Thread() {
	    @Override
	    public void run() {
		final String fieldLanguageId = cellField.getLanguageId();
		final List<ArbilVocabularyItem> languageItemArray = cellField.getDocumentationLanguages().getSortedLanguageListSubset();
		if (languageItemArray != null) {
		    // Load and add language items
		    setItems(languageItemArray, fieldLanguageId);
		    // Add the action listener, should be done on EDT
		    SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    setActionListener();
			    // Box was disabled on construction, now initialization is complete we can re-enable
			    setEnabled(true);
			}
		    });
		}
	    }
	}.start();
    }

    private void setItems(final List<ArbilVocabularyItem> languageItemArray, final String fieldLanguageId) {
	final ArbilVocabularyItem selectedItem = findSelectedItem(fieldLanguageId, languageItemArray);
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		addItems(languageItemArray, selectedItem);
	    }
	});
    }

    private ArbilVocabularyItem findSelectedItem(final String fieldLanguageId, final List<ArbilVocabularyItem> languageItemArray) {
	ArbilVocabularyItem selectedItem = null;
	if (fieldLanguageId != null) {
	    for (final ArbilVocabularyItem currentItem : languageItemArray) {
		// the code and description values have become unreliable due to changes to the controlled vocabularies see https://trac.mpi.nl/ticket/563#
		if (fieldLanguageId.equals(currentItem.itemCode) || fieldLanguageId.equals(currentItem.descriptionString)) {
		    selectedItem = currentItem;
		    break;
		}
	    }
	}
	return selectedItem;
    }

    private void addItems(final List<ArbilVocabularyItem> languageItemArray, final ArbilVocabularyItem selectedItem) {
	removeItem(initializingDropDownValue);
	for (final ArbilVocabularyItem currentItem : languageItemArray) {
	    addItem(currentItem);
	}
	addItem(defaultLanguageDropDownValue);
	if (selectedItem != null) {
	    setSelectedItem(selectedItem);
	} else {
	    setSelectedItem(defaultLanguageDropDownValue);
	}
    }

    private void setActionListener() {
	addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    if (LanguageIdBox.this.getSelectedItem() instanceof ArbilVocabularyItem) {
			final ArbilVocabularyItem selectedLanguage = (ArbilVocabularyItem) LanguageIdBox.this.getSelectedItem();
			String languageCode = selectedLanguage.itemCode;
			if (languageCode == null) {
			    // the code and description values have become unreliable due to changes to the controlled vocabularies see https://trac.mpi.nl/ticket/563#
			    languageCode = selectedLanguage.descriptionString;
			}
			cellField.setLanguageId(languageCode, true, false);
		    } else {
			if (defaultLanguageDropDownValue.equals(LanguageIdBox.this.getSelectedItem())) {
			    cellField.setLanguageId(null, true, false);
			}
		    }
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
    }
}
