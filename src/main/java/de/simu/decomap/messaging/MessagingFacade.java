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
package de.simu.decomap.messaging;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.simu.decomap.component.mapping.result.MappingResult;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.enums.MappingResultType;
import de.simu.decomap.main.IfMapClient;
import de.simu.decomap.messaging.pollingthread.SubscriptionPollingThread;
import de.simu.decomap.messaging.resultprocessor.PollResultProcessor;
import de.simu.decomap.messaging.sender.MessageSender;
import de.simu.decomap.messaging.sender.MessageSenderError;
import de.simu.decomap.messaging.sender.MessageSenderException;

/**
 * Class from where the Massaging is handled. The MessagingFacade can use
 * different Messaging implementation
 * 
 * @author Leonid Schwenke, DECOIT GmbH
 * 
 */
@Singleton
public class MessagingFacade {

	private MessageSender sender;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Inject
	public MessagingFacade(MessageSender sender, SubscriptionPollingThread pollingThread, PollResultProcessor resultProcessor) {
		this.sender = sender;

		if (pollingThread != null && resultProcessor != null) {
			sender.setPollingThread(pollingThread);
			sender.setPollResultProcessor(resultProcessor);
		}
	}

	/**
	 * Initialize config
	 * 
	 * @param cfg
	 *            config to initialize
	 */
	public void init(GeneralConfig cfg) {
		logger.info("Initilizing MessagingFacade");
		// this.mapServerUrl = cfg.mapServerUrl();
		try {
			this.sender.init(cfg);
		} catch (MessageSenderException e) {
			logger.error("MessageSenderException while initialize config!");
			IfMapClient.criticalError(e);
		}

	}

	/**
	 * send a new-session request to map-server
	 */
	public void sendNewSessionRequest() throws MessageSenderException, MessageSenderError {
		sender.startSession();
	}

	/**
	 * send a purge-publish request to map-server
	 */
	public void sendPurgePublishRequest() {
		sender.purgePublisher();
	}

	/**
	 * send a end-session request to map-server
	 */
	public void sendEndSessionRequest() {
		sender.endSession();
	}

	/**
	 * Sending a search request
	 * 
	 * @param params
	 *            searchRequest
	 * @return results from searchRequest
	 */
	public ArrayList<SearchRequestResult> sendSearchRequest(SearchRequestParams params) {
		return sender.publishSearchRequest(params);
	}

	/**
	 * Initialize the ArcPolling-Thread
	 */
	public void initArcPollingThread() {
		sender.initArcPollingThread();
	}

	/**
	 * Starting the ArcPolling-Thread
	 */
	public void startArcPollingThread() {
		sender.startArcPollingThread();
	}

	/**
	 * Send a subscription to the Server
	 * 
	 * @param subscription
	 *            subscription to send
	 */
	public void sendSubscription(Subscription subscription) {
		sender.sendSubscription(subscription);
	}

	/**
	 * get the last poll-result
	 * 
	 * @return Last poll-result
	 */
	public ArrayList<SearchRequestResult> getLastPollResult() {
		return sender.getLastPollResult();
	}

	/**
	 * Publish onto the server
	 * 
	 * @param resultList
	 *            results to publish
	 * @param resultType
	 *            type of publish
	 */
	public void sendPublish(MappingResult[] resultList, MappingResultType resultType) {
		try {
			sender.publish(resultList, resultType);
		} catch (MessageSenderError e) {
			logger.error("MessageSenderError while publishing!");
			IfMapClient.criticalError(e);
		} catch (MessageSenderException e) {
			logger.error("MessageSenderException while publishing!");
			IfMapClient.criticalError(e);
		}
	}

	/**
	 * 
	 * @return IfMap publisher ID
	 */
	public String getIfMapPublisherId() {
		return this.sender.getIfMapPublisherId();
	}
}
