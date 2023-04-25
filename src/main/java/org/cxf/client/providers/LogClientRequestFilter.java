package org.cxf.client.providers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogClientRequestFilter implements ClientRequestFilter{

	@Override
	public void filter(ClientRequestContext request) throws IOException {
		if(log.isDebugEnabled() || log.isTraceEnabled()) {
			StringBuilder str = new StringBuilder();
			str.append("\n**************CXF Client Request**************").append("\n");
			str.append("Requested URI: ").append(request.getUri().toString()).append("\n");
			str.append("Method: ").append(request.getMethod()).append("\n");
			str.append("MediaType: ").append(request.getMediaType().toString()).append("\n");
			List<MediaType> acceptMediaTypes = request.getAcceptableMediaTypes();
			if(!acceptMediaTypes.isEmpty()) {
				str.append("Accepted Media Types: ").append("\n");
				for(MediaType mType: acceptMediaTypes) {
					str.append("\t").append(mType.toString()).append("\n");
				}
			}
			Map<String, Cookie> cookies = request.getCookies();
			if(!cookies.isEmpty()) {
				str.append("Cookies: ").append("\n");
				for(Cookie cookie: cookies.values()) {
					str.append("\t").append(cookie.getName()).append(": ").append(cookie.getValue()).append("\n");
				}
			}
			MultivaluedMap<String, String> headers = request.getStringHeaders();
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
