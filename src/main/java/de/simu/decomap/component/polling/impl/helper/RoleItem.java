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
package de.simu.decomap.component.polling.impl.helper;

import java.util.Calendar;

/**
 * Container to give a Role a Time.
 * @author lschwenke
 *
 */
public class RoleItem {

	private String role;
	private Calendar date;
	
	/**
	 * Constructor 
	 * @param role
	 * 		Role
	 * @param date2
	 * 		Date
	 */
	public RoleItem(String role, Calendar date2) {
		this.setRole(role);
		this.setDate(date2);
	}

	/**
	 * 
	 * @return Date
	 */
	public Calendar getDate() {
		return date;
	}

	/**
	 * 
	 * @param date Date
	 */
	public void setDate(Calendar date) {
		this.date = date;
	}

	/**
	 * 
	 * @return Role
	 */
	public String getRole() {
		return role;
	}

	/**
	 * 
	 * @param role Role
	 */
	public void setRole(String role) {
		this.role = role;
	}
}
