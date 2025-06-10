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

import java.net.URI;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.ProgressMonitor;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.ui.AbstractArbilTableModel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface WindowManager {

    JFrame getMainFrame();

    void closeAllWindows();

    void openFloatingTable(ArbilDataNode[] rowNodesArray, String frameTitle);

    void openFloatingTableOnce(URI[] rowNodesArray, String frameTitle);

    void openFloatingTableOnce(ArbilDataNode[] rowNodesArray, String frameTitle);

    void openSearchTable(ArbilNode[] selectedNodes, String frameTitle);

    void openFloatingSubnodesWindows(ArbilDataNode[] arbilDataNodes);

    void openUrlWindowOnce(String frameTitle, URL locationUrl);

    ProgressMonitor newProgressMonitor(Object message, String note, int min, int max);

    /* Methods below are implemented by ArbilWindowManager and may be relevant
     * but currently are not used through the interface so they are not included
     * so as not to put any additional burden on potential other implementers
     */
    //JInternalFrame createWindow(String windowTitle, Component contentsComponent);
    //JInternalFrame focusWindow(String windowName);
    AbstractArbilTableModel openFloatingTableOnceGetModel(URI[] rowNodesArray, String frameTitle);

    AbstractArbilTableModel openAllChildNodesInFloatingTableOnce(URI[] rowNodesArray, String frameTitle);

    void saveWindowStates();
    //void stopEditingInCurrentWindow();

    boolean openFileInExternalApplication(URI targetUri);

    void openImdiXmlWindow(Object userObject, boolean formatXml, boolean launchInBrowser);
}
