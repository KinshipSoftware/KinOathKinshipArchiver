/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for
 * Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.userstorage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JOptionPane;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.importexport.ShibbolethNegotiator;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.DownloadAbortFlag;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.ProgressListener;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document : ArbilSessionStorage use to save and load objects from disk and to
 * manage items in the local cache Created on :
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilSessionStorage extends CommonsSessionStorage implements SessionStorage {

    private final static Logger logger = LoggerFactory.getLogger(ArbilSessionStorage.class);
    private final static String TYPECHECKER_CONFIG_FILENAME = "filetypes.txt";
    public static final String UTF8_ENCODING = "UTF8";
    public static final String CONFIG_FILE = "arbil.config";
    private final ResourceBundle services = java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Services");

    public void setMessageDialogHandler(MessageDialogHandler handler) {
        messageDialogHandler = handler;
    }
    private WindowManager windowManager;

    public void setWindowManager(WindowManager windowManagerInstance) {
        windowManager = windowManagerInstance;
    }
    private TreeHelper treeHelper;

    public void setTreeHelper(TreeHelper treeHelperInstance) {
        treeHelper = treeHelperInstance;
    }

    protected void logError(Exception exception) {
        BugCatcherManager.getBugCatcher().logError(exception);
    }

    protected void logError(String message, Exception exception) {
        BugCatcherManager.getBugCatcher().logError(message, exception);
    }

    /**
     * These get used to construct absolute working directory path candidates
     *
     * @return Application directory alternatives (relative)
     */
    protected String[] getAppDirectoryAlternatives() {
        return new String[]{".arbil", ".linorg"};
    }

    public ArbilSessionStorage() {
        HttpURLConnection.setFollowRedirects(false); // TODO: replace with calls to setInstanceFollowRedirects in the right places
    }

    public void changeCacheDirectory(File preferedCacheDirectory, boolean moveFiles) {
        File fromDirectory = getProjectWorkingDirectory();
        if (!preferedCacheDirectory.getAbsolutePath().contains(getProjectDirectoryName()) && !preferedCacheDirectory.getAbsolutePath().contains(".arbil/imdicache") && !localCacheDirectory.getAbsolutePath().contains(".linorg/imdicache")) {
            preferedCacheDirectory = new File(preferedCacheDirectory, getProjectDirectoryName());
        }
        // this moving of files is not relevant for projects and it does not work across devices so it has been removed, it should be replaced by the projects functionality
        if (!moveFiles || JOptionPane.YES_OPTION == messageDialogHandler.showDialogBox(
                java.text.MessageFormat.format(services.getString("CHANGING YOUR CURRENT PROJECT FROM {0} TO {1}"), fromDirectory.getParent(), preferedCacheDirectory.getParent())
                + "\n"
                + services.getString("ARBIL WILL NEED TO CLOSE ALL TABLES ONCE THE FILES ARE MOVED"), "Arbil", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
            if (moveFiles) {
                // move the files
                changeStorageDirectory(fromDirectory, preferedCacheDirectory);
            } else {
                saveString("cacheDirectory", preferedCacheDirectory.getAbsolutePath());
            }
        }
    }

//    public void changeStorageDirectory(String preferedDirectory) {
////        TODO: this caused isses on windows 20100416 test and confirm if this is an issue
//        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "Arbil will need to close in order to move the storage directory.\nDo you wish to continue?", "Arbil", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
//            File fromDirectory = storageDirectory;
//            File toDirectory = new File(preferedDirectory);
//            storageDirectory = new File(preferedDirectory);
//            changeStorageDirectory(fromDirectory, toDirectory);
//        }
//    }
    // Move the storage directory and change the local corpus tree links to the new directory.
    // After completion the application will be closed!
    private void changeStorageDirectory(File fromDirectory, File toDirectory) {
        String toDirectoryUriString = toDirectory.toURI().toString().replaceAll("/$", "");
        String fromDirectoryUriString = fromDirectory.toURI().toString().replaceAll("/$", "");
        logger.debug("toDirectoryUriString: {}", toDirectoryUriString);
        logger.debug("fromDirectoryUriString: {}", fromDirectoryUriString);
        try {
            toDirectoryUriString = URLDecoder.decode(toDirectoryUriString, "UTF-8");
            fromDirectoryUriString = URLDecoder.decode(fromDirectoryUriString, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            // UTF-8 not supported, should not occur
            throw new RuntimeException(uee);
        }
        boolean success = fromDirectory.renameTo(toDirectory);
        // This sometimes fails on Windows 7, JRE 6 without any clear reason. See https://trac.mpi.nl/ticket/1553
        if (!success) {
            if (JOptionPane.YES_OPTION == messageDialogHandler.showDialogBox(
                    services.getString("THE FILES IN YOUR 'LOCAL CORPUS' COULD NOT BE MOVED TO THE REQUESTED LOCATION"),
                    "Arbil", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                saveString("cacheDirectory", toDirectory.getAbsolutePath());
                localCacheDirectory = null;
                getProjectWorkingDirectory();
                treeHelper.loadLocationsList();
                treeHelper.applyRootLocations();
            }
        } else {
            try {
                Vector<String> locationsList = new Vector<String>();
                for (ArbilDataNode[] currentTreeArray : new ArbilDataNode[][]{
                    treeHelper.getRemoteCorpusNodes(),
                    treeHelper.getLocalCorpusNodes(),
                    treeHelper.getLocalFileNodes(),
                    treeHelper.getFavouriteNodes()}) {
                    for (ArbilDataNode currentLocation : currentTreeArray) {
                        String currentLocationString = URLDecoder.decode(currentLocation.getUrlString(), "UTF-8");
                        logger.debug("currentLocationString: {}", currentLocationString);
                        logger.debug("prefferedDirectoryUriString: {}", toDirectoryUriString);
                        logger.debug("storageDirectoryUriString: {}", fromDirectoryUriString);
                        locationsList.add(currentLocationString.replace(fromDirectoryUriString, toDirectoryUriString));
                    }
                }
                //LinorgSessionStorage.getSingleInstance().saveObject(locationsList, "locationsList");
                saveStringArray("locationsList", locationsList.toArray(new String[]{}));
                saveString("cacheDirectory", toDirectory.getAbsolutePath());
                localCacheDirectory = null;
                getProjectWorkingDirectory();
                treeHelper.loadLocationsList();
                treeHelper.applyRootLocations();
                windowManager.closeAllWindows();
            } catch (UnsupportedEncodingException ex) {
                // UTF-8 unsupported, should not occur
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                logger.error("Could not save locations list", ex);
            } catch (Exception ex) {
                logError(ex);
//            logger.debug("save locationsList exception: " + ex.getMessage());
            }
//            treeHelper.loadLocationsList();
//            JOptionPane.showOptionDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "The working files have been moved.", "Arbil", JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"Exit"}, "Exit");
//            System.exit(0); // TODO: this exit might be unrequired
        }
    }

//    public void showDirectorySelectionDialogue() {
//        settingsjDialog = new JDialog(JOptionPane.getFrameForComponent(LinorgWindowManager.getSingleInstance().linorgFrame));
//        settingsjDialog.setLocationRelativeTo(LinorgWindowManager.getSingleInstance().linorgFrame);
//        JPanel optionsPanel = new JPanel();
//        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));
//        ButtonGroup group = new ButtonGroup();
//        for (String currentLocation : getLocationOptions()) {
//            if (!currentLocation.startsWith("null")) {
//                JRadioButton locationButton = new JRadioButton(currentLocation);
//                locationButton.setActionCommand(currentLocation);
//                locationButton.setSelected(storageDirectory.equals(currentLocation));
//                group.add(locationButton);
//                optionsPanel.add(locationButton);
////            birdButton.addActionListener(this);
//            }
//        }
//        JPanel buttonsPanel = new JPanel();
//        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 30));
//        JButton moveButton = new JButton("Move");
//        moveButton.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//
//                settingsjDialog.dispose();
//                settingsjDialog = null;
//            }
//        });
//        JButton cancelButton = new JButton("Cancel");
//        cancelButton.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                settingsjDialog.dispose();
//                settingsjDialog = null;
//            }
//        });
//        moveButton.setEnabled(false);
//        buttonsPanel.add(moveButton);
//        buttonsPanel.add(cancelButton);
//        optionsPanel.add(buttonsPanel);
//        settingsjDialog.add(optionsPanel);
////        optionsPanel.setBackground(Color.BLUE);
////        buttonsPanel.setBackground(Color.GREEN);
//        settingsjDialog.setTitle("Storage Directory Location");
////        settingsjDialog.setMinimumSize(new Dimension(400, 300));
//        settingsjDialog.pack();
//        settingsjDialog.setVisible(true);
//    }
    /**
     * Tests if the a string points to a file that is in the favourites
     * directory.
     *
     * @return Boolean
     */
    public boolean pathIsInFavourites(File fullTestFile) { //todo: test me
        String favouritesString = "favourites";
        int foundPos = fullTestFile.getPath().indexOf(favouritesString) + favouritesString.length();
        if (foundPos == -1) {
            return false;
        }
        if (foundPos > fullTestFile.getPath().length()) {
            return false;
        }
        File testFile = new File(fullTestFile.getPath().substring(0, foundPos));
        return testFile.equals(getFavouritesDir());
    }

    public URI getOriginatingUri(URI locationInCacheURI) {
        URI returnUri = null;
        String uriPath = locationInCacheURI.getPath();
//        logger.debug("pathIsInsideCache" + storageDirectory + " : " + fullTestFile);
        logger.debug("uriPath: {}", uriPath);
        int foundPos = uriPath.indexOf("imdicache");
        if (foundPos == -1) {
            foundPos = uriPath.indexOf(getProjectDirectoryName());
            if (foundPos == -1) {
                return null;
            }
        }
        uriPath = uriPath.substring(foundPos);
        String[] uriParts = uriPath.split("/", 4);
        try {
            if (uriParts[1].toLowerCase().equals("http")) {
                returnUri = new URI(uriParts[1], uriParts[2], "/" + uriParts[3], null); // [0] will be "imdicache"
                logger.debug("returnUri: {}", returnUri);
            }
        } catch (URISyntaxException urise) {
            logger.error("Could not construct orginating URI from {}", uriPath, urise);
        }
        return returnUri;
    }

    /**
     * Tests if the a string points to a flie that is in the cache directory.
     *
     * @return Boolean
     */
    public boolean pathIsInsideCache(File fullTestFile) {
        File cacheDirectory = getProjectWorkingDirectory();
        File testFile = fullTestFile;
        while (testFile != null) {
            if (testFile.equals(cacheDirectory)) {
                return true;
            }
            testFile = testFile.getParentFile();
        }
        return false;
    }

    /**
     * Checks for the existance of the favourites directory exists and creates
     * it if it does not.
     *
     * @return File pointing to the favourites directory
     */
    public File getFavouritesDir() {
        File favDirectory = new File(getProjectDirectory(), "favourites"); // storageDirectory already has the file separator appended
        boolean favDirExists = favDirectory.exists();
        if (!favDirExists) {
            if (!favDirectory.mkdir()) {
                logError("Could not create favourites directory", null);
                return null;
            }
        }
        return favDirectory;
    }

    /**
     * @return the name of the project directory
     */
    protected String getProjectDirectoryName() {
        return "ArbilWorkingFiles";
    }

    /**
     * Serialises the passed object to a file in the linorg storage directory so
     * that it can be retrieved on application restart.
     *
     * @param object The object to be serialised
     * @param filename The name of the file the object is to be serialised into
     * @throws java.io.IOException
     */
    public void saveObject(Serializable object, String filename) throws IOException {
        logger.debug("saveObject: {}", filename);
        ObjectOutputStream objstream = new ObjectOutputStream(new FileOutputStream(new File(getApplicationSettingsDirectory(), filename)));
        objstream.writeObject(object);
        objstream.close();
    }

    /**
     * Deserialises the file from the linorg storage directory into an object.
     * Use to recreate program state from last save.
     *
     * @param filename The name of the file containing the serialised object
     * @return The deserialised object
     * @throws java.lang.Exception
     */
    public Object loadObject(String filename) throws Exception {
        logger.debug("loadObject: " + filename);
        // this must be allowed to throw so don't do checks here
        ObjectInputStream objstream = new ObjectInputStream(new FileInputStream(new File(getApplicationSettingsDirectory(), filename)));
        Object object = objstream.readObject();
        objstream.close();
        if (object == null) {
            throw (new Exception("Loaded object is null"));
        }
        return object;
    }

    public String[] loadStringArray(String filename) throws IOException {
        File currentConfigFile = null;
        if (filename.equals("locationsList")) {
	    // The tree locations config has been moved into the project directory,
            // for now we are setting this by the location name.
            // However this will be replaced by a project configuration class that will use JAXB to save to disk.
            currentConfigFile = new File(getProjectDirectory(), filename + ".config");
        }
        if (currentConfigFile == null || !currentConfigFile.exists()) {
            // if the project tree locations config does not exist then read the application version
            currentConfigFile = new File(getApplicationSettingsDirectory(), filename + ".config");
        }
        // read the location list from a text file that admin-users can read and hand edit if they really want to
        if (currentConfigFile.exists()) {
            ArrayList<String> stringArrayList = new ArrayList<String>();
            FileInputStream fstream = new FileInputStream(currentConfigFile);
            DataInputStream in = new DataInputStream(fstream);
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(in, UTF8_ENCODING));
                try {
                    String strLine;
                    while ((strLine = br.readLine()) != null) {
                        stringArrayList.add(strLine);
                    }
                } finally {
                    br.close();
                }
            } finally {
                in.close();
                fstream.close();
            }
            return stringArrayList.toArray(new String[]{});
        }
        return null;

//        String[] stringProperty = {};
//        Properties propertiesObject = new Properties();
//        try {
//            // load the file
//            FileInputStream propertiesInStream = new FileInputStream(new File(storageDirectory, filename + ".config"));
//            propertiesObject.load(propertiesInStream);
//            // load all the values into an array
//            stringProperty = propertiesObject.values().toArray(new String[]{});
//            // close the file
//            propertiesInStream.close();
//        } catch (IOException ioe) {
//            // file not found so create the file
//            saveStringArray(filename, stringProperty);
//        }
//        return stringProperty;
    }

    public void saveStringArray(String filename, String[] storableValue) throws IOException {
        File destinationDirectory;
        if (filename.equals("locationsList")) {
	    // The tree locations config has been moved into the project directory,
            // for now we are setting this by the location name.
            // However this will be replaced by a project configuration class that will use JAXB to save to disk.
            destinationDirectory = getProjectDirectory();
        } else {
            destinationDirectory = getApplicationSettingsDirectory();
        }
        // save the location list to a text file that admin-users can read and hand edit if they really want to
        File destinationConfigFile = new File(destinationDirectory, filename + ".config");
        File tempConfigFile = new File(destinationDirectory, filename + ".config.tmp");

        Writer out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempConfigFile), UTF8_ENCODING));
            for (String currentString : storableValue) {
                out.write(currentString + "\r\n");
            }
        } catch (IOException ex) {
            logError(ex);
            throw ex;
        } finally {
            if (out != null) {
                out.close();
            }
        }
        if (!destinationConfigFile.exists() || destinationConfigFile.delete()) {
            if (!tempConfigFile.renameTo(destinationConfigFile)) {
                messageDialogHandler.addMessageDialogToQueue(MessageFormat.format(services.getString("ERROR SAVING CONFIGURATION TO {0}"), filename), services.getString("ERROR SAVING CONFIGURATION"));
            }
        } else {
            messageDialogHandler.addMessageDialogToQueue(MessageFormat.format(services.getString("COULD NOT WRITE NEW CONFIGURATION TO {0}"), filename), services.getString("ERROR SAVING CONFIGURATION"));
        }
//        try {
//            Properties propertiesObject = new Properties();
//            FileOutputStream propertiesOutputStream = new FileOutputStream(new File(storageDirectory, filename + ".config"));
//            for (int valueCounter = 0; valueCounter < storableValue.length; valueCounter++) {
//                propertiesObject.setProperty("nl.mpi.arbil." + filename + "." + valueCounter, storableValue[valueCounter]);
//            }
//            propertiesObject.store(propertiesOutputStream, null);
//            propertiesOutputStream.close();
//        } catch (IOException ioe) {
//            bugCatcher.logError(ioe);
//        }
    }

    public String loadString(String filename) {
        Properties configObject = getConfig();
        String stringProperty = configObject.getProperty("nl.mpi.arbil." + filename);
        return stringProperty;
    }

    public void saveString(String filename, String storableValue) {
        Properties configObject = getConfig();
        configObject.setProperty("nl.mpi.arbil." + filename, storableValue);
        saveConfig(configObject);
    }

    public final boolean loadBoolean(String filename, boolean defaultValue) {
        Properties configObject = getConfig();
        String stringProperty = configObject.getProperty("nl.mpi.arbil." + filename);
        if (stringProperty == null) {
            stringProperty = Boolean.toString(defaultValue);
            saveBoolean(filename, defaultValue);
        }
        return Boolean.valueOf(stringProperty);
    }

    public void saveBoolean(String filename, boolean storableValue) {
        Properties configObject = getConfig();
        configObject.setProperty("nl.mpi.arbil." + filename, Boolean.toString(storableValue));
        saveConfig(configObject);
    }

    private Properties getConfig() {
        Properties propertiesObject = new Properties();
        FileInputStream propertiesInStream = null;
        try {
            propertiesInStream = new FileInputStream(new File(getApplicationSettingsDirectory(), CONFIG_FILE));
            if (canUsePropertiesReaderWriter()) {
                InputStreamReader reader = new InputStreamReader(propertiesInStream, UTF8_ENCODING);
                propertiesObject.load(reader);
            } else {
                propertiesObject.load(propertiesInStream);
            }
        } catch (IOException ioe) {
            // file not found so create the file
            saveConfig(propertiesObject);
        } finally {
            if (propertiesInStream != null) {
                try {
                    propertiesInStream.close();
                } catch (IOException ioe) {
                    logError(ioe);
                }
            }
        }
        return propertiesObject;
    }

    private void saveConfig(Properties configObject) {
        FileOutputStream propertiesOutputStream = null;
        try {
            //new OutputStreamWriter
            propertiesOutputStream = new FileOutputStream(new File(getApplicationSettingsDirectory(), CONFIG_FILE));
            if (canUsePropertiesReaderWriter()) {
                OutputStreamWriter propertiesOutputStreamWriter = new OutputStreamWriter(propertiesOutputStream, UTF8_ENCODING);
                configObject.store(propertiesOutputStreamWriter, null);
                propertiesOutputStreamWriter.close();
            } else {
                configObject.store(propertiesOutputStream, null);
            }
            propertiesOutputStream.close();
        } catch (IOException ioe) {
            logError(ioe);
        } finally {
            if (propertiesOutputStream != null) {
                try {
                    propertiesOutputStream.close();
                } catch (IOException ioe2) {
                    logError(ioe2);
                }
            }
        }
    }

    /**
     * Fetch the file from the remote URL and save into the cache. Does not
     * check whether copy may have been expired
     *
     * @param pathString Path of the remote file.
     * @param followRedirect Whether to follow redirects
     * @return The path of the file in the cache.
     */
    public File getFromCache(String pathString, boolean followRedirect) {
        return updateCache(pathString, null, false, followRedirect, new DownloadAbortFlag(), null);
    }

    /**
     * Fetch the file from the remote URL and save into the cache. Currently
     * this does not expire the objects in the cache, however that will be
     * required in the future.
     *
     * @param pathString Path of the remote file.
     * @param followRedirect Whether to follow redirects
     * @param expireCacheDays Number of days old that a file can be before it is
     * replaced.
     * @return The path of the file in the cache.
     */
    public File updateCache(String pathString, int expireCacheDays, boolean followRedirect) { // update if older than the date - x
        File targetFile = getSaveLocation(pathString);
        boolean fileNeedsUpdate = !targetFile.exists();
        if (!fileNeedsUpdate) {
            Date lastModified = new Date(targetFile.lastModified());
            Date expireDate = new Date(System.currentTimeMillis());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(expireDate);
            calendar.add(Calendar.DATE, -expireCacheDays);
            expireDate.setTime(calendar.getTime().getTime());

            fileNeedsUpdate = expireDate.after(lastModified);
            if (fileNeedsUpdate) {
                logger.info("Existing file {} in cache will be updated (at least {} days since {})", pathString, expireCacheDays, lastModified);
            }
        }
        return updateCache(pathString, targetFile, null, fileNeedsUpdate, followRedirect, new DownloadAbortFlag(), null);
//	}
    }

    public File updateCache(String pathString, ShibbolethNegotiator shibbolethNegotiator, boolean expireCacheCopy, boolean followRedirect, DownloadAbortFlag abortFlag, ProgressListener progressLabel) {
        return updateCache(pathString, getSaveLocation(pathString), shibbolethNegotiator, expireCacheCopy, followRedirect, abortFlag, progressLabel);
    }

    /**
     * Fetch the file from the remote URL and save into the cache. Currently
     * this does not expire the objects in the cache, however that will be
     * required in the future.
     *
     * @param pathString Path of the remote file.
     * @return The path of the file in the cache.
     */
    private File updateCache(String pathString, File cachePath, ShibbolethNegotiator shibbolethNegotiator, boolean expireCacheCopy, boolean followRedirect, DownloadAbortFlag abortFlag, ProgressListener progressLabel) {
        // to expire the files in the cache set the expireCacheCopy flag.
        try {
            URL pathUrl = null;
            if (expireCacheCopy) {
                // Try getting from resource first
                URL resourceURL = getFromResources(pathString);
                if (resourceURL != null) {
                    pathUrl = resourceURL;
                }
            }
            if (pathUrl == null) {
                pathUrl = new URL(pathString);
            }

            saveRemoteResource(pathUrl, cachePath, shibbolethNegotiator, expireCacheCopy, followRedirect, abortFlag, progressLabel);
        } catch (MalformedURLException mul) {
            logError(pathString, mul);
        }
        return cachePath;
    }

    public boolean replaceCacheCopy(String pathString) {
        File cachePath = getSaveLocation(pathString);
        boolean fileDownloadedBoolean = false;
        try {
            fileDownloadedBoolean = saveRemoteResource(new URL(pathString), cachePath, null, true, false, new DownloadAbortFlag(), null);
        } catch (MalformedURLException mul) {
            logError(mul);
        }
        return fileDownloadedBoolean;
    }

    /**
     * Removes the cache path component from a path string and appends it to the
     * destination directory. Then tests for and creates the directory structure
     * in the destination directory if required.
     *
     * @param pathString Path of a file within the cache.
     * @param destinationDirectory Path of the destination directory.
     * @return The path of the file in the destination directory.
     */
    public File getExportPath(String pathString, String destinationDirectory) {
        logger.debug("pathString: {}", pathString);
        logger.debug("destinationDirectory: {}", destinationDirectory);
        String cachePath = pathString;
        for (String testDirectory : new String[]{"imdicache", getProjectDirectoryName()}) {
            if (pathString.contains(testDirectory)) {
                cachePath = destinationDirectory + cachePath.substring(cachePath.lastIndexOf(testDirectory) + testDirectory.length()); // this path must be inside the cache for this to work correctly
            }
        }
        File returnFile = new File(cachePath);
        if (!returnFile.getParentFile().exists()) {
            if (!returnFile.getParentFile().mkdirs()) {
                logger.error("Could not create directory structure for export of {}", pathString);
                return null;
            }
        }
        return returnFile;
    }

    public URI getNewArbilFileName(File parentDirectory, String nodeType) {
        String suffixString;
        if (nodeType.endsWith(".cmdi") || CmdiProfileReader.pathIsProfile(nodeType)) {
            suffixString = ".cmdi";
        } else {
            suffixString = ".imdi";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        int fileCounter = 0;
        File returnFile = new File(parentDirectory, formatter.format(new Date()) + suffixString);
        while (returnFile.exists()) {
            returnFile = new File(parentDirectory, formatter.format(new Date()) + (fileCounter++) + suffixString);
        }
        return returnFile.toURI();
    }

    /**
     * Tries to find a match ('mirror') for the requested path string in the
     * resources. The method may depend on the type of the requested file In the
     * case of http/www.w3.org/2001/xml.xsd (which exists in the jar file) this
     * will be retrieved from the Arbil jar file rather than from the web.
     *
     * @param pathString Requested file
     * @return Resource URL if available in resources, otherwise null
     */
    public URL getFromResources(String pathString) {
        if (pathString.endsWith(".xsd")) {
            pathString = fixCachePath("/nl/mpi/arbil/resources/xsd/" + preProcessPathString(pathString));
            URL resourceURL = ArbilSessionStorage.class.getResource(pathString);
            if (resourceURL != null) {
                return resourceURL;
            }
        }
        return null;
    }

    /**
     * Converts a String path from the remote location to the respective
     * location in the cache. Then tests for and creates the directory structure
     * in the cache if required.
     *
     * @param pathString Path of the remote file.
     * @return The path in the cache for the file.
     */
    public File getSaveLocation(String pathString) {
        pathString = preProcessPathString(pathString);
        for (String searchString : new String[]{".linorg/imdicache", ".arbil/imdicache", ".linorg\\imdicache", ".arbil\\imdicache", getProjectDirectoryName()}) {
            if (pathString.indexOf(searchString) > -1) {
                logger.error("Recursive path error (about to be corrected) in: {}", pathString);
                pathString = pathString.substring(pathString.lastIndexOf(searchString) + searchString.length());
            }
        }
        String cachePath = fixCachePath(pathString);
        File returnFile = new File(getProjectWorkingDirectory(), cachePath);
        if (!returnFile.getParentFile().exists()) {
            if (!returnFile.getParentFile().mkdirs()) {
                logger.error("Could not ccrate directory structure for saving {}", pathString);
                return null;
            }
        }
        return returnFile;
    }

    private String preProcessPathString(String pathString) {
        try {
            pathString = URLDecoder.decode(pathString, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            logError(uee);
        }
        pathString = pathString.replace("//", "/");
        return pathString;
    }

    private String fixCachePath(String pathString) {
        String cachePath = pathString.replace(":/", "/").replace("//", "/").replace('?', '/').replace('&', '/').replace('=', '/');
        while (cachePath.contains(":")) { // todo: this may not be the only char that is bad on file systems and this will cause issues reconstructing the url later
            cachePath = cachePath.replace(":", "_");
        }
        // make the xsd path tidy for viewing in an editor durring testing
        cachePath = cachePath.replaceAll("/xsd$", ".xsd");
        if (cachePath.matches(".*/[^.]*$")) {
            // rest paths will create files and then require directories of the same name and this must be avoided
            cachePath = cachePath + ".dat";
        }
        return cachePath;
    }

    /**
     * Copies a remote file over http and saves it into the cache.
     *
     * @param targetUrlString The URL of the remote file as a string
     * @param destinationPath The local path where the file should be saved
     * @return boolean true only if the file was downloaded, this will be false
     * if the file exists but was not re-downloaded or if the download failed
     */
    public boolean saveRemoteResource(URL targetUrl, File destinationFile, ShibbolethNegotiator shibbolethNegotiator, boolean expireCacheCopy, boolean followRedirect, DownloadAbortFlag abortFlag, ProgressListener progressLabel) {
        boolean downloadSucceeded = false;
//        String targetUrlString = getFullResourceURI();
//        String destinationPath = GuiHelper.linorgSessionStorage.getSaveLocation(targetUrlString);
//        logger.debug("saveRemoteResource: " + targetUrlString);
//        logger.debug("destinationPath: " + destinationPath);
//        File destinationFile = new File(destinationPath);
        if (destinationFile.length() == 0) {
	    // todo: check the file size on the server and maybe its date also
            // if the file is zero length then is presumably should either be replaced or the version in the jar used.
            if (destinationFile.delete()) {
                logger.debug("Deleted zero length (!) file: " + destinationFile);
            }
        }
        String fileName = destinationFile.getName();
        if (!destinationFile.exists() || expireCacheCopy || destinationFile.length() <= 0) {
            FileOutputStream outFile = null;
            File tempFile = null;
            try {
                URLConnection urlConnection = openResourceConnection(targetUrl, shibbolethNegotiator, followRedirect);

                if (urlConnection != null) {
                    tempFile = File.createTempFile(destinationFile.getName(), "tmp", destinationFile.getParentFile());
                    tempFile.deleteOnExit();
                    int bufferLength = 1024 * 3;
                    outFile = new FileOutputStream(tempFile); //targetUrlString
                    logger.debug("getting file");
                    InputStream stream = urlConnection.getInputStream();
                    byte[] buffer = new byte[bufferLength]; // make this 1024*4 or something and read chunks not the whole file
                    int bytesread = 0;
                    int totalRead = 0;
                    while (bytesread >= 0 && !abortFlag.abortDownload) {
                        bytesread = stream.read(buffer);
                        totalRead += bytesread;
//                        logger.debug("bytesread: " + bytesread);
//                        logger.debug("Mbs totalRead: " + totalRead / 1048576);
                        if (bytesread == -1) {
                            break;
                        }
                        outFile.write(buffer, 0, bytesread);
                        if (progressLabel != null) {
                            progressLabel.setProgressText(fileName + " : " + totalRead / 1024 + " Kb");
                        }
                    }
                    outFile.close();
                    outFile = null;
                    if (tempFile.length() > 0 && !abortFlag.abortDownload) { // TODO: this should check the file size on the server
                        if (destinationFile.exists()) {
                            if (!destinationFile.delete()) {
                                throw new Exception("Changes not saved. Could not delete old file " + destinationFile.toString());
                            }
                        }
                        if (tempFile.renameTo(destinationFile)) {
                            tempFile = null;
                        } else {
                            throw new Exception("Changes not saved. Could not rename temporary file to " + destinationFile.toString());
                        }
                        downloadSucceeded = true;
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("Downloaded: %.3f Mb", ((double) totalRead) / (1024 * 1024)));
                    }
                }
            } catch (Exception ex) {
                logError(ex);
//                logger.debug(ex.getMessage());
            } finally {
                if (outFile != null) {
                    try {
                        outFile.close();
                    } catch (IOException ioe) {
                        logError(ioe);
                    }
                }
                if (tempFile != null && tempFile.exists()) {
                    if (!tempFile.delete()) {
                        BugCatcherManager.getBugCatcher().logError("Could not delete temporary file " + tempFile.getAbsolutePath(), null);
                    }
                }
            }
        }
        return downloadSucceeded;
    }

    /**
     * Opens connection to resource at target url. Follows redirects if required
     *
     * @param resourceUrl
     * @param shibbolethNegotiator
     * @return Connection to resource. Null if response code not ok (not 200
     * after optional redirects)
     * @throws IOException
     */
    private URLConnection openResourceConnection(URL resourceUrl, ShibbolethNegotiator shibbolethNegotiator, boolean followRedirects) throws IOException {
        URLConnection urlConnection = resourceUrl.openConnection();
        if (urlConnection instanceof JarURLConnection) {
            return urlConnection;
        }
        HttpURLConnection httpConnection = null;
        if (urlConnection instanceof HttpURLConnection) {
            httpConnection = (HttpURLConnection) urlConnection;
//                    httpConnection.setFollowRedirects(false); // this is done when this class is created because it is a static call
            //h.setFollowRedirects(false);
            if (logger.isDebugEnabled()) {
                logger.debug("Code: {}, Message: {}", httpConnection.getResponseCode(), httpConnection.getResponseMessage() + resourceUrl.toString());
            }
        }

        if (httpConnection != null && httpConnection.getResponseCode() != 200) { // if the url points to a file on disk then the httpconnection will be null, hence the response code is only relevant if the connection is not null
            final int responseCode = httpConnection.getResponseCode();
            if (responseCode == 301 || responseCode == 302 || responseCode == 303 || responseCode == 307) { // Redirect codes
                String redirectLocation = httpConnection.getHeaderField("Location");
                logger.debug("{}, redirect to {}", responseCode, redirectLocation);
                if (followRedirects) {
                    // Redirect. Get new location.
                    if (redirectLocation != null && redirectLocation.length() > 0) {
                        try {
                            URI resolvedRedirectLocation = resourceUrl.toURI().resolve(redirectLocation);
                            return openResourceConnection(resolvedRedirectLocation.toURL(), shibbolethNegotiator, true);
                        } catch (URISyntaxException ex) {
                            BugCatcherManager.getBugCatcher().logError(String.format("Cannot resolve redirect location: %s. Reference URL: %s", redirectLocation, resourceUrl), ex);
                            return null;
                        }
                    }
                } else {
                    logger.debug("Not following redirect. Skipping file");
                }
            } else {
                logger.debug("non 200 response, skipping file");
            }
            return null;
        } else {
            return urlConnection;
        }
    }

    public File getTypeCheckerConfig() {
        File typeCheckerConfig = new File(getApplicationSettingsDirectory(), TYPECHECKER_CONFIG_FILENAME);
        if (typeCheckerConfig.exists()) {
            return typeCheckerConfig;
        } else {
            return null;
        }
    }
    private Boolean propertiesReaderWriterAvailable = null;

    /**
     * Writing to an (encoding-specific) StreamWriter is not supported until
     * 1.6. Checks if this is available.
     *
     * @return
     * @throws SecurityException
     */
    private synchronized boolean canUsePropertiesReaderWriter() throws SecurityException {
        if (propertiesReaderWriterAvailable == null) {
            try {
                propertiesReaderWriterAvailable = null != Properties.class.getMethod("store", Writer.class, String.class);
            } catch (NoSuchMethodException ex) {
                propertiesReaderWriterAvailable = false;
            }
        }
        return propertiesReaderWriterAvailable;
    }
}
