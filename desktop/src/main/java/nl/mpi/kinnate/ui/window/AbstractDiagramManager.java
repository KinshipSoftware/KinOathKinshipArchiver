package nl.mpi.kinnate.ui.window;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.kinnate.KinTermSavePanel;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.ui.EntityUploadPanel;
import nl.mpi.kinnate.ui.GedcomImportPanel;
import nl.mpi.kinnate.ui.KinDiagramPanel;
import nl.mpi.kinnate.ui.menu.DocumentNewMenu;
import nl.mpi.kinnate.ui.menu.MainMenuBar;
import nl.mpi.kinnate.ui.menu.RecentFileMenu;

/**
 * Document : AbstractDiagramManager Created on : Dec 6, 2011, 12:28:56 PM
 *
 * @author Peter Withers
 */
public abstract class AbstractDiagramManager {

    private EntityUploadPanel entityUploadPanel;
    private ApplicationVersionManager versionManager;
    private ArbilWindowManager dialogHandler;
    private SessionStorage sessionStorage;
    private ArbilDataNodeLoader dataNodeLoader;
    private ArbilTreeHelper treeHelper;
    private EntityCollection entityCollection;

    public AbstractDiagramManager(ApplicationVersionManager versionManager, ArbilWindowManager dialogHandler, SessionStorage sessionStorage, ArbilDataNodeLoader dataNodeLoader, ArbilTreeHelper treeHelper, EntityCollection entityCollection) {
        this.versionManager = versionManager;
        this.dialogHandler = dialogHandler;
        this.sessionStorage = sessionStorage;
        this.dataNodeLoader = dataNodeLoader;
        this.treeHelper = treeHelper;
        this.entityCollection = entityCollection;
    }

    abstract public void createApplicationWindow();

    public JFrame createDiagramWindow(String diagramTitle, Component diagramComponent, Rectangle preferredSizeLocation) {
        JFrame diagramFame;
        if (diagramComponent instanceof SavePanel) {
            diagramFame = new SavePanelFrame((SavePanel) diagramComponent);
        } else {
            diagramFame = new JFrame();
        }
        setWindowTitle(diagramFame, diagramTitle);
        diagramFame.setJMenuBar(new MainMenuBar(this, sessionStorage, dialogHandler, versionManager, diagramFame));
        if (diagramComponent != null) {
            diagramFame.setContentPane((Container) diagramComponent);
//        } else {
//            diagramFame.setMaximumSize(new Dimension(800, 600));
        }

        setWindowIcon(diagramFame);
        diagramFame.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        diagramFame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeWindowAction((JFrame) e.getWindow());
            }
        });
        if (preferredSizeLocation != null) {
            diagramFame.setLocation(preferredSizeLocation.x, preferredSizeLocation.y);
            diagramFame.setPreferredSize(preferredSizeLocation.getSize());
        }
//        diagramFame.doLayout();
        diagramFame.pack();
        diagramFame.setVisible(true);
        return diagramFame;
    }

    public JFrame createHelpWindow(String diagramTitle, Component diagramComponent, Rectangle preferredSizeLocation) {
        JFrame diagramFame;
        if (diagramComponent instanceof SavePanel) {
            diagramFame = new SavePanelFrame((SavePanel) diagramComponent);
        } else {
            diagramFame = new JFrame();
        }
        setWindowTitle(diagramFame, diagramTitle);
        if (diagramComponent != null) {
            diagramFame.setContentPane((Container) diagramComponent);
        }
        setWindowIcon(diagramFame);
        diagramFame.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        diagramFame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ((JFrame) e.getWindow()).setVisible(false);
            }
        });
        if (preferredSizeLocation != null) {
            diagramFame.setLocation(preferredSizeLocation.x, preferredSizeLocation.y);
            diagramFame.setPreferredSize(preferredSizeLocation.getSize());
        }
        diagramFame.pack();
        diagramFame.setVisible(true);
        return diagramFame;
    }

    public void setWindowTitle(JFrame windowFrame, String titleString) {
        windowFrame.setTitle(versionManager.getApplicationVersion().applicationTitle + " " + versionManager.getApplicationVersion().currentMajor + "." + versionManager.getApplicationVersion().currentMinor + "." + versionManager.getApplicationVersion().currentRevision + " - " + titleString);
    }

    public void setWindowIcon(JFrame windowFrame) {
        // set the icon for the application (if this is still required for the various OSs). This is not required for Mac but might be needed for windows or linux.
        windowFrame.setIconImage(ArbilIcons.getSingleInstance().linorgIcon.getImage());
    }

    protected void closeWindowAction(JFrame windowFrame) {
        // check that all diagrams are saved and ask user if not
        if (offerUserToSaveAll()) {
            System.exit(0);
        }
    }

    abstract public Component createDiagramContainer(Component diagramComponent, Rectangle preferredSizeLocation);

    public JDialog createDialogueContainer(Component diagramComponent, Component parentComponent) {
        String diagramTitle = diagramComponent.getName();
        JFrame parentFrame = (JFrame) SwingUtilities.getRoot(parentComponent);
        JDialog jDialog = new JDialog(parentFrame, diagramTitle, true);
        jDialog.getContentPane().removeAll();
        jDialog.getContentPane().add(diagramComponent);
        jDialog.pack();
        return jDialog;
    }

    abstract public void createDiagramSubPanel(String diagramTitle, Component diagramComponent, Component parentPanel);

    public void newDiagram(Rectangle preferredSizeLocation) {
        URI defaultDiagramUri = null;
        if (KinDiagramPanel.getDefaultDiagramFile(sessionStorage).exists()) {
            defaultDiagramUri = KinDiagramPanel.getDefaultDiagramFile(sessionStorage).toURI();
        }
        KinDiagramPanel egoSelectionTestPanel = new KinDiagramPanel(defaultDiagramUri, false, sessionStorage, dialogHandler, dataNodeLoader, treeHelper, entityCollection, this);
        egoSelectionTestPanel.setName("Unsaved Default Diagram");
        createDiagramContainer(egoSelectionTestPanel, preferredSizeLocation);
        egoSelectionTestPanel.loadAllTrees();
    }

    public void newDiagram(DocumentNewMenu.DocumentType documentType, Rectangle preferredSizeLocation) {
        KinDiagramPanel egoSelectionTestPanel = new KinDiagramPanel(documentType, sessionStorage, dialogHandler, dataNodeLoader, treeHelper, entityCollection, this);
        egoSelectionTestPanel.setName("Unsaved " + documentType.getDisplayName());
        createDiagramContainer(egoSelectionTestPanel, preferredSizeLocation);
        egoSelectionTestPanel.loadAllTrees();
    }

    public void openDiagram(String diagramTitle, URI selectedUri, boolean saveToRecentMenu, Rectangle preferredSizeLocation) {
        if (saveToRecentMenu) {
            // prevent files from the samples menu being added to the recent files menu
            RecentFileMenu.addRecentFile(sessionStorage, new File(selectedUri));
        }
        KinDiagramPanel egoSelectionTestPanel = new KinDiagramPanel(selectedUri, saveToRecentMenu, sessionStorage, dialogHandler, dataNodeLoader, treeHelper, entityCollection, this);
//        egoSelectionTestPanel.setTransferHandler(dragTransferHandler);
        egoSelectionTestPanel.setName(diagramTitle);
        createDiagramContainer(egoSelectionTestPanel, preferredSizeLocation);
        egoSelectionTestPanel.loadAllTrees();
    }

    abstract Component getSelectedDiagram();

//    public void loadAllTrees(KinDiagramPanel kinDiagramPanel) {
//        Component selectedComponent = getSelectedDiagram();
//        if (selectedComponent instanceof KinDiagramPanel) {
//            ((KinDiagramPanel) selectedComponent).loadAllTrees();
//        }
//    }
    public void openImportPanel(File importFile, Component parentComponent) throws ImportException {
        new GedcomImportPanel(this, parentComponent, entityCollection, sessionStorage, dialogHandler, dataNodeLoader, treeHelper).startImport(importFile);
    }

    public void openImportPanel(String importUrlString, Component parentComponent) throws ImportException {
        new GedcomImportPanel(this, parentComponent, entityCollection, sessionStorage, dialogHandler, dataNodeLoader, treeHelper).startImport(importUrlString);
    }

    public void openJarImportPanel(String importUrlString, Component parentComponent) throws ImportException {
        new GedcomImportPanel(this, parentComponent, entityCollection, sessionStorage, dialogHandler, dataNodeLoader, treeHelper).startImportJar(importUrlString);
    }

    public abstract void setSelectedDiagram(Component diagramComponent);

    public abstract void setSelectedDiagram(int diagramIndex);

    public void openEntityUploadPanel(Rectangle preferredSizeLocation) {
        if (entityUploadPanel == null) {
            entityUploadPanel = new EntityUploadPanel(sessionStorage, entityCollection, dialogHandler);
            entityUploadPanel.setName("Entity Upload");
            createDiagramContainer(entityUploadPanel, preferredSizeLocation);
        }
        setSelectedDiagram(entityUploadPanel);
//        JDialog uploadDialog = new JDialog(this, "Entity Upload", true);
//        uploadDialog.setContentPane(new EntityUploadPanel());
//        uploadDialog.setLocationRelativeTo(this);
//        uploadDialog.setPreferredSize(new Dimension(100, 150));
//        uploadDialog.setVisible(true);
    }

    public abstract int getSavePanelIndex(Component eventTarget);

    public abstract String getSavePanelTitle(int selectedIndex);

    public SavePanel getCurrentSavePanel(Component parentComponent) {
        return getSavePanel(getSavePanelIndex(parentComponent));
    }

    abstract Component getDiagramAt(int diagramIndex);

    public SavePanel getSavePanel(int tabIndex) {
        Object selectedComponent = getDiagramAt(tabIndex);
        SavePanel savePanel = null;
        if (selectedComponent instanceof SavePanel) {
            savePanel = (SavePanel) selectedComponent;
        }
        return savePanel;
    }

    public abstract void closeSavePanel(int selectedIndex);

    public KinTermSavePanel getKinTermPanel(Component parentComponent) {
        SavePanel selectedComponent = getCurrentSavePanel(parentComponent);
        if (selectedComponent == null) {
            return null;
        }
        KinTermSavePanel kinTermSavePanel = null;
        if (selectedComponent instanceof SavePanelFrame) {
            selectedComponent = (SavePanel) ((SavePanelFrame) selectedComponent).getContentPane();
        }
        if (selectedComponent instanceof KinTermSavePanel) {
            kinTermSavePanel = (KinTermSavePanel) selectedComponent;
        }
        return kinTermSavePanel;
    }

    public abstract void setDiagramTitle(int diagramIndex, String diagramTitle);

    abstract public Component[] getAllDiagrams();

    public boolean offerUserToSaveAll() {
        int diagramCount = getAllDiagrams().length;
        for (int diagramCounter = 0; diagramCounter < diagramCount; diagramCounter++) {
            Component selectedComponent = getDiagramAt(diagramCounter);
            if (selectedComponent instanceof SavePanel) {
                SavePanel savePanel = (SavePanel) selectedComponent;
                setSelectedDiagram(selectedComponent);
                String diagramName = getSavePanelTitle(diagramCounter);
                boolean userCanceled = offerUserToSave(savePanel, diagramName);
                if (userCanceled) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean offerUserToSave(SavePanel savePanel, String diagramName) {
        if (savePanel.requiresSave()) {
            // warn user to save
            boolean fileSaved = false;
            while (!fileSaved) {
                switch (dialogHandler.showDialogBox("There are unsaved changes in: \"" + diagramName + "\"\nDo you want to save before closing?", "Close Diagram", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                    case JOptionPane.YES_OPTION:
                        if (savePanel.hasSaveFileName()) {
                            savePanel.saveToFile();
                            return false;
                        } else {
                            fileSaved = null != saveDiagramAs(savePanel);
                        }
                        break;
                    case JOptionPane.NO_OPTION:
                        return false;
                    case JOptionPane.CANCEL_OPTION:
                        return true;
                }
            }
        }
        return false;
    }

    private String saveDiagramAs(SavePanel savePanel) {
        // todo: update the file select to limit to svg and test that a file has been selected
        // todo: move this into the arbil window manager and get the last used directory
        // todo: make sure the file has the svg suffix
        JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                return (file.getName().toLowerCase().endsWith(".svg"));
            }

            @Override
            public String getDescription() {
                return "Scalable Vector Graphics (SVG)";
            }
        });

        int returnVal = fc.showSaveDialog((Component) savePanel);
        // make sure the file path ends in .svg lowercase
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File svgFile = fc.getSelectedFile();
            if (!svgFile.getName().toLowerCase().endsWith(".svg")) {
                svgFile = new File(svgFile.getParentFile(), svgFile.getName() + ".svg");
            }
            savePanel.saveToFile(svgFile);
            RecentFileMenu.addRecentFile(sessionStorage, svgFile);
            return svgFile.getName();
        } else {
            // user canceled so there is no file selected and nothing to save
            return null;
        }
//        File selectedFile[] = LinorgWindowManager.getSingleInstance().showFileSelectBox("Save Kin Diagram", false, false, false);
//        if (selectedFile != null && selectedFile.length > 0) {
//            int tabIndex = Integer.valueOf(evt.getActionCommand());
//            SavePanel savePanel = getSavePanel(tabIndex);
//            savePanel.saveToFile(selectedFile[0]);
//            jTabbedPane1.setTitleAt(tabIndex, selectedFile[0].getName());
//        } else {
//            // todo: warn user that no file selected and so cannot save
//        }
    }
}
