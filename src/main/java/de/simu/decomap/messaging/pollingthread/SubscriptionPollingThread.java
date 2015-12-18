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

import java.util.Observable;

import de.simu.decomap.config.interfaces.GeneralConfig;

/**
 * Abstract Base-Class for PollingThread-Classes
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 */
public abstract class SubscriptionPollingThread extends Observable implements Runnable {

	// flag indicating if polling thread is currently running
	public boolean running = false;

	// flag indicating if polling thread is currently pausing
	public boolean pausing = false;

	/**
	 * abstract method for initializing properties before the thread is executed
	 * 
	 * @param props
	 *            properties-object containing values for initialization
	 */
	public abstract void init(final GeneralConfig mainConfig);

	/**
	 * abstract method for notifying observers about occurred updates
	 * 
	 * @param Object
	 *            read in data as object
	 */
	protected abstract void notify(final Object o);

}
