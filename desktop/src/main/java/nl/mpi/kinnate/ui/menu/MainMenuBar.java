package nl.mpi.kinnate.ui.menu;

import java.awt.Component;
import javax.swing.JMenuBar;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.kinnate.plugins.KinOathPluginManager;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;
import nl.mpi.pluginloader.PluginService;
import nl.mpi.pluginloader.ui.PluginMenu;

/**
 * Document : MainMenuBar
 * Created on : Dec 6, 2011, 7:26:07 PM
 * Author : Peter Withers
 */
public class MainMenuBar extends JMenuBar {

    public MainMenuBar(AbstractDiagramManager abstractDiagramManager, SessionStorage sessionStorage, ArbilWindowManager dialogHandler, ApplicationVersionManager versionManager, Component parentComponent) {
        this.add(new FileMenu(abstractDiagramManager, sessionStorage, dialogHandler, parentComponent));
        this.add(new EditMenu(abstractDiagramManager, parentComponent));
        this.add(new DiagramOptionsMenu(abstractDiagramManager, parentComponent));
        this.add(new KinTermsMenu(abstractDiagramManager, parentComponent));
        this.add(new ArchiveMenu(abstractDiagramManager, parentComponent));
        this.add(new DiagramPanelsMenu(abstractDiagramManager, parentComponent));
        this.add(new PluginMenu(PluginService.getInstance(), new KinOathPluginManager(abstractDiagramManager, dialogHandler, parentComponent)));
        this.add(new WindowMenu(abstractDiagramManager, parentComponent));
        this.add(new HelpMenu(abstractDiagramManager, dialogHandler, sessionStorage, versionManager));
    }
}
