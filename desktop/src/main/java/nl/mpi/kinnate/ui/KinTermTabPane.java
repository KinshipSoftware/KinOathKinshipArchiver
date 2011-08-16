package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.kintypestrings.KinTermGroup;

/**
 *  Document   : KinTermTabPane
 *  Created on : Apr 8, 2011, 4:24:56 PM
 *  Author     : Peter Withers
 */
public class KinTermTabPane extends JPanel {

    JTabbedPane kinTermTabbedPane;
    SavePanel savePanel;
    String defaultKinType = "";

    public KinTermTabPane(SavePanel savePanelLocal, KinTermGroup[] kinTermsArray) {
        savePanel = savePanelLocal;
        kinTermTabbedPane = new JTabbedPane();
        this.setLayout(new BorderLayout());
//        JMenuBar kintermMenuBar = new JMenuBar();
//        kintermMenuBar.add(new KinTermsMenu());
//        this.add(kintermMenuBar, BorderLayout.PAGE_START);
        this.add(kinTermTabbedPane, BorderLayout.CENTER);
        for (KinTermGroup kinTerms : kinTermsArray) {
            kinTermTabbedPane.add(kinTerms.titleString, new KinTermPanel(savePanelLocal, kinTerms, defaultKinType));
        }
    }

    public void updateKinTerms(KinTermGroup[] kinTermsArray) {
        int lastSelectedIndex = kinTermTabbedPane.getSelectedIndex();
        int lastTabCount = kinTermTabbedPane.getTabCount();
        kinTermTabbedPane.removeAll();
        for (KinTermGroup kinTerms : kinTermsArray) {
            kinTermTabbedPane.add(kinTerms.titleString, new KinTermPanel(savePanel, kinTerms, defaultKinType));
        }
        if (lastTabCount != kinTermTabbedPane.getTabCount()) {
            kinTermTabbedPane.setSelectedIndex(kinTermTabbedPane.getTabCount() - 1);
        } else {
            kinTermTabbedPane.setSelectedIndex(lastSelectedIndex);
        }
    }

    public KinTermGroup getSelectedKinTerms() {
        return ((KinTermPanel) kinTermTabbedPane.getSelectedComponent()).kinTerms;
    }

    public KinTermPanel getSelectedKinTermPanel() {
        return (KinTermPanel) kinTermTabbedPane.getSelectedComponent();
    }

    public void setAddableKinTypeSting(String kinTypeStrings) {
        defaultKinType = kinTypeStrings;
        for (Component tabComponent : kinTermTabbedPane.getComponents()) {
            KinTermPanel kinTermPanel = (KinTermPanel) tabComponent;
            kinTermPanel.setDefaultKinType(defaultKinType);
        }
    }
}
