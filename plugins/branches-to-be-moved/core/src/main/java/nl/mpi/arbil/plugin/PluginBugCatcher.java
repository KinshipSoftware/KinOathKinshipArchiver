package nl.mpi.arbil.plugin;

/**
 * Document : PluginBugCatcher <br> Created on Aug 15, 2012, 4:10:04 PM <br>
 *
 * @author Peter Withers <br>
 */
public interface PluginBugCatcher {

    public void logException(PluginException exception);
}
