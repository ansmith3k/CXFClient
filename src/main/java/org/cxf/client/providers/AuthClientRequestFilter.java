package org.cxf.client.providers;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.cxf.client.WebClientProxyBuilder;
import org.cxf.client.token.TokenValidator;
import org.drop.utils.TimeUtils;

import io.jsonwebtoken.Claims;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthClientRequestFilter implements ClientRequestFilter{

	private static final String AUTHORIZATION_PROPERTY = "Authorization";
	private static final String AUTHORIZATION_SCHEME= "Basic";
	private static final String AUTH_REQUEST_HEADER_USER = "user";
	//private static final String AUTH_REQUEST_HEADER_PASS = "pass";
	
	private String username;
	private String password;
	private WebClientProxyBuilder client;
	private Class<?> authApi;
	private String authMethodName;
	private TokenValidator tokenValidator;
	private String token = null;
	private LocalDateTime expires = null;
	private Claims claims = null;
	
	public AuthClientRequestFilter(WebClientProxyBuilder client, TokenValidator tokenValidator, Class<?> authApi, String authMethodName, String username, String password) {
		if(client == null) {
			throw new IllegalArgumentException("Invalid WebClientProxyBuilder. WebClientProxyBuilder is null.");
		}
		if(username == null) {
			throw new IllegalArgumentException("Invalid username. Username is null.");
		}
		if(password == null) {
			throw new IllegalArgumentException("Invalid password. Password is null.");
		}
		if(tokenValidator == null) {
			throw new IllegalArgumentException("Invalid tokenValidator. TokenValidator is null.");
		}
		if(authApi == null) {
			throw new IllegalArgumentException("Invalid authentication api. Authentication api is null.");
		}
		if(authMethodName == null) {
			throw new IllegalArgumentException("Invalid authentication method name. Authentication Method Name is null.");
		}
		this.client = client;
		this.username = username;
		this.password = password;
		this.authApi = authApi;
		this.authMethodName = authMethodName;
		this.tokenValidator = tokenValidator;
	}
	
	@Override
	public void filter(ClientRequestContext request) throws IOException {
		//Already has a token.. Skip this filter.
		log.debug("AuthClientRequestFilter called.");
		if(request.getHeaders().containsKey(AUTHORIZATION_PROPERTY)) {
			return;
		}
		log.debug("AuthClientRequestFilter: No authorization property found.");
		if(!request.getHeaders().containsKey(AUTH_REQUEST_HEADER_USER)) {
			synchronized(this) {
				if(token == null || (expires != null && LocalDateTime.now().isBefore(expires))) {
					getNewToken();
				}
			}
		}
		if(token != null) {
			log.debug("AuthClientRequestFilter: Authorization property set.");
			request.getHeaders().add(AUTHORIZATION_PROPERTY, AUTHORIZATION_SCHEME + " " + token);
		}
	}

	private void getNewToken() {
		log.debug("AuthClientRequestFilter: Requesting new token.");
		Object api = null;
		Method methodCall;
		try {
			api = client.buildProxy(authApi);
			methodCall = api.getClass().getDeclaredMethod(authMethodName, String.class, String.class);
			Object rtn = methodCall.invoke(api, username, password);
			String tmpToken = null;
			if(rtn != null) {
				if(rtn instanceof String) {
					tmpToken = (String) rtn;
				}else if(rtn instanceof Response) {
					Response tResponse = (Response)rtn;
					tmpToken = tResponse.readEntity(String.class);
					tResponse.close();
				}
			}
			if(tmpToken != null) {
				claims = tokenValidator.getClaims(tmpToken);
				Date expireDate = claims.getExpiration();
				if(expireDate != null) {
					expires = TimeUtils.getLocalDateTime(expireDate, ZoneId.systemDefault());
				}
			}
			log.debug("AuthClientRequestFilter: New token received. Expires: " + expires.toString());
		}catch(Exception e) {
			log.error(e.getMessage(), e);
		}finally{
			if(api != null) {
				client.closeProxy(api);
			}
		}
	}
}
