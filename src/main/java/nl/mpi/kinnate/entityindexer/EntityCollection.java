package nl.mpi.kinnate.entityindexer;

import java.io.IOException;
import java.util.ArrayList;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.LinorgBugCatcher;
import nl.mpi.arbil.LinorgSessionStorage;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

/**
 *  Document   : EntityCollection
 *  Created on : Feb 15, 2011, 5:37:06 PM
 *  Author     : Peter Withers
 */
public class EntityCollection {

    private String databaseName = "nl-mpi-kinnate";
    static Context context = new Context();

    public class SearchResults {

        public String[] resultsPathArray;
        public String statusMessage;
        public int resultCount = 0;
    }

    public void createDatabase() {
        try {
            new DropDB(databaseName).execute(context);
            new Set("CREATEFILTER", "*.cmdi").execute(context);
            new CreateDB(databaseName, LinorgSessionStorage.getSingleInstance().getCacheDirectory().toString()).execute(context);
//            context.close();
        } catch (BaseXException baseXException) {
            GuiHelper.linorgBugCatcher.logError(baseXException);
        }
    }

    public SearchResults searchByName(String namePartString) {
        String queryString = "";
        SearchResults searchResults = new SearchResults();
        ArrayList<String> resultPaths = new ArrayList<String>();
        try {
            //for $doc in collection('nl-mpi-kinnate')  where $doc//NAME="Bob /Cox/" return base-uri($doc)
            queryString = "for $doc in collection('nl-mpi-kinnate') where contains(string-join($doc//text()), \"" + namePartString + "\") return base-uri($doc)";
//            String query = "for $doc in collection('nl-mpi-kinnate') where $doc//NAME = \"" + namePartString + "\" return base-uri($doc)";
            QueryProcessor proc = new QueryProcessor(queryString, context);//Emp[contains(Ename,"AR")]
            Iter iter = proc.iter();
            Item item;
            while ((item = iter.next()) != null) {
//                System.out.println(item.toJava());
                resultPaths.add(item.toJava().toString());
                searchResults.resultCount++;
            }
            proc.close();

            searchResults.statusMessage = "found " + searchResults.resultCount + " records";
        } catch (QueryException exception) {
            new LinorgBugCatcher().logError(exception);
            searchResults.statusMessage = exception.getMessage();
        } catch (IOException exception) {
            new LinorgBugCatcher().logError(exception);
            searchResults.statusMessage = exception.getMessage();
        }
        searchResults.resultsPathArray = resultPaths.toArray(new String[]{});
        searchResults.statusMessage = searchResults.statusMessage + "\n query: " + queryString;
        return searchResults;
    }
}
