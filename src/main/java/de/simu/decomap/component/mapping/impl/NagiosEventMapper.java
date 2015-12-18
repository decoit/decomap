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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simu.decomap.component.mapping.EventMapper;
import de.simu.decomap.component.mapping.result.EventMappingResult;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.enums.IpType;
import de.simu.decomap.enums.PublishType;
import de.simu.decomap.util.Toolbox;

/**
 * Mapping-Class for converting Results from NagiosSocketPollingThread to
 * IF-MAP-Results
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 */
public class NagiosEventMapper extends EventMapper {

	// name of keys for Result-Hashmap-list from NagiosSocketPollingThread
	private final String timestampKey = "timestamp";
	private final String addressKey = "address";
	private final String sourceKey = "source";
	private final String stateKey = "state";

	// prefixes for constructing IF-MAP-Event names
	private final String hostStatePrefix = "Detected Host State: ";
	private final String serviceStatePrefix = "Detected Service State: ";
	private final String undefinedStatePrefix = "Undefined State: ";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init(final GeneralConfig mainConfig) {
		super.init(mainConfig);
		logger.info("Initilizing NagiosEventMapper");
	}

	@Override
	protected EventMappingResult[] getEventMappingResult(
			final List<HashMap<String, String>> res, String publisherId) {
		List<EventMappingResult> mappingResultList = new ArrayList<>();

		for (HashMap<String, String> currentMap : res) {

			// check if all required values are present in passed in result-list
			if (!requiredValuesExists(currentMap)) {
				logger.debug("Not all requiered values found! Skipping Event!");
				break;
			}

			EventMappingResult currentEvent = new EventMappingResult();

			currentEvent.setDiscoveredTime(Toolbox
					.convertTimestampToIfMapFormat(
							currentMap.get(timestampKey), "/", "-"));
			currentEvent.setIp(currentMap.get(addressKey));
			currentEvent.setIpType(IpType.IPV4.getTypeString());
			currentEvent.setDiscovererId(publisherId);
			currentEvent.setSignificance(significance);
			currentEvent.setType(eventType);
			currentEvent.setConfidence(confidence);
			currentEvent.setMagnitude(magnitude);

			String eventName = null;
			if (currentMap.get(sourceKey).startsWith("host")) {
				eventName = hostStatePrefix + currentMap.get(stateKey);
			} else if (currentMap.get(sourceKey).startsWith("service")) {
				eventName = serviceStatePrefix + currentMap.get(stateKey);
			} else {
				eventName = undefinedStatePrefix;
			}
			currentEvent.setName(eventName);

			// if event has been published before, delete old entry
			if (publishUpdate) {
				for (int j = 0; j < previousEventMappingResults.size(); j++) {
					EventMappingResult current = previousEventMappingResults
							.get(j);
					if (current.getIp().equals(currentEvent.getIp())) {
						String newEventName = current.getName().substring(0,
								current.getName().lastIndexOf(" "));
						String oldEventName = currentEvent.getName().substring(
								0, currentEvent.getName().lastIndexOf(" "));
						if (newEventName.equals(oldEventName)) {
							current.setPublishType(PublishType.DELETE);
							mappingResultList.add(current);
							previousEventMappingResults.remove(j);
						}
					}
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Adding event: " + currentEvent.toString());
			}
			previousEventMappingResults.add(currentEvent);

			currentEvent.setPublishType(publishType);

			mappingResultList.add(currentEvent);
		}

		return mappingResultList
				.toArray(new EventMappingResult[mappingResultList.size()]);
	}

	/**
	 * check if passed in HashMap contains all entries that are required to
	 * convert it into an IF-MAP-Result
	 * 
	 * @param map
	 *            HashMap to check for requires values
	 * 
	 * @return true if required values exists
	 */
	private boolean requiredValuesExists(final HashMap<String, String> map) {
		if (map.get(timestampKey) == null || map.get(addressKey) == null
				|| map.get(sourceKey) == null || map.get(stateKey) == null) {
			return false;
		}

		return true;
	}

}
