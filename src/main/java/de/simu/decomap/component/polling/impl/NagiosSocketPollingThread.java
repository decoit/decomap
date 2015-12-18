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

import java.util.HashMap;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simu.decomap.component.polling.SocketPollingThread;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.util.Toolbox;

/**
 * Thread for receiving Nagios-Events over a Socket-Connection
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 */
public class NagiosSocketPollingThread extends SocketPollingThread {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init(final GeneralConfig mainConfig) {
		super.init(mainConfig);
		logger.info("Initilizing NagiosSocketPollingThread");
	}

	@Override
	protected HashMap<String, String> parseLine(final String line) {

		logger.info("Start parsing line " + line);

		/*
		 * MAKRONAME:VALUE;MAKRONAME:VALUE;MAKRONAME:VALUE;... ONE LINE!
		 * 
		 * possible makro-values for hosts: hostname, hostalias, hostaddress,
		 * hoststate, hoststatetype, hostoutput
		 * 
		 * possible makro-values for services:
		 * 
		 * servicestate, servicestatetype, serviceattempt, servicedescription
		 * serviceoutput, servicelatency, serviceduration, servicedowntime
		 * servicenotes
		 */

		// current validation rule: minimum of two chars separated by ":" and
		// ending with ";"
		if (line != null && Toolbox.getRegExPattern("regex.valid").matcher(line).find()) {

			// initialize temporary nagios-makro-list
			HashMap<String, String> makrosList = new HashMap<String, String>();

			// get "makro:value" pair as array
			Scanner scanner = new Scanner(line);
			scanner.useDelimiter(";");
			while (scanner.hasNext()) {
				String pair = scanner.next();
				String[] parsedpair = pair.split("\\=");
				makrosList.put(parsedpair[0], parsedpair[1]);
			}
			scanner.close();
			return makrosList;
		}

		else {
			logger.warn("line not valid!");
			return null;
		}
	}

}
