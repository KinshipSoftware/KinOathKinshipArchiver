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
package nl.mpi.arbil.util;

import java.io.File;
import nl.mpi.flap.plugin.PluginDialogHandler;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface MessageDialogHandler extends PluginDialogHandler {

    File[] showMetadataFileSelectBox(String titleText, boolean multipleSelect);

    File[] showDirectorySelectBox(String titleText, boolean multipleSelect);

    File showEmptyExportDirectoryDialogue(String titleText);

    /**
     * Shows a dialog that allows a user to confirm save pending changes
     *
     * @throws Exception When user cancels save action
     */
    void offerUserToSaveChanges() throws Exception; // Maybe should be put in a separate interface...

    /**
     * Displays a dialog asking the user whether to go ahead and save changes.
     * Intended for use in actions that require the pending changes on an entity
     * to be saved (mainly mutations on a metadata file)
     *
     * @param Name of the entity that needs saving in order to proceed
     * @return Whether user agrees on saving the changes
     */
    boolean askUserToSaveChanges(String entityName);

    DialogBoxResult showDialogBoxRememberChoice(String message, String title, int optionType, int messageType);

    public static class DialogBoxResult {

	private final int result;
	private final boolean rememberChoice;

	public DialogBoxResult(int result, boolean rememberChoice) {
	    this.result = result;
	    this.rememberChoice = rememberChoice;
	}

	public int getResult() {
	    return result;
	}

	public boolean isRememberChoice() {
	    return rememberChoice;
	}
    };
}
