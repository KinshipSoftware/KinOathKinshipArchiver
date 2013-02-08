package nl.mpi.kinnate.plugins.export;

import junit.framework.TestCase;
import nl.mpi.flap.plugin.PluginException;

/**
 * Created on : Sep 27, 2012, 16:57
 *
 * @author Peter Withers
 */
public class KinOathExportTest extends TestCase {

    /**
     * Test the build number against the maven project version.
     */
    public void testBuildVersion() {
        System.out.println("testBuildVersion");
        try {
            KinOathExport abstractBasePlugin = new KinOathExport();
            assertTrue(abstractBasePlugin.isMavenVersionCorrect());
        } catch (PluginException exception) {
            fail(exception.getMessage());
        }
    }
}
