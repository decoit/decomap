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

import java.util.Calendar;

import de.decoit.simumetadata.CredentialType;
import de.decoit.simumetadata.LoginFailureReason;
import de.simu.decomap.enums.PublishType;

/**
 * Basic Simu-Eventstructure with AccessRequest, IP-Address, Identity and
 * Success
 * 
 * @author Leonid Schwenke, DECOIT GmbH
 * 
 */
public class BasicSimuMappingResult extends MappingResult implements Cloneable,
		Comparable<BasicSimuMappingResult> {

	private String ip;
	private Calendar discoveredTime;
	private String name;
	private String ipType;
	private String identity;

	private CredentialType credentialType;
	private String otherTypDef;
	private LoginFailureReason loginFailureReasion;
	private String loginFailureReasionDef;

	private PublishType publishType = PublishType.UPDATE;
	private boolean mLogginSuccess = false;

	/**
	 * setter for login success boolean
	 * 
	 * @param badLogin
	 *            new value of badLogin
	 */
	public void setLoginSuccess(Boolean logginSuccess) {
		this.mLogginSuccess = logginSuccess;
	}

	/**
	 * getter for login success boolean
	 * 
	 * @return BadLogin
	 */
	public boolean isLoginSuccessful() {
		return mLogginSuccess;
	}

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
	 * @return the discoveredTime. If mLogginSuccess is true, then this is the
	 *         minimum life date
	 */
	public Calendar getDiscoveredTime() {
		return discoveredTime;
	}

	/**
	 * @param discoveredTime
	 *            the discoveredTime to set. If mLogginSuccess is true, then
	 *            this is the minimum life date
	 */
	public void setDiscoveredTime(Calendar discoveredTime) {
		this.discoveredTime = discoveredTime;
	}

	/**
	 * @return the name
	 */
	public String getARName() {
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

	/**
	 * 
	 * @return Credential Type
	 */
	public CredentialType getCredentialType() {
		return credentialType;
	}

	/**
	 * 
	 * @param credentialType
	 *            Credential Type
	 */
	public void setCredentialType(CredentialType credentialType) {
		this.credentialType = credentialType;
	}

	/**
	 * 
	 * @return Credential Other Type Definition. Only needed if Credential Type
	 *         is other
	 */
	public String getCedentialOtherTypDef() {
		return otherTypDef;
	}

	/**
	 * 
	 * @param otherTypDef
	 *            Credential Other Type Definition. Only needed if Credential
	 *            Type is other
	 */
	public void setOtherTypDef(String otherTypDef) {
		this.otherTypDef = otherTypDef;
	}

	/**
	 * 
	 * @return Login Failure Reason
	 */
	public LoginFailureReason getLoginFailureReasion() {
		return loginFailureReasion;
	}

	/**
	 * 
	 * @param loginFailureReasion
	 *            Login Failure Reason
	 */
	public void setLoginFailureReasion(LoginFailureReason loginFailureReasion) {
		this.loginFailureReasion = loginFailureReasion;
	}

	/**
	 * 
	 * @return Login Failure Reason Other Type Definition. Only needed if Login
	 *         Failure Reason is other
	 */
	public String getLoginFailureReasionDef() {
		return loginFailureReasionDef;
	}

	/**
	 * 
	 * @param loginFailureReasionDef
	 *            Login Failure Reason Other Type Definition. Only needed if
	 *            Login Failure Reason is other
	 */
	public void setLoginFailureReasionDef(String loginFailureReasionDef) {
		this.loginFailureReasionDef = loginFailureReasionDef;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BasicSimuMappingResult) {
			BasicSimuMappingResult result = (BasicSimuMappingResult) obj;
			return identity != null && identity.equals(result.identity)
					&& ip != null && ip.equals(result.ip) && ipType != null
					&& ipType.equals(result.ipType) && discoveredTime != null
					&& discoveredTime.equals(result.discoveredTime)
					&& mLogginSuccess == result.mLogginSuccess;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("\n--> BasicSimuMappingResult \n");
		sb.append("\tidentity: ");
		sb.append(identity);
		sb.append("\n");
		sb.append("\tip-address: ");
		sb.append(ip);
		sb.append("\n");
		sb.append("\tdiscovered-time: ");
		sb.append(discoveredTime);
		sb.append("\n");
		sb.append("\tname: ");
		sb.append(name);
		sb.append("\n");
		sb.append("\tip-type: ");
		sb.append(ipType);
		sb.append("\n");
		sb.append("\tlogin success: ");
		sb.append(mLogginSuccess);
		sb.append("\n");
		sb.append("\tCredential type: ");
		sb.append(credentialType);
		sb.append("\n");
		sb.append("\tCredential Other type definition: ");
		sb.append("\n");
		sb.append(otherTypDef);
		sb.append("\n");
		sb.append("\tLogin failure reasion: ");
		sb.append(loginFailureReasion);
		sb.append("\n");
		sb.append("\tLogin failure reasion definition: ");
		sb.append("\n");
		sb.append(loginFailureReasionDef);
		return sb.toString();
	}

	@Override
	public int compareTo(BasicSimuMappingResult o) {
		return (int) (discoveredTime.getTimeInMillis() - o.discoveredTime
				.getTimeInMillis());
	}

}
