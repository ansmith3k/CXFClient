package org.cxf.client.providers;

import org.apache.cxf.jaxrs.client.ResponseExceptionMapper;

import jakarta.ws.rs.core.Response;

public class ApiClientErrorHandler implements ResponseExceptionMapper<Throwable> {

	@Override
	public Throwable fromResponse(Response r) {
		// TODO Auto-generated method stub
		return null;
	}

}
