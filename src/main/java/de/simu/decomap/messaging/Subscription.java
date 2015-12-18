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
package de.simu.decomap.messaging;

/**
 * Subscription dataclass
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 * 
 */
public class Subscription {

	private static Integer subscriptionCounter = 0;

	private String clientIp;
	private String subscriptionName;

	/**
	 * Constructor
	 * 
	 * @param clientIp
	 *            ip which want to subscribe
	 */
	public Subscription(String clientIp) {
		this.clientIp = clientIp;
		this.subscriptionName = subscriptionCounter.toString();
		subscriptionCounter++;
	}

	/**
	 * 
	 * @return ip of client which want to subscribe
	 */
	public String getClientIp() {
		return clientIp;
	}

	/**
	 * 
	 * @param clientIp
	 *            ip of client which want to subscribe
	 */
	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	/**
	 * 
	 * @return name of subscription
	 */
	public String getSubscriptionName() {
		return subscriptionName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clientIp == null) ? 0 : clientIp.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Subscription other = (Subscription) obj;
		if (clientIp == null) {
			if (other.clientIp != null)
				return false;
		} else if (!clientIp.equals(other.clientIp))
			return false;
		return true;
	}

}
