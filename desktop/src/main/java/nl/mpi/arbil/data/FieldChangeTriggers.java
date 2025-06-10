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
package nl.mpi.arbil.data;

import nl.mpi.arbil.util.MessageDialogHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FieldChangeTriggers.java
 * Created on Jul 13, 2009, 4:15:02 PM
 *
 * @author Peter.Withers@mpi.nl
 */
public class FieldChangeTriggers {
    private final static Logger logger = LoggerFactory.getLogger(FieldChangeTriggers.class);

    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }

    // the following strings need to be read from a template file or a vocaulary etc
    public void actOnChange(ArbilField changedArbilField) {
	String fieldPath = changedArbilField.getGenericFullXmlPath();
	logger.debug("fieldPath: {}", fieldPath);
	for (String[] currentTrigger : changedArbilField.getParentDataNode().getNodeTemplate().getFieldTriggersArray()) {
	    if (fieldPath.equals(currentTrigger[0])) {
		// we now have the path for two fields:
		// .METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Languages.Language(x).Id
		// .METATRANSCRIPT.Session.MDGroup.Actors.Actor(2).Languages.Language(3).Name
		// and we need to put the (\d) back into the (x)
		String originalFieldPath = changedArbilField.getFullXmlPath();
		int lastBracketPos = originalFieldPath.lastIndexOf(")");
		int lastTriggerBracket = currentTrigger[1].lastIndexOf(")");
		if (lastTriggerBracket < 0) {
		    messageDialogHandler.addMessageDialogToQueue("Error in trigger from template (missing bracket): " + currentTrigger[1], "Field Trigger");
		    break;
		}
		// care must me taken here to prevet issues with child nodes greater than 9 ie (12), (x) etc.
		String targetFieldPath = originalFieldPath.substring(0, lastBracketPos) + currentTrigger[1].substring(lastTriggerBracket);
		logger.debug("originalFieldPath: {}", originalFieldPath);
		logger.debug("targetFieldPath: {}", targetFieldPath);
		final ArbilField[] targetField = changedArbilField.getSiblingField(targetFieldPath);
		if (targetField != null && targetField.length > 0) {
		    ArbilVocabularyItem vocabItem = changedArbilField.getVocabulary().findVocabularyItem(changedArbilField.getFieldValue());
		    if (vocabItem != null) {
			String valueForTargetField = null;
			if (currentTrigger[2].equals("Content")) {
			    valueForTargetField = vocabItem.descriptionString;
			} else if (currentTrigger[2].equals("Value")) {
			    valueForTargetField = vocabItem.itemDisplayName;
			} else if (currentTrigger[2].equals("Code")) {
			    valueForTargetField = vocabItem.itemCode;
			} else if (currentTrigger[2].equals("FollowUp")) {
			    targetField[0].loadVocabulary();
			}
			if (valueForTargetField != null) {
			    targetField[0].setFieldValue(valueForTargetField, true, false);
			}
		    }
		}
	    }
	}
    }
}
