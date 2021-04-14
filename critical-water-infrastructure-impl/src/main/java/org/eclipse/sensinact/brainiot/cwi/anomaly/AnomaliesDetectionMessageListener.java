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
package org.eclipse.sensinact.brainiot.cwi.anomaly;

import org.eclipse.sensinact.brainiot.cwi.api.AnomaliesDetectionMessage;
import org.eclipse.sensinact.brainiot.cwi.api.AnomalyDetectionMessage;
import org.eclipse.sensinact.brainiot.cwi.api.AnomalyStatus;
import org.eclipse.sensinact.brainiot.cwi.api.AnomalyType;
import org.eclipse.sensinact.gateway.brainiot.service.api.SnaSet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.EventBus;
import eu.brain.iot.eventing.api.SmartBehaviour;

@Component(immediate=true, service= {SmartBehaviour.class})
@SmartBehaviourDefinition(consumed = {AnomaliesDetectionMessage.class}, filter="(timestamp=*)", name="Anomalies Detection Listener")
public class AnomaliesDetectionMessageListener implements SmartBehaviour<AnomaliesDetectionMessage> {

	private static final Logger LOG = LoggerFactory.getLogger(AnomaliesDetectionMessageListener.class);
	
	@Reference
	private EventBus bus;

	@Activate
	public void activate() {
		LOG.info("ACTIVATING ACTUATION BEHAVIOUR ANOMALIES LISTENER");
	}
	
	@Deactivate
	public void deactivate() {
		LOG.info("DEACTIVATING ACTUATION BEHAVIOUR ANOMALIES LISTENER");
	}

	@Override
	public void notify(AnomaliesDetectionMessage event) {
		LOG.info(String.format("AnomaliesDetectionMessageListener receives : %s",event.toString()));
		event.anomalies.entrySet().stream().forEach( e ->{
			String device = e.getKey();
			AnomalyDetectionMessage msg = e.getValue();
			AnomalyStatus status = AnomalyStatus.anomalyStatusFromString(msg.status);
			System.out.println(msg);
			switch(status) {
				case ANOMALY:
					AnomalyType type = AnomalyType.anomalyTypeFromString(msg.type);
					switch(type) {
						case NONE:
							break;
						case SPOT:
						case INTERVAL:
						case PERIODICAL:
							SnaSet set = new SnaSet();
							set.provider = device;
							set.service  = "values";
							set.resource = "anomaly";
							set.timestamp = Long.parseLong(msg.timestamp);
							set.type = "boolean";
							set.value = "true";
							bus.deliver(set);
							break;
						default:
							break;
					}
					break;
				case NORMAL:
					break;
				default:
					break;
			}
		});
	}

}
