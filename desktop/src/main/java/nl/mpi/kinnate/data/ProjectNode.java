package nl.mpi.kinnate.data;

import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ContainerNode;
import nl.mpi.kinnate.entityindexer.EntityCollection;

/**
 * Document : ProjectNode
 * Created on : Apr 25, 2012, 11:33:19 AM
 * Author : Peter Withers
 */
public class ProjectNode extends ContainerNode {

    EntityCollection entityCollection;

    public ProjectNode(EntityCollection entityCollection, String labelString) {
        super(labelString, null, new ContainerNode[]{new ContainerNode("loading...", null, new ArbilNode[0])});
        this.entityCollection = entityCollection;
    }
}
