package nl.mpi.kinnate.kintypestrings;

import java.util.ArrayList;
import nl.mpi.kinnate.kindata.EntityData;

/**
 * Document : KinTypeElement
 * Created on : Apr 20, 2012, 3:40:29 PM
 * Author : Peter Withers
 */
public class KinTypeElement {

    public KinTypeElement() {
        entityData = new ArrayList<EntityData>();
    }
    public KinTypeElement prevType;
    public KinTypeElement nextType;
    public KinType kinType;
    public ArrayList<QueryTerm> queryTerms;
    public ArrayList<EntityData> entityData; // there may be multiple entities for each kin term
    ParserHighlight[] highlightLocs;
}