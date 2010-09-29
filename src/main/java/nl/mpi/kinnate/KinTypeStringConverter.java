package nl.mpi.kinnate;

/**
 *  Document   : KinTypeStringConverter
 *  Created on : Sep 29, 2010, 12:52:33 PM
 *  Author     : Peter Withers
 */
public class KinTypeStringConverter extends GraphData {

    public void readKinTypes(String[] kinTypes) {
        for (String kinTypeString : kinTypes) {
            System.out.println("kinTypeString: " + kinTypeString);
        }
        super.readData();
    }
}
