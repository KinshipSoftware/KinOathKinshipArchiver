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
package nl.mpi.arbil.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Document : BinaryMetadataReader
 * Created on : Jul 5, 2010, 3:25:32 PM (content moved to here from a different file "ImdiSchema")
 * Author : Peter Withers
 */
public class BinaryMetadataReader {

    private final static Logger logger = LoggerFactory.getLogger(BinaryMetadataReader.class);

    // functions to extract the exif data from images
// this will probably need to be moved to a more appropriate class
    public ArbilField[] getExifMetadata(ArbilDataNode resourceNode, int currentFieldId) {
	List<ArbilField> exifTagFields = new ArrayList<ArbilField>();
	try {
	    URI uri = resourceNode.getURI();
	    if (resourceNode.getFile().getName().contains(".")) {
		String fileSuffix = resourceNode.getFile().getName().substring(resourceNode.getFile().getName().lastIndexOf(".") + 1);
		Iterator readers = ImageIO.getImageReadersBySuffix(fileSuffix);
		if (readers.hasNext()) {
		    ImageReader reader = (ImageReader) readers.next();
		    logger.debug("Reading Exif metadata for {} using {}", resourceNode.getFile(), reader.getClass());
		    reader.setInput(ImageIO.createImageInputStream(uri.toURL().openStream()));
		    IIOMetadata metadata = reader.getImageMetadata(0);
		    if (metadata != null) {
			String[] names = metadata.getMetadataFormatNames();
			for (int i = 0; i < names.length; ++i) {
			    logger.debug("Metadata for format: {}", names[i]);
			    decendExifTree(resourceNode, metadata.getAsTree(names[i]), new StringBuilder()/*"." + names[i]*/, exifTagFields, currentFieldId);
			}
		    }
		}
	    }
	} catch (Exception ex) {
	    logger.warn("Exception while reading Exif metadata", ex);
	}
	return exifTagFields.toArray(new ArbilField[]{});
    }

    private void decendExifTree(ArbilDataNode resourceNode, Node node, StringBuilder prefixString, List<ArbilField> exifTagFields, int currentFieldId) {
	if (prefixString.length() == 0) {
	    prefixString.append("EXIF"); // skip the first node name
	} else {
	    prefixString.append(MetadataReader.imdiPathSeparator).append(node.getNodeName());
	}
	NamedNodeMap namedNodeMap = node.getAttributes();
	if (namedNodeMap != null) {
	    for (int attributeCounter = 0; attributeCounter < namedNodeMap.getLength(); attributeCounter++) {
		String attributeName = namedNodeMap.item(attributeCounter).getNodeName();
		String attributeValue = namedNodeMap.item(attributeCounter).getNodeValue();
		final CharSequence attributePath = new StringBuilder(prefixString).append(MetadataReader.imdiPathSeparator).append(attributeName);
		logger.debug("Exif metadata: {}=>{} = {}", resourceNode, attributePath, attributeValue);
		exifTagFields.add(new ArbilField(currentFieldId++, resourceNode, attributePath.toString(), attributeValue, 0, false));
	    }
	}
	if (node.hasChildNodes()) {
	    NodeList children = node.getChildNodes();
	    for (int i = 0, ub = children.getLength(); i < ub; ++i) {
		decendExifTree(resourceNode, children.item(i), prefixString, exifTagFields, currentFieldId);
	    }
	}
    }
}
