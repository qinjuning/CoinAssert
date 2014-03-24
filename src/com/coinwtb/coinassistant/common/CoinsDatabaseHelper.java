/*
 * 
 */

package com.coinwtb.coinassistant.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.text.TextUtils;

import com.coinwtb.coinassistant.bean.CoinBean;
import com.coinwtb.coinassistant.ui.base.CoinApplication;
import com.coinwtb.coinassistant.utils.Slog;

public class CoinsDatabaseHelper extends CommonDatabaseHelper {

	private static final String TAG = CoinsDatabaseHelper.class.getSimpleName();
	
	private static final String DB_NAME = "coins.db";  
	
	public static final String BTER_TABLE_NAME = "bter";
	
	public static final String TBL_NAME = "push_message";  
	
	/**
	 * @see CoinBean
	 */
	public static final String COLUMN_ID= "id";  
	public static final String COLUMN_EN_NAME= "en_name";  
	public static final String COLUMN_CN_NAME= "cn_name";  
	public static final String COLUMN_SHORT_NAME = "short_name";  
	public static final String COLUMN_TRANSFER_TYPE = "transfer_type"; 
	public static final String COLUMN_API_URL = "api_url";  
	public static final String COLUMN_NOTIFY_FLAGS = "notify_flags";  
	public static final String COLUMN_NOTIFY_DEFAULTS= "notify_defaults";  
	public static final String COLUMN_NOTIFY_SOUND = "notify_sound";  
	public static final String COLUMN_UPPER_PRICE = "upper_price";  
	public static final String COLUMN_LOWER_PRICE = "lower_price";  
	public static final String COLUMN_IS_WARNING = "is_warning";  
	public static final String COLUMN_REFRESH_TIME = "refresh_time";  
	
	private static CoinsDatabaseHelper _INSTANCE;
	
	public CoinsDatabaseHelper( Context context) {
	    this(context, null);
	}
	
    public CoinsDatabaseHelper( Context context, CursorFactory cursorFactory ) {
        super( context, DB_NAME, cursorFactory);
    }

    @Override
    public void onCreate( SQLiteDatabase db ) {
    	super.onCreate(db);
    }

    public void onCreateDatabase() {
    	List<Field> fieldList = new ArrayList<Field>();
    	fieldList.add(new Field(COLUMN_ID, FieldType.FieldInteger));
    	fieldList.add(new Field(COLUMN_EN_NAME, FieldType.FieldString, 10));
    	fieldList.add(new Field(COLUMN_CN_NAME, FieldType.FieldString, 10));
    	fieldList.add(new Field(COLUMN_SHORT_NAME, FieldType.FieldString, 10));
    	fieldList.add(new Field(COLUMN_TRANSFER_TYPE, FieldType.FieldString, 5));
    	fieldList.add(new Field(COLUMN_API_URL, FieldType.FieldString, 60));
    	fieldList.add(new Field(COLUMN_NOTIFY_FLAGS, FieldType.FieldInteger, 10));
    	fieldList.add(new Field(COLUMN_NOTIFY_DEFAULTS, FieldType.FieldInteger, 10));
    	fieldList.add(new Field(COLUMN_NOTIFY_SOUND, FieldType.FieldString, 20));
    	fieldList.add(new Field(COLUMN_UPPER_PRICE, FieldType.FieldDouble));
    	fieldList.add(new Field(COLUMN_LOWER_PRICE, FieldType.FieldDouble));
    	fieldList.add(new Field(COLUMN_IS_WARNING, FieldType.FieldInteger, 2));
    	fieldList.add(new Field(COLUMN_REFRESH_TIME, FieldType.FieldInteger));
    
    	createTable(BTER_TABLE_NAME, fieldList);
    	
    	String bterValue = readStringFromAssetByName(BTER_TABLE_NAME);
    	if (null != bterValue) {
    		try {
				JSONObject bterJson = new JSONObject(bterValue);
				String platform = bterJson.getString(CoinJsonFields.PLATFORM);
				String api_format = bterJson.getString(CoinJsonFields.API_FORMAT);
				JSONArray bterArray = bterJson.getJSONArray(CoinJsonFields.COINS_SUPPERT_LIST);
				List<CoinBean> toAddCoinBeans = new ArrayList<CoinBean>();
				for (int i = 0; i < bterArray.length(); i++) {
    				JSONObject coinJson = bterArray.getJSONObject(i);
    				CoinBean coinBean = new CoinBean();
    				int id = coinJson.getInt(CoinJsonFields.ID);
    				coinBean.setId(id);
    				String cn_name = coinJson.getString(CoinJsonFields.CN_NAME);
    				coinBean.setCnName(cn_name);
    				String en_name = coinJson.getString(CoinJsonFields.EN_NAME);
    				coinBean.setEnName(en_name);
    				String short_name = coinJson.getString(CoinJsonFields.SHORT_NAME);
    				coinBean.setShortName(short_name);
    				String transfer_type = coinJson.getString(CoinJsonFields.TRANSFER_TYPE);
    				coinBean.setTransferType(transfer_type);
    				String api_url = coinJson.getString(CoinJsonFields.API_URL);
    				if (TextUtils.isEmpty(api_url)) {
    					api_url = String.format(api_format, short_name.toLowerCase(), transfer_type.toLowerCase());
    				} 
    				coinBean.setApiUrl(api_url);
    				toAddCoinBeans.add(coinBean);
    				Slog.v(TAG, "coinBean=" + coinBean);
    			}
				insterItems(toAddCoinBeans, BTER_TABLE_NAME);
			} catch (JSONException e) {
				e.printStackTrace();
			}
    	}
    }
    
	@Override
	public void onUpgradeDatabase() {
		
	}  

	public static synchronized CoinsDatabaseHelper getInstance(Context context) {
		_INSTANCE = new CoinsDatabaseHelper(context);
		return _INSTANCE;
	}
	
	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
	    if (mDatabase != null) {
	    	return mDatabase;
	    }
		return super.getWritableDatabase();
	}
	
	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		if (mDatabase != null) {
		   return mDatabase;
		}
		return super.getReadableDatabase();
	}
	
	private String readStringFromAssetByName(String fileName) {
		InputStream inputstream = null;
		try {
			inputstream = CoinApplication.getInstance().getResources().getAssets().open(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != inputstream) {
				try {
					inputstream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public boolean insterItems(List<CoinBean> coinBeans, String tableName) {
		if (coinBeans == null || coinBeans.isEmpty()) {
			return false;
		}
		boolean result = false;
		mDatabase.beginTransaction();
		for (int i = 0, N = coinBeans.size() ; i < N ; i++) {
			result |= insterItem(coinBeans.get(i),mDatabase, tableName);
		}
		mDatabase.setTransactionSuccessful();
		mDatabase.endTransaction();
		
		return result;
	}
	
	public boolean insterItem(CoinBean coinBean, SQLiteDatabase db, String tableName) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(COLUMN_ID, coinBean.getId());
		contentValues.put(COLUMN_EN_NAME, coinBean.getEnName());
		contentValues.put(COLUMN_CN_NAME, coinBean.getCnName());
		contentValues.put(COLUMN_SHORT_NAME, coinBean.getShortName());
		contentValues.put(COLUMN_TRANSFER_TYPE, coinBean.getTransferType());
		contentValues.put(COLUMN_API_URL, coinBean.getApiUrl());
		contentValues.put(COLUMN_NOTIFY_FLAGS, coinBean.getNotifyFlags());
		contentValues.put(COLUMN_NOTIFY_DEFAULTS, coinBean.getNotifyDefaults());
		contentValues.put(COLUMN_NOTIFY_SOUND, coinBean.getNotifySound());
		contentValues.put(COLUMN_UPPER_PRICE, coinBean.getUpperPrice());
		contentValues.put(COLUMN_LOWER_PRICE, coinBean.getLowerPrice());
		contentValues.put(COLUMN_IS_WARNING, coinBean.getIsWarning());
		contentValues.put(COLUMN_REFRESH_TIME, coinBean.getRefreshTime());
		
		return db.insert(tableName, null, contentValues) != -1;
	}
	
}
