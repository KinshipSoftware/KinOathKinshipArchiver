package nl.mpi.arbil.plugin;

import java.net.URI;

/**
 * Document : PluginArbilDataNodeLoader <br> Created on Sep 10, 2012, 5:59:04 PM
 * <br>
 *
 * @author Peter Withers <br>
 */
public interface PluginArbilDataNodeLoader {

    PluginArbilDataNode getPluginArbilDataNode(Object registeringObject, URI localUri);
}
