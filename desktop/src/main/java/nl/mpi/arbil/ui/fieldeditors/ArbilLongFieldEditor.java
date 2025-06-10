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
package nl.mpi.arbil.ui.fieldeditors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.WindowManager;

/**
 * Document : ArbilLongFieldEditor
 * Created on : Sep 14, 2010, 1:53:15 PM
 * Author : Peter Withers, Twan Goosen
 */
public class ArbilLongFieldEditor extends JPanel implements ArbilDataNodeContainer {

    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");

    ArbilTable parentTable = null;
    ArbilDataNode parentArbilDataNode;
    ArbilField[] arbilFields;
    String fieldName = "unknown";
    JTabbedPane tabPane;
    int selectedField = -1;
//    Object[] cellValue;
    JTextField keyEditorFields[] = null;
    JComponent fieldEditors[] = null;
    LanguageIdBox fieldLanguageBoxs[] = null;
    JInternalFrame editorFrame = null;
    private FieldAttributesTableModel[] attributeTableModels;
    private JTable attributesTable[] = null;
    private JPanel contentPanel;
    private List<ArbilField[]> parentFieldList;
    private JButton prevButton;
    private JButton nextButton;
    private static ArbilWindowManager windowManager;

    public static void setWindowManager(WindowManager windowManagerInstance) {
	if (windowManagerInstance instanceof ArbilWindowManager) {
	    windowManager = (ArbilWindowManager) windowManagerInstance;
	} else {
	    throw new RuntimeException("Long field editor requires instance of ArbilWindowManager");
	}
    }
    private static MessageDialogHandler dialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler dialogHandlerInstance) {
	dialogHandler = dialogHandlerInstance;
    }

    public ArbilLongFieldEditor() {
	this(null);
    }

    public ArbilLongFieldEditor(ArbilTable parentTableLocal) {
	parentTable = parentTableLocal;

	setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, new lfeInputMap(getInputMap()));
	setActionMap(new lfeActionMap(getActionMap()));

	// Window has two panels: content panel and panel with previous/next buttons
	contentPanel = new JPanel();
	setLayout(new BorderLayout());
	contentPanel.setLayout(new BorderLayout());

	this.add(contentPanel, BorderLayout.CENTER);

	this.add(createPreviousNextPanel(), BorderLayout.PAGE_END);
    }

    public void showEditor(ArbilField[] cellValueLocal, String currentEditorText, int selectedFieldLocal) {
	selectedField = selectedFieldLocal;
	arbilFields = cellValueLocal;
	parentArbilDataNode = arbilFields[0].getParentDataNode();
	// todo: registerContainer should not be done on the parent node and the remove should scan all child nodes also, such that deleting a child like and actor would remove the correct nodes
	parentArbilDataNode.registerContainer(this);
	fieldName = arbilFields[0].getTranslateFieldName();

	setParentFieldList();

	contentPanel.removeAll();

	JLabel parentLabel = new JLabel(parentArbilDataNode.toString(), ArbilIcons.getSingleInstance().getIconForNode(parentArbilDataNode), SwingConstants.LEADING);
	parentLabel.setBorder(BorderFactory.createEmptyBorder(5, 2, 0, 0));
	contentPanel.add(parentLabel, BorderLayout.NORTH);

	tabPane = new JTabbedPane();
	final JComponent focusedTabTextArea = populateTabbedPane(currentEditorText);
	contentPanel.add(tabPane, BorderLayout.CENTER);

	// todo: add all unused attributes as editable text
	editorFrame = windowManager.createWindow(getWindowTitle(), this);
	editorFrame.addInternalFrameListener(new InternalFrameAdapter() {
	    @Override
	    public void internalFrameClosed(InternalFrameEvent e) {
		// deregister component from imditreenode
		parentArbilDataNode.removeContainer(ArbilLongFieldEditor.this);
		super.internalFrameClosed(e);
		if (parentTable != null) {
		    parentTable.requestFocusInWindow();
		}
	    }
	});
	if (selectedField != -1) {
	    tabPane.setSelectedIndex(selectedField);
	} else {
	    tabPane.setSelectedIndex(0);
	}
	setNavigationEnabled();
	requestFocusFor(focusedTabTextArea);
    }

    private JPanel createLanguageBox(final int cellFieldIndex) {
	fieldLanguageBoxs[cellFieldIndex] = new LanguageIdBox(arbilFields[cellFieldIndex], null);
	fieldLanguageBoxs[cellFieldIndex].init();
	JPanel languagePanel = new JPanel(new BorderLayout());
	languagePanel.add(new JLabel("Language:"), BorderLayout.LINE_START);
	if (parentArbilDataNode.getParentDomNode().isEditable()) {
	    languagePanel.add(fieldLanguageBoxs[cellFieldIndex], BorderLayout.CENTER);
	} else {
	    languagePanel.add(new JLabel(fieldLanguageBoxs[cellFieldIndex].getSelectedItem().toString()), BorderLayout.CENTER);
	}
	return languagePanel;
    }

    private JComponent populateTabbedPane(String currentEditorText) {
	//int titleCount = 1;
	JComponent focusComponent = null;

	fieldEditors = new JComponent[arbilFields.length];
	keyEditorFields = new JTextField[arbilFields.length];
	fieldLanguageBoxs = new LanguageIdBox[arbilFields.length];
	attributeTableModels = new FieldAttributesTableModel[arbilFields.length];
	attributesTable = new JTable[arbilFields.length];

	for (int cellFieldIndex = 0; cellFieldIndex < arbilFields.length; cellFieldIndex++) {
	    if (arbilFields[cellFieldIndex].hasVocabulary()) {
		fieldEditors[cellFieldIndex] = new ControlledVocabularyComboBox(arbilFields[cellFieldIndex]);
	    } else {
		fieldEditors[cellFieldIndex] = new JTextArea();
	    }
	    if (focusComponent == null || selectedField == cellFieldIndex) {
		// set the selected field as the first one or in the case of a single node being selected tab to its pane
		focusComponent = fieldEditors[cellFieldIndex];
	    }
	    if (arbilFields[cellFieldIndex].hasEditableFieldAttributes()) {
		attributeTableModels[cellFieldIndex] = new FieldAttributesTableModel(cellFieldIndex);
	    }

	    JPanel tabPanel = createTabPanel(cellFieldIndex, currentEditorText, createFieldDescription(cellFieldIndex));
	    tabPane.add(fieldName + ((arbilFields.length <= 1) ? "" : " " + (cellFieldIndex + 1)), tabPanel);
	}

	// If field has language attribute but no language has been chosen yet, request focus on the language select drop down
	if (arbilFields[selectedField].isAllowsLanguageId()
		&& (arbilFields[selectedField].getLanguageId() == null || arbilFields[selectedField].getLanguageId().length() == 0)) {
	    return fieldLanguageBoxs[selectedField];
	} else {
	    return focusComponent;
	}
    }

    private JPanel createTabPanel(final int cellFieldIndex, String currentEditorText, JComponent fieldDescription) {
	JPanel tabPanel = new JPanel();
	JPanel tabTitlePanel = new JPanel();
	tabPanel.setLayout(new BorderLayout());
	tabTitlePanel.setLayout(new BoxLayout(tabTitlePanel, BoxLayout.PAGE_AXIS));

	tabTitlePanel.add(fieldDescription);

	initFieldEditor(cellFieldIndex, currentEditorText);

	//fieldLanguageBoxs[cellFieldIndex] = null;
	if (arbilFields[cellFieldIndex].isAllowsLanguageId()) {
	    tabTitlePanel.add(createLanguageBox(cellFieldIndex));
	}

	if (arbilFields[cellFieldIndex].getKeyName() != null) {
	    tabTitlePanel.add(new KeyPanel(cellFieldIndex));
	}

	tabPanel.add(tabTitlePanel, BorderLayout.PAGE_START);

	JPanel editorPanel = new JPanel(new BorderLayout());
	if (fieldEditors[cellFieldIndex] instanceof JTextArea) {
	    editorPanel.add(new JScrollPane(fieldEditors[cellFieldIndex]), BorderLayout.CENTER);
	} else if (fieldEditors[cellFieldIndex] instanceof ControlledVocabularyComboBox) {
	    editorPanel.add(fieldEditors[cellFieldIndex], BorderLayout.PAGE_START);
	}
	tabPanel.add(editorPanel, BorderLayout.CENTER);

	if (arbilFields[cellFieldIndex].hasEditableFieldAttributes()) {
	    attributesTable[cellFieldIndex] = new JTable(attributeTableModels[cellFieldIndex]);

	    JScrollPane tablePane = new JScrollPane(attributesTable[cellFieldIndex]);
	    tablePane.setPreferredSize(new Dimension(this.getWidth(), 100));
	    tabPanel.add(tablePane, BorderLayout.SOUTH);
	} else {
	    attributesTable[cellFieldIndex] = null;
	}

	return tabPanel;
    }

    private JComponent createFieldDescription(int cellFieldIndex) {
	// Create actual description textarea
	final JTextArea fieldDescription = new JTextArea();
	fieldDescription.setLineWrap(true);
	fieldDescription.setEditable(false);
	fieldDescription.setOpaque(false);

	final String fullXmlPath = arbilFields[cellFieldIndex].getFullXmlPath();

	// Start separate thread to get the help string as this may involve a http request and parsing, don't want to wait for that...
	new Thread() {
	    @Override
	    public void run() {
		final String helpString = parentArbilDataNode.getNodeTemplate().getHelpStringForField(fullXmlPath);
		// Set field description on event dispatching thread
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			fieldDescription.setText(helpString);
		    }
		});
	    }
	}.start();

	JComponent component = fieldDescription;
	// Try to find an icon for the field
	Icon icon = ArbilIcons.getSingleInstance().getIconForField(arbilFields[cellFieldIndex]);
	if (icon != null) {
	    // Add description text and icon to a panel
	    JPanel panel = new JPanel(new BorderLayout());
	    JLabel iconLabel = new JLabel(icon);
	    iconLabel.setVerticalAlignment(SwingConstants.TOP);
	    panel.add(fieldDescription, BorderLayout.CENTER);
	    panel.add(iconLabel, BorderLayout.LINE_END);
	    component = panel;
	}

	// Apply some padding to the component (i.e. text area or iconed panel)
	component.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
	return component;
    }

    private void initFieldEditor(final int cellFieldIndex, String currentEditorText) {
	String text = currentEditorText;
	if ((currentEditorText == null || !(selectedField == cellFieldIndex || (selectedField == -1 && cellFieldIndex == 0)))) {
	    text = arbilFields[cellFieldIndex].getFieldValue();
	}

	if (fieldEditors[cellFieldIndex] instanceof JTextArea) {
	    JTextArea fieldEditor = (JTextArea) fieldEditors[cellFieldIndex];

	    fieldEditor.setEditable(parentArbilDataNode.getParentDomNode().isEditable());
	    // insert the last key for only the selected field
	    fieldEditor.setText(text);
	    fieldEditor.setLineWrap(true);
	    fieldEditor.setWrapStyleWord(true);
	} else {
	    ControlledVocabularyComboBox fieldEditor = (ControlledVocabularyComboBox) fieldEditors[cellFieldIndex];
	    ControlledVocabularyComboBoxEditor cvcbEditor = new ControlledVocabularyComboBoxEditor(text, text, arbilFields[cellFieldIndex], fieldEditor);
	    fieldEditor.setEditor(cvcbEditor);

	    cvcbEditor.getEditorComponent().addFocusListener(editorFocusListener);
	    cvcbEditor.getTextField().setInputMap(JComponent.WHEN_FOCUSED, new lfeInputMap(cvcbEditor.getTextField().getInputMap()));
	    cvcbEditor.getTextField().setActionMap(new lfeActionMap(cvcbEditor.getTextField().getActionMap()));

	    fieldEditor.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	}
	fieldEditors[cellFieldIndex].addFocusListener(editorFocusListener);
	fieldEditors[cellFieldIndex].setInputMap(JComponent.WHEN_FOCUSED, new lfeInputMap(fieldEditors[cellFieldIndex].getInputMap()));
	fieldEditors[cellFieldIndex].setActionMap(new lfeActionMap(fieldEditors[cellFieldIndex].getActionMap()));
    }

    private String getWindowTitle() {
	return fieldName + " in " + String.valueOf(parentArbilDataNode);
    }

    private JPanel createPreviousNextPanel() {
	prevButton = new JButton(previousAction);
	prevButton.setText(widgets.getString("PREVIOUS"));
	prevButton.setMnemonic('p');

	nextButton = new JButton(nextAction);
	nextButton.setText(widgets.getString("NEXT"));
	nextButton.setMnemonic('n');

	JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
	buttonsPanel.add(prevButton);
	buttonsPanel.add(nextButton);
	return buttonsPanel;
    }

    private void setParentFieldList() {
	parentFieldList = parentArbilDataNode.getFieldsSorted();
    }

    private void setNavigationEnabled(boolean enabled) {
	nextButton.setEnabled(enabled);
	prevButton.setEnabled(enabled);
    }

    private void setNavigationEnabled() {
	int index = parentFieldList.indexOf(arbilFields);
	nextButton.setEnabled(index < parentFieldList.size() - 1);
	prevButton.setEnabled(index > 0);
    }

    private void requestFocusFor(final JComponent component) {
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		component.requestFocusInWindow();
	    }
	});
    }

    public void updateEditor() {
	arbilFields = parentArbilDataNode.getFields().get(fieldName);
	selectedField = Math.max(0, Math.min(tabPane.getTabCount(), tabPane.getSelectedIndex()));

	tabPane.removeAll();
	if (arbilFields != null && arbilFields.length > 0) {
	    editorFrame.setTitle(getWindowTitle());
	    JComponent focusedTabTextArea = populateTabbedPane(null);
//            fieldDescription.setText(parentArbilDataNode.getNodeTemplate().getHelpStringForField(arbilFields[0].getFullXmlPath()));
	    requestFocusFor(focusedTabTextArea);
	    if (selectedField < tabPane.getTabCount()) {
		tabPane.setSelectedIndex(selectedField);
	    }
	}

	// todo: this could be done more softly by using the below code when the fields have not been reloaded, but be carefull that the ImdiFields in the language boxes are not out of sync.
//        // todo: the number of fields might have changed so we really should update the tabs or re create the form
//        // this will only be called when the long field editor is shown
//        // when an imdi node is edited or saved or reloaded this will be called to update the displayed values
//        fieldDescription.setText(parentImdiTreeObject.getNodeTemplate().getHelpStringForField(imdiFields[0].getFullXmlPath()));
//        for (int cellFieldCounter = 0; cellFieldCounter < imdiFields.length; cellFieldCounter++) {
//            fieldEditors[cellFieldCounter].setText(imdiFields[cellFieldCounter].getFieldValue());
//            if (fieldLanguageBoxs[cellFieldCounter] != null) {
//                // set the language id selection in the dropdown
//                boolean selectedValueFound = false;
//                for (int itemCounter = 0; itemCounter < fieldLanguageBoxs[cellFieldCounter].getItemCount(); itemCounter++) {
//                    Object currentObject = fieldLanguageBoxs[cellFieldCounter].getItemAt(itemCounter);
//                    if (currentObject instanceof ImdiVocabularies.VocabularyItem) {
//                        ImdiVocabularies.VocabularyItem currentItem = (ImdiVocabularies.VocabularyItem) currentObject;
////                        logger.debug(((ImdiField[]) cellValue)[cellFieldCounter].getLanguageId());
////                            logger.debug(currentItem.languageCode);
//                        if (currentItem.languageCode.equals(imdiFields[cellFieldCounter].getLanguageId())) {
////                            logger.debug("setting as current");
////                            logger.debug(currentItem.languageCode);
////                            logger.debug(imdiFields[cellFieldCounter].getLanguageId());
//                            fieldLanguageBoxs[cellFieldCounter].setSelectedIndex(itemCounter);
//                            selectedValueFound = true;
//                        }
//                    }
//                }
//                if (selectedValueFound == false) {
//                    fieldLanguageBoxs[cellFieldCounter].addItem(LanguageIdBox.defaultLanguageDropDownValue);
//                    fieldLanguageBoxs[cellFieldCounter].setSelectedItem(LanguageIdBox.defaultLanguageDropDownValue);
//                }
////                    logger.debug("field language: " + ((ImdiField[]) cellValue)[cellFieldCounter].getLanguageId());
//            }
//            if (keyEditorFields[cellFieldCounter] != null) {
//                keyEditorFields[cellFieldCounter].setText(imdiFields[cellFieldCounter].getKeyName());
//            }
//        }
    }

    public void closeWindow() {
	editorFrame.doDefaultCloseAction();
    }

    public synchronized void storeChanges() {
	if (arbilFields != null) {
	    for (int cellFieldCounter = 0; cellFieldCounter < arbilFields.length; cellFieldCounter++) {
		checkSaveCellField(cellFieldCounter);
	    }
	}
    }

    private void checkSaveCellField(int cellFieldIndex) {
	final ArbilField cellField = (ArbilField) arbilFields[cellFieldIndex];
	if (cellField.getParentDataNode().getParentDomNode().isEditable()) {
	    if (fieldEditors[cellFieldIndex] instanceof JTextArea) {
		cellField.setFieldValue(((JTextArea) fieldEditors[cellFieldIndex]).getText(), true, false);
	    } else if (fieldEditors[cellFieldIndex] instanceof ControlledVocabularyComboBox) {
		cellField.setFieldValue(((ControlledVocabularyComboBox) fieldEditors[cellFieldIndex]).getCurrentValue(), true, false);
	    }
	}
    }

    /**
     * Checks whether the key editor field has been changed, if so asks the user to go ahead and save pending changes, and finally
     * saves changes if the user agrees
     *
     * @param cellFieldIndex
     * @return False if the user has canceled save changes. True otherwise (i.e. there are no changes or user agrees to save)
     */
    private boolean checkSaveKeyEditorField(int cellFieldIndex) {
	final ArbilField cellField = (ArbilField) arbilFields[cellFieldIndex];
	if (!keyEditorFields[cellFieldIndex].getText().equals(arbilFields[cellFieldIndex].getKeyName())) {
	    // Changing the key will save to disk. Warn user
	    if (dialogHandler.askUserToSaveChanges(arbilFields[cellFieldIndex].getParentDataNode().getParentDomNode().toString())) {
		// Remember index of current field
		int index = parentFieldList.indexOf(arbilFields);
		// Let field apply new name if necessary (true,false = Request update UI, don't exclude from undo history)
		if (cellField.setKeyName(keyEditorFields[cellFieldIndex].getText(), true, false)) {
		    // Field name has changed, reload as required
		    setParentFieldList();
		    moveTo(index);
		}
	    } else {
		return false;
	    }
	}
	return true;
    }

    public boolean isFullyLoadedNodeRequired() {
	return true;
    }

    /**
     * Data node is to be removed
     *
     * @param dataNode Data node that should be removed
     */
    public void dataNodeRemoved(ArbilNode dataNode) {
	closeWindow();
    }

    /**
     * Data node is clearing its icon
     *
     * @param dataNode Data node that is clearing its icon
     */
    public void dataNodeIconCleared(ArbilNode dataNode) {
	updateEditor();
	setParentFieldList();
	setNavigationEnabled();
    }

    /**
     * A new child node has been added to the destination node
     *
     * @param destination Node to which a node has been added
     * @param newNode The newly added node
     */
    public void dataNodeChildAdded(ArbilNode destination, ArbilNode newNode) {
	// Nothing to do(?)
    }

    private synchronized void changeTab(int d) {
	final int index;
	if (d > 0) {
	    index = Math.min(tabPane.getSelectedIndex() + 1, tabPane.getTabCount() - 1);
	} else {
	    index = Math.max(tabPane.getSelectedIndex() - 1, 0);
	}
	tabPane.setSelectedIndex(index);
	requestFocusFor(fieldEditors[index]);
    }

    private synchronized void moveAdjacent(int d) {
	stopAttributesEditing();

	int index = parentFieldList.indexOf(arbilFields) + d;
	if (index < parentFieldList.size() && index >= 0) {
	    storeChanges();
	    moveTo(index);
	}
    }

    private void stopAttributesEditing() {
	// Stop editing in all fields' attribute tables
	if (attributesTable != null) {
	    for (int fieldIndex = 0; fieldIndex < arbilFields.length; fieldIndex++) {
		if (attributesTable[fieldIndex] != null && attributesTable[fieldIndex].getCellEditor() != null) {
		    attributesTable[fieldIndex].getCellEditor().stopCellEditing();
		}
	    }
	}
    }

    private synchronized void moveTo(int index) {
	fieldName = parentFieldList.get(index)[0].getTranslateFieldName();
	updateEditor();
	setNavigationEnabled();
    }
    private Action nextAction = new AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	    moveAdjacent(+1);
	}
    };
    private Action previousAction = new AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	    moveAdjacent(-1);
	}
    };

    private class lfeActionMap extends ActionMap {

	public lfeActionMap(ActionMap parent) {
	    super();
	    if (parent != null) {
		setParent(parent);
	    }

	    put("nextField", nextAction);
	    put("previousField", previousAction);

	    // Action that switches to the next tab (if there is one)
	    put("nextTab", new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    changeTab(+1);
		}
	    });

	    // Action that switches to the previous tab (if there is one)
	    put("previousTab", new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    changeTab(-1);
		}
	    });
	}
    }

    private static class lfeInputMap extends InputMap {

	public lfeInputMap(InputMap parent) {
	    super();
	    if (parent != null) {
		setParent(parent);
	    }

	    // Next field keys
	    put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.META_DOWN_MASK), "nextField");
	    put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.META_DOWN_MASK), "nextField");
	    put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK), "nextField");
	    put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_DOWN_MASK), "nextField");

	    // Previous field keys
	    put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.META_DOWN_MASK), "previousField");
	    put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.META_DOWN_MASK), "previousField");
	    put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK), "previousField");
	    put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.CTRL_DOWN_MASK), "previousField");

	    // Next tab keys
	    put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.META_DOWN_MASK), "nextTab");
	    put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK), "nextTab");

	    // Previous tab keys
	    put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.META_DOWN_MASK), "previousTab");
	    put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK), "previousTab");
	}
    }
    // NOTE: Not serializable!
    private FocusListener editorFocusListener = new FocusListener() {
	public void focusGained(FocusEvent e) {
	}

	public void focusLost(FocusEvent e) {
	    storeChanges();
	}
    };
    private final static int ATTR_NAME_COLUMN = 0;
    private final static int ATTR_VALUE_COLUMN = 1;
    private final static String[] ATTR_COLUMN_NAMES = new String[]{widgets.getString("LONGFIELD_ATTRIBUTE"), widgets.getString("LONGFIED_ATTRIBUTE_VALUE")};
    private final static Class[] ATTR_COLUMN_TYPES = new Class[]{String.class, Object.class};

    private class FieldAttributesTableModel implements TableModel {

	private List<String[]> attributePaths;
	private int cellFieldIndex;

	public FieldAttributesTableModel(int cellFieldIndex) {
	    this.cellFieldIndex = cellFieldIndex;
	    attributePaths = arbilFields[cellFieldIndex].getAttributePaths();
	}

	public int getRowCount() {
	    return attributePaths.size();
	}

	public int getColumnCount() {
	    return 2;
	}

	public String getColumnName(int columnIndex) {
	    return ATTR_COLUMN_NAMES[columnIndex];
	}

	public Class<?> getColumnClass(int columnIndex) {
	    return ATTR_COLUMN_TYPES[columnIndex];
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
	    return columnIndex == ATTR_VALUE_COLUMN; // Values are editable
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
	    final String path[] = attributePaths.get(rowIndex);
	    switch (columnIndex) {
		case ATTR_NAME_COLUMN:
		    return path[1]; // description
		case ATTR_VALUE_COLUMN:
		    return arbilFields[cellFieldIndex].getAttributeValue(path[0]);
		default:
		    return null;
	    }
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	    if (columnIndex == ATTR_VALUE_COLUMN) {
		final String path[] = attributePaths.get(rowIndex);
		arbilFields[cellFieldIndex].setAttributeValue(path[0], aValue, true);
	    }
	}

	public void addTableModelListener(TableModelListener l) {
	}

	public void removeTableModelListener(TableModelListener l) {
	}
    }

    /**
     * Panel for showing/editing the key name for fields with a custom key name
     */
    private class KeyPanel extends JPanel {

	private final int cellFieldIndex;
	private JButton changeKeyNameButton;
	private JPanel changeActionButtonsPanel;
	private JTextField keyEditorField;
	private JLabel keyNameLabel;

	public KeyPanel(final int cellFieldIndex) {
	    super(new BorderLayout());

	    this.cellFieldIndex = cellFieldIndex;

	    initComponents();
	    addComponents();
	}

	private void initComponents() {
	    keyNameLabel = new JLabel(arbilFields[cellFieldIndex].getKeyName());
	    initKeyEditorField(cellFieldIndex);
	    initChangeKeyNameButton();
	    initActionButtonsPanel();
	}

	private void addComponents() {
	    add(new JLabel(widgets.getString("KEY NAME: ")), BorderLayout.LINE_START);
	    add(keyNameLabel, BorderLayout.CENTER);
	    add(changeKeyNameButton, BorderLayout.LINE_END);
	}

	private void initActionButtonsPanel() {
	    JButton saveFieldNameButton = new JButton(keyNameSaveAction);
	    saveFieldNameButton.setText(widgets.getString("APPLY"));
	    JButton cancelFieldNameButton = new JButton(keyNameCancelAction);
	    cancelFieldNameButton.setText(widgets.getString("CANCEL"));
	    changeActionButtonsPanel = new JPanel(new BorderLayout());
	    changeActionButtonsPanel.add(saveFieldNameButton, BorderLayout.LINE_START);
	    changeActionButtonsPanel.add(cancelFieldNameButton, BorderLayout.LINE_END);
	}

	private void initChangeKeyNameButton() {
	    changeKeyNameButton = new JButton(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    enableEditMode();
		}
	    });
	    changeKeyNameButton.setText(widgets.getString("CHANGE KEY NAME"));
	    changeKeyNameButton.setMnemonic('c');
	}

	private void initKeyEditorField(final int cellFieldIndex) {
	    keyEditorField = new JTextField(arbilFields[cellFieldIndex].getKeyName());
	    keyEditorField.setEditable(parentArbilDataNode.getParentDomNode().isEditable());

	    // Add keyboard actions
	    keyEditorField.getActionMap().put("KeyNameOk", keyNameSaveAction);
	    keyEditorField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), "KeyNameOk");
	    keyEditorField.getActionMap().put("KeyNameCancel", keyNameCancelAction);
	    keyEditorField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true), "KeyNameCancel");

	    // Add editor field to long field editor model, so that it can access it for saving the value
	    keyEditorFields[cellFieldIndex] = keyEditorField;
	}
	/**
	 * Action that saves the entered key name
	 */
	private Action keyNameSaveAction = new AbstractAction("KeyNameOk") {
	    public void actionPerformed(ActionEvent e) {
		if (checkSaveKeyEditorField(cellFieldIndex)) {
		    disableEditMode();
		}
	    }
	};
	/**
	 * Action that cancels the editing of the key name
	 */
	private Action keyNameCancelAction = new AbstractAction("KeyNameCancel") {
	    public void actionPerformed(ActionEvent e) {
		disableEditMode();
	    }
	};

	private void enableEditMode() {
	    keyNameLabel.setVisible(false);
	    remove(keyNameLabel);
	    changeKeyNameButton.setVisible(false);
	    remove(changeKeyNameButton);

	    keyEditorField.setText(arbilFields[cellFieldIndex].getKeyName());

	    add(keyEditorField, BorderLayout.CENTER);
	    keyEditorField.setVisible(true);
	    add(changeActionButtonsPanel, BorderLayout.LINE_END);
	    changeActionButtonsPanel.setVisible(true);

	    validate();
	    keyEditorField.requestFocusInWindow();

	    setNavigationEnabled(false);
	}

	private void disableEditMode() {
	    keyEditorField.setVisible(false);
	    remove(keyEditorField);
	    changeActionButtonsPanel.setVisible(false);
	    remove(changeActionButtonsPanel);

	    keyNameLabel.setText(arbilFields[cellFieldIndex].getKeyName());

	    add(keyNameLabel, BorderLayout.CENTER);
	    keyNameLabel.setVisible(true);
	    add(changeKeyNameButton, BorderLayout.LINE_END);
	    changeKeyNameButton.setVisible(true);

	    validate();
	    setNavigationEnabled(true);
	}
    }
}
