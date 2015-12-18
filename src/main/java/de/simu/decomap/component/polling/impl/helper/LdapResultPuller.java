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
package de.simu.decomap.component.polling.impl.helper;

import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simu.decomap.config.interfaces.mapping.LdapEventMappingConfig;
import de.simu.decomap.main.IfMapClient;

/**
 * herlperclass for the Ldap log-File poller
 * 
 * @version 0.2
 * @author Leonid Schwenke, DECOIT GmbH
 */
public class LdapResultPuller {

	private Hashtable<String, String> env = new Hashtable<String, String>();
	private LdapEventMappingConfig ldapMappingConfiguration;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * constructor
	 * 
	 * @param ldapMappingConfiguration
	 *            configuration settings
	 */
	public LdapResultPuller(LdapEventMappingConfig ldapMappingConfiguration) {
		this.ldapMappingConfiguration = ldapMappingConfiguration;
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapMappingConfiguration.ldapURL());
		env.put(Context.SECURITY_AUTHENTICATION, ldapMappingConfiguration.ldapAuth());
		if (ldapMappingConfiguration.ldapUsername() != null) {
			env.put(Context.SECURITY_PRINCIPAL, ldapMappingConfiguration.ldapUsername());
			env.put(Context.SECURITY_CREDENTIALS, ldapMappingConfiguration.ldapPassword());
		}
	}

	/**
	 * Get roles from LDAP
	 * 
	 * @return list with roles
	 */
	public HashMap<String, String> pullRole() {
		return pullResult(ldapMappingConfiguration.ldapSearch(), ldapMappingConfiguration.ldapAttribute());
	}

	/**
	 * Search for given attributes
	 * 
	 * @param search
	 *            where to search
	 * @param attribute
	 *            what to search
	 * @return search result from search
	 */
	public HashMap<String, String> pullResult(String search, String attribute) {
		HashMap<String, String> result = new HashMap<String, String>();
		DirContext ctx = null;
		NamingEnumeration<?> results = null;
		try {
			logger.info("pulling Ldap Data");
			ctx = new InitialDirContext(env);
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			results = ctx.search("", search, controls);
			while (results.hasMore()) {
				SearchResult searchResult = (SearchResult) results.next();
				Attributes attributes = searchResult.getAttributes();
				result.put(searchResult.getNameInNamespace(), (String) attributes.get(attribute).get());
			}
		} catch (Throwable e) {
			logger.error("error on data pull");
			IfMapClient.criticalError(e);
		} finally {
			if (results != null) {
				try {
					results.close();
				} catch (Exception e) {
					logger.error("error on closing connection!");
					IfMapClient.criticalError(e);
				}
			}
			if (ctx != null) {
				try {
					ctx.close();
				} catch (Exception e) {
					logger.error("error on closing connection!");
					IfMapClient.criticalError(e);
				}
			}
		}
		return result;
	}

}
