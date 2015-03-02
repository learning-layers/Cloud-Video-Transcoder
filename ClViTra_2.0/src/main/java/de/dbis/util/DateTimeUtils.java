package de.dbis.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
 


import org.joda.time.Interval;
import org.joda.time.Period;
 
public class DateTimeUtils {
	
	public static Date currentTime() {
		
		  
		DateTimeUtils obj = new DateTimeUtils();
	 
		DateFormat dateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");
		//get current date time with Date()
		Date date = new Date();
		//System.out.println(dateFormat.format(date));
	 
		return date;
	}
	
	/*public static void main(String[] args) {
 
	  DateTimeUtils obj = new DateTimeUtils();
	  SimpleDateFormat simpleDateFormat = 
               new SimpleDateFormat("dd/M/yyyy hh:mm:ss");
 
	  try {
 
		Date date1 = simpleDateFormat.parse("10/10/2013 11:30:10");
		Date date2 = simpleDateFormat.parse("13/11/2014 20:35:55");
 
		obj.printDifference(date1, date2);	
 
	  } catch (ParseException e) {
		e.printStackTrace();
	  }
 
	}*/
 
	public static String[] printDifference(Date startDate, Date endDate){
 
	  Interval interval = new Interval(startDate.getTime(), endDate.getTime());
	  Period period = interval.toPeriod();
 
          /*System.out.printf(
	       "%d years, %d months, %d days, %d hours, %d minutes, %d seconds%n", 
	       period.getYears(), period.getMonths(), period.getDays(),
	       period.getHours(), period.getMinutes(), period.getSeconds());*/
	    
	  String[] timeDiff = new String[3];
	  
	  timeDiff[0]=Integer.toString(period.getHours());
	  timeDiff[1]=Integer.toString(period.getMinutes());
	  timeDiff[2]=Integer.toString(period.getSeconds());
	  
	  //System.out.println("TIME DIFF1: "+timeDiff1[0]+" "+timeDiff1[1]+" "+timeDiff1[2]);
	  return timeDiff;
 
	}
}