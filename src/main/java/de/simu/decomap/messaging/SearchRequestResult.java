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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * SearchRequestResult dataclass
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 * 
 */
public class SearchRequestResult {

	private String identifier1;
	private String identifier2;
	private List<HashMap<String, String>> metadata = new ArrayList<>();

	/**
	 * consructor
	 */
	public SearchRequestResult() {
	}

	/**
	 * constructor 2
	 * 
	 * @param ident1
	 *            first identifier
	 * @param ident2
	 *            second identifier
	 * @param metadata
	 *            metadata to earch for
	 */
	public SearchRequestResult(String ident1, String ident2, List<HashMap<String, String>> metadata) {
		this.identifier1 = ident1;
		this.identifier2 = ident2;
		this.metadata = metadata;
	}

	/**
	 * 
	 * @return Identifier1
	 */
	public String getIdentifier1() {
		return identifier1;
	}

	/**
	 * 
	 * @param identifier1
	 */
	public void setIdentifier1(String identifier1) {
		this.identifier1 = identifier1;
	}

	/**
	 * 
	 * @return Identifier2
	 */
	public String getIdentifier2() {
		return identifier2;
	}

	/**
	 * 
	 * @param identifier2
	 */
	public void setIdentifier2(String identifier2) {
		this.identifier2 = identifier2;
	}

	/**
	 * 
	 * @return Metadata to ssearch for
	 */
	public List<HashMap<String, String>> getMetadata() {
		return metadata;
	}

	/**
	 * 
	 * @param metadata
	 *            metadata to search for
	 */
	public void setMetadata(List<HashMap<String, String>> metadata) {
		this.metadata = metadata;
	}

	@Override
	public String toString() {
		String bla = "SearchRequestResult [identifier1=" + identifier1 + ", identifier2=" + identifier2 + "]\n";
		for (int i = 0; i < this.metadata.size(); i++) {
			bla += "\nMetadata[" + i + "]:";
			bla += this.metadata.get(i).toString();
		}

		return bla;
	}

}
