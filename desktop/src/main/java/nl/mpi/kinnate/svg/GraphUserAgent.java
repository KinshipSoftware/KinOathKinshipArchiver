package nl.mpi.kinnate.svg;

import java.net.URI;
import java.net.URISyntaxException;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import org.apache.batik.swing.svg.SVGUserAgentGUIAdapter;

/**
 *  Document   : GraphUserAgent
 *  Created on : Oct 25, 2011, 11:53:00 AM
 *  Author     : Peter Withers
 */
public class GraphUserAgent extends SVGUserAgentGUIAdapter {

    GraphPanel graphPanel;

    public GraphUserAgent(GraphPanel parentComponent) {
        super(parentComponent);
        graphPanel = parentComponent;
    }

    @Override
    public void displayError(String message) {
        ArbilWindowManager.getSingleInstance().addMessageDialogToQueue(message, "SVG Error");
    }

    @Override
    public void displayError(Exception exception) {
        GuiHelper.linorgBugCatcher.logError(exception);
    }

    @Override
    public void displayMessage(String message) {
        ArbilWindowManager.getSingleInstance().addMessageDialogToQueue(message, "SVG Error");
    }

    @Override
    public void showAlert(String message) {
        ArbilWindowManager.getSingleInstance().addMessageDialogToQueue(message, "SVG Notification");
    }

    @Override
    public void openLink(String targetUri, boolean newc) {
        graphPanel.metadataPanel.removeAllArbilDataNodeRows();
        try {
            // put link target into the table
            final ArbilDataNode arbilDataNode = ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, new URI(targetUri));
            graphPanel.metadataPanel.addSingleArbilDataNode(arbilDataNode);
        } catch (URISyntaxException urise) {
            GuiHelper.linorgBugCatcher.logError(urise);
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
