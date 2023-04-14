package com.easyjava.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String _YYYYMMDD = "yyyy/MM/dd";
    public static final String YYYYMMDD = "yyyyMMdd";

    public static String Format(Date date, String patten){
        return new SimpleDateFormat(patten).format(date);
    }

    public static Date paras(String date,String patten){
        try {
            new SimpleDateFormat(patten).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
