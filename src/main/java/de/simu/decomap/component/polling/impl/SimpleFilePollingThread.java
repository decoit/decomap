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
import de.simu.decomap.main.IfMapClient;
import de.simu.decomap.util.Toolbox;

/**
 * Simple class for reading and polling log-files. This class uses the most
 * simple way to read in data from a log-file and can be used for most of the
 * components
 * 
 * @version 0.2
 * @author Dennis Dunekacke, DECOIT GmbH
 */
public class SimpleFilePollingThread extends FilePollingThread {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * read and parse log-file row by row
	 */
	@Override
	protected ArrayList<HashMap<String, String>> readFile() {
		logger.info("Start reading file");

		int currentCycleLineNumber = 0;
		BufferedReader input = null;
		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
		try {
			input = new BufferedReader(new FileReader(file), 1);
			String line = null;
			while ((line = input.readLine()) != null) {
				if (isFirstStart) {
					lastEntryLineNumber++;
				}
				currentCycleLineNumber++;
				if (currentCycleLineNumber > lastEntryLineNumber) {
					if (!Toolbox.isNullOrEmpty(line)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Reading line " + currentCycleLineNumber + ": " + line);
						}
						HashMap<String, String> currentEntry = new HashMap<String, String>();
						currentEntry.put("0", line);
						result.add(currentEntry);
					}
				}
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