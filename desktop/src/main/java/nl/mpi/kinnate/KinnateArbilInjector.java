package nl.mpi.kinnate;

import nl.mpi.arbil.ArbilSwingInjector;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.arbil.util.ArbilMimeHashQueue;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.userstorage.KinSessionStorage;

/**
 * Takes care of injecting certain class instances into objects or classes.
 * This provides us with a sort of dependency injection, which enables loosening
 * the coupling between for example data classes and UI classes.
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 *
 * @author     : Peter Withers
 * Cut down version as required for kinship
 * Created on : May 3, 2011, 12:17:34 PM
 */
public class KinnateArbilInjector extends ArbilSwingInjector {

    private ArbilTreeHelper treeHelper;
    private ArbilMimeHashQueue mimeHashQueue;
    private ArbilWindowManager windowManager;
    private ArbilBugCatcher bugCatcher;
    private ArbilDataNodeLoader dataNodeLoader;
    private ArbilSessionStorage sessionStorage;
    private EntityCollection entityCollection;

    public synchronized void injectHandlers() {
        injectHandlers(new ApplicationVersionManager(new KinOathVersion()));
    }

    /**
     * Does initial injection into static classes. Needs to be called only once.
     */
    public synchronized void injectHandlers(final ApplicationVersionManager versionManager) {
        injectVersionManager(versionManager);

        sessionStorage = new KinSessionStorage();
        injectSessionStorage(sessionStorage);

        bugCatcher = new ArbilBugCatcher(sessionStorage, versionManager);
        sessionStorage.setBugCatcher(bugCatcher);
        injectBugCatcher(bugCatcher);

        windowManager = new ArbilWindowManager();
        windowManager.setBugCatcher(bugCatcher);
        windowManager.setSessionStorage(sessionStorage);
        windowManager.setVersionManager(versionManager);

        final MessageDialogHandler messageDialogHandler = windowManager;
        sessionStorage.setMessageDialogHandler(messageDialogHandler);
        injectDialogHandler(messageDialogHandler);

        sessionStorage.setWindowManager(windowManager);
        injectWindowManager(windowManager);

        mimeHashQueue = new ArbilMimeHashQueue(windowManager, sessionStorage);
        mimeHashQueue.setBugCatcher(bugCatcher);
        mimeHashQueue.setMessageDialogHandler(messageDialogHandler);
        injectMimeHashQueue(mimeHashQueue);

        treeHelper = new ArbilTreeHelper(sessionStorage, messageDialogHandler, bugCatcher);
        windowManager.setTreeHelper(treeHelper);
        sessionStorage.setTreeHelper(treeHelper);
        injectTreeHelper(treeHelper);

        dataNodeLoader = new ArbilDataNodeLoader(bugCatcher, messageDialogHandler, sessionStorage, mimeHashQueue, treeHelper);
        treeHelper.setDataNodeLoader(dataNodeLoader);
        mimeHashQueue.setDataNodeLoader(dataNodeLoader);
        windowManager.setDataNodeLoader(dataNodeLoader);
        injectDataNodeLoader(dataNodeLoader);

        entityCollection = new EntityCollection(sessionStorage, windowManager, bugCatcher);
    }

    /**
     * Should not be called before injectHandlers()!!
     * @return the treeHelper
     */
    public ArbilTreeHelper getTreeHelper() {
        return treeHelper;
    }

    /**
     * Should not be called before injectHandlers()!!
     * @return the treeHelper
     */
    public ArbilMimeHashQueue getMimeHashQueue() {
        return mimeHashQueue;
    }

    /**
     * Should not be called before injectHandlers()!!
     * @return the treeHelper
     */
    public ArbilWindowManager getWindowManager() {
        return windowManager;
    }

    /**
     * Should not be called before injectHandlers()!!
     * @return the treeHelper
     */
    public ArbilBugCatcher getBugCatcher() {
        return bugCatcher;
    }

    /**
     * Should not be called before injectHandlers()!!
     * @return the treeHelper
     */
    public ArbilDataNodeLoader getDataNodeLoader() {
        return dataNodeLoader;
    }

    public SessionStorage getSessionStorage() {
        return sessionStorage;
    }

    public EntityCollection getEntityCollection() {
        return entityCollection;
    }
}
