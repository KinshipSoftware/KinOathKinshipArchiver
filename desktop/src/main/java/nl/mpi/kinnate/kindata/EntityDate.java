package nl.mpi.kinnate.kindata;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

/**
 *  Document   : EntityDate
 *  Created on : Mar 15, 2012, 4:23:27 PM
 *  Author     : Peter Withers
 */
public class EntityDate {

    @XmlValue
    private String fullDateString;

    public EntityDate() {
    }

    public EntityDate(String fullDateString) {
        // todo: validate this date format
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
            for (String prefixString : new String[]{"ABT", "BEF", "AFT"}) {
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

    @XmlTransient
    public String getDateString() {
        // todo: validate this date format
        return fullDateString;
    }

    public boolean dateIsValid() {
        // todo: validate the date string
        return false;
    }
}
