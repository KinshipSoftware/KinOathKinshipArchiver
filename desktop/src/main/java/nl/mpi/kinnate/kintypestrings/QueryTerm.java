package nl.mpi.kinnate.kintypestrings;

/**
 * Document : QueryTerm
 * Created on : Apr 20, 2012, 12:10:24 PM
 * Author : Peter Withers
 */
public class QueryTerm {

    public QueryTerm(String fieldXPath, KinTypeStringConverter.QueryType comparatorType, String searchValue) {
        this.fieldXPath = fieldXPath;
        this.comparatorType = comparatorType;
        this.searchValue = searchValue;
    }
    public String fieldXPath;
    public KinTypeStringConverter.QueryType comparatorType;
    public String searchValue;
}