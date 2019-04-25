package com.simple.server.controller;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.simple.server.config.AppConfig;
import com.simple.server.domain.contract.Login;
import com.simple.server.security.PasswordUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;



public class CustomFilter implements Filter {

	private AppConfig appConfig;

    public void setAppConfig(AppConfig appConfig) {
    	this.appConfig = appConfig;
    }
    
    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        
        try {
	        if (PasswordUtils.isAuthentication(req, appConfig)) {
	        	if (PasswordUtils.isExpired(req, appConfig) == false ) {
	        		chain.doFilter(request, response);
	        	} else {
	        		resp.sendRedirect("/front/sync/expired");
	        	}
	        } else {        	
	        	resp.sendRedirect("/front/sync/unauth");        	
	        }
        } 
        catch (ExpiredJwtException err) {
        	resp.sendRedirect("/front/sync/unauth");
        }
        catch (MalformedJwtException err) {
        	resp.sendRedirect("/front/sync/unauth");
        }
        catch(SignatureException  err) {
        	resp.sendRedirect("/front/sync/unauth");
        }
        catch (UnsupportedJwtException err) {
        	resp.sendRedirect("/front/sync/unauth");
        }
        catch (IllegalArgumentException err) {
        	resp.sendRedirect("/front/sync/unauth");
        }
    }
    

    @Override
    public void destroy() {
    	System.out.println("Destroy!!!!!!!!!!");
    }
       
}