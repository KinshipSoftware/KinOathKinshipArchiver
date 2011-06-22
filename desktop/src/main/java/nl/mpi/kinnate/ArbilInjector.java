package nl.mpi.kinnate;

import java.awt.datatransfer.ClipboardOwner;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader;
import nl.mpi.arbil.clarin.profiles.CmdiTemplate;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilEntityResolver;
import nl.mpi.arbil.data.ArbilJournal;
import nl.mpi.arbil.data.ArbilVocabularies;
import nl.mpi.arbil.data.DocumentationLanguages;
import nl.mpi.arbil.data.FieldChangeTriggers;
import nl.mpi.arbil.data.MetadataBuilder;
import nl.mpi.arbil.data.TreeHelper;
import nl.mpi.arbil.data.metadatafile.CmdiUtils;
import nl.mpi.arbil.data.metadatafile.ImdiUtils;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.templates.ArbilFavourites;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.ui.ArbilFieldViews;
import nl.mpi.arbil.ui.ArbilTableModel;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.util.XsdChecker;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.arbil.util.BinaryMetadataReader;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.WindowManager;

/**
 * Takes care of injecting certain class instances into objects or classes.
 * This provides us with a sort of dependency injection, which enables loosening
 * the coupling between for example data classes and UI classes.
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * Cut down version as required for kinship
 * Created on : May 3, 2011, 12:17:34 PM
 * @author     : Peter Withers
 */
public class ArbilInjector {

    private final static BugCatcher bugCatcher = GuiHelper.linorgBugCatcher;
    private final static MessageDialogHandler messageDialogHandler = ArbilWindowManager.getSingleInstance();
    private final static WindowManager windowManager = ArbilWindowManager.getSingleInstance();
    private final static ClipboardOwner clipboardOwner = GuiHelper.getClipboardOwner();

    /**
     * Does initial injection into static classes. Needs to be called only once.
     */
    public static synchronized void injectHandlers() {
        // Inject window manager
        ArbilBugCatcher.setWindowManager(windowManager);
        ArbilSessionStorage.setWindowManager(windowManager);
        ArbilTree.setWindowManager(windowManager);
        ArbilVocabularies.setWindowManager(windowManager);
        MetadataBuilder.setWindowManager(windowManager);

        // Inject message dialog handler
        ArbilComponentBuilder.setMessageDialogHandler(messageDialogHandler);
//        ArbilCsvImporter.setMessageDialogHandler(messageDialogHandler);
        ArbilDataNode.setMessageDialogHandler(messageDialogHandler);
        ArbilFavourites.setMessageDialogHandler(messageDialogHandler);
        ArbilJournal.setMessageDialogHandler(messageDialogHandler);
        ArbilSessionStorage.setMessageDialogHandler(messageDialogHandler);
        ArbilTableModel.setMessageDialogHandler(messageDialogHandler);
        ArbilTemplate.setMessageDialogHandler(messageDialogHandler);
//        ArbilToHtmlConverter.setMessageDialogHandler(messageDialogHandler);
//        ArbilVersionChecker.setMessageDialogHandler(messageDialogHandler);
        ArbilVocabularies.setMessageDialogHandler(messageDialogHandler);
        CmdiTemplate.setMessageDialogHandler(messageDialogHandler);
        FieldChangeTriggers.setMessageDialogHandler(messageDialogHandler);
        ImdiUtils.setMessageDialogHandler(messageDialogHandler);
        MetadataBuilder.setMessageDialogHandler(messageDialogHandler);
        MetadataReader.setMessageDialogHandler(messageDialogHandler);
//        ShibbolethNegotiator.setMessageDialogHandler(messageDialogHandler);
        TreeHelper.setMessageDialogHandler(messageDialogHandler);

        // Inject bug catcher
        ArbilComponentBuilder.setBugCatcher(bugCatcher);
//        ArbilCsvImporter.setBugCatcher(bugCatcher);
        ArbilDataNode.setBugCatcher(bugCatcher);
        ArbilEntityResolver.setBugCatcher(bugCatcher);
        ArbilFavourites.setBugCatcher(bugCatcher);
        ArbilFieldViews.setBugCatcher(bugCatcher);
        ArbilIcons.setBugCatcher(bugCatcher);
        ArbilJournal.setBugCatcher(bugCatcher);
        ArbilSessionStorage.setBugCatcher(bugCatcher);
        ArbilTableModel.setBugCatcher(bugCatcher);
        ArbilTemplate.setBugCatcher(bugCatcher);
        ArbilTemplateManager.setBugCatcher(bugCatcher);
//        ArbilToHtmlConverter.setBugCatcher(bugCatcher);
        ArbilTree.setBugCatcher(bugCatcher);
//        ArbilVersionChecker.setBugCatcher(bugCatcher);
        ArbilVocabularies.setBugCatcher(bugCatcher);
        BinaryMetadataReader.setBugCatcher(bugCatcher);
        CmdiComponentLinkReader.setBugCatcher(bugCatcher);
        CmdiTemplate.setBugCatcher(bugCatcher);
        CmdiUtils.setBugCatcher(bugCatcher);
        DocumentationLanguages.setBugCatcher(bugCatcher);
        ImdiUtils.setBugCatcher(bugCatcher);
        MetadataBuilder.setBugCatcher(bugCatcher);
        MetadataReader.setBugCatcher(bugCatcher);
        MimeHashQueue.setBugCatcher(bugCatcher);
//        ShibbolethNegotiator.setBugCatcher(bugCatcher);
        TreeHelper.setBugCatcher(bugCatcher);
        XsdChecker.setBugCatcher(bugCatcher);

        // Clipboard owner
//        ArbilTree.setClipboardOwner(clipboardOwner);
//        ArbilTableModel.setClipboardOwner(clipboardOwner);
    }
}
