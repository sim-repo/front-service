package com.simple.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.simple.server.config.AppConfig;
import com.simple.server.domain.contract.Login;
import com.simple.server.security.PasswordUtils;

@Controller
public class SyncJwtController {
	
	
	@Autowired
	private AppConfig appConfig;
	
	
	
	@RequestMapping(value = "/sync/unauth", method = RequestMethod.GET, consumes = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> unauth() {

		String charset = "utf-8";	
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/plain;charset=" + charset);
		
		return new ResponseEntity<String>("", responseHeaders, HttpStatus.UNAUTHORIZED);			
	}
	
	
	@RequestMapping(value = "/sync/revokeToken", method = RequestMethod.GET, consumes = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> revokeToken() {

		String charset = "utf-8";	
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/plain;charset=" + charset);
		
		return new ResponseEntity<String>("Token has been revoked", responseHeaders, HttpStatus.LOCKED);			
	}
	
	
	@RequestMapping(value = "/sync/expired", method = RequestMethod.GET, consumes = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> expired() {

		String charset = "utf-8";	
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/plain;charset=" + charset);
		
		return new ResponseEntity<String>("token has expired", responseHeaders, HttpStatus.UPGRADE_REQUIRED);			
	}
	 
	
	
	@RequestMapping(value = "/sync/internalServerError", method = RequestMethod.GET, consumes = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> internalError() {

		String charset = "utf-8";	
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/plain;charset=" + charset);
		
		return new ResponseEntity<String>("", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);			
	}
	 
	
	
	
	@RequestMapping(value = "secure/sync/security/signup", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	public ResponseEntity<String> signup(@RequestHeader("username") String username, 
										 @RequestHeader("password") String password) {
						
		String charset = "utf-8";	
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/plain;charset=" + charset);
		
		if (username.equals("") || password.equals("")) {
			return new ResponseEntity<String>("user error: use login/psw with header request!", responseHeaders, HttpStatus.NON_AUTHORITATIVE_INFORMATION);
		}
				
		
		Login login = appConfig.getLogin(username);
		if (login != null) {
			return new ResponseEntity<String>("user error: username has already registered", responseHeaders, HttpStatus.CONFLICT);
		}
				
		String token = PasswordUtils.getFirstToken(username, password, appConfig);			
		
		if (token == null || token.equals("")) {
			return new ResponseEntity<String>("error has occured: can't generate new token", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<String>(token, responseHeaders, HttpStatus.OK);				
	}
	
	
	
	
	@RequestMapping(value = "secure/sync/security/changePsw", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	public ResponseEntity<String> forgot(@RequestHeader("username") String username, 
										 @RequestHeader("oldPassword") String oldPassword,
										 @RequestHeader("newPassword") String newPassword
										) {
						
		String charset = "utf-8";	
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/plain;charset=" + charset);
		
		if (username.equals("") || oldPassword.equals("") || newPassword.equals("")) {
			return new ResponseEntity<String>("use login/psw with header request!", responseHeaders, HttpStatus.NON_AUTHORITATIVE_INFORMATION);
		}
		
	
		Login login = appConfig.getLogin(username);
		
		if (login == null) {
			return new ResponseEntity<String>("wrong login", responseHeaders, HttpStatus.UNAUTHORIZED);
		}	
		
		String token = PasswordUtils.tryChangePsw(login, oldPassword, newPassword, appConfig);			
		
		if (token == null || token.equals("")) {
			return new ResponseEntity<String>("wrong login or old password", responseHeaders, HttpStatus.UNAUTHORIZED);
		}
		
		return new ResponseEntity<String>(token, responseHeaders, HttpStatus.OK);				
	}
}
