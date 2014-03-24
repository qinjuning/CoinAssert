package com.coinwtb.coinassistant.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;


import android.content.Context;

/**
 * 格式(字符串、时间)转换
 * @author Administrator
 *
 */
public class FormatUtils {

	/**
	 *  字符串格式转换，工具方法:String.format
	 */
	public static String formateWithResId(Context context , int resId, Object...formatArgs) {
		String formater = context.getString(resId, formatArgs);
		return formater;
	}
	
	/*  Try to use String.format() as little as possible, because it creates a
     *  new Formatter every time you call it, which is very inefficient.
     *  Reusing an existing Formatter more than tripled the speed of
     *  makeTimeString().
     *  This Formatter/StringBuilder are also used by makeAlbumSongsLabel()
     */
    private static StringBuilder sFormatBuilder = new StringBuilder();
    private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());

    /**
     * <xliff:g>
     * @param format
     * @param args
     * @return
     */
    public static String makeFormatString(String format, Object...args) {
    	/* Provide multiple arguments so the format can be changed easily
         * by modifying the xml.
         */
    	sFormatBuilder.setLength(0);
        return sFormatter.format(format, args).toString();
    }
    
    private static SimpleDateFormat sLongDateFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static String makeLongDateStringFromCalendar(Calendar calendar) {
    	return makeLongDateStringFromDate(calendar.getTime());
    }
    
    public static String makeLongDateStringFromDate(Date date) {
    	return sLongDateFormat.format(date);
    }
    
    public static String makeShortDateStringFromCalendar(Calendar calendar) {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	return dateFormat.format(calendar.getTime());
    }
}
