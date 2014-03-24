package com.coinwtb.coinassistant.widget;

import java.util.Calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coinwtb.coinassistant.R;
import com.coinwtb.coinassistant.utils.FormatUtils;

/**
 * 选择的虚拟货币实时价格显示表
 *
 */
public class CoinRealPriceRelativeLayout extends RelativeLayout {

	private static final String TAG = "CoinRealPriceLinearLayout";
	
	
	private TextView mTvLastPrice;   // 最新价
	private TextView mTvHighPrice;   // 最高价
	private TextView mTvLowPrice;    // 最低价
	private TextView mTvSellPrice;   // 卖一价
	private TextView mTvBuytPrice;   // 买一价
	private TextView mTvVolume;      // 成交量
	private TextView mTvWaringHigh;       // 预警上限
	private TextView mTvWaringLower;      // 预警下限
	private TextView mTvRefreshTime;      // 更新时间
	
	public CoinRealPriceRelativeLayout(Context context) {
		this(context, null);
	}
	
	public CoinRealPriceRelativeLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public CoinRealPriceRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onFinishInflate() {
		mTvLastPrice = (TextView) findViewById(R.id.tv_coin_last_price);
		mTvHighPrice = (TextView) findViewById(R.id.tv_coin_high_price);
		mTvLowPrice = (TextView) findViewById(R.id.tv_coin_low_price);
		mTvSellPrice = (TextView) findViewById(R.id.tv_coin_sell_price);
		mTvBuytPrice = (TextView) findViewById(R.id.tv_coin_buy_price);
		mTvVolume = (TextView) findViewById(R.id.tv_coin_volume);
		mTvWaringHigh = (TextView) findViewById(R.id.tv_coin_waring_high);
		mTvWaringLower = (TextView) findViewById(R.id.tv_coin_waring_lower);
		mTvRefreshTime = (TextView) findViewById(R.id.tv_coin_update_time);
		initData();
	}
	
	// 初始化数据
	private void initData() {
		// 判断是否存在该控件，存在则给于初始化
		if (mTvSellPrice != null) {
			setLastPrice(0.0f);
			setLowerPrice(0.0f);
			setHighPrice(0.0f);
			setBuy1Price(0.0f);
			setSellPrice(0.0f);
			setCoinVolume(0.0f);
		}
	}
	
	private void setUiDisplay(TextView tv, String format, Object value) {
		tv.setText(FormatUtils.makeFormatString(format, value));;
	}
	
	public void setLastPrice(double lastPrice) {
		setUiDisplay(mTvLastPrice, getResources().getString(R.string.coin_last_price_format), lastPrice);
	}
	
	public void setLowerPrice(double lowerPrice) {
		setUiDisplay(mTvLowPrice, getResources().getString(R.string.coin_low_price_format), lowerPrice);
	}

	public void setHighPrice(double hightPrice) {
		setUiDisplay(mTvHighPrice, getResources().getString(R.string.coin_high_price_format), hightPrice);
	}
	
	public void setBuy1Price(double buyPrice) {
		setUiDisplay(mTvBuytPrice, getResources().getString(R.string.coin_buy_price_format), buyPrice);
	}

	public void setSellPrice(double sellPrice) {
		setUiDisplay(mTvSellPrice, getResources().getString(R.string.coin_sell_price_format), sellPrice);
	}
	
	public void setCoinVolume(double volume) {
		if (mTvVolume != null) {
			setUiDisplay(mTvVolume, getResources().getString(R.string.coin_volume_format), volume);
		}
	}
	
	public void setWaringHigh(double high) {
		if (mTvWaringHigh != null) {
			mTvWaringHigh.setVisibility(View.VISIBLE);
			setUiDisplay(mTvWaringHigh, getResources().getString(R.string.coin_waring_high), high);
		}
	}
	
    public void setWaringLower(double lower) {
    	if (mTvWaringLower != null) {
    		mTvWaringLower.setVisibility(View.VISIBLE);
        	setUiDisplay(mTvWaringLower, getResources().getString(R.string.coin_waring_lower), lower);
    	}
	}
    
    /**
     * 
     * 设置时间段
     */
    public void setRefreshTime() {
    	if (mTvRefreshTime != null) {
    		mTvRefreshTime.setVisibility(View.VISIBLE);
    		String time = FormatUtils.makeLongDateStringFromCalendar(Calendar.getInstance());
    		setUiDisplay(mTvRefreshTime, getResources().getString(R.string.coin_waring_update_time), time);
    	}
	}
	
}
