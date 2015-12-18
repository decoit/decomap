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

public class MessageSenderException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1631157081251862905L;

	public MessageSenderException() {
	}

	public MessageSenderException(String msg) {
		super(msg);
	}

	public MessageSenderException(Throwable cause) {
		super(cause);
	}

	public MessageSenderException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
