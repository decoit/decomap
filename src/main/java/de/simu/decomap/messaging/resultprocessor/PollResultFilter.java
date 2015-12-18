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
package de.simu.decomap.messaging.resultprocessor;

/**
 * Poll result filter data-class
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 * 
 */
public class PollResultFilter {

	private String name;
	private String attribute;
	private String value;
	private FilterOperator operator;

	public enum FilterOperator {
		CONTAINS, MATCHES;
	}

	/**
	 * constructor
	 * 
	 * @param name
	 *            name of event
	 * @param attribute
	 *            attribute to check
	 * @param value
	 *            if its this value
	 * @param operator
	 *            based on this operator
	 */
	public PollResultFilter(final String name, final String attribute, final String value, final FilterOperator operator) {
		this.name = name;
		this.attribute = attribute;
		this.value = value;
		this.operator = operator;
	}

	/**
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return attribute
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * 
	 * @param attribute
	 */
	public void setAttribute(final String attribute) {
		this.attribute = attribute;
	}

	/**
	 * 
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * 
	 * @param value
	 */
	public void setValue(final String value) {
		this.value = value;
	}

	/**
	 * 
	 * @return operator
	 */
	public FilterOperator getOperator() {
		return operator;
	}

	/**
	 * 
	 * @param operator
	 */
	public void setOperator(final FilterOperator operator) {
		this.operator = operator;
	}

	@Override
	public String toString() {
		return "PollResultFilter [name=" + name + ", attribute=" + attribute + ", value=" + value + ", operator=" + operator + "]";
	}

}
