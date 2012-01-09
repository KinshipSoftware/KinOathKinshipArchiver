package nl.mpi.kinnate.transcoder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import nl.mpi.arbil.util.BugCatcher;
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
 *  Document   : DiagramTranscoder
 *  Created on : May 10, 2011, 9:03:47 PM
 *  Author     : Peter Withers
 */
public class DiagramTranscoder {

    public void saveAsJpg(SavePanel savePanel, BugCatcher bugCatcher) {
        // todo: offer user to select an export file name
        if (savePanel.hasSaveFileName()) {
            // todo: tell user to save as
        }
        if (savePanel.requiresSave()) {
            // todo: tell user to save
        }

        File diagramSvg = savePanel.getFileName();
        File diagramJpg = new File(diagramSvg.getParentFile(), diagramSvg.getName().replaceFirst("\\.[Ss][Vv][Gg]$", ".jpg"));
        JPEGTranscoder transcoder = new JPEGTranscoder();

        transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(.8));
        final int dpi = 300;
        transcoder.addTranscodingHint(ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, new Float((float) (25.4 / dpi)));
        transcoder.addTranscodingHint(XMLAbstractTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE);
        transcoder.addTranscodingHint(PDFTranscoder.KEY_STROKE_TEXT, Boolean.FALSE);
        InputStream inputStream = null;
        try {
            inputStream = new java.io.FileInputStream(diagramSvg);
            TranscoderInput transcoderInput = new TranscoderInput(inputStream);
            transcoderInput.setURI(diagramSvg.toURI().toASCIIString());
            OutputStream outputStream = new java.io.FileOutputStream(diagramJpg);
            outputStream = new java.io.BufferedOutputStream(outputStream);
            try {
                TranscoderOutput transcoderOutput = new TranscoderOutput(outputStream);
                // todo: resolve the issue here when transcoding to jpg
                transcoder.transcode(transcoderInput, transcoderOutput);
            } finally {
                outputStream.close();
            }
        } catch (TranscoderException exception) {
            bugCatcher.logError(exception);
        } catch (IOException exception) {
            bugCatcher.logError(exception);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException exception) {
                    bugCatcher.logError(exception);
                }
            }
        }
    }

    public void saveAsPdf(SavePanel savePanel, BugCatcher bugCatcher) {
        // todo: offer user to select an export file name
        if (savePanel.hasSaveFileName()) {
            // todo: tell user to save as
        }
        if (savePanel.requiresSave()) {
            // todo: tell user to save
        }
        File diagramSvg = savePanel.getFileName();
        File diagramPdf = new File(diagramSvg.getParentFile(), diagramSvg.getName().replaceFirst("\\.[Ss][Vv][Gg]$", ".pdf"));
        Transcoder transcoder = new PDFTranscoder();

        //Configure the transcoder
//        try {
//            DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
//            Configuration cfg = cfgBuilder.buildFromFile(new File("pdf-renderer-cfg.xml"));
//            ContainerUtil.configure(transcoder, cfg);
//        } catch (Exception e) {
//            throw new TranscoderException(e);
//        }

        final int dpi = 300;
        transcoder.addTranscodingHint(ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, new Float((float) (25.4 / dpi)));
        transcoder.addTranscodingHint(XMLAbstractTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE);
        transcoder.addTranscodingHint(PDFTranscoder.KEY_STROKE_TEXT, Boolean.FALSE);
        InputStream inputStream = null;
        try {
            inputStream = new java.io.FileInputStream(diagramSvg);
            TranscoderInput transcoderInput = new TranscoderInput(inputStream);
            transcoderInput.setURI(diagramSvg.toURI().toASCIIString());
            OutputStream outputStream = new java.io.FileOutputStream(diagramPdf);
            outputStream = new java.io.BufferedOutputStream(outputStream);
            try {
                TranscoderOutput transcoderOutput = new TranscoderOutput(outputStream);
                transcoder.transcode(transcoderInput, transcoderOutput);
            } finally {
                outputStream.close();
            }
        } catch (TranscoderException exception) {
            bugCatcher.logError(exception);
        } catch (IOException exception) {
            bugCatcher.logError(exception);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException exception) {
                    bugCatcher.logError(exception);
                }
            }
        }
    }
}
