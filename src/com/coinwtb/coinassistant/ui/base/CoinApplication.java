package com.coinwtb.coinassistant.ui.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.baidu.frontia.FrontiaApplication;
import com.coinwtb.coinassistant.bean.CoinBean;
import com.coinwtb.coinassistant.common.CoinsDatabaseHelper;
import com.coinwtb.coinassistant.common.DBAdapter;
import com.coinwtb.coinassistant.fragment.CoinWaringFragment;
import com.coinwtb.coinassistant.ui.CoinWaringManagerService;
import com.coinwtb.coinassistant.utils.Constants;
import com.coinwtb.coinassistant.utils.PreferencesService;
import com.coinwtb.coinassistant.utils.Slog;
import com.coinwtb.coinassistant.utils.UncaughtCrashHandler;


public class CoinApplication extends FrontiaApplication {
	
	String TAG = "CoinApplication";
	
	private static CoinApplication _INSTANCE;

	private List<Activity> mActivityList = new LinkedList<Activity>();
	
	private DBAdapter mDbAdapter;
	
	// 保存所有存放的虚拟货币列表
	private static List<CoinBean> mBterCoins = null;
	// 保存所有设置为预警的虚拟货币列表
	private static List<CoinBean> mBterWaringCoins = new ArrayList<CoinBean>();;
	
	/**
	 * 当前显示货币价格的虚拟货币索引
	 */
	private volatile int mShowPriceIndex = 0;

	private int index;
	
	@Override
	public void onCreate() {
		super.onCreate();
		_INSTANCE = this;
		mDbAdapter = DBAdapter.getInstance(this);
		mDbAdapter.open();
		UncaughtCrashHandler.getInstance().init(this);
		mShowPriceIndex = PreferencesService.getInstance().getInt(PreferencesService.PRE_LAST_SHOW_COIN_INDEX, 0);
	}

	public static CoinApplication getInstance() {
		return _INSTANCE;
	}
	
	public int getLastShowPriceIndex() {
		return mShowPriceIndex;
	}
	
	public DBAdapter getDbAdapter() {
		openDbAdapter();
		return mDbAdapter;
	}

	private void openDbAdapter() {
		if (mDbAdapter != null && !mDbAdapter.isOpen()) {
			mDbAdapter.open();
		}
	}
	
	/**
	 * 查询数据，该方法可能由其他线程调用
	 * @return 是否查询成功
	 */
	public synchronized boolean startQueryDatabase() {
		// 数据已查询
		if (mBterCoins != null && !mBterCoins.isEmpty()) {
			return true;
		}
		if (!mDbAdapter.isOpen()) {
			mDbAdapter.open();
		}
		mBterCoins = mDbAdapter.queryAllData(CoinsDatabaseHelper.BTER_TABLE_NAME);
		Slog.d(TAG, mBterCoins.toString());
		synchronized (mBterWaringCoins) {
			if (mBterCoins != null || mBterCoins.isEmpty()) {
				for (int i = 0; i < mBterCoins.size(); i++) {
					if (mBterCoins.get(i).getIsWarning()) {
						mBterWaringCoins.add(mBterCoins.get(i));
					}
				}
			}
			Slog.d(TAG, "mBterWaringCoins=" + mBterWaringCoins.toString());
		}
		//扫描完成启动预警Service
		Intent service = new Intent(CoinApplication.getInstance(), CoinWaringManagerService.class);
		service.setAction(Constants.ACTION_COIN_WARING_ALL);
		CoinApplication.getInstance().startService(service);
		return mBterCoins != null;
	}
	
	public synchronized List<CoinBean> getBterCoinList() {
		Slog.d(TAG, "get mBterCoins = " + mBterCoins.toString());
		return Collections.unmodifiableList(mBterCoins);
	}
	
	public synchronized List<CoinBean> getBterWaringCoinList() {
		synchronized (mBterWaringCoins) {
			Slog.d(TAG, "get mBterWaringCoins = " + mBterWaringCoins.toString());
			return Collections.unmodifiableList(mBterWaringCoins);
		}
	}
	
	public boolean updateWaringState(CoinBean coinBean) {
		synchronized (mBterWaringCoins) {
			boolean found = false;
			for (int i = 0;i < mBterWaringCoins.size(); i++) {
				if (mBterWaringCoins.get(i).equals(coinBean)) {
					found = true;
				}
			}
			if (found) {
				if (!coinBean.getIsWarning()) {
					// 删除
					mBterWaringCoins.remove(coinBean);  
				} 
			} else {
				if (coinBean.getIsWarning()) {
					// 添加至列表中
					mBterWaringCoins.add(coinBean);  
				} 
			}
			Slog.d(TAG, "update mBterWaringCoins=" + mBterWaringCoins.toString());
			return false;
		}
	}
	
	/**
	 * 根据货币ID找到对应的实体信息类
	 * @param id
	 * @return
	 */
	public CoinBean getWaringCoinById(int id) {
		synchronized (mBterWaringCoins) {
			for (int i = 0; i < mBterWaringCoins.size(); i++) {
				if (mBterWaringCoins.get(i).getId() == id) {
					if (mBterWaringCoins.get(i).getIsWarning()) {
						return mBterWaringCoins.get(i);
					}
					return null;
				}
			}
			return null;
		}
	}
	
	/**
	 * 根据货币ID找到对应的实体信息类
	 * @param id
	 * @return
	 */
	public synchronized CoinBean getCoinById(int id) {
		for (int i = 0; i < mBterCoins.size(); i++) {
			if (mBterCoins.get(i).getId() == id) {
				if (mBterCoins.get(i).getIsWarning()) {
					return mBterCoins.get(i);
				}
				return null;
			}
		}
		return null;
	}
	
	/**
	 *  保存索引
	 */
	public void saveLastShowPriceIndex() {
		PreferencesService.getInstance().putInt(PreferencesService.PRE_LAST_SHOW_COIN_INDEX, mShowPriceIndex);
	}
	
	public void setShowPriceIndex(int index) {
		if (index >= 0 && index < mBterCoins.size()) {
			mShowPriceIndex = index;
		} 
	}
	
	public synchronized CoinBean getShowCoinBean() {
		if (mBterCoins == null) {
			return null;
		}
		if (mShowPriceIndex >= 0 && mShowPriceIndex < mBterCoins.size()) {
			return mBterCoins.get(mShowPriceIndex);
		} 
		return null;
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	public void addActivity(Activity activity) {
		if (!mActivityList.contains(activity)) {
			mActivityList.add(activity);
		}
	}
	
    public void removeActivity(Activity activity) {
    	mActivityList.remove(activity);
	}
	
    /**
     * 结束所有Activity
     */
    public void exit() {
    	saveLastShowPriceIndex();
    	mDbAdapter.close();
    	for (Activity activity : mActivityList) {
    		if (activity != null) {
    			activity.finish();
    		}
    	}
    	mActivityList.clear();
    }
	
}
