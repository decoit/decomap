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
package de.simu.decomap.config.regex;

import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simu.decomap.util.Toolbox;

/**
 * 
 * Holding Regex pattern
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 * 
 */
public class RegExHolder {

	private String path;
	private Properties props;
	private HashMap<String, Pattern> regExMap = new HashMap<String, Pattern>();

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * constructor
	 * 
	 * @param filePath
	 */
	public RegExHolder(final String filePath) {
		path = filePath;
		props = loadProperties();

		if (props != null) {
			Enumeration<Object> em = props.keys();
			while (em.hasMoreElements()) {
				String str = (String) em.nextElement();
				if (!Toolbox.isNullOrEmpty(str) && !Toolbox.isNullOrEmpty(props.get(str).toString()) && str.startsWith("regex")) {
					regExMap.put(str, Pattern.compile(props.get(str).toString()));
				}
			}
		}
	}

	/**
	 * load regex from properties
	 * 
	 * @return properties
	 */
	private Properties loadProperties() {
		Properties props = new Properties();
		try {
			FileInputStream in;
			in = new FileInputStream(path);
			props.load(in);
			in.close();
		} catch (Exception e) {
			logger.error("Error on loading properties!");
			return null;
		}

		return props;
	}

	/**
	 * 
	 * @param key
	 *            which pattern
	 * @return regex pattern
	 */
	public Pattern getRegExPattern(final String key) {
		if (regExMap == null) {
			logger.warn("no Regex pattern found!");
			return null;
		}
		return regExMap.get(key);
	}

}
