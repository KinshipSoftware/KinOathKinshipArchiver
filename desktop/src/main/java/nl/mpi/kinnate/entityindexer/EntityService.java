package nl.mpi.kinnate.entityindexer;

import java.net.URI;
import java.util.ArrayList;
import javax.swing.JProgressBar;
import nl.mpi.kinnate.kindata.EntityData;
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
    public EntityData[] processKinTypeStrings(URI[] egoNodes, ArrayList<KinTypeStringProvider> kinTypeStringProviders, IndexerParameters indexParameters, DataStoreSvg dataStoreSvg, JProgressBar progressBar) throws EntityServiceException, ProcessAbortException;
}
