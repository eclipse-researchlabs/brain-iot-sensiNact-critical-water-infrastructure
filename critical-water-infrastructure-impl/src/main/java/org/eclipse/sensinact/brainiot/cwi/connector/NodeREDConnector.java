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
package org.eclipse.sensinact.brainiot.cwi.connector;


import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NodeRED's HTTP endpoint connector
 */
public class NodeREDConnector {

	private static final Logger LOG = LoggerFactory.getLogger(NodeREDConnector.class);

	private static final String NODE_RED_IP = "192.168.40.10";
	private static final String NODE_RED_PATH = "/test";	
	private static final int NODE_RED_PORT = 1881;
	
	private CloseableHttpClient client;
	
	public NodeREDConnector(){
		this.client = HttpClients.createDefault();
	}

	public void notifyIAControlFinished(List<String> finished) {
		StringBuilder builder = finished.stream().collect(StringBuilder::new, (b,s)->{
			b.append(s);
			b.append("=IA_Control_finished");
			b.append("&");
		},(h,m)->h.append(m.toString()));
		builder.deleteCharAt(builder.length()-1);
		String query = builder.toString();
		System.out.println("\t..."+query);
		send(query);
	}

	public void notifyActuations(Map<String,String> map) {
		StringBuilder builder = map.entrySet().stream().collect(StringBuilder::new, (b,e)->{
			b.append(e.getKey());
			b.append("=");
			b.append(e.getValue());
			b.append("&");
		},(h,m)->h.append(m.toString()));
		builder.deleteCharAt(builder.length()-1);
		String query = builder.toString();
		send(query);
	}
	
	public void notifyValue(String identifier, String value) {
		String query = String.format("%s=%s",identifier,value);
		send(query);
	}
	
	private synchronized void send(String query) {
		String url = String.format("http://%s:%s%s?%s", NODE_RED_IP,NODE_RED_PORT,NODE_RED_PATH,query);
		System.out.println("NodeRED Request: " + url);
		HttpGet http = new HttpGet(url);
		try (CloseableHttpResponse response = client.execute(http)) {
			int code = response.getStatusLine().getStatusCode();
			LOG.debug(response.toString());
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e.getMessage(),e);
		}
	}
	
}
