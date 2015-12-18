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
package de.simu.decomap.component.polling.impl.helper;

import java.util.ArrayList;
import java.util.Map;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.parser.JSONParser;

import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.config.interfaces.polling.RestPollingConfig;
import de.simu.decomap.main.IfMapClient;
import de.simu.decomap.messaging.sender.MessageSenderException;
import de.simu.decomap.util.Toolbox;

/**
 * herlperclass for the Icinga-Rest connection
 * 
 * @version 0.2
 * @author Leonid Schwenke, DECOIT GmbH
 */
public class IcingaRestHostGetter {

	// instance
	private static IcingaRestHostGetter mInstance;

	// list of all hosts
	private ArrayList<Map<String, String>> hostlist = null;

	// REST connector
	private final IcingaDataConnectionRest connection;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * @return Singleton instance of this class
	 */
	public synchronized static IcingaRestHostGetter getInstance(
			final GeneralConfig mainConfig) {
		if (mInstance == null) {
			mInstance = new IcingaRestHostGetter(mainConfig);
		}
		return mInstance;
	}

	/**
	 * contruktor
	 * 
	 * preparing the rest connection
	 */
	private IcingaRestHostGetter(final GeneralConfig mainConfig) {
		RestPollingConfig configuration = (RestPollingConfig) Toolbox
				.loadConfig(mainConfig.pollingComponentConfigPath(),
						RestPollingConfig.class);

		connection = new IcingaDataConnectionRest(configuration.serverIP(),
				"config", configuration.filePath(), configuration.username(),
				configuration.password());
		connection.setOnlyFileData(true);
		connection.addFilter("type", "hosts");
		refreshData();
	}

	/**
	 * searching for the IP with a hostname
	 * 
	 * @param hostname
	 *            hostname which get resolved into an ip
	 * @return ip of hostname
	 */
	public String getIPforHost(final String hostname) {

		for (int i = 0; i < 2; i++) {
			if (hostlist != null && hostlist.size() > 0) {

				for (Map<String, String> host : hostlist) {
					if (host.get("host_name").equals(hostname)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Ressolved IP " + host.get("address")
									+ " for host " + hostname);
						}
						return host.get("address");
					}
				}
			} else {
				logger.error("Error at config.cgi file");
				IfMapClient.criticalError(new MessageSenderException(
						"Rest IP fo Host Error"));
			}
			if (i != 2) {
				refreshData();
			}
		}
		logger.warn("HOST: " + hostname + " NOT FOUND!!!");
		return null;
	}

	private void refreshData() {
		try {
			logger.info("Refreshing Host Data");
			String data = connection.getData();
			if (data != null) {
				JSONParser parser = new JSONParser();

				@SuppressWarnings("unchecked")
				Map<String, Map<String, ArrayList<Map<String, String>>>> hostInformations = (Map<String, Map<String, ArrayList<Map<String, String>>>>) parser
						.parse(data);
				hostlist = hostInformations.get("config").get("hosts");
			} else {
				logger.warn("Failed get request!");
			}
		} catch (Exception e) {
			logger.error("Error while tryed to get data from server over rest", e);
			IfMapClient.criticalError(e);
		}
	}

	public Config loadConfig(String path, Class<? extends Config> configClass) {
		ConfigFactory.setProperty("filename", path);
		return ConfigFactory.create(configClass);
	}

}
