package nl.mpi.kinnate.ui;

import java.io.IOException;
import java.util.Arrays;
import nl.mpi.arbil.ui.HelpViewerPanel;
import nl.mpi.arbil.ui.HelpViewerPanel.HelpResourceSet;
import org.xml.sax.SAXException;

/**
 * Document : KinOathHelp.java Created on : March 9, 2009, 1:38 PM
 *
 * @author Peter Withers <Peter.Withers@mpi.nl>
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class KinOathHelp extends HelpViewerPanel {

    public final static String KMDI_HELPSET = "KMDI";
    private final static String KMDI_HELP_RESOURCE_BASE = "/nl/mpi/kinnate/resources/html/help/kinoath/";
    private final static HelpResourceSet KMDI_HELP_SET = new HelpResourceSet(KMDI_HELPSET, KinOathHelp.class, KMDI_HELP_RESOURCE_BASE, KMDI_HELP_RESOURCE_BASE + "kinoath.xml");
    private static KinOathHelp singleInstance = null;

    public static synchronized KinOathHelp getArbilHelpInstance() throws IOException, SAXException {
        //TODO: This should not be a singleton...
        if (singleInstance == null) {

            singleInstance = new KinOathHelp();
        }
        return singleInstance;
    }

    public KinOathHelp() throws IOException, SAXException {
        super(Arrays.asList(KMDI_HELP_SET));
    }
}
