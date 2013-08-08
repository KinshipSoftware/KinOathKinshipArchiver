/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;

/**
 * Document : RecentFileMenu Created on : Apr 15, 2011, 11:30:39 AM
 *
 * @author Peter Withers
 */
public class SamplesFileMenu extends JMenu implements ActionListener {

    private static final ResourceBundle menus = ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Menus");
    private final AbstractDiagramManager diagramWindowManager;
    private final MessageDialogHandler dialogHandler;
    private final Component parentComponent;

    public SamplesFileMenu(AbstractDiagramManager diagramWindowManager, MessageDialogHandler dialogHandler, Component parentComponent) {
        this.diagramWindowManager = diagramWindowManager;
        this.dialogHandler = dialogHandler;
        this.parentComponent = parentComponent;
        addSampleToMenu(menus.getString("FREEFORM DIAGRAM SYNTAX"), "FreeformDiagramSyntax.svg");
        addSampleToMenu(menus.getString("QUERY DIAGRAM SYNTAX"), "QueryDiagramSyntax.svg");
//        addSampleToMenu("Application Overview", "ApplicationOverview.svg");
        addSampleToMenu(menus.getString("HAWAIIAN KIN TERMS"), "HawaiianKinTerms.svg");
        addSampleToMenu(menus.getString("JAPANESE KIN TERMS"), "JapaneseKinTerms.svg");
//        addSampleToMenu("Japanese Kin Terms (simplified)", "JapaneseKinTermsStyle1.svg");
        addSampleToMenu(menus.getString("SWEDISH KIN TERMS"), "SwedishKinTerms.svg");
//        addSampleToMenu("Custom Symbols", "CustomSymbols.svg");
        addSampleToMenu(menus.getString("CUSTOM SYMBOLS"), "CustomEntitySymbols.svg");
        addSampleToMenu(menus.getString("NAMED TRANSIENT ENTITIES"), "NamedTransientEntities.svg");
//        addSampleToMenu("Cha'palaa Kin Terms", "ChapalaaKinTerms.svg");
//        addSampleToMenu("Gendered Ego", "GenderedEgo.svg");
//        addSampleToMenu("Olivier Kyburz Examples", "N40.svg");
        addSampleToMenu(menus.getString("MATRIMONIAL RING EXAMPLES"), "MatrimonialRings.svg");
//        addSampleToMenu("Archive Link Example", "ArchiveLinks.svg");
        addSampleToMenu(menus.getString("CHARLES II OF SPAIN"), "Charles_II_of_Spain.svg");
//        addSampleToMenu("Imported Data Query Example (ANTONIO DE PAULA PESSOA DE /FIGUEIREDO/)", "QueryExample.svg");
        addSampleToMenu(menus.getString("HAEMOPHILIA IN EUROPEAN ROYALTY"), "HaemophiliaEuropeanRoyalty.svg");
// todo:      addSampleToMenu("Bengkala Sample Diagram", "Bengkala.svg");
//        addSampleToMenu("Imported Entities (600)", "600ImportedEntities.svg");
//        addSampleToMenu("R Usage of the Entity Server", "R-ServerUsage.svg");
    }

    private void addSampleToMenu(String menuText, String sampleFileString) {
        String currentFilePath = SamplesFileMenu.class.getResource("/svgsamples/" + sampleFileString).toString();
        JMenuItem currentMenuItem = new JMenuItem(menuText);
        currentMenuItem.setActionCommand(currentFilePath);
        currentMenuItem.addActionListener(this);
        this.add(currentMenuItem);
    }

    public void actionPerformed(ActionEvent e) {
        try {
            final URI sampleFile = new URI(e.getActionCommand());
            if (e.getSource() instanceof JMenuItem) {
                String sampleName = ((JMenuItem) e.getSource()).getText();
                final Dimension parentSize = parentComponent.getSize();
                final Point parentLocation = parentComponent.getLocation();
                int offset = 10;
                final Rectangle windowRectangle = new Rectangle(parentLocation.x + offset, parentLocation.y + offset, parentSize.width - offset, parentSize.height - offset);
                diagramWindowManager.openDiagram(sampleName, sampleFile, false, windowRectangle);
            }
        } catch (URISyntaxException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            dialogHandler.addMessageDialogToQueue(menus.getString("FAILED TO LOAD SAMPLE"), menus.getString("SAMPLE DIAGRAM"));
        } catch (EntityServiceException entityServiceException) {
            dialogHandler.addMessageDialogToQueue(java.text.MessageFormat.format(menus.getString("FAILED TO LOAD SAMPLE: {0}"), new Object[]{entityServiceException.getMessage()}), "Open Diagram Error");
        }
    }
//    private void addLaunchSampleToMenu(String menuText, String sampleFileString) {
//        String currentFilePath = SamplesFileMenu.class.getResource("../../../../svgsamples/" + sampleFileString).getPath();
//        JMenuItem currentMenuItem = new JMenuItem(menuText);
//        currentMenuItem.setActionCommand(currentFilePath);
//        currentMenuItem.addActionListener(new LaunchExternal());
//        this.add(currentMenuItem);
//    }
//
//    class LaunchExternal implements ActionListener {
//
//        public void actionPerformed(ActionEvent e) {
//            try {
//                GuiHelper.getSingleInstance().openFileInExternalApplication(new URI(e.getActionCommand()));
//            } catch (URISyntaxException exception) {
//                System.err.println(exception.getMessage());
//            }
//        }
//    }
}
