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
package de.simu.decomap.component;

import javax.annotation.Nullable;




import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.simu.decomap.component.mapping.MappingInterface;
import de.simu.decomap.component.polling.PollingThread;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.config.regex.RegExHolder;
import de.simu.decomap.enums.MappingResultType;
import de.simu.decomap.main.IfMapClient;
import de.simu.decomap.messaging.pollingthread.SubscriptionPollingThread;
import de.simu.decomap.messaging.resultprocessor.PollResultProcessor;
import de.simu.decomap.messaging.sender.MessageSender;
import de.simu.decomap.messaging.sender.MessageSenderException;

/**
 * Container for holding all components that are required for reading out
 * log-data from a specific Data-Source (via PollingThread) and mapping this
 * data to the IF-MAP-Format (using MappingFactory, MappingResult)
 * 
 * Uses Google Guice to inject dependencies. Each Component and its dependencies
 * must be defined inside the component.module package
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 */
public class DataSourceComponent {

	// read-out and mapping components
	private PollingThread dateSourcePollingThread;
	private MappingInterface mappingFactory;
	private MappingResultType mappingResultType;
	private RegExHolder regExHolder;

	// messaging components
	private MessageSender messageSender;
	private SubscriptionPollingThread subscriptionPollingThread;
	private PollResultProcessor pollResultProcessor;

	// logger
	private final Logger logger = LoggerFactory.getLogger(DataSourceComponent.class.getName());

	/**
	 * Constructor, called by Google Guice Framework
	 * 
	 * Definition of each DataSourceComponent and its dependencies can be found
	 * inside the component-modules package
	 * 
	 * @param poller
	 *            thread which polls data-source for new entries
	 * @param mapper
	 *            mapping-class which converts the read-out data to IF-MAP
	 * @param type
	 *            type of mapping-result which is used to hold the converted
	 *            data
	 */
	@Inject
	public DataSourceComponent(final PollingThread dataPoller, final MappingInterface dataMapper, final MappingResultType resultType,
			final MessageSender sender, @Nullable final SubscriptionPollingThread subscriptionPoller,
			@Nullable final PollResultProcessor subscribtionResultProcessor) {

		logger.debug("Injecting Module");
		this.dateSourcePollingThread = dataPoller;
		this.mappingFactory = dataMapper;
		this.mappingResultType = resultType;

		this.messageSender = sender;
		this.subscriptionPollingThread = subscriptionPoller;
		this.pollResultProcessor = subscribtionResultProcessor;

	}

	/**
	 * initialize components from passed in Config-Object
	 * 
	 * @param mainConfig
	 *            the main Configuration-Object
	 */
	public void init(final GeneralConfig mainConfig) {
		try {
			this.dateSourcePollingThread.init(mainConfig);
			this.mappingFactory.init(mainConfig);
			this.regExHolder = new RegExHolder(mainConfig.regexComponentConfigPath());

			this.messageSender.init(mainConfig);
		} catch (MessageSenderException e) {
			logger.error("Error on initialize mainConfig", e);
			IfMapClient.criticalError(e);
		}

		// start polling-thread
		this.dateSourcePollingThread.running = true;
		this.dateSourcePollingThread.pausing = false;
	}

	/**
	 * @return the pollingThread
	 */
	public PollingThread getPollingThread() {
		return dateSourcePollingThread;
	}

	/**
	 * @return the mappingFactory
	 */
	public MappingInterface getMappingFactory() {
		return mappingFactory;
	}

	/**
	 * @return the mappingResultType
	 */
	public MappingResultType getMappingResultType() {
		return mappingResultType;
	}

	/**
	 * @return the regExHolder
	 */
	public RegExHolder getRegExHolder() {
		return regExHolder;
	}

	/**
	 * @return the messageSender
	 */
	public MessageSender getMessageSender() {
		return messageSender;
	}

	/**
	 * @return the subscriptionPollingThread
	 */
	public SubscriptionPollingThread getSubscriptionPollingThread() {
		return subscriptionPollingThread;
	}

	/**
	 * @return the pollResultProcessor
	 */
	public PollResultProcessor getPollResultProcessor() {
		return pollResultProcessor;
	}

}
