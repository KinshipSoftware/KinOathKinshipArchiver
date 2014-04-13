/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for
 * Psycholinguistics
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
package nl.mpi.kinnate;

import java.net.URI;
import nl.mpi.arbil.ArbilSwingInjector;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.ui.ArbilTableController;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.kinnate.userstorage.KinSessionStorage;

/**
 * Takes care of injecting certain class instances into objects or classes. This
 * provides us with a sort of dependency injection, which enables loosening the
 * coupling between for example data classes and UI classes.
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 *
 * @author Peter Withers
 *
 * Cut down version as required for kinship Created on : May 3, 2011, 12:17:34
 * PM
 */
public class KinnateArbilInjector extends ArbilSwingInjector {

    private ArbilTreeHelper treeHelper;
    private MimeHashQueue mimeHashQueue;
    private ArbilWindowManager windowManager;
    private ArbilDataNodeLoader dataNodeLoader;
    private KinSessionStorage sessionStorage;
    private ArbilTableController arbilTableController;

    public synchronized void injectHandlers() {
        injectHandlers(new ApplicationVersionManager(new KinOathVersion()));
    }

    /**
     * Does initial injection into static classes. Needs to be called only once.
     */
    public synchronized void injectHandlers(final ApplicationVersionManager versionManager) {
        injectVersionManager(versionManager);

        sessionStorage = new KinSessionStorage(versionManager);
        injectSessionStorage(sessionStorage);

        windowManager = new ArbilWindowManager();
        windowManager.setSessionStorage(sessionStorage);
        windowManager.setVersionManager(versionManager);

        final MessageDialogHandler messageDialogHandler = windowManager;
        sessionStorage.setMessageDialogHandler(messageDialogHandler);
        injectDialogHandler(messageDialogHandler);

        sessionStorage.setWindowManager(windowManager);
        injectWindowManager(windowManager);

        treeHelper = new ArbilTreeHelper(new ArbilSessionStorage(), messageDialogHandler); // here we need to use the arbil tree locations for the archive linker
        windowManager.setTreeHelper(treeHelper);
        sessionStorage.setTreeHelper(treeHelper);
        injectTreeHelper(treeHelper);

        mimeHashQueue = new MimeHashQueue() {

            @Override
            public void addToQueue(ArbilDataNode dataNode) {
                // kinoath does not need the MimeHashQueue functionality
            }

            @Override
            public void forceInQueue(ArbilDataNode dataNode) {
                // kinoath does not need the MimeHashQueue functionality
            }

            @Override
            public boolean isCheckResourcePermissions() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void setCheckResourcePermissions(boolean checkResourcePermissions) {
                // kinoath does not need the MimeHashQueue functionality
            }

            @Override
            public void startMimeHashQueueThread() {
                // kinoath does not need the MimeHashQueue functionality
            }

            @Override
            public void stopMimeHashQueueThread() {
                // kinoath does not need the MimeHashQueue functionality
            }

            @Override
            public String[] getMimeType(URI fileUri) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void terminateQueue() {
                // kinoath does not need the MimeHashQueue functionality
            }

            @Override
            public ArbilNode getActiveNode() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        dataNodeLoader = new ArbilDataNodeLoader(messageDialogHandler, sessionStorage, mimeHashQueue, treeHelper);
        treeHelper.setDataNodeLoader(dataNodeLoader);
        windowManager.setDataNodeLoader(dataNodeLoader);
        injectDataNodeLoader(dataNodeLoader);
        arbilTableController = new ArbilTableController(treeHelper, messageDialogHandler, windowManager);
    }

    /**
     * Should not be called before injectHandlers()!!
     *
     * @return the treeHelper
     */
    public ArbilTreeHelper getTreeHelper() {
        return treeHelper;
    }

//    /**
//     * Should not be called before injectHandlers()!!
//     *
//     * @return the treeHelper
//     */
//    public ArbilMimeHashQueue getMimeHashQueue() {
//        return mimeHashQueue;
//    }
    /**
     * Should not be called before injectHandlers()!!
     *
     * @return the treeHelper
     */
    public ArbilWindowManager getWindowManager() {
        return windowManager;
    }

    /**
     * Should not be called before injectHandlers()!!
     *
     * @return the treeHelper
     */
    public ArbilDataNodeLoader getDataNodeLoader() {
        return dataNodeLoader;
    }

    public SessionStorage getSessionStorage() {
        return sessionStorage;
    }

    public ArbilTableController getArbilTableController() {
        return arbilTableController;
    }
}
