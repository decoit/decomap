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
package de.simu.decomap.component.modules;

import com.google.inject.AbstractModule;
import com.google.inject.util.Providers;

import de.simu.decomap.component.mapping.MappingInterface;
import de.simu.decomap.component.mapping.impl.NagiosEventMapper;
import de.simu.decomap.component.polling.PollingThread;
import de.simu.decomap.component.polling.impl.NagiosSocketPollingThread;
import de.simu.decomap.enums.MappingResultType;
import de.simu.decomap.messaging.pollingthread.SubscriptionPollingThread;
import de.simu.decomap.messaging.resultprocessor.PollResultProcessor;
import de.simu.decomap.messaging.sender.IFMAPJMessageSender;
import de.simu.decomap.messaging.sender.MessageSender;

/**
 * Injection of concrete classes which are necessary for the module
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 * 
 */
public class NagiosSocketPollingModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(PollingThread.class).to(NagiosSocketPollingThread.class);
		bind(MappingInterface.class).to(NagiosEventMapper.class);
		bind(MappingResultType.class).toInstance(MappingResultType.EVENT);

		bind(MessageSender.class).to(IFMAPJMessageSender.class);
		bind(SubscriptionPollingThread.class).toProvider(Providers.<SubscriptionPollingThread> of(null));
		bind(PollResultProcessor.class).toProvider(Providers.<PollResultProcessor> of(null));
	}
}
