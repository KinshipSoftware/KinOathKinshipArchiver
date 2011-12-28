package nl.mpi.kinnate.svg;

import java.net.URI;
import java.net.URISyntaxException;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import org.apache.batik.swing.svg.SVGUserAgentGUIAdapter;

/**
 *  Document   : GraphUserAgent
 *  Created on : Oct 25, 2011, 11:53:00 AM
 *  Author     : Peter Withers
 */
public class GraphUserAgent extends SVGUserAgentGUIAdapter {

    private GraphPanel graphPanel;
    private MessageDialogHandler dialogHandler;
    private BugCatcher bugCatcher;
    private ArbilDataNodeLoader dataNodeLoader;

    public GraphUserAgent(GraphPanel parentComponent, MessageDialogHandler dialogHandler, BugCatcher bugCatcher, ArbilDataNodeLoader dataNodeLoader) {
        super(parentComponent);
        graphPanel = parentComponent;
        this.dialogHandler = dialogHandler;
        this.bugCatcher = bugCatcher;
        this.dataNodeLoader = dataNodeLoader;
    }

    @Override
    public void displayError(String message) {
        dialogHandler.addMessageDialogToQueue(message, "SVG Error");
    }

    @Override
    public void displayError(Exception exception) {
        bugCatcher.logError(exception);
    }

    @Override
    public void displayMessage(String message) {
        dialogHandler.addMessageDialogToQueue(message, "SVG Error");
    }

    @Override
    public void showAlert(String message) {
        dialogHandler.addMessageDialogToQueue(message, "SVG Notification");
    }

    @Override
    public void openLink(String targetUri, boolean newc) {
        graphPanel.metadataPanel.removeAllArbilDataNodeRows();
        try {
            // put link target into the table
            final ArbilDataNode arbilDataNode = dataNodeLoader.getArbilDataNode(null, new URI(targetUri));
            graphPanel.metadataPanel.addArbilDataNode(arbilDataNode);
        } catch (URISyntaxException urise) {
            bugCatcher.logError(urise);
        }
        // set the graph selection
        graphPanel.setSelectedIds(new UniqueIdentifier[]{});
        graphPanel.metadataPanel.updateEditorPane();
        // todo: provide a url for the imdiviewer or to launch arbil
//        try {
//            GuiHelper.getSingleInstance().openFileInExternalApplication(new URI(targetUri));
//        } catch (URISyntaxException exception) {
//            GuiHelper.linorgBugCatcher.logError(exception);
//        }
    }
}
