package nl.mpi.kinnate;

/**
 *  Document   : KinTypeStringConverter
 *  Created on : Sep 29, 2010, 12:52:33 PM
 *  Author     : Peter Withers
 */
public class KinTypeStringConverter extends GraphData {

    public void readKinTypes(String[] kinTypes) {
        for (String kinTypeString : kinTypes) {
            if (kinTypeString != null && kinTypeString.length() > 0) {
                System.out.println("kinTypeString: " + kinTypeString);
                if (graphDataNodeList.containsKey(kinTypeString)) {
                    graphDataNodeList.get(kinTypeString); // add any child nodes
                } else {
                    graphDataNodeList.put(kinTypeString, new GraphDataNode(kinTypeString));
                    // add any child nodes
                }
            }
        }
        calculateLinks();
        calculateLocations();
//        printLocations();
    }
}
