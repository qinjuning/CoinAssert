package com.coinwtb.coinassistant.utils;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Utils {

	/**
	 * 获取Baidu ApiKey
	 * @param context
	 * @return
	 */
	public static String getBaiduApiKey(Context context) {
		return getMetaValue(context, "api_key");
	}
	
	// 获取ApiKey
    public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String apiKey = null;
        if (context == null || metaKey == null) {
        	return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
            	apiKey = metaData.getString(metaKey);
            }
        } catch (NameNotFoundException e) {

        }
        return apiKey;
    }
    
    /**
     * 获取货币的 Drawable 的 id, 不会抛出异常
     * @param context
     * @param name
     * @return
     */
    public static int getCoinDrawableIdentifierSafely(Context context, String name) {
    	return getResouceIdentifier(context, name + "_logo", "drawable", false);
    }
    
    public static int getResouceIdentifier(Context context, String name, String defType, boolean throwException) {
        int resId = context.getResources().getIdentifier(name, defType, context.getPackageName());
        if (throwException && resId == 0) {
        	throw new IllegalArgumentException("You must supply the name " + name + " with " + " the type of " + defType);
        }
        return resId;
    }
    
    // Baidu PushServer 用share preference来实现是否绑定的开关。在ionBind且成功时设置true，unBind且成功时设置false
    public static boolean hasBind(Context context) {
    	boolean flag = PreferencesService.getInstance().getBoolean(PreferencesService.PRE_PUSH_BIND_FLAG, false);
		if (flag) {
			return true;
		}
		return false;
    }

    public static void setBind(Context context, boolean flag) {
		PreferencesService.getInstance().putBoolean(PreferencesService.PRE_PUSH_BIND_FLAG, flag);
    }
    
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static String formateWithResId(Context context , int resId, Object...formatArgs) {
		String formater = context.getString(resId, formatArgs);
		return formater;
	}
	
	public static void showLongToast(Context context, int resId) {
		showToastWithDuration(context, context.getString(resId), Toast.LENGTH_LONG);
	}
	
	public static void showShortToast(Context context, int resId) {
		showToastWithDuration(context, context.getString(resId), Toast.LENGTH_SHORT);
	}
	
	public static void showToastWithDuration(Context context, int resId, int duration) {
		showToastWithDuration(context, context.getString(resId), duration);
	}
	
	public static void showLongToast(Context context,CharSequence hint) {
		showToastWithDuration(context, hint, Toast.LENGTH_LONG);
	}
	
	public static void showShortToast(Context context, CharSequence hint) {
		showToastWithDuration(context, hint, Toast.LENGTH_SHORT);
	}
	
	public static void showToastWithDuration(Context context, CharSequence hint, int duration) {
		Toast.makeText(context, hint, duration).show();
	}
	
	private static boolean checkExternalStorage() {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		return mExternalStorageAvailable && mExternalStorageWriteable;
	}
	
	public static final String APP_ROOT_FOLDER_NAME = "coinassisant";
	
    public static File createAppPublicFolderIfRequired(String folderName) throws IOException {
		if (!checkExternalStorage()) {
			return null;
		}

		File f = new File(Environment.getExternalStoragePublicDirectory(APP_ROOT_FOLDER_NAME), folderName);
		if (!f.exists()) {
			f.mkdirs();
		}
		return f;
    }
}
