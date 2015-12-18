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
package de.simu.decomap.messaging.sender;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.regex.Matcher;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import de.simu.decomap.component.garbagecollector.GarbageCollectorThread;
import de.simu.decomap.component.mapping.result.SimuRoleAccessRequestMappingResult;
import de.decoit.simumetadata.SimuMetaDataFactory;
import de.decoit.simumetadata.SimuMetadataFactoryImpl;
import de.hshannover.f4.trust.ifmapj.IfmapJ;
import de.hshannover.f4.trust.ifmapj.binding.IfmapStrings;
import de.hshannover.f4.trust.ifmapj.channel.SSRC;
import de.hshannover.f4.trust.ifmapj.config.BasicAuthConfig;
import de.hshannover.f4.trust.ifmapj.config.CertAuthConfig;
import de.hshannover.f4.trust.ifmapj.exception.IfmapErrorResult;
import de.hshannover.f4.trust.ifmapj.exception.IfmapException;
import de.hshannover.f4.trust.ifmapj.exception.InitializationException;
import de.hshannover.f4.trust.ifmapj.exception.MarshalException;
import de.hshannover.f4.trust.ifmapj.identifier.AccessRequest;
import de.hshannover.f4.trust.ifmapj.identifier.Device;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.ifmapj.identifier.Identity;
import de.hshannover.f4.trust.ifmapj.identifier.IdentityType;
import de.hshannover.f4.trust.ifmapj.identifier.IpAddress;
import de.hshannover.f4.trust.ifmapj.messages.MetadataLifetime;
import de.hshannover.f4.trust.ifmapj.messages.PollResult;
import de.hshannover.f4.trust.ifmapj.messages.PublishDelete;
import de.hshannover.f4.trust.ifmapj.messages.PublishNotify;
import de.hshannover.f4.trust.ifmapj.messages.PublishRequest;
import de.hshannover.f4.trust.ifmapj.messages.PublishUpdate;
import de.hshannover.f4.trust.ifmapj.messages.Requests;
import de.hshannover.f4.trust.ifmapj.messages.ResultItem;
import de.hshannover.f4.trust.ifmapj.messages.SearchRequest;
import de.hshannover.f4.trust.ifmapj.messages.SearchResult;
import de.hshannover.f4.trust.ifmapj.messages.SubscribeRequest;
import de.hshannover.f4.trust.ifmapj.metadata.StandardIfmapMetadataFactory;
import de.simu.decomap.component.mapping.result.AttackDetectedEventMappingResult;
import de.simu.decomap.component.mapping.result.BasicSimuMappingResult;
import de.simu.decomap.component.mapping.result.EnforcementReportMappingResult;
import de.simu.decomap.component.mapping.result.EventMappingResult;
import de.simu.decomap.component.mapping.result.MappingResult;
import de.simu.decomap.component.mapping.result.OpenVPNMappingResult;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.config.interfaces.mapping.BasicSimuMappingConfig;
import de.simu.decomap.enums.MappingResultType;
import de.simu.decomap.enums.PublishType;
import de.simu.decomap.main.IfMapClient;
import de.simu.decomap.messaging.SearchRequestParams;
import de.simu.decomap.messaging.SearchRequestResult;
import de.simu.decomap.messaging.Subscription;
import de.simu.decomap.messaging.pollingthread.IfMapJPollingThread;
import de.simu.decomap.messaging.pollingthread.SubscriptionPollingThread;
import de.simu.decomap.messaging.resultprocessor.PollResultProcessor;
import de.simu.decomap.util.Toolbox;

/**
 * Implementation for a MessageSender over SOAP
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 * @author Leonid Schwenke, DECOIT GmbH
 * 
 */
public class IFMAPJMessageSender implements MessageSender {

	private String ipAddress;
	private int port;
	private String serviceType;
	private String serviceName;
	private String ad;
	private boolean isService = false;

	// Garbagecollector minimum time to live
	private int gcmttl = 0;

	private Device device;
	private IpAddress deviceIP;
	private Identifier deviceService;

	// represents our SSRC to the MAPS
	private SSRC mSSRC;

	// indicates whether a session is active or not.
	private volatile boolean mSessionActive;

	private IfMapJPollingThread pollingThread;

	private List<Subscription> subscriptions = new ArrayList<>();

	private PollResult lastPollResult;

	private PollResultProcessor pollResultProcessor;

	// document builder and factory

	private StandardIfmapMetadataFactory mBasicMetadataFactory;
	private SimuMetaDataFactory mSimuMetadataFactory;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * constructor
	 */
	public IFMAPJMessageSender() {
		mBasicMetadataFactory = IfmapJ.createStandardMetadataFactory();
		try {
			mSimuMetadataFactory = new SimuMetadataFactoryImpl();
		} catch (ParserConfigurationException e) {
			logger.error("Parserexception from Simumetadatafactory!", e);
			IfMapClient.criticalError(e);
		}
	}

	@Override
	public void setPollingThread(SubscriptionPollingThread pollingThread) {
		if (!(pollingThread instanceof IfMapJPollingThread)) {
			logger.warn("Wrong PollingThread!!!");
			return;
		}

		// TODO: DO changeable
		this.pollingThread = (IfMapJPollingThread) pollingThread;
	}

	@Override
	public void setPollResultProcessor(PollResultProcessor resultProcessor) {
		this.pollResultProcessor = resultProcessor;
	}

	@Override
	public void init(GeneralConfig cfg) throws MessageSenderException {
		logger.debug("Initialize IFMAPJMessageSender");

		this.ipAddress = cfg.applicationIpAddress();
		isService = cfg.isService();
		if (this.isService) {
			this.port = cfg.applicationServicePort();
			this.serviceType = cfg.applicationServiceType();
			this.serviceName = cfg.applicationServiceName();
			this.ad = cfg.applicationAdministrativDomain();
		}

		this.pollingThread = new IfMapJPollingThread();
		if (pollResultProcessor != null
				&& cfg.pollResultFilterConfigPath() != null) {
			this.pollResultProcessor.init(cfg, this);
		}

		if (mSSRC == null) {
			try {

				if (cfg.mapServerBasicAuthEnabled()) {
					mSSRC = IfmapJ.createSsrc(new BasicAuthConfig(cfg
							.mapServerUrl(), cfg.mapServerbasicAuthUser(), cfg
							.mapServerbasicAuthPassword(), cfg
							.mapServerKeystorePath(), cfg
							.mapServerKeystorePassword()));
				} else {
					mSSRC = IfmapJ.createSsrc(new CertAuthConfig(cfg
							.mapServerUrl(), cfg.mapServerKeystorePath(), cfg
							.mapServerKeystorePassword(), cfg
							.mapServerKeystorePath(), cfg
							.mapServerKeystorePassword()));
				}
			} catch (InitializationException e) {
				logger.error("Error on initialize IFMAPJMessageSender");
				IfMapClient.criticalError(e);
			}
		}

		BasicSimuMappingConfig basicSimuMappingConfiguration = (BasicSimuMappingConfig) Toolbox
				.loadConfig(cfg.mappingComponentConfigPath(),
						BasicSimuMappingConfig.class);

		gcmttl = basicSimuMappingConfiguration.mttl();

	}

	@Override
	public String getIfMapPublisherId() {
		if (mSSRC != null && mSessionActive) {
			return mSSRC.getPublisherId();
		}
		logger.warn("Can't get publisher ID!");
		return null;
	}

	@Override
	public void startSession() {
		logger.info("Starting new Session");

		if (!mSessionActive && mSSRC != null) {
			try {
				mSSRC.newSession();
			} catch (IfmapErrorResult e) {
				logger.error("IfmapErrorResult while starting Session");
				IfMapClient.criticalError(e);
			} catch (IfmapException e) {
				logger.error("IfmapException while starting Session");
				IfMapClient.criticalError(e);
			}
			mSessionActive = true;
		}

	}

	@Override
	public void endSession() {
		logger.info("Ending session!");
		if (!mSessionActive || mSSRC == null) {
			return;
		}
		try {
			mSSRC.endSession();
		} catch (IfmapErrorResult e) {
			logger.error("IfmapErrorResult while ending Session");
			IfMapClient.criticalError(e);
		} catch (IfmapException e) {
			logger.error("IfmapException while ending Session");
			IfMapClient.criticalError(e);
		}
		mSessionActive = false;

	}

	@Override
	public void purgePublisher() {
		if (mSSRC != null && mSessionActive)
			try {
				mSSRC.purgePublisher();
			} catch (IfmapErrorResult e) {
				logger.error("IfmapErrorResult while purge Publisher");
				IfMapClient.criticalError(e);
			} catch (IfmapException e) {
				logger.error("IfmapException while purge Publisher");
				IfMapClient.criticalError(e);
			}
	}

	/**
	 * Publish standard event data
	 * 
	 * @param resultList
	 *            result which to publish
	 * @throws MessageSenderError
	 * @throws MessageSenderException
	 */
	private void publishEventMetaData(MappingResult[] resultList)
			throws MessageSenderError, MessageSenderException {

		logger.debug("Publishing event metadata");
		// create publish update and requests objects
		PublishRequest pr = Requests.createPublishReq();

		for (MappingResult mappingResult : resultList) {
			// prepare event-data
			EventMappingResult tmpEvent = (EventMappingResult) mappingResult;

			if (tmpEvent.getName() == null || tmpEvent.getName().length() == 0) {
				logger.warn("No event name found! Using undefined!");
				tmpEvent.setName("undefined");
			}

			prepareEventMetadataPublushRequest(tmpEvent, pr);

		}

		if (pr.getPublishElements().size() > 0) {
			publish(pr);
		} else {
			logger.debug("No elements to publish!");
		}
	}

	/**
	 * Publish snort cve event data
	 * 
	 * @param resultList
	 *            result which to publish
	 * @throws MessageSenderError
	 * @throws MessageSenderException
	 */
	private void publishSnortAttackDetectedMetaData(MappingResult[] resultList)
			throws MessageSenderError, MessageSenderException {
		logger.debug("Publishing snort AttackDetected metadata");
		// create publish update and requests objects
		PublishRequest pr = Requests.createPublishReq();

		for (MappingResult mappingResult : resultList) {
			// prepare event-data
			AttackDetectedEventMappingResult tmpEvent = (AttackDetectedEventMappingResult) mappingResult;

			// get values for IP-Identifiers from mapping-result
			IpAddress attackerIpAddress = createIpAddress(
					tmpEvent.getAttackerIp(), tmpEvent.getAttackerIpType());

			IpAddress serviceIpAddress = createIpAddress(
					tmpEvent.getServiceIp(), tmpEvent.getServiceIpType());
			if (attackerIpAddress == null || serviceIpAddress == null) {
				logger.warn("Event with missing IP found! Skipping Event!");
				continue;
			}

			Identifier service = null;
			try {
				int srvPort = 0;
				if(tmpEvent.getServicePort() != null) {
					srvPort = Integer.parseInt(tmpEvent.getServicePort(), 10);
				}
				
				service = mSimuMetadataFactory.createService("Unknown",
						"Unknown", srvPort, "Unknown");
			} catch (NumberFormatException | MarshalException e1) {
				logger.error("Error while creating service", e1);
				IfMapClient.criticalError(e1);
			}

			logger.debug("Try search for Service!");
			try {
				SearchRequest req = Requests.createSearchReq(null, 1, null,
						null, null,
						Identifiers.createIp4(tmpEvent.getServiceIp()));
				SearchResult myResult;

				myResult = mSSRC.search(req);

				for (ResultItem current : myResult.getResultItems()) {
					if (logger.isDebugEnabled()) {
						logger.debug("Checking: " + current.getIdentifier1());
					}
					Matcher serviceMatcher = Toolbox.getRegExPattern(
							"regex.service").matcher(
							current.getIdentifier1().toString());
					if (serviceMatcher.find()
							&& serviceMatcher.group(1).equals(
									tmpEvent.getServicePort())) {
						logger.debug("Service found!");
						service = current.getIdentifier1();
						break;
					} else {
						logger.debug("Not a Service!");
					}
				}
			} catch (IfmapErrorResult e) {
				logger.error("Ifmap Errorresult while searching for Service", e);
				IfMapClient.criticalError(e);
			} catch (IfmapException e) {
				logger.error("Ifmap Exception while searching for Service", e);
				IfMapClient.criticalError(e);
			}

			if (tmpEvent.getPublishType().equals(PublishType.DELETE)) {
				PublishDelete pd = null;

				pd = Requests.createPublishDelete();

				pd.setIdentifier1(attackerIpAddress);
				pd.setIdentifier2(service);

				pd.addNamespaceDeclaration(
						SimuMetaDataFactory.SIMU_METADATA_PREFIX,
						SimuMetaDataFactory.SIMU_METADATA_URI);
				pd.setFilter("simu:attackdetected");
				pr.addPublishElement(pd);
			} else if (tmpEvent.getPublishType().equals(PublishType.UPDATE)) {
				PublishUpdate pu = null;
				PublishUpdate pu2 = null;

				pu = Requests.createPublishUpdate();
				pu.setLifeTime(MetadataLifetime.session);

				pu.setIdentifier1(attackerIpAddress);
				pu.setIdentifier2(service);

				pu.addMetadata(mSimuMetadataFactory.createAttackDetected(
						tmpEvent.getType(), tmpEvent.getId(),
						tmpEvent.getSeverity()));

				pr.addPublishElement(pu);
				
				if (tmpEvent.isSendDiscoveredBy()) {
					pu2 = Requests.createPublishUpdate();
					pu2.setLifeTime(MetadataLifetime.session);
					pu2.setIdentifier1(attackerIpAddress);
					pu2.setIdentifier2(device);
					pu2.addMetadata(mBasicMetadataFactory.createDiscoveredBy());
					pr.addPublishElement(pu2);
				}
			}

		}

		if (pr.getPublishElements().size() > 0) {
			publish(pr);
		} else {
			logger.debug("No elements to publish!");
		}
	}

	/**
	 * Adding a PublishRequest from a EventMappingResult
	 * 
	 * @param tmpEvent
	 *            Event to Parse
	 * @param pr
	 *            PublishRequest to add to
	 * @return PublishRequest with included Event
	 */
	private PublishRequest prepareEventMetadataPublushRequest(
			EventMappingResult tmpEvent, PublishRequest pr) {

		Document event = null;

		// create new event from data, other_type_definition and information
		// is currently empty
		event = mBasicMetadataFactory.createEvent(tmpEvent.getName(),
				tmpEvent.getDiscoveredTime(), getIfMapPublisherId(),
				Integer.valueOf(tmpEvent.getMagnitude()),
				Integer.valueOf(tmpEvent.getConfidence()),
				tmpEvent.getSignificance(), tmpEvent.getType(),
				tmpEvent.getOtherTypeDef(), "", tmpEvent.getVulnerabilityUri());

		Identity ident = null;
		if (tmpEvent.getIdentity() != null) {
			ident = Identifiers.createIdentity(IdentityType.aikName,
					tmpEvent.getIdentity());
		}

		// get values for IP-Identifier from mapping-result
		IpAddress ipAddress = createIpAddress(tmpEvent.getIp(),
				tmpEvent.getIpType());
		if (ipAddress == null) {
			logger.warn("Event with no IP found! Skipping Event!");
			return pr;
		}

		if (tmpEvent.getPublishType().equals(PublishType.DELETE)) {
			PublishDelete pd = null;

			pd = Requests.createPublishDelete();

			pd.setIdentifier1(ipAddress);

			pd.addNamespaceDeclaration(IfmapStrings.STD_METADATA_PREFIX,
					IfmapStrings.STD_METADATA_NS_URI);

			if (tmpEvent.getIdentity() != null) {
				pd.setIdentifier2(ident);
			}
			pd.setFilter("meta:event[name='" + tmpEvent.getName() + "']");
			pr.addPublishElement(pd);
		} else if (tmpEvent.getPublishType().equals(PublishType.UPDATE)) {
			// create publish update/notify request and add data to it
			PublishUpdate pu = null;
			PublishUpdate pu2 = null;

			pu = Requests.createPublishUpdate();
			pu.setLifeTime(MetadataLifetime.session);
			pu.setIdentifier1(ipAddress);
			if (tmpEvent.getIdentity() != null) {
				pu.setIdentifier2(ident);
			}
			pu.addMetadata(event);

			pr.addPublishElement(pu);

			if (tmpEvent.isSendDiscoveredBy()) {
				pu2 = Requests.createPublishUpdate();
				pu2.setLifeTime(MetadataLifetime.session);
				pu2.setIdentifier1(ipAddress);
				pu2.setIdentifier2(device);
				pu2.addMetadata(mBasicMetadataFactory.createDiscoveredBy());
				pr.addPublishElement(pu2);
			}
		} else if (tmpEvent.getPublishType().equals(PublishType.NOTIFY)) {
			PublishNotify pn = null;
			PublishUpdate pu = null;

			pn = Requests.createPublishNotify();
			pn.setIdentifier1(ipAddress);
			if (tmpEvent.getIdentity() != null) {
				pn.setIdentifier2(ident);
			}
			pn.addMetadata(event);

			pr.addPublishElement(pn);

			if (tmpEvent.isSendDiscoveredBy()) {
				pu = Requests.createPublishUpdate();
				pu.setLifeTime(MetadataLifetime.session);
				pu.setIdentifier1(ipAddress);
				pu.setIdentifier2(device);
				pu.addMetadata(mBasicMetadataFactory.createDiscoveredBy());
				pr.addPublishElement(pu);
			}
		}

		return pr;
	}

	/**
	 * For publishing enforcement-reports
	 * 
	 * @param resultList
	 *            lsit with enforcementreports
	 */
	private void publishEnforcementReportUpdate(
			EnforcementReportMappingResult[] resultList) {
		logger.debug("publish enforcementreport update!");
		PublishRequest pr = Requests.createPublishReq();
		PublishUpdate pu = null;

		for (EnforcementReportMappingResult current : resultList) {

			if (current.getOtherTypeDefinition() == null) {
				current.setOtherTypeDefinition("");
			}
			if (current.getEnforcementReason() == null) {
				current.setEnforcementReason("");
			}

			IpAddress currentIp = createIpAddress(current.getIp(), "IPv4");

			Document enforcementReport = mBasicMetadataFactory
					.createEnforcementReport(current.getEnforcementAction(),
							current.getOtherTypeDefinition(),
							current.getEnforcementReason());

			if (enforcementReport != null && currentIp != null) {
				pu = Requests.createPublishUpdate();
				pu.setLifeTime(MetadataLifetime.session);
				pu.setIdentifier1(currentIp);
				pu.addMetadata(enforcementReport);
				pr.addPublishElement(pu);
			}
		}

		if (pr.getPublishElements().size() > 0) {
			try {
				publish(pr);
			} catch (MessageSenderError | MessageSenderException e) {
				logger.error("Error while publishing EnforcementReport!");
				IfMapClient.criticalError(e);
			}
		}
	}

	/**
	 * Publishing openvpn data
	 * 
	 * @param resultList
	 *            result list with openvpn data, which is to publish
	 * @throws MessageSenderError
	 * @throws MessageSenderException
	 */
	private void publishOpenVpnData(MappingResult[] resultList)
			throws MessageSenderError, MessageSenderException {
		PublishRequest pr = Requests.createPublishReq();
		logger.debug("publishing openVPN data");
		for (int i = 0; i < resultList.length; i++) {
			OpenVPNMappingResult current = (OpenVPNMappingResult) resultList[i];

			IpAddress ispIp = Identifiers.createIp4(current.getIp());
			IpAddress vpnIp = Identifiers.createIp4(current.getVpnIpAddress());
			AccessRequest myAr = Identifiers.createAr(getIfMapPublisherId()
					+ ":" + current.getARName());
			Identity myIdentity = Identifiers.createIdentity(
					IdentityType.userName, current.getIdentity());
			if (current.getPublishType().equals(PublishType.UPDATE)) {
				PublishUpdate pu = null;

				if (current.isLoginSuccessful()) {
					pu = Requests.createPublishUpdate();
					pu.setLifeTime(MetadataLifetime.session);
					pu.setIdentifier1(vpnIp);
					pu.setIdentifier2(myAr);
					pu.addMetadata(mBasicMetadataFactory.createArIp());

					pr.addPublishElement(pu);
				}

				publishSimuBasicAccessRequest(pr, current, ispIp, myAr,
						myIdentity);
			} else if (current.getPublishType().equals(PublishType.DELETE)) {
				PublishDelete pd = Requests.createPublishDelete();
				pd.addNamespaceDeclaration(IfmapStrings.STD_METADATA_PREFIX,
						IfmapStrings.STD_METADATA_NS_URI);
				pd.setIdentifier1(vpnIp);
				pd.setIdentifier2(myAr);
				pr.addPublishElement(pd);
				deleteSimuBasicAccessRequest(pr, current, ispIp, myAr,
						myIdentity);
			}
		}

		// publish to MAP-Server
		if (pr != null && pr.getPublishElements() != null
				&& pr.getPublishElements().size() > 0) {
			publish(pr);
		} else {
			logger.debug("No elements to publish!");
		}
	}

	/**
	 * * Creating basic PublishUpdates for the SimuBasicAccessRequest
	 * 
	 * @param pr
	 *            PublishUpdates gone be attached into the PublishRequest pr
	 * @param result
	 *            Result with he Data for the PUs
	 * @param ip
	 *            IP Identifier
	 * @param ar
	 *            AccessRequest Identifier
	 * @param identity
	 *            Identity Identifier
	 * @return Login successful
	 */
	private void publishSimuBasicAccessRequest(PublishRequest pr,
			BasicSimuMappingResult result, IpAddress ip, AccessRequest ar,
			Identity identity) {

		PublishUpdate pu = Requests.createPublishUpdate();
		PublishUpdate pu2 = Requests.createPublishUpdate();
		PublishUpdate pu3 = Requests.createPublishUpdate();
		PublishUpdate pu4 = Requests.createPublishUpdate();
		PublishUpdate pu5 = null;

		pu.setLifeTime(MetadataLifetime.session);
		pu.setIdentifier1(ar);
		pu.setIdentifier2(device);
		pu.addMetadata(mBasicMetadataFactory.createAuthBy());

		pu2.setLifeTime(MetadataLifetime.session);
		pu2.setIdentifier1(identity);
		pu2.setIdentifier2(ar);
		pu2.addMetadata(mSimuMetadataFactory.createIdentifiesAs());

		pu3.setLifeTime(MetadataLifetime.session);
		pu3.setIdentifier1(ip);
		pu3.setIdentifier2(ar);
		pu3.addMetadata(mBasicMetadataFactory.createArIp());

		Document login;
		if (result.isLoginSuccessful()) {
			login = mSimuMetadataFactory.createLoginSuccess(
					result.getCredentialType(),
					result.getCedentialOtherTypDef());
		} else {
			login = mSimuMetadataFactory.createLoginFailure(
					result.getCredentialType(),
					result.getLoginFailureReasion(),
					result.getCedentialOtherTypDef(),
					result.getLoginFailureReasionDef());
		}

		pu4.setLifeTime(MetadataLifetime.session);
		pu4.setIdentifier1(ar);
		pu4.setIdentifier2(deviceService);
		pu4.addMetadata(login);

		pr.addPublishElement(pu);
		pr.addPublishElement(pu2);
		pr.addPublishElement(pu3);
		pr.addPublishElement(pu4);

		if (result.isLoginSuccessful()) {
			pu5 = Requests.createPublishUpdate();
			pu5.setLifeTime(MetadataLifetime.session);
			pu5.setIdentifier1(identity);
			pu5.setIdentifier2(ar);
			pu5.addMetadata(mBasicMetadataFactory.createAuthAs());

			pr.addPublishElement(pu5);
		} else {
			result.getDiscoveredTime().add(Calendar.SECOND, gcmttl);
			GarbageCollectorThread.garbageQueue.add(result);
		}
	}

	/**
	 * 
	 * @param pr
	 *            Adding Delete to this PublishRequest
	 * @param current
	 *            SimuRoleAccessRequestMappingResult event with the Information
	 *            for the Delete
	 * @param ip
	 *            IP Identifier
	 * @param ar
	 *            Access Request Identifier
	 * @param identity
	 *            Identity Identifier
	 */
	private void deleteSimuBasicAccessRequest(PublishRequest pr,
			BasicSimuMappingResult current, IpAddress ip, AccessRequest ar,
			Identity identity) {

		PublishDelete pd = null;
		PublishDelete pd2 = null;
		PublishDelete pd3 = null;
		PublishDelete pd4 = null;
		PublishDelete pd5 = null;

		pd = Requests.createPublishDelete();
		pd.addNamespaceDeclaration(IfmapStrings.STD_METADATA_PREFIX,
				IfmapStrings.STD_METADATA_NS_URI);
		pd.setIdentifier1(identity);
		pd.setIdentifier2(ar);
		// pd.setFilter("meta:event[name='" + tmpEvent.getName() + "']");

		pd2 = Requests.createPublishDelete();
		pd2.addNamespaceDeclaration(SimuMetaDataFactory.SIMU_METADATA_PREFIX,
				SimuMetaDataFactory.SIMU_METADATA_URI);
		pd2.setIdentifier1(identity);
		pd2.setIdentifier2(ar);

		pd3 = Requests.createPublishDelete();
		pd3.addNamespaceDeclaration(IfmapStrings.STD_METADATA_PREFIX,
				IfmapStrings.STD_METADATA_NS_URI);
		pd3.setIdentifier1(ip);
		pd3.setIdentifier2(ar);

		pd4 = Requests.createPublishDelete();
		pd4.addNamespaceDeclaration(IfmapStrings.STD_METADATA_PREFIX,
				IfmapStrings.STD_METADATA_NS_URI);
		pd4.setIdentifier1(device);
		pd4.setIdentifier2(ar);

		pd5 = Requests.createPublishDelete();
		pd5.addNamespaceDeclaration(SimuMetaDataFactory.SIMU_METADATA_PREFIX,
				SimuMetaDataFactory.SIMU_METADATA_URI);
		pd5.setIdentifier1(deviceService);
		pd5.setIdentifier2(ar);

		pr.addPublishElement(pd);
		pr.addPublishElement(pd2);
		pr.addPublishElement(pd3);
		pr.addPublishElement(pd4);
		pr.addPublishElement(pd5);
	}

	/**
	 * Publish SimuRoleAccessRequestMappingResults
	 * 
	 * @param resultList
	 *            results with Eventdata, which is to publish
	 * @throws MessageSenderError
	 * @throws MessageSenderException
	 */
	private void publishSimuRoleAccessRequest(MappingResult[] resultList)
			throws MessageSenderError, MessageSenderException {
		PublishRequest pr = Requests.createPublishReq();

		for (int i = 0; i < resultList.length; i++) {
			SimuRoleAccessRequestMappingResult current = (SimuRoleAccessRequestMappingResult) resultList[i];

			IpAddress ip = Identifiers.createIp4(current.getIp());
			AccessRequest ar = Identifiers.createAr(getIfMapPublisherId() + ":"
					+ current.getARName());
			Identity identity = Identifiers.createIdentity(
					IdentityType.aikName, current.getIdentity());

			if (current.getPublishType().equals(PublishType.UPDATE)) {
				PublishUpdate pu6 = null;

				if (current.isLoginSuccessful()) {
					pu6 = Requests.createPublishUpdate();
					pu6.setLifeTime(MetadataLifetime.session);
					pu6.setIdentifier1(identity);
					pu6.setIdentifier2(ar);
					pu6.addMetadata(mBasicMetadataFactory.createRole(current
							.getRole()));

					pr.addPublishElement(pu6);
				}

				publishSimuBasicAccessRequest(pr, current, ip, ar, identity);

			} else if (current.getPublishType().equals(PublishType.DELETE)) {
				deleteSimuBasicAccessRequest(pr, current, ip, ar, identity);
			}
			// publish to MAP-Server
			if (pr != null && pr.getPublishElements() != null
					&& pr.getPublishElements().size() > 0) {
				publish(pr);
			} else {
				logger.debug("No elements to publish!");
			}
		}
	}

	/**
	 * create IpAddress-Object from passed in String
	 * 
	 * @param address
	 *            ip-address string
	 * @return ip-address
	 */
	private IpAddress createIpAddress(String address, String type) {
		if (type.startsWith("IPv6")) {
			return Identifiers.createIp6(address);
		}
		return Identifiers.createIp4(address);
	}

	/**
	 * send passed in publish-request to MAP-Server
	 * 
	 * @param pr
	 *            publish-request to be send
	 */
	private void publish(PublishRequest pr) throws MessageSenderError,
			MessageSenderException {
		try {
			logger.info("Publishing to server!");
			mSSRC.publish(pr);
		} catch (IfmapErrorResult e) {
			logger.error("IfmapErrorResult while publishing!");
			throw new MessageSenderError(e.getMessage(), e.getCause());
		} catch (IfmapException e) {
			logger.error("IfmapException while publishing!");
			throw new MessageSenderException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public void publish(MappingResult[] result, MappingResultType resultType)
			throws MessageSenderError, MessageSenderException {
		switch (resultType) {
		case EVENT:
			publishEventMetaData(result);
			break;
		case ATTACKDETECTED:
			publishSnortAttackDetectedMetaData(result);
			break;
		case ENFORCEMENT_REPORT:
			publishEnforcementReportUpdate((EnforcementReportMappingResult[]) result);
			break;
		case OPENVPN:
			publishOpenVpnData(result);
			break;
		case SIMUROLEACCESSREQUEST:
			publishSimuRoleAccessRequest(result);
			break;
		default:
			logger.warn("Unknown resultType! DO NOTHING!");
			break;
		}

	}

	@Override
	public ArrayList<SearchRequestResult> publishSearchRequest(
			SearchRequestParams request) {

		// build ifmapj-search-request from passed in parameters
		SearchRequest req = Requests.createSearchReq(request.getMatchLinks(),
				request.getMaxDepth(), request.getTerminalIdentifier(),
				request.getMaxSize(), request.getResultFilter(),
				Identifiers.createIp4(request.getStartIpIdentifier()));

		// execute search-request and return the result
		try {
			SearchResult myResult = mSSRC.search(req);
			return transformPollResult(myResult);
		} catch (IfmapErrorResult e) {
			logger.error("IfmapErrorResult while publishing search request!");
			IfMapClient.criticalError(e);
		} catch (IfmapException e) {
			logger.error("IfmapException while publishing search request!");
			IfMapClient.criticalError(e);
		}

		return null;
	}

	@Override
	public void initArcPollingThread() {
		if (pollingThread != null && mSSRC != null) {
			try {
				logger.info("Init ArcPolling Thread");
				pollingThread.setArcChannel(mSSRC.getArc());
			} catch (InitializationException e) {
				logger.error("Error while initialize polling thread!");
				IfMapClient.criticalError(e);
			}
		}
	}

	@Override
	public void startArcPollingThread() {
		logger.debug("Checking for Starting Poll-Thread");
		if (pollingThread != null && !pollingThread.running) {
			pollingThread.running = true;
			pollingThread.pausing = false;

			// add corresponding observer-entity
			pollingThread.addObserver(this);

			logger.info("Starting ArcPolling Thread");

			new Thread(pollingThread).start();

		}
		// sending device with his ip on startup
		sendDeviceIP();
	}

	@Override
	public void sendSubscription(Subscription subscription) {
		if (!this.subscriptions.contains(subscription)) {
			logger.info("Sending Subscription");
			IpAddress myIp = Identifiers.createIp4(subscription.getClientIp());
			SubscribeRequest subsc = Requests.createSubscribeReq((Requests
					.createSubscribeUpdate(subscription.getSubscriptionName(),
							null, 10, "device", null, null, myIp)));
			try {
				mSSRC.subscribe(subsc);
				subscriptions.add(subscription);
			} catch (IfmapErrorResult e) {
				logger.error("IfmapErrorResult while sending subscription!");
				IfMapClient.criticalError(e);
			} catch (IfmapException e) {
				logger.error("IfmapException while sending subscription!");
				IfMapClient.criticalError(e);
			}
		}

		else {
			logger.debug("Not sending subscription because its already there...");
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o != null && pollResultProcessor != null) {
			PollResult tmpResult = (PollResult) arg;
			logger.info("INCOMMMING POLL RESULT!!!...checking");
			lastPollResult = tmpResult;

			pollResultProcessor.processPollResult(getLastPollResult());

		}

		pollingThread.running = true;
		pollingThread.pausing = false;

	}

	@Override
	public ArrayList<SearchRequestResult> getLastPollResult() {
		ArrayList<SearchRequestResult> searchRequestResults = new ArrayList<>();
		if (this.lastPollResult != null
				&& this.lastPollResult.getResults().size() > 0) {
			for (SearchResult current : this.lastPollResult.getResults()) {
				searchRequestResults.addAll(transformPollResult(current));
			}
			return searchRequestResults;
		} else {
			logger.info("No poll result!");
		}

		return null;
	}

	@Override
	public ArrayList<SearchRequestResult> transformPollResult(
			SearchResult searchResult) {
		ArrayList<SearchRequestResult> searchRequestResults = new ArrayList<>();

		if (searchResult != null) {
			for (ResultItem current : searchResult.getResultItems()) {

				SearchRequestResult currentSearchResultItem = new SearchRequestResult();

				if (current.getIdentifier1() != null) {
					currentSearchResultItem.setIdentifier1(current
							.getIdentifier1().toString());
				}

				if (current.getIdentifier2() != null) {
					currentSearchResultItem.setIdentifier2(current
							.getIdentifier2().toString());
				}

				// get metadata
				if (current.getMetadata() != null) {

					List<HashMap<String, String>> metadaMapList = new ArrayList<>();

					// get current metadata-nodes
					for (Document doc : current.getMetadata()) {

						NodeList nodes = doc.getChildNodes();

						for (int i = 0; i < nodes.getLength(); i++) {
							HashMap<String, String> metadataMap = new HashMap<>();

							// type of metadata
							metadataMap.put("metadatatype", nodes.item(i)
									.getLocalName());

							// add all metadata to map
							NodeList childs = nodes.item(i).getChildNodes();
							for (int j = 0; j < childs.getLength(); j++) {
								metadataMap.put(childs.item(j).getLocalName(),
										childs.item(j).getTextContent());
							}

							metadaMapList.add(metadataMap);

						}
						currentSearchResultItem.setMetadata(metadaMapList);
					}
				}
				searchRequestResults.add(currentSearchResultItem);
			}
		}
		return searchRequestResults;
	}

	/**
	 * Publishing device information
	 */
	private void sendDeviceIP() {
		try {
			logger.debug("Sending device informations!");
			PublishRequest pr = Requests.createPublishReq();
			PublishUpdate pu = Requests.createPublishUpdate();
			PublishUpdate pu2 = Requests.createPublishUpdate();

			device = Identifiers.createDev(mSSRC.getPublisherId());
			deviceIP = Identifiers.createIp4(ipAddress);

			pu.setLifeTime(MetadataLifetime.session);
			pu.setIdentifier1(device);
			pu.setIdentifier2(deviceIP);
			pu.addMetadata(mBasicMetadataFactory.createDevIp());
			pr.addPublishElement(pu);

			if (isService) {
				deviceService = mSimuMetadataFactory.createService(serviceType,
						serviceName, port, ad);

				pu2.setLifeTime(MetadataLifetime.session);
				pu2.setIdentifier1(deviceService);
				pu2.setIdentifier2(deviceIP);
				pu2.addMetadata(mSimuMetadataFactory.createServiceIP());
				pr.addPublishElement(pu2);
			}

			publish(pr);

			// publishRequest();
		} catch (MessageSenderError e) {
			logger.error("MessageSenderError while sending device informations!");
			IfMapClient.criticalError(e);
		} catch (MessageSenderException e) {
			logger.error("MessageSenderException while sending device informations!");
			IfMapClient.criticalError(e);
		} catch (MarshalException e) {
			logger.error("Error while creating service", e);
			IfMapClient.criticalError(e);
		}
	}

}