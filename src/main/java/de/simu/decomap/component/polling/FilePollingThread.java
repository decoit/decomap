/* 
 * Copyright 2015 DECOIT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.simu.decomap.component.polling;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.config.interfaces.polling.FilePollingConfig;
import de.simu.decomap.main.IfMapClient;
import de.simu.decomap.util.Toolbox;

/**
 * Abstract base class for reading and polling files
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 * @author Leonid Schwenke, DECOIT GmbH
 */
public abstract class FilePollingThread extends PollingThread {

	// thread sleeping time between two polls
	protected int sleepTime = 0;

	// perform file-exists-check before running the polling thread
	protected boolean performPreLogFileExistsCheck = false;
	protected boolean logFound = false;

	// properties from config-file
	protected String filePath;

	// log-file to watch/poll
	protected File file;

	// flag for indicating that reading of file is necessary, even if there was
	// no update
	protected boolean isFirstStart = false;

	// position of last read-in entry in last cycle
	protected int lastEntryLineNumber = 0;

	// time stamp of last file modification
	protected long lastModified;

	// log-rotate
	protected boolean useLogRotate;
	protected String logRotatePattern;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 
	 * @return is the log rotation active?
	 */
	public boolean isLogRotateActive() {
		return this.useLogRotate;
	}

	@Override
	public void init(final GeneralConfig mainConfig) {
		logger.info("Initilizing FilePollingThread");

		FilePollingConfig configuration = (FilePollingConfig) Toolbox
				.loadConfig(mainConfig.pollingComponentConfigPath(),
						FilePollingConfig.class);

		// flag indicating first start
		this.isFirstStart = !mainConfig.applicationSendOldEvents();

		// interval between poll-requests
		this.sleepTime = mainConfig.applicationPollingInterval() * 1000;

		// get file-path from configuration
		filePath = configuration.logFilePath(); // null?

		// if log-rotate is activated, build log-file-name
		if (configuration.useLogrotate()) {
			logRotatePattern = configuration.logRotatePattern(); // null?
			filePath = filePath.replace("$",
					Toolbox.getNowDateAsString(logRotatePattern));
			logger.debug("Using logrotation!");
		}

		// open file if precheck-flag from configuration is set
		if (configuration.usePrecheck()) {
			if (!new File(filePath).exists()) {
				logger.error("log-file at [" + filePath
						+ "] could not be found");
				IfMapClient.criticalError(new FileNotFoundException(filePath
						+ " not found!"));
			} else {
				logger.info("opening file at path: [" + filePath + "]");
				file = new File(filePath);
				logFound = true;
				lastModified = file.lastModified();
			}
		}
	}

	@Override
	public void run() {
		while (running) {
			// if (!pausing) {
			try {
				logger.info("Poll Rottation!");
				if (!logFound) {
					if (new File(filePath).exists()) {
						file = new File(filePath);
						lastModified = file.lastModified();
						logFound = true;
					} else {
						logger.info("File not found! Thread start sleep!");
						Thread.sleep(sleepTime);
					}
				}

				if (logFound) {
					logger.info("Checking file for updates!");
					long actualLastModified = file.lastModified();
					// check if file changes have occurred since last cycle
					if (isFirstStart | lastModified != actualLastModified) {
						lastModified = actualLastModified;
						logger.info("Changes found! Start reading file!");
						putIntoMappingQueue(readFile());
					}
					Thread.sleep(sleepTime);
				}
			} catch (Exception e) {
				logger.error("Error while runnning!");
				IfMapClient.criticalError(e);
			}
			// }
		}
	}


	/**
	 * read entries from file and save them into List of HashMaps for related
	 * Mapper-Class to process
	 * 
	 * @return the result-list
	 */
	protected abstract ArrayList<HashMap<String, String>> readFile();
}