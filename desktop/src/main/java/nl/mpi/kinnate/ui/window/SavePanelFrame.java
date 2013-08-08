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
package nl.mpi.kinnate.ui.window;

import java.awt.HeadlessException;
import java.io.File;
import javax.swing.JFrame;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.svg.MouseListenerSvg.ActionCode;

/**
 * Created on : Dec 7, 2011, 2:10:09 PM
 *
 * @author Peter Withers
 */
public class SavePanelFrame extends JFrame implements SavePanel {

    SavePanel savePanel;

    public SavePanelFrame(SavePanel savePanel) throws HeadlessException {
        this.savePanel = savePanel;
    }

    public void doActionCommand(ActionCode actionCode) {
        savePanel.doActionCommand(actionCode);
    }

    public File getFileName() {
        return savePanel.getFileName();
    }

    public boolean hasSaveFileName() {
        return savePanel.hasSaveFileName();
    }

    public boolean requiresSave() {
        return savePanel.requiresSave();
    }

    public void saveToFile() {
        savePanel.saveToFile();
    }

    public void saveToFile(File saveFile) {
        savePanel.saveToFile(saveFile);
    }

    public void setRequiresSave() {
        savePanel.setRequiresSave();
    }

    public void updateGraph() {
        savePanel.updateGraph();
    }

    public GraphPanel getGraphPanel() {
        return savePanel.getGraphPanel();
    }

    public void showSettings() {
        savePanel.showSettings();
    }
}
