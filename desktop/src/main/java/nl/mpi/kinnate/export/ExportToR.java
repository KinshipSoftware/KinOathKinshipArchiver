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
package nl.mpi.kinnate.export;

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.kindata.EntityData;

/**
 * Document : ExportToR Created on : May 30, 2011, 2:30:34 PM
 *
 * @author Peter Withers
 */
public class ExportToR {

    private SessionStorage sessionStorage;
    private MessageDialogHandler dialogHandler;

    public ExportToR(SessionStorage sessionStorage, MessageDialogHandler dialogHandler) {
        this.sessionStorage = sessionStorage;
        this.dialogHandler = dialogHandler;
    }

    public void doExport(Component mainFrame, SavePanel savePanel, File destinationFile) {
        PedigreePackageExport packageExport = new PedigreePackageExport(savePanel.getGraphPanel().dataStoreSvg.defaultSymbol);
        try {
            FileWriter fileWriter = new FileWriter(destinationFile, false);
            final EntityData[] dataNodes = savePanel.getGraphPanel().getSVGDocument().graphData.getDataNodes();
            ArrayList<EntityData> visibleEntities = new ArrayList<EntityData>();
            for (EntityData currentEntity : dataNodes) {
                if (currentEntity.isVisible) {
                    visibleEntities.add(currentEntity);
                }
            }
            fileWriter.write(packageExport.createCsvContents(visibleEntities.toArray(new EntityData[]{})));
            fileWriter.close();
            dialogHandler.addMessageDialogToQueue("Exported " + visibleEntities.size() + " entities", "Export");
//                ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("File saved", "Export");
        } catch (IOException exception) {
            dialogHandler.addMessageDialogToQueue("Error, could not export the data to file", "Export");
            BugCatcherManager.getBugCatcher().logError(exception);
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
