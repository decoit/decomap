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

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.simu.decomap.main.IfMapClient;

/**
 * configure the rest connection and getting data over rest connection from a
 * Icingaserver.
 * 
 * @version 0.2
 * @author Leonid Schwenke, DECOIT GmbH
 */
public class IcingaDataConnectionRest {

	// rest settings
	private boolean onlyFileData = false;
	private String ip;
	private String file;
	private String path;
	private String username;
	private String password;
	private ArrayList<String> filters = new ArrayList<String>();
	private RestClient restClient = new RestClient();

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * constructor
	 * 
	 * @param ip
	 *            ip of icinga server
	 * 
	 * @param file
	 *            cgi file which should be read
	 * 
	 * @param path
	 *            path to the cgi files
	 */
	public IcingaDataConnectionRest(final String ip, final String file,
			final String path, final String username, final String password) {
		logger.info("Creating Icinga Data REST Connection");
		this.ip = ip;
		this.file = file;
		this.path = path;
		this.username = username;
		this.password = password;
	}

	/**
	 * Setting the client for connection. client can include an authentification
	 * for the server
	 * 
	 * @param restClient
	 *            client for connection
	 */
	public void setRestClient(RestClient restClient) {
		if (restClient != null) {
			this.restClient = restClient;
		}
	}

	/**
	 * 
	 * @return client for connection
	 */
	public RestClient getRestClient() {
		return restClient;
	}

	/**
	 * 
	 * @param ip
	 *            ip of icinga server
	 */
	public void setIp(final String ip) {
		this.ip = ip;
	}

	/**
	 * 
	 * @return ip of icinga server
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * 
	 * @param file
	 *            cgi file which should be read
	 */
	public void setFile(final String file) {
		this.file = file;
	}

	/**
	 * 
	 * @return cgi file which should be read
	 */
	public String getFile() {
		return file;
	}

	/**
	 * 
	 * @return all set filters
	 */
	public ArrayList<String> getFilters() {
		return filters;
	}

	/**
	 * clearing all filters
	 */
	public void resetFilters() {
		filters.clear();
	}

	/**
	 * Add an filter for the request
	 * 
	 * @param filterOption
	 *            filter option
	 * 
	 * @param value
	 *            value of the option
	 */
	public void addFilter(final String filterOption, final String value) {
		filters.add(filterOption + "=" + value);
	}

	/**
	 * set a list of filter
	 * 
	 * @param filters
	 *            list of filters
	 */
	public void setFilters(final ArrayList<String> filters) {
		if (filters != null) {
			this.filters = filters;
		}
	}

	/**
	 * should getdate() only returning data from the cgi file without all the
	 * icingaserver infos
	 * 
	 * @param onlyFileData
	 *            should only the file date be returned
	 */
	public void setOnlyFileData(final boolean onlyFileData) {
		this.onlyFileData = onlyFileData;
	}

	/**
	 * 
	 * @return should getdate() only returning data from the cgi file without
	 *         all the icingaserver infos
	 */
	public boolean getOnlyFileData() {
		return onlyFileData;
	}

	/**
	 * 
	 * @param path
	 *            path to the cgi files on the icingaserver
	 */
	public void setPath(final String path) {
		this.path = path;
	}

	/**
	 * 
	 * @return path to the cgi files on the icingaserver
	 */
	public String getPath() {
		return path;
	}

	/**
	 * preparing rest request and getting data
	 * 
	 * @return data from rest request
	 */
	public String getData() {
		try {
			StringBuilder url = new StringBuilder();

			url.append("http://" + ip + path + file + ".cgi?");
			for (String filter : filters) {
				url.append("&" + filter);
			}
			url.append("&jsonoutput");

			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(ip, AuthScope.ANY_PORT),
					new UsernamePasswordCredentials(username, password));

			CloseableHttpClient httpClient = HttpClientBuilder.create()
					.setDefaultCredentialsProvider(credsProvider).build();

			// specify the get request
			HttpGet getRequest = new HttpGet(url.toString());

			CloseableHttpResponse httpResponse = httpClient.execute(getRequest);
			HttpEntity entity = httpResponse.getEntity();

			if (entity != null) {
				return EntityUtils.toString(entity);
			}
		} catch (ParseException | IOException e) {
			logger.error("Error while getting data over rest", e);
			IfMapClient.criticalError(e);
		}

		return null;
	}

}
