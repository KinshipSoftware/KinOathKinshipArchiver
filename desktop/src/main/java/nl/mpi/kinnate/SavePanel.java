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
package nl.mpi.kinnate;

import java.io.File;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.svg.MouseListenerSvg;

/**
 * Created on : Feb 17, 2011, 1:37:48 PM
 *
 * @author Peter Withers
 */
public interface SavePanel {

    public boolean requiresSave();

    public void setRequiresSave();

    public boolean hasSaveFileName();

    public File getFileName();

    public void saveToFile();

    public void saveToFile(File saveFile);

    public void updateGraph();

    public void doActionCommand(MouseListenerSvg.ActionCode actionCode);

    public GraphPanel getGraphPanel();

    public void showSettings();
}
