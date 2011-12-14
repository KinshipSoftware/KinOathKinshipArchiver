package nl.mpi.kinnate.export;

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.kinnate.KinTermSavePanel;
import nl.mpi.kinnate.userstorage.KinSessionStorage;

/**
 *  Document   : ExportToR
 *  Created on : May 30, 2011, 2:30:34 PM
 *  Author     : Peter Withers
 */
public class ExportToR {

    public void doExport(Component mainFrame, KinTermSavePanel savePanel) {
        // todo: modify this to use the ArbilWindowManager and update the ArbilWindowManager file select to support save file actions
        JFileChooser fc = new JFileChooser();
        String lastSavedFileString = KinSessionStorage.getSingleInstance().loadString("kinoath.ExportToR");
        if (lastSavedFileString != null) {
            fc.setSelectedFile(new File(lastSavedFileString));
        }
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
            PedigreePackageExport packageExport = new PedigreePackageExport();
            KinSessionStorage.getSingleInstance().saveString("kinoath.ExportToR", file.getPath());
            try {
                FileWriter fileWriter = new FileWriter(file, false);
                fileWriter.write(packageExport.createCsvContents(savePanel.getGraphEntities()));
                fileWriter.close();
//                ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("File saved", "Export");
            } catch (IOException exception) {
                ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Error, could not save file", "Export");
                GuiHelper.linorgBugCatcher.logError(exception);
            }
        }
    }
    // example usage:
//    install.packages()
//    pid	id	momid	dadid	sex	affected
//    24	1	0	0	1	1
//    24	2	0	0	2	1
//    24	3	1	2	1	2
//    24	4	0	0	2	2
//    24	5	3	4	1	3
//
//    dataFrame <- read.table("https://192.168.1.24/gallery/kinship-r.csv",header=T)
//    # url not required: dataFrame <- read.table(url("http://192.168.1.24/gallery/kinship-r.csv"),header=T)
//    dataFrame <- read.table("~/kinship-r.csv",header=T)
//    library(kinship)
//    attach(dataFrame)
//    pedigreeObj <- pedigree(id,momid,dadid,sex, affected)
//    plot(pedigreeObj)
/////////////////////////////////////////////////////////////////
//  Suggestion from Dan on R package to look into in detail
//    http://statnet.org/
//     http://www.jstatsoft.org/v24/i06/
//    http://www.jstatsoft.org/v24/i09
/////////////////////////////////////////////////////////////////
}
