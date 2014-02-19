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
package nl.mpi.kinnate.ui.menu;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.ArbilVersion;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersion;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilLogConfigurer;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbilcommons.ui.LocalisationSelector;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.ui.KinDiagramPanel;
import nl.mpi.kinnate.ui.KinOathHelp;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Document : HelpMenu Created on : Dec 8, 2011, 4:25:49 PM
 *
 * @author Peter Withers
 */
public class HelpMenu extends JMenu {

    private final static Logger logger = LoggerFactory.getLogger(HelpMenu.class);
    private static final ResourceBundle menus = ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Menus");
    JFrame helpWindow = null;
    private final ArbilLogConfigurer logConfigurer;

    public HelpMenu(final AbstractDiagramManager diagramWindowManager, final ArbilWindowManager dialogHandler, final SessionStorage sessionStorage, final ApplicationVersionManager versionManager, final Component parentComponent, final ArbilLogConfigurer logConfigurer) {
        this.setText(menus.getString("HELP"));
        this.logConfigurer = logConfigurer;
        JMenuItem aboutMenuItem = new JMenuItem(menus.getString("ABOUT"));
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // todo: move this into the window manager
                ApplicationVersion appVersion = versionManager.getApplicationVersion();
                String messageString = menus.getString("KINOATH IS AN APPLICATION FOR  KINSHIP AND ARCHIVING.")
                        + java.text.MessageFormat.format(menus.getString("KINOATH VERSION: {0}.{1}.{2}"), new Object[]{appVersion.currentMajor, appVersion.currentMinor, appVersion.currentRevision})
                        + "\n"
                        + appVersion.lastCommitDate
                        + "\n"
                        + java.text.MessageFormat.format(menus.getString("COMPILE DATE: {0}"), new Object[]{appVersion.compileDate})
                        + "\n"
                        + java.text.MessageFormat.format(menus.getString("ARBIL VERSION: {0}.{1}.{2}"), new Object[]{new ArbilVersion().currentMajor, new ArbilVersion().currentMinor, new ArbilVersion().currentRevision})
                        + "\n"
                        + java.text.MessageFormat.format(menus.getString("JAVA VERSION: {0} BY {1}"), new Object[]{System.getProperty("java.version"), System.getProperty("java.vendor")});
                dialogHandler.addMessageDialogToQueue(messageString, java.text.MessageFormat.format(menus.getString("ABOUT_BOX_TITLE {0}"), new Object[]{versionManager.getApplicationVersion().applicationTitle}));
            }
        });
        this.add(aboutMenuItem);
        JMenuItem helpMenuItem = new JMenuItem(menus.getString("INTERNAL HELP"));
        helpMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        helpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    if (helpWindow == null) {
                        helpWindow = diagramWindowManager.createHelpWindow(menus.getString("INTERNAL HELP"), new KinOathHelp(), new Rectangle(600, 400));
                    } else {
                        helpWindow.setVisible(true);
                        helpWindow.toFront();
                    }
                } catch (IOException ex) {
                    dialogHandler.addMessageDialogToQueue(java.text.MessageFormat.format(menus.getString("COULD NOT START THE HELP SYSTEM:{0}"), new Object[]{ex.getMessage()}), "Internal Help");
                    BugCatcherManager.getBugCatcher().logError(ex);
                } catch (SAXException ex) {
                    dialogHandler.addMessageDialogToQueue(java.text.MessageFormat.format(menus.getString("COULD NOT START THE HELP SYSTEM:{0}"), new Object[]{ex.getMessage()}), "Internal Help");
                    BugCatcherManager.getBugCatcher().logError(ex);
                }
            }
        });
        helpMenuItem.setEnabled(true);
        this.add(helpMenuItem);
        JMenuItem arbilWebsiteMenuItem = new JMenuItem(menus.getString("KINOATH WEBSITE"));
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

        JMenuItem arbilOnlineManualMenuItem = new JMenuItem(menus.getString("KINOATH ONLINE MANUAL"));
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

        JMenuItem arbilForumMenuItem = new JMenuItem(menus.getString("KINOATH FORUM (WEBSITE)"));
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
        final JMenuItem viewErrorLogMenuItem = new JMenuItem(menus.getString("VIEW ERROR LOG"));
        viewErrorLogMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    dialogHandler.openFileInExternalApplication(logConfigurer.getLogFile(sessionStorage).toURI());
                } catch (Exception ex) {
                    BugCatcherManager.getBugCatcher().logError(ex);
                }
            }
        });
        this.add(viewErrorLogMenuItem);

        JMenuItem reindexFilesMenuItem = new JMenuItem(menus.getString("REINDEX ALL FILES FOR THIS PROJECT"));
        reindexFilesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (dialogHandler.showConfirmDialogBox(menus.getString("THIS WILL REINDEX ALL FILES IN THE PROJECT, THIS MAY TAKE SOME TIME, DO YOU WANT TO PROCEED?"), menus.getString("REINDEX ALL FILES"))) {
                    new Thread(new Runnable() {
                        public void run() {
                            SavePanel currentSavePanel = diagramWindowManager.getCurrentSavePanel(parentComponent);
                            try {
                                if (currentSavePanel instanceof KinDiagramPanel) {
                                    final KinDiagramPanel diagramPanel = (KinDiagramPanel) currentSavePanel;
                                    diagramPanel.showProgressBar();
                                    diagramPanel.getEntityCollection().recreateDatabase();
                                }
//                                JProgressBar progressBar = new JProgressBar();
//                                progressBar.setString("reindexing all files");
//                                progressBar.setIndeterminate(true);
                                dialogHandler.addMessageDialogToQueue(menus.getString("REINDEXING COMPLETE."), menus.getString("REINDEX ALL FILES"));
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

        JMenuItem checkForUpdatesMenuItem = new JMenuItem(menus.getString("CHECK FOR UPDATES"));
        checkForUpdatesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    if (!versionManager.forceUpdateCheck()) {
                        ApplicationVersion appVersion = versionManager.getApplicationVersion();
                        String versionString = appVersion.currentMajor + "." + appVersion.currentMinor + "." + appVersion.currentRevision;
                        dialogHandler.addMessageDialogToQueue(java.text.MessageFormat.format(menus.getString("NO UPDATES FOUND, CURRENT VERSION IS {0}"), new Object[]{versionString}), menus.getString("CHECK FOR UPDATES"));
                    }
                } catch (Exception ex) {
                    BugCatcherManager.getBugCatcher().logError(ex);
                }
            }
        });
        this.add(checkForUpdatesMenuItem);

        JMenuItem selectLanguageMenuItem = new JMenuItem();
        selectLanguageMenuItem.setText(menus.getString("SELECT LANGUAGE"));
        selectLanguageMenuItem.addActionListener(
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    final String availableLanguages = ResourceBundle.getBundle("nl/mpi/arbil/localisation/AvailableLanguages").getString("LANGUAGE CODES");
                    final LocalisationSelector localisationSelector = new LocalisationSelector(sessionStorage, availableLanguages.split(","));
                    final String please_select_your_preferred_language = menus.getString("PLEASE SELECT YOUR PREFERRED LANGUAGE");
                    final String language_Selection = menus.getString("LANGUAGE SELECTION");
                    final String system_Default = menus.getString("SYSTEM DEFAULT");
                    Window windowAncestor = SwingUtilities.getWindowAncestor(parentComponent);
                    final JFrame parentFrame = (windowAncestor instanceof JFrame) ? (JFrame) windowAncestor : null;
                    localisationSelector.askUser(parentFrame, ArbilIcons.getSingleInstance().linorgIcon, please_select_your_preferred_language, language_Selection, system_Default);
                    localisationSelector.setLanguageFromSaved();
                    dialogHandler.addMessageDialogToQueue(menus.getString("PLEASE RESTART THE APPLICATION FOR THE LANGUAGE SELECTION TO BECOME VISIBLE"), menus.getString("SELECT LANGUAGE"));
                } catch (Exception ex) {
                    BugCatcherManager.getBugCatcher().logError(ex);
                }
            }
        });
        this.add(selectLanguageMenuItem);

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
                viewErrorLogMenuItem.setEnabled(logConfigurer.getLogFile(sessionStorage).exists());
            }
        });
    }
}
