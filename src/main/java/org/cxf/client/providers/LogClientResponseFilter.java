package org.cxf.client.providers;

import java.io.IOException;
import java.util.Map;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogClientResponseFilter implements ClientResponseFilter{
	@Override
	public void filter(ClientRequestContext request, ClientResponseContext response) throws IOException {
		if(log.isDebugEnabled() || log.isTraceEnabled()) {
			StringBuilder str = new StringBuilder();
			str.append("\n**************CXF Client Response**************").append("\n");
			str.append("Requested URI: ").append(request.getUri().toString()).append("\n");
			str.append("Status: ").append(response.getStatus()).append("\n");
			str.append("Length: ").append(response.getLength()).append("\n");
			str.append("MediaType: ").append(response.getMediaType().toString()).append("\n");
			Map<String, NewCookie> cookies = response.getCookies();
			if(!cookies.isEmpty()) {
				str.append("Cookies: ").append("\n");
				for(NewCookie cookie: cookies.values()) {
					str.append("\t").append(cookie.getName()).append(": ").append(cookie.getValue()).append("\n");
				}
			}
			MultivaluedMap<String, String> headers = response.getHeaders();
			if(!headers.isEmpty()) {
				str.append("Headers: ").append("\n");
				for(String key: headers.keySet()) {
					str.append("\t").append(key).append(": ").append(headers.get(key)).append("\n");
				}
			}
			log.debug(str.toString());
		}
	}
}
