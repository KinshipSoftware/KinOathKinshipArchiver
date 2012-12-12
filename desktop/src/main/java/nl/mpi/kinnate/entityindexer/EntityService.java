/**
 * Copyright (C) 2012 The Language Archive
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
package nl.mpi.kinnate.entityindexer;

import java.util.ArrayList;
import javax.swing.JProgressBar;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kintypestrings.ImportRequiredException;
import nl.mpi.kinnate.svg.DataStoreSvg;
import nl.mpi.kinnate.ui.KinTypeStringProvider;

/**
 * Document : EntityService
 * Created on : Mar 22, 2011, 1:09:33 PM
 * Author : Peter Withers
 */
public interface EntityService {

    public void clearAbortRequest();

    public void requestAbortProcess();

//    public void primeWithEntities(EntityData[] preLoadedEntities); // it would seem not to be a good idea to try and use existing entities from an svg file when their relations might not exist, so we will allow the existing entities to be used on the graph but not for database actions
    public EntityData[] processKinTypeStrings(ArrayList<KinTypeStringProvider> kinTypeStringProviders, IndexerParameters indexParameters, DataStoreSvg dataStoreSvg, JProgressBar progressBar) throws EntityServiceException, ProcessAbortException, ImportRequiredException;
}
