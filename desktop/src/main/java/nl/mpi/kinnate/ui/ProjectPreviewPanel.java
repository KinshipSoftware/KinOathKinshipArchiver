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
package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.bind.JAXBException;
import nl.mpi.kinnate.projects.ProjectManager;
import nl.mpi.kinnate.projects.ProjectRecord;

/**
 * Created on : June 3, 2013, 15:58 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class ProjectPreviewPanel extends JPanel implements PropertyChangeListener, HierarchyListener {

    final private JLabel projectTitle;
    final private JTextField projectTitleField;
    final private JLabel projectDescription;
    final private JTextField projectDescriptionField;
    final private JLabel lastChangeDate;
    final private JLabel entityCount;
    final private JLabel relationCount;
    final private ProjectManager projectManager;
    private JFileChooser fileChooser = null;

    public ProjectPreviewPanel(ProjectManager projectManager, boolean isEditable) {
        super(new BorderLayout());
        this.projectManager = projectManager;
        JPanel labelsPanel = new JPanel(new GridLayout(5, 2));
        this.projectTitle = new JLabel();
        this.projectDescription = new JLabel();
        this.projectTitleField = new JTextField();
        this.projectDescriptionField = new JTextField();
        this.lastChangeDate = new JLabel();
        this.entityCount = new JLabel();
        relationCount = new JLabel();
        labelsPanel.add(new JLabel("Project Title"));
        if (isEditable) {
            labelsPanel.add(projectTitleField);
        } else {
            labelsPanel.add(projectTitle);
        }
        labelsPanel.add(new JLabel("Project Description"));
        if (isEditable) {
            labelsPanel.add(projectDescriptionField);
        } else {
            labelsPanel.add(projectDescription);
        }
        labelsPanel.add(new JLabel("Entity Count"));
        labelsPanel.add(entityCount);
        labelsPanel.add(new JLabel("Relation Count"));
        labelsPanel.add(relationCount);
        labelsPanel.add(new JLabel("Last Changed"));
        labelsPanel.add(lastChangeDate);
        this.add(labelsPanel, BorderLayout.NORTH);
        this.addHierarchyListener(this);
    }

    public void setProjectRecord(ProjectRecord projectRecord) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        this.projectTitle.setText(projectRecord.getProjectName());
        this.projectDescription.setText(projectRecord.getProjectDescription());
        final Date lastChangeDate1 = projectRecord.getLastChangeDate();
        if (lastChangeDate1 != null) {
            this.lastChangeDate.setText(dateFormat.format(lastChangeDate1));
        } else {
            this.lastChangeDate.setText("");
        }
        this.entityCount.setText(Integer.toString(projectRecord.getEntityCount()));
        this.relationCount.setText(Integer.toString(projectRecord.getRelationCount()));
    }

    public void clearProjectRecord() {
        this.projectTitle.setText("");
        this.projectDescription.setText("");
        this.lastChangeDate.setText("");
        this.entityCount.setText("");
        this.relationCount.setText("");
    }

    public void propertyChange(PropertyChangeEvent changeEvent) {
        if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(changeEvent.getPropertyName())) {
            clearProjectRecord();
        } else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(changeEvent.getPropertyName())) {
            try {
                final File selectedFile = (File) changeEvent.getNewValue();
                if (selectedFile != null) {
                    final ProjectRecord projectRecord = projectManager.loadProjectRecord(selectedFile);
                    if (projectRecord == null) {
                        clearProjectRecord();
                    }
                    setProjectRecord(projectRecord);
                } else {
                    clearProjectRecord();
                }
            } catch (JAXBException exception) {
                // if we cannot read the project file then we cannot open the project
                clearProjectRecord();
            }
        }
    }

    public void hierarchyChanged(HierarchyEvent he) {
//        System.out.println("hierarchyChanged parent:" + ProjectPreviewPanel.this.getParent());
        if (fileChooser == null) {
            Container parent = ProjectPreviewPanel.this.getParent();
            while (parent != null) {
//                System.out.println("parent:" + parent.getClass().getCanonicalName());
                if (parent instanceof JFileChooser) {
                    fileChooser = (JFileChooser) parent;
                    fileChooser.addPropertyChangeListener(this);
                }
                parent = parent.getParent();
            }
        }
    }
}
