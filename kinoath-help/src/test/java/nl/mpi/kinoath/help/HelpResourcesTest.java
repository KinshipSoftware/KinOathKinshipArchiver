package nl.mpi.kinoath.help;

import java.io.InputStream;
import junit.framework.TestCase;
import nl.mpi.flap.module.AbstractBaseModule;
import nl.mpi.flap.plugin.PluginException;

/**
 * Created on : Oct 11, 2012, 17:48
 *
 * @author Peter Withers
 */
public class HelpResourcesTest extends TestCase {

    /**
     * Test of getArtifactVersion method, of class AbstractBasePlugin.
     */
    public void testGetArtifactVersion() {
        System.out.println("getArtifactVersion");
        try {
            AbstractBaseModule abstractBasePlugin = new HelpResources();
            assertTrue(abstractBasePlugin.isMavenVersionCorrect());
        } catch (PluginException exception) {
            fail(exception.getMessage());
        }
    }

    public void testIndexResources() throws Exception {
        InputStream resourceAsStream = getClass().getResourceAsStream(HelpResources.HELP_INDEX_XML);
        assertNotNull("Expected IMDI index resource at " + HelpResources.HELP_INDEX_XML, resourceAsStream);
    }
}
