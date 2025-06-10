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

import javax.swing.JComboBox;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilVocabulary;
import nl.mpi.arbil.data.ArbilVocabularyItem;

/**
 * Editable combo box that has the items of a controlled vocabulary in it.
 * Use with {@link ControlledVocabularyComboBoxEditor}.
 * Rendering of list items is done by a {@link ControlledVocabularyComboBoxRenderer}.
 *
 * Document : ControlledVocabularyComboBox
 * Created on : Wed Oct 07 11:07:30 CET 2009
 *
 * @author Peter.Withers@mpi.nl
 * @author Twan.Goosen@mpi.nl
 * @see ControlledVocabularyComboBoxEditor
 * @see ControlledVocabularyComboBoxRenderer
 */
public class ControlledVocabularyComboBox extends JComboBox {

    public ControlledVocabularyComboBox(ArbilField targetField) {
	ArbilVocabulary fieldsVocabulary = targetField.getVocabulary();
	if (null == fieldsVocabulary || null == fieldsVocabulary.findVocabularyItem(targetField.getFieldValue())) {
	    this.addItem(targetField.getFieldValue());
	}
	if (null != fieldsVocabulary) {
	    for (ArbilVocabularyItem vocabularyListItem : fieldsVocabulary.getVocabularyItems()) {
		this.addItem(vocabularyListItem);
	    }
	}

	this.setUI(new javax.swing.plaf.basic.BasicComboBoxUI());
	this.setRenderer(new ControlledVocabularyComboBoxRenderer());
    }

    public String getCurrentValue() {
	if (getEditor() != null) {
	    if (getEditor() instanceof ControlledVocabularyComboBoxEditor) {
		return ((ControlledVocabularyComboBoxEditor) getEditor()).getCurrentValue();
	    } else {
		return getEditor().getItem().toString();
	    }
	} else {
	    return getSelectedItem().toString();
	}
    }
}
