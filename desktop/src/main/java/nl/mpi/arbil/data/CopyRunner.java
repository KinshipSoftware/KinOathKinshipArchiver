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
package nl.mpi.arbil.data;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.data.metadatafile.MetadataUtils;
import nl.mpi.arbil.data.metadatafile.MetadataUtils.MetadataLinkSet;
import nl.mpi.arbil.ui.ImportExportUI;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.XsdChecker;
import nl.mpi.arbilcommons.journal.ArbilJournal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runner for copy actions in import or export context. Split off from {@link nl.mpi.arbil.ui.ImportExportDialog}
 *
 * @author Twan.Goosen@mpi.nl
 * @author Peter.Withers@mpi.nl
 */
public class CopyRunner implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(CopyRunner.class);
    public final static String DISK_FREE_LABEL_TEXT = "Total Disk Free: ";
    private final ImportExportUI impExpUI;
    private final SessionStorage sessionStorage;
    private final DataNodeLoader dataNodeLoader;
    private final TreeHelper treeHelper;
    private Boolean exportToUniqueLocation = null;

    public CopyRunner(ImportExportUI ui, SessionStorage sessionStorage, DataNodeLoader dataNodeLoader, TreeHelper treeHelper) {
	this.impExpUI = ui;
	this.sessionStorage = sessionStorage;
	this.dataNodeLoader = dataNodeLoader;
	this.treeHelper = treeHelper;
    }
    private int freeGbWarningPoint = 3;
    private int xsdErrors = 0;
    private int totalLoaded = 0;
    private int totalErrors = 0;
    private int totalExisting = 0;
    private int resourceCopyErrors = 0;
    private String finalMessageString = "";
    private File directoryForSizeTest;
    private boolean testFreeSpace;

    @Override
    public void run() {
	String javaVersionString = System.getProperty("java.version");
	// TG: Apparently test not required for version >= 1.5 (2011/2/3)
	testFreeSpace = !(javaVersionString.startsWith("1.4.") || javaVersionString.startsWith("1.5."));

	// Export destination will be null in case of import into local corpus
	final File exportDestinationDirectory = impExpUI.getExportDestinationDirectory();
	directoryForSizeTest = exportDestinationDirectory != null ? exportDestinationDirectory : sessionStorage.getProjectWorkingDirectory();
	// Append message about copying resource files to the copy output
	if (impExpUI.isCopyFilesOnImport() || impExpUI.isCopyFilesOnExport()) {
	    impExpUI.appendToResourceCopyOutput("'Copy Resource Files' is selected: Resource files will be downloaded where appropriate permission are granted." + "\n");
	} else {
	    impExpUI.appendToResourceCopyOutput("'Copy Resource Files' is not selected: No resource files will be downloaded, however they will be still accessible via the web server." + "\n");
	}
	try {
	    impExpUI.onCopyStart();
	    impExpUI.setProgressIndeterminate(true);
	    // Copy the selected nodes
	    copyElements(impExpUI.getSelectedNodesIterator(), impExpUI.getDestinationNode(), exportDestinationDirectory);
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	    finalMessageString = finalMessageString + "There was a critical error.";
	}
	// Done copying
	impExpUI.onCopyEnd(finalMessageString);
    }

    private void copyElements(final Iterator<ArbilDataNode> selectedNodesEnum, final ArbilDataNode destinationNode, final File exportDestinationDirectory) {
	final List<ArbilDataNode> finishedTopNodes = new ArrayList<ArbilDataNode>();
	final Map<URI, RetrievableFile> seenFiles = new HashMap<URI, RetrievableFile>();
	final List<URI> getList = new ArrayList<URI>();
	final List<URI> doneList = new ArrayList<URI>();
	final XsdChecker xsdChecker = new XsdChecker();
	while (selectedNodesEnum.hasNext() && !impExpUI.isStopCopy()) {
	    final ArbilDataNode currentElement = selectedNodesEnum.next();
	    copyElement(currentElement, exportDestinationDirectory, getList, seenFiles, doneList, xsdChecker, finishedTopNodes);
	}
	finalMessageString = finalMessageString + "Processed " + totalLoaded + " Metadata Files.\n";

	if (exportDestinationDirectory == null) {
	    // This is in the case of an import into the local corpus
	    addImportedNodesToLocalCorpus(destinationNode, finishedTopNodes);
	}

	impExpUI.setProgressIndeterminate(false);

	if (totalErrors != 0) {
	    finalMessageString = finalMessageString + "There were " + totalErrors + " errors, some files may not have been copied.\n";
	}
	if (xsdErrors != 0) {
	    finalMessageString = finalMessageString + "There were " + xsdErrors + " files that failed to validate and have xml errors.\n";
	}
	if (impExpUI.isStopCopy()) {
	    impExpUI.appendToTaskOutput("copy canceled");
	    logger.debug("copy canceled");
	    finalMessageString = finalMessageString + "The process was canceled, some files may not have been copied.\n";
	} else {
	    impExpUI.removeNodeSelection();
	}
    }

    private void addImportedNodesToLocalCorpus(final ArbilDataNode destinationNode, final List<ArbilDataNode> copiedNodes) {
	if (!impExpUI.isStopCopy()) {
	    for (ArbilDataNode currentFinishedNode : copiedNodes) {
		if (destinationNode != null) {
		    // Import into specific corpus inside local corpus
		    if (!destinationNode.getURI().equals(currentFinishedNode.getURI())) {
			destinationNode.addCorpusLink(currentFinishedNode);
		    }
		} else {
		    // Import to root level of local corpus
		    if (!treeHelper.addLocation(currentFinishedNode.getURI())) {
			finalMessageString = finalMessageString + "The location:\n" + currentFinishedNode + "\nalready exists and need not be added again\n";
		    }
		}
		currentFinishedNode.reloadNode();
	    }
	}
	if (destinationNode == null) {
	    treeHelper.applyRootLocations();
	} else {
	    destinationNode.reloadNode();
	}
    }

    private void copyElement(final Object currentElement, final File exportDestinationDirectory, final List<URI> getList, final Map<URI, RetrievableFile> seenFiles, final List<URI> doneList, final XsdChecker xsdChecker, final List<ArbilDataNode> finishedTopNodes) {
	final URI currentGettableUri = ((ArbilDataNode) currentElement).getParentDomNode().getURI();
	getList.add(currentGettableUri);
	if (!seenFiles.containsKey(currentGettableUri)) {
	    seenFiles.put(currentGettableUri, new RetrievableFile(((ArbilDataNode) currentElement).getParentDomNode().getURI(), exportDestinationDirectory));
	}
	while (!impExpUI.isStopCopy() && getList.size() > 0) {
	    RetrievableFile currentRetrievableFile = seenFiles.get(getList.remove(0));
	    copyFile(currentRetrievableFile, exportDestinationDirectory, seenFiles, doneList, getList, xsdChecker);
	}
	if (exportDestinationDirectory == null) {
	    // This is an import into the local corpus
	    File newNodeLocation = sessionStorage.getSaveLocation(((ArbilDataNode) currentElement).getParentDomNode().getUrlString());
	    finishedTopNodes.add(dataNodeLoader.getArbilDataNodeWithoutLoading(newNodeLocation.toURI()));
	}
    }

    private void copyFile(final RetrievableFile currentRetrievableFile, final File exportDestinationDirectory, final Map<URI, RetrievableFile> seenFiles, final List<URI> doneList, final List<URI> getList, final XsdChecker xsdChecker) {
	try {
	    if (!doneList.contains(currentRetrievableFile.sourceURI)) {
		final String journalActionString;
		if (exportDestinationDirectory == null) {
		    // This is an import into the local corpus
		    currentRetrievableFile.calculateUriFileName();
		    journalActionString = "import";
		} else {
		    // This is an export to a location on the file system
		    if (impExpUI.isRenameFileToNodeName()) {
			calculateTreeFileName(currentRetrievableFile, Collections.<URI[]>emptyList());
		    } else {
			currentRetrievableFile.calculateUriFileName();
		    }
		    journalActionString = "export";
		}
		if (impExpUI.isStopCopy()) {
		    return;
		}

		final MetadataUtils metadataUtils = ArbilDataNode.getMetadataUtils(currentRetrievableFile.sourceURI.toString());
		if (metadataUtils == null) {
		    throw new ArbilMetadataException("Metadata format could not be determined");
		}

		final List<URI[]> uncopiedLinks = new ArrayList<URI[]>();
		final MetadataLinkSet linkSet = metadataUtils.getCorpusLinks(currentRetrievableFile.sourceURI);
		if (linkSet != null) {
		    copyLinks(exportDestinationDirectory, linkSet, seenFiles, currentRetrievableFile, getList, uncopiedLinks);
		}

		final boolean overwriteFile;
		if (currentRetrievableFile.destinationFile.exists()) {
		    totalExisting++;
		    overwriteFile = impExpUI.askOverwrite(currentRetrievableFile);
		} else {
		    overwriteFile = false;
		}

		// Request to stop copy can have been triggered from UI (probably when presenting user with pre-existing copy)
		if (!impExpUI.isStopCopy()) {
		    if (currentRetrievableFile.destinationFile.exists() && !overwriteFile) {
			impExpUI.appendToTaskOutput(currentRetrievableFile.sourceURI.toString());
			impExpUI.appendToTaskOutput("Destination already exists, skipping file: " + currentRetrievableFile.destinationFile.getAbsolutePath());
		    } else {
			final boolean replacingExistingFile = currentRetrievableFile.destinationFile.exists() && overwriteFile;
			if (replacingExistingFile) {
			    impExpUI.appendToTaskOutput("Replaced: " + currentRetrievableFile.destinationFile.getAbsolutePath());
			}
			final ArbilDataNode destinationNode = dataNodeLoader.getArbilDataNodeWithoutLoading(currentRetrievableFile.destinationFile.toURI());
			if (destinationNode.getNeedsSaveToDisk(false)) {
			    destinationNode.saveChangesToCache(true);
			}
			if (destinationNode.hasHistory()) {
			    destinationNode.bumpHistory();
			}
			if (!currentRetrievableFile.destinationFile.getParentFile().exists()) {
			    if (!currentRetrievableFile.destinationFile.getParentFile().mkdir()) {
				logger.error("Could not create missing parent directory for {}", currentRetrievableFile.destinationFile);
			    }
			}
			metadataUtils.copyMetadataFile(currentRetrievableFile.sourceURI, currentRetrievableFile.destinationFile, uncopiedLinks.toArray(new URI[][]{}), true);
			ArbilJournal.getSingleInstance().saveJournalEntry(currentRetrievableFile.destinationFile.getAbsolutePath(), "", currentRetrievableFile.sourceURI.toString(), "", journalActionString);
			final String checkerResult = xsdChecker.simpleCheck(currentRetrievableFile.destinationFile);
			if (checkerResult != null) {
			    impExpUI.appendToXmlOutput(currentRetrievableFile.sourceURI.toString() + "\n");
			    impExpUI.appendToXmlOutput("destination path: " + currentRetrievableFile.destinationFile.getAbsolutePath());
			    logger.debug("checkerResult: {}", checkerResult);
			    impExpUI.appendToXmlOutput(checkerResult + "\n");
			    impExpUI.addToValidationErrors(currentRetrievableFile.sourceURI);
			    xsdErrors++;
			}
			if (replacingExistingFile) {
			    dataNodeLoader.requestReloadOnlyIfLoaded(currentRetrievableFile.destinationFile.toURI());
			}
		    }
		}
	    }
	} catch (ArbilMetadataException ex) {
	    logger.error("Error processing {} with destination {}", currentRetrievableFile.sourceURI, currentRetrievableFile.destinationFile, ex);
	    totalErrors++;
	    impExpUI.addToMetadataCopyErrors(currentRetrievableFile.sourceURI);
	    impExpUI.appendToTaskOutput(String.format("Unable to process the file: %1$s (%2$s)", currentRetrievableFile.sourceURI, ex.getLocalizedMessage()));
	} catch (MalformedURLException ex) {
	    logger.error("Error processing {} with destination {}", currentRetrievableFile.sourceURI, currentRetrievableFile.destinationFile, ex);
	    totalErrors++;
	    impExpUI.addToMetadataCopyErrors(currentRetrievableFile.sourceURI);
	    impExpUI.appendToTaskOutput(String.format("Unable to process the file: %1$s", currentRetrievableFile.sourceURI));
	    logger.debug("Error getting links from: {}", currentRetrievableFile.sourceURI);
	} catch (IOException ex) {
	    logger.error("Error while trying to copy {} to {}", currentRetrievableFile.sourceURI, currentRetrievableFile.destinationFile, ex);
	    totalErrors++;
	    impExpUI.addToMetadataCopyErrors(currentRetrievableFile.sourceURI);
	    impExpUI.appendToTaskOutput(String.format("Unable to process the file: %1$s (%2$s)", currentRetrievableFile.sourceURI, ex.getLocalizedMessage()));
	}
	totalLoaded++;
	final int getCount = getList.size();
	impExpUI.updateStatus(getCount, totalLoaded, totalExisting, totalErrors, xsdErrors, resourceCopyErrors);
	if (testFreeSpace) {
	    testFreeSpace();
	}
    }

    private void copyLinks(final File exportDestinationDirectory, MetadataLinkSet linksUriArray, Map<URI, RetrievableFile> seenFiles, RetrievableFile currentRetrievableFile, List<URI> getList, List<URI[]> uncopiedLinks) throws MalformedURLException {
	// Copy resource links
	for (final URI linkUri : linksUriArray.getResourceLinks()) {
	    if (impExpUI.isStopCopy()) {
		return;
	    } else {
		logger.debug("Resource link: {}", linkUri);
		final String currentLink = linkUri.toString();
		final URI gettableLinkUri = linkUri.normalize();
		if (!seenFiles.containsKey(gettableLinkUri)) {
		    seenFiles.put(gettableLinkUri, new RetrievableFile(gettableLinkUri, currentRetrievableFile.childDestinationDirectory));
		}
		final RetrievableFile retrievableLink = seenFiles.get(gettableLinkUri);
		copyResourceLink(currentRetrievableFile, currentLink, retrievableLink, exportDestinationDirectory, uncopiedLinks, linkUri);
	    }
	}

	// Copy metadata links
	for (final URI linkUri : linksUriArray.getMetadataLinks()) {
	    if (impExpUI.isStopCopy()) {
		return;
	    } else {
		logger.debug("Metadata link: {}", linkUri);
		final URI gettableLinkUri = linkUri.normalize();
		if (!seenFiles.containsKey(gettableLinkUri)) {
		    seenFiles.put(gettableLinkUri, new RetrievableFile(gettableLinkUri, currentRetrievableFile.childDestinationDirectory));
		}
		final RetrievableFile retrievableLink = seenFiles.get(gettableLinkUri);
		copyMetadataLink(gettableLinkUri, retrievableLink, exportDestinationDirectory, getList, uncopiedLinks, linkUri);
	    }
	}
    }

    private void copyMetadataLink(final URI gettableLinkUri, final RetrievableFile retrievableLink, final File exportDestinationDirectory, List<URI> getList, List<URI[]> uncopiedLinks, URI linkUri) {
	getList.add(gettableLinkUri);
	if (impExpUI.isRenameFileToNodeName() && exportDestinationDirectory != null) {
	    // On export there is the option to rename the file to the name of the node as present in the metadata
	    calculateTreeFileName(retrievableLink, uncopiedLinks);
	} else {
	    retrievableLink.calculateUriFileName();
	}
	uncopiedLinks.add(new URI[]{linkUri, retrievableLink.destinationFile.toURI()});
    }

    private void copyResourceLink(RetrievableFile currentRetrievableFile, final String currentLink, final RetrievableFile retrievableLink, final File exportDestinationDirectory, List<URI[]> uncopiedLinks, URI linkUri) throws MalformedURLException {
	if (!impExpUI.isCopyFilesOnImport() && !impExpUI.isCopyFilesOnExport()) {
	    uncopiedLinks.add(new URI[]{linkUri, linkUri});
	} else {
	    final File downloadFileLocation;
	    if (exportDestinationDirectory == null) {
		// On export
		downloadFileLocation = sessionStorage.updateCache(currentLink, impExpUI.getShibbolethNegotiator(), false, false, impExpUI.getDownloadAbortFlag(), impExpUI);
	    } else {
		// On import
		if (impExpUI.isRenameFileToNodeName()) {
		    calculateTreeFileName(retrievableLink, uncopiedLinks);
		} else {
		    retrievableLink.calculateUriFileName();
		}
		if (!retrievableLink.destinationFile.getParentFile().exists()) {
		    if (!retrievableLink.destinationFile.getParentFile().mkdirs()) {
			logger.error("Could not create missing parent directory for {}", retrievableLink.destinationFile);
		    }
		}
		downloadFileLocation = retrievableLink.destinationFile;
		sessionStorage.saveRemoteResource(new URL(currentLink), downloadFileLocation, impExpUI.getShibbolethNegotiator(), true, false, impExpUI.getDownloadAbortFlag(), impExpUI);
		impExpUI.setProgressText(" ");
	    }
	    if (downloadFileLocation != null && downloadFileLocation.exists()) {
		impExpUI.appendToTaskOutput("Downloaded resource: " + downloadFileLocation.getAbsolutePath());
		uncopiedLinks.add(new URI[]{linkUri, downloadFileLocation.toURI()});
	    } else {
		impExpUI.appendToResourceCopyOutput("Download failed: " + currentLink + " \n");
		impExpUI.addToFileCopyErrors(currentRetrievableFile.sourceURI);
		uncopiedLinks.add(new URI[]{linkUri, linkUri});
		resourceCopyErrors++;
	    }
	}
    }

    private void testFreeSpace() {
	try {
	    int freeGBytes = (int) (directoryForSizeTest.getFreeSpace() / 1073741824);
	    impExpUI.setDiskspaceState(DISK_FREE_LABEL_TEXT + freeGBytes + "GB");
	    if (freeGbWarningPoint > freeGBytes) {
		impExpUI.setProgressIndeterminate(false);
		if (impExpUI.askContinue("There is only " + freeGBytes + "GB free space left on the disk.\nTo you still want to continue?")) {
		    freeGbWarningPoint = freeGBytes - 1;
		} else {
		    impExpUI.setStopCopy(true);
		}
		impExpUI.setProgressIndeterminate(true);
	    }
	} catch (Exception ex) {
	    impExpUI.setDiskspaceState(DISK_FREE_LABEL_TEXT + "N/A");
	    testFreeSpace = false;
	}
    }

    private void calculateTreeFileName(final RetrievableFile retrievableFile, List<URI[]> reservedLinks) {
	retrievableFile.calculateTreeFileName(impExpUI.isRenameFileToLamusFriendlyName(), reservedLinks);
	if (retrievableFile.destinationFile.exists()) {
	    if (exportToUniqueLocation == null) {
		exportToUniqueLocation = impExpUI.askCreateNewExportDir(retrievableFile.destinationFile);
	    }
	    if (exportToUniqueLocation) {
		retrievableFile.makeUnique(reservedLinks);
	    }
	}
    }

    public class RetrievableFile {

	private final URI sourceURI;
	private final File destinationDirectory;
	private File childDestinationDirectory;
	private File destinationFile;
	private String fileName;
	private String fileSuffix;
	boolean lamusFriendly;
	boolean metadata;

	public RetrievableFile(URI sourceURILocal, File destinationDirectoryLocal) {
	    sourceURI = sourceURILocal;
	    destinationDirectory = destinationDirectoryLocal;
	}

	public void calculateUriFileName() {
	    if (destinationDirectory != null) {
		destinationFile = sessionStorage.getExportPath(sourceURI.toString(), destinationDirectory.getPath());
	    } else {
		destinationFile = sessionStorage.getSaveLocation(sourceURI.toString());
	    }
	    childDestinationDirectory = destinationDirectory;
	}

	/**
	 * Evaluates {@link #destinationFile current destination file} and
	 *
	 * @param lamusFriendly whether destination file should be lamus friendly
	 * @param reservedLinks list of reserved links with each entry being an array of [original file, target file]. reservedLink[1] will
	 * be matched against
	 */
	public void calculateTreeFileName(boolean lamusFriendly, List<URI[]> reservedLinks) {
	    final String urlString = sourceURI.toString();
	    final int suffixSeparator = urlString.lastIndexOf(".");
	    if (suffixSeparator > 0 && suffixSeparator > urlString.lastIndexOf("/")) {
		fileSuffix = urlString.substring(suffixSeparator);
	    } else {
		fileSuffix = "";
	    }

	    final ArbilDataNode currentNode = dataNodeLoader.getArbilDataNode(null, sourceURI);
	    currentNode.waitTillLoaded();

	    fileName = normalizeFileName(determineFileName(currentNode), lamusFriendly);
	    destinationFile = new File(destinationDirectory, fileName + fileSuffix);
	    childDestinationDirectory = new File(destinationDirectory, fileName);
	}

	private void makeUnique(List<URI[]> reservedLinks) {
	    if (destinationFile.exists()) {
		int fileCounter = 1;
		while (destinationExistsOrIsReserved(reservedLinks)) {
		    if (lamusFriendly) {
			destinationFile = new File(destinationDirectory, fileName + "_" + fileCounter + fileSuffix);
			childDestinationDirectory = new File(destinationDirectory, fileName + "_" + fileCounter);
		    } else {
			destinationFile = new File(destinationDirectory, fileName + "(" + fileCounter + ")" + fileSuffix);
			childDestinationDirectory = new File(destinationDirectory, fileName + "(" + fileCounter + ")");
		    }
		    fileCounter++;
		}
	    }
	}

	/**
	 * Checks whether the {@link #destinationFile current destination file} already exists or has been reserved
	 *
	 * @param reservedLinks list of reserved links with each entry being an array of [original file, target file]. reservedLink[1] will
	 * be matched against
	 * @return whether the current destination file already exists or has been reserved
	 */
	private boolean destinationExistsOrIsReserved(List<URI[]> reservedLinks) {
	    final URI destinationURI = destinationFile.toURI();
	    if (destinationFile.exists()) {
		// File already exists, obvious case
		return true;
	    } else {
		// Check if any of the reserved links matches the destination
		for (URI[] link : reservedLinks) {
		    if (link[1].equals(destinationURI)) {
			return true;
		    }
		}
	    }
	    return false;
	}

	private String determineFileName(final ArbilDataNode currentNode) {
	    if (currentNode.isMetaDataNode()) {
		return currentNode.toString();
	    } else {
		String urlString = sourceURI.toString();
		try {
		    urlString = URLDecoder.decode(urlString, "UTF-8");
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(urlString, ex);
		    impExpUI.appendToTaskOutput("unable to decode the file name for: " + urlString);
		    logger.debug("unable to decode the file name for: {}", urlString);
		}
		if (urlString.endsWith("/")) {
		    // Strip off tailing slash
		    urlString = urlString.substring(0, urlString.length() - 1);
		}

		// Only use final section of path
		final int lastPathSeparatorIndex = urlString.lastIndexOf("/");
		if (lastPathSeparatorIndex >= 0) {
		    final int lastSuffixIndex = urlString.lastIndexOf(".");
		    if (lastSuffixIndex > 0 && lastSuffixIndex > lastPathSeparatorIndex) {
			// Strip off file suffix from last path section
			return urlString.substring(lastPathSeparatorIndex + 1, lastSuffixIndex);
		    } else {
			// No suffix, just take last path section
			return urlString.substring(lastPathSeparatorIndex + 1);
		    }
		} else {
		    // No separators, use entire path
		    return urlString;
		}
	    }
	}

	private String makeFileNameLamusFriendly(String fileNameString) {
	    return fileNameString
		    .replaceAll("[^A-Za-z0-9-]", "_")
		    .replaceAll("__+", "_");
	}

	public URI getSourceURI() {
	    return sourceURI;
	}

	private String normalizeFileName(String fileNameString, final boolean lamusFriendly) {
	    fileNameString = fileNameString.replace("\\", "_");
	    fileNameString = fileNameString.replace("/", "_");
	    if (lamusFriendly) {
		fileNameString = makeFileNameLamusFriendly(fileNameString);
	    }
	    if (fileNameString.length() < 1) {
		fileNameString = "unnamed";
	    }
	    return fileNameString;
	}
    }
}
