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
package de.simu.decomap.messaging.sender;

import java.util.ArrayList;
import java.util.Observer;

import de.hshannover.f4.trust.ifmapj.messages.SearchResult;
import de.simu.decomap.component.mapping.result.MappingResult;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.enums.MappingResultType;
import de.simu.decomap.messaging.SearchRequestParams;
import de.simu.decomap.messaging.SearchRequestResult;
import de.simu.decomap.messaging.Subscription;
import de.simu.decomap.messaging.pollingthread.SubscriptionPollingThread;
import de.simu.decomap.messaging.resultprocessor.PollResultProcessor;

/**
 * Interface for a sender that gone be used by the MessagingFacade to
 * communicate with the server
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 * 
 */
public interface MessageSender extends Observer {

	/**
	 * Initialize configuration
	 * 
	 * @param cfg
	 *            Initialize from this config
	 * @throws MessageSenderException
	 */
	void init(GeneralConfig cfg) throws MessageSenderException;

	/**
	 * start a session
	 */
	void startSession();

	/**
	 * Initialize the ArcPolling-Thread
	 */
	void initArcPollingThread();

	/**
	 * start the ArcPolling-Thread
	 */
	void startArcPollingThread();

	/**
	 * set a polling-thread, from where the data is coming
	 * 
	 * @param pollingThread
	 *            polling thread to set
	 */
	void setPollingThread(SubscriptionPollingThread pollingThread);

	/**
	 * PollResultProcessor which evaluate the subscription results
	 * 
	 * @param resultProcessor
	 *            resultProcessor to use
	 */
	void setPollResultProcessor(PollResultProcessor resultProcessor);

	/**
	 * Sending a subscription
	 * 
	 * @param subscription
	 *            subscription to send
	 */
	void sendSubscription(Subscription subscription);

	/**
	 * end the session
	 */
	void endSession();

	/**
	 * purge a publisher
	 */
	void purgePublisher();

	/**
	 * publish event
	 * 
	 * @param result
	 *            result to publish
	 * @param resultType
	 *            type of result
	 * @throws MessageSenderError
	 * @throws MessageSenderException
	 */
	void publish(MappingResult[] result, MappingResultType resultType) throws MessageSenderError, MessageSenderException;

	/**
	 * publish search-request
	 * 
	 * @param request
	 *            search-request to publish
	 * @return result form request
	 */
	ArrayList<SearchRequestResult> publishSearchRequest(SearchRequestParams request);

	/**
	 * get last polling result
	 * 
	 * @return last polling result
	 */
	ArrayList<SearchRequestResult> getLastPollResult();

	/**
	 * 
	 * @param searchResult
	 * @return
	 */
	ArrayList<SearchRequestResult> transformPollResult(SearchResult searchResult);

	/**
	 * 
	 * @return IfMap publisher ID
	 */
	String getIfMapPublisherId();

}
