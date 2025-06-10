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
package nl.mpi.arbil.templates;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.clarin.profiles.CmdiProfileProvider.CmdiProfile;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.clarin.profiles.CmdiProfileSchemaHeaderReader;
import nl.mpi.arbil.clarin.profiles.CmdiTemplate;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
//import nl.mpi.arbil.userstorage.ArbilConfiguration;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.DownloadAbortFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ArbilTemplateManager.java
 * Created on Jul 15, 2009, 11:56:57 AM
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTemplateManager {

    private final static Logger logger = LoggerFactory.getLogger(ArbilTemplateManager.class);
    public static final String CLARIN_PREFIX = "clarin:";
    public static final String CUSTOM_PREFIX = "custom:";
    private static final String PREFIX_REGEX = "^(clarin|custom):(\"(.*)\":)?(.*)$";
    private static final Pattern PREFIX_PATTERN = Pattern.compile(PREFIX_REGEX);
    private final CmdiProfileSchemaHeaderReader headerReader = new CmdiProfileSchemaHeaderReader();
    private static SessionStorage sessionStorage;
//    private ArbilConfiguration applicationConfiguration = new ArbilConfiguration(); //TODO: make immutable and get injected through constructor (post singleton)
    private boolean verbatimXmlTreeStructure = false;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    //private String defaultArbilTemplateName;
    static private ArbilTemplateManager singleInstance = null;
    private Hashtable<String, ArbilTemplate> templatesHashTable;
    private String[] builtInTemplates2 = {"Default", "Sign Language", "Language and Genetics"}; // the first item in this list is the default template

//    private ArbilTemplate defaultArbilTemplate;
    //public String[] builtInTemplates = {"Corpus Branch (internal)", "Session (internal)", "Catalogue (internal)", "Sign Language (internal)"};
    static synchronized public ArbilTemplateManager getSingleInstance() {
	if (singleInstance == null) {
	    singleInstance = new ArbilTemplateManager();
	}
	return singleInstance;
    }

    /**
     * Returns a matcher for template string. The matcher should match the entire template string. It has the following groups:<br />
     * 1. The template type (clarin or custom)
     * 2. (optional group, should not be used)
     * 3. The name of the profile, optional so result can be null
     * 4. The location of the profile
     *
     * @param templateString
     * @return matcher for the provided template string
     */
    public static Matcher getTemplateStringMatcher(String templateString) {
	return PREFIX_PATTERN.matcher(templateString);
    }

    /**
     * Create new template of the given name from default
     *
     * @param selectedTemplate Name of the new template. Cannot be empty or equal to the name of a built in template
     * @return File handle to the newly created template file, or null if the request is invalid
     */
    public File createTemplate(String selectedTemplate) {
	if (selectedTemplate.length() == 0) {
	    // Template name cannot be null
	    return null;
	} else if (Arrays.binarySearch(builtInTemplates2, selectedTemplate) > -1) {
	    // Cannot have the name of one of the built in templates
	    return null;
	} else {
	    File selectedTemplateFile = getTemplateFile(selectedTemplate);
	    // Make directory for new template
	    selectedTemplateFile.getParentFile().mkdir();
	    // Copy template.xml from jar to the new directory
	    sessionStorage.saveRemoteResource(MetadataReader.class.getResource("/nl/mpi/arbil/resources/templates/template.xml"), selectedTemplateFile, null, true, false, new DownloadAbortFlag(), null);
	    // Make components directory
	    File componentsDirectory = new File(selectedTemplateFile.getParentFile(), "components");
	    componentsDirectory.mkdir(); // create the components directory
	    // Copy default.xml from jar to components directory
	    sessionStorage.saveRemoteResource(MetadataReader.class.getResource("/nl/mpi/arbil/resources/templates/default.xml"), new File(componentsDirectory, "default.xml"), null, true, false, new DownloadAbortFlag(), null);
	    // Make example-components directory
	    File examplesDirectory = new File(selectedTemplateFile.getParentFile(), "example-components");
	    if (!examplesDirectory.mkdir()) { // create the example components directory
		logger.error("Could not create example components directory: {}");
	    }
	    // copy example components from the jar file
	    for (String[] pathString : ArbilTemplateManager.getSingleInstance().getTemplate(builtInTemplates2[0]).templatesArray) {
		sessionStorage.saveRemoteResource(MetadataReader.class.getResource("/nl/mpi/arbil/resources/templates/" + pathString[0]), new File(examplesDirectory, pathString[0]), null, true, false, new DownloadAbortFlag(), null);
	    }
	    // copy example "format.xsl" from the jar file which is used in the imdi to html conversion
	    sessionStorage.saveRemoteResource(MetadataReader.class.getResource("/nl/mpi/arbil/resources/xsl/imdi-viewer.xsl"), new File(selectedTemplateFile.getParentFile(), "example-format.xsl"), null, true, false, new DownloadAbortFlag(), null);
	    return selectedTemplateFile;
	}
    }

    public File getTemplateFile(String currentTemplate) {
	File currentTemplateFile = new File(getTemplateDirectory().getAbsolutePath() + File.separatorChar + currentTemplate + File.separatorChar + "template.xml");
//        if (!currentTemplateFile.getParentFile().exists()) {
//            currentTemplateFile.getParentFile().mkdir();
//        }
	return currentTemplateFile;
    }

    public File getDefaultComponentOfTemplate(String currentTemplate) {
	File currentTemplateFile = new File(getTemplateDirectory().getAbsolutePath() + File.separatorChar + currentTemplate + File.separatorChar + "components" + File.separatorChar + "default.xml");
	return currentTemplateFile;
    }

//    public boolean defaultTemplateIsCurrentTemplate() {
//        return defaultArbilTemplateName.equals(builtInTemplates2[0]);
//    }
//    public String getCurrentTemplateName() {
//        return defaultArbilTemplateName;
//    }
//    public void setCurrentTemplate(String currentTemplateLocal) {
//        defaultArbilTemplateName = currentTemplateLocal;
//        try {
//            LinorgSessionStorage.getSingleInstance().saveString("CurrentTemplate", currentTemplateLocal);
//        } catch (Exception ex) {
//            GuiHelper.linorgBugCatcher.logError(ex);
//        }
//    }
    public File getTemplateDirectory() {
	return new File(sessionStorage.getProjectDirectory(), "templates");
    }

    /**
     * Stores the provided location as a custom profile according to the schema <em>custom:"&lt;profile name&gt;":&lt;profile
     * location&gt;</em>
     *
     * @param profileLocation
     */
    public void addCustomProfile(String profileLocation) {
	final CmdiProfile profile = headerReader.getProfile(profileLocation);
	final String templateString = String.format("%s\"%s\":%s", CUSTOM_PREFIX, profile.name, profileLocation);
	addSelectedTemplates(templateString);
    }

    public void addSelectedTemplates(String templateString) {
	ArrayList<String> selectedTemplates = new ArrayList<String>();
	try {
	    selectedTemplates.addAll(Arrays.asList(loadSelectedTemplates()));
	} catch (Exception e) {
	    BugCatcherManager.getBugCatcher().logError("No selectedTemplates file, will create one now.", e);
	}
	if (!selectedTemplates.contains(templateString)) {
	    selectedTemplates.add(templateString);
	}
	try {
	    saveSelectedTemplates(selectedTemplates);
	} catch (IOException ex) {
	    BugCatcherManager.getBugCatcher().logError("Could not crate new selectedTemplates file.", ex);
	}
    }

    private String[] loadSelectedTemplates() throws IOException {
	return sessionStorage.loadStringArray("selectedTemplates");
    }

    private void saveSelectedTemplates(ArrayList<String> selectedTamplates) throws IOException {
	sessionStorage.saveStringArray("selectedTemplates", selectedTamplates.toArray(new String[]{}));
    }

    public void removeSelectedTemplates(String templateString) {
	ArrayList<String> selectedTamplates = new ArrayList<String>();
	try {
	    selectedTamplates.addAll(Arrays.asList(loadSelectedTemplates()));
	    while (selectedTamplates.contains(templateString)) {
		selectedTamplates.remove(templateString);
	    }
	    saveSelectedTemplates(selectedTamplates);
	} catch (IOException ex) {
	    BugCatcherManager.getBugCatcher().logError("Could not load or create selectedTemplates file.", ex);
	}
    }

    public List<String> getCMDIProfileHrefs() {
	final List<String> profilesToReload = new ArrayList<String>();
	for (String templateString : getSelectedTemplates()) {
	    final Matcher matcher = getTemplateStringMatcher(templateString);
	    if (matcher.matches()) {// && matcher.group(1).equals(CUSTOM_PREFIX) || matcher.group(1).equals(CLARIN_PREFIX)) {
//	    if (templateString.startsWith(ArbilTemplateManager.CUSTOM_PREFIX) || templateString.startsWith(ArbilTemplateManager.CLARIN_PREFIX)) {
		// Remove prefix and name
		final String xsdHref = matcher.group(4); //templateString.replaceFirst(PREFIX_REGEX, "");
		profilesToReload.add(xsdHref);
	    }
	}
	return profilesToReload;
    }

    public List<String> getSelectedTemplates() {
	ArrayList<String> selectedTamplates = new ArrayList<String>();
	try {
	    selectedTamplates.addAll(Arrays.asList(loadSelectedTemplates()));
	} catch (Exception e) {
	    BugCatcherManager.getBugCatcher().logError("No selectedTemplates file, will create one now.", e);
	    addDefaultImdiTemplates();
	}
	return selectedTamplates;
    }

    public static class MenuItemData {

	public enum Type {

	    IMDI, CMDI, OTHER
	}
	public Type type;
	public String menuText;
	public String menuAction;
	public String menuToolTip;
	public ImageIcon menuIcon;
    }

    public void addDefaultImdiTemplates() {
	addSelectedTemplates("builtin:METATRANSCRIPT.Corpus.xml");
	addSelectedTemplates("builtin:METATRANSCRIPT.Catalogue.xml");
	addSelectedTemplates("builtin:METATRANSCRIPT.Session.xml");
    }

    public void removeDefaultImdiTemplates() {
	removeSelectedTemplates("builtin:METATRANSCRIPT.Corpus.xml");
	removeSelectedTemplates("builtin:METATRANSCRIPT.Catalogue.xml");
	removeSelectedTemplates("builtin:METATRANSCRIPT.Session.xml");
    }

    private MenuItemData createMenuItemForTemplate(String location) {
	ArbilIcons arbilIcons = ArbilIcons.getSingleInstance();
	MenuItemData menuItem = new MenuItemData();
	menuItem.type = MenuItemData.Type.IMDI;
	if (location.startsWith("builtin:")) {
	    String currentString = location.substring("builtin:".length());
	    return getMenuItemDataForBuiltinTemplate(currentString, menuItem, arbilIcons);
	} else if (location.startsWith("template:")) {
	    String currentString = location.substring("template:".length());
	    menuItem.menuText = currentString;
	    menuItem.menuAction = currentString;
	    menuItem.menuToolTip = currentString;
	    menuItem.menuIcon = arbilIcons.sessionColorIcon;
	} else if (location.startsWith(CUSTOM_PREFIX)) {
	    menuItem.type = MenuItemData.Type.CMDI;
	    final Matcher matcher = getTemplateStringMatcher(location);
	    if (matcher.find()) {
		final String profileUrl = matcher.group(4);
		final String profileName = getCustomProfileName(matcher);
		menuItem.menuText = profileName;
		menuItem.menuAction = profileUrl;
		menuItem.menuToolTip = profileUrl;
	    } else {
		menuItem.menuText = location;
	    }
	    menuItem.menuIcon = ArbilIcons.clarinIcon;
	} else if (location.startsWith(CLARIN_PREFIX)) {
	    menuItem.type = MenuItemData.Type.CMDI;

	    String currentString = location.substring(CLARIN_PREFIX.length());
	    CmdiProfile cmdiProfile = CmdiProfileReader.getSingleInstance().getProfile(currentString);
	    if (cmdiProfile == null) {
		menuItem.menuText = "<unknown>";
		menuItem.menuAction = "<unknown>";
		menuItem.menuToolTip = currentString;
	    } else {
		menuItem.menuText = cmdiProfile.name;
		menuItem.menuAction = cmdiProfile.getXsdHref();
		menuItem.menuToolTip = cmdiProfile.description;
	    }
	    menuItem.menuIcon = ArbilIcons.clarinIcon;
	} else {
	    BugCatcherManager.getBugCatcher().logError("Unknown template location type in " + location, null);
	}
	return menuItem;
    }

    /**
     * Determines the name for a manually added profile. If present, it returns the name stored in the template string, e.g. an entry
     * <em>custom:"My Custom Profile":file:/myprofile.xsd</em> returns "My Custom Profile". If it is not present, it returns the filename
     * minus the extensions, e.g. <em>custom:file:/test/myprofile.xsd</em> returns "myprofile".
     *
     * @param matcher matcher created on template string for custom profile on {@link #PREFIX_PATTERN}
     * @return a name for the custom profile based on the matcher
     */
    public static String getCustomProfileName(final Matcher matcher) {
	String customName = matcher.group(3);
	if (customName == null) {
	    // Name not stored in the string template string, extract from file name instead
	    customName = matcher.group(4).replaceAll("[/.]xsd$", "");
	    if (customName.contains("/")) {
		customName = customName.substring(customName.lastIndexOf("/") + 1);
	    }
	}
	return customName;
    }

    private MenuItemData getMenuItemDataForBuiltinTemplate(String currentString, MenuItemData menuItem, ArbilIcons arbilIcons) {
	for (String currentTemplateName[] : ArbilTemplateManager.getSingleInstance().getTemplate(null).rootTemplatesArray) {
	    if (currentString.equals(currentTemplateName[0])) {
		menuItem.menuText = currentTemplateName[1];
		menuItem.menuAction = "." + currentTemplateName[0].replaceFirst("\\.xml$", "");
		menuItem.menuToolTip = currentTemplateName[1];
		if (menuItem.menuText.contains("Corpus")) {
		    menuItem.menuIcon = arbilIcons.corpusnodeColorIcon;
		} else if (menuItem.menuText.contains("Catalogue")) {
		    menuItem.menuIcon = arbilIcons.catalogueColorIcon;
		} else {
		    menuItem.menuIcon = arbilIcons.sessionColorIcon;
		}
		return menuItem;
	    }
	}
	BugCatcherManager.getBugCatcher().logError("Could not find builtin template location " + currentString, null);
	// No match found
	return null;
    }

    public MenuItemData[] getSelectedTemplatesMenuItems() {
	String[] locationsArray = null;
	try {
	    locationsArray = loadSelectedTemplates();
	} catch (IOException ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
	if (locationsArray == null || locationsArray.length == 0) {
	    try {
		addDefaultImdiTemplates();
		locationsArray = loadSelectedTemplates();
	    } catch (IOException ex) {
		BugCatcherManager.getBugCatcher().logError(ex);
	    }
	}
	if (locationsArray == null) {
	    return new MenuItemData[0];
	}

	List<MenuItemData> returnArray = new ArrayList<MenuItemData>(locationsArray.length);
	for (int insertableCounter = 0; insertableCounter < locationsArray.length; insertableCounter++) {
	    final String templateLocation = locationsArray[insertableCounter];
	    final MenuItemData menuItemData = createMenuItemForTemplate(templateLocation);
	    if (menuItemData != null) {
		returnArray.add(menuItemData);
	    }
	}
	Collections.sort(returnArray, new Comparator<MenuItemData>() {
	    public int compare(MenuItemData firstItem, MenuItemData secondItem) {
		return firstItem.menuText.compareToIgnoreCase(secondItem.menuText);
	    }
	});
	return returnArray.toArray(new MenuItemData[]{});
    }

//     
//                Vector childTypes = new Vector();
//        if (targetNodeUserObject instanceof ImdiTreeObject) {
//            String xpath = MetadataReader.getNodePath((ImdiTreeObject) targetNodeUserObject);
//            childTypes = getSubnodesFromTemplatesDir(xpath); // add the main entries based on the node path of the target
//            if (((ImdiTreeObject) targetNodeUserObject).isCorpus()) { // add any corpus node entries
//                for (String[] currentTemplate : rootTemplatesArray) {
//                    boolean suppressEntry = false;
//                    if (currentTemplate[1].equals("Catalogue")) {
//                        if (((ImdiTreeObject) targetNodeUserObject).hasCatalogue()) {
//                            // make sure the catalogue can only be added once
//                            suppressEntry = true;
//                        }
//                    }
//                    if (!suppressEntry) {
//                        childTypes.add(new String[]{currentTemplate[1], "." + currentTemplate[0].replaceFirst("\\.xml$", "")});
//                    }
//                }
//            }
////            logger.debug("childTypes: " + childTypes);
//        } else {
//            // add the the root node items
//            for (String[] currentTemplate : rootTemplatesArray) {
//                if (!currentTemplate[1].equals("Catalogue")) {// make sure the catalogue can not be added at the root level
//                    childTypes.add(new String[]{"Unattached " + currentTemplate[1], "." + currentTemplate[0].replaceFirst("\\.xml$", "")});
//                }
//            }
//        }
//        Collections.sort(childTypes, new Comparator() {
//
//            public int compare(Object o1, Object o2) {
//                String value1 = ((String[]) o1)[0];
//                String value2 = ((String[]) o2)[0];
//                return value1.compareTo(value2);
//            }
//        });
//        return childTypes.elements();
//    HashSet<String> locationsSet = new HashSet<String>();
//            for (ImdiTreeObject[] currentTreeArray : new ImdiTreeObject[][]{remoteCorpusNodes, localCorpusNodes, localFileNodes, favouriteNodes}) {
//                for (ImdiTreeObject currentLocation : currentTreeArray) {
//                    locationsSet.add(currentLocation.getUrlString());
//                }
//            }
//            if (nodesToAdd != null) {
//                for (ImdiTreeObject currentAddable : nodesToAdd) {
//                    locationsSet.add(currentAddable.getUrlString());
//                }
//            }
//            if (nodesToRemove != null) {
//                for (ImdiTreeObject currentRemoveable : nodesToRemove) {
//                    locationsSet.remove(currentRemoveable.getUrlString());
//                }
//            }
//            Vector<String> locationsList = new Vector<String>(); // this vector is kept for backwards compatability
//            for (String currentLocation : locationsSet) {
//                locationsList.add(URLDecoder.decode(currentLocation, "UTF-8"));
//            }
//            //LinorgSessionStorage.getSingleInstance().saveObject(locationsList, "locationsList");
//            LinorgSessionStorage.getSingleInstance().saveStringArray("locationsList", locationsList.toArray(new String[]{}));
    public String[] getAvailableTemplates() {
	File templatesDir = getTemplateDirectory();
	if (!templatesDir.exists()) {
	    if (!templatesDir.mkdir()) {
		logger.error("Could not create template directory: {}", templatesDir);
	    }
	}
	ArrayList<String> templateList = new ArrayList<String>();
	String[] templatesList = templatesDir.list();
	for (String currentTemplateName : templatesList) {
	    // if the template file does not exist then remove from the array
	    if (getTemplateFile(currentTemplateName).exists()) {
		templateList.add(currentTemplateName);
	    }
	}
//        for (String currentTemplateName : builtInTemplates) {
//            // add the Default and SignLanguage built in templates
//            templateList.add(currentTemplateName);
//        }
	templatesList = templateList.toArray(new String[]{});
	Arrays.sort(templatesList);
	return templatesList;
    }

    private ArbilTemplateManager() {
	templatesHashTable = new Hashtable<String, ArbilTemplate>();
//        defaultArbilTemplateName = LinorgSessionStorage.getSingleInstance().loadString("CurrentTemplate");
//        if (defaultArbilTemplateName == null) {
//            defaultArbilTemplateName = builtInTemplates2[0];
//            LinorgSessionStorage.getSingleInstance().saveString("CurrentTemplate", defaultArbilTemplateName);
//        }
    }

    public ArbilTemplate getDefaultTemplate() {
	return getTemplate(builtInTemplates2[0]);
    }
    private final Map<String, Object> cmdiLoadingState = new HashMap<String, Object>();

    /**
     * Gets a CMDI template by namespace string. Each template gets loaded only once, so callers might end
     * up in a queue.
     *
     * @param nameSpaceString
     * @return
     */
    public ArbilTemplate getCmdiTemplate(String nameSpaceString) {
	if (nameSpaceString != null) {
	    try {
		// Check if template is being loaded and if so retrieve it
		ArbilTemplate template = waitForCmdiTemplateProfileLoading(nameSpaceString);
		// Did we get a loaded CMDI profile template?
		if (template == null) {
		    // Template has not been loaded, do so now
		    template = loadCmdiTemplateProfile(nameSpaceString);
		}
		return template;
	    } catch (InterruptedException iEx) {
		BugCatcherManager.getBugCatcher().logError("Interrupted while waiting for CMDI profile to load", iEx);
		return null;
	    }
	} else {
	    logger.error("Name space URL not provided, cannot load the CMDI template, please check the XML file and ensure that the name space is specified.");
	    return null;
	}
    }

    /**
     * Checks if template is being loaded, and if so wait for it to finish loading.
     * If the template has not been loaded and is not being loading, creates a lock object in
     * {@link #cmdiLoadingState}.
     *
     * @param nameSpaceString
     * @return CMDI profile template if it was already loaded. Null if it still needs to be loaded
     * @throws InterruptedException if interrupted while waiting
     */
    private CmdiTemplate waitForCmdiTemplateProfileLoading(String nameSpaceString) throws InterruptedException {
	synchronized (cmdiLoadingState) {
	    // Look for lock object for namespace string
	    while (cmdiLoadingState.containsKey(nameSpaceString)) {
		// Lock object found, wait for loading to finish
		cmdiLoadingState.wait();
	    }
	    // Nothing is loading, see if already loaded
	    CmdiTemplate cmdiTemplate = (CmdiTemplate) templatesHashTable.get(nameSpaceString);
	    if (cmdiTemplate != null) {
		// Was already loaded, return
		return cmdiTemplate;
	    } else {
		// Not loading, not loaded. Prepare for loading now.
		cmdiLoadingState.put(nameSpaceString, new Object());
		return null;
	    }
	}
    }

    public void unloadCmdiTemplates() {
	synchronized (cmdiLoadingState) {
	    templatesHashTable.clear();
	}
    }

    /**
     * Loads a CMDI profile template and tells others to wait for it.
     * After loading has finished, removes the lock object from {@link #cmdiLoadingState} and notifies
     * all waiting threads.
     *
     * @param nameSpaceString
     * @return Loaded CMDI template
     */
    private ArbilTemplate loadCmdiTemplateProfile(String nameSpaceString) {
	try {
	    CmdiTemplate cmdiTemplate = new CmdiTemplate(sessionStorage, verbatimXmlTreeStructure);
	    cmdiTemplate.loadTemplate(nameSpaceString, CmdiProfileReader.getSingleInstance());
	    cmdiTemplate.startLoadingDatacategoryDescriptions();
	    templatesHashTable.put(nameSpaceString, cmdiTemplate);
	    return cmdiTemplate;
	} finally {
	    // remove from loading state whether load was successful or not
	    synchronized (cmdiLoadingState) {
		cmdiLoadingState.remove(nameSpaceString);
		cmdiLoadingState.notifyAll();
	    }
	}
    }

    public ArbilTemplate getTemplate(String templateName) {
	if (templateName == null || templateName.length() < 1) {
	    return getDefaultTemplate(); // if the template string is not provided the default template is used
	}
	if (!templatesHashTable.containsKey(templateName)) {
	    ArbilTemplate returnTemplate = new ArbilTemplate(sessionStorage);
	    if (returnTemplate.readTemplate(getTemplateFile(templateName), templateName)) {
		templatesHashTable.put(templateName, returnTemplate);
		return returnTemplate;
	    } else {
		return getDefaultTemplate();
	    }
	} else {
	    return templatesHashTable.get(templateName);
	}
    }

//    public void setApplicationConfiguration(ArbilConfiguration applicationConfiguration) {
//	this.applicationConfiguration = applicationConfiguration;
//    }
}
