/**
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
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

    private final AbstractDiagramManager diagramWindowManager;
    private final MessageDialogHandler dialogHandler;
    private final Component parentComponent;

    public SamplesFileMenu(AbstractDiagramManager diagramWindowManager, MessageDialogHandler dialogHandler, Component parentComponent) {
        this.diagramWindowManager = diagramWindowManager;
        this.dialogHandler = dialogHandler;
        this.parentComponent = parentComponent;
        addSampleToMenu("Freeform Diagram Syntax", "FreeformDiagramSyntax.svg");
        addSampleToMenu("Query Diagram Syntax", "QueryDiagramSyntax.svg");
//        addSampleToMenu("Application Overview", "ApplicationOverview.svg");
        addSampleToMenu("Hawaiian Kin Terms", "HawaiianKinTerms.svg");
        addSampleToMenu("Japanese Kin Terms", "JapaneseKinTerms.svg");
//        addSampleToMenu("Japanese Kin Terms (simplified)", "JapaneseKinTermsStyle1.svg");
        addSampleToMenu("Swedish Kin Terms", "SwedishKinTerms.svg");
//        addSampleToMenu("Custom Symbols", "CustomSymbols.svg");
        addSampleToMenu("Custom Symbols", "CustomEntitySymbols.svg");
        addSampleToMenu("Named Transient Entities", "NamedTransientEntities.svg");
//        addSampleToMenu("Cha'palaa Kin Terms", "ChapalaaKinTerms.svg");
//        addSampleToMenu("Gendered Ego", "GenderedEgo.svg");
//        addSampleToMenu("Olivier Kyburz Examples", "N40.svg");
        addSampleToMenu("Matrimonial Ring Examples", "MatrimonialRings.svg");
//        addSampleToMenu("Archive Link Example", "ArchiveLinks.svg");
        addSampleToMenu("Charles II of Spain", "Charles_II_of_Spain.svg");
//        addSampleToMenu("Imported Data Query Example (ANTONIO DE PAULA PESSOA DE /FIGUEIREDO/)", "QueryExample.svg");
        addSampleToMenu("Haemophilia in European Royalty", "HaemophiliaEuropeanRoyalty.svg");
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
            dialogHandler.addMessageDialogToQueue("Failed to load sample", "Sample Diagram");
        } catch (EntityServiceException entityServiceException) {
            dialogHandler.addMessageDialogToQueue("Failed to load sample: " + entityServiceException.getMessage(), "Open Diagram Error");
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
