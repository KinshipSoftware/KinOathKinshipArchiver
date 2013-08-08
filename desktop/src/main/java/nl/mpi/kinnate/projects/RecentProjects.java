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
package nl.mpi.kinnate.projects;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created on : June 5, 2013, 15:28 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
@XmlRootElement(name = "RecentProjects")
public class RecentProjects {

    final private int maxRecentCount = 9;
    @XmlElement(name = "ProjectRecord")
    public ArrayList<ProjectRecord> recentProjects;

    public RecentProjects() {
        this.recentProjects = new ArrayList<ProjectRecord>();
    }

    public void moveProjectRecordToTop(ProjectRecord projectRecord) {
        ProjectRecord existingRecord;
        if (recentProjects.contains(projectRecord)) {
            existingRecord = recentProjects.get(recentProjects.indexOf(projectRecord));
        } else {
            existingRecord = projectRecord;
        }
        recentProjects.remove(existingRecord);
        while (recentProjects.size() > maxRecentCount) {
            recentProjects.remove(recentProjects.size() - 1);
        }
        recentProjects.add(0, existingRecord);
    }

    public void updateProjectRecord(ProjectRecord projectRecord) {
        recentProjects.remove(projectRecord);
        while (recentProjects.size() > maxRecentCount) {
            recentProjects.remove(recentProjects.size() - 1);
        }
        recentProjects.add(0, projectRecord);
    }

    public void removeMissingProjects() {
        for (ProjectRecord projectRecord : recentProjects.toArray(new ProjectRecord[0])) {
            if (!new File(projectRecord.getProjectDirectory(), ProjectManager.kinoathproj).exists()) {
                recentProjects.remove(projectRecord);
            }
        }
    }

    public void clearList() {
        recentProjects.clear();
    }

    public List<ProjectRecord> getProjectRecords() {
        return Collections.unmodifiableList(recentProjects);
    }
}
