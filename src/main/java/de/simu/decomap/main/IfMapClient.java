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
package de.simu.decomap.main;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.log4j.PropertyConfigurator;

import de.simu.decomap.component.DataSourceComponent;
import de.simu.decomap.component.garbagecollector.GarbageCollectorThread;
import de.simu.decomap.component.mapping.result.MappingResult;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.enums.Component;
import de.simu.decomap.messaging.MessagingFacade;
import de.simu.decomap.messaging.Subscription;
import de.simu.decomap.messaging.sender.MessageSenderError;
import de.simu.decomap.messaging.sender.MessageSenderException;
import de.simu.decomap.util.Toolbox;

/**
 * Main-Class
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 * @author Leonid Schwenke, DECOIT GmbH
 */
public class IfMapClient {

	private DataSourceComponent dataSourceComponent;
	private MessagingFacade messagingFacade;
	private GeneralConfig mainConfig;

	private Component component;

	public static BlockingQueue<Object> mappingQueue = new LinkedBlockingQueue<Object>();

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Constructor Initialize the configuration and needed components
	 * 
	 * @param args
	 */
	public IfMapClient(String[] args) {
		PropertyConfigurator.configureAndWatch("config/logging.properties");
		logger.info("Application Start");

		initConfigurations();
		initRegExes();
		initComponents();

		logger.info("...everything seems to be ready...");

	}

	/**
	 * Initialize the main configuration
	 */
	private void initConfigurations() {
		logger.info("Initialize Configuration");

		mainConfig = (GeneralConfig) loadConfig("config.properties",
				GeneralConfig.class);
	}

	/**
	 * Initialize the main components
	 */
	private void initComponents() {
		logger.info("Initialize Componentes");

		component = Component.valueOf(mainConfig.applicationComponent()
				.toUpperCase());

		dataSourceComponent = component.getDataSourceComponent();

		messagingFacade = new MessagingFacade(
				dataSourceComponent.getMessageSender(),
				dataSourceComponent.getSubscriptionPollingThread(),
				dataSourceComponent.getPollResultProcessor());

		dataSourceComponent.init(mainConfig);
		// dataSourceComponent.getPollingThread().addObserver(this);

	}

	/**
	 * Initialize needed regexes
	 */
	private void initRegExes() {
		Toolbox.loadAndPrepareRegExFromFile(mainConfig
				.regexComponentConfigPath());
	}

	/**
	 * load the configuration injected into a class
	 * 
	 * @param path
	 *            Path to the configuration
	 * @param configClass
	 *            Injecting configuration into this class
	 * @return Configurationclass
	 */
	private Config loadConfig(String path, Class<? extends Config> configClass) {
		ConfigFactory.setProperty("filename", path);
		return ConfigFactory.create(configClass);
	}

	/**
	 * Starting the client. Starting the connection over the messagingFacade.
	 * Everything starts from here.
	 * 
	 */
	private void start() {
		// open new connection
		try {
			logger.info("Client Start!");

			messagingFacade.sendNewSessionRequest();
			messagingFacade.sendPurgePublishRequest();

			if (dataSourceComponent.getPollingThread() != null) {
				messagingFacade.initArcPollingThread();
				messagingFacade.startArcPollingThread();
			}

		} catch (MessageSenderException e) {
			logger.error("MessageSenderException!");
			criticalError(e);
		} catch (MessageSenderError e) {
			logger.error("MessageSenderError!");
			criticalError(e);
		}

		new Thread(dataSourceComponent.getPollingThread()).start();
		GarbageCollectorThread collector = new GarbageCollectorThread(messagingFacade,
				dataSourceComponent.getMappingResultType(), mainConfig);
		new Thread(collector).start();
		workOffQueue();
	}

	// @Override
	// public synchronized void update(final Observable o, final Object arg) {
	// if (o != null) {
	// logger.debug("New Update!");
	// mappingQueue.add(arg);
	//
	// if()
	//
	// }
	//
	// //dataSourceComponent.getPollingThread().pausing = false;
	//
	// }

	/**
	 * Working off the Queue, which contains results from the pollingthreads
	 */
	private void workOffQueue() {
		while (true) {
			logger.info("Cheking Queue!");
			try {
				// check? was anderes?
				@SuppressWarnings("unchecked")
				List<HashMap<String, String>> tmpResultList = (List<HashMap<String, String>>) mappingQueue
						.take();
				logger.info("Received new element from pollingthread!");
				if (tmpResultList != null) {
					MappingResult[] resultList = dataSourceComponent
							.getMappingFactory().getMappingResult(
									tmpResultList,
									messagingFacade.getIfMapPublisherId());
					if (resultList != null && resultList.length > 0) {
						logger.info("Send Publish");
						messagingFacade.sendPublish(resultList,
								dataSourceComponent.getMappingResultType());

						if (dataSourceComponent.getSubscriptionPollingThread() != null) {
							// wenn neuer Client erkannt, subskription senden
							for (MappingResult current : resultList) {
								if (current.getClientIpAddress() != null) {
									logger.info("Sending subscription with IP: "
											+ current.getClientIpAddress());
									Subscription sub = new Subscription(
											current.getClientIpAddress());
									messagingFacade.sendSubscription(sub);
								}
							}
						} else {
							logger.info("NO SUBSCRIPTION POLLINGTHREAD FOUND.....NOT SUBSCRIBING ANY CLIENTS");
						}

					} else {
						logger.info("resultList is empty or null!");
					}
				} else {
					logger.info("tmpresult is null!");
				}
			} catch (Exception e) {
				logger.error("Error while working with the mapping queue! ", e);
			}
		}
	}

	/**
	 * Critical Error handling
	 * 
	 * @param e
	 *            Throwable error
	 */
	public static void criticalError(Throwable e) {
		Logger logger = LoggerFactory.getLogger("de.Main");
		logger.error("Critical Error! Shutting down application!");
		logger.error(e.getMessage(), e);
		System.exit(1);
	}

	/**
	 * main
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		IfMapClient client = new IfMapClient(args);

		client.start();
	}

}
