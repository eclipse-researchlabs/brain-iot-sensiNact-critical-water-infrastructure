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

import org.eclipse.sensinact.gateway.generic.ExtModelInstance;
import org.eclipse.sensinact.gateway.generic.ExtResourceConfig;
import org.eclipse.sensinact.gateway.generic.ExtResourceImpl;
import org.eclipse.sensinact.gateway.generic.ExtServiceImpl;
import org.eclipse.sensinact.gateway.generic.annotation.Act;
import org.json.JSONObject;

public class SicaAction extends ExtResourceImpl {

	public SicaAction(ExtModelInstance<?> modelInstance, ExtResourceConfig resourceConfig, ExtServiceImpl service) {
		super(modelInstance, resourceConfig, service);
	}

	@Act
	public JSONObject dim(double apertura) {
		JSONObject obj = new JSONObject(
		    ).put("name" , super.getName()
			).put("value", apertura
		    ).put("call" , "actuation");
		System.out.println(obj);
		return obj;
	}
	
	@Act
	public JSONObject act() {
		JSONObject obj = new JSONObject(
		).put("name",super.getName()
		).put("call", "actuation");
		System.out.println(obj);
		return obj;
		
	}

}
