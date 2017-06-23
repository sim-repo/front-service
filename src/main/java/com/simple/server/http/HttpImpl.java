package com.simple.server.http;

import java.net.URI;
import java.nio.charset.Charset;

import org.apache.commons.net.util.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.simple.server.config.AppConfig;
import com.simple.server.domain.contract.RedirectRouting;
import com.simple.server.util.ObjectConverter;

import org.springframework.http.MediaType;

public class HttpImpl {

	public static ResponseEntity<String> get(RedirectRouting redirect) throws Exception {
		ResponseEntity<String> res;
		try {
			URI uri = new URI(redirect.getUrl());
			RestTemplate restTemplate = new RestTemplate();					
			res = restTemplate.getForEntity(uri,  String.class);
		} catch (HttpStatusCodeException e) {					
			String json = ObjectConverter.xmlToJson(e.getResponseBodyAsString());			
			return new ResponseEntity<String>(json, createHeaders(), e.getStatusCode());
		}
		return res;
	}

	public static ResponseEntity<String> exchange(RedirectRouting redirect) throws Exception {
		ResponseEntity<String> res;
		try {
			URI uri = new URI(redirect.getUrl());
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<String> entity = null;
			if (redirect.getUseAuth())
				entity = new HttpEntity<String>("", createHeaders(AppConfig.ACC, AppConfig.PSW));
			else {		
				entity = new HttpEntity<String>("", createHeaders());
			}			
			res = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);			
		} catch (HttpStatusCodeException e) {					
			String json = ObjectConverter.xmlToJson(e.getResponseBodyAsString());			
			return new ResponseEntity<String>(json, createHeaders(), e.getStatusCode());
		}
		return res;
	}
	
	public static HttpHeaders createHeaders(){
		return new HttpHeaders(){
			{setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));}
		};
	}
	
	public static HttpHeaders createHeaders(String username, String password) {
		return new HttpHeaders() {
			{
				String auth = username + ":" + password;
				byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
				String authHeader = "Basic " + new String(encodedAuth);
				set("Authorization", authHeader);
				setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
			}
		};
	}
		
}
