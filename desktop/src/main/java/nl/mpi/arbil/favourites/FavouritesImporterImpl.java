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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import nl.mpi.arbil.ArbilMetadataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class FavouritesImporterImpl implements FavouritesImporter {
    
    private final static Logger logger = LoggerFactory.getLogger(FavouritesImporterImpl.class);
    private final FavouritesService favouritesService;
    
    public FavouritesImporterImpl(FavouritesService favouritesService) {
	this.favouritesService = favouritesService;
    }

    /**
     *
     * @param importDirectory URI providing the location of the directory to import from
     * @throws FavouritesImportExportException if the import location is not accessible or its contents cannot be read
     */
    public void importFavourites(File importDirectory) throws FavouritesImportExportException {
	final URI importFile = new File(importDirectory, FavouritesExporter.FAVOURITES_LIST_FILE).toURI();
	final List<URI> locations = readFavouritesLocations(importFile);
	
	try {
	    // Add locations as favourites
	    for (URI favouriteURI : locations) {
		// Resolve against location of import file
		favouritesService.addAsFavourite(importFile.resolve(favouriteURI));
	    }
	} catch (ArbilMetadataException ex) {
	    throw new FavouritesImportExportException(ex);
	}
    }
    
    private List<URI> readFavouritesLocations(URI location) throws FavouritesImportExportException {
	try {
	    final InputStream openStream = location.toURL().openStream();
	    final BufferedReader reader = new BufferedReader(new InputStreamReader(openStream));
	    try {
		final List<URI> favouriteLocations = new ArrayList<URI>();
		String favouriteLocation;
		while ((favouriteLocation = reader.readLine()) != null) {
		    if (!lineIsComment(favouriteLocation)) { // Skip comment lines
			try {
			    final URI favouriteURI = new URI(favouriteLocation);
			    favouriteLocations.add(favouriteURI);
			} catch (URISyntaxException ex) {
			    // Skip broken URIs in favourites list
			    logger.warn("Skipping broken favourite URI {} while importing. Reason: ", favouriteLocation, ex);
			}
		    }
		}
		return favouriteLocations;
	    } finally {
		reader.close();
	    }
	} catch (MalformedURLException ex) {
	    throw new FavouritesImportExportException("Invalid import location: " + location, ex);
	} catch (IOException ex) {
	    throw new FavouritesImportExportException("Error while trying to read favourites list", ex);
	}
    }
    
    private boolean lineIsComment(String favouritesLine) {
	return favouritesLine.startsWith("#");
    }
}
