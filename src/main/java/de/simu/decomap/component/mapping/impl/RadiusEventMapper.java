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
import de.simu.decomap.component.mapping.MappingInterface;
import de.simu.decomap.component.mapping.result.MappingResult;
import de.simu.decomap.component.mapping.result.SimuRoleAccessRequestMappingResult;
import de.simu.decomap.component.polling.impl.helper.RoleItem;
import de.simu.decomap.config.interfaces.GeneralConfig;
import de.simu.decomap.enums.PublishType;
import de.simu.decomap.util.Toolbox;

/**
 * Mapping-Class for converting Results from RadiusFilePollingThread to
 * IF-MAP-Results
 * 
 * @author Leonid Schwenke, DECOIT GmbH
 */
public class RadiusEventMapper implements MappingInterface {

	private int legalTimeDifferenz;

	private int accessRequestCount = 0;

	// aktive Roles
	private HashMap<String, RoleItem> roleItems = new HashMap<String, RoleItem>();
	private HashMap<String, SimuRoleAccessRequestMappingResult> eventsWithoutRole = new HashMap<String, SimuRoleAccessRequestMappingResult>();

	// active connections
	private HashMap<String, SimuRoleAccessRequestMappingResult> activeConnections = new HashMap<String, SimuRoleAccessRequestMappingResult>();

	private final Calendar calender = Toolbox.getCalendarFromString(
			Toolbox.clientStartTime, "yyyy-MM-dd HH:mm:ss", null);

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init(GeneralConfig mainConfig) {
		logger.info("Initilizing RadiusEventMapper");

		legalTimeDifferenz = mainConfig.applicationPollingInterval() * 2000;
	}

	@Override
	public MappingResult[] getMappingResult(List<HashMap<String, String>> data,
			String publisherId) {

		ArrayList<MappingResult> results = new ArrayList<MappingResult>();

		for (int i = 0; i < data.size(); i++) {
			HashMap<String, String> temp = data.get(i);
			for (int j = 1; j <= temp.size(); j++) {
				String line = temp.get(String.valueOf(j));
				if (line != null) {
					if (logger.isDebugEnabled()) {
						logger.debug("Mapping line: "
								+ temp.get(String.valueOf(j)));
					}
					Calendar date = null;
					Matcher dateMatcher = Toolbox.getRegExPattern("regex.date")
							.matcher(line);
					Date datum = null;
					if (dateMatcher.find()) {
						try {
							datum = Toolbox.dateFormat.parse(dateMatcher
									.group());
							date = Toolbox.getCalendarFromString(
									Toolbox.calenderFormat.format(datum),
									Toolbox.calenderFormat.toPattern(), null);
						} catch (ParseException e) {
							logger.error("Parse exception on date");
							continue;
						}

						if (date.compareTo(calender) >= 0) {

							if (!searchLoginIncorrect(line, date, results)) {
								if (!searchConnection(line, date, results)) {
									if (!searchRole(line, date, results)) {
										if (logger.isDebugEnabled()) {
											logger.debug("can't map line!");
										}
										continue;
									}
								}
							}
						}
					}
				}
			}
		}

		return results.toArray(new MappingResult[0]);
	}

	/**
	 * 
	 * @param line
	 * @param date
	 * @param results
	 * @return true if matcher found something and login is incorrect or an
	 *         error occurred(no ip is found)
	 */
	private boolean searchLoginIncorrect(String line, Calendar date,
			ArrayList<MappingResult> results) {
		if (logger.isDebugEnabled()) {
			logger.debug("Searching for incorrect login");
		}
		Matcher typeMatcher = Toolbox.getRegExPattern("regex.defaultlogin")
				.matcher(line);
		if (typeMatcher.find()) {

			if (logger.isDebugEnabled()) {
				logger.debug("Creating new Event");
			}

			SimuRoleAccessRequestMappingResult event = new SimuRoleAccessRequestMappingResult();

			event.setDiscoveredTime(date);

			//String content = typeMatcher.group();
			event.setIdentity(typeMatcher.group(1));

			if (logger.isDebugEnabled()) {
				logger.debug("Identity found: " + event.getIdentity());
			}

			// find & set IP
			Matcher ipMatcher = Toolbox.getRegExPattern("regex.ip4").matcher(
					line.replace("localhost", "127.0.0.1"));
			if (ipMatcher.find()) {
				event.setIp(ipMatcher.group());
				event.setIpType("IPv4");
			} else {
				logger.warn("No IP found!");
				return true;
			}

			event.setName("Access-Request: " + accessRequestCount);

			// TODO: gibt es andere?
			event.setCredentialType(CredentialType.PASSWORD);
			event.setLoginFailureReasion(LoginFailureReason.INVALID_CREDENTIALS);

			if (logger.isDebugEnabled()) {
				logger.debug("Event with Login incorrect!");
				logger.debug("Sending Event: " + event.getARName());
			}

			accessRequestCount++;
			results.add(event);
			return true;

		}
		if (logger.isDebugEnabled()) {
			logger.debug("No incorrect login found!");
		}
		return false;
	}

	/**
	 * 
	 * @param line
	 * @param date
	 * @param results 
	 * @return true if matcher found something and login is incorrect or an
	 *         error occurred(no ip is found)
	 */
	private boolean searchConnection(String line, Calendar date,
			ArrayList<MappingResult> results) {

		if (logger.isDebugEnabled()) {
			logger.debug("Searching for connection events");
		}
		Matcher typeMatcher = Toolbox.getRegExPattern("regex.acctstatus")
				.matcher(line);
		if (typeMatcher.find()) {
			SimuRoleAccessRequestMappingResult event;
			String content = typeMatcher.group(1);
			String uid;

			typeMatcher = Toolbox.getRegExPattern("regex.acctuniqueid")
					.matcher(line);
			if (typeMatcher.find()) {
				uid = typeMatcher.group(1);
			} else {
				logger.warn("No Unique ID found! Skipping Event!");
				return true;
			}

			if (content.endsWith("Start")) {
				event = new SimuRoleAccessRequestMappingResult();
				event.setDiscoveredTime(date);
				event.setName("Access-Request: " + accessRequestCount);
				event.setCredentialType(CredentialType.PASSWORD);

				typeMatcher = Toolbox.getRegExPattern("regex.nasipv4").matcher(
						line);
				if (typeMatcher.find()) {
					event.setIp(typeMatcher.group(1));
					event.setIpType("IPv4");
				} else {
					logger.warn("No Nas-IP found! Skipping Event!");
					return true;
				}

				typeMatcher = Toolbox.getRegExPattern("regex.username")
						.matcher(line);
				if (typeMatcher.find()) {
					event.setIdentity(typeMatcher.group(1));
				} else {
					logger.warn("No username found! Skipping Event!");
					return true;
				}

				event.setLoginSuccess(true);
				if (logger.isDebugEnabled()) {
					logger.debug("New successful connection found!");
				}

				RoleItem role;
				if ((role = roleItems.get(event.getIdentity())) != null) {
					roleItems.remove(event.getIdentity());
					Calendar roleTime = null;
					roleTime = role.getDate();

					if ((date.getTimeInMillis() - roleTime.getTimeInMillis()) <= legalTimeDifferenz) {
						event.setRole(role.getRole());
						if (logger.isDebugEnabled()) {
							logger.debug("Role found: " + event.toString());
							logger.debug("Adding Event: " + event.getARName());
						}

						results.add(event);

					} else {
						if (logger.isDebugEnabled()) {
							logger.debug("Found Event for Role \""
									+ role.getRole()
									+ "\" is to old. Droping them!");
						}
						eventsWithoutRole.put(event.getIdentity(), event);
					}

				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("No Role found! Waiting for Role for Event: "
								+ event.getARName());
					}
					eventsWithoutRole.put(event.getIdentity(), event);
				}
				accessRequestCount++;
				activeConnections.put(uid, event);
			} else if (content.endsWith("Stop")) {
				event = activeConnections.get(uid);
				if (event == null) {
					logger.warn("Found not existing or already closed session id: "
							+ uid);
					return true;
				}
				logger.info("Sesson closed: " + uid);
				activeConnections.remove(uid);
				event.setPublishType(PublishType.DELETE);
				results.add(event);

			} else {
				logger.warn("Unknown " + content);
			}
			return true;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("No connection events found!");
		}
		return false;
	}

	/**
	 * 
	 * @param line
	 * @param date
	 * @param results
	 * @return true if matcher found something and login is incorrect or an
	 *         error occurred(no ip is found)
	 */
	private boolean searchRole(String line, Calendar date,
			ArrayList<MappingResult> results) {
		if (logger.isDebugEnabled()) {
			logger.debug("Searching for Role");
		}
		Matcher typeMatcher = Toolbox.getRegExPattern("regex.role").matcher(
				line);
		if (typeMatcher.find()) {
			String identity = typeMatcher.group(2);
			SimuRoleAccessRequestMappingResult event;

			if ((event = eventsWithoutRole.get(identity)) != null) {
				eventsWithoutRole.remove(identity);
				Calendar eventTime = null;
				eventTime = event.getDiscoveredTime();
				if ((date.getTimeInMillis() - eventTime.getTimeInMillis()) <= legalTimeDifferenz) {

					if (logger.isDebugEnabled()) {
						logger.debug("Role found for event: "
								+ event.getARName());
						logger.debug("Sending Event: " + event.getARName());
					}
					event.setRole(typeMatcher.group(1));
					results.add(event);
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("Found role for event \""
								+ event.getARName()
								+ "\" is to old. Droping them!");
					}
					roleItems.put(identity, new RoleItem(typeMatcher.group(1),
							date));
				}

			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("No event for role: " + typeMatcher.group(1));
				}
				roleItems.put(identity,
						new RoleItem(typeMatcher.group(1), date));
			}
			return true;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("No role found");
		}
		return false;
	}

}
