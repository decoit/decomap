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
package de.simu.decomap.component.mapping.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.decoit.simumetadata.CredentialType;
import de.decoit.simumetadata.LoginFailureReason;
import de.simu.decomap.util.Toolbox;
import de.simu.decomap.component.mapping.MappingInterface;
import de.simu.decomap.component.mapping.result.MappingResult;
import de.simu.decomap.component.mapping.result.SimuRoleAccessRequestMappingResult;
import de.simu.decomap.component.polling.impl.helper.LdapResultPuller;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.config.interfaces.mapping.LdapEventMappingConfig;
import de.simu.decomap.enums.PublishType;

/**
 * Mapping-Class for converting Results from LdapFilePollingThread to
 * IF-MAP-Results
 * 
 * @author Leonid Schwenke
 */
public class LdapEventMapper implements MappingInterface {

	private HashMap<String, SimuRoleAccessRequestMappingResult> openResults = new HashMap<String, SimuRoleAccessRequestMappingResult>();

	private LdapResultPuller ldapPuller;
	private HashMap<String, String> roles;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init(GeneralConfig mainConfig) {
		logger.info("Initilizing LdapEventMapper");
		LdapEventMappingConfig ldapMappingConfiguration = (LdapEventMappingConfig) Toolbox
				.loadConfig(mainConfig.mappingComponentConfigPath(),
						LdapEventMappingConfig.class);

		ldapPuller = new LdapResultPuller(ldapMappingConfiguration);
		roles = ldapPuller.pullRole();
	}

	@SuppressWarnings("deprecation")
	@Override
	public MappingResult[] getMappingResult(List<HashMap<String, String>> data,
			String publisherId) {

		MappingResult[] result = null;

		ArrayList<String> sendConns = new ArrayList<String>();
		Date datum = null;
		Calendar date = null;

		SimuRoleAccessRequestMappingResult event;

		for (HashMap<String, String> temp : data) {

			event = new SimuRoleAccessRequestMappingResult();

			String line = temp.get("0");

			if (logger.isDebugEnabled()) {
				logger.debug("Start map line: " + line);
			}

			Matcher dateMatcher = Toolbox.getRegExPattern("regex.date")
					.matcher(line);
			if (dateMatcher.find()) {
				try {
					datum = Toolbox.dateFormatNoYearNoDay.parse(dateMatcher
							.group());
					// immer festes Jahr :D
					datum.setYear(Calendar.getInstance().get(Calendar.YEAR) - 1900);

					date = Toolbox.getCalendarFromString(
							Toolbox.calenderFormat.format(datum),
							Toolbox.calenderFormat.toPattern(), null);
				} catch (ParseException e) {
					logger.error("Parse exception on date", e);
					continue;
				}
				Matcher connection = Toolbox
						.getRegExPattern("regex.connection").matcher(line);

				String conn = null;
				if (connection.find()) {

					conn = connection.group(1);

					// example line:
					// conn=1064 fd=17 ACCEPT from
					// IP=10.10.100.31:51257 (IP=0.0.0.0:389)
					Matcher message = Toolbox.getRegExPattern("regex.msg")
							.matcher(line);
					if (message.find()) {
						event.setDiscoveredTime(date);
						event.setName("connection " + conn + " "
								+ message.group(1));

						Matcher ipMatcher = Toolbox
								.getRegExPattern("regex.ip4").matcher(
										line.replace("localhost", "127.0.0.1"));
						if (ipMatcher.find()) {
							event.setIp(ipMatcher.group());
							event.setIpType("IPv4");
							event.setClientIpAddress(event.getIp());
						}
						event.setPublishType(PublishType.UPDATE);


						openResults.put(conn, event);
					} else {

						Matcher close = Toolbox.getRegExPattern("regex.closed")
								.matcher(line);

						// example line:
						// conn=1064 fd=17 closed
						if (close.find()) {
							if (openResults.get(conn) != null) {
								event = openResults.get(conn);

								if (event.isLoginSuccessful()) {
									event.setPublishType(PublishType.DELETE);
									if (sendConns.contains(conn)) {
										sendConns.remove(conn);
										openResults.remove(conn);
									} else {
										sendConns.add(conn);
										if (logger.isDebugEnabled()) {
											logger.debug("Adding delete event: "
													+ event.toString());
										}
									}

								}
							}
						}
					}

				} else {
					Matcher connOperation = Toolbox.getRegExPattern(
							"regex.conop").matcher(line);
					if (connOperation.find()) {

						// Example lines:
						// op=0 BIND
						// dn="cn=admin,dc=decoit,dc=de"
						// method=128
						// May 12 13:42:51 debian slapd[2399]:
						// conn=1063 op=0 BIND
						// dn="cn=admin,dc=decoit,dc=de"
						// mech=SIMPLE ssf=0
						// May 12 13:42:51 debian slapd[2399]:
						// conn=1063 op=0 RESULT tag=97 err=0
						// text=

						conn = connOperation.group(1);

						if (openResults.get(conn) != null) {
							event = openResults.get(conn);

							Matcher identity = Toolbox.getRegExPattern(
									"regex.bind").matcher(line);
							if (identity.find()) {
								event.setIdentity(identity.group(1));
							} else {
								Matcher success = Toolbox.getRegExPattern(
										"regex.success").matcher(line);
								if (success.find()) {
									// ggf type in den regex einbeziehen???
									event.setLoginSuccess(success.group(1)
											.equals("0"));

									// TODO: aus code auslesen!!!! codes? type
									// zu other & simple etc????
									event.setCredentialType(CredentialType.PASSWORD);
									if (!event.isLoginSuccessful()) {
										if (success.group(1).equals("49")) {
											event.setLoginFailureReasion(LoginFailureReason.INVALID_CREDENTIALS);
										} else {
											event.setLoginFailureReasion(LoginFailureReason.OTHER);
											event.setLoginFailureReasionDef("LDAP error: "
													+ success.group(1));
										}
									} else {

										// rolle setzen und Event abschließen
										if (!roles.containsKey(event
												.getIdentity())) {
											roles = ldapPuller.pullRole();
											if (!roles.containsKey(event
													.getIdentity())) {
												roles.put(event.getIdentity(),
														"none");
											}
										}
										event.setRole(roles.get((event
												.getIdentity())));
									}
									if (!sendConns.contains(conn)) {
										sendConns.add(conn);
										if (logger.isDebugEnabled()) {
											logger.debug("Adding event: "
													+ event.toString());
										}
									}
								}
							}

						}
					}
				}

			}
		}
		result = new MappingResult[sendConns.size()];
		for (int i = 0; i < sendConns.size(); i++) {
			event = openResults.get(sendConns.get(i));
			if (!event.isLoginSuccessful()) {
				openResults.remove(sendConns.get(i));
			}
			result[i] = event;
		}

		return result;
	}
}
