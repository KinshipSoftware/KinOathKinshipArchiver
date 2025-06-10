/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.ui;

import nl.mpi.arbil.data.ArbilDataNode;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import nl.mpi.arbil.util.BugCatcherManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document   : ImageBoxRenderer
 * Created on : Wed Dec 03 15:44:20
 * @author Peter.Withers@mpi.nl
 */
public class ImageBoxRenderer extends JLabel implements ListCellRenderer {
    private final static Logger logger = LoggerFactory.getLogger(ImageBoxRenderer.class);

    int outputWidth = 200;
    int outputHeight = 130;
    int textStartX = 0;
    int textStartY = 0;
    boolean ffmpegFound = true;
    boolean imageMagickFound = true;
    String ffmpegPath = null;
    String imageMagickPath = null;
    private List<String> searchPath = Arrays.asList("utils\\ImageMagick\\", "/opt/local/bin/", "/usr/local/bin/", "/sw/bin/convert", "");
    //    boolean loadedMfcDlls = false;
    // the thumbnail files are stored in a temp file on disk and the file location kept in the imditreeobject

    public ImageBoxRenderer() {
//        setBorder(new DashedBorder(Color.DARK_GRAY));
	setOpaque(true);
	setHorizontalAlignment(CENTER);
	setVerticalAlignment(CENTER);
	setVerticalTextPosition(JLabel.BOTTOM);
	setHorizontalTextPosition(JLabel.CENTER);
	setPreferredSize(new Dimension(outputWidth + 10, outputHeight + 50));

	final Map<String, String> env = System.getenv();
	if (env.containsKey("PATH")) {
	    final String path = env.get("PATH");
	    final String osNameString = System.getProperty("os.name").toLowerCase();
	    String[] pathElements;
	    if (osNameString.contains("windows")) {
		// Win has semicolon 
		pathElements = path.split(";");
	    } else {
		// Assume unix-style (Mac or Linux)
		pathElements = path.split(":");
	    }
	    searchPath = new ArrayList<String>(searchPath);
	    searchPath.addAll(Arrays.asList(pathElements));
	}


    }

    // returns a boolean value indicating if the node has or can have a thumbnail
    // if it can but does not yet then a thumbnail will be made
    public boolean canDisplay(ArbilDataNode testableObject) {
	if (testableObject.thumbnailFile != null) {
	    return true;
	}
	if (!testableObject.hasLocalResource()) {
	    return false;
	}
	if (testableObject.mpiMimeType == null) {
	    return false;
	}
	if (testableObject.mpiMimeType.toLowerCase().contains("text")) {
	    createThumbnail(testableObject);
	}
	if (testableObject.mpiMimeType.toLowerCase().contains("image")) {
	    createImageThumbnail(testableObject);
	    if (testableObject.thumbnailFile == null) {
		if (testableObject.mpiMimeType.toLowerCase().contains("jp") || testableObject.mpiMimeType.toLowerCase().contains("gif")) {
		    //  if we get here then resourt to creating the thumbnail in java
		    createThumbnail(testableObject);
		}
	    }
	}
	if (testableObject.mpiMimeType.toLowerCase().contains("video")) {
	    createVideoThumbnail(testableObject);
	}
	return testableObject.thumbnailFile != null;
    }

    /*
     * This method finds the image and text corresponding
     * to the selected value and returns the label, set up
     * to display the text and image.
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	//Get the selected index. (The index param isn't
	//always valid, so just use the value.)
//            int selectedIndex = ((Integer) value).intValue();

	if (isSelected) {
	    setBackground(list.getSelectionBackground());
	    setForeground(list.getSelectionForeground());
	} else {
	    setBackground(list.getBackground());
	    setForeground(list.getForeground());
	}
	//Set the icon and text.
	if (value instanceof ArbilDataNode) {
	    ArbilDataNode arbilObject = (ArbilDataNode) value;
	    setFont(list.getFont());
	    setText(arbilObject.toString());
	    if (arbilObject.thumbnailFile != null) {
		try {
		    setIcon(new ImageIcon(arbilObject.thumbnailFile.toURL()));
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	} else {
	    setText(value.toString() + " (no image available)");
	}
	return this;
    }

    private void drawFileText(Graphics2D targegGraphics, URL targetURL) {
	int linePosY = textStartY;
	targegGraphics.setBackground(Color.white);
	targegGraphics.clearRect(0, 0, outputWidth, outputHeight);
	targegGraphics.setColor(Color.BLACK);
	targegGraphics.drawRect(0, 0, outputWidth - 1, outputHeight - 1);
	targegGraphics.setColor(Color.DARK_GRAY);
	Font currentFont = targegGraphics.getFont();
	Font renderFont = new Font(currentFont.getFontName(), currentFont.getStyle(), 11);
	targegGraphics.setFont(renderFont);
	BufferedReader bufferedReader = null;
	try {
	    bufferedReader = new BufferedReader(new InputStreamReader(targetURL.openStream()));
	    String textToDraw;
	    while ((textToDraw = bufferedReader.readLine()) != null && outputHeight > linePosY) {
		textToDraw = textToDraw.replaceAll("\\<.*?>", "");
		textToDraw = textToDraw.replaceAll("^\\\\[^ ]*", "");
		textToDraw = textToDraw.replaceAll("\\s+", " ");
		textToDraw = textToDraw.trim();
		if (textToDraw.length() > 0) {
		    double lineHeight = targegGraphics.getFont().getStringBounds(textToDraw, targegGraphics.getFontRenderContext()).getHeight();
		    linePosY = linePosY + (int) lineHeight;
		    targegGraphics.drawString(textToDraw, textStartX + 2, linePosY);
		}
	    }
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	} finally {
	    if (bufferedReader != null) {
		try {
		    bufferedReader.close();
		} catch (IOException ioe) {
		    BugCatcherManager.getBugCatcher().logError(ioe);
		}
	    }
	}
    }

    private File getTargetFile(ArbilDataNode targetDataNode) {
	if (targetDataNode.hasResource()) {
	    return new File(targetDataNode.getFullResourceURI());
	} else if (targetDataNode.isArchivableFile()) {
	    return targetDataNode.getFile();
	} else {
	    return null;
	}
    }

    private void createVideoThumbnail(ArbilDataNode targetDataNode) {
	if (ffmpegPath == null) {
	    // todo: replaces this with a parameter or a properties file
	    for (String currentPath : searchPath) {
		for (String currentSuffix : new String[]{".exe", ""}) {
		    File ffmpegFile = new File(currentPath, "ffmpeg" + currentSuffix);
		    ffmpegPath = ffmpegFile.toString();
		    if (ffmpegFile.exists()) {
			break;
		    }
		}
		if (new File(ffmpegPath).exists()) {
		    break;
		}
	    }
	}
	if (ffmpegFound) {
	    BufferedReader errorStreamReader = null;
	    try {
		File iconFile = File.createTempFile("arbil", ".jpg");
		iconFile.deleteOnExit();
		File targetFile = getTargetFile(targetDataNode);
		String[] execString = new String[]{ffmpegPath, "-itsoffset", "-4", "-i", targetFile.getCanonicalPath(), "-vframes", "1", "-s", outputWidth + "x" + outputHeight, iconFile.getAbsolutePath()};
//                logger.debug(execString);
		Process launchedProcess = Runtime.getRuntime().exec(execString);
		errorStreamReader = new BufferedReader(new InputStreamReader(launchedProcess.getErrorStream()));
		String line;
		while ((line = errorStreamReader.readLine()) != null) { // read until EOF
//                    ffmpegFound = false;
		    logger.debug("Launched process error stream: \"" + line + "\"");
		}
		// NOTE: We should also wait for launchedProcess to exit
		iconFile.deleteOnExit();
		if (iconFile.exists()) {
		    targetDataNode.thumbnailFile = iconFile;
		}
//        /data1/apps/ffmpeg-deb/usr/bin/ffmpeg
//            ffmpeg  -itsoffset -4  -i test.avi -vcodec mjpeg -vframes 1 -an -f rawvideo -s 320x240 test.jpg
	    } catch (IOException ex) {
		ffmpegFound = false; //todo this is not getting hit when ffmpeg is not available
		BugCatcherManager.getBugCatcher().logError(ex);
	    } finally {
		if (errorStreamReader != null) {
		    try { // close pipeline
			errorStreamReader.close();
		    } catch (IOException ioe) {
			BugCatcherManager.getBugCatcher().logError(ioe);
		    }
		}
	    }
	}
    }

    private void createImageThumbnail(ArbilDataNode targetDataNode) {
//        if (!loadedMfcDlls) {
//            loadedMfcDlls = true;
//            try {
//                // todo: this need not be done in a non windows environment or when imagemagick is installed
//                System.loadLibrary("CVCOMP90");
//            } catch (Exception ex) {
//                BugCatcherManager.getBugCatcher().logError(ex);
//            }
//        }
	if (imageMagickPath == null) {
	    // todo: replaces this process with a parameter or a properties file so that the jnlp version can benifit from the installed version
	    for (String currentPath : searchPath) {
		for (String currentSuffix : new String[]{".exe", ""}) {
		    File imageMagickFile = new File(currentPath, "convert" + currentSuffix);
		    imageMagickPath = imageMagickFile.toString();
		    if (imageMagickFile.exists()) {
			break;
		    }
		}
		if (new File(imageMagickPath).exists()) {
		    break;
		}
	    }
	}
	if (imageMagickFound) {
	    try {
		File iconFile = File.createTempFile("arbil", ".jpg");
		iconFile.deleteOnExit();
		File targetFile = getTargetFile(targetDataNode);
		if (targetFile.exists()) {
		    String[] execString = new String[]{imageMagickPath, "-define", "jpeg:size=" + outputWidth * 2 + "x" + outputHeight * 2, targetFile.getCanonicalPath(), "-auto-orient", "-thumbnail", outputWidth + "x" + outputHeight, "-unsharp", "0x.5", iconFile.getAbsolutePath()};
		    logger.debug(Arrays.toString(execString));
		    Process launchedProcess = Runtime.getRuntime().exec(execString);
		    BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(launchedProcess.getErrorStream()));
		    String line;
		    while ((line = errorStreamReader.readLine()) != null) {
//                    ffmpegFound = false;
			logger.debug("Launched process error stream: \"" + line + "\"");
		    }
		    iconFile.deleteOnExit();
		    if (iconFile.exists()) {
			targetDataNode.thumbnailFile = iconFile;
		    }
		}
//        /data1/apps/ffmpeg-deb/usr/bin/ffmpeg
//            ffmpeg  -itsoffset -4  -i test.avi -vcodec mjpeg -vframes 1 -an -f rawvideo -s 320x240 test.jpg
	    } catch (Exception ex) {
		imageMagickFound = false; //todo this is not getting hit when x is not available
		BugCatcherManager.getBugCatcher().logError(ex);
	    }
	}
    }

    private void createThumbnail(ArbilDataNode targetDataNode) {
	try {
	    BufferedImage resizedImg = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_RGB);
	    Graphics2D g2 = resizedImg.createGraphics();
	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    if (((ArbilDataNode) targetDataNode).mpiMimeType.contains("image")) {
		ImageIcon nodeImage = new ImageIcon(getTargetFile(targetDataNode).toURL());
		if (nodeImage != null) {
		    g2.drawImage(nodeImage.getImage(), 0, 0, outputWidth, outputHeight, null);
		}
	    } else if (targetDataNode.mpiMimeType.contains("text")) {
		drawFileText(g2, getTargetFile(targetDataNode).toURL());
	    }
	    g2.dispose();
	    File iconFile = File.createTempFile("arbil", ".jpg");
	    iconFile.deleteOnExit();
	    ImageIO.write(resizedImg, "JPEG", iconFile);
	    if (iconFile.exists()) {
		targetDataNode.thumbnailFile = iconFile;
	    }
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
    }
}
