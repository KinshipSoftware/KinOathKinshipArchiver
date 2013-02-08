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
package nl.mpi.kinnate.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;

/**
 * Document : RecentFileMenu Created on : Apr 15, 2011, 11:30:39
 *
 * @author Peter Withers
 */
public class ImportSamplesFileMenu extends JMenu implements ActionListener {

    private AbstractDiagramManager diagramWindowManager;
    private MessageDialogHandler dialogHandler;
    private Component parentComponent;

    public ImportSamplesFileMenu(AbstractDiagramManager diagramWindowManager, MessageDialogHandler dialogHandler, Component parentComponent) {
        this.diagramWindowManager = diagramWindowManager;
        this.parentComponent = parentComponent;
        this.dialogHandler = dialogHandler;
        addSampleToMenu("Gedcom Simple File (small sample of three individuals)", "/gedcomsamples/wiki-test-ged.ged");
        addSampleToMenu("Gedcom Torture File (only for testing GEDOM compliance)", "/TestGED/TGC55C.ged");
//        addSampleToMenu("Descententes de Jose Antonio de Figueiredo", "/gedcomsamples/descententes_de_jose_antonio_de_figueiredo.ged");
//        addSampleToMenu("Wadeye-Joe-Blythe-20110525", "/AllianceSamples/Wadeye-Joe-Blythe-20110525.csv");
        addSampleToMenu("European Royalty (royal92.ged)", "/gedcomsamples/royal92.ged");
// todo:         addSampleToMenu("Bengkala Sample Data", "/csvexamples/bengkala.csv");
//        addSampleToMenu("Doerte Sample", "/DoerteSamples/sample.csv");
//        addSampleToMenu("AltNetspace Sample", "/AltNetspace/people.csv");
    }

    private void addSampleToMenu(String menuText, String sampleFileString) {
//        String currentFilePath = ImportSamplesFileMenu.class.getResource("../../../../svgsamples/" + sampleFileString).getPath();
        JMenuItem currentMenuItem = new JMenuItem(menuText);
        currentMenuItem.setActionCommand(sampleFileString);
        currentMenuItem.addActionListener(this);
        this.add(currentMenuItem);
    }

    public void actionPerformed(ActionEvent e) {
//        SavePanel originatingSavePanel = diagramWindowManager.getCurrentSavePanel(parentComponent);
        try {
            diagramWindowManager.openJarImportPanel(e.getActionCommand(), parentComponent);
        } catch (ImportException exception1) {
            dialogHandler.addMessageDialogToQueue(exception1.getMessage() + "\n" + e.getActionCommand(), "Import Sample Data");
        }
    }
}
