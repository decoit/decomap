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
 * BasicSimuMappingResult with a Role
 * 
 * @author Leonid Schwenke, DECOIT GmbH
 */
public class SimuRoleAccessRequestMappingResult extends BasicSimuMappingResult {

	private String mRole;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append("\n--> SimuRoleAccessRequestMappingResult \n");
		sb.append("\trole: ");
		sb.append(getRole());
		return sb.toString();
	}

	public boolean equals(Object obj) {
		if (obj instanceof SimuRoleAccessRequestMappingResult) {
			SimuRoleAccessRequestMappingResult result = (SimuRoleAccessRequestMappingResult) obj;
			return mRole != null && mRole.equals(result.mRole)
					&& super.equals(obj);
		}
		return false;
	}

	/**
	 * Getter for role
	 * 
	 * @return Role
	 */
	public String getRole() {
		return mRole;
	}

	/**
	 * setter for role
	 * 
	 * @param role
	 *            new value of role
	 */
	public void setRole(String role) {
		this.mRole = role;
	}

}
