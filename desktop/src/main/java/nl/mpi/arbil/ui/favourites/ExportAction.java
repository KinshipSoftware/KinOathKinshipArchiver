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
package nl.mpi.arbil.ui.favourites;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.favourites.FavouritesExporter;
import nl.mpi.arbil.favourites.FavouritesImportExportException;
import nl.mpi.flap.plugin.PluginDialogHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for handling export requests from the UI
 *
 * @see ExportUI
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ExportAction extends AbstractAction {
    
    private final static Logger logger = LoggerFactory.getLogger(ExportAction.class);
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");
    private final PluginDialogHandler dialogHandler;
    private final FavouritesExporter exporter;
    
    public ExportAction(PluginDialogHandler dialogHandler, FavouritesExporter exporter) {
	super("export");
	this.dialogHandler = dialogHandler;
	this.exporter = exporter;
    }

    /**
     *
     * @param e action event, the {@link ActionEvent#getSource() source} of which should implement {@link ExportUI}
     */
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() instanceof ExportUI) {
	    final ExportUI source = (ExportUI) e.getSource();
	    exportFavourites(source.getSelectedFavourites());
	} else {
	    throw new RuntimeException("Cannot retrieve favourites selection from UI, action source does not implement ExportUI");
	}
    }
    
    private void exportFavourites(List<ArbilDataNode> nodesToExport) {
	if (nodesToExport.size() > 0) {
	    try {
		File[] exportLocation = dialogHandler.showFileSelectBox(widgets.getString("SELECT EXPORT DESTINATION"), true, false, null, PluginDialogHandler.DialogueType.save, null);
		if (exportLocation != null && exportLocation.length > 0 && exportLocation[0] != null) {
		    // Carry out export
		    exporter.exportFavourites(exportLocation[0], nodesToExport.toArray(new ArbilDataNode[]{}));
		    // Show result in file browser
		    openDirectory(exportLocation[0]);
		    // Applause!
		    dialogHandler.addMessageDialogToQueue(widgets.getString("FAVOURITES HAVE BEEN EXPORTED"), widgets.getString("EXPORT COMPLETE"));
		}
	    } catch (FavouritesImportExportException ex) {
		logger.error("An error occurred while exporting favourites", ex);
		dialogHandler.addMessageDialogToQueue(
			MessageFormat.format(widgets.getString("AN ERROR OCCURRED WHILE EXPORTING FAVOURITES"),
			ex.getMessage()), "Error");
	    }
	} else {
	    dialogHandler.addMessageDialogToQueue(widgets.getString("NO NODES ARE SELECTED. SELECT AT LEAST ONE NODE TO EXPORT."), widgets.getString("SELECT NODES TO EXPORT"));
	}
    }

    /**
     * Opens the export result location in the system's file browser
     *
     * @param exportLocation location to open
     */
    private void openDirectory(File exportLocation) {
	try {
	    if (!GraphicsEnvironment.isHeadless()) {
		logger.info("Headless mode, not opening export target directory {}", exportLocation);
		Desktop.getDesktop().open(exportLocation);
	    }
	} catch (IOException ex) {
	    // No associated application, or the associated application fails to be launched. Fail silently.
	    logger.warn("Could not open target location {}", exportLocation, ex);
	} catch (RuntimeException ex) {
	    // Not supported, security issue, file does not exist... No reason to crash, fail silently.
	    logger.warn("Could not open target location {}", exportLocation, ex);
	}
    }
}
