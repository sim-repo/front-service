package com.simple.server.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.simple.server.config.AppConfig;
import com.simple.server.config.JwtStatusType;
import com.simple.server.domain.contract.Login;
import com.simple.server.util.DateTimeConverter;
import com.simple.server.util.MyLogger;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
 
public class PasswordUtils {
    
    private static final Random RANDOM = new SecureRandom();
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;  
	
    static final Integer EXPIRATIONDAY = 2; // 10 days
    
    static final String SECRET = "ThisIsASecret";
     
    static final String TOKEN_PREFIX = "Bearer";
     
    static final String HEADER_STRING = "Authorization";
    
    
     public static String getSalt(int length) {
        StringBuilder returnValue = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return new String(returnValue);
    }
     
    public static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        Arrays.fill(password, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }
    
    public static String generateSecurePassword(String password, String salt) {
        String returnValue = null;
        byte[] securePassword = hash(password.toCharArray(), salt.getBytes());
 
        returnValue = Base64.getEncoder().encodeToString(securePassword);
 
        return returnValue;
    }
    
    public static boolean verifyUserPassword(String providedPassword, String securedPassword, String salt) {
        boolean returnValue = false;
        
        String newSecurePassword = generateSecurePassword(providedPassword, salt);
        
        returnValue = newSecurePassword.equalsIgnoreCase(securedPassword);
        
        return returnValue;
    }
    
    private static JwtStatusType isExpired(HttpServletRequest request, Date untilDate) {
    	if (untilDate == null) {
    		return JwtStatusType.RevokeToken;
    	}
		String token = request.getHeader(HEADER_STRING);
		Date expirationDate = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token.replace(TOKEN_PREFIX, "")).getBody().getExpiration();  
			
		if (expirationDate.compareTo(untilDate) > 0) {	    	
	     	return JwtStatusType.Expired;
	    }
	    return JwtStatusType.Authorized;
    }
    
    public static JwtStatusType isAuthentication(HttpServletRequest request, AppConfig appConfig) {
        String token = request.getHeader(HEADER_STRING);
        if (token != null) {
            String username = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token.replace(TOKEN_PREFIX, "")).getBody().getSubject();     
           
            Login login = appConfig.getLogin(username); 
            if (login != null) {
            	return isExpired(request, login.getExpire());            	
            }
            return JwtStatusType.UnAuhorized;
        }
        return JwtStatusType.UnAuhorized;
    }   
    
    
    public static String getFirstToken(String username, String psw, AppConfig appConfig) {
    	String salt = PasswordUtils.getSalt(30);
        String encryptedPsw = PasswordUtils.generateSecurePassword(psw, salt);
        Date expire = new DateTime(new Date()).plusDays(EXPIRATIONDAY).toDate();
    	Login login = new Login(username, encryptedPsw, salt, expire);
    	
    	try {
			appConfig.getRemoteLogService().putLogin(login);
			appConfig.setLoginHashMap(login.getLogin(), login);
			return  Jwts.builder().setSubject(username)
	                .setExpiration(expire)
	                .signWith(SignatureAlgorithm.HS512, SECRET).compact();	
		} catch (Exception e) {
			 StackTraceElement[] stktrace = e.getStackTrace(); 
			 StringBuilder builder = new StringBuilder();
			 builder.append(stktrace[0].toString());			 
			 builder.append(" : "+e.getLocalizedMessage());	         
	         MyLogger.error(PasswordUtils.class, builder.toString());	
		}
    	return "";
    	
    }
    
    public static String tryChangePsw(Login login, String oldPassword, String newPassword, AppConfig appConfig) {
    	if (PasswordUtils.verifyUserPassword(oldPassword, login.getPsw(), login.getSalt()) == true) {
    		return PasswordUtils.doChangePsw(login, newPassword, appConfig);
    	}    	
        return "";
    }  
    
    private static String doChangePsw(Login login, String newPassword, AppConfig appConfig) {
		 String salt = PasswordUtils.getSalt(30);
         String encryptedPassword = PasswordUtils.generateSecurePassword(newPassword, salt);
         Date expire = new DateTime(new Date()).plusDays(EXPIRATIONDAY).toDate();
         login.setSalt(salt);
         login.setPsw(encryptedPassword); 
         login.setExpire(expire);
         
         try {
 			appConfig.getRemoteLogService().putLogin(login);
 			return  Jwts.builder().setSubject(login.getLogin())
 	                .setExpiration(expire)
 	                .signWith(SignatureAlgorithm.HS512, SECRET).compact();		
 		} catch (Exception e) {
 			MyLogger.error(PasswordUtils.class, e); 			
 		}
        return "";
    }
      
}