package nl.mpi.kinnate;

/**
 *  Document   : KinTermPanel
 *  Created on : Apr 1, 2011, 9:41:11 AM
 *  Author     : Peter Withers
 */
public interface KinTermSavePanel {

    public void hideShow();

    public void addKinTermGroup();

    public void importKinTerms();

    public void exportKinTerms();

    public void setSelectedKinTypeSting(String kinTypeStrings);

    public boolean isHidden();
}
