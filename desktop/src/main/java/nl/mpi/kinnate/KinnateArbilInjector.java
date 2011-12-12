package nl.mpi.kinnate;

import java.awt.datatransfer.ClipboardOwner;
import nl.mpi.arbil.ArbilInjector;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilMimeHashQueue;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.WindowManager;
import nl.mpi.kinnate.ui.menu.HelpMenu;
//import nl.mpi.kinnate.userstorage.KinSessionStorage;

/**
 * Takes care of injecting certain class instances into objects or classes.
 * This provides us with a sort of dependency injection, which enables loosening
 * the coupling between for example data classes and UI classes.
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * Cut down version as required for kinship
 * Created on : May 3, 2011, 12:17:34 PM
 * @author     : Peter Withers
 */
public class KinnateArbilInjector extends ArbilInjector {

    /**
     * Does initial injection into static classes. Needs to be called only once.
     */
    public static synchronized void injectHandlers(final ApplicationVersionManager versionManager) {
        injectVersionManager(versionManager);

        final BugCatcher bugCatcher = GuiHelper.linorgBugCatcher;
         ArbilSessionStorage.setBugCatcher(bugCatcher);
//KinSessionStorage.setBugCatcher(bugCatcher);
        ArbilMimeHashQueue.setBugCatcher(bugCatcher);
        injectBugCatcher(bugCatcher);

        final MessageDialogHandler messageDialogHandler = ArbilWindowManager.getSingleInstance();
        ArbilSessionStorage.setMessageDialogHandler(messageDialogHandler);
        ArbilMimeHashQueue.setMessageDialogHandler(messageDialogHandler);
        injectDialogHandler(messageDialogHandler);

        final WindowManager windowManager = ArbilWindowManager.getSingleInstance();
        ArbilSessionStorage.setWindowManager(windowManager);
        injectWindowManager(windowManager);

        final ClipboardOwner clipboardOwner = GuiHelper.getClipboardOwner();
        injectClipboardOwner(clipboardOwner);

        ArbilSessionStorage.setBugCatcher(bugCatcher);
        // Ticket #1305 Move the kinoath working directory out of the .arbil directory into a .kinoath directory.
        final SessionStorage sessionStorage = ArbilSessionStorage.getSingleInstance();
        ArbilDataNodeLoader.setSessionStorage(sessionStorage);
        ArbilMimeHashQueue.setSessionStorage(sessionStorage);
        injectSessionStorage(sessionStorage);

        final MimeHashQueue mimeHashQueue = ArbilMimeHashQueue.getSingleInstance();
        injectMimeHashQueue(mimeHashQueue);

        final DataNodeLoader dataNodeLoader = ArbilDataNodeLoader.getSingleInstance();
        ArbilMimeHashQueue.setDataNodeLoader(dataNodeLoader);
        injectDataNodeLoader(dataNodeLoader);

        HelpMenu.setVersionManager(versionManager);

//        final TreeHelper treeHelper = ArbilTreeHelper.getSingleInstance();
//        injectTreeHelper(treeHelper);
    }
}
