package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.kintypestrings.KinTerms;

/**
 *  Document   : KinTermTabPane
 *  Created on : Apr 8, 2011, 4:24:56 PM
 *  Author     : Peter Withers
 */
public class KinTermTabPane extends JPanel {

    JTabbedPane tabbedPane;
    SavePanel savePanel;
    String defaultKinType = "";

    public KinTermTabPane(SavePanel savePanelLocal, KinTerms[] kinTermsArray) {
        savePanel = savePanelLocal;
        tabbedPane = new JTabbedPane();
        this.setLayout(new BorderLayout());
        JMenuBar kintermMenuBar = new JMenuBar();
        kintermMenuBar.add(new KinTermsMenu());
        this.add(kintermMenuBar, BorderLayout.PAGE_START);
        this.add(tabbedPane, BorderLayout.CENTER);
        for (KinTerms kinTerms : kinTermsArray) {
            tabbedPane.add(kinTerms.titleString, new KinTermPanel(savePanelLocal, kinTerms, defaultKinType));
        }
    }

    public void updateKinTerms(KinTerms[] kinTermsArray) {
        int lastSelectedIndex = tabbedPane.getSelectedIndex();
        int lastTabCount = tabbedPane.getTabCount();
        tabbedPane.removeAll();
        for (KinTerms kinTerms : kinTermsArray) {
            tabbedPane.add(kinTerms.titleString, new KinTermPanel(savePanel, kinTerms, defaultKinType));
        }
        if (lastTabCount != tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        } else {
            tabbedPane.setSelectedIndex(lastSelectedIndex);
        }
    }

    public KinTerms getSelectedKinTerms() {
        return ((KinTermPanel) tabbedPane.getSelectedComponent()).kinTerms;
    }

    public KinTermPanel getSelectedKinTermPanel() {
        return (KinTermPanel) tabbedPane.getSelectedComponent();
    }

    public void setAddableKinTypeSting(String kinTypeStrings) {
        defaultKinType = kinTypeStrings;
        for (Component tabComponent : tabbedPane.getComponents()) {
            KinTermPanel kinTermPanel = (KinTermPanel) tabComponent;
            kinTermPanel.setDefaultKinType(defaultKinType);
        }
    }
}
