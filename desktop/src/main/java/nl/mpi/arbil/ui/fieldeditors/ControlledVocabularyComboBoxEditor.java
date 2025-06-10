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
package nl.mpi.arbil.ui.fieldeditors;

import javax.swing.JComboBox;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilVocabulary;
import nl.mpi.arbil.data.ArbilVocabularyItem;
import nl.mpi.arbil.ui.TypeAheadComboBoxEditor;

/**
 * Text editor intended for use with ControlledVocabularyComboBox
 * It has typeahead and can deal with open and closed vocabularies, and both
 * single valued vocabularies and lists.
 *
 * @see ControlledVocabularyComboBox
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ControlledVocabularyComboBoxEditor extends TypeAheadComboBoxEditor {

    public ControlledVocabularyComboBoxEditor(String initialValue, String originalValue, ArbilField arbilField, JComboBox comboBox) {
	super(new ArbilFieldEditor(initialValue), initialValue, originalValue, comboBox);

	this.targetField = arbilField;
	this.vocabulary = arbilField.getVocabulary();

	init();
    }

    /**
     * Local convenience method. Gets item from vocabulary
     *
     * @param index
     * @return
     */
    protected ArbilVocabularyItem getItemAt(int index) {
	return vocabulary.getVocabularyItems().get(index);
    }

    protected int getItemsCount() {
	return vocabulary.getVocabularyItems().size();
    }

    protected boolean isList() {
	return targetField.isVocabularyList();
    }

    protected boolean isOpen() {
	return targetField.isVocabularyOpen();
    }
    private ArbilVocabulary vocabulary;
    private ArbilField targetField;
}
