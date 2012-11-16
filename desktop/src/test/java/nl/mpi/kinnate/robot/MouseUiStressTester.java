package nl.mpi.kinnate.robot;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import javax.swing.JFrame;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.kinnate.KinOathVersion;
import nl.mpi.kinnate.KinnateArbilInjector;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;
import nl.mpi.kinnate.ui.window.WindowedDiagramManager;

/**
 * Created on : Nov 15, 2012, 12:46:14 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class MouseUiStressTester {

    final private JFrame diagramComponent;
    final private int applicationCenterX;
    final private int applicationCenterY;
    private Robot robot;

    public MouseUiStressTester() {
        final ApplicationVersionManager versionManager = new ApplicationVersionManager(new KinOathVersion());
        final KinnateArbilInjector injector = new KinnateArbilInjector();
        injector.injectHandlers(versionManager);
        final AbstractDiagramManager abstractDiagramManager;
        abstractDiagramManager = new WindowedDiagramManager(versionManager, injector.getWindowManager(), injector.getSessionStorage(), injector.getDataNodeLoader(), injector.getTreeHelper(), injector.getEntityCollection());
        abstractDiagramManager.newDiagram(new Rectangle(0, 0, 640, 480));
        abstractDiagramManager.createApplicationWindow();
        injector.getWindowManager().setMessagesCanBeShown(true);
        diagramComponent = (JFrame) abstractDiagramManager.getAllDiagrams()[0];
        applicationCenterX = diagramComponent.getX() + diagramComponent.getWidth() / 2;
        applicationCenterY = diagramComponent.getY() + diagramComponent.getHeight() / 2;
        try {
            robot = new Robot();
        } catch (AWTException exception) {
            System.out.println("Failed to start robot: " + exception.getMessage());
        }
    }

    public void openContextMenu() {
        robot.mouseMove(applicationCenterX, applicationCenterY);
        robot.delay(200);
        robot.mousePress(InputEvent.BUTTON3_MASK);
        robot.delay(200);
        robot.mouseRelease(InputEvent.BUTTON3_MASK);
        robot.delay(200);
//        diagramComponent.dispatchEvent(new MouseEvent(
//                //source - the Component that originated the event
//                diagramComponent,
//                //id - the integer that identifies the event
//                MouseEvent.BUTTON2_DOWN_MASK,
//                //when - a long int that gives the time the event occurred
//                System.currentTimeMillis(),
//                //modifiers - the modifier keys down during event (e.g. shift, ctrl, alt, meta) Either extended _DOWN_MASK or old _MASK modifiers should be used, but both models should not be mixed in one event. Use of the extended modifiers is preferred.
//                0,
//                //x - the horizontal x coordinate for the mouse location
//                applicationCenterX,
//                //y - the vertical y coordinate for the mouse location
//                applicationCenterY,
//                //clickCount - the number of mouse clicks associated with event
//                1,
//                //popupTrigger - a boolean, true if this event is a trigger for a popup menu
//                true));
        robot.delay(200);
    }

    public void useAddMenu() {
        robot.mouseMove(applicationCenterX + 20, applicationCenterY + 20);
        robot.delay(200);
        robot.mouseMove(applicationCenterX + 270, applicationCenterY + 20);
        robot.delay(200);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(200);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.delay(200);
    }

    public void dragDiagramAcross() {
        robot.mouseMove(applicationCenterX + 100, applicationCenterY);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(200);
        robot.mouseMove(applicationCenterX, applicationCenterY);
        robot.delay(200);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.delay(200);
    }

    public void waitForEntityAddProcess() {
        robot.delay(2000);
    }

    static public void main(String[] args) {
        final MouseUiStressTester mouseUiStressTester = new MouseUiStressTester();
        new Thread() {
            @Override
            public void run() {
                mouseUiStressTester.openContextMenu();
                mouseUiStressTester.useAddMenu();
                mouseUiStressTester.waitForEntityAddProcess();
                mouseUiStressTester.dragDiagramAcross();
            }
        }.start();
    }
}
