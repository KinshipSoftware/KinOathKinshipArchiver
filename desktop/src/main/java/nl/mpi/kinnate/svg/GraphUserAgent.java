package nl.mpi.kinnate.svg;

import java.awt.Component;
import java.net.URI;
import java.net.URISyntaxException;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;
import org.apache.batik.swing.svg.SVGUserAgentGUIAdapter;

/**
 *  Document   : GraphUserAgent
 *  Created on : Oct 25, 2011, 11:53:00 AM
 *  Author     : Peter Withers
 */
public class GraphUserAgent extends SVGUserAgentGUIAdapter {

    public GraphUserAgent(Component parentComponent) {
        super(parentComponent);
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
        try {
            GuiHelper.getSingleInstance().openFileInExternalApplication(new URI(targetUri));
        } catch (URISyntaxException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        }
    }
}
