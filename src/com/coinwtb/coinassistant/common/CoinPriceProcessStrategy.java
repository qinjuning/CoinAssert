package com.coinwtb.coinassistant.common;

import com.coinwtb.coinassistant.bean.CoinBean;
import com.coinwtb.coinassistant.bean.CoinPriceBean;
import com.coinwtb.coinassistant.ui.base.CoinApplication;
import com.coinwtb.coinassistant.utils.PreferencesService;
import com.coinwtb.coinassistant.utils.Slog;


/**
 * 获取API以及处理价格的策略类
 * 
 *
 */
public class CoinPriceProcessStrategy {
	ICoinPriceChangedListener listener;
	
	/**
	 * 是否设置了默认的获取货币实体信息
	 */
	private CoinBean mCoinBean;
	/**
	 * 是否是预警
	 */
	private boolean isWaring;
	
	public CoinPriceProcessStrategy(ICoinPriceChangedListener listener) {
		this.listener = listener;
		this.isWaring = false;
	}
	
	public CoinPriceProcessStrategy(ICoinPriceChangedListener listener, boolean isWaring) {
		this.listener = listener;
		this.isWaring = isWaring;
	}
	
	/*
	 * 设置查看对应的实体价格实体
	 */
	public CoinPriceProcessStrategy(ICoinPriceChangedListener listener, CoinBean coinBean) {
		this(listener, coinBean, false);
	}
	
	
	/*
	 * 设置查看对应的实体价格实体
	 */
	public CoinPriceProcessStrategy(ICoinPriceChangedListener listener, CoinBean coinBean, boolean isWaring) {
		this.listener = listener;
		this.mCoinBean = coinBean;
		this.isWaring = isWaring;
	}

	public String getApiUrl() {
		final CoinBean coinBean = mCoinBean != null ? mCoinBean : CoinApplication.getInstance().getShowCoinBean();
		return coinBean.getApiUrl();
	}
	
	public void processCoinPrice(CoinPriceBean coinPriceBean) {
		final CoinBean coinBean = mCoinBean != null ? mCoinBean : CoinApplication.getInstance().getShowCoinBean();
		//默认添加至列表中
		coinBean.addCoinPrice(coinPriceBean);
		sendPriceChanged(coinBean);
	}

	private void sendPriceChanged(CoinBean coinBean) {
		if (listener != null) {
			Slog.d(CoinPriceProcessStrategy.class.getSimpleName(), "sendPriceChanged=" + coinBean);
			listener.onCoinPriceChanged(coinBean);
		}
	}
	
	/**
	 * 获取线程睡眠时间
	 * @return
	 */
	public long sleepTime() {
		if (isWaring) {
			return mCoinBean != null ? mCoinBean.getRefreshTime() : PreferencesService.getInstance().getAlertTime();
		}
		return PreferencesService.getInstance().getHomeTime();
	}
} 

class StringApiCoinPriceStrategy extends CoinPriceProcessStrategy {

	private String apiUrl;
	
	public StringApiCoinPriceStrategy(ICoinPriceChangedListener listener, String apiUrl) {
		super(listener);
		this.apiUrl = apiUrl;
	}

	public String getApiUrl() {
		return apiUrl;
	}
	
	public void processCoinPrice(CoinPriceBean coinPriceBean) {
		if (listener != null) {
			Slog.d(StringApiCoinPriceStrategy.class.getSimpleName(), "sendPriceChanged=" + coinPriceBean);
			listener.onCoinPriceChanged(coinPriceBean.getLastPrice(), coinPriceBean.getCoinName());
		}
		
	}
}
