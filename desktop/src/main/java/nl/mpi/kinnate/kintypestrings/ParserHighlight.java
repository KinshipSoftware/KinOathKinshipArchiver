package nl.mpi.kinnate.kintypestrings;

/**
 *  Document   : ParserHighlight
 *  Created on : May 30, 2011, 3:06:02 PM
 *  Author     : Peter Withers
 */
public class ParserHighlight {

    public enum ParserHighlightType {

        KinType, Comment, Error, Query, Parameter, Unknown
    }
    public ParserHighlight nextHighlight = null;
    public ParserHighlightType highlight;
    public int startChar = 0;

    public ParserHighlight addHighlight(ParserHighlightType highlightType, int startChar) {

        this.highlight = highlightType;
        this.startChar = startChar;
        this.nextHighlight = new ParserHighlight();
        return this.nextHighlight;
    }
}
