/**
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.ui.menu;

import java.awt.Component;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JMenuBar;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.kinnate.plugins.KinOathPluginManager;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;
import nl.mpi.pluginloader.PluginService;
import nl.mpi.pluginloader.ui.PluginMenu;

/**
 * Document : MainMenuBar <br> Created on : Dec 6, 2011, 7:26:07 PM
 *
 * @author Peter Withers
 */
public class MainMenuBar extends JMenuBar {

    public MainMenuBar(AbstractDiagramManager abstractDiagramManager, SessionStorage sessionStorage, ArbilWindowManager dialogHandler, ApplicationVersionManager versionManager, Component parentComponent) {
        this.add(new FileMenu(abstractDiagramManager, sessionStorage, dialogHandler, parentComponent));
        this.add(new EditMenu(abstractDiagramManager, parentComponent));
        this.add(new DiagramOptionsMenu(abstractDiagramManager, parentComponent));
        this.add(new KinTermsMenu(abstractDiagramManager, parentComponent));
        this.add(new ArchiveMenu(abstractDiagramManager, parentComponent));
        this.add(new DiagramPanelsMenu(abstractDiagramManager, parentComponent));
        // get the list of available plugins
        ArrayList<URL> pluginUlrs = new ArrayList<URL>();
        String errorMessages = "";
        try {
            final String[] pluginStringArray = sessionStorage.loadStringArray("PluginList");
            if (pluginStringArray != null) {
                for (String pluginString : pluginStringArray) {
                    try {
                        pluginUlrs.add(new URL(pluginString));
                    } catch (MalformedURLException exception) {
                        System.out.println(exception.getMessage());
                        errorMessages = errorMessages + "Could not load plugin: " + pluginString + "\n";
                    }
                }
//            } else {
//                sessionStorage.saveStringArray("PluginList", new String[]{"file:///<path to plugin>.jar", "file:///<path to plugin>.jar"});
            }
            if (!"".equals(errorMessages)) {
                dialogHandler.addMessageDialogToQueue(errorMessages, "Plugin Error");
            }
        } catch (IOException ex) {
            // if the list is not found then we need not worry at this point.
            System.out.println("PluginList not found");
        }
        this.add(new PluginMenu(new PluginService(pluginUlrs.toArray(new URL[0])), new KinOathPluginManager(abstractDiagramManager, dialogHandler, parentComponent), false));
        this.add(new WindowMenu(abstractDiagramManager, parentComponent));
        this.add(new HelpMenu(abstractDiagramManager, dialogHandler, sessionStorage, versionManager));
    }
}
