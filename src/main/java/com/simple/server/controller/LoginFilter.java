package com.simple.server.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import javax.servlet.http.HttpServletRequestWrapper;
import com.simple.server.util.MutableHttpServletRequest;

public class LoginFilter implements Filter {
    
    static final long EXPIRATIONTIME = 864_000_000; // 10 days
    
    static final String SECRET = "ThisIsASecret";
     
    static final String TOKEN_PREFIX = "Bearer";
     
    static final String HEADER_STRING = "Authorization";

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    	
    	HttpServletRequest httpRequest = (HttpServletRequest) request;
        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(httpRequest);
        System.out.println(mutableRequest.getHeaderNames());
        System.out.println("----------");;
        String username = mutableRequest.getHeader("username");
        System.out.println(username);;
        System.out.println("----------");;
        if (username != null) {
        	System.out.println("CATCH "+username);;
        	String token = getNewAuthentication(username);
        	mutableRequest.putHeader("Authorization", token);        	
        }
        chain.doFilter(mutableRequest, response);    		    
    }

    @Override
    public void destroy() {
    	System.out.println("Destroy!!!!!!!!!!");
    }
    
 
    public static String getNewAuthentication(String username) {
        return  Jwts.builder().setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.HS512, SECRET).compact();
    }
     
}