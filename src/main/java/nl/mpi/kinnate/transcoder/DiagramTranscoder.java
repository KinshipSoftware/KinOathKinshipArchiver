package nl.mpi.kinnate.transcoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import nl.mpi.kinnate.SavePanel;
//import org.apache.batik.transcoder.image.JPEGTranscoder;
//import org.apache.batik.transcoder.TranscoderInput;
//import org.apache.batik.transcoder.TranscoderOutput;

/**
 *  Document   : DiagramTranscoder
 *  Created on : May 10, 2011, 9:03:47 PM
 *  Author     : Peter Withers
 */
public class DiagramTranscoder {

    public void saveAsJpg(SavePanel savePanel) {
        if (savePanel.hasSaveFileName()) {
            // todo: tell user to save as
        }
        if (savePanel.requiresSave()) {
            // todo: tell user to save
        }
        File diagramSvg = savePanel.getFileName();
        File diagramPdf = new File(diagramSvg.getParentFile(), diagramSvg.getName().replaceFirst("\\.[Ss][Vv][Gg]$", ".pdf"));
        File diagramJpg = new File(diagramSvg.getParentFile(), diagramSvg.getName().replaceFirst("\\.[Ss][Vv][Gg]$", ".jpg"));
//        JPEGTranscoder transcoder = new JPEGTranscoder();
//        transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(.8));
//        TranscoderInput transcoderInput = new TranscoderInput(diagramSvg.toString());
//        OutputStream outputStream = new FileOutputStream(diagramJpg);
//        TranscoderOutput transcoderOutput = new TranscoderOutput(outputStream);
//        transcoder.transcode(transcoderInput, transcoderOutput);
//        outputStream.flush();
//        outputStream.close();
    }

    public void saveAsPdf(SavePanel savePanel) {
    }
}
