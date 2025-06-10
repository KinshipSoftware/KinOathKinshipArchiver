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
package nl.mpi.arbil.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import nl.mpi.arbil.util.task.ArbilTask;
import nl.mpi.arbil.util.task.ArbilTaskEvent;
import nl.mpi.arbil.util.task.ArbilTaskEvent.ArbilTaskEventType;
import nl.mpi.arbil.util.task.ArbilTaskListener;

/**
 * Status bar for Arbil that shows progress bars for tasks it gets subscribed to
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilTaskStatusBar extends JPanel implements ArbilTaskListener {

    public static final int STATUS_BAR_HEIGHT = 25;
    public static final int PROGRESS_AREA_WIDTH = 400;
    private JPanel progressArea;
    private HashMap<ArbilTask, ArbilTaskProgressPanel> progressPanels;

    public ArbilTaskStatusBar() {
	super();
	progressPanels = new HashMap<ArbilTask, ArbilTaskProgressPanel>();

	setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY));
	// Create an area that will contain all the progress panels
	progressArea = new JPanel();
	progressArea.setLayout(new BoxLayout(progressArea, BoxLayout.PAGE_AXIS));
	progressArea.setSize(new Dimension(PROGRESS_AREA_WIDTH, STATUS_BAR_HEIGHT));
	progressArea.setPreferredSize(new Dimension(PROGRESS_AREA_WIDTH, STATUS_BAR_HEIGHT));
	progressArea.setMinimumSize(new Dimension(PROGRESS_AREA_WIDTH, STATUS_BAR_HEIGHT));
	// Some margins around the area
	progressArea.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 10));
	setLayout(new BorderLayout());
	add(progressArea, BorderLayout.EAST);

	revalidate();
	repaint();
    }

    @Override
    public Dimension getMinimumSize() {
	return new Dimension(getParent().getWidth(), STATUS_BAR_HEIGHT);
    }

    @Override
    public Dimension getPreferredSize() {
	return new Dimension(getParent().getWidth(), STATUS_BAR_HEIGHT);
    }

    /**
     * Callback for publishers of task events
     * @param taskEvent 
     */
    public void notifyTask(final ArbilTaskEvent taskEvent) {
	// All events will trigger mainly UI-related events so do work on EDT
	SwingUtilities.invokeLater(new Runnable() {

	    public void run() {
		final ArbilTask task = taskEvent.getSource();
		// Get panel for task from map (if not there will return null)
		ArbilTaskProgressPanel panel = progressPanels.get(task);

		// Add panel if needed
		if (taskEvent.getEventType() == ArbilTaskEventType.STARTED && panel == null) {
		    panel = createProgressPanel(task);
		}
		if (panel != null) {
		    // Let panel do the actual handling
		    panel.handleEvent(taskEvent);
		    // Remove panel if needed
		    if (taskEvent.getEventType() == ArbilTaskEventType.COMPLETED
			    || taskEvent.getEventType() == ArbilTaskEventType.CANCELED) {
			removeTask(task);
		    }
		}
	    }
	});
    }

    /**
     * Will create a new progress panel for the specified task and add it to both the map and the UI
     * @param task
     * @return 
     */
    private synchronized ArbilTaskProgressPanel createProgressPanel(final ArbilTask task) {
	ArbilTaskProgressPanel progressPanel = new ArbilTaskProgressPanel(task);
	progressPanels.put(task, progressPanel);
	progressArea.add(progressPanel);
	revalidate();
	repaint();
	return progressPanel;
    }

    /**
     * Wil remove the panel for the specified task form both the map and the UI
     * @param task 
     */
    private synchronized void removeTask(ArbilTask task) {
	ArbilTaskProgressPanel bar = progressPanels.remove(task);
	if (bar != null) {
	    progressArea.remove(bar);
	    revalidate();
	    repaint();
	}
    }

    /**
     * Panel for displaying task progress with the task's name as a label next to it
     */
    private static class ArbilTaskProgressPanel extends JPanel {

	private JProgressBar progressBar;
	private JLabel label;
	private ArbilTask task;

	public ArbilTaskProgressPanel(final ArbilTask task) {
	    this.task = task;

	    progressBar = new JProgressBar(0, task.getTargetValue());
	    progressBar.setStringPainted(true);

	    label = new JLabel(task.getName());

	    setLayout(new BorderLayout());
	    add(label, BorderLayout.WEST);
	    add(progressBar, BorderLayout.CENTER);
	    progressBar.setSize(200, STATUS_BAR_HEIGHT);

	    setToolTipText("");

	    revalidate();
	    repaint();
	}

	@Override
	public Dimension getMinimumSize() {
	    return new Dimension(PROGRESS_AREA_WIDTH, STATUS_BAR_HEIGHT);
	}

	@Override
	public Dimension getPreferredSize() {
	    return new Dimension(getParent().getWidth(), STATUS_BAR_HEIGHT);
	}

	@Override
	public String getToolTipText(MouseEvent event) {
	    return ArbilTaskProgressPanel.getToolTipText(task);
	}

	public void handleEvent(final ArbilTaskEvent taskEvent) {
	    switch (taskEvent.getEventType()) {
		case CHANGED:
		    update();
		    return;
		case STARTED:
		case PROGRESS:
		    setProgress();
		    return;
		default:
		// No actions for COMPLETED and  CANCELED
	    }
	}

	/**
	 * Updates name, description etc for task
	 */
	private synchronized void update() {
	    label.setText(task.getName());
	}

	/**
	 * Updates the progress
	 */
	private synchronized void setProgress() {
	    if (task.isIndeterminate()) {
		if (!progressBar.isIndeterminate()) {
		    progressBar.setIndeterminate(true);
		}
		if (task.getStatus() != null) {
		    progressBar.setString(task.getStatus());
		}
	    } else {
		if (task.getTargetValue() != progressBar.getMaximum()) {
		    progressBar.setMaximum(task.getTargetValue());
		}
		progressBar.setValue(task.getProgressValue());
	    }
	    progressBar.setString(task.getProgressString());
	}

	/**
	 * Creates tooltip text for task
	 * @param task
	 * @return 
	 */
	private static String getToolTipText(ArbilTask task) {
	    StringBuilder toolTipBuilder = new StringBuilder();
	    if (task.getDescription() != null) {
		toolTipBuilder.append(task.getDescription());
		toolTipBuilder.append("<br /><br />");
	    }

	    if (task.getStatus() != null) {
		toolTipBuilder.append(task.getStatus());
	    }

	    if (toolTipBuilder.length() > 0) {
		return toolTipBuilder.insert(0, "<html>").append("</html>").toString();
	    } else {
		return null;
	    }
	}
    }
}
