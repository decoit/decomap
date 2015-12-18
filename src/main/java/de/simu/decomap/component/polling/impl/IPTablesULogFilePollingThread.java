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
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simu.decomap.component.polling.FilePollingThread;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.config.interfaces.polling.FilterListFilePollingConfig;
import de.simu.decomap.main.IfMapClient;
import de.simu.decomap.util.Toolbox;

/**
 * Thread for polling the ulog-File for new iptables-events
 * 
 * @version 0.2
 * @author Dennis Dunekacke, DDECOIT GmbH
 */
public class IPTablesULogFilePollingThread extends FilePollingThread {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String[] blackList = new String[0];

	private boolean isWhiteList = false;

	/**
	 * constructor
	 * 
	 * @param path
	 *            path of snort-log-file
	 */
	public IPTablesULogFilePollingThread() {
	}

	@Override
	public void init(final GeneralConfig mainConfig) {
		super.init(mainConfig);
		logger.info("Initilizing IPTablesULogFilePollingThread");

		FilterListFilePollingConfig configuration = (FilterListFilePollingConfig) Toolbox
				.loadConfig(mainConfig.pollingComponentConfigPath(),
						FilterListFilePollingConfig.class);
		blackList = configuration.iplistFilterlist();
		for (int i = 0; i < blackList.length; i++) {
			blackList[i] = blackList[i].replace("*", "\\d{1,3}");
		}

		isWhiteList = configuration.isWhiteList();
	}

	/**
	 * read and parse alert-log-file
	 * 
	 * @return list containing all read in entries from ulog-file
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected ArrayList<HashMap<String, String>> readFile() {
		logger.info("Start reading file");

		int currentLineInSingleEntry = 0;
		int currentCycleLineNumber = 0;

		BufferedReader input = null;
		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> tempEventData = new HashMap<String, String>();

		try {
			input = new BufferedReader(new FileReader(file), 1);
			String line = null;
			String entryDate = null;
			boolean skipCurrentEntry = false;

			while ((line = input.readLine()) != null) {

				if (isFirstStart) {
					lastEntryLineNumber++;
				}

				currentCycleLineNumber++;
				if (currentCycleLineNumber > lastEntryLineNumber) {
					if (logger.isDebugEnabled()) {
						logger.debug("Reading line " + currentCycleLineNumber
								+ ": " + line);
					}
					if (!skipCurrentEntry) {
						// convert last entry
						if (!tempEventData.isEmpty()) {
							result.add((HashMap<String, String>) tempEventData
									.clone());
						}
					} else {
						// reset skip-current-entry-flag
						skipCurrentEntry = false;
					}

					// reset entries from last cycle
					tempEventData.clear();

					// reset current entry line counter
					currentLineInSingleEntry = 0;

					// get ip of current entry and perform blacklist-check
					Matcher ip4Matcher = Toolbox.getRegExPattern("regex.ip4")
							.matcher(line);
					if (ip4Matcher.find()) {
						skipCurrentEntry = checkFilterList(ip4Matcher.group());
						if (isWhiteList) {
							skipCurrentEntry = !skipCurrentEntry;
						}
					} else {
						skipCurrentEntry = true;
						logger.warn("could not find IP4-address in current entry...skipping");
					}

					if (!skipCurrentEntry) {
						// get timestamp of current entry
						Matcher timestampMatcher = Toolbox.getRegExPattern(
								"regex.ulogtimestamp").matcher(line);
						if (timestampMatcher.find()) {
							// convert entry to "ifmap-compatible" format to
							// perform date check
							entryDate = rearrangeDate(Toolbox
									.getNowDateAsString("yyyy")
									+ "/"
									+ timestampMatcher.group());
							// replace current entries date with new format
							String newLine = line.replaceFirst(Toolbox
									.getRegExPattern("regex.ulogtimestamp")
									.toString(), entryDate);
							line = newLine;
						} else {
							skipCurrentEntry = true;
							logger.warn("could not find timestamp in current entry...will now skip this entry");
						}

						// add current entry without time-stamp-check
						tempEventData.put(new Integer(currentLineInSingleEntry)
								.toString(), line);
					}
				}
			}

			// loop over, add last remaining entry
			if (!tempEventData.isEmpty() & !skipCurrentEntry) {
				logger.debug("New entries found!");
				result.add(tempEventData);
			}
			logger.info("Reading done!");

			if (!isFirstStart) {
				// set new last entry index
				lastEntryLineNumber = currentCycleLineNumber;
			} else {
				// first cycle finished
				isFirstStart = false;
			}
		} catch (FileNotFoundException ex) {
			logger.error("could not find ip-tables ulog-file.");
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

	/**
	 * 
	 * @param ip IP to check
	 * @return true if contained in ip list. This list can be a whitelist or blacklist, see configuration.
	 */
	private boolean checkFilterList(String ip) {
		for (String blockedip : blackList) {
			if (ip.matches(blockedip)) {
				if(isWhiteList){
					logger.info("Whitelisted ip " + ip + ". Not skipping entry!");
				} else{
					logger.info("Blacklisted ip " + ip + ". Skipping entry!");
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * rearrange passed in date (as parsed from alertlog-file) to fit IF-MAP
	 * Timestamp format
	 * 
	 * @param String
	 *            currentDate [YYYY/MONTH(3-chars)_DD_HH:MM:SS]
	 * 
	 * @return timestamp in IF-MAP format
	 */
	private String rearrangeDate(String currentDate) {
		String[] date = new String[3];
		date[0] = currentDate.substring(0, 4); // Year
		date[1] = Toolbox.getAplhaNumericMonthMap().get(
				currentDate.substring(5, 8));

		// if the date consist only of one digit, u-log doesn't fill it up with
		// a leading
		// zero (e.g 02.08.2011), instead it leaves a space (e.g. 2.08.2011)
		date[2] = currentDate.substring(9, 11); // Day
		if (date[2].startsWith(" ")) {
			date[2] = date[2].replaceFirst(" ", "0");
		}

		// YYYY/MM/DD-hh:mm:ss.S => [0]Date [1]Time
		String timestamp = currentDate.substring(12, currentDate.length()); // Day

		// return new timestamp-string
		return date[0] + "-" + date[1] + "-" + date[2] + " " + timestamp;
	}
}