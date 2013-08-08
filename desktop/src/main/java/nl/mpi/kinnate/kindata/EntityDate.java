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
package nl.mpi.kinnate.kindata;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

/**
 * Document : EntityDate
 * Created on : Mar 15, 2012, 4:23:27 PM
 * Author : Peter Withers
 */
public class EntityDate implements Comparable<EntityDate> {

    @XmlValue
    private String fullDateString;
    @XmlTransient
    Boolean dateIsValid = null;

    public EntityDate() {
    }

    public EntityDate(String fullDateString) {
        this.fullDateString = fullDateString;
    }

    public EntityDate(String yearString, String monthString, String dayString, String qualifierString) throws EntityDateException {
        if (yearString == null) {
            throw new EntityDateException("cannot create date without a year");
        }
        if (dayString != null && monthString == null) {
            throw new EntityDateException("cannot create date with a day but no month");
        }
        if (dayString != null) {
            this.fullDateString = yearString + "/" + monthString + "/" + dayString;
        } else if (monthString != null) {
            this.fullDateString = yearString + "/" + monthString;
        } else {
            this.fullDateString = yearString;
        }
        if (qualifierString != null) {
            boolean foundValidQualifier = false;
            for (String prefixString : new String[]{"abt", "bef", "aft"}) {
                if (qualifierString.startsWith(prefixString)) {
                    foundValidQualifier = true;
                }
            }
            if (!foundValidQualifier) {
                throw new EntityDateException("invalid prefix: " + foundValidQualifier);
            }
            this.fullDateString = this.fullDateString + " " + qualifierString;
        }
    }

    private void checkDateString() {
        dateIsValid = fullDateString.matches("([0-9]{4}(/[0-9]{2}){0,2}(\\sabt|\\sbef|\\saft){0,1}(-[0-9]{4}(/[0-9]{2}){0,2})?(\\sabt|\\sbef|\\saft){0,1}$){0,1}");
    }

    @XmlTransient
    public String getDateString() {
        return fullDateString;
    }

    public boolean dateIsValid() {
        if (dateIsValid == null) {
            checkDateString();
        }
        return dateIsValid;
    }

    public int compareTo(EntityDate o) {
        if (o == null) {
            return -1;
        }
        if (!this.dateIsValid() && !o.dateIsValid()) {
            return 0;
        }
        if (this.dateIsValid() && !o.dateIsValid()) {
            return -1;
        }
        if (!this.dateIsValid() && o.dateIsValid()) {
            return +1;
        }
        return this.fullDateString.compareTo(o.fullDateString);
    }
}
