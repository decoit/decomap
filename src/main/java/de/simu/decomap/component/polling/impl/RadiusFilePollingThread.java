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
package de.simu.decomap.component.polling.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simu.decomap.component.polling.FilePollingThread;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.config.interfaces.polling.RadiusFilePollingConfig;
import de.simu.decomap.main.IfMapClient;
import de.simu.decomap.util.Toolbox;

/**
 * Thread for polling the log-File for new radius-events
 * 
 * @author Leonid Schwenke, DECOIT GmbH
 * 
 */
public class RadiusFilePollingThread extends FilePollingThread {

	// radacct dir path
	// properties from config-file
	private String radacctDir;

	// position of last read-in entry in last cycle
	protected int lastEntryLineNumber = 0;

	// position of last read-in entry in last cycle of radacct
	protected int lastEntryLineNumberRadAcct = 0;


	// save the read line numbers of radacct files
	private HashMap<String, RadiusIpFolderPollingThread> ipFolderThreadMap = new HashMap<String, RadiusIpFolderPollingThread>();

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init(final GeneralConfig mainConfig) {
		super.init(mainConfig);
		logger.info("Initilizing RadiusFilePollingThread");
		RadiusFilePollingConfig configuration = (RadiusFilePollingConfig) Toolbox
				.loadConfig(mainConfig.pollingComponentConfigPath(),
						RadiusFilePollingConfig.class);
		radacctDir = configuration.radacctDirPath();

	}

	@Override
	protected ArrayList<HashMap<String, String>> readFile() {
		logger.info("Start reading file" + filePath);

		int currentCycleLineNumber = 0;
		int newEntryNumber = 1;

		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> tempEventData = new HashMap<String, String>();

		BufferedReader logReader = null;
		String line = null;

		try {
			logReader = new BufferedReader(new FileReader(file), 1);

			while ((line = logReader.readLine()) != null) {

				if (isFirstStart) {
					lastEntryLineNumber++;
				}
				currentCycleLineNumber++;
				if (currentCycleLineNumber > lastEntryLineNumber) {
					if (logger.isDebugEnabled()) {
						logger.debug("Reading line " + currentCycleLineNumber
								+ ": " + line);
					}
					if (line.contains("Auth:")) {
						tempEventData.put(String.valueOf(newEntryNumber), line);
						newEntryNumber++;
					}
				}
			}
			if (isFirstStart) {
				// first cycle finished
				isFirstStart = false;
			}
			lastEntryLineNumber = currentCycleLineNumber;

			logReader.close();
		} catch (FileNotFoundException ex) {
			logger.error("could not find  logfile.");
			IfMapClient.criticalError(ex);
		} catch (IOException ex) {
			logger.error("I/O error while reading logfile");
			IfMapClient.criticalError(ex);
		} catch (NullPointerException e) {
			logger.error("Nullpointer!");
			IfMapClient.criticalError(e);
		}
		result.add(tempEventData);
		return result;
	}

	@Override
	public void run() {

		long actualLastModified;
		while (running) {
			if (!pausing && file != null) {
				try {
					logger.info("Poll Rottation!");
					actualLastModified = file.lastModified();

					// check if file changes have occurred since last cycle
					logger.info("Checking files for updates");
					if (isFirstStart | lastModified != actualLastModified) {
						lastModified = actualLastModified;
						logger.info("reading new entries found inside radius-logfile");
						putIntoMappingQueue(readFile());
					}

					manageIpFolderThreads();

					Thread.sleep(sleepTime);
				} catch (Exception e) {
					logger.error("error while polling log file!");
					IfMapClient.criticalError(e);
				}
			}
		}

		stopAllIpFolderThreads();
	}

	/**
	 * Stopping all file subthreads
	 */
	private void stopAllIpFolderThreads() {
		for (String key : ipFolderThreadMap.keySet()) {
			ipFolderThreadMap.get(key).running = false;
		}
	}

	/**
	 * Starting subfilethreads for each ipfolder
	 */
	private void manageIpFolderThreads() {
		for (String path : getRaddactContentFolders()) {
			if (ipFolderThreadMap.get(path) == null) {
				// TODO: vll nicht mehr vorhandene Ordnerthreads schließen?

				RadiusIpFolderPollingThread newIpFolderThread = new RadiusIpFolderPollingThread(
						path, sleepTime);
				newIpFolderThread.running = true;
				new Thread(newIpFolderThread).start();
				ipFolderThreadMap.put(path, newIpFolderThread);
			}
		}
	}

	/**
	 * 
	 * @return Content of the raddactfolder
	 */
	private ArrayList<String> getRaddactContentFolders() {
		ArrayList<String> ipFolderList = new ArrayList<String>();
		File radacctFolder = new File(radacctDir);

		if (radacctFolder.exists()) {
			File[] ipFolders = radacctFolder.listFiles();
			for (int i = 0; i < ipFolders.length; i++) {
				ipFolderList.add(ipFolders[i].getAbsolutePath());
			}
		}
		return ipFolderList;
	}

}
