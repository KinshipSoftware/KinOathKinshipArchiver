package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import nl.mpi.arbil.ui.GuiHelper;
import org.apache.batik.bridge.UpdateManager;
import org.w3c.dom.Element;

/**
 *  Document   : SvgElementEditor
 *  Created on : Aug 17, 2011, 1:17:13 PM
 *  Author     : Peter Withers
 */
public class SvgElementEditor extends JPanel {

    UpdateManager updateManager;

    public SvgElementEditor(final UpdateManager updateManager, final Element svgElement) {
        this.updateManager = updateManager;
        this.setLayout(new BorderLayout());
        JPanel outerPanel = new JPanel(new BorderLayout());
        JPanel pickerPanel = new JPanel(new BorderLayout());
        JPanel pickerWrapperPanel = new JPanel(new BorderLayout());
        JPanel sidePanel = new JPanel(new GridLayout(0, 2));
        JPanel sideWrapperPanel = new JPanel(new BorderLayout());
        sideWrapperPanel.add(sidePanel, BorderLayout.PAGE_START);
        pickerWrapperPanel.add(pickerPanel, BorderLayout.CENTER);
        pickerWrapperPanel.add(sideWrapperPanel, BorderLayout.LINE_END);
        String editorMode = svgElement.getTagName();
        if (editorMode.equals("text")) {
//            this.add(new JLabel("Label Text:"));
            final JTextArea textArea = new JTextArea(svgElement.getTextContent());
            textArea.addFocusListener(new FocusListener() {

                public void focusGained(FocusEvent e) {
                }

                public void focusLost(FocusEvent e) {
                    updateValue(svgElement, textArea.getText());
                }
            });
            addColourInput(svgElement, sidePanel, pickerPanel, "fill");
            addNumberSpinner(svgElement, sidePanel, "font-size", 1, 100);
            outerPanel.add(textArea, BorderLayout.CENTER);
            outerPanel.add(pickerWrapperPanel, BorderLayout.LINE_END);
        } else {
            addColourInput(svgElement, sidePanel, pickerPanel, "fill");
            addColourInput(svgElement, sidePanel, pickerPanel, "stroke");
            addNumberSpinner(svgElement, sidePanel, "stroke-width", 1, 100);
            outerPanel.add(pickerWrapperPanel, BorderLayout.CENTER);
        }
        this.add(new JScrollPane(outerPanel));
        // todo: Ticket #1065 Enable editing of the text and font size of labels in the diagram.
    }

    private void addNumberSpinner(final Element svgElement, JPanel sidePanel, final String attributeString, int minValue, int maxValue) {
        int initialValue = 0;
        try {
            initialValue = Integer.decode(svgElement.getAttribute(attributeString).trim());
        } catch (NumberFormatException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        }
        sidePanel.add(new JLabel(attributeString));
        SpinnerModel spinnerModel =
                new SpinnerNumberModel(initialValue, minValue, maxValue, 1);
        final JSpinner numberSpinner = new JSpinner(spinnerModel);
        numberSpinner.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                updateValue(svgElement, attributeString, numberSpinner.getValue().toString());
            }
        });
        sidePanel.add(numberSpinner);
    }

    private void addColourInput(final Element svgElement, JPanel sidePanel, final JPanel pickerPanel, final String attributeString) {
        Color initialColour = Color.white;
        try {
            initialColour = Color.decode(svgElement.getAttribute(attributeString).trim());
        } catch (NumberFormatException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        }
        sidePanel.add(new JLabel(attributeString));
        final JPanel colourSquare = new JPanel();
        colourSquare.setBackground(initialColour);
        colourSquare.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                pickerPanel.removeAll();
                final JColorChooser colourChooser = new JColorChooser(colourSquare.getBackground());
                final Color revertColour = colourSquare.getBackground();
                final JPanel buttonPanel = new JPanel();
                final JButton cancelButton = new JButton("Cancel");
                buttonPanel.add(cancelButton);
                final JButton revertButton = new JButton("Revert");
                buttonPanel.add(revertButton);
                final JButton okButton = new JButton("OK");
                buttonPanel.add(okButton);
                cancelButton.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        colourSquare.setBackground(revertColour);
                        colourChooser.setColor(revertColour);
                        pickerPanel.removeAll();
                        revalidate();
                        repaint();
                    }
                });
                revertButton.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        colourSquare.setBackground(revertColour);
                        colourChooser.setColor(revertColour);
                    }
                });
                okButton.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        pickerPanel.removeAll();
                        revalidate();
                        repaint();
                    }
                });


                colourChooser.setPreviewPanel(new JPanel());
                colourChooser.getSelectionModel().addChangeListener(new ChangeListener() {

                    public void stateChanged(ChangeEvent e) {
                        colourSquare.setBackground(colourChooser.getColor());
                        updateValue(svgElement, attributeString, "#" + Integer.toHexString(colourChooser.getColor().getRGB()).substring(2));
                    }
                });
                pickerPanel.add(colourChooser, BorderLayout.CENTER);
                pickerPanel.add(buttonPanel, BorderLayout.PAGE_START);
                revalidate();
                repaint();
            }
        });
        sidePanel.add(colourSquare);
    }

    protected void updateValue(final Element changeTarget, final String attributeName, final String changeValue) {
        if (updateManager != null) {
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {

                public void run() {
                    changeTarget.setAttribute(attributeName, changeValue);
                }
            });
        }
    }

    protected void updateValue(final Element changeTarget, final String changeValue) {
        if (updateManager != null) {
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {

                public void run() {
                    changeTarget.setTextContent(changeValue);
                }
            });
        }
    }
}
