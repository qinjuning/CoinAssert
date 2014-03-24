package com.coinwtb.coinassistant.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import com.coinwtb.coinassistant.ui.base.CoinApplication;
import com.coinwtb.coinassistant.utils.PreferencesService;
import com.coinwtb.coinassistant.utils.Slog;

import android.app.Notification;

/**
 * 每个虚拟货币对应的实体类
 * @author Administrator
 *
 */
public class CoinBean {
	
	private int id;
	private String cnName;                                //   比特币
	private String enName;                                //   Bitcoin
	private String shortName;                             //   BTC
	private String transferType;                          //   CNY
	private String apiUrl;                                //   http://bter.com/api/1/ticker/btc_cny
	
	// 通知属性
	private int notify_flags = 0;
	private int notify_defaults = Notification.DEFAULT_SOUND;
	private String notify_sound = "";
	
	private int isWarning = 0 ;                           // 是否开启预警，1为开启，0为未开启
	private double upperPrice = 0.0d;                     // 预警的最高值
	private double lowerPrice = 0.0d;                     // 预警的最低值
	private long refreshTime = 0 ;                        // 设置刷新时间 单位：秒
	
	private static final int PRICE_CAPACITY = 30;         // 保存最近30个价格表
	/**
	 * 虚拟货币对应的价格实时表
	 */
	private List<CoinPriceBean> mCoinPriceList = null;
	
	// 是否需要震动
	private boolean isVibrateWhenPopNofication = true;
	
	/**
	 * 是否名称相同
	 * @param name
	 * @return
	 */
	public boolean hasSameName(String name) {
		return enName.equalsIgnoreCase(name) || shortName.equalsIgnoreCase(name) || cnName.equalsIgnoreCase(name);
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCnName() {
		return cnName;
	}
	public void setCnName(String cnName) {
		this.cnName = cnName;
	}
	public String getEnName() {
		return enName;
	}
	public void setEnName(String enName) {
		this.enName = enName;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	/**
	 * 获取英文简称和中文名
	 * @return
	 */
	public String getFullName() {
		return shortName + " " + cnName;
	}
	/**
	 * 获取中文名和兑换类型
	 * @return
	 */
	public String getFullTradingName() {
		return cnName + " " + shortName + "/" + transferType;
	}
	public String getTransferType() {
		return transferType;
	}
	public void setTransferType(String transferType) {
		this.transferType = transferType;
	}
	public String getApiUrl() {
		return apiUrl;
	}
	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}
	
	public int getNotifyFlags() {
		return notify_flags;
	}
	public void setNotifyFlags(int notify_flags) {
		this.notify_flags = notify_flags;
	}
	public int getNotifyDefaults() {
		int defaults = notify_defaults;
		if (!isVibrateWhenPopNofication) {
			return Notification.DEFAULT_SOUND;
		}
		return defaults;
	}
	public void setNotifyDefaults(int notify_defaults) {
		this.notify_defaults |= notify_defaults;
	}
	public void removeNotifyDefaults(int notify_defaults) {
		this.notify_defaults &=  ~notify_defaults;
	}
	public String getNotifySound() {
		return notify_sound;
	}
	public void setNotifySound(String notify_sound) {
		this.notify_sound = notify_sound;
	}
	public synchronized boolean getIsWarning() {
		return isWarning == 1;
	}
	public synchronized void setIsWarning(boolean isWarning) {
		this.isWarning = isWarning ? 1 : 0;
		if (!isWarning) {
			this.upperPrice = 0.0f;
			this.upperPrice = 0.0f;
			this.notify_flags = Notification.DEFAULT_SOUND;
		}
	}
	public long getRefreshTime() {
		int baseTime = PreferencesService.getInstance().getAlertTime();
		return refreshTime > baseTime ? refreshTime :  baseTime;
	}
	
	public void setRefreshTime(long refreshTime) {
		int baseTime = PreferencesService.getInstance().getAlertTime();
		this.refreshTime = refreshTime > baseTime ? refreshTime :  baseTime;
	}
	
	public double getUpperPrice() {
		return upperPrice;
	}
	public void setUpperPrice(double upperPrice) {
		this.upperPrice = upperPrice;
	}
	public double getLowerPrice() {
		return lowerPrice;
	}
	public void setLowerPrice(double lowerPrice) {
		this.lowerPrice = lowerPrice;
	}
	
	public synchronized CoinPriceBean getLastCoinPrice() {
		if (mCoinPriceList == null || mCoinPriceList.isEmpty()) {
			return null;
		}
		CoinPriceBean lastPrice = mCoinPriceList.get(mCoinPriceList.size() - 1);
		Slog.i(this.getClass().getSimpleName(), getFullTradingName() + " @@ LastCoinPrice = " + lastPrice);
		return lastPrice;
	}
	
	public synchronized boolean addCoinPrice(CoinPriceBean coinPriceBean) {
		if (mCoinPriceList == null) {
			mCoinPriceList = new ArrayList<CoinPriceBean>();
		}
		boolean result = mCoinPriceList.add(coinPriceBean);
		if (mCoinPriceList.size() > PRICE_CAPACITY) {
			int end = mCoinPriceList.size() - PRICE_CAPACITY;
			List<CoinPriceBean> toRemovedList = new ArrayList<CoinPriceBean>();
			for (int i = 0; i < end; i++) {
				toRemovedList.add(mCoinPriceList.get(i));
			}
			for (int i = 0; i < toRemovedList.size(); i++) {
				mCoinPriceList.remove(toRemovedList.get(i));
			}
		}
		return result;
	}
	
	public synchronized List<CoinPriceBean> getCoinPriceList() {
		return mCoinPriceList;
	}

	public boolean isVibrateWhenPopNofication() {
		return isVibrateWhenPopNofication;
	}

	public void setVibrateWhenPopNofication(boolean isVibrateWhenPopNofication) {
		this.isVibrateWhenPopNofication = isVibrateWhenPopNofication;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null){
			return false;
		}
		if (object instanceof CoinBean){
			CoinBean that = (CoinBean) object;
			return this.id == that.id;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "CoinBean [id=" + id + ", cnName=" + cnName + ", enName="
				+ enName + ", shortName=" + shortName + ", transferType="
				+ transferType + ", apiUrl=" + apiUrl + ", notify_flags="
				+ notify_flags + ", notify_defaults=" + notify_defaults
				+ ", notify_sound=" + notify_sound + ", isWarning=" + isWarning
				+ ", upperPrice=" + upperPrice + ", lowerPrice=" + lowerPrice
				+ ", refreshTime=" + refreshTime + "]";
	}
}
