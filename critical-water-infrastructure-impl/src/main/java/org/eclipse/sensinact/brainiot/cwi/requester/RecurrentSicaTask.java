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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.sensinact.brainiot.cwi.api.Measure;
import org.eclipse.sensinact.brainiot.cwi.api.MeasuresEvent;
import org.eclipse.sensinact.gateway.brainiot.sica.service.api.SicaReadRequest;
import org.eclipse.sensinact.gateway.brainiot.sica.service.api.SicaReadResponse;
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
@SmartBehaviourDefinition(consumed = {SicaReadResponse.class}, filter="(timestamp=*)", name="Sica System Reader")
public class RecurrentSicaTask implements SmartBehaviour<SicaReadResponse> {

	private static final Logger LOG = LoggerFactory.getLogger(RecurrentSicaTask.class);
	
	public static class DeviceDescriptor {
		public final String name;
		public final String profile;
		public final int index;
		
		public DeviceDescriptor(String name,String profile,int index){
			this.name = name;
			this.profile = profile;
			this.index = index;
		}
	}
	
	public static class DeviceCollection {
		
		public double cft002 = -1d;
		public long cft002Timestamp = 0;
		public double cft003 = -1d;
		public long cft003Timestamp = 0;
		public double dft001 = -1d;
		public long dft001Timestamp = 0;
		public double eft001 = -1d;
		public long eft001Timestamp = 0;
		
		public boolean complete() {
			return cft002 > -1d &&
				   cft003 > -1d &&
				   dft001 > -1d &&
				   eft001 > -1d;
		}
		
		public MeasuresEvent asMeasuresEvent() {
			MeasuresEvent event = new MeasuresEvent();			
			event.measures = new ArrayList<>();
			
			Measure measure = new Measure();
			measure.deviceId = "CFT002";
			measure.value = cft002;
			measure.timestamp = cft002Timestamp==0?System.currentTimeMillis():cft002Timestamp;
			event.measures.add(measure);
			
			measure = new Measure();
			measure.deviceId = "CFT003";
			measure.value = cft003;
			measure.timestamp = cft003Timestamp==0?System.currentTimeMillis():cft003Timestamp;
			event.measures.add(measure);	
			
			measure = new Measure();
			measure.deviceId = "DFT001";
			measure.value = dft001;
			measure.timestamp = dft001Timestamp==0?System.currentTimeMillis():dft001Timestamp;
			event.measures.add(measure);
			
			measure = new Measure();
			measure.deviceId = "EFT001";
			measure.value = eft001;
			measure.timestamp = eft001Timestamp==0?System.currentTimeMillis():eft001Timestamp;
			event.measures.add(measure);
			return event;
		}
	}
	
	@Reference
	private EventBus bus;	
	private Timer timer;
	private Map<String,DeviceDescriptor> map;

	private DeviceCollection deviceCollection;
	

	public RecurrentSicaTask() {
		this.map = new HashMap<>();
		this.map.put("CFT002",new DeviceDescriptor("CFT002","1_23",0));
		this.map.put("CFT003",new DeviceDescriptor("CFT003","1_25",0)); 
		this.map.put("DFT001",new DeviceDescriptor("DFT001","1_40",0));
		this.map.put("EFT001",new DeviceDescriptor("EFT001","1_71",0));
		deviceCollection = new DeviceCollection();
	}
	
	@Activate
	public void activate() {
		this.timer  = new Timer();
		this.timer.schedule(new TimerTask() {
			@Override
			public void run() {
				RecurrentSicaTask.this.map.values().stream().forEach(
					v -> { 
//						  String[] sid = e.split("_");
						  SicaReadRequest req = new SicaReadRequest();
						  req.field = null; 
						  req.device = v.name;
//						  req.serverId = Integer.parseInt(sid[0]);	
//						  req.groupId = Integer.parseInt(sid[1]);				
						  RecurrentSicaTask.this.bus.deliver(req);
						  LOG.debug(String.format("DELIVERING :",req.toString()));
					});
			}			
		}, 60*1000, 60*1000*2);		
	}
	
	@Deactivate
	public void deactivate() {
		if(this.timer!=null)
			this.timer.cancel();
		this.timer = null;
	}

	@Override
	public void notify(SicaReadResponse event) {
		LOG.debug(String.format("RecurrentSicaTask receives : %s",event.toString()));
//		String profile = new StringBuilder(
//				).append(event.serverId
//				).append("_"
//				).append(event.groupId < 10?"0":""
//				).append(event.groupId
//				).toString();
		double[] values = event.value;
		if(values == null || values.length == 0) {
			LOG.warn(String.format("NO VALUES RETURNED [%s]",event.device));
			return;
		}
		DeviceDescriptor device = this.map.get(event.device);
		if(device == null) {
			LOG.warn(String.format("INVALID PROFILE [%s]",event.device));
			return;
		}
		if(device.index >= values.length) {
			LOG.warn(String.format("INVALID INDEX [%s]",event.device));
			return;
		}
		switch(device.name) {
			case "CFT002":
				this.deviceCollection.cft002=values[device.index];
				this.deviceCollection.cft002Timestamp=event.timestamp;
				break;
			case "CFT003": 
				this.deviceCollection.cft003=values[device.index];
				this.deviceCollection.cft003Timestamp=event.timestamp;
				break;
			case "DFT001":
				this.deviceCollection.dft001=values[device.index];
				this.deviceCollection.dft001Timestamp=event.timestamp;
				break;
			case "EFT001":
				this.deviceCollection.eft001=values[device.index];
				this.deviceCollection.eft001Timestamp=event.timestamp;
				break;
			default:break;
		}
		if(deviceCollection.complete()) {
			this.bus.deliver(deviceCollection.asMeasuresEvent());
			this.deviceCollection = new DeviceCollection();
		}
	}

}
