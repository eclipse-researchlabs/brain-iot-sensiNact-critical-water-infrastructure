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
package org.eclipse.sensinact.gateway.brainiot.sica;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.brainiot.cwi.api.ActuationRequest;
import org.eclipse.sensinact.brainiot.cwi.api.ActuationsRequest;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.annotation.TaskCommand;
import org.eclipse.sensinact.gateway.generic.annotation.TaskExecution;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.brain.iot.eventing.api.EventBus;

/**
 * {@link TaskExecution} annotated POJO, in charge of translating sensiNact access method invocation into 
 * SicaProxy call
 */
@TaskExecution(profile = {"1_10","1_12","1_20","1_22","1_24","1_28","1_32","1_33","1_37","1_38","1_54","1_55",
"1_56","1_59","1_60","1_62","1_73","1_74","1_75","1_76","1_77","1_78","1_79","1_81","1_82","1_84","1_166",
"1_169","1_170","1_171","1_172","1_177","1_188"})
public class SicaAperturaEndpoint {
	
	private Mediator mediator;

	private Map<String,Long> propagate;
	
	private static final Logger LOG = LoggerFactory.getLogger(SicaAperturaEndpoint.class);
	
	/**
	 * Constructor
	 */
	public SicaAperturaEndpoint(Mediator mediator) {
		this.mediator = mediator;
		this.propagate = Collections.<String,Long>synchronizedMap(new HashMap<>());
	}
	
	/**
	 * Propagates a SET access method invocation to the SicaProxy
	 * 
	 * @param uri the String path of the resource whose SET access method invocation is propagated 
	 * in here 
	 * @param attributeName the String name of the attribute targeted by the SET access method
	 * @param value the Object value to be set
	 * 
	 * @return the propagated and translated SET access method invocation result
	 */
	@TaskCommand(target="/*/values/apertura", synchronization=TaskCommand.SynchronizationPolicy.SYNCHRONOUS, method=CommandType.SET)
	public Object set(String uri, String attributeName, Object value) {
		if(value == null)
			return String.format("No event delivered : null value [%s] ", uri);
		long current = System.currentTimeMillis();
		Long last = this.propagate.get(uri);
		if(last != null) {
			if(current - last.longValue() < 2000)
				return String.format("No event delivered : multiple events for '%s' in less than 2 seconds", uri);
		}
		this.propagate.put(uri, Long.valueOf(current));
		String sp = UriUtils.getRoot(uri).substring(1);
		
		ActuationRequest actuation = new ActuationRequest();
		actuation.deviceId = String.format("Apertura_%s",sp);		
		actuation.value = String.valueOf(Double.valueOf(String.valueOf(value)).intValue());
		
		final ActuationsRequest req = new ActuationsRequest();
		req.actuations = Collections.singletonList(actuation);
		String sent = mediator.callService(EventBus.class, new Executable<EventBus,String>() {
			@Override
			public String execute(EventBus bus) throws Exception {
				bus.deliver(req);
				return  String.format("Delivered event: %s", req);
			}			
		});
		if(sent == null)
			return String.format("No event delivered : enable to access to EventBus [%s]", uri);
		return sent;
	}
}
