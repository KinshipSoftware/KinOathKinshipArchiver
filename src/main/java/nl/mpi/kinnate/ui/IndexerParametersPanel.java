package nl.mpi.kinnate.ui;

import nl.mpi.kinnate.ui.KinTypeEgoSelectionTestPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import nl.mpi.kinnate.EntityIndexer.EntityIndex;
import nl.mpi.kinnate.GraphPanel;

/**
 *  Document   : IndexerParametersPanel
 *  Created on : Feb 14, 2011, 2:07:19 PM
 *  Author     : Peter Withers
 */
public class IndexerParametersPanel extends JTabbedPane {

    public IndexerParametersPanel(KinTypeEgoSelectionTestPanel egoSelectionTestPanel, GraphPanel graphPanel, EntityIndex entityIndex) {

        // todo: add drag drop of field to these lists and initially populate them from the SVG data
        this.add("Symbol Fields", new JScrollPane(new FieldSelectionList(egoSelectionTestPanel, graphPanel.getIndexParameters().symbolFieldsFields)));
//        this.add("Relation Fields", new JScrollPane(new FieldSelectionList(graphPanel.getIndexParameters().relevantLinkData)));
        this.add("Ancestor Fields", new JScrollPane(new FieldSelectionList(egoSelectionTestPanel, graphPanel.getIndexParameters().ancestorFields)));
        this.add("Decendant Fields", new JScrollPane(new FieldSelectionList(egoSelectionTestPanel, graphPanel.getIndexParameters().decendantFields)));
        this.add("Label Fields", new JScrollPane(new FieldSelectionList(egoSelectionTestPanel, graphPanel.getIndexParameters().labelFields)));
    }
}
