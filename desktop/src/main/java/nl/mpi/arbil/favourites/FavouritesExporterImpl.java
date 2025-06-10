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
package nl.mpi.arbil.favourites;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.metadatafile.MetadataUtils;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersion;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class FavouritesExporterImpl implements FavouritesExporter {

    private final SessionStorage sessionStorage;
    private final ApplicationVersion applicationVersion;

    public FavouritesExporterImpl(SessionStorage sessionStorage, ApplicationVersion applicationVersion) {
	this.sessionStorage = sessionStorage;
	this.applicationVersion = applicationVersion;
    }

    public void exportFavourites(final File exportLocation, ArbilDataNode[] favouriteNodes) throws FavouritesImportExportException {
	final List<URI> favouriteNodeUris = copyNodes(exportLocation, favouriteNodes);
	try {
	    writeFavouritesList(favouriteNodeUris, new File(exportLocation, FavouritesExporter.FAVOURITES_LIST_FILE));
	} catch (IOException ioEx) {
	    throw new FavouritesImportExportException("An I/O exception occurred while exporting favourites", ioEx);
	}
    }

    /**
     * Copies the favourites files from their original location to the export location
     *
     * @param exportLocation location to export favourites to
     * @return List of relative (to the export location) URIs of nodes that were exported
     * @throws FavouritesImportExportException in case of failure during export
     */
    private List<URI> copyNodes(final File exportLocation, ArbilDataNode[] favouriteNodes) throws FavouritesImportExportException {
	final URI favouritesBaseUri = sessionStorage.getFavouritesDir().toURI();
	final URI exportUri = exportLocation.toURI();
	final List<URI> favouriteNodeUris = new ArrayList<URI>(favouriteNodes.length);
	for (ArbilDataNode favouriteNode : favouriteNodes) {
	    final URI nodeUri = favouriteNode.getURI();
	    final URI relativeNodeUri = favouritesBaseUri.relativize(nodeUri);
	    favouriteNodeUris.add(relativeNodeUri);

	    final URI targetUri = exportUri.resolve(relativeNodeUri);
	    copyNode(nodeUri, targetUri);
	}
	return favouriteNodeUris;
    }

    private void copyNode(final URI sourceUri, final URI targetUri) throws FavouritesImportExportException {
	try {
	    // URI of source file with fragment stripped off
	    final URI sourceFileUri = new URI(sourceUri.getScheme(), sourceUri.getSchemeSpecificPart(), null);
	    // URI of target file with fragment stripped off
	    final URI targetFileUri = new URI(targetUri.getScheme(), targetUri.getSchemeSpecificPart(), null);
	    // Get format-specific metadata utils 
	    final MetadataUtils mdUtils = ArbilDataNode.getMetadataUtils(targetFileUri.toString());
	    // Copy file using metadata utils
	    mdUtils.copyMetadataFile(sourceFileUri, new File(targetFileUri), null, false);
	} catch (IOException ex) {
	    throw new FavouritesImportExportException("Could not copy metadata file", ex);
	} catch (ArbilMetadataException ex) {
	    throw new FavouritesImportExportException("Could not process metadata file", ex);
	} catch (URISyntaxException ex) {
	    throw new FavouritesImportExportException("Invalid target URI", ex);
	}
    }

    private void writeFavouritesList(List<URI> favouriteNodeUris, File listFile) throws FavouritesImportExportException, IOException {
	if (listFile.exists()) {
	    throw new FavouritesImportExportException("Target list file already exists");
	} else {
	    final FileOutputStream os = new FileOutputStream(listFile);
	    final Writer osWriter = new OutputStreamWriter(os, "UTF-8");
	    final BufferedWriter writer = new BufferedWriter(osWriter);

	    // Write some lines of explanatory comments
	    writer.write(getPreamble());
	    // Write favourite file URLs
	    try {
		for (URI favouriteNodeUri : favouriteNodeUris) {
		    writer.write(favouriteNodeUri.toString());
		    writer.write("\n");
		}
	    } finally {
		writer.close();
	    }
	}
    }

    private String getPreamble() {
	return String.format(
		"# The files in this directory are the result of an export that was created by Arbil.\n"
		+ "# You can re-import them into Arbil on another computer. \n"
		+ "# \n"
		+ "# Arbil can be downloaded for free at http://tla.mpi.nl/tools/arbil\n"
		+ "# \n"
		+ "# Arbil version: %s version %s.%sr%s\n"
		+ "# Export date: %s\n"
		+ "# \n",
		applicationVersion.applicationTitle,
		applicationVersion.currentMajor,
		applicationVersion.currentMinor,
		applicationVersion.currentRevision,
		DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
    }
}
