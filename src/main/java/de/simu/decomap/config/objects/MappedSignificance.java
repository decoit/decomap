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
package de.simu.decomap.config.objects;

import de.hshannover.f4.trust.ifmapj.metadata.Significance;

/**
 * 
 * Dataclass for significance
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 * 
 */
public class MappedSignificance {

	private Significance significance;

	/**
	 * constructor to resolve signifiance name
	 * 
	 * @param significanceName
	 */
	public MappedSignificance(final String significanceName) {
		switch (significanceName) {
		case "informational":
			significance = Significance.informational;
			break;
		case "important":
			significance = Significance.important;
			break;
		case "critical":
			significance = Significance.critical;
			break;
		default:
			significance = Significance.informational;
		}
	}

	/**
	 * 
	 * @return significance
	 */
	public Significance getSignificance() {
		return this.significance;
	}

}