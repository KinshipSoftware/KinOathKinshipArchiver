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
