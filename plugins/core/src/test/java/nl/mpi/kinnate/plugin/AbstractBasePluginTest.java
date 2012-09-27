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
        // this tests that the correct build number is specified in the pom.xml based on the current svn version
        // either the correct build number or a snapshot version are valid
        String errorMessage = "The maven version does not match either the snapshot nor the current svn build number.\n The pom.xml must be updated, please use either the correct build number or a snapshot version.";
        try {
            AbstractBasePlugin abstractBasePlugin = new AbstractBasePluginImpl();
            String svnVersion = abstractBasePlugin.getMajorVersionNumber() + "." + abstractBasePlugin.getMinorVersionNumber() + "." + abstractBasePlugin.getBuildVersionNumber() + "-";
            System.out.println("svnVersion: " + svnVersion + " ... ");
            String snapshotVersion = abstractBasePlugin.getMajorVersionNumber() + "." + abstractBasePlugin.getMinorVersionNumber() + "-";
            System.out.println("snapshotVersion: " + snapshotVersion + " ... " + "-SNAPSHOT");
            String mavenBuildVersion = abstractBasePlugin.getArtifactVersion();
            System.out.println("mavenBuildVersion: " + mavenBuildVersion);
            if (mavenBuildVersion.endsWith("-SNAPSHOT")) {
                assertTrue(errorMessage, mavenBuildVersion.startsWith(snapshotVersion));
            } else {
                assertTrue(errorMessage, mavenBuildVersion.startsWith(svnVersion));
            }
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
