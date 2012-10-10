package nl.mpi.arbil.plugin;

import java.io.File;
import java.util.Map;
import javax.swing.JComponent;

/**
 * Document : PluginDialogHandler <br> Created on Aug 15, 2012, 1:49:59 PM <br>
 *
 * @author Peter Withers <br>
 */
public interface PluginDialogHandler {

    static enum DialogueType {

        open, save, custom
    };

    void addMessageDialogToQueue(String messageString, String messageTitle);

    boolean showConfirmDialogBox(String messageString, String messageTitle);

    int showDialogBox(String message, String title, int optionType, int messageType);

    int showDialogBox(String message, String title, int optionType, int messageType, Object[] options, Object initialValue);

    File[] showFileSelectBox(String titleText, boolean directorySelectOnly, boolean multipleSelect, Map<String, javax.swing.filechooser.FileFilter> fileFilterMap, DialogueType dialogueType, JComponent customAccessory);
}
