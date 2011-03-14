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

    public String[] searchByName(String namePartString) {
        ArrayList<String> resultPaths = new ArrayList<String>();
        try {
            //for $doc in collection('nl-mpi-kinnate')  where $doc//NAME="Bob /Cox/" return base-uri($doc)
            String query = "for $doc in collection('nl-mpi-kinnate') where contains($doc//NAME/text(), \"" + namePartString + "\") return base-uri($doc)";
            QueryProcessor proc = new QueryProcessor(query, context);//Emp[contains(Ename,"AR")]
            Iter iter = proc.iter();
            Item item;
            resultPaths.add(query);
            while ((item = iter.next()) != null) {
//                System.out.println(item.toJava());
                resultPaths.add(item.toJava().toString());
            }
            proc.close();
        } catch (QueryException exception) {
            new LinorgBugCatcher().logError(exception);
            resultPaths.add(exception.getMessage());
        } catch (IOException exception) {
            new LinorgBugCatcher().logError(exception);
            resultPaths.add(exception.getMessage());
        }
        return resultPaths.toArray(new String[]{});
    }
}
