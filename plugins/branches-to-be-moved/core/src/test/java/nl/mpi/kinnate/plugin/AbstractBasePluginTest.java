package nl.mpi.kinnate.plugin;

import junit.framework.TestCase;
import nl.mpi.arbil.plugin.PluginException;

/**
 * Created on : Sep 27, 2012, 11:30
 *
 * @author Peter Withers
 */
public class AbstractBasePluginTest extends TestCase {

    /**
     * Test of getArtifactVersion method, of class AbstractBasePlugin.
     */
    public void testGetArtifactVersion() {
        System.out.println("getArtifactVersion");
        try {
            AbstractBasePlugin abstractBasePlugin = new AbstractBasePluginImpl();
            assertTrue(abstractBasePlugin.isMavenVersionCorrect());
        } catch (PluginException exception) {
            fail(exception.getMessage());
        }
    }

    public class AbstractBasePluginImpl extends AbstractBasePlugin {

        public AbstractBasePluginImpl() throws PluginException {
            super("test name", "test description", "nl.mpi.kinnate.plugin");
        }
    }
}
