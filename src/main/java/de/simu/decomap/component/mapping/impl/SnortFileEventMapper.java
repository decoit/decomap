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

import de.hshannover.f4.trust.ifmapj.metadata.EventType;
import de.simu.decomap.component.mapping.SnortEventMapper;
import de.simu.decomap.component.mapping.result.EventMappingResult;
import de.simu.decomap.enums.PublishType;
import de.simu.decomap.util.Toolbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mapping-Class for converting Results from SnortFilePollingThread to
 * IF-MAP-Results
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 * @author Leonid Schwenke, DECOIT GmbH
 */
public class SnortFileEventMapper extends SnortEventMapper {

	// predefined line-positions <-> keys of data inside passed in HashMaps
	private final String nameKey = "0";
	private final String classKey = "1";
	private final String piorityKey = "1";
	private final String discTimeKey = "2";
	private final String ipKey = "2";
	// private final String vulnaribilityUriKey = "5";// Use last entry!

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// flag detecting whether to skip ip6 addresses or not
	private final boolean mSkipIP6 = true;

	@Override
	protected EventMappingResult[] getEventMappingResult(
			final List<HashMap<String, String>> res, final String publisherId) {
		// result-list to be returned
		List<EventMappingResult> mappingResultList = new ArrayList<>();

		for (HashMap<String, String> currentMap : res) {

			// check if all required values are present in current entry from
			// passed in result-list
			if (!requiredValuesExists(currentMap)) {
				logger.debug("Not all requiered values found! Skipping Event!");
				continue;
			}

			// initialize RegEx-Matchers
			Matcher classMatcher = Toolbox.getRegExPattern("regex.class")
					.matcher(currentMap.get(classKey));
			Matcher timestampMatcher = Toolbox.getRegExPattern(
					"regex.timestamp").matcher(currentMap.get(discTimeKey));
			Matcher ipMatcher = Toolbox.getRegExPattern("regex.ip4").matcher(
					currentMap.get(ipKey));
			Matcher ip6Matcher = Toolbox.getRegExPattern("regex.ip6").matcher(
					currentMap.get(ipKey));
			Matcher typeMatcher = Toolbox.getRegExPattern("regex.type")
					.matcher(currentMap.get(nameKey));
			Matcher priorityMatcher = Toolbox.getRegExPattern("regex.priority")
					.matcher(currentMap.get(piorityKey));
			Matcher vulnaribilityMatcher = null;
			if (currentMap.get(currentMap.size() - 2 + "") != null) {
				vulnaribilityMatcher = Toolbox.getRegExPattern(
						"regex.vulnaribilityuri").matcher(
						currentMap.get(currentMap.size() - 2 + ""));
			}

			// get classification <-> event message type
			EventType msgType = eventType;
			if (classMatcher.find()) {
				msgType = getEventMappingForSignatureName(classMatcher.group(1));
			}

			// discovered time
			String discTime = null;
			if (timestampMatcher.find()) {
				discTime = Toolbox.getNowDateAsString("yyyy") + "/"
						+ timestampMatcher.group();
			}

			// check if event should be converted and send to server
			if (discTime != null && doConvert(msgType)) {
				EventMappingResult event = new EventMappingResult();

				// set event message-type
				event.setType(msgType);
				if (msgType.equals(EventType.other)) {
					System.out.println("---------------------------");
					event.setOtherTypeDef(classMatcher.group(1));
				}

				event.setPublishType(publishType);

				// set discovered time
				event.setDiscoveredTime(Toolbox.convertTimestampToIfMapFormat(
						discTime, "/", "-"));

				// set source-ip
				if (ipMatcher.find()) {
					event.setIp(ipMatcher.group());
					event.setIpType("IPv4");
				} else if (!mSkipIP6) {
					// if no IPv4-Address, check for IPv6-address (if the
					// related for using IPv6 flag is set)
					if (ip6Matcher.find()) {
						event.setIp(Toolbox
								.convertIP6AddressToIFMAPIP6AddressPattern(ip6Matcher
										.group()));
						event.setIpType("IPv6");
					}
				} else {
					logger.debug("No IP found! Skipping Event!");
					// skip entry if no source-ip was found
					continue;
				}

				// set event name
				if (typeMatcher.find()) {
					event.setName(typeMatcher.group(1));
				}

				// set event priority
				if (priorityMatcher.find()) {
					event.setSignificance(getSignificanceValue(new Integer(
							priorityMatcher.group(1)).intValue()));
				} else {
					event.setSignificance(significance);
				}

				// set vulnerabilty-url
				if (currentMap.get(currentMap.size() - 2 + "") != null
						&& vulnaribilityMatcher.find()) {
					event.setVulnerabilityUri(vulnaribilityMatcher.group(1));
				} else {
					event.setVulnerabilityUri("http://cve.mitre.org/cgi-bin/cvename.cgi?name=2005-0068");
				}

				event.setConfidence(confidence);
				event.setMagnitude(magnitude);

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

				// add current event to return-list
				if (logger.isDebugEnabled()) {
					logger.debug("Adding Event: " + event.toString());
				}
				mappingResultList.add(event);
				previousEventMappingResults.add(event);
			}
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
		if (map.get(discTimeKey) == null || map.get(ipKey) == null) {
			return false;
		}

		return true;
	}

}