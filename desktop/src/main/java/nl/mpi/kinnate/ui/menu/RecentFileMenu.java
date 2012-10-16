package nl.mpi.kinnate.ui.menu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;

/**
 * Document : RecentFileMenu Created on : Apr 15, 2011, 11:30:39 AM
 *
 * @author Peter Withers
 */
public class RecentFileMenu extends JMenu implements ActionListener {

    private final AbstractDiagramManager diagramWindowManager;
    private final SessionStorage sessionStorage;
    private final Component parentComponent;

    public RecentFileMenu(AbstractDiagramManager diagramWindowManager, SessionStorage sessionStorage, Component parentComponent) {
        this.diagramWindowManager = diagramWindowManager;
        this.sessionStorage = sessionStorage;
        this.parentComponent = parentComponent;
        this.setText("Open Recent Diagram");
        this.addMenuListener(new MenuListener() {
            public void menuCanceled(MenuEvent evt) {
            }

            public void menuDeselected(MenuEvent evt) {
            }

            public void menuSelected(MenuEvent evt) {
                setupMenu();
            }
        });
    }

    static public void addRecentFile(SessionStorage sessionStorageS, File recentFile) {
        // store the accessed and saved files and provide a menu of recent files
        ArrayList<String> tempList = new ArrayList<String>();
        String[] tempArray;
        try {
            tempArray = sessionStorageS.loadStringArray("RecentKinFiles");
            if (tempArray != null) {
                tempList.addAll(Arrays.asList(tempArray));
            }
            // restrict the recent file list to x number but make sure only the oldest gets removed
            while (tempList.size() > 10) {
                tempList.remove(0);
            }
            // todo: make sure the list is kept in order
            tempList.remove(recentFile.toString());
            tempList.add(recentFile.toString());
        } catch (IOException exception) {
//            BugCatcherManager.getBugCatcher().logError(exception);
            tempArray = new String[]{recentFile.toString()};
        }
        try {
            sessionStorageS.saveStringArray("RecentKinFiles", tempList.toArray(new String[]{}));
        } catch (IOException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        }
//        setupMenu();
    }

    private void setupMenu() {
        this.removeAll();
        try {
            String[] recentFileArray = sessionStorage.loadStringArray("RecentKinFiles");
            if (recentFileArray != null) {
                for (int currentIndex = recentFileArray.length - 1; currentIndex >= 0; currentIndex--) {
                    String currentFilePath = recentFileArray[currentIndex];
                    JMenuItem currentMenuItem = new JMenuItem(currentFilePath);
                    currentMenuItem.setActionCommand(currentFilePath);
                    currentMenuItem.addActionListener(this);
                    this.add(currentMenuItem);
                }
            }
            this.add(new JSeparator());
            JMenuItem clearMenuItem = new JMenuItem("Clear List");
            clearMenuItem.setActionCommand("Clear List");
            clearMenuItem.addActionListener(this);
            this.add(clearMenuItem);
        } catch (IOException exception) {
            JMenuItem currentMenuItem = new JMenuItem("no recent files");
            currentMenuItem.setEnabled(false);
            this.add(currentMenuItem);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if ("Clear List".equals(e.getActionCommand())) {
            try {
                sessionStorage.saveStringArray("RecentKinFiles", new String[]{});
            } catch (IOException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
            }
//            setupMenu();
        } else {
//            try {
            final String actionString = e.getActionCommand();
            final File recentFile = new File(actionString);
//                final int startIndex = actionString.lastIndexOf('/');
//                final String recentName = actionString.substring(startIndex + 1);
            final String recentName = recentFile.getName();
            final Dimension parentSize = parentComponent.getSize();
            final Point parentLocation = parentComponent.getLocation();
            int offset = 10;
            final Rectangle windowRectangle = new Rectangle(parentLocation.x + offset, parentLocation.y + offset, parentSize.width - offset, parentSize.height - offset);
            diagramWindowManager.openDiagram(recentName, recentFile.toURI(), true, windowRectangle);
//            } catch (URISyntaxException exception) {
//                bugCatcher.logError(exception);
//                ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Failed to load sample", "Sample Diagram");
//            }
        }
    }
}
