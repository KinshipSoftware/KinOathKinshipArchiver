package nl.mpi.kinnate.ui;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import nl.mpi.kinnate.kintypestrings.ParserHighlight;

/**
 *  Document   : KinTypeStringInput
 *  Created on : Sep 7, 2011, 3:52:06 PM
 *  Author     : Peter Withers
 */
public class KinTypeStringInput extends JTextPane {

    private String previousKinTypeStrings = null;
    private String defaultString;
    private Color defaultColour = Color.GRAY;
    protected Style styleComment;
    protected Style styleKinType;
    protected Style styleQuery;
    protected Style styleParamater;
    protected Style styleError;
    protected Style styleUnknown;
    private ParserHighlight[] parserHighlight = null;

    public KinTypeStringInput(String defaultString) {
        this.defaultString = defaultString;
        this.setToolTipText("");
        // set the styles for the kin type string text
        styleComment = this.addStyle("Comment", null);
//        StyleConstants.setForeground(styleComment, new Color(247,158,9));
        StyleConstants.setForeground(styleComment, Color.GRAY);
        styleKinType = this.addStyle("KinType", null);
        StyleConstants.setForeground(styleKinType, new Color(43, 32, 161));
        styleQuery = this.addStyle("Query", null);
        StyleConstants.setForeground(styleQuery, new Color(183, 7, 140));
        styleParamater = this.addStyle("Parameter", null);
        StyleConstants.setForeground(styleParamater, new Color(103, 7, 200));
        styleError = this.addStyle("Error", null);
//        StyleConstants.setForeground(styleError, new Color(172,3,57));
        StyleConstants.setForeground(styleError, Color.RED);
        styleUnknown = this.addStyle("Unknown", null);
        StyleConstants.setForeground(styleUnknown, Color.BLACK);
        this.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                clearDefaultKinTypeInput();
            }

            public void focusLost(FocusEvent e) {
                checkKinTypeInput();
            }
        });
        this.setText(defaultString);
        this.setForeground(defaultColour);
    }

    public void setDefaultText() {
        this.setText(defaultString);
        this.setForeground(defaultColour);
    }

    public void clearDefaultKinTypeInput() {
        if (this.getText().equals(defaultString)) {
            this.setText("");
//                    kinTypeStringInput.setForeground(Color.BLACK);
        }
    }

    public void checkKinTypeInput() {
        if (this.getText().length() == 0) {
            this.setText(defaultString);
            this.setForeground(defaultColour);
        }
    }

    public String[] getCurrentStrings() {
        previousKinTypeStrings = this.getText();
        return this.getText().split("\n");
    }

    public boolean hasChanges() {
        return (previousKinTypeStrings == null || !previousKinTypeStrings.equals(this.getText()));
    }

    protected void highlightKinTerms(ParserHighlight[] parserHighlight, String[] kinTypeStrings) {
        this.parserHighlight = parserHighlight;
        StyledDocument styledDocument = this.getStyledDocument();
        int lineStart = 0;
        for (int lineCounter = 0; lineCounter < parserHighlight.length; lineCounter++) {
            ParserHighlight currentHighlight = parserHighlight[lineCounter];
//                int lineStart = styledDocument.getParagraphElement(lineCounter).getStartOffset();
//                int lineEnd = styledDocument.getParagraphElement(lineCounter).getEndOffset();
            int lineEnd = lineStart + kinTypeStrings[lineCounter].length();
            styledDocument.setCharacterAttributes(lineStart, lineEnd, this.getStyle("Unknown"), true);
            while (currentHighlight.highlight != null) {
                int startPos = lineStart + currentHighlight.startChar;
                int charCount = lineEnd - lineStart;
                if (currentHighlight.nextHighlight.highlight != null) {
                    charCount = currentHighlight.nextHighlight.startChar - currentHighlight.startChar;
                }
                if (currentHighlight.highlight != null) {
                    String styleName = currentHighlight.highlight.name();
                    styledDocument.setCharacterAttributes(startPos, charCount, this.getStyle(styleName), true);
                }
                currentHighlight = currentHighlight.nextHighlight;
            }
            lineStart += kinTypeStrings[lineCounter].length() + 1;
        }
    }

    @Override
    public Point getToolTipLocation(MouseEvent event) {
        if (parserHighlight != null) {
            int textPosition = this.viewToModel(event.getPoint());
            final String[] lineStrings = previousKinTypeStrings.substring(0, textPosition).split("\n");

            int linePosition = lineStrings.length;
            int lineChar = lineStrings[linePosition - 1].length();
            ParserHighlight currentHighlight = parserHighlight[linePosition - 1];

            while (currentHighlight.highlight != null && currentHighlight.nextHighlight.highlight != null && currentHighlight.nextHighlight.startChar <= lineChar) {
                currentHighlight = currentHighlight.nextHighlight;
            }
//            this.setToolTipText("loc: " + textPosition + " line: " + linePosition + " char: " + lineChar + " startChar: " + currentHighlight.startChar + " : " + currentHighlight.tooltipText);
            this.setToolTipText(currentHighlight.tooltipText);
        } else {
            this.setToolTipText(null);
        }
        return super.getToolTipLocation(event);
    }
}