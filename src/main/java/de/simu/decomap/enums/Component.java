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
package de.simu.decomap.enums;

import com.google.inject.Injector;
import com.google.inject.Guice;

import de.simu.decomap.component.DataSourceComponent;
import de.simu.decomap.component.modules.IcingaRestPollingModule;
import de.simu.decomap.component.modules.IpTablesFilePollingModule;
import de.simu.decomap.component.modules.NagiosSocketPollingModule;
import de.simu.decomap.component.modules.OpenVPNFilePollingModule;
import de.simu.decomap.component.modules.RadiusFilePollingModule;
import de.simu.decomap.component.modules.SnortAttackDetectedFilePollingModule;
import de.simu.decomap.component.modules.SnortFilePollingModule;
import de.simu.decomap.component.modules.LdapFilePollingModule;

/**
 * Enum for each Module Injecting components with the module
 * 
 * @author Leonid Schwenke, DECOIT GmbH
 * 
 */
public enum Component {
	SNORT_ASCIILOG(Guice.createInjector(new SnortFilePollingModule())), SNORT_AD_FILE(
			Guice.createInjector(new SnortAttackDetectedFilePollingModule())), NAGIOS_SOCKET(
			Guice.createInjector(new NagiosSocketPollingModule())), OPENVPN_FILE(
			Guice.createInjector(new OpenVPNFilePollingModule())), IPTABLES_FILE(
			Guice.createInjector(new IpTablesFilePollingModule())), RADIUS_FILE(
			Guice.createInjector(new RadiusFilePollingModule())), LDAP_FILE(
			Guice.createInjector(new LdapFilePollingModule())), ICINGA_REST(
			Guice.createInjector(new IcingaRestPollingModule()));

	public Injector injector;

	/**
	 * constructor
	 * 
	 * @param guiceInjector
	 *            injector
	 */
	private Component(Injector guiceInjector) {
		this.injector = guiceInjector;
	}

	/**
	 * 
	 * @return DataSourceComponent with injected module
	 */
	public DataSourceComponent getDataSourceComponent() {
		return injector.getInstance(DataSourceComponent.class);
	}
}