/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.data;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import nl.mpi.arbil.util.XsdChecker;
import nl.mpi.arbil.util.task.ArbilTask;
import nl.mpi.arbil.util.task.ArbilTaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the loader threads and queues for loading ArbilDataNodes. It also has the implementation of the actual loading threads. Structure
 * and relation between this class and te DataNodeLoader should to be reconsidered.
 * Used by DataNodeLoader.
 *
 * @see nl.mpi.arbil.data.DataNodeLoader
 *
 * @author Peter Wither <peter.withers@mpi.nl>
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class DataNodeLoaderThreadManager {

    private final static Logger logger = LoggerFactory.getLogger(DataNodeLoaderThreadManager.class);
    private final static int MAX_REMOTE_THREADS = 6;
    private final static int MAX_LOCAL_THREADS = 6;
    private boolean continueThread = true;
    private int arbilFilesLoaded = 0;
    private int remoteArbilFilesLoaded = 0;
    private int threadStartCounter = 0;
    private ThreadGroup remoteLoaderThreadGroup;
    private ThreadGroup localLoaderThreadGroup;
    private final Vector<ArbilDataNode> arbilRemoteNodesToInit = new Vector<ArbilDataNode>();
    private final Vector<ArbilDataNode> arbilLocalNodesToInit = new Vector<ArbilDataNode>();
    private boolean schemaCheckLocalFiles = false;
    private final Map<ArbilDataNode, Collection<Entry<ArbilDataNode, ArbilDataNodeLoaderCallBack>>> callbacksMap = Collections.synchronizedMap(new HashMap<ArbilDataNode, Collection<Entry<ArbilDataNode, ArbilDataNodeLoaderCallBack>>>());

    public DataNodeLoaderThreadManager() {
	continueThread = true;
	remoteLoaderThreadGroup = new ThreadGroup("RemoteLoaderThreads");
	localLoaderThreadGroup = new ThreadGroup("LocalLoaderThreads");
    }

    /**
     * Registers a new callback for the specified data node that should be called after the first successful reload of the specified node.
     * The callback will never be triggered more than once for each registration. If you want to register a reload, call this method before
     * you make the call to {@link #addNodeToQueue(nl.mpi.arbil.data.ArbilDataNode) }
     *
     * @param dataNode node to register callback for
     * @param callback callback object to trigger after reload
     */
    public void addLoaderCallback(ArbilDataNode dataNode, ArbilDataNodeLoaderCallBack callback) {
	synchronized (callbacksMap) {
	    // map by parent dom node, reload always happens on parent dom
	    final ArbilDataNode parentDomNode = dataNode.getParentDomNode();
	    Collection<Entry<ArbilDataNode, ArbilDataNodeLoaderCallBack>> parentDomNodeCallbacks = callbacksMap.get(parentDomNode);
	    if (parentDomNodeCallbacks == null) {
		// each map entry has a tuple {data node (child node of reloaded parent dom), callback}
		// this way the callback can be called on the child node for which it was registered
		parentDomNodeCallbacks = new ArrayList<Entry<ArbilDataNode, ArbilDataNodeLoaderCallBack>>();
		callbacksMap.put(parentDomNode, parentDomNodeCallbacks);
	    }
	    parentDomNodeCallbacks.add(new AbstractMap.SimpleImmutableEntry<ArbilDataNode, ArbilDataNodeLoaderCallBack>(dataNode, callback));
	}
    }

    /**
     *
     * @param dataNode parent dom node to call callbacks for
     */
    private void callCallBacks(ArbilDataNode dataNode) {
	final Collection<Entry<ArbilDataNode, ArbilDataNodeLoaderCallBack>> callbacks = callbacksMap.remove(dataNode);
	if (callbacks != null) {
	    for (Entry<ArbilDataNode, ArbilDataNodeLoaderCallBack> callbackEntry : callbacks) {
		final ArbilDataNode targetNode = callbackEntry.getKey();
		final ArbilDataNodeLoaderCallBack callback = callbackEntry.getValue();
		if (callback != null) {
		    callback.dataNodeLoaded(targetNode);
		}
	    }
	}
    }

    public void addNodeToQueue(ArbilDataNode nodeToAdd) {
	startLoaderThreads();
	if (ArbilDataNode.isStringLocal(nodeToAdd.getUrlString())) {
	    synchronized (arbilLocalNodesToInit) {
		if (!arbilLocalNodesToInit.contains(nodeToAdd)) {
		    nodeToAdd.updateLoadingState(+1);
		    arbilLocalNodesToInit.addElement(nodeToAdd);
		    arbilLocalNodesToInit.notifyAll();
		}
	    }
	} else {
	    synchronized (arbilRemoteNodesToInit) {
		if (!arbilRemoteNodesToInit.contains(nodeToAdd)) {
		    nodeToAdd.updateLoadingState(+1);
		    arbilRemoteNodesToInit.addElement(nodeToAdd);
		    arbilRemoteNodesToInit.notifyAll();
		}
	    }
	}
    }

    private ArbilDataNode getNodeFromQueue(Vector<ArbilDataNode> dataNodesQueue) {
	synchronized (dataNodesQueue) {
	    if (dataNodesQueue.size() > 0) {
		ArbilDataNode tempDataNode = dataNodesQueue.remove(0);
		if (tempDataNode.lockedByLoadingThread) {
		    dataNodesQueue.add(tempDataNode);
		    return null;
		} else {
		    tempDataNode.lockedByLoadingThread = true;
		    dataNodesQueue.notifyAll();
		    return tempDataNode;
		}
	    } else {
		return null;
	    }
	}
    }

    synchronized public void startLoaderThreads() {
	// start the remote imdi loader threads
	while (isContinueThread() && remoteExecutor.getActiveCount() < MAX_REMOTE_THREADS()) {
	    remoteExecutor.submit(new RemoteLoader());
	}
	// due to an apparent deadlock in the imdi api only one thread is used for local files. the deadlock appears to be in the look up host area
	// start the local imdi threads
	while (isContinueThread() && localExecutor.getActiveCount() < MAX_LOCAL_THREADS()) {
	    localExecutor.submit(new LocalLoader());
	}
    }

    synchronized void stopLoaderThreads() {
	remoteExecutor.shutdownNow();
	localExecutor.shutdownNow();
    }

    /**
     * @return the schemaCheckLocalFiles
     */
    public boolean isSchemaCheckLocalFiles() {
	return schemaCheckLocalFiles;
    }

    /**
     * @param schemaCheckLocalFiles the schemaCheckLocalFiles to set
     */
    public void setSchemaCheckLocalFiles(boolean schemaCheckLocalFiles) {
	this.schemaCheckLocalFiles = schemaCheckLocalFiles;
    }

    /**
     * @return the continueThread
     */
    public boolean isContinueThread() {
	return continueThread;
    }

    /**
     * @param continueThread the continueThread to set
     */
    public void setContinueThread(boolean continueThread) {
	this.continueThread = continueThread;
    }

    /**
     * @return the MAX_REMOTE_THREADS
     */
    public static int MAX_REMOTE_THREADS() {
	return MAX_REMOTE_THREADS;
    }

    /**
     * @return the MAX_LOCAL_THREADS
     */
    public static int MAX_LOCAL_THREADS() {
	return MAX_LOCAL_THREADS;
    }

    protected void beforeExecuteLoaderThread(Thread t, Runnable r, boolean local) {
    }

    protected void afterExecuteLoaderThread(Runnable r, Throwable t, boolean local) {
    }
    private ScheduledThreadPoolExecutor remoteExecutor = new ScheduledThreadPoolExecutor(MAX_REMOTE_THREADS()) {
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
	    beforeExecuteLoaderThread(t, r, false);
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
	    afterExecuteLoaderThread(r, t, false);
	}

	@Override
	public ThreadFactory getThreadFactory() {
	    return new ThreadFactory() {
		public Thread newThread(Runnable r) {
		    String threadName = "ArbilDataNodeLoader-remote-" + threadStartCounter++;
		    Thread thread = new Thread(remoteLoaderThreadGroup, r, threadName);
		    thread.setPriority(Thread.NORM_PRIORITY - 1);
		    return thread;
		}
	    };
	}
    };
    private ScheduledThreadPoolExecutor localExecutor = new ScheduledThreadPoolExecutor(MAX_LOCAL_THREADS()) {
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
	    beforeExecuteLoaderThread(t, r, true);
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
	    afterExecuteLoaderThread(r, t, true);
	}

	@Override
	public ThreadFactory getThreadFactory() {
	    return new ThreadFactory() {
		public Thread newThread(Runnable r) {
		    String threadName = "ArbilDataNodeLoader-local-" + threadStartCounter++;
		    Thread thread = new Thread(localLoaderThreadGroup, r, threadName);
		    thread.setPriority(Thread.NORM_PRIORITY - 1);
		    return thread;
		}
	    };
	}
    };

    private abstract class Loader implements Runnable {
	// this has been separated in to two separate threads to prevent long delays when there is no server connection
	// each node is loaded one at a time and must time out before the next is started
	// the local corpus nodes are the fastest so they are now loaded in a separate thread
	// alternatively a thread pool may be an option

	protected abstract void loadNode(ArbilDataNode currentArbilDataNode);

	protected abstract Vector<ArbilDataNode> getNodesToInit();

	public void run() {
	    ArbilDataNode currentArbilDataNode = null;
	    while (isContinueThread() && !Thread.currentThread().isInterrupted()) {
		try {
		    currentArbilDataNode = waitForNodes(getNodesToInit());
		} catch (InterruptedException ex) {
		    logger.debug("{} interrupted. ", Thread.currentThread().getName(), ex);
		    return;
		}
		if (currentArbilDataNode != null) {
		    loadNode(currentArbilDataNode);
		}
	    }
	}

	private ArbilDataNode waitForNodes(Vector<ArbilDataNode> queue) throws InterruptedException {
	    synchronized (queue) {
		while (queue.isEmpty()) {
		    queue.wait();
		}
		return getNodeFromQueue(queue);
	    }
	}
    }
    private Set<ArbilTaskListener> taskListeners = new HashSet<ArbilTaskListener>();
    private int loadingNodesCount = 0;
    private ArbilTask loadingTask = null;

    private synchronized void addToLoadingTask(int count) {
	loadingNodesCount += count;
	if (loadingNodesCount == 0) {
	    // TODO
	}
    }

    /** *
     * Runnable that gets a node from the remote queue and loads it
     */
    private class RemoteLoader extends Loader {

	@Override
	protected Vector<ArbilDataNode> getNodesToInit() {
	    return arbilRemoteNodesToInit;
	}

	protected void loadNode(ArbilDataNode currentArbilDataNode) {
	    try {
		currentArbilDataNode.loadArbilDom();
		currentArbilDataNode.updateLoadingState(-1);
		currentArbilDataNode.clearIcon();
		currentArbilDataNode.clearChildIcons();
		remoteArbilFilesLoaded++;
	    } finally {
		currentArbilDataNode.lockedByLoadingThread = false;
		currentArbilDataNode.notifyLoaded();
	    }

	    callCallBacks(currentArbilDataNode);
	}
    }

    /**
     * Runnable that gets a node from the local queue and loads it
     */
    private class LocalLoader extends Loader {

	@Override
	protected Vector<ArbilDataNode> getNodesToInit() {
	    return arbilLocalNodesToInit;
	}

	protected void loadNode(final ArbilDataNode currentArbilDataNode) {
	    if (currentArbilDataNode.getNeedsSaveToDisk(false)) {
		currentArbilDataNode.saveChangesToCache(false);
	    }
	    currentArbilDataNode.loadArbilDom();
	    if (isSchemaCheckLocalFiles()) {
		if (currentArbilDataNode.isMetaDataNode()) {
		    XsdChecker xsdChecker = new XsdChecker();
		    String checkerResult;
		    checkerResult = xsdChecker.simpleCheck(currentArbilDataNode.getFile());
		    currentArbilDataNode.hasSchemaError = (checkerResult != null);
		}
	    } else {
		currentArbilDataNode.hasSchemaError = false;
	    }
	    try {
		currentArbilDataNode.updateLoadingState(-1);
		currentArbilDataNode.clearIcon();
		currentArbilDataNode.clearChildIcons();
		arbilFilesLoaded++;
	    } finally {
		currentArbilDataNode.lockedByLoadingThread = false;
		currentArbilDataNode.notifyLoaded();
	    }
	    callCallBacks(currentArbilDataNode);
	}
    }
}
