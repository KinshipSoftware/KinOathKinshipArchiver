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
package nl.mpi.kinnate.entityindexer;

import java.util.ArrayList;
import junit.framework.TestCase;
import nl.mpi.kinnate.kintypestrings.KinTypeElement;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter;
import nl.mpi.kinnate.kintypestrings.QueryTerm;

/**
 * Document : QueryBuilderTest Created on : Jun 27, 2012, 10:03:23 AM
 *
 * @author Peter Withers
 */
public class QueryBuilderTest extends TestCase {

    public QueryBuilderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of asSequenceString method, of class QueryBuilder.
     */
    public void testAsSequenceString_StringArr() {
        System.out.println("asSequenceString");
        String[] stringArray = new String[]{"one", "two", "one&two", "single'quote", "double\"quote"};
        QueryBuilder instance = new QueryBuilder();
        String expResult = "{\"one\",\"two\",\"one&amp;two\",\"single'quote\",\"double&quot;quote\"}";
        String result = instance.asSequenceString(stringArray);
        assertEquals(expResult, result);
    }

    /**
     * Test of asSequenceString method, of class QueryBuilder.
     */
    public void testAsSequenceString_IndexerParam() {
        System.out.println("asSequenceString");
        IndexerParam indexerParam = new IndexerParam(new String[][]{
            {"one&two", "triangle"},
            {"single'quote", "triangle"},
            {"double\"quote", "triangle"},
            {"*:Kinnate/*:CustomData[*:Gender='Male']", "triangle"},
            {"*:Kinnate/*:CustomData[*:Gender='']", "square"},
            {"*:Kinnate/*:CustomData[*:DateOfDeath!='']", "blackstrikethrough"}
        }, "%s");
        QueryBuilder instance = new QueryBuilder();
        String expResult = "(\"one&amp;two\",\"single'quote\",\"double&quot;quote\",\"*:Kinnate/*:CustomData[*:Gender='Male']\",\"*:Kinnate/*:CustomData[*:Gender='']\",\"*:Kinnate/*:CustomData[*:DateOfDeath!='']\")";
        String result = instance.asSequenceString(indexerParam);
        assertEquals(expResult, result);
    }

    /**
     * Test of getLabelsClause method, of class QueryBuilder.
     */
    public void testGetLabelsClause() {
        /*
         * todo: this test needs to allow complex xpath queries eg "[starts-with(local-name(), 'Name')]"
         * the escapeBadChars method is not adequate because this is an unquoted xpath
         * so all xpath characters need to be allowed, etc.
         */
//        System.out.println("getLabelsClause");
//        IndexerParameters indexParameters = new IndexerParameters();
//        indexParameters.labelFields = new IndexerParam(new String[][]{
//                    {"*:Kinnate/*:CustomData/*:Type"},
//                    {"*:Kinnate/*:CustomData/*[starts-with(local-name(), 'Name')]"},
//                    {"one&two"},
//                    {"single'quote"},
//                    {"double\"quote"}
//                });
//        String docRootVar = "docrootvar";
//        QueryBuilder instance = new QueryBuilder();
//        String result = instance.getLabelsClause(indexParameters, docRootVar);
//        System.out.println("result: " + result);
//        assertEquals(result.indexOf("\""), -1);
//        assertEquals(result.indexOf("&"), -1);
    }

    /**
     * Test of getDatesClause method, of class QueryBuilder.
     */
//    public void testGetDatesClause() {
//        System.out.println("getDatesClause");
//        IndexerParameters indexParameters = null;
//        String docRootVar = "";
//        QueryBuilder instance = new QueryBuilder();
//        String expResult = "";
//        String result = instance.getDatesClause(indexParameters, docRootVar);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of getSymbolClause method, of class QueryBuilder.
     */
//    public void testGetSymbolClause() {
//        System.out.println("getSymbolClause");
//        IndexerParameters indexParameters = null;
//        String docRootVar = "";
//        QueryBuilder instance = new QueryBuilder();
//        String expResult = "";
//        String result = instance.getSymbolClause(indexParameters, docRootVar);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of getEntityByKeyWordQuery method, of class QueryBuilder.
     */
    public void testGetEntityByKeyWordQuery() {
        System.out.println("getEntityByKeyWordQuery");
        String keyWords = "one two one&two single'quote double\"quote";
        IndexerParameters indexParameters = new IndexerParameters();
        QueryBuilder instance = new QueryBuilder();
        String result = instance.getEntityByKeyWordQuery(keyWords, indexParameters, "dbname");
        assertEquals(result.indexOf("one&two"), -1);
//        assertEquals(result.indexOf("single'quote"), -1); // single quotes are fine because we use double quotes for the query strings
        assertEquals(result.indexOf("double\"quote"), -1);
    }

    /**
     * Test of getTermQuery method, of class QueryBuilder.
     */
    public void testGetTermQuery() {
        System.out.println("getTermQuery");
        KinTypeElement queryTerms = new KinTypeElement();
        queryTerms.queryTerms = new ArrayList<QueryTerm>();
//        queryTerms.queryTerms.add(new QueryTerm("one&two", KinTypeStringConverter.QueryType.Greater, "value"));
        queryTerms.queryTerms.add(new QueryTerm("field", KinTypeStringConverter.QueryType.Greater, "one&two"));
        queryTerms.queryTerms.add(new QueryTerm("single'quote", KinTypeStringConverter.QueryType.Greater, "value"));
        queryTerms.queryTerms.add(new QueryTerm("field", KinTypeStringConverter.QueryType.Greater, "single'quote"));
//        queryTerms.queryTerms.add(new QueryTerm("double\"quote", KinTypeStringConverter.QueryType.Greater, "value"));
        queryTerms.queryTerms.add(new QueryTerm("field", KinTypeStringConverter.QueryType.Greater, "double\"quote"));
        QueryBuilder instance = new QueryBuilder();
        String result = instance.getTermQuery(queryTerms, "dbname");
//        System.out.println("result: " + result);
        assertEquals(result.indexOf("one&two"), -1);
//        assertEquals(result.indexOf("single'quote"), -1); // single quotes are fine because we use double quotes for the query strings
        assertEquals(result.indexOf("double\"quote"), -1);
    }
}
