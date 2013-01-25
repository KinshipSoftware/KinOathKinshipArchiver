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
package nl.mpi.kinnate.ui.entityprofiles;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created on : Mar 21, 2012, 2:45:30 PM
 *
 * @author Peter Withers
 */
public class ProfileRecord {

    @XmlAttribute(name = "ProfileName", namespace = "http://mpi.nl/tla/kin")
    public String profileName;
    @XmlAttribute(name = "ProfileId", namespace = "http://mpi.nl/tla/kin")
    public String profileId;

    private ProfileRecord() {
    }

    public ProfileRecord(String profileName, String profileId) {
        this.profileName = profileName;
        this.profileId = profileId;
    }

    @Override
    public String toString() {
        return profileName;
    }

    static public ProfileRecord getDefaultImportProfile() {
        return new ProfileRecord("<default>", "clarin.eu:cr1:p_1332345811039");
    }

    static public ProfileRecord[] getDefaultProfiles() {
        return new ProfileRecord[]{new ProfileRecord("Individual", "clarin.eu:cr1:p_1320657629627"), new ProfileRecord("Chromosome_Example", "clarin.eu:cr1:p_1332345811038"), new ProfileRecord("Clan_Example", "clarin.eu:cr1:p_1337778924934"), new ProfileRecord("KinTerm", "clarin.eu:cr1:p_1337778924930"), new ProfileRecord("Social_Event", "clarin.eu:cr1:p_1357720977500")};
//         <kin:Profile kin:ProfileName="Chromosome_Example" kin:ProfileId="clarin.eu:cr1:p_1337778924932"/>
    }
}
