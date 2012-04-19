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
    private int dpi = 300;
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
        return new Dimension((int) (diagramSize.getWidth() / 25.4 * dpi), (int) (diagramSize.getHeight() / 25.4 * dpi));
    }

    public int getDpi() {
        return dpi;
    }

    public void setDpi(int dpi) {
        this.dpi = dpi;
    }

    public OutputType getOutputType() {
        return outputType;
    }

    public void setOutputType(OutputType outputType) {
        this.outputType = outputType;
    }

    public void exportDiagram(File outputFile) {
        this.outputFile = outputFile;
        switch (outputType) {
            case JPEG:
                fixSuffix(".jpg");
                saveAsJpg();
                break;
            case PDF:
                fixSuffix(".pdf");
                saveAsPdf();
                break;
            case PNG:
                fixSuffix(".png");
                saveAsPng();
                break;
        }
    }

    private void fixSuffix(String requiredSuffix) {
        if (!outputFile.getName().toLowerCase().endsWith(requiredSuffix)) {
            String fileName = outputFile.getName();
            fileName = fileName.replaceFirst("\\....$", "");
            outputFile = new File(outputFile.getParentFile(), fileName + requiredSuffix);
        }
    }

    private void saveAsJpg() {
        JPEGTranscoder transcoder = new JPEGTranscoder();
        transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(.8));
        transcoder.addTranscodingHint(ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, new Float((float) (25.4 / dpi)));
        transcoder.addTranscodingHint(XMLAbstractTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE);
        transcoder.addTranscodingHint(PDFTranscoder.KEY_STROKE_TEXT, Boolean.FALSE);
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

    private void saveAsPdf() {
        Transcoder transcoder = new PDFTranscoder();
        transcoder.addTranscodingHint(ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, new Float((float) (25.4 / dpi)));
        transcoder.addTranscodingHint(XMLAbstractTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE);
        transcoder.addTranscodingHint(PDFTranscoder.KEY_STROKE_TEXT, Boolean.FALSE);
        try {
            TranscoderInput transcoderInput = new TranscoderInput(savePanel.getGraphPanel().doc);
            OutputStream outputStream = new java.io.FileOutputStream(outputFile);
            outputStream = new java.io.BufferedOutputStream(outputStream);
            try {
                TranscoderOutput transcoderOutput = new TranscoderOutput(outputStream);
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

    private void saveAsPng() {
//        savePanel.getGraphPanel().doc;
        // strip out the entity data
        // save the dom in pretty format
    }
}
