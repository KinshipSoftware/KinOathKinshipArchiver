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
package nl.mpi.arbil;

import java.awt.datatransfer.ClipboardOwner;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.clarin.profiles.CmdiTemplate;
import nl.mpi.arbil.clarin.profiles.ProfilePreview;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilDataNodeArrayTableCell;
import nl.mpi.arbil.data.ArbilDataNodeTableCell;
import nl.mpi.arbil.data.ArbilEntityResolver;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.FieldChangeTriggers;
import nl.mpi.arbil.data.IMDIVocabularies;
import nl.mpi.arbil.data.MetadataBuilder;
import nl.mpi.arbil.data.importexport.ArbilCsvImporter;
import nl.mpi.arbil.data.importexport.ArbilToHtmlConverter;
import nl.mpi.arbil.data.importexport.ShibbolethNegotiator;
import nl.mpi.arbil.data.metadatafile.ImdiUtils;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.favourites.ArbilFavourites;
import nl.mpi.arbil.search.ArbilSearch;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.ui.ArbilFieldPlaceHolder;
import nl.mpi.arbil.ui.ArbilHyperlinkListener;
import nl.mpi.arbil.ui.ArbilTableModel;
import nl.mpi.arbil.ui.ImportExportDialog;
//import nl.mpi.arbil.ui.TableController;
//import nl.mpi.arbil.ui.menu.ArbilMenuBar;
//import nl.mpi.arbil.ui.wizard.setup.RemoteLocationsContent;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;
import nl.mpi.arbil.util.XsdChecker;
import nl.mpi.arbilcommons.journal.ArbilJournal;

/**
 * Takes care of injecting certain class instances into objects or classes.
 * This provides us with a sort of dependency injection, which enables loosening
 * the coupling between for example data classes and UI classes.
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ArbilInjector {

    protected synchronized void injectHandlers(
	    MessageDialogHandler messageDialogHandler,
	    WindowManager windowManager,
	    SessionStorage sessionStorage,
	    TreeHelper treeHelper,
	    DataNodeLoader dataNodeLoader,
	    ApplicationVersionManager versionManager,
	    ClipboardOwner clipboardOwner) {
	injectVersionManager(versionManager);
	injectDataNodeLoader(dataNodeLoader);
	injectSessionStorage(sessionStorage);
	injectWindowManager(windowManager);
	injectDialogHandler(messageDialogHandler);
	injectTreeHelper(treeHelper);
    }

    public void injectVersionManager(ApplicationVersionManager versionManager) {
	ArbilIcons.setVersionManager(versionManager);
	ArbilTableModel.setVersionManager(versionManager);
	MetadataReader.setVersionManager(versionManager);
    }

    public void injectDataNodeLoader(DataNodeLoader dataNodeLoader) {
	ArbilCsvImporter.setDataNodeLoader(dataNodeLoader);
	ArbilDataNodeTableCell.setDataNodeLoader(dataNodeLoader);
	ArbilDataNodeArrayTableCell.setDataNodeLoader(dataNodeLoader);
	ArbilField.setDataNodeLoader(dataNodeLoader);
	ArbilFieldPlaceHolder.setDataNodeLoader(dataNodeLoader);
	ArbilSearch.setDataNodeLoader(dataNodeLoader);
	ImportExportDialog.setDataNodeLoader(dataNodeLoader);
	MetadataBuilder.setDataNodeLoader(dataNodeLoader);
	MetadataReader.setDataNodeLoader(dataNodeLoader);
	ProfilePreview.setDataNodeLoader(dataNodeLoader);
    }

    public void injectTreeHelper(TreeHelper treeHelper) {
	//Inject tree helper
	ArbilFavourites.setTreeHelper(treeHelper);
	ImportExportDialog.setTreeHelper(treeHelper);
	MetadataBuilder.setTreeHelper(treeHelper);
//	RemoteLocationsContent.setTreeHelper(treeHelper);
	ArbilHyperlinkListener.setTreeHelper(treeHelper);
    }

    public void injectDialogHandler(MessageDialogHandler messageDialogHandler) {
	// Inject message dialog handler
	ArbilComponentBuilder.setMessageDialogHandler(messageDialogHandler);
	ArbilCsvImporter.setMessageDialogHandler(messageDialogHandler);
	ArbilFavourites.setMessageDialogHandler(messageDialogHandler);
	ArbilJournal.setMessageDialogHandler(messageDialogHandler);
	ArbilTableModel.setMessageDialogHandler(messageDialogHandler);
	ArbilTemplate.setMessageDialogHandler(messageDialogHandler);
	ArbilToHtmlConverter.setMessageDialogHandler(messageDialogHandler);
	ApplicationVersionManager.setMessageDialogHandler(messageDialogHandler);
	IMDIVocabularies.setMessageDialogHandler(messageDialogHandler);
	CmdiTemplate.setMessageDialogHandler(messageDialogHandler);
	FieldChangeTriggers.setMessageDialogHandler(messageDialogHandler);
	ImdiUtils.setMessageDialogHandler(messageDialogHandler);
	MetadataBuilder.setMessageDialogHandler(messageDialogHandler);
	MetadataReader.setMessageDialogHandler(messageDialogHandler);
	ShibbolethNegotiator.setMessageDialogHandler(messageDialogHandler);
    }

    public void injectWindowManager(WindowManager windowManager) {
	IMDIVocabularies.setWindowManager(windowManager);
	MetadataBuilder.setWindowManager(windowManager);
    }

    public void injectSessionStorage(SessionStorage sessionStorage) {
	ArbilComponentBuilder.setSessionStorage(sessionStorage);
	ArbilEntityResolver.setSessionStorage(sessionStorage);
	ArbilField.setSessionStorage(sessionStorage);
	ArbilJournal.setSessionStorage(sessionStorage);
	IMDIVocabularies.setSessionStorage(sessionStorage);
	MetadataBuilder.setSessionStorage(sessionStorage);
	CmdiProfileReader.setSessionStorage(sessionStorage);
	ProfilePreview.setSessionStorage(sessionStorage);
	ArbilFavourites.setSessionStorage(sessionStorage);
	XsdChecker.setSessionStorage(sessionStorage);
	ApplicationVersionManager.setSessionStorage(sessionStorage);
	ArbilTemplateManager.setSessionStorage(sessionStorage);
    }
}
