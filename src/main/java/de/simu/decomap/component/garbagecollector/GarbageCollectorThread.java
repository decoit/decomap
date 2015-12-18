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
package de.simu.decomap.component.garbagecollector;

import java.util.Calendar;
import java.util.PriorityQueue;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simu.decomap.component.mapping.result.BasicSimuMappingResult;
import de.simu.decomap.component.mapping.result.MappingResult;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.enums.MappingResultType;
import de.simu.decomap.enums.PublishType;
import de.simu.decomap.messaging.MessagingFacade;
import de.simu.decomap.util.Toolbox;

/**
 * Thread can store BasicSimuMappingResult into a priority queue. The
 * DiscoveredTime from the BasicSimuMappingResult should be set to the preferred
 * delete time and should be the compare condition. The thread checks
 * periodically (equals the application.polling.interval from the config) if the
 * actual time exceed the delete time. If this is the case the thread is going
 * to delete this event from the map-server.
 * 
 * @author Leonid Schwenke, DECOIT GmbH
 * 
 */
public class GarbageCollectorThread implements Runnable {

	private int checkTimeIntervall = 20000;

	private BasicSimuMappingResult oldestResult = null;

	private boolean running = false;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final MappingResultType mappingType;

	private final MessagingFacade messagingFacade;

	public static Queue<BasicSimuMappingResult> garbageQueue = new PriorityQueue<BasicSimuMappingResult>();

	/**
	 * Constructor
	 * 
	 * @param messagingFacade MassagingFacade which the GarbageCollectorThread can use to send the delete
	 * @param mappingType Typ of the event, so the MassageSender know how to send the event
	 * @param generalConfig a GeneralConfig for the confuguration
	 */
	public GarbageCollectorThread(final MessagingFacade messagingFacade,
			final MappingResultType mappingType, GeneralConfig generalConfig) {
		logger.info("Starting garbagecollector!");
		this.messagingFacade = messagingFacade;
		this.mappingType = mappingType;

		checkTimeIntervall = generalConfig.applicationPollingInterval() * 1000;
	}

	@Override
	public void run() {
		running = true;
		MappingResult[] mappingResult;
		try {
			while (running) {
				logger.info("Garbage routin!");
				oldestResult = garbageQueue.peek();
				if (oldestResult == null) {
					logger.info("No new Garbage! Sleeping!");
					Thread.sleep(checkTimeIntervall);
				} else {

					Calendar eventTime = null;
					eventTime = oldestResult.getDiscoveredTime();

					if ((Toolbox.getNowDate(Toolbox.dateFormatString).getTime() > eventTime
							.getTimeInMillis())) {
						logger.info("New Garbage found! Deleting!");
						garbageQueue.poll();
						mappingResult = new MappingResult[1];
						mappingResult[0] = oldestResult;
						oldestResult.setPublishType(PublishType.DELETE);
						messagingFacade.sendPublish(mappingResult, mappingType);
					} else {
						logger.info("Garbage not old enought! Sleeping!");
						Thread.sleep(checkTimeIntervall);
					}
				}
			}
		} catch (InterruptedException e) {
			logger.error(
					"Garbagecollector failed! Tell my wife i love her! Garbagecollector out x.x ",
					e);
		}
	}
}
