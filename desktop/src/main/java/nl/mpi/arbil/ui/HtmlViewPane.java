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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.html.HTMLDocument;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class HtmlViewPane extends JTextPane {
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");
    
    public HtmlViewPane() {
	setContentType("text/html;charset=UTF-8");
	setEditable(false);
    }
    
    public HtmlViewPane(URL url) throws IOException {
	this();
	setContents(url);
    }
    
    public JScrollPane createScrollPane() {
	JScrollPane scrollPane = new JScrollPane();
	scrollPane.setViewportView(this);
	return scrollPane;
    }
    
    public void setDocumentBase(Class resourceRefClass, String location) {
	((HTMLDocument) getDocument()).setBase(resourceRefClass.getResource(location));
    }
    
    public final void setContents(URL url) throws IOException {
	setContents(url.openStream());
    }
    
    public final void setContents(final InputStream itemStream) throws IOException {
	if (itemStream == null) {
	    setText(widgets.getString("PAGE NOT FOUND"));
	} else {
	    final StringBuilder completeHelpText = new StringBuilder();
	    BufferedReader bufferedHelpReader = new BufferedReader(new InputStreamReader(itemStream, "UTF-8"));
	    try {
		for (String helpLine = bufferedHelpReader.readLine(); helpLine != null; helpLine = bufferedHelpReader.readLine()) {
		    completeHelpText.append(helpLine);
		}
	    } finally {
		bufferedHelpReader.close();
	    }
	    setText(completeHelpText.toString());
	}
	// Scroll to top
	setCaretPosition(0);
    }
}
