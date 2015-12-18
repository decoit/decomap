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
package de.simu.decomap.config.interfaces.mapping;

import org.aeonbits.owner.Config.Sources;

@Sources("file:${filename}")
public interface SnortEventMappingConfig extends SimpleEventMappingConfig {

	@Key("eventmapping.p2p")
	String[] p2p();

	@Key("eventmapping.cve")
	String[] cve();

	@Key("eventmapping.botnet_infection")
	String[] botnetInfection();

	@Key("eventmapping.worm_infection")
	String[] wormInfection();

	@Key("eventmapping.excessive_flows")
	String[] excessiveFlows();

	@Key("eventmapping.behavioral_change")
	String[] behavioralChange();

	@Key("eventmapping.policy_violation")
	String[] policyViolation();

	@Key("eventmapping.other")
	String[] other();

	@Key("eventlog.p2p")
	@DefaultValue("true")
	boolean logP2P();

	@Key("eventlog.cve")
	@DefaultValue("true")
	boolean logCVE();

	@Key("eventlog.botnet_infection")
	@DefaultValue("true")
	boolean logBotnetInfection();

	@Key("eventlog.worm_infection")
	@DefaultValue("true")
	boolean logWormInfection();

	@Key("eventlog.excessive_flows")
	@DefaultValue("true")
	boolean logExcessiveFlows();

	@Key("eventlog.behavioral_change")
	@DefaultValue("true")
	boolean logBehavioralChange();

	@Key("eventlog.other")
	@DefaultValue("true")
	boolean logOther();

	@Key("eventlog.policy_violation")
	@DefaultValue("true")
	boolean logPolicyViolation();

	@Key("significancemapping.critical")
	@DefaultValue("true")
	String[] significanceCritical();

	@Key("significancemapping.important")
	@DefaultValue("true")
	String[] significanceImportant();

	@Key("significancemapping.informational")
	@DefaultValue("true")
	String[] significanceInformational();
	
}
