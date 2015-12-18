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
package de.simu.decomap.component.mapping.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.decoit.simumetadata.CredentialType;
import de.simu.decomap.component.mapping.MappingInterface;
import de.simu.decomap.component.mapping.result.MappingResult;
import de.simu.decomap.component.mapping.result.OpenVPNMappingResult;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.enums.PublishType;
import de.simu.decomap.util.Toolbox;

/**
 * Mapping-Class for converting Results from OpenVPNFilePollingThread to
 * IF-MAP-Results
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 * @author Leonid Schwenke, DECOUT GmbH
 */
public class OpenVPNMapper implements MappingInterface {

	private int arCounter;

	// list of clients to be published
	// private HashMap<String, OpenVPNMappingResult> unfinishedEvents = new
	// HashMap<String, OpenVPNMappingResult>();

	// List of connected clients
	private ArrayList<OpenVPNMappingResult> currentClientsList = new ArrayList<OpenVPNMappingResult>();

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init(GeneralConfig mainConfig) {
		logger.info("Initilizing OpenVPNMapper");
		// no values from configuration required here...
	}

	// Mon Jan 19 10:15:43 2015 us=337182 10.10.100.26:52587 VERIFY OK: depth=0,
	// /C=DE/ST=HB/L=Bremen/O=DECOIT/OU=changeme/CN=vpnclient-DD/name=changeme/emailAddress=mail@host.domain

	// Mon Jan 19 10:34:32 2015 us=122626 lschwenke-ws/10.10.100.26:55518 MULTI:
	// Learn: 172.16.0.6 -> lschwenke-ws/10.10.100.26:55518
	// Mon Jan 19 10:34:32 2015 us=122636 lschwenke-ws/10.10.100.26:55518 MULTI:
	// primary virtual IP for lschwenke-ws/10.10.100.26:55518: 172.16.0.6

	// Tue Jan 20 09:06:38 2015 us=9320 10.10.100.26:42114 write UDPv4
	// [ECONNREFUSED]: Connection refused (code=111)

	@Override
	public MappingResult[] getMappingResult(
			final List<HashMap<String, String>> data, final String publisherId) {
		List<OpenVPNMappingResult> mappingResultList = new ArrayList<OpenVPNMappingResult>();

		Date datum = null;
		Calendar date = null;

		if (data.size() < 1 && currentClientsList.size() > 0) {
			if (logger.isDebugEnabled()) {
				logger.debug("No connection left. Deleting all Events!");
			}
			for (OpenVPNMappingResult current : currentClientsList) {
				current.setPublishType(PublishType.DELETE);
			}
			mappingResultList.addAll(currentClientsList);
			currentClientsList.clear();
			return mappingResultList
					.toArray(new MappingResult[mappingResultList.size()]);
		}

		// convert passed in entries to list of OpenVPNMappingResults
		List<OpenVPNMappingResult> passedInEntries = new ArrayList<>();
		for (HashMap<String, String> hashMap : data) {
			String line = hashMap.get("0");

			if (logger.isDebugEnabled()) {
				logger.debug("Start map line: " + line);
			}
			Matcher dateMatcher = Toolbox.getRegExPattern("regex.date")
					.matcher(line);
			if (dateMatcher.find()) {
				try {
					datum = Toolbox.dateFormatNoDay.parse(dateMatcher.group());
					date = Toolbox.getCalendarFromString(
							Toolbox.calenderFormat.format(datum),
							Toolbox.calenderFormat.toPattern(), null);
				} catch (ParseException e) {
					logger.error("Parse exception on date. Skipping line!");
					continue;
				}

				String[] entries = line.split(",");
				OpenVPNMappingResult mappingResult = new OpenVPNMappingResult();

				mappingResult.setDiscoveredTime(date);
				mappingResult.setLoginSuccess(true);
				mappingResult.setCredentialType(CredentialType.PUBLIC_KEY);
				mappingResult.setIdentity(entries[1]);
				mappingResult.setIp(entries[2].split(":")[0]);

				Matcher ip4Matcher = Toolbox.getRegExPattern("regex.isip4")
						.matcher(line);
				Matcher ip6Matcher = Toolbox.getRegExPattern("regex.isip6")
						.matcher(line);
				if (ip4Matcher.find()) {
					mappingResult.setIpType("IPv4");
				} else if (ip6Matcher.find()) {
					mappingResult.setIpType("IPv6");
				} else {
					logger.error("Error while parsing local ip! Skipping line!");
					continue;
				}
				mappingResult.setClientIpAddress(mappingResult.getIp());
				mappingResult.setVpnIpAddress(entries[0]);
				ip4Matcher = Toolbox.getRegExPattern("regex.ip4").matcher(line);
				ip6Matcher = Toolbox.getRegExPattern("regex.ip6").matcher(line);
				if (ip4Matcher.find()) {
					mappingResult.setVpnIpType("IPv4");
				} else if (ip6Matcher.find()) {
					mappingResult.setVpnIpType("IPv6");
				} else {
					logger.error("Error while parsing virtuell ip! Skipping line!");
					continue;
				}

				passedInEntries.add(mappingResult);
			}

			// check for new clients
			for (OpenVPNMappingResult openVPNMappingResult : passedInEntries) {
				// passed in client is not in client-list -> add
				if (!currentClientsList.contains(openVPNMappingResult)) {
					// openVPNMappingResult.setArCounter(arCounter++);
					openVPNMappingResult.setName("AccessRequest: " + arCounter);
					arCounter++;
					mappingResultList.add(openVPNMappingResult);
					currentClientsList.add(openVPNMappingResult);
					if (logger.isDebugEnabled()) {
						logger.debug("Adding event: "
								+ openVPNMappingResult.toString());
					}
				}
			}

			// check for clients to be deleted
			for (int i = 0; i < currentClientsList.size(); i++) {
				// client from client-list is not in passed in result ->
				// delete
				OpenVPNMappingResult current = currentClientsList.get(i);
				if (!passedInEntries.contains(current)) {
					current.setPublishType(PublishType.DELETE);
					mappingResultList.add(current);
					currentClientsList.remove(i);
					if (logger.isDebugEnabled()) {
						logger.debug("Add delete event: " + current.toString());
					}
				}
			}
		}

		return mappingResultList.toArray(new MappingResult[mappingResultList
				.size()]);
	}

}
