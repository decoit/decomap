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
package de.simu.decomap.config.converter;

import java.lang.reflect.Method;

import org.aeonbits.owner.Converter;

import de.simu.decomap.messaging.resultprocessor.PollResultFilter;
import de.simu.decomap.messaging.resultprocessor.PollResultFilter.FilterOperator;

/**
 * Converter filter operation into meaning
 * 
 * @author Leonid Schwenke, DECOIT GmbH
 * 
 */
public class PollResultFilterConverter implements Converter<PollResultFilter> {

	@Override
	public PollResultFilter convert(Method targetMethod, String text) {
		String[] split = text.split("_");
		String name = split[0];
		String attribute = null;
		String operator = null;
		String value = null;
		FilterOperator filter = null;
		if (split.length == 4) {
			attribute = split[1];
			operator = split[2];
			value = split[3];

			switch (operator) {
			case "c":
				filter = FilterOperator.CONTAINS;
				break;
			case "m":
				filter = FilterOperator.MATCHES;
				break;
			default:
				filter = FilterOperator.MATCHES;
			}
		}
		return new PollResultFilter(name, attribute, value, filter);
	}
}
