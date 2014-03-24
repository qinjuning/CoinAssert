package com.coinwtb.coinassistant.utils;

import java.util.Map;
import java.util.Set;

import com.coinwtb.coinassistant.ui.base.CoinApplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 数据存取
 * 
 */
public class PreferencesService {
	
	/**
	 * 刷新时间设置
	 */
	private static final String PRE_HOME_TIME = "home_time";
	private static final String PRE_ALERT_TIME = "alert_time";
	
	/**
	 *  退出时保存的列表索引
	 */
	public static final String PRE_LAST_SHOW_COIN_INDEX = "show_coin_index";
	
	public static final String PRE_FIRST_USER= "first_use";
	
    // Baidu PushServer 用share preference来实现是否绑定的开关。在ionBind且成功时设置true，unBind且成功时设置false
	public static final String PRE_PUSH_BIND_FLAG = "push_bind_flag";
	
	
	public static final int  MILLIONS =  1000;           
	
	public static final int DEFAULT_HOME_TIME = 10 * MILLIONS;              // 10s
	public static final int DEFAULT_ALERT_TIME = 10 * MILLIONS;             // 10s
	
    private static SharedPreferences mPreferences;
    private static Context           mContext;
    private static String            mShareName = "settings.xml";

    private PreferencesService() {}

    private static PreferencesService mPreferencesService;

    public static PreferencesService getInstance( Context context ) {
        if ( mPreferencesService == null ) {
            mContext = context;
            mPreferencesService = new PreferencesService();
            mPreferences = mContext.getSharedPreferences( mShareName, Context.MODE_PRIVATE );
        }
        return mPreferencesService;
    }

    public static PreferencesService getInstance() {
        if ( mPreferencesService == null ) {
            mContext = CoinApplication.getInstance();
            mPreferencesService = new PreferencesService();
            mPreferences = mContext.getSharedPreferences( mShareName, Context.MODE_PRIVATE );
        }
        return mPreferencesService;
    }
    
    /**
     * 批量保存
     */
    void saveInfo( Map<String, String> info ) {
        Editor editor = mPreferences.edit();
        Set<String> keys = info.keySet();
        for ( String str : keys ) {
            editor.putString( str, info.get( str ) );
        }
        editor.commit();
    }

    /**
     * 保存string类型数据
     */
    public void setPreferences( String name, String value ) {
        Editor editor = mPreferences.edit();
        editor.putString( name, value );
        editor.commit();
    }

    /**
     * 保存long类型数据
     */
    public void putLong( String name, long value ) {
        Editor editor = mPreferences.edit();
        editor.putLong( name, value );
        editor.commit();
    }

    /**
     * 保存long类型数据
     */
    public long getLong( String name ) {
        return mPreferences.getLong( name, -1 );
    }

    /**
     * 根据key读取参数
     */
    public String getInfo( String key ) {
        return mPreferences.getString( key, "" );
    }

    /**
     * 读取状�?
     */
    public boolean getBoolean( String key, boolean def ) {
        return mPreferences.getBoolean( key, def );
    }

    /**
     * 读取状�?
     */
    public void putBoolean( String key, boolean val ) {
        Editor editor = mPreferences.edit();
        editor.putBoolean( key, val );
        editor.commit();
    }
    
    /**
     * 保存int类型数据
     */
    public void putInt( String name, int value ) {
    	if (PRE_HOME_TIME.equals(name)) {
    		 setHomeTime(value);
    	} else if (PRE_ALERT_TIME.equals(name)) {
    		 setAlertTime(value);
    	} else {
    		Editor editor = mPreferences.edit();
    		editor.putInt( name, value );
    		editor.commit();
    	}
    }

    /**
     * 保存int类型数据
     */
    public int getInt( String name , int defaultValue) {
    	if (PRE_HOME_TIME.equals(name)) {
    		return getAlertTime();
    	} else if (PRE_ALERT_TIME.equals(name)) {
    		return getAlertTime();
    	}
        return mPreferences.getInt( name, defaultValue);
    }

    /**
     * 获取首页刷新时间
     */
    public int getHomeTime() {
        int initTime = mPreferences.getInt(PRE_HOME_TIME, DEFAULT_HOME_TIME);
        return initTime >  DEFAULT_HOME_TIME ? initTime : DEFAULT_HOME_TIME;
    }
    
    /**
     * 设置首页刷新时间
     */
    public void setHomeTime(int value) {
    	Editor editor = mPreferences.edit();
    	editor.putInt( PRE_HOME_TIME, value > DEFAULT_HOME_TIME ? value : DEFAULT_HOME_TIME );
    	editor.commit();
    }

    /**
     * 获取预警刷新时间
     */
    public int getAlertTime() {
        int initTime = mPreferences.getInt(PRE_ALERT_TIME, DEFAULT_ALERT_TIME);
        return initTime >  DEFAULT_ALERT_TIME ? initTime : DEFAULT_ALERT_TIME;
    }
    
    /**
     * 设置预警刷新时间
     */
    public void setAlertTime(int value) {
    	Editor editor = mPreferences.edit();
    	editor.putInt(PRE_ALERT_TIME, value > DEFAULT_ALERT_TIME ? value : DEFAULT_ALERT_TIME);
    	editor.commit();
    }
}
