package nl.mpi.kinnate.ui;

import nl.mpi.kinnate.kintypestrings.ParserHighlight;

/**
 * Document : KinTypeStringProvider
 * Created on : Mar 27, 2012, 10:11:25 AM
 * Author : Peter Withers
 */
public interface KinTypeStringProvider {

    public String[] getCurrentStrings();

    public int getTotalLength();

    public void highlightKinTypeStrings(ParserHighlight[] parserHighlight, String[] kinTypeStrings);
}
