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
package nl.mpi.arbil.userstorage;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import nl.mpi.arbil.data.importexport.ShibbolethNegotiator;
import nl.mpi.arbil.util.DownloadAbortFlag;
import nl.mpi.arbil.util.ProgressListener;
import nl.mpi.flap.plugin.PluginSessionStorage;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface SessionStorage extends PluginSessionStorage {

    public static final String PARAM_LAST_FILE_FILTER = "metadataFileFilter";
    public static final String PARAM_WIZARD_RUN = "wizardHasRun";

    void changeCacheDirectory(File preferedCacheDirectory, boolean moveFiles);

    /**
     * Removes the cache path component from a path string and appends it to the
     * destination directory. Then tests for and creates the directory structure
     * in the destination directory if required.
     *
     * @param pathString Path of a file within the cache.
     * @param destinationDirectory Path of the destination directory.
     * @return The path of the file in the destination directory.
     */
    File getExportPath(String pathString, String destinationDirectory);

    /**
     * Checks for the existance of the favourites directory exists and creates
     * it if it does not.
     *
     * @return File pointing to the favourites directory
     */
    File getFavouritesDir();

    URI getNewArbilFileName(File parentDirectory, String nodeType);

    URI getOriginatingUri(URI locationInCacheURI);

    /**
     * Converts a String path from the remote location to the respective
     * location in the cache. Then tests for and creates the directory structure
     * in the cache if requred.
     *
     * @param pathString Path of the remote file.
     * @return The path in the cache for the file.
     */
    File getSaveLocation(String pathString);

    boolean loadBoolean(String filename, boolean defaultValue);

    /**
     * Deserialises the file from the linorg storage directory into an object.
     * Use to recreate program state from last save.
     *
     * @param filename The name of the file containing the serialised object
     * @return The deserialised object
     * @throws java.lang.Exception
     */
    Object loadObject(String filename) throws Exception;

    String loadString(String filename);

    String[] loadStringArray(String filename) throws IOException;

    /**
     * Tests if the a string points to a file that is in the favourites
     * directory.
     *
     * @return Boolean
     */
    boolean pathIsInFavourites(File fullTestFile);

    /**
     * Tests if the a string points to a flie that is in the cache directory.
     *
     * @return Boolean
     */
    boolean pathIsInsideCache(File fullTestFile);

    boolean replaceCacheCopy(String pathString);

    void saveBoolean(String filename, boolean storableValue);

    /**
     * Serialises the passed object to a file in the linorg storage directory so
     * that it can be retrieved on application restart.
     *
     * @param object The object to be serialised
     * @param filename The name of the file the object is to be serialised into
     * @throws java.io.IOException
     */
    void saveObject(Serializable object, String filename) throws IOException;

    /**
     * Copies a remote file over http and saves it into the cache.
     *
     * @param targetUrlString The URL of the remote file as a string
     * @param destinationPath The local path where the file should be saved
     * @return boolean true only if the file was downloaded, this will be false
     * if the file exists but was not re-downloaded or if the download failed
     */
    boolean saveRemoteResource(URL targetUrl, File destinationFile, ShibbolethNegotiator shibbolethNegotiator, boolean expireCacheCopy, boolean followRedirect, DownloadAbortFlag abortFlag, ProgressListener progressLabel);

    void saveString(String filename, String storableValue);

    void saveStringArray(String filename, String[] storableValue) throws IOException;

    File getFromCache(String pathString, boolean followRedirect);

    /**
     * Fetch the file from the remote URL and save into the cache. Currently
     * this does not expire the objects in the cache, however that will be
     * required in the future.
     *
     * @param pathString Path of the remote file.
     * @param expireCacheDays Number of days old that a file can be before it is
     * replaced.
     * @return The path of the file in the cache.
     */
    File updateCache(String pathString, int expireCacheDays, boolean followRedirect);

    /**
     * Fetch the file from the remote URL and save into the cache. Currently
     * this does not expire the objects in the cache, however that will be
     * required in the future.
     *
     * @param pathString Path of the remote file.
     * @return The path of the file in the cache.
     */
    File updateCache(String pathString, ShibbolethNegotiator shibbolethNegotiator, boolean expireCacheCopy, boolean followRedirect, DownloadAbortFlag abortFlag, ProgressListener progressLabel);


    /**
     * @return Custom TypeChecker config file, or null if no such file exists
     */
    public File getTypeCheckerConfig();
}
