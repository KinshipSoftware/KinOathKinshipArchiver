package nl.mpi.kinnate.entityindexer;

import junit.framework.TestCase;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.kintypestrings.KinTypeElement;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

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
        String[] stringArray = new String[]{"one", "two"};
        QueryBuilder instance = new QueryBuilder();
        String expResult = "(\"one\",\"two\")";
        String result = instance.asSequenceString(stringArray);
        assertEquals(expResult, result);
    }

    /**
     * Test of asSequenceString method, of class QueryBuilder.
     */
    public void testAsSequenceString_IndexerParam() {
        System.out.println("asSequenceString");
        IndexerParam indexerParam = null;
        QueryBuilder instance = new QueryBuilder();
        String expResult = "";
        String result = instance.asSequenceString(indexerParam);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLabelsClause method, of class QueryBuilder.
     */
    public void testGetLabelsClause() {
        System.out.println("getLabelsClause");
        IndexerParameters indexParameters = null;
        String docRootVar = "";
        QueryBuilder instance = new QueryBuilder();
        String expResult = "";
        String result = instance.getLabelsClause(indexParameters, docRootVar);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDatesClause method, of class QueryBuilder.
     */
    public void testGetDatesClause() {
        System.out.println("getDatesClause");
        IndexerParameters indexParameters = null;
        String docRootVar = "";
        QueryBuilder instance = new QueryBuilder();
        String expResult = "";
        String result = instance.getDatesClause(indexParameters, docRootVar);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSymbolClause method, of class QueryBuilder.
     */
    public void testGetSymbolClause() {
        System.out.println("getSymbolClause");
        IndexerParameters indexParameters = null;
        String docRootVar = "";
        QueryBuilder instance = new QueryBuilder();
        String expResult = "";
        String result = instance.getSymbolClause(indexParameters, docRootVar);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getArchiveLinksClause method, of class QueryBuilder.
     */
    public void testGetArchiveLinksClause() {
        System.out.println("getArchiveLinksClause");
        QueryBuilder instance = new QueryBuilder();
        String expResult = "";
        String result = instance.getArchiveLinksClause();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRelationQuery method, of class QueryBuilder.
     */
    public void testGetRelationQuery() {
        System.out.println("getRelationQuery");
        QueryBuilder instance = new QueryBuilder();
        String expResult = "";
        String result = instance.getRelationQuery();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEntityQueryReturn method, of class QueryBuilder.
     */
    public void testGetEntityQueryReturn() {
        System.out.println("getEntityQueryReturn");
        IndexerParameters indexParameters = null;
        QueryBuilder instance = new QueryBuilder();
        String expResult = "";
        String result = instance.getEntityQueryReturn(indexParameters);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEntityByEndPointQuery method, of class QueryBuilder.
     */
    public void testGetEntityByEndPointQuery() {
        System.out.println("getEntityByEndPointQuery");
        RelationType relationType = null;
        IndexerParameters indexParameters = null;
        QueryBuilder instance = new QueryBuilder();
        String expResult = "";
        String result = instance.getEntityByEndPointQuery(relationType, indexParameters);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEntityByKeyWordQuery method, of class QueryBuilder.
     */
    public void testGetEntityByKeyWordQuery() {
        System.out.println("getEntityByKeyWordQuery");
        String keyWords = "";
        IndexerParameters indexParameters = null;
        QueryBuilder instance = new QueryBuilder();
        String expResult = "";
        String result = instance.getEntityByKeyWordQuery(keyWords, indexParameters);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEntityWithRelationsQuery method, of class QueryBuilder.
     */
    public void testGetEntityWithRelationsQuery() {
        System.out.println("getEntityWithRelationsQuery");
        UniqueIdentifier uniqueIdentifier = null;
        String[] excludeUniqueIdentifiers = null;
        IndexerParameters indexParameters = null;
        QueryBuilder instance = new QueryBuilder();
        String expResult = "";
        String result = instance.getEntityWithRelationsQuery(uniqueIdentifier, excludeUniqueIdentifiers, indexParameters);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEntityPath method, of class QueryBuilder.
     */
    public void testGetEntityPath() {
        System.out.println("getEntityPath");
        UniqueIdentifier uniqueIdentifier = null;
        QueryBuilder instance = new QueryBuilder();
        String expResult = "";
        String result = instance.getEntityPath(uniqueIdentifier);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEntityQuery method, of class QueryBuilder.
     */
    public void testGetEntityQuery() {
        System.out.println("getEntityQuery");
        UniqueIdentifier uniqueIdentifier = null;
        IndexerParameters indexParameters = null;
        QueryBuilder instance = new QueryBuilder();
        String expResult = "";
        String result = instance.getEntityQuery(uniqueIdentifier, indexParameters);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
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
