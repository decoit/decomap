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

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

/**
 * Client for REST-connection
 * 
 * @author Leonid Schwenke, DECOIT GmbH
 *
 */
public class RestClient {//extends RestTemplate {

	/**
	 * Standard contruktor for a normal RestTemplate object without an
	 * authentification
	 */
	public RestClient() {
	}

	/**
	 * 
	 * @param username
	 *            The username for the authentification
	 * @param password
	 *            The password for the authentification
	 */
	public RestClient(String username, String password) {
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(username, password));
//		HttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
		//setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
	}
}