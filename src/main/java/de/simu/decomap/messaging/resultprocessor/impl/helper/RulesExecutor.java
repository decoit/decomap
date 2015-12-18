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
package de.simu.decomap.messaging.resultprocessor.impl.helper;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Singleton for executing ip-tables commands using the apache-commons-exec
 * library
 * 
 * @version 0.1.4
 * @author Dennis Dunekacke, DECOIT GmbH
 */
public class RulesExecutor {

	// predefined exit code
	public static final int EXIT_CODE_SUCCESS = 0;
	public static final int EXIT_CODE_ERROR = 1;

	// Singleton
	private static RulesExecutor sInstance;

	// ip-tables command
	public final String command = "/sbin/iptables";

	// timeout for execution
	private int mTimeout = 60000;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private RulesExecutor() {
	}

	public synchronized static RulesExecutor getInstance() {
		if (sInstance == null) {
			sInstance = new RulesExecutor();
		}
		return sInstance;
	}

	/**
	 * Executing a predefined Rule
	 * @param ruleType RuleType
	 * @param arg args for Rule
	 * @return Success
	 */
	public boolean executePredefinedRule(byte ruleType, String arg) {
		logger.info("[IPTABLES] -> executing predefined command...");

		// use apache's commons-exec for executing command!
		CommandLine cmdLine = new CommandLine(command);

		// get the predefined rule-parameters
		String[] ruleParams = Rules.getPredefindedRuleParameters(ruleType, arg);

		if (ruleParams != null) {

			// add rule-parameters to CommanLine-Object
			for (int i = 0; i < ruleParams.length; i++) {
				cmdLine.addArgument(ruleParams[i]);
			}

			// execute command
			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
			ExecuteWatchdog watchdog = new ExecuteWatchdog(mTimeout);
			Executor executor = new DefaultExecutor();
			executor.setExitValue(1);
			executor.setWatchdog(watchdog);
			try {
				executor.execute(cmdLine, resultHandler);
			} catch (ExecuteException e) {
				logger.warn("[IPTABLES] -> error while executing predefined command: execute-exception occured!");
				return false;
			} catch (IOException e) {
				logger.warn("[IPTABLES] -> error while executing predefined command: io-exception occured!");
				return false;
			}

			try {
				// some time later the result handler callback was invoked so we
				// can safely request the exit value
				resultHandler.waitFor();
				int exitCode = resultHandler.getExitValue();
				logger.info("[IPTABLES] -> command " + ruleType
						+ " executed, exit-code is: " + exitCode);

				switch (exitCode) {
				case EXIT_CODE_SUCCESS:
					return true;
				case EXIT_CODE_ERROR:
					return false;
				default:
					return false;
				}

			} catch (InterruptedException e) {
				logger.warn("[IPTABLES] -> error while executing predefined command: interrupted-exception occured!");
				return false;
			}

		} else {
			logger.warn("[IPTABLES] -> error while excuting predefined command: rule-parameters-list is null!");
			return false;
		}
	}
}