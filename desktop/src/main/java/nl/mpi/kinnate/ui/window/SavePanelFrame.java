package nl.mpi.kinnate.ui.window;

import java.awt.HeadlessException;
import java.io.File;
import javax.swing.JFrame;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.svg.MouseListenerSvg.ActionCode;

/**
 *  Document   : SavePanelFrame
 *  Created on : Dec 7, 2011, 2:10:09 PM
 *  Author     : Peter Withers
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
}
