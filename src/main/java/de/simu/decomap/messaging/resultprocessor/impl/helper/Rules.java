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

/**
 * Class containing different statics Strings for executing IPTables-Rules and
 * methods for returning a string- array containing IPTables-Parameters for a
 * predefined Rule
 * 
 * @version 0.1.4
 * @author Dennis Dunekacke, DECOIT Gmbh
 */
public class Rules {

	public static final byte PREDEFINED_RULE_INSERT_BLOCK_IP = 0;
	public static final byte PREDEFINED_RULE_DELETE_BLOCK_IP = 1;
	public static final byte PREDEFINED_RULE_INSERT_INPUT_APPEND_ALLOW__IP = 2;
	public static final byte PREDEFINED_RULE_INSERT_FORWARD_APPEND_ALLOW__IP = 3;
	public static final byte PREDEFINED_RULE_DELETE_APPEND_ALLOW_IP_INPUT = 4;
	public static final byte PREDEFINED_RULE_FLUSH = 5;
	public static final byte PREDEFINED_RULE_POLICY_DROP = 6;
	public static final byte PREDEFINED_RULE_INSERT_ULOG = 7;
	public static final byte PREDEFINED_RULE_FORWARD_ULOG = 8;
	public static final byte PREDEFINED_RULE_DELETE_APPEND_ALLOW_IP_FORWARD = 9;

	// chains
	public static final String CHAIN_INPUT = "INPUT";
	public static final String CHAIN_OUTPUT = "OUTPUT";
	public static final String CHAIN_FOWARD = "FORWARD";

	// actions
	public static final String ACTION_ACCEPT = "ACCEPT";
	public static final String ACTION_DROP = "DROP";
	public static final String ACTION_REJECT = "REJECT";
	public static final String ACTION_LOG = "LOG";
	public static final String ACTION_ULOG = "ULOG";

	// commands
	public static final String COMMAND_INSERT = "-I";
	public static final String COMMAND_DELETE = "-D";
	public static final String COMMAND_APPEND = "-A";
	public static final String COMMAND_POLICY = "-P";

	/**
	 * get predefined rules-parameters determined by passed in rule-type using
	 * the passed in arguments
	 * 
	 * @param ruleType
	 * @param args
	 * @return
	 */
	public static String[] getPredefindedRuleParameters(byte ruleType, String args) {
		if (args == null) {
			System.out.println("wrong parameter count!");
			return null;
		}
		switch (ruleType) {
		case PREDEFINED_RULE_INSERT_BLOCK_IP:
			return new String[] { COMMAND_INSERT, CHAIN_INPUT, "-s", args, "-j", ACTION_DROP };

		case PREDEFINED_RULE_DELETE_BLOCK_IP:
			return new String[] { COMMAND_DELETE, CHAIN_INPUT, "-s", args, "-j", ACTION_DROP };

		case PREDEFINED_RULE_INSERT_INPUT_APPEND_ALLOW__IP:
			return new String[] { COMMAND_APPEND, CHAIN_INPUT, "-s", args, "-j", ACTION_ACCEPT };

		case PREDEFINED_RULE_DELETE_APPEND_ALLOW_IP_INPUT:
			return new String[] { COMMAND_DELETE, CHAIN_INPUT, "-s", args, "-j", ACTION_ACCEPT };

		case PREDEFINED_RULE_DELETE_APPEND_ALLOW_IP_FORWARD:
			return new String[] { COMMAND_DELETE, CHAIN_FOWARD, "-s", args, "-j", ACTION_ACCEPT };

		case PREDEFINED_RULE_FLUSH:
			return new String[] { "--flush" };

		case PREDEFINED_RULE_POLICY_DROP:
			return new String[] { COMMAND_POLICY, CHAIN_INPUT, ACTION_DROP };

		case PREDEFINED_RULE_INSERT_ULOG:
			return new String[] { COMMAND_INSERT, CHAIN_INPUT, "-j", ACTION_ULOG };

		case PREDEFINED_RULE_FORWARD_ULOG:
			return new String[] { COMMAND_INSERT, CHAIN_FOWARD, "-j", ACTION_ULOG };

		case PREDEFINED_RULE_INSERT_FORWARD_APPEND_ALLOW__IP:
			return new String[] { COMMAND_APPEND, CHAIN_FOWARD, "-s", args, "-j", ACTION_ACCEPT };
		default:
			return null;
		}
	}
}