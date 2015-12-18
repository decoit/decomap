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
package de.simu.decomap.messaging.resultprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.config.interfaces.pollresultprocessor.PollResultProcessorConfig;
import de.simu.decomap.messaging.SearchRequestResult;
import de.simu.decomap.messaging.sender.MessageSender;
import de.simu.decomap.util.Toolbox;

/**
 * Getting results from Subscriptions and evaluate them
 * 
 * @author Leonid Schwenke, DECOIT GmbH
 * 
 */
public abstract class PollResultProcessor {

	protected PollResultFilter[] pollResultFilter = null;
	protected MessageSender sender = null;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Initialize sender and mainconfig
	 * 
	 * @param mainConfig
	 *            mainconfig to Initialize
	 * @param sende
	 *            sender to use for sending
	 */
	public void init(final GeneralConfig mainConfig, MessageSender sender) {
		logger.info("Initilizing PollResultProcessor");
		PollResultProcessorConfig configuration = (PollResultProcessorConfig) Toolbox.loadConfig(mainConfig.pollResultFilterConfigPath(),
				PollResultProcessorConfig.class);

		this.pollResultFilter = configuration.resultProcessorFilters();
		this.sender = sender;
	}

	/**
	 * evaluate results
	 * 
	 * @param result
	 *            results to evaluate
	 */
	public void processPollResult(ArrayList<SearchRequestResult> result) {
		String ip = null;
		for (SearchRequestResult currentResult : result) {
			Matcher ipMatcher = Toolbox.getRegExPattern("regex.ip4").matcher(
					currentResult.getIdentifier1().replace("localhost", "127.0.0.1"));
			if (ipMatcher.find()) {
				ip = ipMatcher.group();
			} else if (currentResult.getIdentifier2() != null) {
				ipMatcher = Toolbox.getRegExPattern("regex.ip4").matcher(currentResult.getIdentifier2().replace("localhost", "127.0.0.1"));
				if (ipMatcher.find()) {
					ip = ipMatcher.group();
				}
			}
			if (ip == null) {
				logger.warn("Skipping poll Result with no IP!");
				continue;
			}
			for (HashMap<String, String> currentMetadataMap : currentResult.getMetadata()) {

				if (pollResultFilter == null || pollResultFilter.length < 1) {
					logger.warn("Filter empty!");
					return;
				}

				for (int i = 0; i < pollResultFilter.length; i++) {
					// check metadata-type
					if (!currentMetadataMap.get("metadatatype").equals(pollResultFilter[i].getName())) {
						continue;
					}
					// check attribute
					if (currentMetadataMap.get(pollResultFilter[i].getAttribute()) != null) {
						// enforcement check
						switch (pollResultFilter[i].getOperator()) {
						case CONTAINS:
							System.out.println(currentMetadataMap.get(pollResultFilter[i].getAttribute()));
							if (currentMetadataMap.get(pollResultFilter[i].getAttribute()).contains(pollResultFilter[i].getValue())) {
								filterMatch(currentMetadataMap, pollResultFilter[i], ip);
							}
							break;
						case MATCHES:
							if (currentMetadataMap.get(pollResultFilter[i].getAttribute()).equals(pollResultFilter[i].getValue())) {
								filterMatch(currentMetadataMap, pollResultFilter[i], ip);
							}
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * Evaluate based on this filter
	 * 
	 * @param entry
	 *            what to filter
	 * @param matchedFilter
	 *            filter with
	 * @param ip
	 *            who
	 */
	protected abstract void filterMatch(HashMap<String, String> entry, PollResultFilter matchedFilter, String ip);

}
