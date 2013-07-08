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
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created on : Oct 22, 2011, 09:33
 *
 * @author Peter Withers
 */
@XmlRootElement(name = "ProjectRecord")
public class ProjectRecord {

    protected File projectDirectory;
    protected String projectName;
    protected String projectDescription;
    protected String projectUUID;
    protected int entityCount;
    protected int relationCount;
    protected Date lastChangeDate;
    protected UUID lastChangeUUID = UUID.randomUUID(); // because this has just been added 2013/07/08 we need to make sure it is not null in old records

    public ProjectRecord() {
    }

    public ProjectRecord(File projectDirectory, String projectName) {
        this.projectName = projectName;
        this.projectDirectory = projectDirectory;
        this.projectUUID = UUID.randomUUID().toString();
        this.lastChangeDate = Calendar.getInstance().getTime();
        this.lastChangeUUID = UUID.randomUUID();
    }

    @Deprecated
    public ProjectRecord(File projectDirectory, String projectName, String projectUUID) {
        this.projectName = projectName;
        this.projectDirectory = projectDirectory;
        this.projectUUID = projectUUID;
        this.lastChangeDate = Calendar.getInstance().getTime();
        this.lastChangeUUID = UUID.randomUUID();
    }

    public File getProjectDirectory() {
        return projectDirectory;
    }

    @XmlElement(name = "ProjectDirectory")
    public void setProjectDirectory(File projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    public void bumpLastChangeDate() {
        lastChangeDate = Calendar.getInstance().getTime();
        this.lastChangeUUID = UUID.randomUUID();
    }

    public String getProjectName() {
        return projectName;
    }

    @XmlAttribute(name = "ProjectName")
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    @XmlElement(name = "ProjectDescription")
    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public int getEntityCount() {
        return entityCount;
    }

    @XmlAttribute(name = "EntityCount")
    public void setEntityCount(int entityCount) {
        this.entityCount = entityCount;
    }

    public int getRelationCount() {
        return relationCount;
    }

    @XmlAttribute(name = "RelationCount")
    public void setRelationCount(int relationCount) {
        this.relationCount = relationCount;
    }

    public Date getLastChangeDate() {
        return lastChangeDate;
    }

    @XmlElement(name = "LastChangeTime")
    public void setLastChangeDate(Date lastChangeDate) {
        this.lastChangeDate = lastChangeDate;
    }

    public UUID getLastChangeId() {
        return lastChangeUUID;
    }

    @XmlElement(name = "LastChangeId")
    public void setLastChangeId(UUID lastChangeUUID) {
        this.lastChangeUUID = lastChangeUUID;
    }

    public File getProjectDataFilesDirectory() {
        return createDirectoryIfNotFound(new File(getProjectDirectory(), "KinDataFiles"));
    }

    public String getProjectUUID() {
        return projectUUID;
    }

    @XmlAttribute(name = "ProjectUUID")
    public void setProjectUUID(String projectUUID) {
        this.projectUUID = projectUUID;
    }

    private File createDirectoryIfNotFound(File directoryFile) {
        if (!directoryFile.exists()) {
            directoryFile.mkdir();
        }
        return directoryFile;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + (this.projectDirectory != null ? this.projectDirectory.hashCode() : 0);
        hash = 73 * hash + (this.projectUUID != null ? this.projectUUID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProjectRecord other = (ProjectRecord) obj;
        if (this.projectDirectory != other.projectDirectory && (this.projectDirectory == null || !this.projectDirectory.equals(other.projectDirectory))) {
            return false;
        }
        if ((this.projectUUID == null) ? (other.projectUUID != null) : !this.projectUUID.equals(other.projectUUID)) {
            return false;
        }
        return true;
    }
}
