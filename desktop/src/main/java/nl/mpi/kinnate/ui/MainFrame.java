package nl.mpi.kinnate.ui;

import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.kinnate.KinOathVersion;
import nl.mpi.kinnate.KinnateArbilInjector;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;
import nl.mpi.kinnate.ui.window.WindowedDiagramManager;

/*
 * Document : MainFrame
 * Author : Peter Withers
 * Created on : Aug 16, 2010, 5:20:20 PM
 */
public class MainFrame extends javax.swing.JFrame {

    /** Creates new form MainFrame */
    public MainFrame() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                final ApplicationVersionManager versionManager = new ApplicationVersionManager(new KinOathVersion());
                final KinnateArbilInjector injector = new KinnateArbilInjector();
                injector.injectHandlers(versionManager);
                AbstractDiagramManager abstractDiagramManager;

//                abstractDiagramManager = new LayeredDiagramManager(versionManager);
//                abstractDiagramManager = new TabbedDiagramManager(versionManager);
                abstractDiagramManager = new WindowedDiagramManager(versionManager, injector.getWindowManager(), injector.getSessionStorage(), injector.getDataNodeLoader(), injector.getTreeHelper(), injector.getEntityCollection());
                abstractDiagramManager.newDiagram();
                abstractDiagramManager.createApplicationWindow();

                injector.getWindowManager().setMessagesCanBeShown(true);
//	if (arbilMenuBar.checkNewVersionAtStartCheckBoxMenuItem.isSelected()) {
                // todo: Ticket #1066 add the check for updates and check now menu items
                versionManager.checkForUpdate();
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
