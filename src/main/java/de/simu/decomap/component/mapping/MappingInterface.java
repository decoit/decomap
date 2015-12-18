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
package de.simu.decomap.component.mapping;

import java.util.HashMap;
import java.util.List;

import de.simu.decomap.component.mapping.result.MappingResult;
import de.simu.decomap.config.interfaces.GeneralConfig;

/**
 * Interface which must be implemented by all Mapping-Classes
 */
public interface MappingInterface {

	/**
	 * initialize Mapping-Class from passed in Config-Object
	 * 
	 * @param mainConfig
	 *            the main Configuration-Object
	 */
	void init(final GeneralConfig mainConfig);

	/**
	 * convert passed in result from polling-thread to a list of
	 * IF-MAP-Mapping-Results
	 * 
	 * @param tmpResultList
	 *            result from PollingThread
	 * @param publisherId
	 *            current IF-MAP publisher id
	 * 
	 * @return the MappingResult list
	 */
	MappingResult[] getMappingResult(final List<HashMap<String, String>> tmpResultList, final String publisherId);

}