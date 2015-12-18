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
package de.simu.decomap.component.mapping.result;

import de.hshannover.f4.trust.ifmapj.metadata.EventType;
import de.hshannover.f4.trust.ifmapj.metadata.Significance;
import de.simu.decomap.enums.PublishType;

/**
 * Concrete implementation of mapping-result. Represents an IF-MAP-Event
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 * @author Leonid Schwenke, DECOIT GmbH
 */
public class EventMappingResult extends MappingResult implements Cloneable {

	private String ip;
	private String discoveredTime;
	private String discovererId;
	private String magnitude;
	private String confidence;
	private String name;
	private String ipType;
	private String vulnerabilityUri;
	private String identity;
	private String otherTypeDef;

	private boolean sendDiscoveredBy = false;;

	private Significance significance;
	private EventType type;
	private PublishType publishType = PublishType.UPDATE;

	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @param ip
	 *            the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * @return the discoveredTime
	 */
	public String getDiscoveredTime() {
		return discoveredTime;
	}

	/**
	 * @param discoveredTime
	 *            the discoveredTime to set
	 */
	public void setDiscoveredTime(String discoveredTime) {
		this.discoveredTime = discoveredTime;
	}

	/**
	 * @return the discovererId
	 */
	public String getDiscovererId() {
		return discovererId;
	}

	/**
	 * @param discovererId
	 *            the discovererId to set
	 */
	public void setDiscovererId(String discovererId) {
		this.discovererId = discovererId;
	}

	/**
	 * @return the magnitude
	 */
	public String getMagnitude() {
		return magnitude;
	}

	/**
	 * @param magnitude
	 *            the magnitude to set
	 */
	public void setMagnitude(String magnitude) {
		this.magnitude = magnitude;
	}

	/**
	 * @return the confidence
	 */
	public String getConfidence() {
		return confidence;
	}

	/**
	 * @param confidence
	 *            the confidence to set
	 */
	public void setConfidence(String confidence) {
		this.confidence = confidence;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the ipType
	 */
	public String getIpType() {
		return ipType;
	}

	/**
	 * @param ipType
	 *            the ipType to set
	 */
	public void setIpType(String ipType) {
		this.ipType = ipType;
	}

	/**
	 * @return the vulnerabilityUri
	 */
	public String getVulnerabilityUri() {
		return vulnerabilityUri;
	}

	/**
	 * @param vulnerabilityUri
	 *            the vulnerabilityUri to set
	 */
	public void setVulnerabilityUri(String vulnerabilityUri) {
		this.vulnerabilityUri = vulnerabilityUri;
	}

	/**
	 * @return the identity
	 */
	public String getIdentity() {
		return identity;
	}

	/**
	 * @param identity
	 *            the identity to set
	 */
	public void setIdentity(String identity) {
		this.identity = identity;
	}

	/**
	 * @return the significance
	 */
	public Significance getSignificance() {
		return significance;
	}

	/**
	 * @param significance
	 *            the significance to set
	 */
	public void setSignificance(Significance significance) {
		this.significance = significance;
	}

	/**
	 * @return the type
	 */
	public EventType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(EventType type) {
		this.type = type;
	}

	/**
	 * @return the publishType
	 */
	public PublishType getPublishType() {
		return publishType;
	}

	/**
	 * @param publishType
	 *            the publishType to set
	 */
	public void setPublishType(PublishType publishType) {
		this.publishType = publishType;
	}

	public boolean isSendDiscoveredBy() {
		return sendDiscoveredBy;
	}

	public void setSendDiscoveredBy(boolean sendDiscoveredBy) {
		this.sendDiscoveredBy = sendDiscoveredBy;
	}

	public String getOtherTypeDef() {
		return otherTypeDef;
	}

	public void setOtherTypeDef(String otherTypeDef) {
		this.otherTypeDef = otherTypeDef;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EventMappingResult) {
			EventMappingResult result = (EventMappingResult) obj;

			return ip.equals(result.ip)
					&& (discoveredTime == result.discoveredTime || discoveredTime != null
							&& discoveredTime.equals(result.discoveredTime))
					&& (discovererId == result.discovererId || discovererId != null
							&& discovererId.equals(result.discovererId))
					&& (magnitude == result.magnitude || magnitude != null
							&& magnitude.equals(result.magnitude))
					&& (confidence == result.confidence || magnitude != null
							&& confidence.equals(result.confidence))
					&& (name == result.name || name != null
							&& name.equals(result.name))
					&& (ipType == result.ipType || ipType != null
							&& ipType.equals(result.ipType))
					&& (vulnerabilityUri == result.vulnerabilityUri || vulnerabilityUri != null
							&& vulnerabilityUri.equals(result.vulnerabilityUri))
					&& (identity == result.identity || identity != null
							&& identity.equals(result.identity))
					&& (significance == result.significance || significance != null
							&& significance.equals(result.significance))
					&& (type == result.type || type != null
							&& type.equals(result.type))
					&& (publishType == result.publishType || publishType != null
							&& publishType.equals(result.publishType))
					&& (otherTypeDef == result.otherTypeDef || otherTypeDef != null
							&& otherTypeDef.equals(result.otherTypeDef));

		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n--> EventMappingResult \n");
		sb.append("\tidentity: ");
		sb.append(identity);
		sb.append("\n");
		sb.append("\tip-address: ");
		sb.append(ip);
		sb.append("\n");
		sb.append("\tdiscovered-time: ");
		sb.append(discoveredTime);
		sb.append("\n");
		sb.append("\tdiscoverer-id: ");
		sb.append(discovererId);
		sb.append("\n");
		sb.append("\tmagnitude: ");
		sb.append(magnitude);
		sb.append("\n");
		sb.append("\tconfidence: ");
		sb.append(confidence);
		sb.append("\n");
		sb.append("\tsignificance: ");
		sb.append(significance);
		sb.append("\n");
		if (type != null) {
			sb.append("\tevent-msg-type: ");
			sb.append(type.toString());
			sb.append("\n");
			if (type.equals(EventType.other)) {
				sb.append("\tother type definition: ");
				sb.append(otherTypeDef);
				sb.append("\n");
			}
		}
		sb.append("\tname: ");
		sb.append(name);
		sb.append("\n");
		sb.append("\tip-type: ");
		sb.append(ipType);
		sb.append("\n");
		sb.append("\tvulnerabilitiy-uri: ");
		sb.append(vulnerabilityUri);
		sb.append("\n");
		sb.append("\tidentity: ");
		sb.append(identity);
		sb.append("\n");
		return sb.toString();
	}

}
