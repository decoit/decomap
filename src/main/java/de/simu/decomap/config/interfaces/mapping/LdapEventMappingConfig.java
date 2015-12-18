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
package de.simu.decomap.config.interfaces.mapping;

import org.aeonbits.owner.Config.Sources;

@Sources("file:${filename}")
public interface LdapEventMappingConfig extends BasicSimuMappingConfig{

	@Key("application.ldap.url")
	String ldapURL();

	@Key("application.ldap.auth")
	String ldapAuth();

	@Key("application.ldap.username")
	String ldapUsername();

	@Key("application.ldap.password")
	String ldapPassword();

	@Key("application.ldap.search")
	String ldapSearch();

	@Key("application.ldap.attribute")
	String ldapAttribute();
}
