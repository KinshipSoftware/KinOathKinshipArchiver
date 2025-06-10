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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellEditor;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilTableCell;
import nl.mpi.arbil.ui.fieldeditors.ArbilFieldEditor;
import nl.mpi.arbil.ui.fieldeditors.ArbilLongFieldEditor;
import nl.mpi.arbil.ui.fieldeditors.ControlledVocabularyComboBox;
import nl.mpi.arbil.ui.fieldeditors.ControlledVocabularyComboBoxEditor;
import nl.mpi.arbil.ui.fieldeditors.LanguageIdBox;
import nl.mpi.arbil.util.BugCatcherManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document : ArbilTableCellEditor
 * Created on :
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final static Logger logger = LoggerFactory.getLogger(ArbilTableCellEditor.class);
    /**
     * Action key that starts appending to field in editor
     */
    public static final int APPEND_TO_FIELD_ACTION_KEY = KeyEvent.VK_F2;
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");
    private final TableController tableController;
    private final JPanel editorPanel;
    private final ArbilTableCellRenderer arbilTableCellRenderer;
    private final JLabel button;
    private int columnIndex;
    private int rowIndex;
    ArbilTable parentTable = null;
    Rectangle parentCellRect = null;
    ArbilDataNode registeredOwner = null;
    String fieldName;
    Component editorComponent = null;
//    boolean receivedKeyDown = false;
    Object[] cellValue;
    int selectedField = -1;
    Vector<Component> componentsWithFocusListners = new Vector();
    private final MouseListener fieldMouseAdapter = new java.awt.event.MouseAdapter() {
	@Override
	public void mouseReleased(MouseEvent evt) {
	    tableController.checkPopup(evt, false);
	}

	@Override
	public void mousePressed(MouseEvent evt) {
	    tableController.checkPopup(evt, false);
	}

	@Override
	public void mouseClicked(java.awt.event.MouseEvent evt) {
	    if (evt.getButton() == MouseEvent.BUTTON1) {
		startEditorMode(isStartLongFieldModifier(evt), KeyEvent.CHAR_UNDEFINED, KeyEvent.CHAR_UNDEFINED);
	    } else {
		tableController.checkPopup(evt, false);
	    }
	}
    };

    public ArbilTableCellEditor(TableController tableController) {
	this.tableController = tableController;

	arbilTableCellRenderer = new ArbilTableCellRenderer();
	button = new JLabel("...");
	editorPanel = new JPanel();

	button.getActionMap().put("appendToEditor", new AbstractAction() {
	    public void actionPerformed(ActionEvent e) {
		startEditorMode(false, KeyEvent.CHAR_UNDEFINED, KeyEvent.CHAR_UNDEFINED);
	    }
	});
	button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK), "appendToEditor");

	button.addKeyListener(new java.awt.event.KeyListener() {
	    public void keyTyped(KeyEvent evt) {
	    }

	    public void keyPressed(KeyEvent evt) {
//                receivedKeyDown = true; // this is to prevent reopening the editor after a ctrl_w has closed the editor
	    }

	    public void keyReleased(KeyEvent evt) {
		if (!cellHasControlledVocabulary() && isStartLongFieldKey(evt)) {// prevent ctrl key events getting through etc.
		    startEditorMode(true, KeyEvent.CHAR_UNDEFINED, KeyEvent.CHAR_UNDEFINED);
		} else if (!evt.isActionKey()
			&& !evt.isMetaDown() // these key down checks will not catch a key up event hence the key codes below which work for up and down events
			&& !evt.isAltDown()
			&& !evt.isAltGraphDown()
			&& !evt.isControlDown()
			&& evt.getKeyCode() != KeyEvent.VK_SHIFT
			&& evt.getKeyCode() != KeyEvent.VK_CONTROL
			&& evt.getKeyCode() != KeyEvent.VK_ALT
			&& evt.getKeyCode() != KeyEvent.VK_META
			&& evt.getKeyCode() != KeyEvent.VK_ESCAPE) {
		    startEditorMode(false, evt.getKeyCode(), evt.getKeyChar());
		} else if (evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_U
			|| evt.getKeyCode() == APPEND_TO_FIELD_ACTION_KEY) {
		    startEditorMode(false, evt.getKeyCode(), evt.getKeyChar());
		}
	    }
	});
	button.addMouseListener(fieldMouseAdapter);
    }

    private void initControlledVocabularyEditor(int lastKeyInt, char lastKeyChar) {
	// Remove 'button', which is the non-editor mode component for the cell
	editorPanel.remove(button);
	// if the cell has a vocabulary then prevent the long field editor
	ControlledVocabularyComboBox cvComboBox = new ControlledVocabularyComboBox((ArbilField) cellValue[selectedField]);
	// Add combobox
	editorPanel.add(cvComboBox);
	editorPanel.doLayout();
	cvComboBox.setPopupVisible(true);
	editorComponent = cvComboBox;
	final String currentCellString = cellValue[selectedField].toString();
	String initialValue = getEditorText(lastKeyInt, lastKeyChar, currentCellString);
	// Set the editor for the combobox, so that it supports typeahead
	final ControlledVocabularyComboBoxEditor cvcbEditor = new ControlledVocabularyComboBoxEditor(initialValue, currentCellString, (ArbilField) cellValue[selectedField], cvComboBox);
	cvComboBox.setEditor(cvcbEditor);
	cvComboBox.addPopupMenuListener(new PopupMenuListener() {
	    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	    }

	    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		fireEditingStopped();
	    }

	    public void popupMenuCanceled(PopupMenuEvent e) {
		//                            cvcbEditor.setText(currentCellString);
		fireEditingStopped();
	    }
	});
	// Make focus work (stop edit mode on lost focus)
	addFocusListener(cvComboBox);
	addFocusListener(cvComboBox.getEditor().getEditorComponent());
	cvComboBox.getEditor().getEditorComponent().requestFocusInWindow();
    }

    private void initFieldEditor(int lastKeyInt, char lastKeyChar) {
	// Remove 'button', which is the non-editor mode component for the cell
	editorPanel.remove(button);
	editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.X_AXIS));
	final String currentCellString = cellValue[selectedField].toString();
	ArbilFieldEditor editorTextField = new ArbilFieldEditor(getEditorText(lastKeyInt, lastKeyChar, currentCellString));
	editorTextField.addKeyListener(new java.awt.event.KeyListener() {
	    public void keyTyped(KeyEvent evt) {
		if (isStartLongFieldKey(evt)) {
		    // if this is a long start long field event the we don't want that key appended so it is not passed on here
		    if (cellValue instanceof ArbilField[]) {
			int currentFieldIndex = selectedField;
			if (currentFieldIndex < 0) {
			    currentFieldIndex = 0;
			}
			new ArbilLongFieldEditor(parentTable).showEditor((ArbilField[]) cellValue, getEditorText(KeyEvent.CHAR_UNDEFINED, KeyEvent.CHAR_UNDEFINED, ((ArbilField) cellValue[currentFieldIndex]).getFieldValue()), currentFieldIndex);
		    }
		}
	    }

	    public void keyPressed(KeyEvent evt) {
	    }

	    public void keyReleased(KeyEvent evt) {
	    }
	});
	editorPanel.setBorder(null);
	editorPanel.add(editorTextField);
	addFocusListener(editorTextField);
	if (cellValue[selectedField] instanceof ArbilField) {
	    if (((ArbilField) cellValue[selectedField]).isAllowsLanguageId()) {
		// this is an ImdiField that has a fieldLanguageId
		LanguageIdBox fieldLanguageBox = new LanguageIdBox((ArbilField) cellValue[selectedField], parentCellRect);
		fieldLanguageBox.init();
		editorPanel.add(fieldLanguageBox);
		addFocusListener(fieldLanguageBox);
	    }
	}
	editorPanel.doLayout();
	editorTextField.requestFocusInWindow();
	editorComponent = editorTextField;
    }

    private boolean requiresLongFieldEditor() {
	boolean requiresLongFieldEditor = false;
	if (cellValue instanceof ArbilField[]) {
	    Graphics g = button.getGraphics();
	    if (g != null) {
		try {
		    FontMetrics fontMetrics = g.getFontMetrics();
		    double availableWidth = parentCellRect.getWidth() + 20; // let a few chars over be ok for the short editor
		    ArbilField[] iterableFields;
		    if (selectedField == -1) { // when a single filed is edited only check that field otherwise check all fields
			iterableFields = (ArbilField[]) cellValue;
		    } else {
			iterableFields = new ArbilField[]{((ArbilField[]) cellValue)[selectedField]};
		    }
		    for (ArbilField currentField : iterableFields) {
//                if (!currentField.hasVocabulary()) { // the vocabulary type field should not get here
			String fieldValue = currentField.getFieldValue();
			// calculate length and look for line breaks
			if (fieldValue.contains("\n")) {
			    requiresLongFieldEditor = true;
			    break;
			} else {
			    int requiredWidth = fontMetrics.stringWidth(fieldValue);
			    if (currentField.isAllowsLanguageId()) {
				requiredWidth += LanguageIdBox.languageSelectWidth;
			    }
			    if (requiredWidth > availableWidth) {
				requiresLongFieldEditor = true;
				break;
			    }
			}
//                }
		    }
		} finally {
		    g.dispose();
		}
	    }
	}
	return requiresLongFieldEditor;
    }

    private boolean isCellEditable() {
	if (cellValue instanceof ArbilField[]) {
	    ArbilDataNode parentObject = ((ArbilField[]) cellValue)[0].getParentDataNode();
	    // check that the field id exists and that the file is in the local cache or in the favourites not loose on a drive, as the determinator of editability
	    return !parentObject.isLoading() && parentObject.isEditable() && parentObject.isMetaDataNode(); // todo: consider limiting editing to files withing the cache only
	}
	return false;
    }

    private boolean isStartLongFieldKey(KeyEvent evt) {
	return (isStartLongFieldModifier(evt) && (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER || evt.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE));
    }

    private boolean isStartLongFieldModifier(InputEvent evt) {
	return ((evt.isAltDown() || evt.isControlDown()));
    }

    private void removeAllFocusListeners() {
	while (componentsWithFocusListners.size() > 0) {
	    Component currentComponent = componentsWithFocusListners.remove(0);
	    if (currentComponent != null) {
		for (FocusListener currentListner : currentComponent.getFocusListeners()) {
		    currentComponent.removeFocusListener(currentListner);
		}
	    }
	}
    }

    private void addFocusListener(Component targetComponent) {
	componentsWithFocusListners.add(targetComponent);
	targetComponent.addFocusListener(new FocusListener() {
	    public void focusGained(FocusEvent e) {
	    }

	    public void focusLost(FocusEvent e) {
		try {
		    final boolean oppositeIsParent = (e.getComponent().equals(e.getOppositeComponent().getParent()) || e.getComponent().getParent().equals(e.getOppositeComponent()));
		    if (!oppositeIsParent && e.getComponent().getParent() != null) {
			if (!e.getOppositeComponent().getParent().equals(editorPanel)) {
			    ArbilTableCellEditor.this.stopCellEditing();
			}
		    }
		} catch (Exception ex) {
		    logger.debug("OppositeComponent or parent container not set");
		}

	    }
	});
    }

    private String getEditorText(int lastKeyInt, char lastKeyChar, String currentCellString) {
	switch (lastKeyInt) {
	    case KeyEvent.VK_DELETE:
		currentCellString = "";
		break;
	    case KeyEvent.VK_BACK_SPACE:
		currentCellString = "";
		break;
	    case KeyEvent.VK_INSERT:
		break;
	    case KeyEvent.VK_ESCAPE:
		break;
	    case KeyEvent.VK_NUM_LOCK:
		break;
	    case KeyEvent.VK_CAPS_LOCK:
		break;
	    case KeyEvent.CHAR_UNDEFINED:
		break;
	    case APPEND_TO_FIELD_ACTION_KEY:
		break;
	    default:
		if (lastKeyChar != KeyEvent.CHAR_UNDEFINED) {
		    currentCellString = "" + lastKeyChar;
		}
		break;
	}
	return currentCellString;
    }

    private boolean cellHasControlledVocabulary() {
	return ((ArbilField) cellValue[0]).hasVocabulary();
    }

    public void startLongfieldEditor(JTable table, Object value, boolean isSelected, int row, int column) {
	getTableCellEditorComponent(table, value, isSelected, row, column);
//        startEditorMode(true, KeyEvent.CHAR_UNDEFINED, KeyEvent.CHAR_UNDEFINED);
	fireEditingStopped();
	if (cellValue instanceof ArbilField[]) {
	    int currentFieldIndex = selectedField;
	    if (currentFieldIndex < 0) {
		currentFieldIndex = 0;
	    }
	    new ArbilLongFieldEditor(parentTable).showEditor((ArbilField[]) cellValue, getEditorText(KeyEvent.CHAR_UNDEFINED, KeyEvent.CHAR_UNDEFINED, ((ArbilField) cellValue[currentFieldIndex]).getFieldValue()), currentFieldIndex);
	}
    }

    private void startEditorMode(boolean startLongFieldEditor, int lastKeyInt, char lastKeyChar) {
	removeAllFocusListeners();
	if (cellValue instanceof ArbilField[]) {
	    if (cellHasControlledVocabulary() && isCellEditable()) {
		initControlledVocabularyEditor(lastKeyInt, lastKeyChar);
	    } else if (!startLongFieldEditor
		    && selectedField != -1
		    && isCellEditable()
		    && (!requiresLongFieldEditor() || getEditorText(lastKeyInt, lastKeyChar, "anystring").length() == 0)) { // make sure the long field editor is not shown when the contents of the field are being deleted
		initFieldEditor(lastKeyInt, lastKeyChar);
	    } else {
		fireEditingStopped();
		if (cellValue instanceof ArbilField[]) {
		    int currentFieldIndex = selectedField;
		    if (currentFieldIndex < 0) {
			currentFieldIndex = 0;
		    }
		    new ArbilLongFieldEditor(parentTable).showEditor((ArbilField[]) cellValue, getEditorText(lastKeyInt, lastKeyChar, ((ArbilField) cellValue[currentFieldIndex]).getFieldValue()), currentFieldIndex);
		}
//                ArbilTableCellEditor.this.stopCellEditing();
	    }
	} else if (cellValue instanceof ArbilDataNode[]) {
	    tableController.openNodesInNewTable((ArbilDataNode[]) cellValue, fieldName, registeredOwner);
	} else if (cellValue.length == 1 && cellValue[0] instanceof ArbilFieldPlaceHolder) {
	    // Grey cell editing is disabled in Arbil 2.5, incomplete functionality: https://trac.mpi.nl/ticket/3340
//	    final ArbilFieldPlaceHolder placeholder = (ArbilFieldPlaceHolder) cellValue[0];
//	    tableController.addFieldFromPlaceholder(parentTable, columnIndex, placeholder);
	} else {
	    BugCatcherManager.getBugCatcher().logError("Edit cell type not supported", null);
	}
    }

    public Object getCellEditorValue() {
	if (selectedField != -1) {
	    if (editorComponent != null) {
		if (editorComponent instanceof ControlledVocabularyComboBox) {
		    ((ArbilField[]) cellValue)[selectedField].setFieldValue(((ControlledVocabularyComboBox) editorComponent).getCurrentValue(), true, false);
		} else if (editorComponent instanceof ArbilFieldEditor) {
		    ((ArbilField[]) cellValue)[selectedField].setFieldValue(((ArbilFieldEditor) editorComponent).getText(), true, false);
		}
	    }
	    return cellValue[selectedField];
	} else {
	    return cellValue; // do we want to return the whole array here? without using clone?
	}
    }

    private void convertCellValue(ArbilTableCell cell) throws ArbilMetadataException {
	Object value = cell.getContent();
	if (value != null) {
	    if (value instanceof ArbilField) {
		// TODO: get the whole array from the parent and select the correct tab for editing
		fieldName = ((ArbilField) value).getTranslateFieldName();
		cellValue = ((ArbilField) value).getParentDataNode().getFields().get(fieldName);
		// TODO: find the chosen fields index in the array and store
		for (int cellFieldCounter = 0; cellFieldCounter < cellValue.length; cellFieldCounter++) {
		    if (cellValue[cellFieldCounter].equals(value)) {
			selectedField = cellFieldCounter;
		    }
		}
	    } else if (value instanceof Object[]) {
		cellValue = (Object[]) value;
		if (cellValue[0] instanceof ArbilField) {
		    fieldName = ((ArbilField[]) cellValue)[0].getTranslateFieldName();
		}
	    } else if (value instanceof ArbilFieldPlaceHolder) {
		fieldName = ((ArbilFieldPlaceHolder) value).getFieldName();
		cellValue = new Object[]{value};
	    } else {
		throw new ArbilMetadataException("Object type unsupported by cell editor");
	    }
	    if (cellValue != null && cellValue[0] instanceof ArbilField) {
		registeredOwner = ((ArbilField) cellValue[0]).getParentDataNode();
	    }
	} else {
	    logger.error("value is null in convertCellValue");
	}
    }

    @Override
    public Component getTableCellEditorComponent(JTable table,
	    Object valueObject,
	    boolean isSelected,
	    int row,
	    int column) {
//         TODO: something in this area is preventing the table selection change listener firing ergo the tree selection does not get updated (this symptom can also be caused by a node not being loaded into the tree)
//        receivedKeyDown = true;

	this.rowIndex = row;
	this.columnIndex = column;
	ArbilTableCell value = (ArbilTableCell) valueObject;

	parentTable = (ArbilTable) table;
	parentCellRect = parentTable.getCellRect(row, column, false);
	ArbilIconCellRenderer cellRenderer = // we call getTableCellRendererComponent so that the renderer gets updated to the required state
		(ArbilIconCellRenderer) arbilTableCellRenderer.getTableCellRendererComponent(table, valueObject, isSelected, true, row, column);
	try {
	    convertCellValue(value);
	} catch (ArbilMetadataException ex) {
	    // Value type not supported, simply return non-edit renderer
	    return cellRenderer;
	}

	// Create and add 'button', which is the non-editor mode component for the cell
	button.setText(arbilTableCellRenderer.getText());
	button.setForeground(arbilTableCellRenderer.getForeground());
	button.setIcon(arbilTableCellRenderer.getIcon());
	editorPanel.setBackground(table.getSelectionBackground());
	editorPanel.setLayout(new BorderLayout());
	editorPanel.add(button);
	addFocusListener(button);
	//table.requestFocusInWindow();
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		button.requestFocusInWindow();
	    }
	});

	if (cellValue instanceof ArbilField[]) {
	    Icon icon = (selectedField < 0) ? null : ArbilIcons.getSingleInstance().getIconForField((ArbilField) cellValue[selectedField]);
	    if (icon != null) {
		// Icon is available for field. Wrap editor panel with icon panel
		ArbilIconCellPanel panel = new ArbilIconCellPanel(editorPanel, icon);
		panel.addIconMouseListener(fieldMouseAdapter);
		return panel;
	    } else {
		return editorPanel;
	    }
	}
	return editorPanel;
    }
}
