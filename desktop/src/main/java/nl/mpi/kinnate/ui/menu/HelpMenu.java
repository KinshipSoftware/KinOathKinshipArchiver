/**
 * Copyright (C) 2012 The Language Archive
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
package nl.mpi.kinnate.ui.menu;

import java.awt.Component;
import java.io.IOException;
import java.net.URI;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JProgressBar;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import nl.mpi.arbil.ArbilVersion;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersion;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.ui.KinDiagramPanel;
import nl.mpi.kinnate.ui.KinOathHelp;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;
import org.xml.sax.SAXException;

/**
 * Document : HelpMenu Created on : Dec 8, 2011, 4:25:49 PM
 *
 * @author Peter Withers
 */
public class HelpMenu extends JMenu {

    JFrame helpWindow = null;

    public HelpMenu(final AbstractDiagramManager diagramWindowManager, final ArbilWindowManager dialogHandler, final SessionStorage sessionStorage, final EntityCollection entityCollection, final ApplicationVersionManager versionManager, final Component parentComponent) {
        this.setText("Help");
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // todo: move this into the window manager
                ApplicationVersion appVersion = versionManager.getApplicationVersion();
                String messageString = "KinOath is an application for  Kinship and Archiving.\n"
                        + "Designed to be flexible and culturally nonspecific, such that\n"
                        + "culturally different social structures can equally be represented.\n\n"
                        + "By linking archived data to kinship individuals, queries can be\n"
                        + "performed to retrieve the archive data based on kinship relations.\n\n"
                        + "Max Planck Institute for Psycholinguistics Nijmegen\n"
                        + "Application design and programming by Peter Withers\n"
                        + "KinOath also uses components of Arbil\n\n"
                        + "KinOath Version: " + appVersion.currentMajor + "." + appVersion.currentMinor + "." + appVersion.currentRevision + "\n"
                        + appVersion.lastCommitDate + "\n" + "Compile Date: " + appVersion.compileDate + "\n"
                        + "Arbil Version: " + new ArbilVersion().currentMajor + "." + new ArbilVersion().currentMinor + "." + new ArbilVersion().currentRevision + "\n"
                        + "Java version: " + System.getProperty("java.version") + " by " + System.getProperty("java.vendor");
                dialogHandler.addMessageDialogToQueue(messageString, "About " + versionManager.getApplicationVersion().applicationTitle);
            }
        });
        this.add(aboutMenuItem);
        JMenuItem helpMenuItem = new JMenuItem("Internal Help");
        helpMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        helpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    if (helpWindow == null) {
                        helpWindow = diagramWindowManager.createHelpWindow("Internal Help", new KinOathHelp(), null);
                    } else {
                        helpWindow.setVisible(true);
                        helpWindow.toFront();
                    }
                } catch (IOException ex) {
                    dialogHandler.addMessageDialogToQueue("Could not start the help system:\n" + ex.getMessage(), "Internal Help");
                    BugCatcherManager.getBugCatcher().logError(ex);
                } catch (SAXException ex) {
                    dialogHandler.addMessageDialogToQueue("Could not start the help system:\n" + ex.getMessage(), "Internal Help");
                    BugCatcherManager.getBugCatcher().logError(ex);
                }
            }
        });
        helpMenuItem.setEnabled(true);
        this.add(helpMenuItem);

        JMenuItem arbilWebsiteMenuItem = new JMenuItem("KinOath Website");
        arbilWebsiteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    dialogHandler.openFileInExternalApplication(new URI("http://tla.mpi.nl/tools/tla-tools/kinoath"));
                } catch (Exception ex) {
                    BugCatcherManager.getBugCatcher().logError(ex);
                }
            }
        });
        this.add(arbilWebsiteMenuItem);

        JMenuItem arbilOnlineManualMenuItem = new JMenuItem("KinOath Online Manual");
        arbilOnlineManualMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    dialogHandler.openFileInExternalApplication(new URI("http://www.mpi.nl/corpus/html/kinoath/index.html"));
                } catch (Exception ex) {
                    BugCatcherManager.getBugCatcher().logError(ex);
                }
            }
        });
        this.add(arbilOnlineManualMenuItem);

        JMenuItem arbilForumMenuItem = new JMenuItem("KinOath Forum (Website)");
        arbilForumMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    dialogHandler.openFileInExternalApplication(new URI("http://tla.mpi.nl/forums/software/kinoath"));
                } catch (Exception ex) {
                    BugCatcherManager.getBugCatcher().logError(ex);
                }
            }
        });
        this.add(arbilForumMenuItem);
        final JMenuItem viewErrorLogMenuItem = new JMenuItem("View Error Log");
        viewErrorLogMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    dialogHandler.openFileInExternalApplication(ArbilBugCatcher.getLogFile(sessionStorage, versionManager.getApplicationVersion()).toURI());
                } catch (Exception ex) {
                    BugCatcherManager.getBugCatcher().logError(ex);
                }
            }
        });
        this.add(viewErrorLogMenuItem);

        JMenuItem reindexFilesMenuItem = new JMenuItem("Reindex all files");
        reindexFilesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (dialogHandler.showConfirmDialogBox("This will reindex all files in the project,\nthis may take some time, do you want to proceed?", "Reindex All Files")) {
                    new Thread(new Runnable() {
                        public void run() {
                            SavePanel currentSavePanel = diagramWindowManager.getCurrentSavePanel(parentComponent);
                            try {
                                if (currentSavePanel instanceof KinDiagramPanel) {
                                    ((KinDiagramPanel) currentSavePanel).showProgressBar();
                                }
                                JProgressBar progressBar = new JProgressBar();
//                                progressBar.setString("reindexing all files");
//                                progressBar.setIndeterminate(true);
                                entityCollection.recreateDatabase();
                                dialogHandler.addMessageDialogToQueue("Reindexing complete.", "Reindex All Files");
                            } catch (EntityServiceException exception) {
                                dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Database Error");
                            }
                            if (currentSavePanel instanceof KinDiagramPanel) {
                                ((KinDiagramPanel) currentSavePanel).clearProgressBar();
                            }
                        }
                    }).start();
                }
            }
        });
        this.add(reindexFilesMenuItem);

        JMenuItem checkForUpdatesMenuItem = new JMenuItem("Check for Updates");
        checkForUpdatesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    if (!versionManager.forceUpdateCheck()) {
                        ApplicationVersion appVersion = versionManager.getApplicationVersion();
                        String versionString = appVersion.currentMajor + "." + appVersion.currentMinor + "." + appVersion.currentRevision;
                        dialogHandler.addMessageDialogToQueue("No updates found, current version is " + versionString, "Check for Updates");
                    }
                } catch (Exception ex) {
                    BugCatcherManager.getBugCatcher().logError(ex);
                }
            }
        });
        this.add(checkForUpdatesMenuItem);

//        JMenuItem updateKmdiProfileMenuItem = new JMenuItem("Check Component Registry Updates (this will be moved to a panel)");
//        updateKmdiProfileMenuItem.addActionListener(new java.awt.event.ActionListener() {
//            // todo: move this to a panel with more options.
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                try {
//                    String profileId = "clarin.eu:cr1:p_1320657629627";
//                    File xsdFile = new File(sessionStorage.getCacheDirectory(), "individual" + "-" + profileId + ".xsd");
//                    new CmdiTransformer(sessionStorage, bugCatcher).transformProfileXmlToXsd(xsdFile, profileId);
//                } catch (IOException exception) {
//                    System.out.println("exception: " + exception.getMessage());
//                } catch (TransformerException exception) {
//                    System.out.println("exception: " + exception.getMessage());
//                }
//            }
//        });

//        this.add(updateKmdiProfileMenuItem);
        this.addMenuListener(new MenuListener() {
            public void menuCanceled(MenuEvent evt) {
            }

            public void menuDeselected(MenuEvent evt) {
            }

            public void menuSelected(MenuEvent evt) {
                viewErrorLogMenuItem.setEnabled(ArbilBugCatcher.getLogFile(sessionStorage, versionManager.getApplicationVersion()).exists());
            }
        });
    }
}
