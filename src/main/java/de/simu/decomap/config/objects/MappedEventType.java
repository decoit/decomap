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
package de.simu.decomap.config.objects;

import de.hshannover.f4.trust.ifmapj.metadata.EventType;

/**
 * Dataclass for mapped event type
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 * 
 */
public class MappedEventType {

	private EventType type;

	/**
	 * constructor to set event type
	 * 
	 * @param typeName
	 *            eventtype name
	 */
	public MappedEventType(final String typeName) {
		switch (typeName) {
		case "behavioral-change":
			type = EventType.behavioralChange;
			break;
		case "botnet-infection":
			type = EventType.botnetInfection;
			break;
		case "cve":
			type = EventType.cve;
			break;
		case "excessive-flows":
			type = EventType.excessiveFlows;
			break;
		case "other":
			type = EventType.other;
			break;
		case "p2p":
			type = EventType.p2p;
			break;
		case "policy-violation":
			type = EventType.policyViolation;
			break;
		case "worm-infection":
			type = EventType.wormInfection;
			break;
		default:
			type = EventType.behavioralChange;
		}
	}

	/**
	 * 
	 * @return Event type
	 */
	public EventType getEventType() {
		return this.type;
	}

}