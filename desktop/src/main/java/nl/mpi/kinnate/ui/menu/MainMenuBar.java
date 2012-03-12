package nl.mpi.kinnate.ui.menu;

import javax.swing.JMenuBar;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;

/**
 *  Document   : MainMenuBar
 *  Created on : Dec 6, 2011, 7:26:07 PM
 *  Author     : Peter Withers
 */
public class MainMenuBar extends JMenuBar {

    public MainMenuBar(AbstractDiagramManager abstractDiagramManager, SessionStorage sessionStorage, ArbilWindowManager dialogHandler, ApplicationVersionManager versionManager) {
        this.add(new FileMenu(abstractDiagramManager, sessionStorage, dialogHandler));
        this.add(new EditMenu(abstractDiagramManager));
        this.add(new DiagramOptionsMenu(abstractDiagramManager));
        this.add(new KinTermsMenu(abstractDiagramManager));
        this.add(new ArchiveMenu(abstractDiagramManager));
        this.add(new DiagramPanelsMenu(abstractDiagramManager));
        this.add(new WindowMenu(abstractDiagramManager));
        this.add(new HelpMenu(abstractDiagramManager, dialogHandler, sessionStorage, versionManager));
    }
}
