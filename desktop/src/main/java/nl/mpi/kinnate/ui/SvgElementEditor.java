package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
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
        JPanel sidePanel = new JPanel(new GridLayout(0, 2));
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
            for (final String attribiteName : new String[]{"fill", "font-size", "stroke-width"}) {
                sidePanel.add(new JLabel(attribiteName));
                final JTextField textField = new JTextField(svgElement.getAttribute(attribiteName));
                textField.addFocusListener(new FocusListener() {

                    public void focusGained(FocusEvent e) {
                    }

                    public void focusLost(FocusEvent e) {
                        updateValue(svgElement, attribiteName, textField.getText());
                    }
                });
                sidePanel.add(textField);
            }
            outerPanel.add(textArea, BorderLayout.CENTER);
//            JPanel sindeInnd
            outerPanel.add(sidePanel, BorderLayout.LINE_END);
        } else {
            sidePanel.add(new JLabel("SvgElementEditor"));
            sidePanel.add(new JLabel(svgElement.getTagName()));
            outerPanel.add(sidePanel, BorderLayout.CENTER);
        }
        this.add(new JScrollPane(outerPanel));
        // todo: Ticket #1065 Enable editing of the text and font size of labels in the diagram.
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
