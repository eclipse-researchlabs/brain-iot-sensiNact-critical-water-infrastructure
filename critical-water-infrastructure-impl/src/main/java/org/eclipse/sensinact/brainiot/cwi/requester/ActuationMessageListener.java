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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.brainiot.cwi.api.ActuationsRequest;
import org.eclipse.sensinact.brainiot.cwi.connector.NodeREDConnector;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.SmartBehaviour;

@Component(immediate=true, service= {SmartBehaviour.class})
@SmartBehaviourDefinition(consumed = {ActuationsRequest.class}, filter="(timestamp=*)", name="Actuation Request Listener")
public class ActuationMessageListener implements SmartBehaviour<ActuationsRequest> {

	private static final Logger LOG = LoggerFactory.getLogger(ActuationMessageListener.class);
	
	private NodeREDConnector connector;
	
	@Activate
	public void activate() {
		this.connector = new NodeREDConnector();
		LOG.info("ACTIVATING ACTUATION MESSAGE LISTENER");
	}
	
	@Deactivate
	public void deactivate() {
		LOG.info("DEACTIVATING ACTUATION MESSAGE LISTENER");
	}

	@Override
	public void notify(ActuationsRequest event) {
		LOG.debug(String.format("ActuationMessageListener receives : %s",event.toString()));
		Map<String,String> map = event.actuations.stream().<Map<String,String>>collect(HashMap::new,(m,a)->{
			m.put(a.deviceId,a.value);
		},Map::putAll);
		this.connector.notifyActuations(map);
	}

}
