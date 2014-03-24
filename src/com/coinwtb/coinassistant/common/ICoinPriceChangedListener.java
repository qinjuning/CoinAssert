package com.coinwtb.coinassistant.common;

import com.coinwtb.coinassistant.bean.CoinBean;

public interface ICoinPriceChangedListener {
	/**
	 * 获取某个币种的详细信息
	 * @param coinBean
	 */
	void onCoinPriceChanged(CoinBean coinBean);
	
	/**
	 * 获取某个币种的最新价格
	 * @param coinBean
	 */
	void onCoinPriceChanged(double lastApi, String coinName);
}