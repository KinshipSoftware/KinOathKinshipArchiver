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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.ResourceBundle;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ArbilTableCell;
import nl.mpi.arbil.util.ApplicationVersion;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilActionBuffer;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of AbstractArbilTableModel for the Swing UI
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTableModel extends AbstractArbilTableModel implements ClipboardOwner {

    private final static Logger logger = LoggerFactory.getLogger(ArbilTableModel.class);
    public static final char CSV_NEWLINE = '\n';
    public static final String CSV_SEPARATOR = "\t"; // excel seems to work with tab but not comma
    public static final String CSV_DOUBLE_QUOTE = "\"\"";
    public static final char CSV_QUOTE = '\"';
    public static final String CSV_QUOTE_STRING = "\"";
    private Hashtable<String, ArbilDataNode> dataNodeHash = new Hashtable<String, ArbilDataNode>();
    private ArbilTableCell[][] data = new ArbilTableCell[0][0];
    private DefaultListModel listModel = new DefaultListModel(); // used by the image display panel
    private boolean widthsChanged = true;
    private JLabel hiddenColumnsLabel;
    public boolean hideContextMenuAndStatusBar;
    private final ImageBoxRenderer imageBoxRenderer;
    // Handlers to be injected
    protected static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }
    private static ApplicationVersionManager versionManager;
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");

    public static void setVersionManager(ApplicationVersionManager versionManagerInstance) {
	versionManager = versionManagerInstance;
    }
    // End handlers

    public ArbilTableModel(ImageBoxRenderer imageBoxRenderer) {
	super(ArbilFieldViews.getSingleInstance().getCurrentGlobalView().clone());
	this.imageBoxRenderer = imageBoxRenderer;
    }

    public ArbilTableModel(ArbilFieldView tableFieldView, ImageBoxRenderer imageBoxRenderer) {
	super(tableFieldView);
	this.imageBoxRenderer = imageBoxRenderer;
    }

    public DefaultListModel getListModel(ArbilSplitPanel arbilSplitPanel) {
	ArbilListDataListener listDataListener = new ArbilListDataListener(arbilSplitPanel);
	listModel.addListDataListener(listDataListener);
	return listModel;
    }
    // end code related to the list display of resources and loose files

    private void updateImageDisplayPanel() {
	listModel.removeAllElements();
	if (imageBoxRenderer != null) {
	    for (int rowCounter = 0; rowCounter < data.length; rowCounter++) {
		ArbilDataNode currentRowDataNode = getDataNodeFromRow(rowCounter);
		if (currentRowDataNode != null) {
		    if (imageBoxRenderer.canDisplay(currentRowDataNode)) {
			if (!listModel.contains(currentRowDataNode)) {
			    listModel.addElement(currentRowDataNode);
			}
		    }
		}
	    }
	}
    }

    public void setHiddenColumnsLabel(JLabel hiddenColumnsLabelLocal) {
	hiddenColumnsLabel = hiddenColumnsLabelLocal;
    }

    @Override
    protected void updateHiddenColumnsLabel(final int hiddenColumnCount, final int hiddenCellsCount) {
	if (hiddenColumnsLabel != null) {
	    SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    hiddenColumnsLabel.setVisible(!hideContextMenuAndStatusBar && hiddenColumnCount > 0);
		    hiddenColumnsLabel.setText(hiddenCellsCount + " cells hidden in " + hiddenColumnCount + " columns (edit \"Column View\" in the table header to show)");
		}
	    });
	}
    }

    /**
     * utility to join an array to a comma separated string
     *
     * @param arrayToJoin
     * @return
     */
    private String joinArray(Object[] arrayToJoin) {
	StringBuilder joinedString = new StringBuilder();
	boolean first = true;
	for (Object currentArrayItem : arrayToJoin) {
	    if (!first) {
		joinedString.append(',');
	    }
	    first = false;
	    joinedString.append(currentArrayItem.toString());
	}
	return joinedString.toString();
    }

    public void copyHtmlEmbedTagToClipboard(int tableHeight, int tableWidth) {
	try {
	    final ApplicationVersion appVersion = versionManager.getApplicationVersion();
	    // Construct HTML for embedding the applet
	    final StringBuilder embadTagStringBuilder = new StringBuilder();
	    embadTagStringBuilder.append("<applet codebase=\"http://www.mpi.nl/tg/j2se/jnlp/arbil/\"\n");
	    // Main class to run
	    embadTagStringBuilder.append(" code=\"nl.mpi.arbil.ui.applet.ArbilTableApplet.class\"\n");
	    // Define class path jars, first arbil main jar
	    embadTagStringBuilder.append(" archive=\"arbil-").append(appVersion.branch).append("-").append(appVersion.currentMajor).append("-").append(appVersion.currentMinor).append("-").append(appVersion.currentRevision).append(".jar,");
	    // Add all jars in current classpath, except those in the exlude pattern defined in this class
	    embadTagStringBuilder.append(
		    "lib/xmlbeans-2.6.0.jar,"
		    + "lib/stax-api-1.0.1.jar,"
		    + "lib/typechecker-1.7.0.jar,"
		    + "lib/slcshttps-0.2.jar,"
		    + "lib/imdi-api-1.1.2.jar,"
		    + "lib/mpi-util-1.0.0.jar,"
		    + "lib/Saxon-HE-9.4.jar,"
		    + "lib/jdom-1.1.jar,"
		    + "lib/xom-1.2.5.jar,"
		    + "lib/dom4j-1.6.1.jar,"
		    + "lib/xml-resolver-1.2.jar,"
		    + "lib/xercesImpl-2.9.0.jar,"
		    + "lib/xml-apis-1.3.04.jar,"
		    + "lib/xalan-2.7.1.jar,"
		    + "lib/serializer-2.7.1.jar,"
		    + "lib/commons-digester-2.0.jar,"
		    + "lib/commons-beanutils-1.8.0.jar,"
		    + "lib/commons-logging-1.1.1.jar,"
		    + "lib/corpusstructure-api-1.7.3.jar,"
		    + "lib/handle-6.1.jar,"
		    + "lib/commons-io-1.4.jar,"
		    + "lib/plugins-core-1.2.39113-testing,"
		    + "lib/2.5.39486-testing.jar,"
		    + "lib/arbil-localisation-2.5.39295-testing.jar"
		    + "\"\n");
	    embadTagStringBuilder.append(String.format(" width=\"%d\" height=\"%d\" >\n", tableWidth, tableHeight));
	    // Application parameters
	    embadTagStringBuilder.append(" <param name=\"ImdiFileList\" value=\"").append(joinArray(this.getArbilDataNodesURLs())).append("\">\n");
	    embadTagStringBuilder.append(" <param name=\"ShowOnlyColumns\" value=\"").append(joinArray(getColumnNames())).append("\">\n");
	    embadTagStringBuilder.append(" <param name=\"ChildNodeColumns\" value=\"").append(joinArray(getChildColumnNames().toArray())).append("\">\n");
	    embadTagStringBuilder.append(" <param name=\"HighlightText\" value=\"").append(joinArray(getHighlightCells().toArray())).append("\">\n");
	    embadTagStringBuilder.append("</applet>");

	    final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    StringSelection stringSelection = new StringSelection(embadTagStringBuilder.toString());
	    clipboard.setContents(stringSelection, this);
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
    }

    /**
     * Data node is to be removed from the table
     *
     * @param dataNode Data node that should be removed
     */
    public void dataNodeRemoved(ArbilNode dataNode) {
	if (dataNode instanceof ArbilDataNode) {
	    removeArbilDataNodes(new ArbilDataNode[]{(ArbilDataNode) dataNode});
	} else {
	    throw new UnsupportedOperationException("Cannot remove an ArbilNode from the table");
	}
    }

    /**
     * Data node is clearing its icon
     *
     * @param dataNode Data node that is clearing its icon
     */
    public void dataNodeIconCleared(ArbilNode dataNode) {
	requestReloadTableData();
    }

    /**
     * A new child node has been added to the destination node
     *
     * @param destination Node to which a node has been added
     * @param newNode The newly added node
     */
    public void dataNodeChildAdded(ArbilNode destination, ArbilNode newNode) {
	// Nothing to do
    }

    /**
     * @return the widthsChanged
     */
    public boolean isWidthsChanged() {
	return widthsChanged;
    }

    /**
     * @param widthsChanged the widthsChanged to set
     */
    public void setWidthsChanged(boolean widthsChanged) {
	this.widthsChanged = widthsChanged;
    }

    /**
     * @return the dataNodeHash
     */
    protected Hashtable<String, ArbilDataNode> getDataNodeHash() {
	return dataNodeHash;
    }
    // NOTE: ArbilActionBuffer is not serializable but ArbilTableModel should be!
    private ArbilActionBuffer reloadRunner = new ArbilActionBuffer("TableReload-" + this.hashCode(), 50) {
	@Override
	public void executeAction() {
	    reloadTableDataPrivate();
	}
    };

    @Override
    protected synchronized void reloadTableDataPrivate() {
	super.reloadTableDataPrivate();
	setWidthsChanged(false);
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		updateImageDisplayPanel(); // update the image panel now the rows have been sorted and the data array updated
	    }
	});
    }

    public void requestReloadTableData() {
	reloadRunner.requestActionAndNotify();
    }

    public void setPreferredColumnWidth(String columnName, Integer width) {
	Integer currentWidth = getFieldView().getColumnWidth(columnName);
	if (!isWidthsChanged()
		&& !(width == null && currentWidth == null || width != null && width.equals(currentWidth))) {
	    setWidthsChanged(true);
	    fireTableStructureChanged();
	}
	getFieldView().setColumnWidth(columnName, width);
    }

    public Integer getPreferredColumnWidth(String columnName) {
	return getFieldView().getColumnWidth(columnName);
    }

    public void copyArbilRows(int[] selectedRows) {
	int firstColumn = 0;
	if (isShowIcons() && isHorizontalView()) {
	    // horizontalView excludes icon display
	    firstColumn = 1;
	}
	// add the headers
	final int columnCount = getColumnCount();
	final StringBuilder copiedString = new StringBuilder();
	for (int selectedColCounter = firstColumn; selectedColCounter < columnCount; selectedColCounter++) {
	    copiedString.append(CSV_QUOTE).append(getColumnName(selectedColCounter)).append(CSV_QUOTE);
	    if (selectedColCounter < columnCount - 1) {
		copiedString.append(CSV_SEPARATOR);
	    }
	}
	copiedString.append(CSV_NEWLINE);
	// add the cell data
	for (int selectedRowCounter = 0; selectedRowCounter < selectedRows.length; selectedRowCounter++) {
	    logger.debug("copying row: {}", selectedRowCounter);
	    for (int selectedColCounter = firstColumn; selectedColCounter < columnCount; selectedColCounter++) {
		copiedString.append(CSV_QUOTE).append(data[selectedRows[selectedRowCounter]][selectedColCounter].toString().replace(CSV_QUOTE_STRING, CSV_DOUBLE_QUOTE)).append(CSV_QUOTE);
		if (selectedColCounter < columnCount - 1) {
		    copiedString.append(CSV_SEPARATOR);
		}
	    }
	    copiedString.append(CSV_NEWLINE);
	}
	//logger.debug("copiedString: {}", this.get getCellSelectionEnabled());
	logger.debug("copiedString: {}", copiedString.toString());
	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	StringSelection stringSelection = new StringSelection(copiedString.toString());
	clipboard.setContents(stringSelection, this);
    }

    public void copyArbilFields(ArbilField[] selectedCells) {
	StringBuilder copiedString = new StringBuilder();
	copiedString.append(CSV_QUOTE).append(SINGLE_NODE_VIEW_HEADING_NAME.replace(CSV_QUOTE_STRING, CSV_DOUBLE_QUOTE)).append(CSV_QUOTE).append(CSV_SEPARATOR);
	copiedString.append(CSV_QUOTE).append(SINGLE_NODE_VIEW_HEADING_VALUE.replace(CSV_QUOTE_STRING, CSV_DOUBLE_QUOTE)).append(CSV_QUOTE);
	copiedString.append(CSV_NEWLINE);
	boolean isFirstCol = true;
	for (ArbilField currentField : selectedCells) {
	    if (!isFirstCol) {
		copiedString.append(CSV_SEPARATOR);
		isFirstCol = false;
	    }
	    copiedString.append(CSV_QUOTE).append(currentField.getTranslateFieldName()).append(CSV_QUOTE).append(CSV_SEPARATOR);
	    copiedString.append(CSV_QUOTE).append(currentField.getFieldValue()).append(CSV_QUOTE);
	    copiedString.append(CSV_NEWLINE);
	}
	logger.debug("copiedString: {}", copiedString.toString());
	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	StringSelection stringSelection = new StringSelection(copiedString.toString());
	clipboard.setContents(stringSelection, this);
    }

    public String pasteIntoArbilFields(ArbilField[] selectedCells) {
	boolean pastedFieldOverwritten = false;
	int pastedCount = 0;
	String resultMessage = null;
	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	Transferable transfer = clipboard.getContents(null);
	try {
	    String clipBoardString = "";
	    Object clipBoardData = transfer.getTransferData(DataFlavor.stringFlavor);
	    if (clipBoardData != null) {
		//TODO: check that this is not null first but let it pass on null so that the no data to paste messages get sent to the user
		clipBoardString = clipBoardData.toString();
	    }
	    logger.debug("clipBoardString: {}", clipBoardString);
	    // to do this there must be either two rows or two columns otherwise we should abort
	    String[] clipBoardLines = clipBoardString.split("\"(\r?\n|\r)\"");
	    if (clipBoardLines.length == 1) {
		// re try in case the csv text is not quoted
		clipBoardLines = clipBoardString.split("\r?\n|\r");
	    }
	    if (clipBoardLines.length == 1) {
		String messageString = MessageFormat.format(widgets.getString("FIELDS WILL BE OVERWRITTEN WITH THE SINGLE VALUE ON THE CLIPBOARD"), selectedCells.length);
		if (messageDialogHandler.showConfirmDialogBox(messageString, widgets.getString("PASTE"))) {
		    for (ArbilField targetField : selectedCells) {
			targetField.setFieldValue(clipBoardString, true, false);
			pastedCount++;
		    }
		} else {
		    return null;
		}
	    } else if (clipBoardLines.length > 1) {
		String areYouSureMessageString = "";
		ArrayList<Object[]> pasteList = new ArrayList<Object[]>();
		int deletingValuesCounter = 0;
		String[] firstLine = clipBoardLines[0].split("\"\\t\"");
		if (firstLine.length == 1) {
		    firstLine = clipBoardLines[0].split(CSV_SEPARATOR);
		}
		boolean singleNodeAxis = false;
		final String regexString = "[(\"^)($\")]";
		if (firstLine[0].replaceAll(regexString, "").equals(SINGLE_NODE_VIEW_HEADING_NAME) && firstLine[1].replaceAll(regexString, "").equals(SINGLE_NODE_VIEW_HEADING_VALUE)) {
		    singleNodeAxis = true;
		}
		if (!singleNodeAxis) {
		    resultMessage = widgets.getString("INCORRECT DATA TO PASTE");
		}
		if (singleNodeAxis) {
		    boolean pasteOneFieldToAll = clipBoardLines.length == 2; /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */
		    HashSet<String> pastedFieldNames = new HashSet();
		    for (int lineCounter = 1; lineCounter < clipBoardLines.length; lineCounter++) {
			String clipBoardLine = clipBoardLines[lineCounter];
			logger.debug("clipBoardLine: {}", clipBoardLine);
			String[] clipBoardCells = clipBoardLine.split("\\t");
			logger.debug("clipBoardCells.length: {}", clipBoardCells.length);
			if (clipBoardCells.length != 2) {
			    resultMessage = widgets.getString("INCONSISTENT NUMBER OF COLUMNS IN THE DATA TO PASTE");
			} else {
			    String currentFieldName = clipBoardCells[0].replaceAll(regexString, "");
			    String currentFieldValue = clipBoardCells[1].replaceAll(regexString, "");
			    if (pastedFieldNames.contains(currentFieldName)) {
				pastedFieldOverwritten = true;
			    } else {
				pastedFieldNames.add(currentFieldName);
			    }
			    if (selectedCells != null) {
				for (ArbilField targetField : selectedCells) {
				    logger.debug("targetField: {}", targetField.getTranslateFieldName());
				    //messagebox "The copied field name does not match the destination, do you want to paste anyway?"
				    if (currentFieldName.equals(targetField.getTranslateFieldName()) || pasteOneFieldToAll) {
					if (currentFieldValue.trim().length() == 0 && targetField.getFieldValue().trim().length() > 0) {
					    deletingValuesCounter++;
					}
					pasteList.add(new Object[]{targetField, currentFieldValue});
				    }
				}
			    }
			}
		    }
		    if (pastedFieldOverwritten) {
			areYouSureMessageString = areYouSureMessageString + widgets.getString("TWO FIELDS OF THE SAME NAME ARE TO BE PASTED");
		    }
		    if (deletingValuesCounter > 0) {
			areYouSureMessageString = areYouSureMessageString + java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString("THERE ARE x FIELDS THAT WILL HAVE THEIR CONTENTS DELETED BY THIS PASTE ACTION"), new Object[]{deletingValuesCounter});
		    }
		    if (areYouSureMessageString.length() > 0) {
			if (!messageDialogHandler.showConfirmDialogBox(areYouSureMessageString + widgets.getString("CONTINUE?"), widgets.getString("PASTE"))) {
			    return null;
			}
		    }
		    for (Object[] pasteListObject : pasteList) {
			ArbilField currentField = (ArbilField) pasteListObject[0];
			String currentValue = (String) pasteListObject[1];
			currentField.setFieldValue(currentValue, true, false);
			pastedCount++;
		    }
		}
	    } else {
		resultMessage = widgets.getString("NO DATA TO PASTE.");
	    }
	    if (pastedCount == 0) {
		if (resultMessage == null) {
		    resultMessage = widgets.getString("NO FIELDS MATCHED THE DATA ON THE CLIPBOARD.");
		}
	    }
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
	return resultMessage;
    }

    @Override
    public void removeAllArbilDataNodeRows() {
	listModel.removeAllElements();
	super.removeAllArbilDataNodeRows();
    }

    @Override
    protected void removeArbilDataNode(ArbilDataNode arbilDataNode) {
	logger.debug("removing: {}", arbilDataNode.toString());
	listModel.removeElement(arbilDataNode);
	super.removeArbilDataNode(arbilDataNode);
    }

    /**
     * @return the data
     */
    protected ArbilTableCell[][] getData() {
	return data;
    }

    /**
     * @param data the data to set
     */
    protected void setData(ArbilTableCell[][] data) {
	this.data = data;
    }

    protected String getRenderedText(ArbilTableCell data) {
	return data.toString();
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
	logger.debug("lost clipboard ownership");
    }
}
