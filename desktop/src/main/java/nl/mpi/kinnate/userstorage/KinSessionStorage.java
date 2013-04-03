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
package nl.mpi.kinnate.userstorage;

import java.io.File;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.kinnate.KinOathVersion;
import nl.mpi.kinnate.projects.ProjectRecord;

/**
 * Created on : Dec 9, 2011, 10:20:43 AM
 *
 * @author : Peter Withers
 */
public class KinSessionStorage extends ArbilSessionStorage /* CommonsSessionStorage */ {

    final private ApplicationVersionManager versionManager;
    private ProjectRecord projectRecord = null;

    public KinSessionStorage(ApplicationVersionManager versionManager) {
        this.versionManager = versionManager;
    }

    public void setProjectRecord(ProjectRecord projectRecord) {
        this.projectRecord = projectRecord;
        getProjectWorkingDirectory().mkdir();
    }

    // The major, minor version numbers will change the working directory name so that each minor version requires
    // an export import operation allowing the internal data structure to be changed. When the internal data structure
    // is stable the minor version can be replaced with an x so that the directory does not change. Exporting will
    // require the use of the old version of the application and this could be achieved by creating a jnlp for the
    // old jars and an export dialog instead of the main application.
    @Override
    protected String[] getAppDirectoryAlternatives() {
        return new String[]{".kinoath-" + new KinOathVersion().currentMajor + "-" + new KinOathVersion().currentMinor};
    }
    
    // todo: remove ArbilWorkingFiles from the working files path and use KinshipData or such like

//    @Override
//    public File getProjectDirectory() {
//        throw new UnsupportedOperationException("getProjectDirectory in session storage should not be used");
//    }

//    @Override
//    public File getProjectWorkingDirectory() {
//        throw new UnsupportedOperationException("getProjectWorkingDirectory in session storage should not be used");
//    }

    @Override
    public boolean pathIsInsideCache(File fullTestFile) {
        return true; // we want all kin files to be editable
    }
}
