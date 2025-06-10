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
package nl.mpi.arbil.ui;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * Document   : ArbilListDataListener
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */

// Updates the split panel when there are resources or loose files in the list to display
public class ArbilListDataListener implements ListDataListener {
    private ArbilSplitPanel arbilSplitPanel;

    public ArbilListDataListener(ArbilSplitPanel localArbilSplitPanel) {
        arbilSplitPanel = localArbilSplitPanel;
    }

    public void contentsChanged(ListDataEvent e) {
        if (arbilSplitPanel != null) {
            arbilSplitPanel.setSplitDisplay();
        }
    }

    public void intervalAdded(ListDataEvent e) {
        if (arbilSplitPanel != null) {
            arbilSplitPanel.setSplitDisplay();
        }
    }

    public void intervalRemoved(ListDataEvent e) {
        if (arbilSplitPanel != null) {
            arbilSplitPanel.setSplitDisplay();
        }
    }
}
