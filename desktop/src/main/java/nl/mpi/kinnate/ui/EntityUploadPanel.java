package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.kinnate.export.EntityUploader;

/**
 *  Document   : UploadWindow
 *  Created on : Jun 29, 2011, 2:08:34 PM
 *  Author     : Peter Withers
 */
public class EntityUploadPanel extends JPanel implements ActionListener {

//    private JList uploadList;
    private JTextArea uploadText;
    private JButton searchNewButton;
    private JButton searchModifiedButton;
    private JButton uploadButton;
    private JButton viewUploadButton;
    private JTextField workspaceName;
//    private JCheckBox createWorkspace;
    private JPasswordField passwordText;
    private JProgressBar uploadProgress;
    private EntityUploader entityUploader;
    private JPanel workspacePanel;
    private JPanel passwordPanel;

    public EntityUploadPanel() {
        entityUploader = new EntityUploader();
//        uploadList = new JList();
        uploadText = new JTextArea();
        searchNewButton = new JButton("Search New Entities");
        searchModifiedButton = new JButton("Search Modified Entities");
        uploadButton = new JButton("Upload Selected");
        viewUploadButton = new JButton("View Uploaded");
        workspaceName = new JTextField();
        workspaceName.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent keyEvent) {
                char keyChar = keyEvent.getKeyChar();
                if (!Character.isLetterOrDigit(keyChar)) {
                    // prevent non url chars being entered
                    keyEvent.consume();
                }
            }
        });
//        createWorkspace=new JCheckBox();
        passwordText = new JPasswordField();
        uploadProgress = new JProgressBar();
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(searchNewButton);
        controlPanel.add(searchModifiedButton);
        controlPanel.add(uploadButton);
        controlPanel.add(viewUploadButton);

        workspacePanel = new JPanel();
        workspacePanel.setLayout(new BorderLayout());
        workspacePanel.add(new JLabel("Target Workspace Name"), BorderLayout.LINE_START);
        workspacePanel.add(workspaceName, BorderLayout.CENTER);
//        workspacePanel.add(createWorkspace, BorderLayout.LINE_END);

        passwordPanel = new JPanel();
        passwordPanel.setLayout(new BorderLayout());
        passwordPanel.add(new JLabel("Workspace Password"), BorderLayout.LINE_START);
        passwordPanel.add(passwordText, BorderLayout.CENTER);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
        topPanel.add(controlPanel);
        topPanel.add(workspacePanel);
        topPanel.add(passwordPanel);

        this.setLayout(new BorderLayout());
        this.add(topPanel, BorderLayout.PAGE_START);
//        this.add(uploadList, BorderLayout.CENTER);
        this.add(new JScrollPane(uploadText), BorderLayout.CENTER);
        this.add(uploadProgress, BorderLayout.PAGE_END);
        uploadButton.setEnabled(false);
        viewUploadButton.setEnabled(false);
//        workspaceName.setEnabled(false);
//        passwordText.setEnabled(false);
        workspacePanel.setVisible(false);
        passwordPanel.setVisible(false);
        searchNewButton.addActionListener(this);
        searchModifiedButton.addActionListener(this);
        uploadButton.addActionListener(this);
        viewUploadButton.addActionListener(this);
        searchNewButton.setActionCommand("searchnew");
        searchModifiedButton.setActionCommand("searchmodified");
        uploadButton.setActionCommand("upload");
        viewUploadButton.setActionCommand("view");
    }

    public void actionPerformed(ActionEvent e) {
        searchNewButton.setEnabled(false);
        searchModifiedButton.setEnabled(false);
        uploadButton.setEnabled(false);
        viewUploadButton.setEnabled(false);
        if (e.getActionCommand().equals("searchnew")) {
            uploadText.setText("Searching for local entities that do not exist on the server\n");
            uploadProgress.setIndeterminate(true);
            entityUploader.findLocalEntities(this);
        } else if (e.getActionCommand().equals("searchmodified")) {
            uploadText.setText("Searching for modified entities that require upload to the server\n");
            uploadProgress.setIndeterminate(true);
            entityUploader.findModifiedEntities(this);
        } else if (e.getActionCommand().equals("upload")) {
            if (!workspaceName.getText().isEmpty()) {
                uploadText.append("Uploading entities to the server\n");
                entityUploader.uploadLocalEntites(this, uploadProgress, uploadText, workspaceName.getText(), passwordText.getPassword() /*, createWorkspace.isSelected()*/);
            } else {
                uploadText.append("Please enter a workspace name\n");
            }
        } else if (e.getActionCommand().equals("seachcomplete")) {
            uploadText.append(entityUploader.getFoundMessage());
            uploadProgress.setIndeterminate(false);
            uploadText.append("Done\n");
        } else if (e.getActionCommand().equals("uploadaborted")) {
            uploadProgress.setIndeterminate(false);
            uploadText.append("Error on upload, does the specified workspace exist?\n");
        } else if (e.getActionCommand().equals("view")) {
            GuiHelper.getSingleInstance().openFileInExternalApplication(entityUploader.getWorkspaceUri());
        }

//        workspaceName.setEnabled(entityUploader.canUpload());
//        passwordText.setEnabled(entityUploader.canUpload());
        workspacePanel.setVisible(entityUploader.canUpload());
        passwordPanel.setVisible(entityUploader.canUpload());
        uploadButton.setEnabled(entityUploader.canUpload());
        viewUploadButton.setEnabled(entityUploader.isUploadComplete());
        searchNewButton.setEnabled(!entityUploader.canUpload());
        searchModifiedButton.setEnabled(!entityUploader.canUpload());
    }
}
