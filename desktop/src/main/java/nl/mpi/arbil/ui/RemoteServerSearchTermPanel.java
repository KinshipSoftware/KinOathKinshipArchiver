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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.search.ArbilRemoteSearch;
import nl.mpi.arbil.search.RemoteServerSearchTerm;
import nl.mpi.arbil.util.MessageDialogHandler;

/**
 * Document : RemoteServerSearchTermPanel
 * Created on : Sept 22, 2010, 15:31:54 PM
 *
 * @author Peter.Withers@mpi.nl
 */
public class RemoteServerSearchTermPanel extends JPanel implements RemoteServerSearchTerm {

    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");
    private ArbilNodeSearchPanel parentPanel;
    private JTextField searchField;
    private JLabel resultCountLabel;
    private ArbilRemoteSearch remoteSearch = new ArbilRemoteSearch();
    private static MessageDialogHandler dialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler dialogHandlerInstance) {
	dialogHandler = dialogHandlerInstance;
    }

    public RemoteServerSearchTermPanel(ArbilNodeSearchPanel parentPanelLocal) {
	parentPanel = parentPanelLocal;
	searchField = new javax.swing.JTextField(valueFieldMessage);
	searchField.setForeground(Color.lightGray);

	this.setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

	searchField.addFocusListener(new FocusListener() {
	    public void focusGained(FocusEvent e) {
		if (searchField.getText().equals(valueFieldMessage)) {
		    searchField.setText("");
		    searchField.setForeground(Color.BLACK);
		}
	    }

	    public void focusLost(FocusEvent e) {
		if (searchField.getText().length() == 0) {
		    searchField.setText(valueFieldMessage);
		    searchField.setForeground(Color.lightGray);
		}
	    }
	});

	searchField.addKeyListener(new java.awt.event.KeyAdapter() {
	    @Override
	    public void keyReleased(java.awt.event.KeyEvent evt) {
		parentPanel.stopSearch();
	    }
	});
	resultCountLabel = new JLabel();
	this.add(searchField);
	this.add(resultCountLabel);
    }

    @Override
    public URI[] getServerSearchResults(ArbilDataNode[] arbilDataNodeArray) {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		resultCountLabel.setText(widgets.getString("SEARCH_CONNECTING TO SERVER"));
	    }
	});

	final String searchFieldText = searchField.getText();
	if (ArbilRemoteSearch.isEmptyQuery(searchFieldText)) {
	    dialogHandler.addMessageDialogToQueue(widgets.getString("NO REMOTE SEARCH TERM PROVIDED, CANNOT SEARCH REMOTELY"), widgets.getString("REMOTE SEARCH"));
	}

	final URI[] searchResult = remoteSearch.getServerSearchResults(searchFieldText, arbilDataNodeArray);

	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		resultCountLabel.setText(MessageFormat.format(widgets.getString("SEARCH_N FOUND ON SERVER "), searchResult.length));
	    }
	});
	return searchResult;
    }
}
