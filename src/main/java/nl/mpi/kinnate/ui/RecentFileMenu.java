package nl.mpi.kinnate.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;

/**
 *  Document   : RecentFileMenu
 *  Created on : Apr 15, 2011, 11:30:39 AM
 *  Author     : Peter Withers
 */
public class RecentFileMenu extends JMenu implements ActionListener {

    MainFrame mainFrame;

    public RecentFileMenu(MainFrame mainFrameLocal) {
        mainFrame = mainFrameLocal;
        setupMenu();
    }

    public void addRecentFile(String recentFile) {
        // store the accessed and saved files and provide a menu of recent files
        ArrayList<String> tempList = new ArrayList<String>();
        String[] tempArray = ArbilSessionStorage.getSingleInstance().loadStringArray("RecentKinFiles");
        if (tempArray != null) {
            tempList.addAll(Arrays.asList(tempArray));
        }
        // todo: restrict the recent file list to x number but make sure only the oldest gets removed
        // todo: make sure the list is kept in order
        tempList.remove(recentFile);
        tempList.add(recentFile);
        ArbilSessionStorage.getSingleInstance().saveStringArray("RecentKinFiles", tempList.toArray(new String[]{}));
        setupMenu();
    }

    private void setupMenu() {
        this.removeAll();
        String[] recentFileArray = ArbilSessionStorage.getSingleInstance().loadStringArray("RecentKinFiles");
        if (recentFileArray != null) {
            for (int currentIndex = recentFileArray.length - 1; currentIndex >= 0; currentIndex--) {
                String currentFilePath = recentFileArray[currentIndex];
                JMenuItem currentMenuItem = new JMenuItem(currentFilePath);
                currentMenuItem.setActionCommand(currentFilePath);
                currentMenuItem.addActionListener(this);
                this.add(currentMenuItem);
            }
            this.add(new JSeparator());
            JMenuItem clearMenuItem = new JMenuItem("Clear List");
            clearMenuItem.setActionCommand("Clear List");
            clearMenuItem.addActionListener(this);
            this.add(clearMenuItem);
        } else {
            JMenuItem currentMenuItem = new JMenuItem("no recent files");
            currentMenuItem.setEnabled(false);
            this.add(currentMenuItem);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if ("Clear List".equals(e.getActionCommand())) {
            ArbilSessionStorage.getSingleInstance().saveStringArray("RecentKinFiles", new String[]{});
            setupMenu();
        } else {
            mainFrame.openDiagram(new File(e.getActionCommand()));
        }
    }
}
