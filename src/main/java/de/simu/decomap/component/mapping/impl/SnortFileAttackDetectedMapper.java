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
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simu.decomap.component.mapping.MappingInterface;
import de.simu.decomap.component.mapping.result.AttackDetectedEventMappingResult;
import de.simu.decomap.component.mapping.result.MappingResult;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.config.interfaces.mapping.SimpleEventMappingConfig;
import de.simu.decomap.enums.PublishType;
import de.simu.decomap.util.Toolbox;

/**
 * Mapping-Class for converting Results from SnortFilePollingThread to
 * IF-MAP-Results
 * 
 * @author Leonid Schwenke, DECOIT GmbH
 */
public class SnortFileAttackDetectedMapper implements MappingInterface {

	private final String piorityKey = "1";
	private final String discTimeKey = "2";
	private final String ipKey = "2";
	// private final String vulnaribilityUriKey = "5";
	// TODO: ALles in eine Zeile.... RegEx vom Ref verbessern!!!!???? Das wäre
	// aber ggf zu aufwendig bei so vielen Daten???

	private boolean sendDiscoveredBy = false;

	private final ArrayList<String> discoveredIps = new ArrayList<String>();

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init(GeneralConfig mainConfig) {
		logger.info("Initilizing SnortFileAttackDetectedMapper");

		SimpleEventMappingConfig eventMappingConfiguration = (SimpleEventMappingConfig) Toolbox
				.loadConfig(mainConfig.mappingComponentConfigPath(),
						SimpleEventMappingConfig.class);
		sendDiscoveredBy = eventMappingConfiguration.sendDiscoveredBy();
	}

	@Override
	public MappingResult[] getMappingResult(
			List<HashMap<String, String>> tmpResultList, String publisherId) {
		if (logger.isDebugEnabled()) {
			logger.debug("Start mapping!");
		}

		// result-list to be returned
		List<MappingResult> mappingResultList = new ArrayList<>();

		for (HashMap<String, String> currentMap : tmpResultList) {

			if (logger.isDebugEnabled()) {
				logger.debug("Mapping next map!");
			}

			float severity = -1;
			String discTime = null;
			String attackerIp = null;
			String attackerIpType = null;
			String serviceIp = null;
			String servicePort = null;
			String serviceIpType = null;

			HashSet<AttackDetectedEventMappingResult> cveEvents = new HashSet<AttackDetectedEventMappingResult>();

			Matcher vulnaribilityMatcher = null;
			vulnaribilityMatcher = Toolbox.getRegExPattern(
					"regex.vulnaribilityuri").matcher(
					currentMap.get(currentMap.size() - 2 + ""));

			if (logger.isDebugEnabled()) {
				logger.debug("Checking entry for CVE vulnerabilities!");
			}

			// Find all CVE vulnerabilities!
			Matcher cveMatcher = null;
			while (vulnaribilityMatcher.find()) {

				AttackDetectedEventMappingResult result = new AttackDetectedEventMappingResult();

				String idLink = vulnaribilityMatcher.group(1);

				if (logger.isDebugEnabled()) {
					logger.debug("Vulnaribility found!: " + idLink);
				}

				cveMatcher = Toolbox.getRegExPattern("regex.cveid").matcher(
						idLink);

				// CVE Check
				// TODO: bessere erkennung ob cve
				if (idLink.toLowerCase().contains("cve") && cveMatcher.find()) {
					result.setId(cveMatcher.group(1));
					result.setType("CVE");
					if (logger.isDebugEnabled()) {
						logger.debug("Vulnaribility is a CVE: "
								+ result.getId());
					}
					cveEvents.add(result);
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("Vulnaribility is not a CVE!");
					}
					continue;
				}
			}

			if (cveEvents.isEmpty()) {
				if (logger.isDebugEnabled()) {
					logger.debug("No CVE found! Skipping entry!");
				}
				continue;
			}
			
			Matcher timestampMatcher = Toolbox.getRegExPattern(
					"regex.timestamp").matcher(currentMap.get(discTimeKey));
			Matcher ipMatcher = Toolbox.getRegExPattern("regex.ip4port")
					.matcher(currentMap.get(ipKey));
			Matcher ip6Matcher = Toolbox.getRegExPattern("regex.ip6port")
					.matcher(currentMap.get(ipKey));
			Matcher priorityMatcher = Toolbox.getRegExPattern("regex.priority")
					.matcher(currentMap.get(piorityKey));

			// set priority
			if (priorityMatcher.find()) {
				severity = new Float(priorityMatcher.group(1)).floatValue();
				if (logger.isDebugEnabled()) {
					logger.debug("severity: " + severity);
				}
			} else {
				logger.info("skipping entry becuse of missing priority");
				continue;
			}

			// discovered time
			if (timestampMatcher.find()) {
				discTime = Toolbox.getNowDateAsString("yyyy") + "/"
						+ timestampMatcher.group();
				if (logger.isDebugEnabled()) {
					logger.debug("discovered time: " + discTime);
				}
			} else {
				logger.info("skipping entry because of missing timestamp");
				continue;
			}

			// AttackerIP
			if (ipMatcher.find()) {
				attackerIp = ipMatcher.group(1);
				attackerIpType = "IPv4";
				if (logger.isDebugEnabled()) {
					logger.debug("ipv4 attacker ip: " + attackerIp);
				}
			} else if (ip6Matcher.find()) {
				attackerIp = Toolbox
						.convertIP6AddressToIFMAPIP6AddressPattern(ip6Matcher
								.group(1));
				attackerIpType = "IPv6";
				if (logger.isDebugEnabled()) {
					logger.debug("ipv6 attacker ip: " + attackerIp);
				}
			} else {
				logger.info("skipping entry because of missing attacker ip");
				continue;
			}

			// ServiceIP
			String[] service;
			if (ipMatcher.find()) {
				serviceIp = ipMatcher.group(1);
				servicePort = ipMatcher.group(8);
				serviceIpType = "IPv4";
				if (logger.isDebugEnabled()) {
					logger.debug("ipv4 service ip: " + serviceIp);
				}
			} else if (ip6Matcher.find()) {
				service = Toolbox.convertIP6AddressToIFMAPIP6AddressPattern(
						ip6Matcher.group()).split(":", 2);
				serviceIp = service[0];
				servicePort = service[1];
				serviceIpType = "IPv6";
				if (logger.isDebugEnabled()) {
					logger.debug("ipv6 service ip: " + serviceIp);
				}
			} else {
				logger.info("skipping entry because of missing service ip");
				continue;
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Start building results!");
			}
			for (AttackDetectedEventMappingResult result : cveEvents) {

				result.setDiscoveredTime(Toolbox.convertTimestampToIfMapFormat(
						discTime, "/", "-"));

				result.setSeverity(severity);

				result.setAttackerIp(attackerIp);
				result.setAttackerIpType(attackerIpType);

				result.setServiceIp(serviceIp);
				result.setServiceIpType(serviceIpType);
				result.setServicePort(servicePort);

				result.setPublishType(PublishType.UPDATE);

				if (sendDiscoveredBy
						&& !discoveredIps.contains(result.getAttackerIp())) {
					discoveredIps.add(result.getAttackerIp());
					result.setSendDiscoveredBy(true);
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Adding result to resultlist! \n"
							+ result.toString());
				}
				mappingResultList.add(result);

			}
		}

		if (logger.isDebugEnabled() && mappingResultList.isEmpty()) {
			logger.debug("Result list is empty!");
		}
		return mappingResultList.toArray(new MappingResult[mappingResultList
				.size()]);
	}

}
