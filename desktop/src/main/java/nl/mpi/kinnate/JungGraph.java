package nl.mpi.kinnate;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
//import edu.uci.ics.jung.graph.Vertex;
//import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.swing.JPanel;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;

/**
 *  Document   : JungGraph
 *  Created on : Aug 25, 2010, 5:20:56 PM
 *  Author     : Peter Withers
 */
public class JungGraph extends JPanel {

    Graph<Integer, String> g;

    public JungGraph() {
        drawNodes();
        Layout<Integer, String> layout = new CircleLayout(g);

//        StringLabeller stringLabeller = new StringLabeller();
        layout.setSize(new Dimension(300, 300)); // sets the initial size of the layout space
        // The BasicVisualizationServer<V,E> is parameterized by the vertex and edge types
        BasicVisualizationServer<Integer, String> vv = new BasicVisualizationServer<Integer, String>(layout);
        vv.setPreferredSize(new Dimension(350, 350)); //Sets the viewing area size
        this.add(vv);
    }

    public void drawNodes() {
        g = new SparseMultigraph<Integer, String>();
        // Add some vertices. From above we defined these to be type Integer.
        g.addVertex((Integer) 1);
        g.addVertex((Integer) 2);
        g.addVertex((Integer) 3);
        // Note that the default is for undirected edges, our Edges are Strings.
        g.addEdge("Edge-A", 1, 2); // Note that Java 1.5 auto-boxes primitives
        g.addEdge("Edge-B", 2, 3);


        String[] treeNodesArray = ArbilSessionStorage.getSingleInstance().loadStringArray("KinGraphTree");
        ArrayList<ArbilDataNode> tempArray = new ArrayList<ArbilDataNode>();
        if (treeNodesArray != null) {
            for (String currentNodeString : treeNodesArray) {
                try {
                    tempArray.add(ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, new URI(currentNodeString)));
                } catch (URISyntaxException exception) {
                    System.err.println(exception.getMessage());
                    exception.printStackTrace();
                }
            }
        }
        for (ArbilDataNode currentChild : tempArray) {
            currentChild.waitTillLoaded();
            if (!currentChild.isEmptyMetaNode()) {
                Integer currentIndex = g.getVertexCount() + 1;
                g.addVertex(currentIndex);
//                try {
//                    StringLabeller sl = StringLabeller.getLabeller(g);
//                    sl.setLabel((Vertex) g.getVertices().toArray()[currentIndex], currentChild.toString());
//                } catch (StringLabeller.UniqueLabelException exception) {
//                    System.err.println(exception.getMessage());
//                }
            }
        }


    }
}
