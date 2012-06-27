package nl.mpi.kinnate.entityindexer;

import junit.framework.TestCase;
import nl.mpi.kinnate.kintypestrings.KinTypeElement;

/**
 * Document : QueryBuilderTest
 * Created on : Jun 27, 2012, 10:03:23 AM
 * Author : Peter Withers
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
                });
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
        String result = instance.getEntityByKeyWordQuery(keyWords, indexParameters);
        assertEquals(result.indexOf("one&two"), -1);
//        assertEquals(result.indexOf("single'quote"), -1); // single quotes are fine because we use double quotes for the query strings
        assertEquals(result.indexOf("double\"quote"), -1);
    }

    /**
     * Test of getTermQuery method, of class QueryBuilder.
     */
    public void testGetTermQuery() {
        System.out.println("getTermQuery");
        KinTypeElement queryTerms = null;
        QueryBuilder instance = new QueryBuilder();
        String expResult = "";
        String result = instance.getTermQuery(queryTerms);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
