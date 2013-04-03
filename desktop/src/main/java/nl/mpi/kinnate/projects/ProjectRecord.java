/**
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.projects;

import java.io.File;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Created on : Oct 22, 2011, 09:33
 *
 * @author Peter Withers
 */
public class ProjectRecord {

    @XmlAttribute(name = "ProjectDirectory", namespace = "http://mpi.nl/tla/kin")
    protected File projectDirectory;
//    final private String projectName;
    @XmlAttribute(name = "ProjectUUID", namespace = "http://mpi.nl/tla/kin")
    protected String projectUUID;

    protected ProjectRecord() {
    }

//    public ProjectRecord(File projectDirectory, String projectName) {
//        this.projectDirectory = projectDirectory;
//        this.projectName = projectName;
//        this.projectUUID = UUID.randomUUID().toString();
//    }
    public ProjectRecord(File projectDirectory) {
        this.projectDirectory = projectDirectory;
        this.projectUUID = UUID.randomUUID().toString();
    }

    public ProjectRecord(File projectDirectory, String projectUUID) {
        this.projectDirectory = projectDirectory;
        this.projectUUID = projectUUID;
    }

    private File getProjectDirectory() {
        return projectDirectory;
    }

    @XmlTransient
    public File getProjectDataBaseDirectory() {
        return new File(getProjectDirectory(), "BaseXData");
    }

    @XmlTransient
    public File getProjectDataFilesDirectory() {
        return new File(getProjectDirectory(), "KinDataFiles");
    }

    @XmlTransient
    public String getProjectUUID() {
        return projectUUID;
    }
}
