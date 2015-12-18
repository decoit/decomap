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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.config.interfaces.polling.SocketPollingConfig;
import de.simu.decomap.main.IfMapClient;
import de.simu.decomap.util.Toolbox;

/**
 * Abstract Base Class for polling messages over a socket
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 */
public abstract class SocketPollingThread extends PollingThread {

	// port
	protected int mPort;

	// socket
	protected ServerSocket mProviderSocket;

	// socket specific fields
	protected Socket mConnection = null;
	protected BufferedReader mBufferReader = null;
	protected DataInputStream mIn = null;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init(final GeneralConfig mainConfig) {
		logger.info("Initilizing SocketPollingThread");
		SocketPollingConfig configuration = (SocketPollingConfig) Toolbox.loadConfig(mainConfig.pollingComponentConfigPath(),
				SocketPollingConfig.class);

		// initialize socket
		mPort = configuration.serverPort();
		try {
			mProviderSocket = new ServerSocket(mPort);
		} catch (IOException e) {
			logger.error("Error on creating server socket!");
			IfMapClient.criticalError(e);
		}
	}

	@Override
	public void run() {
		while (running) {
			if (!pausing) {
				try {
					logger.info("Poll Rottation!");
					// wait for connection
					mConnection = mProviderSocket.accept();
					logger.debug("Connection accepted");

					// I/O
					mIn = new DataInputStream(mConnection.getInputStream());
					mBufferReader = new BufferedReader(new InputStreamReader(mIn));

					// read incoming line from server
					String inputLine = "";
					try {
						inputLine = mBufferReader.readLine();
						
						if (!Toolbox.isNullOrEmpty(inputLine)) {
							ArrayList<HashMap<String, String>> resultList = new ArrayList<HashMap<String, String>>();
							resultList.add(parseLine(inputLine));
							putIntoMappingQueue(resultList);
						} else {
							logger.info("No results! Not calling observer!");
							break;
						}
					} catch (IOException e) {
						logger.error("Error on receiving data over the socket!");
						IfMapClient.criticalError(e);
					}
				} catch (IOException ioException) {
					logger.error("Error on receiving data over the socket!");
					IfMapClient.criticalError(ioException);
				} finally {
					try {
						mIn.close();
					} catch (IOException e) {
						logger.error("Error on closing the socket!");
						IfMapClient.criticalError(e);
					}

				}
			}
		}
	}


	/**
	 * parse a single line
	 * 
	 * @param line
	 *            the line that needs to be parsed
	 * 
	 * @return 2d-list containing the different nagios macros
	 */
	protected abstract HashMap<String, String> parseLine(final String line);
}