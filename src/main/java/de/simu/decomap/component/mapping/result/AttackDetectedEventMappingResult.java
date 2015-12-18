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

import de.simu.decomap.enums.PublishType;

public class AttackDetectedEventMappingResult extends MappingResult {

	private String discoveredTime;
	private String type;
	private String id;
	private float severity;

	private String serviceIp;
	private String serviceIpType;
	private String servicePort;

	private String attackerIp;
	private String attackerIpType;

	private boolean sendDiscoveredBy = false;

	private PublishType publishType = PublishType.UPDATE;

	/**
	 * 
	 * @return Attacked Service IP
	 */
	public String getServiceIp() {
		return serviceIp;
	}

	/**
	 * 
	 * @param serviceIp
	 *            Attacked Service IP
	 */
	public void setServiceIp(String serviceIp) {
		this.serviceIp = serviceIp;
	}

	/**
	 * 
	 * @return Attack Type ID
	 */
	public String getId() {
		return id;
	}

	/**
	 * 
	 * @param id
	 *            Attack Type ID
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 
	 * @return Severity
	 */
	public float getSeverity() {
		return severity;
	}

	/**
	 * 
	 * @param severity
	 *            Severity
	 */
	public void setSeverity(float severity) {
		this.severity = severity;
	}

	/**
	 * 
	 * @return Service IP Type
	 */
	public String getServiceIpType() {
		return serviceIpType;
	}

	/**
	 * 
	 * @param serviceIpType
	 *            Service IP Type
	 */
	public void setServiceIpType(String serviceIpType) {
		this.serviceIpType = serviceIpType;
	}

	/**
	 * 
	 * @return Service Port
	 */
	public String getServicePort() {
		return servicePort;
	}

	/**
	 * 
	 * @param servicePort
	 *            Service Port
	 */
	public void setServicePort(String servicePort) {
		this.servicePort = servicePort;
	}

	/**
	 * 
	 * @return Attacker IP
	 */
	public String getAttackerIp() {
		return attackerIp;
	}

	/**
	 * 
	 * @param ip
	 *            Attacker IP
	 */
	public void setAttackerIp(String ip) {
		this.attackerIp = ip;
	}

	/**
	 * 
	 * @return Attacker IP Type
	 */
	public String getAttackerIpType() {
		return attackerIpType;
	}

	/**
	 * 
	 * @param ipType
	 *            Attacker IP Type
	 */
	public void setAttackerIpType(String ipType) {
		this.attackerIpType = ipType;
	}

	/**
	 * 
	 * @return Discovered Time
	 */
	public String getDiscoveredTime() {
		return discoveredTime;
	}

	/**
	 * 
	 * @param discoveredTime
	 *            Discovered Time
	 */
	public void setDiscoveredTime(String discoveredTime) {
		this.discoveredTime = discoveredTime;
	}

	/**
	 * 
	 * @return Classification of Attack (Example CVE)
	 */
	public String getType() {
		return type;
	}

	/**
	 * 
	 * @param type
	 *            Classification of Attack (Example CVE)
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * 
	 * @return Map-Server Publish Type
	 */
	public PublishType getPublishType() {
		return publishType;
	}

	/**
	 * 
	 * @param publishType
	 *            Map-Server Publish Type
	 */
	public void setPublishType(PublishType publishType) {
		this.publishType = publishType;
	}

	/**
	 * 
	 * @return need to send discovered by?
	 */
	public boolean isSendDiscoveredBy() {
		return sendDiscoveredBy;
	}

	/**
	 * 
	 * @param sendDiscoveredBy
	 *            need to send discovered by?
	 */
	public void setSendDiscoveredBy(boolean sendDiscoveredBy) {
		this.sendDiscoveredBy = sendDiscoveredBy;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AttackDetectedEventMappingResult) {
			AttackDetectedEventMappingResult result = (AttackDetectedEventMappingResult) obj;

			return result.id != null && result.id.equals(id)
					&& result.severity == severity && result.serviceIp != null
					&& result.serviceIp.equals(serviceIp)
					&& result.serviceIpType != null
					&& result.serviceIpType.equals(serviceIpType)
					&& result.servicePort != null
					&& result.servicePort.equals(servicePort)
					&& result.attackerIp != null
					&& result.attackerIp.equals(attackerIp)
					&& result.attackerIpType != null
					&& result.attackerIpType.equals(attackerIpType)
					&& result.discoveredTime != null
					&& result.discoveredTime.equals(discoveredTime)
					&& result.type != null && result.type.equals(type)
					&& result.publishType != null
					&& result.publishType.equals(publishType);
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n--> AtackDetectedEventMappingResult \n");
		sb.append("\ttype: ");
		sb.append(type);
		sb.append("\n");
		sb.append("\tid: ");
		sb.append(id);
		sb.append("\n");
		sb.append("\tdiscovered time: ");
		sb.append(discoveredTime);
		sb.append("\n");
		sb.append("\tseverity: ");
		sb.append(severity);
		sb.append("\n");

		sb.append("\tattacker ip: ");
		sb.append(serviceIp);
		sb.append("\n");
		sb.append("\tattacker iptype: ");
		sb.append(serviceIp);
		sb.append("\n");

		sb.append("\tservice ip: ");
		sb.append(serviceIp);
		sb.append("\n");
		sb.append("\tservice iptype: ");
		sb.append(serviceIpType);
		sb.append("\n");
		sb.append("\tservice port: ");
		sb.append(servicePort);
		sb.append("\n");
		sb.append("\tpublish type: ");
		sb.append(publishType);
		sb.append("\n");

		return sb.toString();
	}
}
