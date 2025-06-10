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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import nl.mpi.arbil.clarin.profiles.CmdiProfileProvider.CmdiProfile;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader.ProfileSelection;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.WindowManager;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CmdiProfilesPanel extends JPanel {

    private final JDialog parentFrame;
    private static WindowManager windowManager;

    public static void setWindowManager(WindowManager windowManagerInstance) {
	windowManager = windowManagerInstance;
    }
    private static MessageDialogHandler dialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler dialogHandlerInstance) {
	dialogHandler = dialogHandlerInstance;
    }
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");
    private javax.swing.JPanel clarinPanel;
    private javax.swing.JPanel profileReloadPanel;
    private javax.swing.JProgressBar profileReloadProgressBar;
    protected javax.swing.JCheckBox profileSelectionCheckBox;
    private javax.swing.JScrollPane clarinScrollPane;
    private javax.swing.JButton reloadListButton;
    private javax.swing.JButton reloadProfilesButton;
    private javax.swing.JButton downloadAllButton;
    private JTextArea profileInstructionsArea;
    private boolean firstLoad = true;

    public CmdiProfilesPanel(JDialog parentFrameLocal) {
	this.parentFrame = parentFrameLocal;

	profileReloadPanel = new javax.swing.JPanel();
	profileReloadProgressBar = new javax.swing.JProgressBar();
	clarinScrollPane = new javax.swing.JScrollPane();
	profileSelectionCheckBox = new JCheckBox();
	reloadListButton = new javax.swing.JButton();
	reloadProfilesButton = new javax.swing.JButton();
	downloadAllButton = new javax.swing.JButton();
	clarinPanel = new javax.swing.JPanel();
	JPanel profileReloadTopPanel = new JPanel();
	setLayout(new java.awt.BorderLayout());

//	profileReloadPanel.setLayout(new javax.swing.BoxLayout(profileReloadPanel, javax.swing.BoxLayout.PAGE_AXIS));
	profileReloadPanel.setLayout(new BorderLayout());
	profileReloadPanel.setAlignmentX(SwingConstants.LEFT);
	profileReloadTopPanel.setLayout(new javax.swing.BoxLayout(profileReloadTopPanel, javax.swing.BoxLayout.LINE_AXIS));

	reloadListButton.setText(widgets.getString("PROFILES_REFRESH LIST"));
	reloadListButton.setToolTipText(widgets.getString("DOWNLOAD THE LATEST CLARIN PROFILES"));
	reloadListButton.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		reloadListButtonActionPerformed(evt);
	    }
	});


	reloadProfilesButton.setText(widgets.getString("PROFILES_RELOAD SELECTION"));
	reloadProfilesButton.setToolTipText(widgets.getString("CLEAR CACHED COPIES AND RE-DOWNLOAD THE SELECTED PROFILES"));
	reloadProfilesButton.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		reloadProfilesButtonActionPerformed(evt);
	    }
	});

	downloadAllButton.setText(widgets.getString("PROFILES_DOWNLOAD ALL"));
	downloadAllButton.setToolTipText(widgets.getString("PROFILES_DOWNLOAD ALL PROFILES FOR OFFLINE USE (INCLUDING UNSELECTED PROFILES)"));
	downloadAllButton.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		downloadAllButtonActionPerformed(evt);
	    }
	});

	profileSelectionCheckBox.setText(widgets.getString("ONLY LOAD PROFILES SELECTED FOR MANUAL EDITING"));
	profileSelectionCheckBox.setSelected(CmdiProfileReader.getSingleInstance().getSelection() == ProfileSelection.SELECTED);

	profileReloadTopPanel.add(reloadListButton);
	profileReloadTopPanel.add(reloadProfilesButton);
	profileReloadTopPanel.add(downloadAllButton);
	profileReloadTopPanel.add(profileReloadProgressBar);
	profileReloadPanel.add(profileReloadTopPanel, BorderLayout.CENTER);
	profileReloadPanel.add(profileSelectionCheckBox, BorderLayout.SOUTH);

	add(profileReloadPanel, java.awt.BorderLayout.PAGE_END);

	clarinScrollPane.setViewportView(clarinPanel);

	add(clarinScrollPane, java.awt.BorderLayout.CENTER);

	profileInstructionsArea = new JTextArea(widgets.getString("PROFILES SELECTED BELOW WILL BECOME AVAILABLE IN THE 'ADD' MENU OF THE LOCAL CORPUS. BY DEFAULT, ONLY SELECTED PROFILES WILL BE DOWNLOADED FOR OFFLINE USE."));
	profileInstructionsArea.setEditable(false);
	profileInstructionsArea.setLineWrap(true);
	profileInstructionsArea.setWrapStyleWord(true);
	profileInstructionsArea.setOpaque(false);

	JPanel profilesTopButtonsPanel = new JPanel();
	profilesTopButtonsPanel.setLayout(new javax.swing.BoxLayout(profilesTopButtonsPanel, javax.swing.BoxLayout.LINE_AXIS));

	JButton addButton = new JButton();
	addButton.setText(widgets.getString("PROFILES_ADD URL"));
	addButton.setToolTipText(widgets.getString("PROFILES_ADD A PROFILE URL TO THE LIST"));
	addButton.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		String newDirectoryName = JOptionPane.showInputDialog(windowManager.getMainFrame(), widgets.getString("ENTER THE PROFILE URL"), widgets.getString("ADD PROFILE"), JOptionPane.PLAIN_MESSAGE, null, null, null).toString();
		ArbilTemplateManager.getSingleInstance().addCustomProfile(newDirectoryName);
		populateList();
	    }
	});
	profilesTopButtonsPanel.add(addButton);
	JButton browseButton = new JButton();
	browseButton.setText(widgets.getString("PROFILES_ADD FILE"));
	browseButton.setToolTipText(widgets.getString("BROWSE FOR LOCAL PROFILES"));
	browseButton.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		for (File selectedFile : dialogHandler.showFileSelectBox(widgets.getString("SELECT PROFILE"), false, true, null, MessageDialogHandler.DialogueType.open, null)) {
		    ArbilTemplateManager.getSingleInstance().addCustomProfile(selectedFile.toURI().toString());
		}
		populateList();
	    }
	});
	profilesTopButtonsPanel.add(browseButton);

	JPanel profilesTopPanel = new JPanel();
	profilesTopPanel.setLayout(new javax.swing.BoxLayout(profilesTopPanel, javax.swing.BoxLayout.Y_AXIS));
	profileInstructionsArea.setAlignmentX(Component.LEFT_ALIGNMENT);
	profilesTopButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

	profilesTopPanel.add(profileInstructionsArea);
	profilesTopPanel.add(profilesTopButtonsPanel);
	add(profilesTopPanel, java.awt.BorderLayout.PAGE_START);
    }

    public void downloadProfiles(final boolean forceUpdate) {
	loadProfiles(forceUpdate, true);
    }

    public void loadProfileDescriptions(final boolean forceUpdate) {
	loadProfiles(forceUpdate, false);
    }

    public void setInstructionsVisible(boolean visible) {
	profileInstructionsArea.setVisible(visible);
    }

    private void showProgressBar(boolean show) {
	reloadListButton.setVisible(!show);
	reloadProfilesButton.setVisible(!show);
	downloadAllButton.setVisible(!show);
	profileSelectionCheckBox.setEnabled(!show);
	profileReloadProgressBar.setVisible(show);
    }

    /**
     * @param forceUpdate whether to force update even if cached copies not out of date
     * @param updateProfilesCache whether to update the profiles cache as well (i.e. in addition to the profiles list)
     */
    private void loadProfiles(final boolean forceUpdate, final boolean updateProfilesCache) {
	CmdiProfileReader.getSingleInstance().setSelection(
		profileSelectionCheckBox.isSelected() ? ProfileSelection.SELECTED : ProfileSelection.ALL);
	clarinPanel.removeAll();
	clarinPanel.add(new JTextField(widgets.getString("PROFILES_LOADING, PLEASE WAIT...")));
	showProgressBar(true);
	doLayout();
	new Thread(widgets.getString("PROFILES_LOADPROFILES")) {
	    @Override
	    public void run() {
		CmdiProfileReader cmdiProfileReader = CmdiProfileReader.getSingleInstance();
		if (updateProfilesCache) {
		    cmdiProfileReader.refreshProfilesAndUpdateCache(profileReloadProgressBar, forceUpdate);
		} else {
		    cmdiProfileReader.refreshProfiles(forceUpdate);
		}
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			showProgressBar(false);
			populateList();
			doLayout();
		    }
		});

	    }
	}.start();
    }

    private void reloadListButtonActionPerformed(java.awt.event.ActionEvent evt) {
	loadProfileDescriptions(true);
    }

    private void reloadProfilesButtonActionPerformed(ActionEvent evt) {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		showProgressBar(true);
		profileReloadProgressBar.setIndeterminate(true);
		profileReloadProgressBar.setString("");
		doLayout();
	    }
	});

	new Thread() {
	    @Override
	    public void run() {
		// Get CMDI profiles from selected templates
		final List<String> profilesToReload = ArbilTemplateManager.getSingleInstance().getCMDIProfileHrefs();

		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			profileReloadProgressBar.setIndeterminate(false);
			profileReloadProgressBar.setMinimum(0);
			profileReloadProgressBar.setMaximum(profilesToReload.size() + 1);
			profileReloadProgressBar.setValue(1);
		    }
		});

		// Reload each of them
		for (String xsdHref : profilesToReload) {
		    CmdiProfileReader.getSingleInstance().storeProfileInCache(xsdHref, 0);
		    SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    profileReloadProgressBar.setValue(profileReloadProgressBar.getValue() + 1);
			}
		    });
		}

		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			profileReloadProgressBar.setValue(0);
			showProgressBar(false);
			doLayout();

			dialogHandler.addMessageDialogToQueue(widgets.getString("THE PROFILES HAVE BEEN RE-DOWNLOADED. YOU NEED TO RESTART ARBIL IN ORDER TO APPLY ANY CHANGES."), widgets.getString("RESTART ARBIL"));
		    }
		});
	    }
	}.start();
    }

    private void downloadAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
	downloadProfiles(true);
    }

    public synchronized void populateList() {
	populateProfilesList();
	if (firstLoad) {
	    parentFrame.pack();
	    TemplateDialogue.setDialogHeight(parentFrame,windowManager);
	    firstLoad = false;
	}
    }

    protected void populateProfilesList() {
	List<String> selectedTemplates = ArbilTemplateManager.getSingleInstance().getSelectedTemplates();

	// add clarin types
	List<JCheckBox> checkBoxArray = new ArrayList<JCheckBox>();
	final CmdiProfileReader cmdiProfileReader = CmdiProfileReader.getSingleInstance();
	populateWithPublicProfiles(cmdiProfileReader, selectedTemplates, checkBoxArray);
	populateWithCustomProfiles(cmdiProfileReader, selectedTemplates, checkBoxArray);
	TemplateDialogue.addSorted(clarinPanel, checkBoxArray);
    }

    private void populateWithPublicProfiles(final CmdiProfileReader cmdiProfileReader, List<String> selectedTemplates, List<JCheckBox> checkBoxArray) {
	for (final CmdiProfile currentCmdiProfile : cmdiProfileReader.cmdiProfileArray) {
	    final String templateId = ArbilTemplateManager.CLARIN_PREFIX + currentCmdiProfile.getXsdHref();
	    JCheckBox clarinProfileCheckBox;
	    clarinProfileCheckBox = new JCheckBox();
	    clarinProfileCheckBox.setText(currentCmdiProfile.name);
	    clarinProfileCheckBox.setName(currentCmdiProfile.name);
	    clarinProfileCheckBox.setActionCommand(templateId);
	    clarinProfileCheckBox.setSelected(selectedTemplates.contains(templateId));
	    clarinProfileCheckBox.setToolTipText(currentCmdiProfile.description);
	    clarinProfileCheckBox.addActionListener(TemplateDialogue.templateSelectionListener);
	    clarinProfileCheckBox.addActionListener(new DownloadSchemaActionListener(clarinProfileCheckBox, cmdiProfileReader, currentCmdiProfile.getXsdHref()));
	    checkBoxArray.add(clarinProfileCheckBox);
	}
    }

    private void populateWithCustomProfiles(final CmdiProfileReader cmdiProfileReader, List<String> selectedTemplates, List<JCheckBox> checkBoxArray) {
	for (String currentSelectedProfile : selectedTemplates) {
	    final Matcher matcher = ArbilTemplateManager.getTemplateStringMatcher(currentSelectedProfile);
	    if (matcher.matches() && currentSelectedProfile.startsWith(ArbilTemplateManager.CUSTOM_PREFIX)) {
		final String profileName = ArbilTemplateManager.getCustomProfileName(matcher);

		final JCheckBox clarinProfileCheckBox = new JCheckBox();
		clarinProfileCheckBox.setText(profileName);
		clarinProfileCheckBox.setName(profileName);
		clarinProfileCheckBox.setActionCommand(currentSelectedProfile);
		clarinProfileCheckBox.setSelected(true);
		clarinProfileCheckBox.setToolTipText(widgets.getString("CUSTOM PROFILE"));
		clarinProfileCheckBox.addActionListener(TemplateDialogue.templateSelectionListener);
		clarinProfileCheckBox.addActionListener(new DownloadSchemaActionListener(clarinProfileCheckBox, cmdiProfileReader, matcher.group(4)));
		checkBoxArray.add(clarinProfileCheckBox);
	    }
	}
    }

    private static class DownloadSchemaActionListener implements ActionListener {

	private CmdiProfileReader reader;
	private String url;
	private JCheckBox checkBox;

	public DownloadSchemaActionListener(JCheckBox clarinProfileCheckBox, CmdiProfileReader reader, String url) {
	    this.reader = reader;
	    this.url = url;
	    this.checkBox = clarinProfileCheckBox;
	}

	public void actionPerformed(ActionEvent e) {
	    new Thread() {
		@Override
		public void run() {
		    if (checkBox.isSelected()) {
			reader.storeProfileInCache(url, 0);
		    }
		}
	    }.start();
	}
    }
}
