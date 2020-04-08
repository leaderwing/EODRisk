package com.vpb.tts.config;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
 
public class DateUtil {
 
    // List of all date formats that we want to parse.
    // Add your own format here.
    private static List<SimpleDateFormat> 
            dateFormats = new ArrayList<SimpleDateFormat>() {
		private static final long serialVersionUID = 1L; 
		{
            add(new SimpleDateFormat("M/dd/yyyy"));
            add(new SimpleDateFormat("yyyyMMdd"));
            add(new SimpleDateFormat("M/dd/yyyy hh:mm:ss a"));
            add(new SimpleDateFormat("dd.M.yyyy hh:mm:ss a"));
            add(new SimpleDateFormat("dd.MMM.yyyy"));
            add(new SimpleDateFormat("dd-MMM-yyyy"));
        }
    };
 
    /**
     * Convert String with various formats into java.util.Date
     * 
     * @param input
     *            Date as a string
     * @return java.util.Date object if input string is parsed 
     *          successfully else returns null
     */
    public static String convertDateFormat(String input) {
        Date date = null;
        if(null == input) {
            return null;
        }
        for (SimpleDateFormat format : dateFormats) {
            try {
            	format.setLenient(false);
                date = format.parse(input);
            } catch (ParseException e) {
                //Shhh.. try other formats
            }
            if (date != null) {
                break;
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }
    
    public static String getStringDate (Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        return format.format(date);
    }
    public static String getStringDateMMDDYYYY (Date date) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        return format.format(date);
    }
    public static String getNextMonth(String date) throws ParseException{
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date input = format.parse(date);
        Calendar c = Calendar.getInstance();
        c.setTime(input);
        c.add(Calendar.MONTH, 1);
        c.set(Calendar.DATE, c.getActualMinimum(Calendar.DAY_OF_MONTH));    
        Date newDate = c.getTime();
        return format.format(newDate);
    }
    public static String getNextMonthOfNextYear(String date) throws ParseException{
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date input = format.parse(date);
        Calendar c = Calendar.getInstance();
        c.setTime(input);
        c.add(Calendar.MONTH, 1);
        c.set(Calendar.DATE, c.getActualMinimum(Calendar.DAY_OF_MONTH));
        c.add(Calendar.YEAR, 1);      
        Date newDate = c.getTime();
        return format.format(newDate);
    }
    public static String getPeriodDateFITB(String date) throws ParseException {
        String result = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date input = format.parse(date);
        Calendar c = Calendar.getInstance();
        c.setTime(input);
        int year = c.get(Calendar.YEAR);
        
        Date fromDate1 = sdf.parse("01/01/"+String.valueOf(year));
        Date toDate1 = sdf.parse("31/03/"+String.valueOf(year));
        Date fromDate2 = sdf.parse("01/04/"+String.valueOf(year));
        Date toDate2 = sdf.parse("30/06/"+String.valueOf(year));
        Date fromDate3 = sdf.parse("01/07/"+String.valueOf(year));
        Date toDate3 = sdf.parse("30/09/"+String.valueOf(year));
        Date fromDate4 = sdf.parse("01/10/"+String.valueOf(year));
        Date toDate4 = sdf.parse("31/12/"+String.valueOf(year));
        Date endYear = sdf.parse("31/12/"+String.valueOf(year));
        Date startYear = sdf.parse("01/01/"+String.valueOf(year));

         if (input.compareTo(fromDate1)>=0 && input.compareTo(toDate1) <= 0) {
            result = (year)+"0401";
        } else if (input.compareTo(fromDate2)>=0 && input.compareTo(toDate2) <= 0) {
            result = (year)+"0701";
        } else if (input.compareTo(fromDate3)>=0 && input.compareTo(toDate3) <= 0) {
            result = (year)+"1001";
        }else if (input.compareTo(fromDate4)>=0 && input.compareTo(toDate4) <= 0) {
            result = (year+1)+"0101";
        }
        return result;
    }
}