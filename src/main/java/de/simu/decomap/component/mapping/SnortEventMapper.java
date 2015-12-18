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
package de.simu.decomap.component.mapping;

import de.hshannover.f4.trust.ifmapj.metadata.EventType;
import de.hshannover.f4.trust.ifmapj.metadata.Significance;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.config.interfaces.mapping.SnortEventMappingConfig;
import de.simu.decomap.util.Toolbox;

import java.util.HashMap;

/**
 * Abstract Base class for all Snort related Mapping-Classes using IFMAPJ
 * Implements the Mapping-Interface and provides some basic values that are
 * required for mapping Snort log-entries to IF-MAP-Results
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 */
public abstract class SnortEventMapper extends EventMapper {

	// mapping between snort and IFMAP values
	private HashMap<String, String> eventMap;
	private HashMap<String, String> significanceMap;

	// flags for determine which events should be processed
	private boolean logP2P = true;
	private boolean logCVE = true;
	private boolean logBotnetInfection = true;
	private boolean logWormInfection = true;
	private boolean logExcessiveFlows = true;
	private boolean logOther = true;
	private boolean logBehavioralChange = true;
	private boolean logPolicyViolation = true;

	/**
	 * Constructor
	 * 
	 * @param mainConfig
	 *            the main Configuration-Object
	 */
	@Override
	public void init(final GeneralConfig mainConfig) {
		super.init(mainConfig);
		SnortEventMappingConfig snortMappingConfiguration = (SnortEventMappingConfig) Toolbox.loadConfig(
				mainConfig.mappingComponentConfigPath(), SnortEventMappingConfig.class);
		
		eventMap = combineEventArrays(snortMappingConfiguration.behavioralChange(), snortMappingConfiguration.botnetInfection(),
				snortMappingConfiguration.cve(), snortMappingConfiguration.excessiveFlows(), snortMappingConfiguration.other(),
				snortMappingConfiguration.p2p(), snortMappingConfiguration.policyViolation(), snortMappingConfiguration.wormInfection());

		HashMap<String, String> significance = combineSignificanceArrays(snortMappingConfiguration.significanceCritical(),
				snortMappingConfiguration.significanceInformational(), snortMappingConfiguration.significanceImportant());

		if (significance != null && significance.size() > 0) {
			significanceMap = significance;
		}
		
		logBehavioralChange = snortMappingConfiguration.logBehavioralChange();
		logBotnetInfection = snortMappingConfiguration.logBotnetInfection();
		logCVE = snortMappingConfiguration.logCVE();
		logExcessiveFlows = snortMappingConfiguration.logExcessiveFlows();
		logOther = snortMappingConfiguration.logOther();
		logP2P = snortMappingConfiguration.logP2P();
		logPolicyViolation = snortMappingConfiguration.logPolicyViolation();
		logWormInfection = snortMappingConfiguration.logWormInfection();
	
	}

	/**
	 * combines
	 * 
	 * @param critical
	 * @param informational
	 * @param important
	 * @return
	 */
	private HashMap<String, String> combineSignificanceArrays(final String[] critical, final String[] informational,
			final String[] important) {

		HashMap<String, String> significanceMap = new HashMap<String, String>();

		for (String current : critical) {
			significanceMap.put(current, "critical");
		}
		for (String current : informational) {
			significanceMap.put(current, "informational");
		}
		for (String current : important) {
			significanceMap.put(current, "important");
		}

		return significanceMap;
	}

	/**
	 * combines
	 * 
	 * @param behavioralChange
	 * @param botnetInfection
	 * @param cve
	 * @param excessiveFlows
	 * @param other
	 * @param p2p
	 * @param policyViolation
	 * @param wormInfection
	 * @return
	 */
	private HashMap<String, String> combineEventArrays(final String[] behavioralChange, final String[] botnetInfection, final String[] cve,
			final String[] excessiveFlows, final String[] other, final String[] p2p, final String[] policyViolation,
			final String[] wormInfection) {

		// build new event-map from event-arrays
		HashMap<String, String> eventMap = new HashMap<String, String>();
		if (behavioralChange != null) {
			for (String current : wormInfection) {
				eventMap.put(current, "behavioral change");
			}
		}

		if (botnetInfection != null) {
			for (String current : botnetInfection) {
				eventMap.put(current, "botnet infection");
			}
		}

		if (cve != null) {
			for (String current : cve) {
				eventMap.put(current, "cve");
			}
		}

		if (excessiveFlows != null) {
			for (String current : excessiveFlows) {
				eventMap.put(current, "excessive flows");
			}

		}

		if (other != null) {
			for (String current : other) {
				eventMap.put(current, "other");
			}
		}

		if (p2p != null) {
			for (String current : p2p) {
				eventMap.put(current, "p2p");
			}
		}

		if (policyViolation != null) {
			for (String current : policyViolation) {
				eventMap.put(current, "policy violation");
			}
		}

		if (wormInfection != null) {
			for (String current : wormInfection) {
				eventMap.put(current, "worm infection");
			}
		}

		return eventMap;
	}

	/**
	 * get the IF-MAP significance value for passed in snort-priority-value
	 * 
	 * @param int snortPriorityValue
	 * 
	 * @return significance value as string
	 */
	protected Significance getSignificanceValue(final int snortPriorityValue) {
		String sigValue = null;
		Significance sign = Significance.informational; // default!

		if (significanceMap.containsKey(new Integer(snortPriorityValue).toString())) {
			sigValue = significanceMap.get(new Integer(snortPriorityValue).toString());
		}

		if (sigValue != null && sigValue.length() > 0) {
			if (sigValue.startsWith("important")) {
				sign = Significance.important;
			} else if (sigValue.startsWith("informational")) {
				sign = Significance.informational;
			} else if (sigValue.startsWith("critical")) {
				sign = Significance.critical;
			}
		}

		return sign;
	}

	/**
	 * get the IF-MAP Event name for passed in Snort-Signature-Name (see
	 * config/snort/mapping.properties)
	 * 
	 * @param sigName
	 *            snort signature name
	 * @return corresponding IF-MAP Signature Name
	 */
	protected EventType getEventMappingForSignatureName(final String sigName) {
		String mappedEvent = null;
		if (eventMap.containsKey(sigName)) {
			mappedEvent = eventMap.get(sigName);
		} else {
			mappedEvent = "other";
		}

		EventType evntype = EventType.other; // default
		if (mappedEvent != null && mappedEvent.length() > 0) {
			if (mappedEvent.startsWith("behavioral change")) {
				evntype = EventType.behavioralChange;
			} else if (mappedEvent.startsWith("botnet infection")) {
				evntype = EventType.botnetInfection;
			} else if (mappedEvent.startsWith("cve")) {
				evntype = EventType.cve;
			} else if (mappedEvent.startsWith("excessive flows")) {
				evntype = EventType.excessiveFlows;
			} else if (mappedEvent.startsWith("p2p")) {
				evntype = EventType.p2p;
			} else if (mappedEvent.startsWith("policy violation")) {
				evntype = EventType.policyViolation;
			} else if (mappedEvent.startsWith("worm infection")) {
				evntype = EventType.wormInfection;
			} else {
				evntype = EventType.other;
			}

		}

		return evntype;
	}

	/**
	 * get event type from passed in string and decide if current message should
	 * be converted to IF-MAP Events, depending on Properties from Settings in
	 * config/snort/mapping.properties
	 * 
	 * @param eventType
	 *            type of the event
	 * 
	 * @return flag indicating whether the message should be convertet or not
	 */
	protected boolean doConvert(final EventType eventType) {
		// map snort-event-type to if map event
		if (eventType == EventType.behavioralChange && !logBehavioralChange) {
			return false;
		} else if (eventType == EventType.botnetInfection && !logBotnetInfection) {
			return false;
		} else if (eventType == EventType.botnetInfection && !logCVE) {
			return false;
		} else if (eventType == EventType.excessiveFlows && !logExcessiveFlows) {
			return false;
		} else if (eventType == EventType.other && !logOther) {
			return false;
		} else if (eventType == EventType.p2p && !logP2P) {
			return false;
		} else if (eventType == EventType.policyViolation && !logPolicyViolation) {
			return false;
		} else if (eventType == EventType.wormInfection && !logWormInfection) {
			return false;
		} else {
			return true;
		}
	}
}