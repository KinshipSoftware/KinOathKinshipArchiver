package nl.mpi.kinnate.plugins.metadatasearch;

import junit.framework.TestCase;
import nl.mpi.arbil.plugin.PluginException;

/**
 * Created on : Sep 27, 2012, 16:57
 *
 * @author Peter Withers
 */
public class SearchPluginTest extends TestCase {

    /**
     * Test the build number against the maven project version.
     */
    public void testBuildVersion() {
        System.out.println("testBuildVersion");
        try {
            SearchPlugin abstractBasePlugin = new SearchPlugin();
            assertTrue(abstractBasePlugin.isMavenVersionCorrect());
        } catch (PluginException exception) {
            fail(exception.getMessage());
        }
    }
}
