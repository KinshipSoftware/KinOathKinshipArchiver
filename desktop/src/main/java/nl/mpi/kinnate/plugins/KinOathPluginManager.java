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
package nl.mpi.kinnate.plugins;

import java.awt.Component;
import java.util.HashSet;
import nl.mpi.arbil.plugin.KinOathPanelPlugin;
import nl.mpi.arbil.plugin.PluginException;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.plugin.BasePlugin;
import nl.mpi.kinnate.ui.KinDiagramPanel;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;
import nl.mpi.pluginloader.PluginManager;

/**
 * Document : KinOathPluginManager Created on : Aug 13, 2012, 5:58:42 PM Author
 * : Peter Withers
 */
public class KinOathPluginManager implements PluginManager {

    final private AbstractDiagramManager diagramWindowManager;
    final private ArbilWindowManager dialogHandler;
    final private Component parentComponent;
    final private HashSet<BasePlugin> hashSet = new HashSet<BasePlugin>();

    public KinOathPluginManager(AbstractDiagramManager diagramWindowManager, ArbilWindowManager dialogHandler, Component parentComponent) {
        this.diagramWindowManager = diagramWindowManager;
        this.dialogHandler = dialogHandler;
        this.parentComponent = parentComponent;
    }

    private void setPluginPanel(KinOathPanelPlugin kinOathPanelPlugin, boolean isVisible) throws PluginException {
        final SavePanel currentSavePanel = diagramWindowManager.getCurrentSavePanel(parentComponent);
        if (currentSavePanel instanceof KinDiagramPanel) {
            ((KinDiagramPanel) currentSavePanel).addPluginPanel(kinOathPanelPlugin, isVisible);
        }
    }

    public void activatePlugin(BasePlugin kinOathPlugin) {
        if (kinOathPlugin instanceof KinOathPanelPlugin) {
            try {
                setPluginPanel((KinOathPanelPlugin) kinOathPlugin, true);
                hashSet.add(kinOathPlugin);
            } catch (PluginException exception) {
                dialogHandler.addMessageDialogToQueue("Failed to activate the requested plugin.\n" + exception.getMessage() + "\n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription(), "Enable Plugin Error");
            }
        } else {
            dialogHandler.addMessageDialogToQueue("No method to activate this type of plugin yet.\n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription(), "Enable Plugin");
        }
    }

    public void deactivatePlugin(BasePlugin kinOathPlugin) {
        if (kinOathPlugin instanceof KinOathPanelPlugin) {
            try {
                setPluginPanel((KinOathPanelPlugin) kinOathPlugin, false);
                hashSet.remove(kinOathPlugin);
            } catch (PluginException exception) {
                dialogHandler.addMessageDialogToQueue("Failed to deactivate the requested plugin.\n" + exception.getMessage() + "\n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription(), "Enable Plugin Error");
            }
        } else {
            dialogHandler.addMessageDialogToQueue("No method to deactivate this type of plugin yet.\n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription(), "Enable Plugin");
        }
    }

    public boolean isActivated(BasePlugin kinOathPlugin) {
        return hashSet.contains(kinOathPlugin);
    }
}
