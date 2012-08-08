package nl.mpi.kinnate.pligins.metadatasearch.ui;

import javax.swing.JComboBox;
import nl.mpi.kinnate.pligins.metadatasearch.db.MetadataFileType;

/**
 * Document : SearchOptionBox
 * Created on : Aug 8, 2012, 4:34:14 PM
 * Author : Peter Withers
 */
public class SearchOptionBox extends JComboBox {

    final private MetadataFileType[] metadataFileTypes;

    public SearchOptionBox(MetadataFileType[] metadataFileTypes) {
        super(metadataFileTypes);
        this.metadataFileTypes = metadataFileTypes;
        
    }
}
