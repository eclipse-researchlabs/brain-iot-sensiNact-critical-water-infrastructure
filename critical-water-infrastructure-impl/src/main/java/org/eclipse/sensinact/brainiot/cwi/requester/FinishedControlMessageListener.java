/*
 * Copyright (c) 2020-2021 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
*/
package org.eclipse.sensinact.brainiot.cwi.requester;

import org.eclipse.sensinact.brainiot.cwi.api.FinishedControlRequest;
import org.eclipse.sensinact.brainiot.cwi.connector.NodeREDConnector;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.SmartBehaviour;

@Component(immediate=true, service= {SmartBehaviour.class})
@SmartBehaviourDefinition(consumed = {FinishedControlRequest.class}, filter="(timestamp=*)", name="Finish Control Listener")
public class FinishedControlMessageListener implements SmartBehaviour<FinishedControlRequest> {

	private static final Logger LOG = LoggerFactory.getLogger(FinishedControlMessageListener.class);
	
	private NodeREDConnector connector;

	
	@Activate
	public void activate() {
		this.connector = new NodeREDConnector();
		LOG.info("ACTIVATING RESET MESSAGE LISTENER");
	}
	
	@Deactivate
	public void deactivate() {
		LOG.info("DEACTIVATING RESET MESSAGE LISTENER");
	}

	@Override
	public void notify(FinishedControlRequest event) {
		LOG.debug(String.format("FinishedControlMessageListener receives : %s",event.toString()));
		this.connector.notifyIAControlFinished(event.deviceIds);
	}
}
