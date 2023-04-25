package org.cxf.client;

import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;

import jakarta.ws.rs.core.Response;

public class WebClientProxyBuilder {

	private WebClient client;
	private boolean closed = false;

	public WebClientProxyBuilder(WebClient client) {
		this.client = client;
	}
	
	public <T> T buildProxy(Class<T> apiClass) {
		if(closed) {
			throw new RuntimeException("Client is shut down.");
		}
		return JAXRSClientFactory.fromClient(client, apiClass);
	}
	
	public void closeProxy(Object proxy) {
		if(proxy != null) {
			Client proxyConnection = WebClient.client(proxy);
			if(proxyConnection != null) {
				Response bufferedRestPayload = proxyConnection.getResponse();
				if(bufferedRestPayload != null){
					bufferedRestPayload.close();
				}
			}
		}
	}
	
	public void close() {
		this.closed = true;
		client.close();
	}
	
	public WebClient getClient() {
		return client;
	}
}
