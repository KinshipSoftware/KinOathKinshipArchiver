package nl.mpi.kinnate.ui.menu;

import java.io.IOException;
import java.net.URI;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import nl.mpi.arbil.ArbilVersion;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersion;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.arbil.util.BugCatcherManager;
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

    public HelpMenu(final AbstractDiagramManager diagramWindowManager, final ArbilWindowManager dialogHandler, final SessionStorage sessionStorage, final ApplicationVersionManager versionManager) {
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
