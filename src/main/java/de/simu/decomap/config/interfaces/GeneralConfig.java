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
package de.simu.decomap.config.interfaces;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources("file:config/${filename}")
public interface GeneralConfig extends Config {

	// /////////////////////////////////
	// APPLICATION-SPECIFIC-SETTINGS //
	// /////////////////////////////////

	@Key("application.version")
	String applicationVersion();

	@Key("application.polling.interval")
	@DefaultValue("20")
	int applicationPollingInterval();

	@Key("application.messaging.sendold")
	@DefaultValue("false")
	boolean applicationSendOldEvents();

	@Key("application.ipaddress")
	String applicationIpAddress();

	@Key("application.servicetype")
	String applicationServiceType();

	@Key("application.servicename")
	String applicationServiceName();

	@Key("application.administrativdomain")
	String applicationAdministrativDomain();

	@Key("application.serviceport")
	Integer applicationServicePort();
	
	@Key("application.isservice")
	@DefaultValue("false")
	boolean isService();
	

	// ////////////////////////////////////////
	// POLLING/MAPPING-COMPONENT TO BE USED //
	// ////////////////////////////////////////

	@Key("application.component")
	String applicationComponent();


	@Key("application.pollingconfig.path")
	String pollingComponentConfigPath();

	@Key("application.mappingconfig.path")
	String mappingComponentConfigPath();

	@Key("application.regexconfig.path")
	String regexComponentConfigPath();

	@Key("application.pollresultfilterconfig.path")
	String pollResultFilterConfigPath();

	// ////////////////////////////////
	// MAP-SERVER-SPECIFIC-SETTINGS //
	// ////////////////////////////////

	@Key("mapserver.url")
	String mapServerUrl();

	@Key("mapserver.keystore.path")
	String mapServerKeystorePath();

	@Key("mapserver.keystore.password")
	String mapServerKeystorePassword();

	@Key("mapserver.truststore.path")
	String mapServerTruststorePath();

	@Key("mapserver.truststore.password")
	String mapServerTruststorePassword();

	@Key("mapserver.basicauth.enabled")
	boolean mapServerBasicAuthEnabled();

	@Key("mapserver.basicauth.user")
	String mapServerbasicAuthUser();

	@Key("mapserver.basicauth.password")
	String mapServerbasicAuthPassword();

}