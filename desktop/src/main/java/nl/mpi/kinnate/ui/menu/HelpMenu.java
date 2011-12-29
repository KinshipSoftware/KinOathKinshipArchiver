package nl.mpi.kinnate.ui.menu;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersion;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.kindocument.CmdiTransformer;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;

/**
 *  Document   : HelpMenu
 *  Created on : Dec 8, 2011, 4:25:49 PM
 *  Author     : Peter Withers
 */
public class HelpMenu extends JMenu {

    public HelpMenu(AbstractDiagramManager diagramWindowManager, final BugCatcher bugCatcher, final MessageDialogHandler dialogHandler, final SessionStorage sessionStorage) {
        this.setText("Help");
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    // todo:...
//                    aboutMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    bugCatcher.logError(ex);
                }
            }
        });
        this.add(aboutMenuItem);
        JMenuItem helpMenuItem = new JMenuItem("Help");
        helpMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        helpMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    // todo: ...
//                    helpMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    bugCatcher.logError(ex);
                }
            }
        });
        this.add(helpMenuItem);
        JMenuItem arbilForumMenuItem = new JMenuItem("KinOath Forum (Website)");
        arbilForumMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    GuiHelper.getSingleInstance().openFileInExternalApplication(new URI("http://www.lat-mpi.eu/tools/kinoath/kinoath-forum/"));
                } catch (Exception ex) {
                    bugCatcher.logError(ex);
                }
            }
        });
        this.add(arbilForumMenuItem);
        final JMenuItem viewErrorLogMenuItem = new JMenuItem("View Error Log");
        viewErrorLogMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    GuiHelper.getSingleInstance().openFileInExternalApplication(new ArbilBugCatcher().getLogFile().toURI());
                } catch (Exception ex) {
                    bugCatcher.logError(ex);
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
                    bugCatcher.logError(ex);
                }
            }
        });

        JMenuItem updateKmdiProfileMenuItem = new JMenuItem("Check Component Registry Updates (this will be moved to a panel)");
        updateKmdiProfileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            // todo: move this to a panel with more options...

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    String profileId = "clarin.eu:cr1:p_1320657629627";
                    File xsdFile = new File(sessionStorage.getCacheDirectory(), "individual" + "-" + profileId + ".xsd");
                    new CmdiTransformer(sessionStorage).transformProfileXmlToXsd(xsdFile, profileId);
                } catch (IOException exception) {
                    System.out.println("exception: " + exception.getMessage());
                } catch (TransformerException exception) {
                    System.out.println("exception: " + exception.getMessage());
                }
            }
        });

        this.add(updateKmdiProfileMenuItem);
        this.addMenuListener(new MenuListener() {

            public void menuCanceled(MenuEvent evt) {
            }

            public void menuDeselected(MenuEvent evt) {
            }

            public void menuSelected(MenuEvent evt) {
                viewErrorLogMenuItem.setEnabled(ArbilBugCatcher.getLogFile().exists());
            }
        });
    }
    private static ApplicationVersionManager versionManager;

    public static void setVersionManager(ApplicationVersionManager versionManagerInstance) {
        versionManager = versionManagerInstance;
    }
}
