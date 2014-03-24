package com.coinwtb.coinassistant.common;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteException;

import com.coinwtb.coinassistant.bean.CoinBean;
import com.coinwtb.coinassistant.ui.base.CoinApplication;

/**
 * TODO 数据库操作
 * 
 */
public class DBAdapter {


	private static DBAdapter mInstance;
	private CoinsDatabaseHelper mCoinDbHelper;
	private SQLiteDatabase mSqLiteDb;
	public static Context mContext;

	int pageSize = 10;

	private DBAdapter(Context context) {
		mContext = context;
	}

	public static synchronized DBAdapter getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DBAdapter(context);
			mContext = context;
		}
		return mInstance;
	}

	/**
	 * 打开数据库
	 * 
	 * @throws SQLiteException
	 */
	public void open() throws SQLiteException {
		mCoinDbHelper = new CoinsDatabaseHelper(mContext);
		mSqLiteDb = mCoinDbHelper.getWritableDatabase();
	}

	public boolean isOpen() {
		if (mSqLiteDb != null)
			return mSqLiteDb.isOpen();
		else {
			return false;
		}
	}

	/**
	 * 关闭数据库
	 */
	public void close() {
		closeDB();
		mInstance = null;
	}
	
	private void closeDB() {
		if (mCoinDbHelper != null) {
			mCoinDbHelper.close();
		}
	}

	/**
	 * 向表中添加一条数据
	 * 
	 * @param table
	 *            表名
	 * @param newValues
	 *            值
	 * @return
	 */
	public long insert(String table, ContentValues newValues) {
		return mSqLiteDb.insert(table, null, newValues);
	}

	/**
	 * 根据id更新数据实体信息类
	 * 
	 * @param table
	 *            表名
	 * @param newValues
	 *            值
	 * @return
	 */
	public long updateCoinBeanWaringState(String table, CoinBean coinBean) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(CoinsDatabaseHelper.COLUMN_UPPER_PRICE, coinBean.getUpperPrice());
		contentValues.put(CoinsDatabaseHelper.COLUMN_LOWER_PRICE, coinBean.getLowerPrice());
		contentValues.put(CoinsDatabaseHelper.COLUMN_NOTIFY_DEFAULTS, coinBean.getNotifyDefaults());
		contentValues.put(CoinsDatabaseHelper.COLUMN_IS_WARNING, coinBean.getIsWarning());
		contentValues.put(CoinsDatabaseHelper.COLUMN_REFRESH_TIME, coinBean.getRefreshTime());
		long id = mSqLiteDb.update(table, contentValues, CoinsDatabaseHelper.COLUMN_ID + " = ? ", 
				new String[]{String.valueOf(coinBean.getId()), });
		CoinApplication.getInstance().updateWaringState(coinBean);
		return id;
	}
	
	/**
	 * 插入实体类
	 * 
	 * @param msg
	 * @return
	 */
	public boolean insert(CoinBean coinBean, String tableName) {
		return mCoinDbHelper.insterItem(coinBean, mSqLiteDb, tableName);
	}

	/**
	 * 根据条件删除数据
	 * 
	 * @param table
	 *            表名
	 * @param where
	 *            条件
	 * @return
	 */
	public long deleteOneData(String table, String where) {
		return mSqLiteDb.delete(table, where, null);
	}

	/**
	 * 删除所有数据
	 * 
	 * @param table
	 *            表名
	 * @return
	 */
	public long deleteAllData(String table) {
		return mSqLiteDb.delete(table, null, null);
	}

	/**
	 * 更新一条数据
	 * 
	 * @param table
	 *            表名
	 * @param newValues
	 *            值
	 * @param where
	 *            条件
	 * @return
	 */
	private long update(String table, ContentValues newValues, String where) {
		return mSqLiteDb.update(table, newValues, where, null);
	}


	/**
	 * 删除一条记录
	 * 
	 * @param id
	 *            记录id
	 * @return
	 */
	public boolean delete(int id, String tableName) {
		try {
			execSQL("delete from " + tableName + " where " + CoinsDatabaseHelper.COLUMN_ID + " = " + id + "");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void execSQL(String sql) throws SQLException {
		try {
			mSqLiteDb.execSQL(sql);
		} catch (SQLiteDatabaseCorruptException e) {
			throw e;
		}
	}

	
	/**
	 * 根据sql查询
	 * 
	 * @param sql
	 *            语句
	 * @return 出错时返回null
	 */
	public List<CoinBean> queryAllData(String tableName) {
		try {
			StringBuilder sb = new StringBuilder(" select * from ");
			sb.append(tableName);
			Cursor result = mSqLiteDb.rawQuery(sb.toString(), null);
			return getAllCoins(result);
		} catch (Exception e) {
			return null;
		}

	}
	
	/**
	 * 将查询结果cursor转化为collection
	 * 
	 * @param cursor
	 *            查询结果
	 * @return
	 */
	private List<CoinBean> getAllCoins(Cursor cursor) {
		List<CoinBean> coinList = new ArrayList<CoinBean>();
		if (cursor == null || cursor.getCount() == 0 || !cursor.moveToFirst()) {
			return coinList;
		}
		
		int _idIndex = cursor.getColumnIndex(CoinsDatabaseHelper.COLUMN_ID);
		int _enIndex = cursor.getColumnIndex(CoinsDatabaseHelper.COLUMN_EN_NAME);
		int _cnIndex = cursor.getColumnIndex(CoinsDatabaseHelper.COLUMN_CN_NAME);
		int _shortIndex = cursor.getColumnIndex(CoinsDatabaseHelper.COLUMN_SHORT_NAME);
		int _transferIndex = cursor.getColumnIndex(CoinsDatabaseHelper.COLUMN_TRANSFER_TYPE);
		int _apiUrlIndex = cursor.getColumnIndex(CoinsDatabaseHelper.COLUMN_API_URL);
		int _flagsIndex = cursor.getColumnIndex(CoinsDatabaseHelper.COLUMN_NOTIFY_FLAGS);
		int _defaultsIndex = cursor.getColumnIndex(CoinsDatabaseHelper.COLUMN_NOTIFY_DEFAULTS);
		int _soundIndex = cursor.getColumnIndex(CoinsDatabaseHelper.COLUMN_NOTIFY_SOUND);
		int _upperIndex = cursor.getColumnIndex(CoinsDatabaseHelper.COLUMN_UPPER_PRICE);
		int _lowerIndex = cursor.getColumnIndex(CoinsDatabaseHelper.COLUMN_LOWER_PRICE);
		int _wraningIndex = cursor.getColumnIndex(CoinsDatabaseHelper.COLUMN_IS_WARNING);
		int _refershIndex = cursor.getColumnIndex(CoinsDatabaseHelper.COLUMN_REFRESH_TIME);
		
		while (!cursor.isAfterLast()) {
			CoinBean coinBean = new CoinBean();
			coinBean.setId(cursor.getInt(_idIndex));
			coinBean.setEnName(cursor.getString(_enIndex));
			coinBean.setCnName(cursor.getString(_cnIndex));
			coinBean.setShortName(cursor.getString(_shortIndex));
			coinBean.setTransferType(cursor.getString(_transferIndex));
			coinBean.setApiUrl(cursor.getString(_apiUrlIndex));
			coinBean.setNotifyFlags(cursor.getInt(_flagsIndex));
			coinBean.setNotifyDefaults(cursor.getInt(_defaultsIndex));
			coinBean.setNotifySound(cursor.getString(_soundIndex));
			coinBean.setUpperPrice(cursor.getDouble(_upperIndex));
			coinBean.setLowerPrice(cursor.getDouble(_lowerIndex));
			coinBean.setIsWarning(cursor.getInt(_wraningIndex) == 1);
			coinBean.setRefreshTime(cursor.getInt(_refershIndex));
			coinList.add(coinBean);
			
			cursor.moveToNext();
		}
		return coinList;
	}

}
