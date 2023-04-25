package org.cxf.client.token;

import io.jsonwebtoken.Claims;

public interface TokenValidator {

	public abstract Claims getClaims(String token) throws Exception;
	public abstract boolean isTokenExpired(Claims claims, Long tokenTimeLimit) throws Exception;
}
