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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import nl.mpi.arbil.data.ArbilField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document : FindReplacePanel
 * Created on : Jun 21, 2010, 10:13:22 AM
 * Author : Peter Withers
 */
public class FindReplacePanel extends JPanel {
    private final static Logger logger = LoggerFactory.getLogger(FindReplacePanel.class);
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");

    private final static String defaultFindText = widgets.getString("<ENTER FIND TEXT>");
    private final static String defaultReplaceText = widgets.getString("<ENTER REPLACEMENT TEXT>");
    private final ArbilSplitPanel splitPanel;
    private final JButton closeButton;
    private final JTextField searchField;
    private final JTextField replaceField;
    private final JCheckBox useJavaRegExCheckBox;
    //private final JCheckBox replaceAllCheckBox;
    private final JButton searchButton;
    private final JButton replaceButton;

    public FindReplacePanel(ArbilSplitPanel splitPanelLocal) {
	splitPanel = splitPanelLocal;
	searchField = new JTextField(defaultFindText);
	replaceField = new JTextField(defaultReplaceText);
	searchField.setName(defaultFindText);
	replaceField.setName(defaultReplaceText);
	searchField.setForeground(Color.gray);
	replaceField.setForeground(Color.gray);
	useJavaRegExCheckBox = new JCheckBox(widgets.getString("REGULAR EXPRESSION"));
	//replaceAllCheckBox = new JCheckBox("Replace All");
	useJavaRegExCheckBox.setToolTipText("Use Java Style Regular Expressions (eg: "
		+ ". any "
		+ "\\d digit "
		+ "\\s whitespace "
		+ "? zero or once "
		+ "* zero or more "
		+ "+ one or more "
		+ "^ line start "
		+ "$ line end)");

	closeButton = new JButton(closeAction);
	searchButton = new JButton(widgets.getString("FIND NEXT"));
	replaceButton = new JButton(widgets.getString("REPLACE SELECTED"));
	searchField.addActionListener(findActionListener);
	replaceField.addActionListener(findActionListener);
//        useJavaRegExCheckBox.addActionListener(this);
	searchButton.addActionListener(findActionListener);
	replaceButton.addActionListener(findActionListener);
	searchField.addFocusListener(findReplaceFocusListener);
	replaceField.addFocusListener(findReplaceFocusListener);
	this.add(new JLabel(widgets.getString("FIND:")));
	this.add(searchField);
	this.add(replaceField);
//        this.add(useJavaRegExCheckBox);
//        this.add(replaceAllCheckBox);
	this.add(searchButton);
	this.add(replaceButton);
	this.add(closeButton);

	getActionMap().put("close", closeAction);
	getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
    }
    final private Action closeAction = new AbstractAction("Close") {
	public void actionPerformed(ActionEvent e) {
	    splitPanel.showSearchPane();
	}
    };
    /**
     * Action listener for search and replace buttons and fields
     */
    final private ActionListener findActionListener = new ActionListener() {
	public void actionPerformed(ActionEvent actionEvent) {
	    logger.debug(actionEvent.toString());
	    String searchString = "";
	    if (!searchField.getText().equals(defaultFindText)) {
		searchString = searchField.getText();
	    }
	    String replaceString = null;
	    if (actionEvent.getSource().equals(replaceButton)) {
		replaceString = "";
		if (!replaceField.getText().equals(defaultReplaceText)) {
		    replaceString = replaceField.getText();
		}
	    }

	    if (actionEvent.getSource().equals(replaceButton)) {
		for (ArbilField currentField : splitPanel.arbilTable.getSelectedFields()) {
		    if (currentField.getParentDataNode().isEditable() && currentField.getFieldValue().contains(searchString)) {
			String currentFieldString = currentField.getFieldValue();
			if (currentFieldString.contains(searchString)) {
			    currentFieldString = currentFieldString.replace(searchString, replaceString);
			    currentField.setFieldValue(currentFieldString, true, false);
			}
		    }
		}
		// in the case where a user has selected a number of rows to replace in we do not want to clear their selection
		return;
	    }

	    //boolean replaceAll = (actionEvent.getSource().equals(replaceButton) && replaceAllCheckBox.isSelected());

	    int startColumn = splitPanel.arbilTable.getSelectedColumn();
	    int startRow = splitPanel.arbilTable.getSelectedRow();
	    boolean firstTime = true;
	    if (startColumn < 0) {
		firstTime = false;
		startColumn = 0;
	    }
	    if (startRow < 0) {
		firstTime = false;
		startRow = 0;
	    }
	    for (int currentRow = startRow; currentRow < splitPanel.arbilTable.getRowCount(); currentRow++) {
		for (int currentColumn = startColumn; currentColumn < splitPanel.arbilTable.getColumnCount(); currentColumn++) {
		    if (firstTime) {
			firstTime = false;
		    } else {
			Object currentCellValue = splitPanel.arbilTable.getTableCellContentAt(currentRow, currentColumn);
			if (currentCellValue instanceof ArbilField) {
			    currentCellValue = new ArbilField[]{(ArbilField) currentCellValue};
			}
			if (currentCellValue instanceof ArbilField[]) {
			    for (ArbilField currentField : (ArbilField[]) currentCellValue) {
				if (currentField.getFieldValue().contains(searchString)) {
				    splitPanel.arbilTable.setCellSelectionEnabled(true);
				    splitPanel.arbilTable.changeSelection(currentRow, currentColumn, false, false);
//                                if (!replaceAll) {
				    return;
//                                }
				}
			    }
			}
		    }
		}
		startColumn = 0;
	    }
	}
    };
    /**
     * Focus listener for search and replace fields
     */
    final private FocusListener findReplaceFocusListener = new FocusListener() {
	public void focusGained(FocusEvent e) {
	    JTextField currentTextField = (JTextField) e.getSource();
	    if (currentTextField.getText().equals(defaultFindText) || currentTextField.getText().equals(defaultReplaceText)) {
		currentTextField.setText("");
	    }
	    currentTextField.setForeground(Color.black);
	}

	public void focusLost(FocusEvent e) {
	    JTextField currentTextField = (JTextField) e.getSource();
	    if (currentTextField.getText().length() < 1) {// || currentTextField.getText().equals(currentTextField.getName())) {
		currentTextField.setText(currentTextField.getName());
		currentTextField.setForeground(Color.gray);
	    }
	}
    };

    public void requestFocusOnSearchField() {
	searchField.requestFocus();
    }
}
