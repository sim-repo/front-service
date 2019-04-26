package com.simple.server.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

import com.simple.server.config.AppConfig;


public class DateTimeConverter {
		
	public final static String NAV_DEFAULT_DATE = "1753-01-01 00:00:00";
	public final static String NAV_DEFAULT_TIME = "000000";
	final static String VALIDATE_DATE_FORMAT = "dd-MM-yyyy";
		
	private static final DateTimeFormatter DATE_FORMATTER =  
		    new DateTimeFormatterBuilder()
		        .append(null, new DateTimeParser[]{
		        		DateTimeFormat.forPattern("yyyyMMdd").getParser(),
		                DateTimeFormat.forPattern("dd/MM/yyyy").getParser(),
		                DateTimeFormat.forPattern("dd.MM.yyyy").getParser(),
		                DateTimeFormat.forPattern("dd-MM-yyyy").getParser(),
		                DateTimeFormat.forPattern("yyyy/MM/dd").getParser(),
		                DateTimeFormat.forPattern("yyyy.MM.dd").getParser(),
		                DateTimeFormat.forPattern("yyyy-MM-dd").getParser(),
		                DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss").getParser(),
		                DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss").getParser(),
		                DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss").getParser(),
		                DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss").getParser(),
		                DateTimeFormat.forPattern("yyyy.MM.dd HH:mm:ss").getParser(),
		                DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").getParser(),
		                DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss.SSSz").getParser(),
		                DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss.SSSz").getParser(),		                										   
		                DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss.SSSz").getParser(),
		                DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss.SSSz").getParser(),
		                DateTimeFormat.forPattern("yyyy.MM.dd HH:mm:ss.SSSz").getParser(),
		                DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSz").getParser(),		                
		                DateTimeFormat.forPattern("HH:mm:ss").getParser(),
		                DateTimeFormat.forPattern("HHmmss").getParser(),
		                DateTimeFormat.forPattern("HH-mm-ss").getParser()		                		                
		        })
		        .toFormatter();
		
	
	public static String getCurDate(){
		return new SimpleDateFormat(AppConfig.DATEFORMAT).format(Calendar.getInstance().getTime());
	}
	
	public static String dateToSQLFormat(String sDate){
		LocalDate localDate = DATE_FORMATTER.parseLocalDate(sDate);
		DateTime dateTime = new DateTime(localDate.getYear(),localDate.getMonthOfYear(),localDate.getDayOfMonth(),0,0,0);		
		return dateTime.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));				
	}


	public static String dateToNavFormat(String sDate){
		LocalDate localDate = DATE_FORMATTER.parseLocalDate(sDate);
		DateTime dateTime = new DateTime(localDate.getYear(),localDate.getMonthOfYear(),localDate.getDayOfMonth(),0,0,0);		
		return dateTime.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));				
	}
	
	public static String timeToNavFormat(String sTime){
		LocalTime localTime = DATE_FORMATTER.parseLocalTime(sTime);
		DateTime dateTime = null;
		if(localTime.toString(DateTimeFormat.forPattern("HHmmss")).equals(NAV_DEFAULT_TIME)){
			dateTime = new DateTime(1753, 1, 1, localTime.getHourOfDay(),localTime.getMinuteOfHour(),localTime.getSecondOfMinute());					
		}
		else{			
			dateTime = new DateTime(1754, 1, 1, localTime.getHourOfDay(),localTime.getMinuteOfHour(),localTime.getSecondOfMinute());
		}
		return dateTime.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
	}
	
	public static DateTime createCurrentDateTime(){
		return new DateTime();
	}
	
	

	public static boolean isDateValid(String date) {
	        try {
	            DateFormat df = new SimpleDateFormat(VALIDATE_DATE_FORMAT);
	            df.setLenient(false);
	            df.parse(date);
	            return true;
	        } catch (ParseException e) {
	            return false;
	        }
	}
	
}
