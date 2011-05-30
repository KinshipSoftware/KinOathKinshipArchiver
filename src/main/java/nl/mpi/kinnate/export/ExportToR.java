package nl.mpi.kinnate.export;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.ui.MainFrame;

/**
 *  Document   : ExportToR
 *  Created on : May 30, 2011, 2:30:34 PM
 *  Author     : Peter Withers
 */
public class ExportToR {

    public void doExport(MainFrame mainFrame, SavePanel savePanel) {
        JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new FileFilter() {

            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                return (file.getName().toLowerCase().endsWith(".csv"));
            }

            @Override
            public String getDescription() {
                return "Data Frame (CSV)";
            }
        });

        int returnVal = fc.showSaveDialog(mainFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
//            int tabIndex = Integer.valueOf(evt.getActionCommand());
//            savePanel.saveToFile(file);
        } else {
            // todo: warn user that no file selected and so cannot save
        }
    }
}
