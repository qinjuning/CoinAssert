package com.coinwtb.coinassistant.widget;

import java.util.Calendar;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.coinwtb.coinassistant.R;
import com.coinwtb.coinassistant.bean.CoinBean;
import com.coinwtb.coinassistant.bean.CoinPriceBean;
import com.coinwtb.coinassistant.common.CoinPriceEventThread;
import com.coinwtb.coinassistant.common.ICoinPriceChangedListener;
import com.coinwtb.coinassistant.ui.base.CoinApplication;
import com.coinwtb.coinassistant.utils.FormatUtils;
import com.coinwtb.coinassistant.utils.Utils;

/**
 * 虚拟货币的实时价格表 - 首页
 *
 */
public class CoinPriceShowLinearLayout extends LinearLayout {

	private static final String TAG = "CoinPriceShowActivity";
	
	private static final int MSG_COIN_PRICE_CHANGED = 1;
	
	private TextView mTitleTv;       // 标题栏初始化
	private CoinRealPriceRelativeLayout mRealPriceLL;   // 实时价格表
	
	private PriceChartRelativeLayout mPriceChartLayout;    // 折线图
	private ProgressBar mProgressRefresh;
	private TextView mTvRefreshTime;    
	
	private CoinPriceEventThread mCoinPriceThread;   
	
	private String mLastCoinName;    // 保存上一次的货币名
	
	private CoinBean mCoinBean;      // 显示的数据实体
	
	public CoinPriceShowLinearLayout(Context context) {
		this(context, null);
	}
	
	public CoinPriceShowLinearLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public CoinPriceShowLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void refreshCoin() {
		Utils.showToastWithDuration(CoinApplication.getInstance(), R.string.toast_refresh , 5000);
		showRefreshView(true);
		mCoinPriceThread.refresh();
	}
	
	@Override
	protected void onFinishInflate() {
		mTitleTv = (TextView) findViewById(R.id.tv_title);
		mRealPriceLL = (CoinRealPriceRelativeLayout) findViewById(R.id.coin_real_price_rl);
		mPriceChartLayout = (PriceChartRelativeLayout) findViewById(R.id.price_chart_continer);
		mTvRefreshTime = (TextView) findViewById(R.id.tv_refresh_time);
		mProgressRefresh  = (ProgressBar) findViewById(R.id.progress_dialog);
		
		initData();
	   
	    showRefreshView(true);
	}
	
	// 初始化数据
	private void initData() {
		//设置标题栏
		CoinBean coinBean = CoinApplication.getInstance().getShowCoinBean();
		if (coinBean != null) {
			mTitleTv.setText(coinBean.getFullTradingName());
		}
	}
	
	private void showRefreshView(boolean show) {
		mProgressRefresh.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
	}
	
	private void setRefreshTime() {
		mTvRefreshTime.setVisibility(View.VISIBLE);
		// 设置时间段
		String time = FormatUtils.makeLongDateStringFromCalendar(Calendar.getInstance());
		setUiDisplay(mTvRefreshTime, getResources().getString(R.string.coin_waring_update_time), time);
	}
	
	private void setUiDisplay(TextView tv, String format, Object value) {
		tv.setText(FormatUtils.makeFormatString(format, value));;
	}
	
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_COIN_PRICE_CHANGED :
				showRefreshView(false);
				handleEventChanged(msg);
				break;
			default :
				break;
			}
		};
		
		private void handleEventChanged(Message msg) {
			if (msg.obj == null) {
				return;
			}
		    final CoinBean coinBean = (CoinBean) msg.obj;
		    CoinPriceBean priceBean = coinBean.getLastCoinPrice();
		    if (!coinBean.getFullName().equals(mLastCoinName)) {
		    	mLastCoinName = coinBean.getFullTradingName();
		        mTitleTv.setText(mLastCoinName);
		    }
		    mRealPriceLL.setLastPrice(priceBean.getLastPrice());
		    mRealPriceLL.setHighPrice(priceBean.getHightPrice());
		    mRealPriceLL.setLowerPrice(priceBean.getLowPrice());
		    mRealPriceLL.setSellPrice(priceBean.getSelle1Price());
		    mRealPriceLL.setBuy1Price(priceBean.getBuy1Price());
		    mRealPriceLL.setCoinVolume(priceBean.getTradingVolume());
		    setRefreshTime();
		    mCoinBean = coinBean;
		    mPriceChartLayout.setCoinbeanAndRepaint(mCoinBean);
		}
	};

	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		startCoinPriceObserver();
	};
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		stopCoinPriceObserver();
	}
	
	// 启动线程检测价格变化
	private void startCoinPriceObserver() {
		if (mCoinPriceThread != null) {
			mCoinPriceThread.stopped();
		}
		mCoinPriceThread = new CoinPriceEventThread(mCoinPriceListener, true);
		mCoinPriceThread.start();
	}
	
	// 停止检测
	private void stopCoinPriceObserver() {
		if (mCoinPriceThread != null) {
			mCoinPriceThread.stopped();
		}
		mCoinPriceThread = null;
	}
	
	private ICoinPriceChangedListener mCoinPriceListener = 
		 new ICoinPriceChangedListener() {

			@Override
			public void onCoinPriceChanged(CoinBean coinBean) {
				if (mHandler != null) {
					Message msg = mHandler.obtainMessage(MSG_COIN_PRICE_CHANGED, coinBean);
					mHandler.sendMessageDelayed(msg, 500);
				}
			}

			@Override
			public void onCoinPriceChanged(double lastApi, String coinName) {
				
			}

	};
	
}
