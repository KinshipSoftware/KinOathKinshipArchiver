package nl.mpi.kinoath.help;

import nl.mpi.arbil.plugin.PluginException;
import nl.mpi.kinnate.plugin.AbstractBasePlugin;

/**
 * Document : HelpResources <br> Created on Oct 2, 2012, 11:53 <br> based on
 * ArbilHelpResources
 *
 * @author Peter Withers <br>
 */
public class HelpResources extends AbstractBasePlugin {

    public final static String HELP_RESOURCE_BASE = "/nl/mpi/kinoath/resources/html/help/kinoath/";
    public final static String HELP_INDEX_XML = HELP_RESOURCE_BASE + "kinoath.xml";

    public HelpResources() throws PluginException {
        super("KinOathHelp", "Package containing help resources. Not a plugin.", "nl.mpi.kinoath.help");
    }
}
