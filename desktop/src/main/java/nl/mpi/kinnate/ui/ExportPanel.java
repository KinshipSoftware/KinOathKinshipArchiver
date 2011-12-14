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
        this.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JTextField("Export Filename"), BorderLayout.CENTER);
        topPanel.add(new JCheckBox("Update export file when diagram changes"), BorderLayout.LINE_END);
        topPanel.add(new JLabel("Table of the exported data"), BorderLayout.PAGE_END);
        this.add(topPanel, BorderLayout.PAGE_START);
        this.add(new JTable(), BorderLayout.CENTER);
    }
}
