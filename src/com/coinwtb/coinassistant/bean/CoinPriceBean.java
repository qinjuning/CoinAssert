package com.coinwtb.coinassistant.bean;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 虚拟货币实时价格
 *
 */
public class CoinPriceBean implements Comparable<CoinPriceBean>{

	private double lastPrice;                                  // 最新价
	private double hightPrice;                                 // 最高价
	private double lowPrice;                                   // 最低价
	private double avgPrice;                                   // 24小时平均价
	private double selle1Price;                                // 卖一价
	private double buy1Price;                                  // 买一价
	private double tradingVolume;                              // 24小时成交量
	
	private String coinName;                                   // 该价格所对应的货币名称
	
	private long retriveTimeInMills = System.currentTimeMillis();     // 获取时间 ,已ms为单位
	
	public CoinPriceBean() {
		
	}

	// 判断返回结果是否正确
	public static boolean isPriceResultSuccess(JSONObject jsonObject) {
		try {
			return "true".equals(jsonObject.getString("result"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	// 从Json中解析，
	//形如：{"result":"true","last":0.1883,"high":0.21,"low":0.1762,"avg":0.1873,"sell":0.1978,"buy":0.1883,"vol_bqc":389583.491,"vol_cny":72974.8319}
	public static CoinPriceBean parseCoinPriceFromJson(JSONObject jsonObject) {
		try {
			CoinPriceBean coinPriceBean = new CoinPriceBean();
			coinPriceBean.setLastPrice(jsonObject.getDouble("last"));
			coinPriceBean.setHightPrice(jsonObject.getDouble("high"));
			coinPriceBean.setLowPrice(jsonObject.getDouble("low"));
			coinPriceBean.setAvgPrice(jsonObject.getDouble("avg"));
			coinPriceBean.setSelle1Price(jsonObject.getDouble("sell"));
			coinPriceBean.setBuy1Price(jsonObject.getDouble("buy"));
			Iterator<String> iterator = (Iterator<String>)jsonObject.keys();
			while (iterator.hasNext()) {
				String key = iterator.next();
				if (key.startsWith("vol")) {
					// 如果是人民币交易则跳过
					if (key.contains("cny")) {
						continue;
					}
					coinPriceBean.setTradingVolume(jsonObject.getDouble(key));
					int _postion = key.indexOf("_");
					if (_postion != -1) {
						coinPriceBean.coinName = key.substring(_postion + 1);
					}
				}
			}
			return coinPriceBean;
		} catch (JSONException jsonException) {
			jsonException.printStackTrace();
		}
		return null;
	}
	
	public double getLastPrice() {
		return lastPrice;
	}

	public void setLastPrice(double lastPrice) {
		this.lastPrice = lastPrice;
	}

	public double getHightPrice() {
		return hightPrice;
	}

	public void setHightPrice(double hightPrice) {
		this.hightPrice = hightPrice;
	}

	public double getLowPrice() {
		return lowPrice;
	}

	public void setLowPrice(double lowPrice) {
		this.lowPrice = lowPrice;
	}

	public double getAvgPrice() {
		return avgPrice;
	}

	public void setAvgPrice(double avgPrice) {
		this.avgPrice = avgPrice;
	}

	public double getSelle1Price() {
		return selle1Price;
	}

	public void setSelle1Price(double selle1Price) {
		this.selle1Price = selle1Price;
	}

	public double getBuy1Price() {
		return buy1Price;
	}

	public void setBuy1Price(double buy1Price) {
		this.buy1Price = buy1Price;
	}

	public double getTradingVolume() {
		return tradingVolume;
	}

	public void setTradingVolume(double tradingVolume) {
		this.tradingVolume = tradingVolume;
	}

	public long getRetriveTime() {
		return retriveTimeInMills;
	}

	public void setRetriveTime(long retriveTimeInMills) {
		this.retriveTimeInMills = retriveTimeInMills;
	}

	public String getCoinName() {
		return coinName;
	}

	public void setCoinName(String coinName) {
		this.coinName = coinName;
	}

	@Override
	public String toString() {
		return "CoinPriceBean [lastPrice=" + lastPrice + ", hightPrice="
				+ hightPrice + ", lowPrice=" + lowPrice + ", avgPrice="
				+ avgPrice + ", selle1Price=" + selle1Price + ", buy1Price="
				+ buy1Price + ", tradingVolume=" + tradingVolume
				+ ", coinName=" + coinName + ", retriveTimeInMills="
				+ retriveTimeInMills + "]";
	}

	@Override
	public int compareTo(CoinPriceBean another) {
		if (retriveTimeInMills == another.getRetriveTime()) {
			return 0;
		}
		
		return retriveTimeInMills < another.getRetriveTime() ? 1 : -1;
	}
	
	
}
