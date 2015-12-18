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
 * 
 * Parameter for a search-request
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 * 
 */
public class SearchRequestParams {

	private String matchLinks;
	private Integer maxDepth;
	private String terminalIdentifier;
	private Integer maxSize;
	private String resultFilter;
	private String startIpIdentifier;

	/**
	 * constructor
	 */
	public SearchRequestParams() {
	}

	/**
	 * constructor
	 * 
	 * @param matchLinks
	 * @param maxDepth
	 * @param terminalIdentifier
	 * @param maxSize
	 * @param resultFilter
	 * @param startIpIdentifier
	 */
	public SearchRequestParams(String matchLinks, Integer maxDepth, String terminalIdentifier, Integer maxSize, String resultFilter,
			String startIpIdentifier) {
		this.matchLinks = matchLinks;
		this.maxDepth = maxDepth;
		this.terminalIdentifier = terminalIdentifier;
		this.maxSize = maxSize;
		this.resultFilter = resultFilter;
		this.startIpIdentifier = startIpIdentifier;
	}

	/**
	 * 
	 * @return matchlinks
	 */
	public String getMatchLinks() {
		return matchLinks;
	}

	/**
	 * 
	 * @param matchLinks
	 */
	public void setMatchLinks(String matchLinks) {
		this.matchLinks = matchLinks;
	}

	/**
	 * 
	 * @return maximal depth
	 */
	public Integer getMaxDepth() {
		return maxDepth;
	}

	/**
	 * 
	 * @param maxDepth
	 */
	public void setMaxDepth(Integer maxDepth) {
		this.maxDepth = maxDepth;
	}

	/**
	 * 
	 * @return terminal identifier
	 */
	public String getTerminalIdentifier() {
		return terminalIdentifier;
	}

	/**
	 * 
	 * @param terminalIdentifier
	 */
	public void setTerminalIdentifier(String terminalIdentifier) {
		this.terminalIdentifier = terminalIdentifier;
	}

	/**
	 * 
	 * @return maximal size
	 */
	public Integer getMaxSize() {
		return maxSize;
	}

	/**
	 * 
	 * @param maxSize
	 */
	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}

	/**
	 * 
	 * @return resultfilter
	 */
	public String getResultFilter() {
		return resultFilter;
	}

	/**
	 * 
	 * @param resultFilter
	 */
	public void setResultFilter(String resultFilter) {
		this.resultFilter = resultFilter;
	}

	/**
	 * 
	 * @return startIPIdentifier
	 */
	public String getStartIpIdentifier() {
		return startIpIdentifier;
	}

	/**
	 * 
	 * @param startIdentifier
	 */
	public void setStartIpIdentifier(String startIdentifier) {
		this.startIpIdentifier = startIdentifier;
	}
}
