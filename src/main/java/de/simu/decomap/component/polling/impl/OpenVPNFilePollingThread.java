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
 * Thread for polling the OpenVPN-Log for new entries
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 */
public class OpenVPNFilePollingThread extends FilePollingThread {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init(final GeneralConfig mainConfig) {
		super.init(mainConfig);
		logger.info("Initilizing OpenVPNFilePollingThread");
	}

	@Override
	protected ArrayList<HashMap<String, String>> readFile() {
		logger.info("Start reading file");

		BufferedReader input = null;
		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();

		try {
			input = new BufferedReader(new FileReader(file), 1);
			String line = null;
			boolean readEntrys = false;
			while ((line = input.readLine()) != null) {

				if (logger.isDebugEnabled()) {
					logger.debug("Reading line " + line);
				}
				if (line.startsWith("Virtual Address,Common Name,Real Address,Last Ref")) {
					readEntrys = true;
				} else if (line.startsWith("GLOBAL STATS")) {
					readEntrys = false;
				} else if (readEntrys) {
					HashMap<String, String> currentEntry = new HashMap<String, String>();
					currentEntry.put("0", line);
					result.add(currentEntry);
				}
			}
			// isFirstStart = false;
			logger.info("Reading done!");
		} catch (FileNotFoundException ex) {
			logger.error("could not find logfile.");
			IfMapClient.criticalError(ex);
		} catch (IOException ex) {
			logger.error("I/O error while reading iptables ulog-file");
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