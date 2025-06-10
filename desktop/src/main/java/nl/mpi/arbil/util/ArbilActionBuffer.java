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
package nl.mpi.arbil.util;

/**
 * Buffer mainly for reloading/sorting actions that get performed on request,
 * where multiple request may occur in a short time frame and repeated execution
 * is not desired. Such requests can be 'time-buffered' so that a set amount of
 * request idleness is required before the action is executed.
 *
 * The actual thread is not created until a request is put
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ArbilActionBuffer {

    private final Object actionLock;
    private String title;
    private int threadPriority;
    private int delay;
    private int maxDelay;
    private boolean actionRequested;
    private Thread workerThread;

    /**
     *
     * @param title Title of the thread that will be created
     * @param delay Amount of time that is waited after a request before action is executed
     * @param threadPriority Priority of thread that will execute actions
     */
    public ArbilActionBuffer(String title, int delay, int threadPriority) {
	this(title, delay, 0, new Object(), threadPriority);
    }

    /**
     *
     * @param title Title of the thread that will be created
     * @param delay Amount of time that is waited after a request before action is executed
     */
    public ArbilActionBuffer(String title, int delay) {
	this(title, delay, Thread.NORM_PRIORITY);
    }

    public ArbilActionBuffer(String title, int delay, int maxDelay, final Object lock) {
	this(title, delay, maxDelay, lock, Thread.NORM_PRIORITY);
    }

    /**
     *
     * @param title Title of the thread that will be created
     * @param delay Amount of time that is waited after a request before action is executed
     * @param lock External lock to use
     * @param threadPriority Priority of thread that will execute actions
     */
    public ArbilActionBuffer(String title, int delay, int maxDelay, final Object lock, int threadPriority) {
	this.actionLock = lock;
	this.delay = delay;
	this.title = title;
	this.maxDelay = maxDelay;
	this.threadPriority = threadPriority;
    }

    public void requestAction() {
	synchronized (actionLock) {
	    actionRequested = true;

	    if (workerThread == null || !workerThread.isAlive()) {
		workerThread = new Thread(new ArbilBufferedWorkerRunnable(), title);
		workerThread.setPriority(threadPriority);
		workerThread.start();
	    }
	}
    }

    public void requestActionAndNotify() {
	synchronized (actionLock) {
	    requestAction();
	    actionLock.notifyAll();
	}
    }

    private class ArbilBufferedWorkerRunnable implements Runnable {

	@Override
	public void run() {
	    // There may be new requests. If so, keep in the loop
	    while (actionRequested) {
		try {
		    // Go into wait for some short time while more actions are requested
		    waitForIncomingRequests();
		    // No requests have been added for some time, so execute the action
		    executeAction();
		} catch (InterruptedException ex) {
		    return;
		}
	    }
	}

	private void waitForIncomingRequests() throws InterruptedException {
	    long waitStartTime = System.currentTimeMillis();
	    synchronized (actionLock) {
		if (!actionRequested) {
		    // No action has been requested. Wait for one to be requested.
		    actionLock.wait();
		}

		while (actionRequested) {
		    // Action requested. Invalidate request.
		    actionRequested = false;

		    if (maxDelay > 0 && (System.currentTimeMillis() - waitStartTime) > maxDelay) {
			// Total waiting time exceeds maximum. Return in any case.
			return;
		    } else {
			// Give some time for another reload to be requested
			actionLock.wait(delay);
		    }
		}
	    }
	}
    }

    protected abstract void executeAction();
}
