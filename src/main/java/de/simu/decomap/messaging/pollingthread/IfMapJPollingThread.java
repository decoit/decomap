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
package de.simu.decomap.messaging.pollingthread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hshannover.f4.trust.ifmapj.channel.ARC;
import de.hshannover.f4.trust.ifmapj.messages.PollResult;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.main.IfMapClient;

/**
 * Thread on which the polling is running
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 * 
 */
public class IfMapJPollingThread extends SubscriptionPollingThread {

	public ARC arcChannel = null;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init(GeneralConfig mainConfig) {
	}

	@Override
	public void run() {
		while (running) {
			if (!pausing) {
				try {
					PollResult pr = arcChannel.poll();
					if (pr != null) {
						logger.info("new poll result! Notifing!");
						notify(pr);
					}
				} catch (Exception e) {
					logger.error("Error while polling on arcChannel!");
					IfMapClient.criticalError(e);
				}
			}
		}
	}

	@Override
	public void notify(Object o) {
		if (o != null) {
			pausing = true;
			PollResult result = (PollResult) o;
			setChanged();
			notifyObservers(result);
		}
	}

	/**
	 * 
	 * @return ArcChannel
	 */
	public ARC getArcChannel() {
		return arcChannel;
	}

	/**
	 * 
	 * @param arcChannel
	 */
	public void setArcChannel(ARC arcChannel) {
		this.arcChannel = arcChannel;
	}

}
