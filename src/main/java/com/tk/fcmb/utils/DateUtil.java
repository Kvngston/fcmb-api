package com.tk.fcmb.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author joel.eze
 */
public class DateUtil {

    // public static String dateFormat = "dd-MM-yyyy hh:mm";
    private static String dateFormat = "yyyy-MM-dd hh:mm:ss";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
    private static String dateFormat1 = "dd MMMM yyyy hh:mm";
    private static SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(dateFormat1);

    public static Date milliSToDate(String m) {
        //2018-10-30 12:17:00
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(m));
        return calendar.getTime();
    }

    public static Date addMinutesToDate(int minutes, Date beforeTime) {
        final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs

        long curTimeInMs = beforeTime.getTime();
        Date afterAddingMins = new Date(curTimeInMs + (minutes * ONE_MINUTE_IN_MILLIS));
        return afterAddingMins;
    }

    public static String formatDate(Date date) {
        return simpleDateFormat1.format(date);
    }

//    public static void main(String[] args) {
//        System.out.println(new DateUtil().formatDate(new Date()));
//    }
}
