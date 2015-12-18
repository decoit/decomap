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

/*
 * NagiosEventMappingFactory.java 0.2 13/02/08
 *
 * DEVELOPED BY DECOIT GMBH WITHIN THE ESUKOM-PROJECT: http://www.decoit.de/
 * http://www.esukom.de/cms/front_content.php?idcat=10&lang=1
 *
 * DERIVED FROM THE DHCP-IFMAP-CLIENT-IMPLEMENTATION DEVELOPED BY FHH/TRUST WITHIN THE IRON-PROJECT:
 * http://trust.inform.fh-hannover.de/joomla/
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simu.decomap.component.mapping.EventMapper;
import de.simu.decomap.component.mapping.result.EventMappingResult;
import de.simu.decomap.component.polling.impl.helper.IcingaRestHostGetter;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.enums.IpType;
import de.simu.decomap.enums.PublishType;

/**
 * Mapping-Class for converting Results from IcingaRestPollingThread to
 * IF-MAP-Results
 * 
 * @author Leonid Schwenke
 */
public class IcingaRestEventMapper extends EventMapper {

	// helper to get the ip from a hostname
	private IcingaRestHostGetter hostGetter;

	private ArrayList<EventMappingResult> previousEventMappingResults = new ArrayList<>();

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public IcingaRestEventMapper() {
	}

	@Override
	public void init(GeneralConfig mainConfig) {
		super.init(mainConfig);
		logger.info("Initilizing IcingaRestEventMapper");

		hostGetter = IcingaRestHostGetter.getInstance(mainConfig);
	}

	@Override
	protected EventMappingResult[] getEventMappingResult(
			final List<HashMap<String, String>> tmpResultList,
			final String publisherId) {
		if (tmpResultList == null || tmpResultList.isEmpty()) {
			logger.debug("MappingResult is empty or null! Skiping mapping!");
			return null;
		}

		ArrayList<EventMappingResult> mappingResultList = new ArrayList<>();

		// message from log
		String[] log;

		// hostname
		String server;

		for (int i = tmpResultList.size() - 1; i >= 0; i--) {
			HashMap<String, String> map = tmpResultList.get(i);
			log = map.get("log_entry").split(";");
			if (logger.isDebugEnabled()) {
				logger.debug("Starting check entry " + i + ": " + log[0]);
			}

			// mapping data if data is relevant
			if (log[0].startsWith("HOST") || log[0].startsWith("SERVICE")) {
				server = log[0].split(": ", 2)[1];
				EventMappingResult event = new EventMappingResult();
				event.setDiscoveredTime(map.get("date_time"));

				event.setIp(hostGetter.getIPforHost(server));

				// setting event name
				if (log[0].startsWith("HOST ALERT:")) {
					event.setName(log[0] + " " + log[1]);
				} else if (log[0].startsWith("SERVICE ALERT:")) {
					event.setName(log[0] + " " + log[1] + " " + log[2]);
				} else if (log[0].startsWith("HOST")
						|| log[0].startsWith("SERVICE")) {
					event.setName(map.get("log_entry"));
				}
				// set predefined values from mapping.properties
				event.setIpType(IpType.IPV4.getTypeString());
				event.setDiscovererId(publisherId);

				if (event.getIp() != null) {

					// check if event is "duplicated"
					if (publishUpdate) {
						for (int j = 0; j < previousEventMappingResults.size(); j++) {
							EventMappingResult current = previousEventMappingResults
									.get(j);
							if (current.getIp().equals(event.getIp())) {
								String newEventName = current.getName()
										.substring(
												0,
												current.getName().lastIndexOf(
														" "));
								String oldEventName = event.getName()
										.substring(
												0,
												event.getName()
														.lastIndexOf(" "));
								if (newEventName.equals(oldEventName)) {
									current.setPublishType(PublishType.DELETE);
									mappingResultList.add(current);
									previousEventMappingResults.remove(j);
								}
							}
						}
					}

					event.setPublishType(publishType);

					if (logger.isDebugEnabled()) {
						logger.debug("Adding Event: " + event.toString());
					}
					previousEventMappingResults.add(event);
					mappingResultList.add(event);
				} else {
					logger.warn("Rest IP getter couldn't get IP from Event: "
							+ event.getName() + ", because can'f find Host: "
							+ server + ". Skipping Event!");
				}
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("skipping entry because its started wrong");
				}
			}
		}
		return mappingResultList
				.toArray(new EventMappingResult[mappingResultList.size()]);
	}

}
