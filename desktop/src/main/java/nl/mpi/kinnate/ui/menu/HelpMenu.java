package nl.mpi.kinnate.ui.menu;

import java.net.URI;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.util.ApplicationVersion;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilBugCatcher;

/**
 *  Document   : HelpMenu
 *  Created on : Dec 8, 2011, 4:25:49 PM
 *  Author     : Peter Withers
 */
public class HelpMenu extends JMenu {

    private HelpMenu() {
        this.setText("Help");
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
//                    aboutMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        this.add(aboutMenuItem);
        JMenuItem helpMenuItem = new JMenuItem("Help");
        helpMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        helpMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
//                    helpMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        this.add(helpMenuItem);
        JMenuItem arbilForumMenuItem = new JMenuItem("KinOath Forum (Website)");
        arbilForumMenuItem.setText("Arbil Forum (Website)");
        arbilForumMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    GuiHelper.getSingleInstance().openFileInExternalApplication(new URI("http://www.lat-mpi.eu/tools/arbil/kinoath-forum/"));
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
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
                    GuiHelper.linorgBugCatcher.logError(ex);
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
                        ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("No updates found, current version is " + versionString, "Check for Updates");
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        this.add(checkForUpdatesMenuItem);
        this.addMenuListener(new MenuListener() {

            public void menuCanceled(MenuEvent evt) {
            }

            public void menuDeselected(MenuEvent evt) {
            }

            public void menuSelected(MenuEvent evt) {
                viewErrorLogMenuItem.setEnabled(new ArbilBugCatcher().getLogFile().exists());
            }
        });
    }
    private static ApplicationVersionManager versionManager;

    public static void setVersionManager(ApplicationVersionManager versionManagerInstance) {
        versionManager = versionManagerInstance;
    }
}
