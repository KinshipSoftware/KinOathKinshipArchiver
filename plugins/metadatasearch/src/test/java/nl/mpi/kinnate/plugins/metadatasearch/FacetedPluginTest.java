package nl.mpi.kinnate.plugins.metadatasearch;

import junit.framework.TestCase;
import nl.mpi.arbil.plugin.PluginException;

/**
 * Created on : Sep 27, 2012, 16:57
 *
 * @author Peter Withers
 */
public class FacetedPluginTest extends TestCase {

    /**
     * Test the build number against the maven project version.
     */
    public void testBuildVersion() {
        System.out.println("testBuildVersion");
        try {
            FacetedPlugin abstractBasePlugin = new FacetedPlugin();
            assertTrue(abstractBasePlugin.isMavenVersionCorrect());
        } catch (PluginException exception) {
            fail(exception.getMessage());
        }
    }
}
