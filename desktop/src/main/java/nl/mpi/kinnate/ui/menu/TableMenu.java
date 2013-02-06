/*
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.kinnate.ui.menu;

import java.awt.Component;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.kinnate.entityindexer.EntityCollection;

/**
 * Created on : Feb 6, 2013, 3:16:43 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class TableMenu extends JPopupMenu {

    private final ArbilField arbilField;
    private final ArbilDataNode arbilDataNode;
    private final EntityCollection entityCollection;

    public TableMenu(EntityCollection entityCollection, Object cellContents) {
        System.out.println("cellContents:" + cellContents.getClass());
        this.entityCollection = entityCollection;
        if (cellContents instanceof ArbilDataNode) {
            arbilDataNode = (ArbilDataNode) cellContents;
            arbilField = null;
            getAddMenu();
        } else if (cellContents instanceof ArbilField) {
            arbilField = (ArbilField) cellContents;
            arbilDataNode = null;
            getAddMenu();
            this.add(getDeleteMenuItem(arbilField));
        } else {
            arbilField = null;
            arbilDataNode = null;
        }
    }
    
    private void getAddMenu() {
        int addedCounter = 0;
        JMenu addMenu = new JMenu("Add");
        for (String fieldName : entityCollection.getAllFieldNames()) {
            if (addedCounter > 20) {
                this.add(addMenu);
                addMenu = new JMenu("Add (" + fieldName.substring(0, 1) + ")");
                addedCounter = 0;
            }
            addMenu.add(new JMenuItem(fieldName));
            addedCounter++;
        }
        this.add(addMenu);
        this.add(new JMenuItem("Add <custom field>"));
    }

    private JMenuItem getDeleteMenuItem(ArbilField arbilField) {
        return new JMenuItem("Delete Field \"" + arbilField.getTranslateFieldName() + "\"");
    }

    @Override
    public void show(Component cmpnt, int i, int i1) {
        if (arbilField == null && arbilDataNode == null) {
            // do not show when there are no menu items 
            return;
        }
        super.show(cmpnt, i, i1);

    }
}
