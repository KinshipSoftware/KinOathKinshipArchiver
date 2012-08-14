package nl.mpi.pluginloader;

/**
 * Document : KinOathPlugin
 * Created on : Dec 20, 2011, 2:49:57 PM
 * Author : Peter Withers
 */
public interface BasePlugin {

    public String getName();

    public int getMajorVersionNumber();

    public int getMinorVersionNumber();

    public int getBuildVersionNumber();

    public String getDescription();
}
