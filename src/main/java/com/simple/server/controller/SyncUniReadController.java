package com.simple.server.controller;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.simple.server.config.AppConfig;
import com.simple.server.util.ObjectConverter;

@Controller
public class SyncUniReadController {

	
	@Autowired
	private AppConfig appConfig;
	
	
	/**
	 * param sql - любая sql-инструкция в LOG для операций чтения
	 * 
	 * @return json
	 */
	/* uncomment if necessary
	 *  
	@RequestMapping(value = "/sync/get/json/log/any/{sql:.+}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	public @ResponseBody String jsonLogAnyGet(@PathVariable("sql") String sql) {
		String res = null;
		try {
			res = appConfig.getRemoteService().getFlatJson(sql, appConfig.LOG_ENDPOINT_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}*/
	

	/*
	@RequestMapping(value = "/sync/get/json/nav/any/{sql:.+}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	private @ResponseBody String jsonNavAnyGet(@PathVariable("sql") String sql,
			@RequestParam(value = "endpointId", required = false) String endpointId) {
		String res = null;
		try {
			res = appConfig.getRemoteService().getFlatJson(sql,
					endpointId != null ? endpointId : appConfig.getDefaultEndpointByGroupId(appConfig.navGroupId));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
	*/
	
	/**
	 * @param sql
	 *            - любая sql-инструкция в NAV для операций чтения
	 * @return json
	 */
	/*
	@RequestMapping(value = "/sync/get/xml/nav/any/{sql:.+}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	private @ResponseBody String xmlNavAnyGet(@PathVariable("sql") String sql,
			@RequestParam(value = "endpointId", required = false) String endpointId) {
		String res = null;
		try {
			res = appConfig.getRemoteService().getFlatXml(sql,
					endpointId != null ? endpointId : appConfig.getDefaultEndpointByGroupId(appConfig.navGroupId));
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return res;
	}
	
	*/
	
	
	
	public static ResponseEntity<String> getBadResponse(){
		return new ResponseEntity<String>("request does not contain a parameter named 'method' ", HttpStatus.BAD_REQUEST);
	}
	

	@RequestMapping(value = "uni/sync/get/json/db", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	public @ResponseBody String jsonDBUniGet(HttpServletRequest request){

		String method = request.getParameter("method");
		if (method == null || method == "") {
			return getBadResponse().getBody();
		}
		
		String res = appConfig.runDbUniStatement(method, request.getQueryString());				
		return res;
	}
	
	
	@RequestMapping(value = "uni/sync/get/json/db/b64", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	public @ResponseBody String jsonDBUni64Get(HttpServletRequest request){

		Enumeration enumeration = request.getParameterNames();
	    Map<String, String> modelMap = new HashMap<>();
	    while(enumeration.hasMoreElements()){
	        String parameterName = (String) enumeration.nextElement();
	        String val = request.getParameter(parameterName);
	        if (ObjectConverter.isNumeric(val) == false) {
		        if (Base64.isBase64(val)) {	
					byte[] converted = Base64.decodeBase64(val.getBytes());
					val = new String(converted, StandardCharsets.UTF_8);
				} 
	        }
	        modelMap.put(parameterName, val);	          
	    }
	
		String method = request.getParameter("method");
		if (method == null || method == "") {
			return getBadResponse().getBody();
		}
		
		String res = appConfig.runDbUniStatement(method, modelMap);				
		return res;
	}
	
	
	
	
	@RequestMapping(value = "uni/secure/sync/get/json/db", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	public @ResponseBody String jsonSecureUniGet(HttpServletRequest request){

		String method = request.getParameter("method");
		if (method == null || method == "") {
			return getBadResponse().getBody();
		}
		
		String res = appConfig.runDbSecureUniStatement(method, request.getQueryString());				
		return res;
	}
	
	
	@RequestMapping(value = "uni/secure/sync/get/json/db/b64", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	public @ResponseBody String jsonSecureUni64Get(HttpServletRequest request){

		Enumeration enumeration = request.getParameterNames();
	    Map<String, String> modelMap = new HashMap<>();
	    while(enumeration.hasMoreElements()){
	        String parameterName = (String) enumeration.nextElement();
	        String val = request.getParameter(parameterName);
	        if (ObjectConverter.isNumeric(val) == false) {
		        if (Base64.isBase64(val)) {	
					byte[] converted = Base64.decodeBase64(val.getBytes());
					val = new String(converted, StandardCharsets.UTF_8);
				} 
	        }
	        modelMap.put(parameterName, val);	          
	    }
	
		String method = request.getParameter("method");
		if (method == null || method == "") {
			return getBadResponse().getBody();
		}
		
		String res = appConfig.runSecureUniStatement(method, modelMap);				
		return res;
	}	
	
}
