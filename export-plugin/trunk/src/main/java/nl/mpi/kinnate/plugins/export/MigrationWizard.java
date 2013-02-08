package nl.mpi.kinnate.plugins.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.ProgressMonitor;
import nl.mpi.flap.kinnate.entityindexer.QueryException;
import nl.mpi.flap.plugin.PluginBugCatcher;
import nl.mpi.flap.plugin.PluginDialogHandler;
import nl.mpi.flap.plugin.PluginException;
import nl.mpi.flap.plugin.PluginSessionStorage;
import nl.mpi.kinnate.entityindexer.CollectionExport;

/**
 * Created on : Nov 9, 2012, 1:40:55 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class MigrationWizard {

    final PluginBugCatcher bugCatcher;
    final PluginDialogHandler dialogHandler;
    final PluginSessionStorage sessionStorage;

    public MigrationWizard(PluginBugCatcher bugCatcher, PluginDialogHandler dialogHandler, PluginSessionStorage sessionStorage) {
        this.bugCatcher = bugCatcher;
        this.dialogHandler = dialogHandler;
        this.sessionStorage = sessionStorage;
    }

    public File checkAndOfferMigration() {
        // look for an old version of the application directory
        File oldAppDir = new File(sessionStorage.getApplicationSettingsDirectory().getParentFile(), ".kinoath-1-0");
        File oldAppExportFile = new File(oldAppDir, "MigrationWizard.kinoath");
        // look for a new version of the application directory
        File newAppDir = sessionStorage.getProjectWorkingDirectory();
        // if the old exists and the new does not or is empty then offer migration 
        if (oldAppDir.exists() && (!newAppDir.exists() || newAppDir.list().length < 3)) {
            if (dialogHandler.showConfirmDialogBox("This is a new version of KinOath.\nWould you like to import the data from the last version?\n", "Migration Wizard")) {
                if (!oldAppExportFile.exists()) {
                    // create export file
                    createDatabase(oldAppDir, oldAppExportFile);
                }
                // return the export file
                return oldAppExportFile;
            } else {
                dialogHandler.addMessageDialogToQueue("If you change you mind, you can migrate your data from the old version manually\nvia the plugins menu and 'single file export' followed by an import.", "Migration Wizard");
                return null;
            }
        } else {
            return null;
        }
    }

    private void createDatabase(final File importDirectory, final File exportFile) {
        ProgressMonitor pogressMonitor = new ProgressMonitor(null, "Migration Wizard", "cteating temporary database", 0, 5);
        pogressMonitor.setMillisToDecideToPopup(0);
        pogressMonitor.setMillisToPopup(0);
        pogressMonitor.setProgress(1);
//        new Thread() {
//            @Override
//            public void run() {
        final CollectionExport entityCollection = new CollectionExport(bugCatcher, sessionStorage);
        try {
            final GedcomExport gedcomExport = new GedcomExport(entityCollection);
            gedcomExport.dropAndCreate(importDirectory, "*.kmdi");
//                    dialogHandler.append("Completed cteating temporary database\n");
            //                    resultsText.setText("Generating export contents.\n");
            pogressMonitor.setNote("generating export file.");
            pogressMonitor.setProgress(2);
            if (pogressMonitor.isCanceled()) {
                return;
            }
            final String generateExportResult = gedcomExport.generateExport(gedcomExport.getGedcomQuery());
//                    resultsText.setText("Creating export file: " + saveFile.toString() + "\n");
            pogressMonitor.setNote("saving export file.");
            pogressMonitor.setProgress(3);
            if (pogressMonitor.isCanceled()) {
                return;
            }
            FileWriter fileWriter = new FileWriter(exportFile);
            fileWriter.write(generateExportResult);
            fileWriter.close();
            pogressMonitor.setNote("save complete.");
            pogressMonitor.setProgress(4);
            if (pogressMonitor.isCanceled()) {
                return;
            }
        } catch (IOException exception) {
//                    resultsText.setText("Error Saving File.\n");
            dialogHandler.addMessageDialogToQueue("Error Saving File\n" + exception.getMessage(), "Migration Wizard");
            bugCatcher.logException(new PluginException(exception.getMessage()));
        } catch (QueryException exception) {
//                    resultsText.setText("Error Creating Export.\n");
            dialogHandler.addMessageDialogToQueue("Error Creating Export\n" + exception.getMessage(), "Migration Wizard");
            bugCatcher.logException(new PluginException(exception.getMessage()));
        }
        pogressMonitor.setProgress(5);
//            }
//        }.start();
    }
}
