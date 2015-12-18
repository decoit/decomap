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

public class RadiusIpFolderPollingThread extends FilePollingThread {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private File detailsFile;
	private long detailsFileLastModified;
	private int detailsFileLastEntryNumber = 0;
	private boolean detailsFileChangesFound = false;

	private File replayFile;
	private long replayFileLastModified;
	private int replayFileLastEntryNumber = 0;
	private boolean replayFileChangesFound = false;

	/**
	 * Constructor
	 * 
	 * @param folderPath
	 * 		Path of folder to monitor
	 * @param sleepTime
	 * 		Sleep Time between scans
	 */
	public RadiusIpFolderPollingThread(String folderPath, int sleepTime) {
		filePath = folderPath;
		this.sleepTime = sleepTime;
		isFirstStart = true;

		file = new File(filePath);
		if (!file.isDirectory()) {
			logger.warn("WARNING! File is not a valid folder!");
		}

		getLatestModified();
	}

	@Override
	public void run() {

		while (running) {
			// if (!pausing) {
			try {
				logger.info("Poll Rottation!");

				if (detailsFile != null && detailsFile.exists()) {
					if (detailsFileLastModified < detailsFile.lastModified()) {
						detailsFileChangesFound = true;
					}
				} else {
					logger.debug("File null or not found");
				}

				if (replayFile != null && replayFile.exists()) {
					if (replayFileLastModified < replayFile.lastModified()) {
						replayFileChangesFound = true;
					}
				} else {
					logger.debug("File null or not found");
				}

				if (replayFileChangesFound || detailsFileChangesFound) {
					logger.info("Changes found! Start reading!");
					putIntoMappingQueue(readFile());
				} else {
					logger.info("No changes in monitored files!");
				}

				if (!getLatestModified()) {
					Thread.sleep(sleepTime);
				}

			} catch (Exception e) {
				logger.error("Error while runnning!", e);
				running = false;
			}
			// }
		}

	}

	@Override
	protected ArrayList<HashMap<String, String>> readFile() {
		logger.info("Start reading file: " + file.getAbsolutePath());

		int newEntryNumber = 1;
		int currentCycleLineNumber = 0;

		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> tempEventData = new HashMap<String, String>();

		BufferedReader logReader = null;
		String[] resultDetail = null;
		String line = null;

		try {
			// Reading DetailsFile
			if (detailsFileChangesFound) {
				detailsFileChangesFound = false;
				logger.info("Start reading file: "
						+ detailsFile.getAbsolutePath());
				logReader = new BufferedReader(new FileReader(detailsFile), 1);
				resultDetail = parseDetialInSingleLine(logReader);

				for (int j = 0; j < resultDetail.length; j++) {
					currentCycleLineNumber++;
					if (isFirstStart) {
						detailsFileLastEntryNumber++;
					} else if (currentCycleLineNumber > detailsFileLastEntryNumber) {
						if (logger.isDebugEnabled()) {
							logger.debug("Reading line "
									+ currentCycleLineNumber + ": " + line);
						}
						tempEventData.put(String.valueOf(newEntryNumber),
								resultDetail[j]);
						newEntryNumber++;
					}
				}
				detailsFileLastEntryNumber = currentCycleLineNumber;

				logReader.close();
			}

			// Reading replayFile
			if (replayFileChangesFound) {
				replayFileChangesFound = false;
				currentCycleLineNumber = 0;

				logger.info("Start reading file: "
						+ replayFile.getAbsolutePath());
				logReader = new BufferedReader(new FileReader(replayFile), 1);
				resultDetail = parseDetialInSingleLine(logReader);

				for (int j = 0; j < resultDetail.length; j++) {
					currentCycleLineNumber++;
					if (isFirstStart) {
						replayFileLastEntryNumber++;
					} else if (currentCycleLineNumber > replayFileLastEntryNumber) {
						if (logger.isDebugEnabled()) {
							logger.debug("Reading line "
									+ currentCycleLineNumber + ": " + line);
						}
						tempEventData.put(String.valueOf(newEntryNumber),
								resultDetail[j]);
						newEntryNumber++;
					}
				}
				replayFileLastEntryNumber = currentCycleLineNumber;

				logReader.close();
			}

			if (isFirstStart) {
				// first cycle finished
				isFirstStart = false;
			}

		} catch (FileNotFoundException ex) {
			logger.error("could not find  logfile.", ex);
			running = false;
		} catch (IOException ex) {
			logger.error("I/O error while reading logfile", ex);
			running = false;
		} catch (NullPointerException e) {
			logger.error("Nullpointer!", e);
			running = false;
		}
		result.add(tempEventData);
		return result;
	}

	/**
	 * Parse one entry into one line
	 * 
	 * @param msg
	 *            reader to read from
	 * @return single line
	 * @throws IOException
	 */
	public String[] parseDetialInSingleLine(BufferedReader msg)
			throws IOException {
		String line = null;
		String temp = "";

		ArrayList<String> tempArray = new ArrayList<String>();
		while ((line = msg.readLine()) != null) {
			if (!line.equalsIgnoreCase("")) {
				temp += line + ",";
			} else if (temp != null) {
				tempArray.add(temp);
				temp = "";
			}
		}
		String result[] = new String[tempArray.size()];
		for (int i = 0; i < tempArray.size(); i++) {
			result[i] = tempArray.get(i);
		}

		return result;
	}

	/**
	 * Checks if there is a change in the folder and setting the newest file
	 * @return Is there a new modified file
	 */
	private boolean getLatestModified() {
		boolean change = false;
		if (file.isDirectory()) {
			if (lastModified < file.lastModified()) {
				logger.debug("Search in " + file.getAbsolutePath()
						+ " for newst files");

				File[] content = file.listFiles();

				for (int j = 0; j < content.length; j++) {
					if (content[j].getName().startsWith("reply-detail-")
							&& (replayFile == null || replayFile.lastModified() < content[j]
									.lastModified())) {
						replayFile = content[j];
						replayFileLastEntryNumber = 0;
						replayFileLastModified = replayFile.lastModified();
						replayFileChangesFound = true;
						change = true;
					} else if (content[j].getName().startsWith("detail-")
							&& (detailsFile == null || detailsFile
									.lastModified() < content[j].lastModified())) {
						detailsFile = content[j];
						detailsFileLastEntryNumber = 0;
						detailsFileLastModified = detailsFile.lastModified();
						detailsFileChangesFound = true;
						change = true;
					}
				}
				lastModified = file.lastModified();
			} else {
				logger.info("No changes found in " + file.getAbsolutePath());
			}
		} else {
			logger.warn("WARNING! File is not a valid folder!");
		}
		return change;
	}

	@Override
	@Deprecated
	/**
	 * Does nothing!
	 */
	public void init(GeneralConfig mainConfig) {
		// DO NOTHING!!!
	}


}
