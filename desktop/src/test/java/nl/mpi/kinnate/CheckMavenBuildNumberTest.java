package nl.mpi.kinnate;

import junit.framework.TestCase;

/**
 * Document : CheckMavenBuildNumberTest <br> Created on : Sep 25, 2012, 13:20:24
 *
 * @author : Peter Withers
 */
public class CheckMavenBuildNumberTest extends TestCase {

    public void testGetNodeTypeFromMimeType() {
        // this tests that the correct build number is specified in the pom.xml based on the current svn version
        // either the correct build number or a snapshot version are valid
        String errorMessage = "The maven version does not match either the snapshot nor the current svn build number.\n The pom.xml must be updated, please use either the correct build number or a snapshot version.";
        KinOathVersion kinoathVersion = new KinOathVersion();
        String svnVersion = kinoathVersion.currentMajor + "." + kinoathVersion.currentMinor + "." + kinoathVersion.currentRevision + "-";
        System.out.println("svnVersion: " + svnVersion + " ... ");
        String snapshotVersion = kinoathVersion.currentMajor + "." + kinoathVersion.currentMinor + "-";
        System.out.println("snapshotVersion: " + snapshotVersion + " ... " + "-SNAPSHOT");
        String mavenBuildVersion = kinoathVersion.artifactVersion;
        System.out.println("mavenBuildVersion: " + mavenBuildVersion);
        if (mavenBuildVersion.endsWith("-SNAPSHOT")) {
            assertTrue(errorMessage, mavenBuildVersion.startsWith(snapshotVersion));
        } else {
            assertTrue(errorMessage, mavenBuildVersion.startsWith(svnVersion));
        }

    }
}
