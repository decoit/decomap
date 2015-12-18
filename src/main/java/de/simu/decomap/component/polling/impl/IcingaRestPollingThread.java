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
package de.simu.decomap.component.polling.impl;

import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.parser.JSONParser;

import de.simu.decomap.component.polling.RestPollingThread;
import de.simu.decomap.config.interfaces.GeneralConfig;

/**
 * Thread for polling new icinga-events over REST
 * 
 * @author Leonid Schwenke, DDECOIT GmbH
 */
public class IcingaRestPollingThread extends RestPollingThread {

	// Json paser
	JSONParser parser = new JSONParser();

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * constructor
	 * 
	 * @param props
	 *            properties-object containing values for initialization
	 */
	public IcingaRestPollingThread() {
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected ArrayList<Map> parseJson(String lines) {
		try {
			// Map<String, ArrayList<Map>> data = (Map<String, ArrayList<Map>>)
			// parser
			// .parse(lines);
			Map<String, Map<String, ArrayList<Map>>> data = (Map<String, Map<String, ArrayList<Map>>>) parser
					.parse(lines);
			ArrayList<Map> list = data.get("showlog").get("log_entries");

			if (list != null && list.size() > 0) {
				super.lastEntry = (Long) list.get(0)
						.get("timestamp")
						+ 1 + "";
				return list;
			}
		} catch (Exception e) {
			logger.error("error while building Results from Json", e);
		}
		return null;
	}

	@Override
	public void init(GeneralConfig mainConfig) {
		super.init(mainConfig);
	}

}
