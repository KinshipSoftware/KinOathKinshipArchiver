package nl.mpi.kinnate.svg;

/**
 *  Document   : GraphPanelSize
 *  Created on : Feb 18, 2011, 10:23:43 AM
 *  Author     : Peter Withers
 */
public class GraphPanelSize {

    public boolean autoSize = false;
    private int preferedWidth = 800;
    private int preferedHeight = 600;
    private int defaultVerticalSpacing = 100;
    private int defaultHorizontalSpacing = 200;
    String[][] preferredSizeArray = new String[][]{
        {"352 x 288", "352", "288"},
        {"640 x 480", "640", "480"},
        {"800 x 600", "800", "600"},
        {"1024 x 768", "1024", "768"}//,
//        {"A4 210 x 297", "210", "297"},
//        {"B4 250 x 353", "250", "353"},
//        {"C4 229 x 324", "229", "324"},
//        {"A5 148 x 210", "148", "210"},
//        {"B5 176 x 250", "176", "250"},
//        {"C5 162 x 229", "162", "229"},
//        {"A6 105 x 148", "105", "148"},
//        {"B6 125 x 176", "125", "176"},
//        {"C6 114 x 162", "114", "162"},
//        {"A7 74 x 105", "74", "105"},
//        {"B7 88 x 125", "88", "125"},
//        {"C7 81 x 114", "81", "114"},
//        {"A8 52 x 74", "52", "74"},
//        {"B8 62 x 88", "62", "88"},
//        {"C8 57 x 81", "57", "81"},
//        {"A9 37 x 52", "37", "52"},
//        {"B9 44 x 62", "44", "62"},
//        {"C9 40 x 57", "40", "57"},
//        {"A10 26 x 37", "26", "37"},
//        {"B10 31 x 44", "31", "44"},
//        {"C10 28 x 40", "28", "40"},
//        {"Custom", null, null},
//        {"Auto", null, null}
    };

    public String[] getPreferredSizes() {
        String[] returnArray = new String[preferredSizeArray.length + 1];
        for (int currentCount = 0; currentCount < preferredSizeArray.length; currentCount++) {
            returnArray[currentCount] = preferredSizeArray[currentCount][0];
        }
        returnArray[preferredSizeArray.length] = "Auto";
        return returnArray;
    }

    public void setSize(String selectedSize) {
        if ("Auto".equals(selectedSize)) {
            autoSize = true;
        } else {
            for (String[] currentEntry : preferredSizeArray) {
                if (selectedSize.equals(currentEntry[0])) {
                    preferedWidth = Integer.valueOf(currentEntry[1]);
                    preferedHeight = Integer.valueOf(currentEntry[2]);
                }
            }
//            if (selectedSize.startsWith("A") || selectedSize.startsWith("B") || selectedSize.startsWith("C")) {
//                // todo: this is suboptimal but will do for now
//                preferedWidth = preferedWidth * 10;
//                preferedHeight = preferedHeight * 10;
//            }
        }
    }

    public int getWidth(int gridWidth, int hSpacing) {
        if (autoSize) {
            return gridWidth * hSpacing + hSpacing * 2;
        } else {
            return preferedWidth;
        }
    }

    public int getHeight(int gridHeight, int vSpacing) {
        if (autoSize) {
            return gridHeight * vSpacing + vSpacing * 2;
        } else {
            return preferedHeight;
        }
    }

    public int getVerticalSpacing(int gridHeight) {
//        if (autoSize) {
            return defaultVerticalSpacing;
//        } else {
//            return preferedHeight / (gridHeight + 1);
//        }
    }

    public int getHorizontalSpacing(int gridWidth) {
//        if (autoSize) {
            return defaultHorizontalSpacing;
//        } else {
//            return preferedWidth / (gridWidth + 1);
//        }
    }
}
