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
package de.simu.decomap.component.polling;

import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simu.decomap.component.polling.impl.helper.IcingaDataConnectionRest;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.config.interfaces.polling.RestPollingConfig;
import de.simu.decomap.main.IfMapClient;
import de.simu.decomap.util.Toolbox;

/**
 * abstract base class for polling messages over rest
 * 
 * @version 0.2
 * @author Leonid Schwenke, DECOIT GmbH
 */
public abstract class RestPollingThread extends PollingThread {

	// thread sleeping time between two polls
	public int sleepTime = 0;

	// flag for indicating that reading of file is necessary, even if there was
	// no update
	protected boolean isFirstStart = false;

	// id of last logentry
	protected String lastEntry = "0";

	// time of last request
	private String time;

	// rest connector
	private IcingaDataConnectionRest connection;

	// nummber of logs loading
	private String lognumber = "50";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void run() {
		while (running) {
			if (!pausing) {
				try {
					logger.info("Poll Rottation!");
					// setting filters
					connection.resetFilters();
					connection.addFilter("limit", lognumber);
					connection.addFilter("ts_start", lastEntry);
					time = System.currentTimeMillis() + "";
					connection.addFilter("ts_end",
							time.substring(0, time.length() - 3));
					connection
							.addFilter(
									"noti=off&hst=on&sst=on&cmd=off&sms=off&evh=off&flp=off&dwn",
									"off");

					// get, format and then send data
					String data = connection.getData();
					if (data != null) {
						putIntoMappingQueue(parseJson(data));
						Thread.sleep(sleepTime);
					} else{
						logger.warn("Failed get request!");
					}
				} catch (Exception e) {
					logger.error("error while getting REST Data!", e);
					IfMapClient.criticalError(e);
				}
			}
		}
	}

	@Override
	public void init(final GeneralConfig mainConfig) {
		logger.info("Initilizing RestPollingThread");
		// flag indicating first start
		this.isFirstStart = true;

		// interval between poll-requests
		this.sleepTime = mainConfig.applicationPollingInterval() * 1000;

		RestPollingConfig configuration = (RestPollingConfig) Toolbox
				.loadConfig(mainConfig.pollingComponentConfigPath(),
						RestPollingConfig.class);

		lognumber = configuration.logNumber();
		connection = new IcingaDataConnectionRest(configuration.serverIP(),
				"showlog", configuration.filePath(), configuration.username(),
				configuration.password());
		connection.setOnlyFileData(true);
	}

	/**
	 * formating the data got from the rest connection
	 * 
	 * @param lines
	 *            data to parse
	 * 
	 * @return formated data
	 */
	@SuppressWarnings("rawtypes")
	protected abstract ArrayList<Map> parseJson(final String lines);
}
