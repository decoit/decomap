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
package de.simu.decomap.messaging.resultprocessor.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hshannover.f4.trust.ifmapj.metadata.EnforcementAction;
import de.simu.decomap.component.mapping.result.EnforcementReportMappingResult;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.config.interfaces.pollresultprocessor.IptablesPollResultProcessorConfig;
import de.simu.decomap.enums.MappingResultType;
import de.simu.decomap.main.IfMapClient;
import de.simu.decomap.messaging.SearchRequestResult;
import de.simu.decomap.messaging.resultprocessor.PollResultFilter;
import de.simu.decomap.messaging.resultprocessor.PollResultProcessor;
import de.simu.decomap.messaging.resultprocessor.impl.helper.Rules;
import de.simu.decomap.messaging.resultprocessor.impl.helper.RulesExecutor;
import de.simu.decomap.messaging.sender.MessageSender;
import de.simu.decomap.messaging.sender.MessageSenderError;
import de.simu.decomap.messaging.sender.MessageSenderException;
import de.simu.decomap.util.Toolbox;

/**
 * Implementation of PollResultProcessor for IPTables
 * 
 * @author Leonid Schwenke, DECOIT GmbH
 * 
 */
public class IpTablesPollResultProcessor extends PollResultProcessor {

	private RulesExecutor executor = RulesExecutor.getInstance();
	private ArrayList<String> blockedClients = new ArrayList<String>();
	private ArrayList<String> allowedClients = new ArrayList<String>();
	private PollResultFilter[] allowResultFilter = null;
	private String startscript = null;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init(final GeneralConfig mainConfig, MessageSender sender) {
		logger.info("Initilizing IpTablesPollResultProcessor");
		IptablesPollResultProcessorConfig configuration = (IptablesPollResultProcessorConfig) Toolbox
				.loadConfig(mainConfig.pollResultFilterConfigPath(),
						IptablesPollResultProcessorConfig.class);
		Matcher ipMatcher = Toolbox.getRegExPattern("regex.ip4").matcher(
				mainConfig.mapServerUrl().replace("localhost", "127.0.0.1"));
		this.pollResultFilter = configuration.resultProcessorFilters();
		allowResultFilter = configuration.allowProcessorFilters();
		startscript = configuration.iptablesStartscript();
		this.sender = sender;

		// init rules
		initIptablesStartscript();
		allowedClients.add(mainConfig.applicationIpAddress());
		executor.executePredefinedRule(
				Rules.PREDEFINED_RULE_INSERT_INPUT_APPEND_ALLOW__IP,
				mainConfig.applicationIpAddress());
		if (ipMatcher.find()) {
			allowedClients.add(ipMatcher.group());
			executor.executePredefinedRule(
					Rules.PREDEFINED_RULE_INSERT_INPUT_APPEND_ALLOW__IP,
					ipMatcher.group());
		}
	}

	/**
	 * IPTables startscript with startup rules
	 */
	private void initIptablesStartscript() {
		Process p = null;
		try {
			logger.info("executing ip-tables-startup-script at: " + startscript);
			p = Runtime.getRuntime().exec("sh " + startscript);
			int returnCode = p.waitFor();
			logger.debug("ip-tables-startup-script return-code: " + returnCode);

			if (returnCode != 0) {
				logger.error("error while executing iptables-startup-rules-script...please check the file-path in config.properties");
				IfMapClient.criticalError(new RuntimeException(
						"IPTables startscript fail"));
			}
		} catch (Exception e) {
			logger.error("error while executing iptables-startup-rules-script...please check the file-path in config.properties");
			IfMapClient.criticalError(e);
		}

	}

	@Override
	public void processPollResult(ArrayList<SearchRequestResult> result) {
		// check both arrays
		String ip = null;
		for (SearchRequestResult currentResult : result) {
			Matcher ipMatcher = Toolbox.getRegExPattern("regex.ip4").matcher(
					currentResult.getIdentifier1().replace("localhost",
							"127.0.0.1"));
			if (ipMatcher.find()) {
				ip = ipMatcher.group();
			} else if (currentResult.getIdentifier2() != null) {
				ipMatcher = Toolbox.getRegExPattern("regex.ip4").matcher(
						currentResult.getIdentifier2().replace("localhost",
								"127.0.0.1"));
				if (ipMatcher.find()) {
					ip = ipMatcher.group();
				}
			}
			if (ip == null) {
				logger.warn("Skipping poll Result with no IP!");
				continue;
			}

			for (HashMap<String, String> currentMetadataMap : currentResult
					.getMetadata()) {

				if (pollResultFilter == null || pollResultFilter.length < 1
						|| allowResultFilter == null
						|| allowResultFilter.length < 1) {
					logger.warn("Filter is empty!");
					return;
				}

				for (int i = 0; i < pollResultFilter.length; i++) {

					// check metadata-type
					if (!currentMetadataMap.get("metadatatype").equals(
							pollResultFilter[i].getName())) {
						continue;
					}
					// check attribute
					if (currentMetadataMap.get(pollResultFilter[i]
							.getAttribute()) != null) {
						// enforcement check
						switch (pollResultFilter[i].getOperator()) {
						case CONTAINS:
							if (currentMetadataMap.get(
									pollResultFilter[i].getAttribute())
									.contains(pollResultFilter[i].getValue())) {
								filterMatch(currentMetadataMap,
										pollResultFilter[i], ip);
							}
							break;
						case MATCHES:
							if (currentMetadataMap.get(
									pollResultFilter[i].getAttribute()).equals(
									pollResultFilter[i].getValue())) {
								filterMatch(currentMetadataMap,
										pollResultFilter[i], ip);
							}
							break;
						}
					}
				}

				for (int i = 0; i < allowResultFilter.length; i++) {

					// check attibute-value
					if (currentMetadataMap.get(allowResultFilter[i]
							.getAttribute()) != null) {
						// allow check
						switch (allowResultFilter[i].getOperator()) {
						case CONTAINS:
							if (currentMetadataMap.get(
									allowResultFilter[i].getAttribute())
									.contains(allowResultFilter[i].getValue())) {
								allowClient(currentMetadataMap,
										allowResultFilter[i], ip);
							}
							break;
						case MATCHES:
							if (currentMetadataMap.get(
									allowResultFilter[i].getAttribute())
									.equals(allowResultFilter[i].getValue())) {
								allowClient(currentMetadataMap,
										allowResultFilter[i], ip);
							}
							break;
						}
					}
				}
			}
		}
	}

	@Override
	protected void filterMatch(HashMap<String, String> entry,
			PollResultFilter matchedFilter, String ip) {

		logger.info("ResultProcessor Match!");
		logger.debug("matchedResult: " + entry.toString());
		logger.debug("matchedFilter.toString()");
		try {
			if (!blockedClients.contains(ip)) {
				blockedClients.add(ip);
				allowedClients.remove(ip);
				logger.info("Blocking Client: " + ip + "!");
				EnforcementReportMappingResult[] result = new EnforcementReportMappingResult[1];
				EnforcementReportMappingResult event = new EnforcementReportMappingResult();
				event.setIp(ip);
				event.setEnforcementAction(EnforcementAction.block);
				event.setEnforcementReason(matchedFilter.getValue());
				result[0] = event;
				sender.publish(result, MappingResultType.ENFORCEMENT_REPORT);
				executor.executePredefinedRule(
						Rules.PREDEFINED_RULE_INSERT_BLOCK_IP, ip);
			}
		} catch (MessageSenderError e) {
			logger.error("MessageSenderError while publishing enforcement!");
			IfMapClient.criticalError(e);
		} catch (MessageSenderException e) {
			logger.error("MessageSenderException while publishing enforcement!");
			IfMapClient.criticalError(e);
		}
	}

	/**
	 * extra filter evaluation for allowing clients
	 * 
	 * @param entry
	 *            what to check
	 * @param matchedFilter
	 *            with what
	 * @param ip
	 *            who
	 */
	private void allowClient(HashMap<String, String> entry,
			PollResultFilter matchedFilter, String ip) {
		if (!allowedClients.contains(ip)) {
			allowedClients.add(ip);
			
			logger.info("Allowing Client: " + ip + "!");
			executor.executePredefinedRule(
					Rules.PREDEFINED_RULE_INSERT_INPUT_APPEND_ALLOW__IP, ip);
		}

	}

}
