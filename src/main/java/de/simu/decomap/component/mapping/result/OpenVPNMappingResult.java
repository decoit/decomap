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

/**
 * Concrete implementation of Mapping-Result for Results from OpenVPNMapper
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 * @author Leonid Schwenke, DECOIT GmbH
 */
public class OpenVPNMappingResult extends BasicSimuMappingResult {

	private String vpnIPAddress;
	private String vpnIpType;

	/**
	 * @return the vpnIPAddress
	 */
	public String getVpnIpAddress() {
		return vpnIPAddress;
	}

	/**
	 * @param vpnIPAddress
	 *            the vpnIPAddress to set
	 */
	public void setVpnIpAddress(String vpnIPAddress) {
		this.vpnIPAddress = vpnIPAddress;
	}

	/**
	 * @return the ispIpAddress
	 */
	public String getvpnIpType() {
		return vpnIpType;
	}

	/**
	 * @param ispIpAddress
	 *            the ispIpAddress to set
	 */
	public void setVpnIpType(String vpnIPType) {
		this.vpnIpType = vpnIPType;
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
		if (obj instanceof OpenVPNMappingResult) {
			OpenVPNMappingResult result = (OpenVPNMappingResult) obj;
			return vpnIPAddress != null && vpnIPAddress.equals(result.vpnIPAddress)
					&& vpnIpType != null && vpnIpType.equals(result.vpnIpType) && super.equals(obj);
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append("\n--> OpenVPNMappingResult \n");
		sb.append("\tvpn-ip-address: ");
		sb.append(getVpnIpAddress());
		sb.append("\n");
		sb.append("\tvpn-ip-type: ");
		sb.append(getvpnIpType());
		return sb.toString();
	}

}