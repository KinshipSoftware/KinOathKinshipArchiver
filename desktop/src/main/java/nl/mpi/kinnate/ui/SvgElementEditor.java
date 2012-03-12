package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BoxLayout;
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
import nl.mpi.arbil.util.BugCatcherManager;
import org.apache.batik.bridge.UpdateManager;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
            addNumberSpinner(svgElement, sidePanel, "font size", "font-size", 1, 100);
            outerPanel.add(textArea, BorderLayout.CENTER);
            outerPanel.add(pickerWrapperPanel, BorderLayout.LINE_END);
        } else {
            addColourInput(svgElement, sidePanel, pickerPanel, "fill");
            addColourInput(svgElement, sidePanel, pickerPanel, "stroke");
            addNumberSpinner(svgElement, sidePanel, "stroke width", "stroke-width", 1, 100);
            if (editorMode.equals("rect")) {
                addNumberSpinner(svgElement, sidePanel, "corner radius", "rx", 0, 100);
            }
            outerPanel.add(pickerWrapperPanel, BorderLayout.LINE_END);
        }
        addDeleteButton(svgElement, sidePanel);
        this.add(new JScrollPane(outerPanel));
    }

    private void addDeleteButton(final Element svgElement, JPanel sidePanel) {
        final Node parentElement = svgElement.getParentNode();
        final JButton unDeleteButton = new JButton("Undelete");
        final JButton deleteButton = new JButton("Delete");
        sidePanel.add(unDeleteButton);
        unDeleteButton.setEnabled(false);
        unDeleteButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reAddElement(parentElement, svgElement);
                unDeleteButton.setEnabled(false);
                deleteButton.setEnabled(true);
            }
        });
        sidePanel.add(deleteButton);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeElement(parentElement, svgElement);
                unDeleteButton.setEnabled(true);
                deleteButton.setEnabled(false);
            }
        });
    }

    private void addNumberSpinner(final Element svgElement, JPanel sidePanel, String labelString, final String attributeString, int minValue, int maxValue) {
        int initialValue = 0;
        try {
            final String initialValueString = svgElement.getAttribute(attributeString).trim();
            if (initialValueString.length() > 0) {
                initialValue = Integer.decode(initialValueString);
            }
        } catch (NumberFormatException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        }
        sidePanel.add(new JLabel(labelString));
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
            final String attributeValue = svgElement.getAttribute(attributeString).trim();
            if (!attributeValue.equals("none")) {
                initialColour = Color.decode(attributeValue);
            }
        } catch (NumberFormatException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
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
                buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
                final JButton cancelButton = new JButton("Cancel");
                buttonPanel.add(cancelButton);
                final JButton revertButton = new JButton("Revert");
                buttonPanel.add(revertButton);
                final JButton noneButton = new JButton("None (Transparent)");
                buttonPanel.add(noneButton);
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
                        updateValue(svgElement, attributeString, "#" + Integer.toHexString(colourChooser.getColor().getRGB()).substring(2));
                    }
                });
                noneButton.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        updateValue(svgElement, attributeString, "none");
                        colourSquare.setBackground(Color.WHITE);
//                        colourChooser.setColor(Color.WHITE);
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
                pickerPanel.add(colourChooser.getChooserPanels()[0], BorderLayout.CENTER);
                pickerPanel.add(buttonPanel, BorderLayout.LINE_END);
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

    protected void removeElement(final Node parentTarget, final Element changeTarget) {
        if (updateManager != null) {
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {

                public void run() {
                    parentTarget.removeChild(changeTarget);
                }
            });
        }
    }

    protected void reAddElement(final Node parentTarget, final Element changeTarget) {
        if (updateManager != null) {
            updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {

                public void run() {
                    parentTarget.appendChild(changeTarget);
                }
            });
        }
    }
}
