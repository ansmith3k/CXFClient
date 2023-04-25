package org.cxf.client.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtils {
	private static final ObjectMapper jsonMapper;
	
	static {
		jsonMapper = new ObjectMapper();
		jsonMapper.registerModule(new JavaTimeModule());
		
		jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}
	
	public static ObjectMapper getJsonMapper() {
		return jsonMapper;
	}
	
	public static <T> T parseJson(String raw, Class<T> target) throws JsonMappingException, JsonProcessingException {
		return jsonMapper.readValue(raw, target);
	}
	
	public static String writeJson(Object jsonObj) throws JsonMappingException, JsonProcessingException {
		return jsonMapper.writeValueAsString(jsonObj);
	}
}
