/**
 * Copyright (C) 2012 The Language Archive
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
package nl.mpi.kinnate.projects;

import java.io.File;
import java.util.UUID;

/**
 * Created on : Oct 22, 2011, 09:33
 *
 * @author Peter Withers
 */
public class ProjectRecord {

    final private File projectDirectory;
//    final private String projectName;
    final private String projectUUID;

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

    public File getProjectDirectory() {
        return projectDirectory;
    }

    public String getProjectUUID() {
        return projectUUID;
    }
}
