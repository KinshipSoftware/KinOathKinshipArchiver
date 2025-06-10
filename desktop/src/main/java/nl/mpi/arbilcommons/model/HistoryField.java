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
package nl.mpi.arbilcommons.model;

import nl.mpi.flap.model.DataField;

/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
/**
 * Created on : Feb 15, 2013, 3:57:36 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public abstract class HistoryField extends DataField {

    /*
     * This method will set the field value but will also always update the UI and add an entry to the undo history. 
     * 
     * @param fieldValueToBe String the value that the field will be set to
     */
    @Override
    public void setFieldValue(String fieldValueToBe) {
        setFieldValue(fieldValueToBe, true, false);
    }

    /*
     * This method will set the field value but will optionally update the UI and optionally add an entry to the undo history. 
     * 
     * @param fieldValueToBe String the value that the field will be set to
     * @param updateUI, if true then the UI will be triggered to update 
     * @param excludeFromUndoHistory, if true then the change will be excluded from the undo history
     */
    public abstract void setFieldValue(String fieldValueToBe, boolean updateUI, boolean excludeFromUndoHistory);

    /*
     * This method will set the language ID but will also always update the UI and add an entry to the undo history. 
     * 
     * @param languageIdToBe String the value that the language ID will be set to
     */
    @Override
    public void setLanguageId(String languageIdToBe) {
        setLanguageId(languageIdToBe, true, false);
    }

    /*
     * This method will set the language ID but will optionally update the UI and optionally add an entry to the undo history. 
     * 
     * @param languageIdToBe String the value that the language ID will be set to
     * @param updateUI, if true then the UI will be triggered to update 
     * @param excludeFromUndoHistory, if true then the change will be excluded from the undo history
     */
    public abstract void setLanguageId(String languageIdToBe, boolean updateUI, boolean excludeFromUndoHistory);

    /*
     * This method will set the key name but will also always update the UI and add an entry to the undo history. 
     * 
     * @param keyNameToBe String the value that the key name will be set to
     */
    @Override
    public void setKeyName(String keyNameToBe) {
        setKeyName(keyNameToBe, true, false);
    }

    /*
     * This method will set the key name but will optionally update the UI and optionally add an entry to the undo history. 
     * 
     * @param keyNameToBe String the value that the key name will be set to
     * @param updateUI, if true then the UI will be triggered to update 
     * @param excludeFromUndoHistory, if true then the change will be excluded from the undo history
     * @return Key name has actually been changed
     */
    public abstract boolean setKeyName(String keyNameToBe, boolean updateUI, boolean excludeFromUndoHistory);
}
