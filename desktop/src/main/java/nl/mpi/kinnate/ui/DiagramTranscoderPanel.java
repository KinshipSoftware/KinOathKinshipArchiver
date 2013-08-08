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
package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Dimension2D;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import nl.mpi.kinnate.svg.DiagramTranscoder;
import nl.mpi.kinnate.svg.DiagramTranscoder.OutputType;

/**
 * Document : DiagramTranscoderPanel
 * Created on : Apr 19, 2012, 1:04:12 PM
 * Author : Peter Withers
 */
public class DiagramTranscoderPanel extends JPanel {

    DiagramTranscoder diagramTranscoder;
//    JSpinner dpiSpinner;
    JComboBox formatComboBox;
    JLabel ouputSizeLabel;

    public DiagramTranscoderPanel(DiagramTranscoder diagramTranscoder) {
        this.setLayout(new BorderLayout());
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.PAGE_AXIS));
        this.add(outerPanel, BorderLayout.PAGE_START);
        this.diagramTranscoder = diagramTranscoder;
        formatComboBox = new JComboBox();
        for (OutputType outputType : OutputType.values()) {
            formatComboBox.addItem(outputType.name());
        }
        formatComboBox.setSelectedItem(this.diagramTranscoder.getOutputType().name());
        outerPanel.add(formatComboBox);
//        dpiSpinner = new JSpinner(new SpinnerNumberModel(this.diagramTranscoder.getDpi(), 10, 600, 10));
//        JPanel dpiPanel = new JPanel();
//        dpiPanel.add(dpiSpinner);
//        dpiPanel.add(new JLabel("DPI"));
//        outerPanel.add(dpiPanel);
        ouputSizeLabel = new JLabel(getSizeString(), JLabel.CENTER);
        outerPanel.add(ouputSizeLabel);
//        final ChangeListener changeListener = new ChangeListener() {
//
//            public void stateChanged(ChangeEvent e) {
//                updateSettings();
//            }
//        };
//        dpiSpinner.addChangeListener(changeListener);
        formatComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateSettings();
            }
        });
    }

    protected void updateSettings() {
//        diagramTranscoder.setDpi(Integer.parseInt(dpiSpinner.getValue().toString()));
        diagramTranscoder.setOutputType(OutputType.valueOf(formatComboBox.getSelectedItem().toString()));
        ouputSizeLabel.setText(getSizeString());
    }

    private String getSizeString() {
        final Dimension2D currentSize = diagramTranscoder.getCurrentSize();
        if (currentSize != null) {
            return (int) currentSize.getWidth() + " x " + (int) currentSize.getHeight() + " ";
        } else {
            return ""; // size not available
        }
    }
}
