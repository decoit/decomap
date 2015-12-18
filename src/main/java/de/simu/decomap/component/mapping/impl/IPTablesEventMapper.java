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
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simu.decomap.component.mapping.EventMapper;
import de.simu.decomap.component.mapping.result.EventMappingResult;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.enums.IpType;
import de.simu.decomap.enums.PublishType;
import de.simu.decomap.util.Toolbox;

/**
 * Mapping-Class for converting Results from IpTablesPollingThread to
 * IF-MAP-Results
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 */
public class IPTablesEventMapper extends EventMapper {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init(final GeneralConfig mainConfig) {
		super.init(mainConfig);
		logger.info("Initilizing IPTablesEventMapper");
	}

	@Override
	protected EventMappingResult[] getEventMappingResult(
			final List<HashMap<String, String>> res, final String publisherId) {
		ArrayList<EventMappingResult> mappingResultList = new ArrayList<EventMappingResult>();

		for (HashMap<String, String> hashMap : res) {
			String discTime = null;
			EventMappingResult event = new EventMappingResult();

			// timestamp -> discovered time
			Matcher timestampMatcher = Toolbox.getRegExPattern(
					"regex.ifmaptimestamp").matcher(hashMap.get("0"));
			if (timestampMatcher != null && timestampMatcher.find()) {
				discTime = timestampMatcher.group();
			} else {
				logger.warn("Not all requiered values found! Skipping Event!");
				continue;
			}

			// get source and destination-ip and append it to event-name
			String srcIp, dstIp = null;
			Matcher srcMatcher = Toolbox.getRegExPattern("regex.ip4.src")
					.matcher(hashMap.get("0"));
			if (srcMatcher != null && srcMatcher.find()) {
				srcIp = srcMatcher.group(1);
			} else {
				logger.warn("Not src IP found! Skipping Event!");
				continue;
			}
			event.setIp(srcIp);
			event.setClientIpAddress(srcIp);

			Matcher dstMatcher = Toolbox.getRegExPattern("regex.ip4.dst")
					.matcher(hashMap.get("0"));
			if (dstMatcher != null && dstMatcher.find()) {
				dstIp = dstMatcher.group(1);
			} else {
				logger.warn("Not dst IP found! Skipping Event!");
				continue;
			}

			// set event name
			event.setName("datastream detected from " + srcIp + " to " + dstIp);

			// check if current entry equals last entry, if so throw it away
			if (publishUpdate) {
				for (int j = 0; j < previousEventMappingResults.size(); j++) {
					EventMappingResult tempEvent = (EventMappingResult) previousEventMappingResults
							.get(j);
					// check for same name and ip
					if (tempEvent.getName().equals(event.getName())
							& tempEvent.getIp().equals(event.getIp())) {
						if (mappingResultList.contains(tempEvent)) {
							mappingResultList.remove(tempEvent);
						} else {
							tempEvent.setPublishType(PublishType.DELETE);
							mappingResultList.add(tempEvent);
							previousEventMappingResults.remove(j);
						}
					}
				}
			}

			// generate "datastream detected" - event
			// set predefined mapping-values from config
			event.setIpType(IpType.IPV4.getTypeString());
			event.setDiscovererId(publisherId);

			// set time (and store it for date-comparison in next loop)
			event.setDiscoveredTime(Toolbox.convertTimestampToIfMapFormat(
					discTime, "-", " "));

			
			event.setSignificance(significance);
			event.setType(eventType);
			event.setConfidence(confidence);
			event.setMagnitude(magnitude);
			event.setPublishType(publishType);

			if (logger.isDebugEnabled()) {
				logger.debug("Adding Event: " + event.toString());
			}
			// add event
			mappingResultList.add(event);
			previousEventMappingResults.add(event);
		}
		return mappingResultList
				.toArray(new EventMappingResult[mappingResultList.size()]);

	}
}