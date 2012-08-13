package nl.mpi.kinnate.plugins;

import nl.mpi.kinnate.ui.KinDiagramPanel;

/**
 *  Document   : DiagramConnector
 *  Created on : Dec 20, 2011, 2:51:57 PM
 *  Author     : Peter Withers
 */
public class DiagramConnector {

    KinDiagramPanel diagramPanel;

    public DiagramConnector(KinDiagramPanel diagramPanel) {
        this.diagramPanel = diagramPanel;
    }

    public void requestRedraw() {
        diagramPanel.updateGraph();
    }
}
