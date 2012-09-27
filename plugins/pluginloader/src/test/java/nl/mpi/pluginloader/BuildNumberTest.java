package nl.mpi.pluginloader;

import junit.framework.TestCase;
import nl.mpi.arbil.plugin.PluginException;
import nl.mpi.kinnate.plugin.AbstractBasePlugin;

/**
 * Created on : Sep 27, 2012, 14:51
 *
 * @author Peter Withers
 */
public class BuildNumberTest extends TestCase {

    /**
     * Test the build number against the maven project version.
     */
    public void testBuildVersion() {
        System.out.println("testBuildVersion");
        try {
            AbstractBasePlugin abstractBasePlugin = new AbstractBasePluginImpl();
            assertTrue(abstractBasePlugin.isMavenVersionCorrect());
        } catch (PluginException exception) {
            fail(exception.getMessage());
        }
    }

    public class AbstractBasePluginImpl extends AbstractBasePlugin {

        public AbstractBasePluginImpl() throws PluginException {
            super("test name", "test description", "nl.mpi.pluginloader");
        }
    }
}
