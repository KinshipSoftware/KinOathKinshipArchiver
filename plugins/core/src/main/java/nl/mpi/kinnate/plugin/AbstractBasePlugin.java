package nl.mpi.kinnate.plugin;

import java.io.IOException;
import java.util.Properties;
import nl.mpi.arbil.plugin.PluginException;

/**
 * Document : AbstractBasePlugin Created on : Sep 27, 2012, 11:30
 *
 * @author Peter Withers
 */
public abstract class AbstractBasePlugin implements BasePlugin {

    final private String nameString;
    final private String descriptionString;
    final private int majorVersionNumber;
    final private int minorVersionNumber;
    final private int buildVersionNumber;
    final private String compileDateString;
    final private String artifactVersionString;
    final private String lastCommitDate;

    public AbstractBasePlugin(String nameString, String descriptionString, String packageString) throws PluginException {
        this.nameString = nameString;
        this.descriptionString = descriptionString;

        Properties properties = new Properties();
        try {
            String propertiesPath = packageString.replace(".", "/");
            properties.load(getClass().getResourceAsStream("/" + propertiesPath + "/version.properties"));
            majorVersionNumber = Integer.parseInt(properties.getProperty("plugin.majorVersion"));
            minorVersionNumber = Integer.parseInt(properties.getProperty("plugin.minorVersion"));
            buildVersionNumber = Integer.parseInt(properties.getProperty("plugin.buildVersion"));
            lastCommitDate = properties.getProperty("plugin.lastCommitDate");
            compileDateString = properties.getProperty("plugin.compileDate");
            artifactVersionString = properties.getProperty("plugin.projectVersion");
        } catch (IOException ex) {
            throw new PluginException("Version properties could not be read!");
        }
    }

    public String getName() {
        return nameString;
    }

    public int getMajorVersionNumber() {
        return majorVersionNumber;
    }

    public int getMinorVersionNumber() {
        return minorVersionNumber;
    }

    public int getBuildVersionNumber() {
        return buildVersionNumber;
    }

    public String getCompileDate() {
        return compileDateString;
    }

    public String getLastCommitDate() {
        return lastCommitDate;
    }

    public String getArtifactVersion() {
        return artifactVersionString;
    }

    public String getDescription() {
        return descriptionString;
    }
}
