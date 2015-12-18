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

import de.hshannover.f4.trust.ifmapj.metadata.EnforcementAction;

/**
 * Concrete implementation of Mapping-Result for EnforcementResults from
 * IPTables
 * 
 * @author Leonid Schwenke, DECOIT GmbH
 */
public class EnforcementReportMappingResult extends MappingResult {

	private String ip;
	private EnforcementAction enforcementAction;
	private String otherTypeDefinition;
	private String enforcementReason;

	public EnforcementReportMappingResult() {

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
	 * @return the enforcementAction
	 */
	public EnforcementAction getEnforcementAction() {
		return enforcementAction;
	}

	/**
	 * @param enforcementAction
	 *            the enforcementAction to set
	 */
	public void setEnforcementAction(EnforcementAction enforcementAction) {
		this.enforcementAction = enforcementAction;
	}

	/**
	 * @return the otherTypeDefinition
	 */
	public String getOtherTypeDefinition() {
		return otherTypeDefinition;
	}

	/**
	 * @param otherTypeDefinition
	 *            the otherTypeDefinition to set
	 */
	public void setOtherTypeDefinition(String otherTypeDefinition) {
		this.otherTypeDefinition = otherTypeDefinition;
	}

	/**
	 * @return the enforcementReason
	 */
	public String getEnforcementReason() {
		return enforcementReason;
	}

	/**
	 * @param enforcementReason
	 *            the enforcementReason to set
	 */
	public void setEnforcementReason(String enforcementReason) {
		this.enforcementReason = enforcementReason;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EnforcementReportMappingResult) {
			EnforcementReportMappingResult result = (EnforcementReportMappingResult) obj;
			return ip.equals(result.ip) && enforcementAction != null
					&& enforcementAction.equals(result.enforcementAction)
					&& enforcementReason != null
					&& enforcementReason.equals(result.enforcementReason)
					&& otherTypeDefinition != null
					&& otherTypeDefinition.equals(result.otherTypeDefinition);
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("\n--> BasicSimuMappingResult \n");
		sb.append("\tIP-Address: ");
		sb.append(ip);
		sb.append("\n");
		sb.append("\tEnforcement action: ");
		sb.append(enforcementAction);
		sb.append("\n");
		sb.append("\tother type definition: ");
		sb.append(otherTypeDefinition);
		sb.append("\n");
		sb.append("\tenforcement reason: ");
		sb.append(enforcementReason);
		return sb.toString();
	}

}
