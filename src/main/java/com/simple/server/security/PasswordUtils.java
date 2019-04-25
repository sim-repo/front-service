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

import org.joda.time.DateTime;

import com.simple.server.config.AppConfig;
import com.simple.server.domain.contract.Login;

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
        
        // Generate New secure password with the same salt
        String newSecurePassword = generateSecurePassword(providedPassword, salt);
        
        // Check if two passwords are equal
        returnValue = newSecurePassword.equalsIgnoreCase(securedPassword);
        
        return returnValue;
    }
    
    public static Boolean isExpired(HttpServletRequest request, AppConfig appConfig) {
    	 String token = request.getHeader(HEADER_STRING);
    	 Date expirationDate = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token.replace(TOKEN_PREFIX, "")).getBody().getExpiration();  
    	 System.out.println(expirationDate);    	 
         if (expirationDate.before(new Date())) {
         	return true;
         }
         return false;
    }
    
    public static Boolean isAuthentication(HttpServletRequest request, AppConfig appConfig) {
        String token = request.getHeader(HEADER_STRING);
        if (token != null) {
            String username = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token.replace(TOKEN_PREFIX, "")).getBody().getSubject();     
           
            Login login = appConfig.getLogin(username); 
            if (login != null) {
            	return true;
            }
            return false;
        }
        return false;
    }   
    
    
    public static String getFirstToken(String username, String psw, AppConfig appConfig) {
    	String salt = PasswordUtils.getSalt(30);
        String encryptedPsw = PasswordUtils.generateSecurePassword(psw, salt);
        
    	Login login = new Login(username, encryptedPsw, salt);
    	
    	try {
			appConfig.getRemoteLogService().putLogin(login);
			appConfig.setLoginHashMap(login.getLogin(), login);
			return  Jwts.builder().setSubject(username)
	                .setExpiration(new DateTime(new Date()).plusDays(EXPIRATIONDAY).toDate())
	                .signWith(SignatureAlgorithm.HS512, SECRET).compact();	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
         login.setSalt(salt);
         login.setPsw(encryptedPassword); 
         
         try {
 			appConfig.getRemoteLogService().putLogin(login);
 			return  Jwts.builder().setSubject(login.getLogin())
 	                .setExpiration(new DateTime(new Date()).plusDays(EXPIRATIONDAY).toDate())
 	                .signWith(SignatureAlgorithm.HS512, SECRET).compact();		
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
        return "";
    }
      
}