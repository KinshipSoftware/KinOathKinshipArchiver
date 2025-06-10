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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.SimpleFormatter;
import nl.mpi.arbil.userstorage.CommonsSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures logging for Arbil, either from property files or by what is available in a provided {@link SessionStorage}
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public final class ArbilLogConfigurer {

    private final static Logger logger = LoggerFactory.getLogger(ArbilLogConfigurer.class);
    private final String logFilePrefix;
    private final ApplicationVersion appVersion;

    public ArbilLogConfigurer(ApplicationVersion appVersion, String logFilePrefix) {
	this.logFilePrefix = logFilePrefix;
	this.appVersion = appVersion;
    }

    /**
     * Reads logging properties from the provided resource by requesting its input stream
     *
     * @param scopeClass class that provides scope for acquiring the resource
     * @param resourceName name of the resource in the scope of the scope class that has the logging properties to load
     * @return whether successful
     * @see #configureLogging(java.io.InputStream)
     */
    public boolean configureLoggingFromResource(Class scopeClass, String resourceName) {
	return configureLogging(scopeClass.getResourceAsStream(resourceName));
    }

    /**
     * Reads logging properties from the provided input stream
     *
     * @param configurationStream stream that has logging properties to load
     * @return whether successful
     * @see LogManager#readConfiguration(java.io.InputStream)
     */
    public boolean configureLogging(InputStream configurationStream) {
	try {
	    LogManager.getLogManager().readConfiguration(configurationStream);
	    return true;
	} catch (IOException ex) {
	    logger.error("Could not configure initial logging", ex);
	} catch (SecurityException ex) {
	    logger.error("Could not configure initial logging", ex);
	}
	return false;
    }

    /**
     * Removes the existing file logger and creates a new one with the specified file name and log level
     *
     * @param logFile file to log to
     * @param level log level to use for the file
     * @throws IOException if there are IO problems opening the log file
     */
    public void setLogFile(File logFile) throws IOException {
	final java.util.logging.Logger defaultLogger = LogManager.getLogManager().getLogger("");
	Level logLevel = Level.WARNING;
	logger.info("Reconfiguring logging to send output to {}", logFile.getAbsolutePath());
	for (Handler handler : defaultLogger.getHandlers()) {
	    if (handler instanceof FileHandler) {
		final FileHandler fileHandler = (FileHandler) handler;
		logger.debug("Closing and removing 1 FileHandler: {}", fileHandler);
		logLevel = fileHandler.getLevel();
		fileHandler.close();
		defaultLogger.removeHandler(handler);
	    }
	}
	final FileHandler handler = new FileHandler(logFile.getAbsolutePath(), true);
	handler.setLevel(logLevel);
	handler.setFormatter(new SimpleFormatter());
	defaultLogger.addHandler(handler);
    }

    /**
     * Configures logging from session storages. Attempts in the following order:
     * <ol>
     * <li>Whether there is a logging.properties file in the
     * {@link CommonsSessionStorage#getApplicationSettingsDirectory() application settings storage directory} and if so uses that to
     * configure logging</li>
     * <li>If not, configures the logger to write to an 'arbil.log' file in the application settings directory</li>
     * </ol>
     *
     * @param sessionStorage session storage to base logging configuration on
     * @return whether logging was successfully configured
     */
    public boolean configureLoggingFromSessionStorage(SessionStorage sessionStorage) {
	final File applicationSettingsDirectory = sessionStorage.getApplicationSettingsDirectory();

	final File loggingPropertiesFile = new File(applicationSettingsDirectory, "logging.properties");
	final boolean foundCustomLoggingProperties = configureFromPropertiesFile(loggingPropertiesFile);

	if (foundCustomLoggingProperties) {
	    return true;
	} else {
	    try {
		removeOldLogs(sessionStorage);
		// Try to configure a log file in the application storage directory
		final File logFile = getLogFile(sessionStorage);
		setLogFile(logFile);
		return true;
	    } catch (IOException ex) {
		logger.warn("Could not configure log file in session storage directory", ex);
		return false;
	    }
	}
    }

    /**
     * Reads logging configuration from a logging.properties file
     *
     * @param sessionStorage
     * @return whether successful
     */
    private boolean configureFromPropertiesFile(File loggingProperties) {
	logger.debug("Looking for custom logging configuration properties at {}", loggingProperties);
	if (loggingProperties.exists()) {
	    try {
		configureLogging(new FileInputStream(loggingProperties));
		logger.info("Reconfigured logging from logging properties at {}", loggingProperties);
		return true;
	    } catch (FileNotFoundException ex) {
		// should not occur, existence has been checked for
		logger.error("Could not find custom logging configuration file {}", loggingProperties, ex);
	    }
	} else {
	    logger.debug("No custom logging configuration found");
	}
	return false;
    }

    public File getLogFile(SessionStorage sessionStorage) {
	File file = new File(sessionStorage.getApplicationSettingsDirectory(), getCurrentVersionLogFileName());
	if (!file.exists()) {
	    startNewLogFile(file, sessionStorage);
	}
	return file;
    }

    private String getCurrentVersionLogFileName() {
	return logFilePrefix + appVersion.currentMajor + "-" + appVersion.currentMinor + "-" + appVersion.currentRevision + ".txt";
    }

    private void startNewLogFile(File file, SessionStorage sessionStorage) {
	try {
	    FileWriter logFile = new FileWriter(file, false);
	    logFile.append(appVersion.applicationTitle + " log" + System.getProperty("line.separator")
		    + "Version: " + appVersion.currentMajor + "." + appVersion.currentMinor + "." + appVersion.currentRevision + System.getProperty("line.separator")
		    + appVersion.lastCommitDate + System.getProperty("line.separator")
		    + "Compile Date: " + appVersion.compileDate + System.getProperty("line.separator")
		    + "Operating System: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version") + System.getProperty("line.separator")
		    + "Java version: " + System.getProperty("java.version") + " by " + System.getProperty("java.vendor") + System.getProperty("line.separator")
		    + "User: " + System.getProperty("user.name") + System.getProperty("line.separator")
		    + "Storage directory: " + sessionStorage.getApplicationSettingsDirectory().toString() + System.getProperty("line.separator")
		    + "Project directory: " + sessionStorage.getProjectDirectory().toString() + System.getProperty("line.separator")
		    + "Project working directory: " + sessionStorage.getProjectWorkingDirectory().toString() + System.getProperty("line.separator")
		    + "Log started: " + new Date().toString() + System.getProperty("line.separator"));
	    logFile.append("======================================================================" + System.getProperty("line.separator"));
	    logFile.close();
	} catch (IOException ex) {
	    logger.error("failed to write to the log", ex);
	}
    }

    private void removeOldLogs(SessionStorage sessionStorage) {
	// look for previous error logs for this version only
	String currentApplicationVersionMatch = logFilePrefix + appVersion.currentMajor + "-" + appVersion.currentMinor + "-";
	String currentLogFileMatch = getCurrentVersionLogFileName();
	for (String currentFile : sessionStorage.getApplicationSettingsDirectory().list()) {
	    if (currentFile.startsWith(currentApplicationVersionMatch)) {
		if (!currentFile.startsWith(currentLogFileMatch)) {
		    logger.debug("deleting old log file: {}", currentFile);
		    if (!new File(sessionStorage.getApplicationSettingsDirectory(), currentFile).delete()) {
			logger.warn("Did not delete old log file: {}", currentFile);
		    }
		}
	    }
	}
    }
}
