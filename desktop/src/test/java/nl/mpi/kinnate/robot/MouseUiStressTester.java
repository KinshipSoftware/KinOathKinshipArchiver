/**
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.robot;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.kinnate.KinOathVersion;
import nl.mpi.kinnate.KinnateArbilInjector;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.projects.ProjectManager;
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
    final private Robot robot;

    public MouseUiStressTester() throws AWTException, EntityServiceException {
        final ApplicationVersionManager versionManager = new ApplicationVersionManager(new KinOathVersion());
        final KinnateArbilInjector injector = new KinnateArbilInjector();
        injector.injectHandlers(versionManager);
        final AbstractDiagramManager abstractDiagramManager;
        final ProjectManager projectManager = new ProjectManager(injector.getSessionStorage());
        final SessionStorage sessionStorage = injector.getSessionStorage();
        abstractDiagramManager = new WindowedDiagramManager(versionManager, injector.getWindowManager(), sessionStorage, injector.getDataNodeLoader(), injector.getTreeHelper(), projectManager);
        abstractDiagramManager.newDiagram(new Rectangle(0, 0, 640, 480), projectManager.getDefaultProject(sessionStorage));
        abstractDiagramManager.createApplicationWindow();
        injector.getWindowManager().setMessagesCanBeShown(true);
        diagramComponent = (JFrame) abstractDiagramManager.getAllDiagrams()[0];
        applicationCenterX = diagramComponent.getX() + diagramComponent.getWidth() / 2;
        applicationCenterY = diagramComponent.getY() + diagramComponent.getHeight() / 2;
        robot = new Robot();
        diagramComponent.setExtendedState(JFrame.MAXIMIZED_BOTH);
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

    public void useAddRelationMenu() {
        robot.mouseMove(applicationCenterX + 20, applicationCenterY + 110);
        robot.delay(200);
        robot.mouseMove(applicationCenterX + 270, applicationCenterY + 130);
        robot.delay(200);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(200);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.delay(200);
    }

    public void dragDiagramAcross(int xDistance, int yDistance) {
        final int startX = applicationCenterX - xDistance / 2;
        final int startY = applicationCenterY - yDistance / 2;
        robot.mouseMove(startX, startY);
        robot.delay(200);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(200);
        for (int divisorValue = 0; divisorValue < 100; divisorValue++) {
//        for (int dragPos = -distance; dragPos < distance; dragPos += 10) {
//            robot.mouseMove(applicationCenterX + dragPos, applicationCenterY + dragPos);
            robot.mouseMove(startX + xDistance / 100 * divisorValue, startY + xDistance / 100 * divisorValue);
        }
        robot.delay(200);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.delay(200);
    }

    public void waitForEntityProcess() {
        robot.delay(2000);
    }

    public void selectAll() {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.delay(200);
        robot.keyPress(KeyEvent.VK_A);
        robot.delay(200);
        robot.keyRelease(KeyEvent.VK_A);
        robot.delay(200);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.delay(200);
    }

    static public void main(String[] args) {
        try {
            final MouseUiStressTester mouseUiStressTester = new MouseUiStressTester();
            new Thread() {
                @Override
                public void run() {
                    mouseUiStressTester.openContextMenu();
                    mouseUiStressTester.useAddMenu();
                    mouseUiStressTester.waitForEntityProcess();
                    mouseUiStressTester.dragDiagramAcross(100, 100);
                    mouseUiStressTester.dragDiagramAcross(100, 100);
                    mouseUiStressTester.dragDiagramAcross(100, 100);
                    mouseUiStressTester.dragDiagramAcross(100, 100);
                    mouseUiStressTester.dragDiagramAcross(100, 100);
                    mouseUiStressTester.dragDiagramAcross(100, 100);
                    for (int entityCount = 0; entityCount < 10000; entityCount++) {
                        mouseUiStressTester.openContextMenu();
                        mouseUiStressTester.useAddMenu();
                        mouseUiStressTester.waitForEntityProcess();
                        mouseUiStressTester.dragDiagramAcross(-10, 0);
                        // add a relation to all entities
                        mouseUiStressTester.selectAll();
                        mouseUiStressTester.openContextMenu();
                        mouseUiStressTester.useAddRelationMenu();
                        mouseUiStressTester.waitForEntityProcess();
                    }
                }
            }.start();
        } catch (AWTException exception) {
            System.out.println("Failed to start robot: " + exception.getMessage());
        } catch (EntityServiceException exception) {
            System.out.println("Failed to start robot: " + exception.getMessage());
        }
    }
}
