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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hshannover.f4.trust.ifmapj.metadata.EventType;
import de.hshannover.f4.trust.ifmapj.metadata.Significance;
import de.simu.decomap.component.mapping.result.EventMappingResult;
import de.simu.decomap.component.mapping.result.MappingResult;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.config.interfaces.mapping.SimpleEventMappingConfig;
import de.simu.decomap.enums.PublishType;
import de.simu.decomap.main.IfMapClient;
import de.simu.decomap.util.Toolbox;

/**
 * 
 * Abstract Base class for all Event based Mapping-Classes. It provides some
 * basic values that are required for mapping and handle the discoveredBy Flag.
 * 
 * @author Leonid Schwenke, DECOIT GmbH
 * 
 */
public abstract class EventMapper implements MappingInterface {

	private final ArrayList<String> discoveredIps = new ArrayList<String>();

	protected boolean publishUpdate = false;
	protected PublishType publishType = PublishType.NOTIFY;

	// holds the previous mapping results so we can check if an event has been
	// sent before
	protected List<EventMappingResult> previousEventMappingResults = new ArrayList<>();

	protected Significance significance;
	protected EventType eventType;
	protected String confidence;
	protected String magnitude;
	protected String otherTypeDef;
	protected boolean sendDiscoveredBy = false;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init(GeneralConfig mainConfig) {
		logger.info("Initilizing EventMapper");
		SimpleEventMappingConfig eventMappingConfiguration = (SimpleEventMappingConfig) Toolbox
				.loadConfig(mainConfig.mappingComponentConfigPath(),
						SimpleEventMappingConfig.class);

		significance = eventMappingConfiguration.mappingSignificance()
				.getSignificance();
		eventType = eventMappingConfiguration.mappingEventType().getEventType();
		confidence = eventMappingConfiguration.mappingEventConfidence();
		magnitude = eventMappingConfiguration.mappingEventMagnitude();
		otherTypeDef = eventMappingConfiguration.otherTypeDef();

		sendDiscoveredBy = eventMappingConfiguration.sendDiscoveredBy();
		publishUpdate = eventMappingConfiguration.publishUpdate();
		if (publishUpdate) {
			publishType = PublishType.UPDATE;
		}
	}

	@Override
	public MappingResult[] getMappingResult(
			List<HashMap<String, String>> tmpResultList, String publisherId) {
		EventMappingResult[] results = getEventMappingResult(tmpResultList,
				publisherId);
		if (results != null) {
			for (EventMappingResult result : results) {
				if (sendDiscoveredBy && !discoveredIps.contains(result.getIp())
						&& !result.getPublishType().equals(PublishType.DELETE)) {
					logger.info("Discovered new IP: " + result.getIp());
					discoveredIps.add(result.getIp());
					result.setSendDiscoveredBy(true);
				}

				if (result.getSignificance() == null) {
					result.setSignificance(significance);
				}
				if (result.getType() == null) {
					result.setType(eventType);
				}
				if (result.getConfidence() == null) {
					result.setConfidence(confidence);
				}
				if (result.getMagnitude() == null) {
					result.setMagnitude(magnitude);
				}
				if (result.getType().equals(EventType.other)
						&& result.getOtherTypeDef() == null) {
					if (otherTypeDef != null) {
						result.setOtherTypeDef(otherTypeDef);
					} else {
						IfMapClient
								.criticalError(new NullPointerException(
										"If EventTyp is other, then Other Type Definition from config can'T be null!"));
					}
				}
			}
		}

		return results;
	}

	/**
	 * See getMappingResult. getMappingResult() just setting sendDiscoveredBy
	 * Flag to the results from this method.
	 * 
	 * @param tmpResultList
	 *            See getMappingResult
	 * @param publisherId
	 *            See getMappingResult
	 * @return See getMappingResult
	 */
	protected abstract EventMappingResult[] getEventMappingResult(
			List<HashMap<String, String>> tmpResultList, String publisherId);
}
