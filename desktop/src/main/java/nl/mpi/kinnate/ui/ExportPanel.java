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

import java.awt.BorderLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 *  Document   : ExportPanel
 *  Created on : Dec 11, 2011, 3:11:42 PM
 *  Author     : Peter Withers
 */
public class ExportPanel extends JPanel {

    public ExportPanel() {
        // todo: complete this panel
        // Ticket #1330 Add an export panel for more control over the exported data and enable output file updates when the diagram changes.
        this.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JTextField("Export Filename"), BorderLayout.CENTER);
        topPanel.add(new JCheckBox("Update export file when diagram changes"), BorderLayout.LINE_END);
        topPanel.add(new JLabel("Table of the exported data (note that this panel is not yet functional!)"), BorderLayout.PAGE_END);
        this.add(topPanel, BorderLayout.PAGE_START);
        this.add(new JTable(), BorderLayout.CENTER);
    }
}
