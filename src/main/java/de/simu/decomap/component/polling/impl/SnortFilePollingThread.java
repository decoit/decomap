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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simu.decomap.component.polling.FilePollingThread;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.main.IfMapClient;

/**
 * Thread for polling the Snort Alertlog-file for new entries
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 */
public class SnortFilePollingThread extends FilePollingThread {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init(final GeneralConfig mainConfig) {
		super.init(mainConfig);
		logger.info("Initilizing SnortFilePollingThread");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ArrayList<HashMap<String, String>> readFile() {
		logger.info("Start reading file");

		// used for distinction of the different lines of a single event entry
		int currentLineInSingleEntry = 0;

		// number of read lines in current cycle
		int currentCycleLineNumber = 0;

		BufferedReader input = null;
		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> tempEventData = new HashMap<String, String>();

		try {
			input = new BufferedReader(new FileReader(file), 1);
			String line = null;
			while ((line = input.readLine()) != null) {

				if (isFirstStart) {
					lastEntryLineNumber++;
				}
				currentCycleLineNumber++;
				if (currentCycleLineNumber > lastEntryLineNumber) {
					if (logger.isDebugEnabled()) {
						logger.debug("Reading line " + currentCycleLineNumber + ": " + line);
					}
					// new event entry in log-file detected
					if (line.startsWith("[**]")) {

						// convert last entry
						if (!tempEventData.isEmpty()) {
							result.add((HashMap<String, String>) tempEventData.clone());

							// reset entries from last cycle
							tempEventData.clear();
						}

						// reset current entry line counter
						currentLineInSingleEntry = 0;
					}
					tempEventData.put(new Integer(currentLineInSingleEntry).toString(), line);
					currentLineInSingleEntry++;
				}
			}

			// loop over, add last remaining entry
			if (!tempEventData.isEmpty()) {
				result.add(tempEventData);
			}

			if (!isFirstStart) {
				// set new last entry index
				lastEntryLineNumber = currentCycleLineNumber;
			} else {
				// first cycle finished
				isFirstStart = false;
			}

			logger.info("Reading done!");

		} catch (FileNotFoundException ex) {
			logger.error("could not found log-file at " + filePath);
			IfMapClient.criticalError(ex);
		} catch (IOException ex) {
			logger.error("I/O error while reading log-file at " + filePath);
			IfMapClient.criticalError(ex);
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				logger.error("error while closing input buffer");
				IfMapClient.criticalError(e);
			}
		}

		return result;
	}
}