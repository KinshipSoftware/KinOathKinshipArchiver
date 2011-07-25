package nl.mpi.kinnate.uniqueidentifiers;

import java.util.UUID;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import nl.mpi.arbil.data.ArbilField;

/**
 *  Document   : UniqueIdentifier
 *  Created on : Jul 21, 2011, 3:23:17 PM
 *  Author     : Peter Withers
 */
@XmlRootElement(name = "UniqueIdentifier")
public class UniqueIdentifier {

    public enum IdentifierType {

        pid /*Persistent Identifier*/, lid /*Local Identifier*/, tid /*Transient Identifier*/, gid /*Graphics Identifier*/

    }
//    private IdentifierType identifierType;
    @XmlElement(name = "LocalIdentifier")
    private String localIdentifier = null;
    @XmlElement(name = "PersistentIdentifier")
    private String persistentIdentifier = null;
//    @XmlTransient
    @XmlElement(name = "TransientIdentifier")
    private String transientIdentifier = null;
    @XmlTransient
    private String graphicsIdentifier = null;

    private UniqueIdentifier() {
    }

    public UniqueIdentifier(IdentifierType identifierType) {
        switch (identifierType) {
            case pid:
                persistentIdentifier = UUID.randomUUID().toString();
                break;
            case lid:
                localIdentifier = UUID.randomUUID().toString();
                break;
            case tid:
                transientIdentifier = UUID.randomUUID().toString();
                break;
            case gid:
                graphicsIdentifier = UUID.randomUUID().toString();
                break;
        }
    }

    public UniqueIdentifier(ArbilField arbilField) {
        IdentifierType identifierType;
        if (arbilField.getFullXmlPath().endsWith(".UniqueIdentifier.LocalIdentifier")) {
            identifierType = IdentifierType.lid;
        } else if (arbilField.getFullXmlPath().endsWith(".UniqueIdentifier.PersistantIdentifier")) {
            identifierType = IdentifierType.pid;
        } else {
            throw new UnsupportedOperationException("Incorrect ArbilField: " + arbilField.getFullXmlPath());
        }
        switch (identifierType) {
            case pid:
                persistentIdentifier = arbilField.getFieldValue();
                break;
            case lid:
                localIdentifier = arbilField.getFieldValue();
                break;
        }
    }

    public UniqueIdentifier(String attributeIdentifier) {
        // reconstruct the identifier from an attribte string originally obtained by getAttributeIdentifier
        String[] attributeIdentifierParts = attributeIdentifier.split("\\:");
        if (attributeIdentifierParts.length != 2) {
            throw new UnsupportedOperationException("Incorrect identifier format: " + attributeIdentifier);
        }
        IdentifierType identifierType = IdentifierType.valueOf(attributeIdentifierParts[0]);
        String identifierString = attributeIdentifierParts[1];
        switch (identifierType) {
            case pid:
                persistentIdentifier = identifierString;
                break;
            case lid:
                localIdentifier = identifierString;
                break;
            case tid:
                transientIdentifier = identifierString;
                break;
            case gid:
                graphicsIdentifier = identifierString;
                break;
        }
    }

    public UniqueIdentifier(String userDefinedIdentifier, IdentifierType identifierType) {
        switch (identifierType) {
            case tid:
                transientIdentifier = userDefinedIdentifier;
                break;
            case pid:
            case lid:
            case gid:
                throw new UnsupportedOperationException("Unsupported user defined identifier, these must be transient identifiers");
        }
    }

    public String getQueryIdentifier() {
        // limit the query to local or persistent
        if (persistentIdentifier != null) {
            return persistentIdentifier;
        } else if (localIdentifier != null) {
            return localIdentifier;
        }
        throw new UnsupportedOperationException();
    }

    public String getAttributeIdentifier() {
        if (persistentIdentifier != null) {
            return IdentifierType.pid.name() + ":" + persistentIdentifier;
        } else if (localIdentifier != null) {
            return IdentifierType.lid.name() + ":" + localIdentifier;
        } else if (transientIdentifier != null) {
            return IdentifierType.tid.name() + ":" + transientIdentifier;
        } else if (graphicsIdentifier != null) {
            return IdentifierType.gid.name() + ":" + graphicsIdentifier;
        }
        throw new UnsupportedOperationException();
    }

    public boolean isGraphicsIdentifier() {
        return graphicsIdentifier != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UniqueIdentifier other = (UniqueIdentifier) obj;
        if ((this.localIdentifier == null) ? (other.localIdentifier != null) : !this.localIdentifier.equals(other.localIdentifier)) {
            return false;
        }
        if ((this.persistentIdentifier == null) ? (other.persistentIdentifier != null) : !this.persistentIdentifier.equals(other.persistentIdentifier)) {
            return false;
        }
        if ((this.transientIdentifier == null) ? (other.transientIdentifier != null) : !this.transientIdentifier.equals(other.transientIdentifier)) {
            return false;
        }
        if ((this.graphicsIdentifier == null) ? (other.graphicsIdentifier != null) : !this.graphicsIdentifier.equals(other.graphicsIdentifier)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.localIdentifier != null ? this.localIdentifier.hashCode() : 0);
        hash = 89 * hash + (this.persistentIdentifier != null ? this.persistentIdentifier.hashCode() : 0);
        hash = 89 * hash + (this.transientIdentifier != null ? this.transientIdentifier.hashCode() : 0);
        hash = 89 * hash + (this.graphicsIdentifier != null ? this.graphicsIdentifier.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }
}
