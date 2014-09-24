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
package nl.mpi.kinnate.ui;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.ArbilMain;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersion;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilLogConfigurer;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbilcommons.ui.LocalisationSelector;
import nl.mpi.kinnate.KinOathVersion;
import nl.mpi.kinnate.KinnateArbilInjector;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.plugins.export.MigrationWizard;
import nl.mpi.kinnate.projects.ProjectManager;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;
import nl.mpi.kinnate.ui.window.WindowedDiagramManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Document : MainFrame
 * Author : Peter Withers
 * Created on : Aug 16, 2010, 5:20:20 PM
 */
public class MainFrame extends javax.swing.JFrame {

    private static final ResourceBundle menus = ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Menus");
    private final static Logger logger = LoggerFactory.getLogger(MainFrame.class);

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        final ApplicationVersionManager versionManager = new ApplicationVersionManager(new KinOathVersion());
        final ArbilLogConfigurer logConfigurer = new ArbilLogConfigurer(versionManager.getApplicationVersion(), "kinoath-log-");
        // See if a logging configuration has been specified manually
        if (System.getProperty("java.util.logging.config.file") == null) {
            // No logging configured, use built in initial logging properties
            logConfigurer.configureLoggingFromResource(ArbilMain.class, "/logging-initial.properties");
        }
        logger.info("Starting KinOath");
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                final KinnateArbilInjector injector = new KinnateArbilInjector();
                injector.injectHandlers(versionManager);
                final AbstractDiagramManager abstractDiagramManager;
                final ArbilWindowManager windowManager = injector.getWindowManager();
                final SessionStorage sessionStorage = injector.getSessionStorage();
                final ProjectManager projectManager = new ProjectManager(sessionStorage, windowManager);

                final String availableLanguages = ResourceBundle.getBundle("nl/mpi/kinoath/localisation/AvailableLanguages").getString("LANGUAGE CODES");
                final LocalisationSelector localisationSelector = new LocalisationSelector(sessionStorage, availableLanguages.split(","));
                if (!localisationSelector.hasSavedLocal()) {
                    final String please_select_your_preferred_language = menus.getString("PLEASE SELECT YOUR PREFERRED LANGUAGE");
                    final String language_Selection = menus.getString("LANGUAGE SELECTION");
                    final String system_Default = menus.getString("SYSTEM DEFAULT");
                    localisationSelector.askUser(null, ArbilIcons.getSingleInstance().linorgIcon, please_select_your_preferred_language, language_Selection, system_Default);
                }
                localisationSelector.setLanguageFromSaved();

//                abstractDiagramManager = new LayeredDiagramManager(versionManager);
//                abstractDiagramManager = new TabbedDiagramManager(versionManager);
                abstractDiagramManager = new WindowedDiagramManager(versionManager, windowManager, sessionStorage, injector.getDataNodeLoader(), injector.getTreeHelper(), projectManager, logConfigurer, injector.getArbilTableController(), injector.getWindowManager());
                try {
                    boolean databaseDirectoryOk = false;
                    while (!databaseDirectoryOk) {
                        try {
                            final String storedDatabasePath = sessionStorage.loadString("KinOathDatabaseDirectory");
                            File databaseDirectory;
                            if (storedDatabasePath == null) {
                                databaseDirectory = sessionStorage.getApplicationSettingsDirectory();
                            } else {
                                databaseDirectory = new File(storedDatabasePath);
                            }
                            final File globalDatabaseDirectory = new File(databaseDirectory, "BaseXData");
                            if (!globalDatabaseDirectory.exists()) {
                                globalDatabaseDirectory.mkdir();
                            }
                            new File(globalDatabaseDirectory, ".basexhome").createNewFile();
                            Properties props = System.getProperties();
                            props.setProperty("org.basex.path", globalDatabaseDirectory.getAbsolutePath());
                            EntityCollection.testGlobalDatabasePath(databaseDirectory);
                            databaseDirectoryOk = true;
                        } catch (EntityServiceException entityServiceException) {
                            final String kinOath_Database_Directory = "KinOath Database Directory";
                            if (!windowManager.showConfirmDialogBox("The KinOath database could not be created. Do you want to choose another location?", kinOath_Database_Directory)) {
                                System.exit(-1);
                            }
                            final File[] databaseDirectorySelection = windowManager.showDirectorySelectBox(kinOath_Database_Directory, false);
                            if (databaseDirectorySelection == null || databaseDirectorySelection.length == 0) {
                                System.exit(-1);
                            }
                            sessionStorage.saveString("KinOathDatabaseDirectory", databaseDirectorySelection[0].toString());
                        } catch (IOException exception2) {
                            logger.warn("Could not create the basexhome file: " + exception2.getMessage());
                        }
                    }
                    final KinDiagramPanel initialDiagram = abstractDiagramManager.newDiagram(new Rectangle(0, 0, 640, 480), null);
                    abstractDiagramManager.createApplicationWindow();

                    windowManager.setMessagesCanBeShown(true);
                    ////////////////////////////////////////
                    // check for old data directories 1-0 and offer the user to export all the old data and import into the new version IF no entities exist in the new version, the user can always use the export plugin at a later date
                    // start handle any migration requirements
//                    new Thread() {
//                        @Override
//                        public void run() {
//                            final ApplicationVersion applicationVersion = versionManager.getApplicationVersion();
//                            File oldAppExportFile = new MigrationWizard(BugCatcherManager.getBugCatcher(), windowManager, sessionStorage).checkAndOfferMigration(Integer.parseInt(applicationVersion.currentMajor), Integer.parseInt(applicationVersion.currentMinor));
//                            if (oldAppExportFile != null) {
//                                try {
//                                    abstractDiagramManager.openImportPanel(oldAppExportFile, initialDiagram, projectManager.getEntityCollectionForProject(projectManager.getDefaultProject(sessionStorage)));
//                                } catch (ImportException exception) {
//                                    windowManager.addMessageDialogToQueue(exception.getMessage() + "\n" + oldAppExportFile.getAbsolutePath(), "Import Existing Data");
//                                } catch (EntityServiceException exception) {
//                                    windowManager.addMessageDialogToQueue(exception.getMessage() + "\n" + oldAppExportFile.getAbsolutePath(), "Import Existing Data");
//                                }
//                            }
//                        }
//                    }.start();
                    // end handle any migration requirements
                    ////////////////////////////////////////
                } catch (EntityServiceException entityServiceException) {
                    System.out.println(entityServiceException.getMessage());
                    JOptionPane.showMessageDialog(null, "Failed to create a new diagram: " + entityServiceException.getMessage(), "Launch Diagram Error", JOptionPane.PLAIN_MESSAGE);
                    System.exit(-1);
                }
//	if (arbilMenuBar.checkNewVersionAtStartCheckBoxMenuItem.isSelected()) {
                // todo: Ticket #1066 add the check for updates and check now menu items
                versionManager.checkForUpdate();
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
