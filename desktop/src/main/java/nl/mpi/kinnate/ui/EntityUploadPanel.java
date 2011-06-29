package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import nl.mpi.kinnate.export.EntityUploader;

/**
 *  Document   : UploadWindow
 *  Created on : Jun 29, 2011, 2:08:34 PM
 *  Author     : Peter Withers
 */
public class EntityUploadPanel extends JPanel implements ActionListener {

//    private JList uploadList;
    private JTextArea uploadText;
    private JButton searchButton;
    private JButton uploadButton;
    private JTextField workspaceName;
    private JPasswordField passwordText;
    private JProgressBar uploadProgress;
    private EntityUploader entityUploader;
    private JPanel workspacePanel;
    private JPanel passwordPanel;

    public EntityUploadPanel() {
        entityUploader = new EntityUploader();
//        uploadList = new JList();
        uploadText = new JTextArea();
        searchButton = new JButton("Search Entities");
        uploadButton = new JButton("Upload Selected");
        workspaceName = new JTextField();
        passwordText = new JPasswordField();
        uploadProgress = new JProgressBar();
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(searchButton);
        controlPanel.add(uploadButton);

        workspacePanel = new JPanel();
        workspacePanel.setLayout(new BorderLayout());
        workspacePanel.add(new JLabel("Target Workspace Name"), BorderLayout.LINE_START);
        workspacePanel.add(workspaceName, BorderLayout.CENTER);

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
//        workspaceName.setEnabled(false);
//        passwordText.setEnabled(false);
        workspacePanel.setVisible(false);
        passwordPanel.setVisible(false);
        searchButton.addActionListener(this);
        uploadButton.addActionListener(this);
        searchButton.setActionCommand("search");
        uploadButton.setActionCommand("upload");
    }

    public void actionPerformed(ActionEvent e) {
        searchButton.setEnabled(false);
        uploadButton.setEnabled(false);
        if (e.getActionCommand().equals("search")) {
            uploadText.append("Searching for local entities that do not exist on the server\n");
            int foundCount = entityUploader.findLocalEntities(uploadProgress);
//            uploadText.append(entityUploader.getSearchMessage());
            uploadText.append("Found " + foundCount + " entities to upload\n");
        } else if (e.getActionCommand().equals("upload")) {
            uploadText.append("Uploading entities to the server\n");
            entityUploader.uploadLocalEntites(uploadProgress, workspaceName.getText(), passwordText.getPassword());
            uploadText.append("Done\n");
        }

//        workspaceName.setEnabled(entityUploader.canUpload());
//        passwordText.setEnabled(entityUploader.canUpload());
        workspacePanel.setVisible(entityUploader.canUpload());
        passwordPanel.setVisible(entityUploader.canUpload());
        uploadButton.setEnabled(entityUploader.canUpload());
        searchButton.setEnabled(!entityUploader.canUpload());
    }
}
