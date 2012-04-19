package nl.mpi.kinnate.svg;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.SavePanel;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.XMLAbstractTranscoder;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.fop.svg.PDFTranscoder;

/**
 * Document : DiagramTranscoder
 * Created on : May 10, 2011, 9:03:47 PM
 * Author : Peter Withers
 */
public class DiagramTranscoder {

    public enum OutputType {

        JPEG, PDF, PNG
    };
//    private int dpi = 300;
    private OutputType outputType = OutputType.PDF;
    private File outputFile;
    private SavePanel savePanel;
    private Dimension2D diagramSize;

    public DiagramTranscoder(SavePanel savePanel) {
        this.savePanel = savePanel;
        diagramSize = savePanel.getGraphPanel().getDiagramSize();
//        if (!outputFile.getName().toLowerCase().endsWith(".jpg")) {
//            outputType = OutputType.JPEG;
//        }
//        if (!outputFile.getName().toLowerCase().endsWith(".pdf")) {
//            outputType = OutputType.PDF;
//        }
    }

    public Dimension getCurrentSize() {
        if (diagramSize != null) {
//            return new Dimension((int) (diagramSize.getWidth() / 25.4 * dpi), (int) (diagramSize.getHeight() / 25.4 * dpi));
            return new Dimension((int) (diagramSize.getWidth()), (int) (diagramSize.getHeight()));
        } else {
            return null;
        }
    }

//    public int getDpi() {
//        return dpi;
//    }

//    public void setDpi(int dpi) {
//        this.dpi = dpi;
//    }

    public OutputType getOutputType() {
        return outputType;
    }

    public void setOutputType(OutputType outputType) {
        this.outputType = outputType;
    }

    public void exportDiagram(File outputFile) {
        this.outputFile = outputFile;
        Transcoder transcoder;
        switch (outputType) {
            case JPEG:
                fixSuffix(".jpg");
                transcoder = new JPEGTranscoder();
                transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(.8));
                break;
            case PNG:
                fixSuffix(".png");
                transcoder = new PNGTranscoder();
                break;
            default:
                fixSuffix(".pdf");
                transcoder = new PDFTranscoder();
                transcoder.addTranscodingHint(PDFTranscoder.KEY_STROKE_TEXT, Boolean.FALSE);
                break;
        }
        transcoder.addTranscodingHint(XMLAbstractTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE);
//        transcoder.addTranscodingHint(ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, new Float((float) (25.4 / dpi)));
        transcodeDom(transcoder);
    }

    private void fixSuffix(String requiredSuffix) {
        if (!outputFile.getName().toLowerCase().endsWith(requiredSuffix)) {
            String fileName = outputFile.getName();
            fileName = fileName.replaceFirst("\\....$", "");
            outputFile = new File(outputFile.getParentFile(), fileName + requiredSuffix);
        }
    }

    private void transcodeDom(Transcoder transcoder) {
        try {
            TranscoderInput transcoderInput = new TranscoderInput(savePanel.getGraphPanel().doc);
            OutputStream outputStream = new java.io.FileOutputStream(outputFile);
            outputStream = new java.io.BufferedOutputStream(outputStream);
            try {
                TranscoderOutput transcoderOutput = new TranscoderOutput(outputStream);
                // todo: resolve the issue here when transcoding to jpg
                transcoder.transcode(transcoderInput, transcoderOutput);
            } finally {
                outputStream.close();
            }
        } catch (TranscoderException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        } catch (IOException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        }
    }
}
