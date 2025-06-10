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
package nl.mpi.arbil.ui;

import java.awt.HeadlessException;
import java.io.File;
import java.net.URI;
import java.util.Iterator;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.CopyRunner.RetrievableFile;
import nl.mpi.arbil.data.importexport.ShibbolethNegotiator;
import nl.mpi.arbil.util.DownloadAbortFlag;
import nl.mpi.arbil.util.ProgressListener;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface ImportExportUI extends ProgressListener {

    void addToFileCopyErrors(URI uri);

    void addToMetadataCopyErrors(URI uri);

    void addToValidationErrors(URI uri);

    void appendToResourceCopyOutput(final String text);

    void appendToTaskOutput(final String lineOfText);

    void appendToXmlOutput(final String text);

    boolean askContinue(String message);

    ArbilDataNode getDestinationNode();

    DownloadAbortFlag getDownloadAbortFlag();

    File getExportDestinationDirectory();

    Iterator<ArbilDataNode> getSelectedNodesIterator();

    ShibbolethNegotiator getShibbolethNegotiator();

    boolean isCopyFilesOnExport();

    boolean isCopyFilesOnImport();

    boolean isRenameFileToLamusFriendlyName();

    boolean isRenameFileToNodeName();

    boolean isStopCopy();

    void onCopyEnd(final String finalMessage) throws HeadlessException;

    void onCopyStart();

    void removeNodeSelection();

    void setProgressIndeterminate(final boolean indeterminate);

    void setProgressText(final String text);

    void updateStatus(final int getCount, final int totalLoaded, final int totalExisting, final int totalErrors, final int xsdErrors, final int resourceCopyErrors);
    
    void setStopCopy(boolean stopCopy);
    
    void setDiskspaceState(String text);

    public boolean askOverwrite(RetrievableFile currentRetrievableFile);

    public boolean askCreateNewExportDir(File destinationFile);
}
