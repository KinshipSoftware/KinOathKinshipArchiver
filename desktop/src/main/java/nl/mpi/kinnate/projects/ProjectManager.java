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

import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.EntityServiceException;

/**
 * Created on : Oct 22, 2011, 09:43
 *
 * @author Peter Withers
 */
public class ProjectManager {

    private ProjectRecord[] projectRecords;
    private ProjectRecord defaultProject = null; // should the default project be discarded and a mandatory import be required?

    public ProjectManager() {
    }

    public ProjectRecord getDefaultProject(SessionStorage sessionStorage) {
        if (defaultProject == null) {
            defaultProject = new ProjectRecord(sessionStorage.getProjectDirectory(), "nl-mpi-kinnate");
        }
        return defaultProject;
    }

    public ProjectRecord[] getProjectRecords(SessionStorage sessionStorage) {
        return projectRecords;
    }
/*
 * todo: Ticket #2880 (new enhancement)
 * The open project window could also show which diagrams are known to use each project. Also it could show any known copies of a given project based on uuid.
 */
    public EntityCollection getEntityCollectionForProject(ProjectRecord projectRecord)throws EntityServiceException{
//         todo: keep track of these collections so that the db does not get locking errors
        throw new EntityServiceException("Test throw of EntityServiceException");
//        return new EntityCollection(projectRecord);
    }
}
