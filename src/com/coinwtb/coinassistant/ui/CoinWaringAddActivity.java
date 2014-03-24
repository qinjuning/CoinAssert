package com.coinwtb.coinassistant.ui;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.coinwtb.coinassistant.R;
import com.coinwtb.coinassistant.bean.CoinBean;
import com.coinwtb.coinassistant.common.CoinsDatabaseHelper;
import com.coinwtb.coinassistant.common.DBAdapter;
import com.coinwtb.coinassistant.common.ICoinPriceChangedListener;
import com.coinwtb.coinassistant.common.RequestCoinPriceRunnable;
import com.coinwtb.coinassistant.fragment.CoinWaringFragment;
import com.coinwtb.coinassistant.ui.base.BaseActivity;
import com.coinwtb.coinassistant.ui.base.CoinApplication;
import com.coinwtb.coinassistant.utils.FormatUtils;
import com.coinwtb.coinassistant.utils.PreferencesService;
import com.coinwtb.coinassistant.utils.Slog;
import com.coinwtb.coinassistant.utils.Utils;

/**
 * 设置、添加预警管理的货币
 *
 */
public class CoinWaringAddActivity extends BaseActivity{

	private static final String TAG = "CoinWaringAddActivity";
	private static final int MSG_COIN_PRICE_CHANGED = 1;

	private final int EXTRA_SPINEER_HINT = 1;
	
	private ProgressBar mProgressRefresh;
	private TextView mLastPriceTv;
	private EditText mAlertHightPriceTv;
	private EditText mAlertLowerPriceTv;
	private EditText mCoinRefreshTimeTv; 
	private Spinner mSpinnerCoins;
	private CheckBox mVibrateChb;
	private TextView mSubmitView;
	
	// 相对上限
	private View mRelativeHighView;          
	private TextView mRelativeHighEdit;  
	private View mHighDescTv;
	//相对下限
	private View mRelativeLowerView;  
	private TextView mRelativeLowerEdit; 
	private View mLowerDescTv;
	
	private ExecutorService mThreadPool = Executors.newCachedThreadPool();
	
	/*
	 * 设置的货币ID，-1代表添加
	 */
	private int setCoinId = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.coin_waring_add);
		setHeaderTitle(R.string.alert_coin_title);
		
		initViews();
		
		setCoinId= getIntent().getIntExtra(CoinWaringFragment.EXTRA_COIN_ID, -1);
		Slog.i(TAG, "setCoinId = " + setCoinId);
		if (isSetWaringCoin()) {
			setUIDisplay();
		}
		showRefreshView(false);
		setSpinner();
		
		showRelativeControl(false);
	}
	
	// 是否显示相对价格
	private void showRelativeControl(boolean show) {
		mRelativeHighView.setVisibility(show ? View.VISIBLE : View.GONE);
		mRelativeLowerView.setVisibility(show ? View.VISIBLE : View.GONE);
	}
	
	private boolean isSetWaringCoin() {
		return setCoinId != -1;
	}
	
	private void setUIDisplay() {
		CoinBean coinBean = CoinApplication.getInstance().getWaringCoinById(setCoinId);
		Slog.i(TAG, "coinBean = " + coinBean);
		if (coinBean != null) {
			mAlertHightPriceTv.setText(String.valueOf(coinBean.getUpperPrice()));
			mAlertLowerPriceTv.setText(String.valueOf(coinBean.getLowerPrice()));
			mSubmitView.setText(R.string.save);
			mVibrateChb.setChecked((coinBean.getNotifyDefaults() & Notification.DEFAULT_VIBRATE) != 0);
			mCoinRefreshTimeTv.setText(String.valueOf(coinBean.getRefreshTime() / PreferencesService.MILLIONS));
		}
	}
	
	private void initViews() {
		mLastPriceTv = (TextView) findViewById(R.id.edit_last_price);
		mAlertHightPriceTv = (EditText) findViewById(R.id.edit_alert_high);
		mAlertLowerPriceTv = (EditText) findViewById(R.id.edit_alert_lower);
		mSpinnerCoins = (Spinner) findViewById(R.id.spinner_coins);
		mVibrateChb =  (CheckBox) findViewById(R.id.chk_vibrate);
		mSubmitView = (TextView) findViewById(R.id.btn_submit);
		mProgressRefresh  = (ProgressBar) findViewById(R.id.progress_dialog);
		mCoinRefreshTimeTv = (EditText) findViewById(R.id.edit_coin_refresh_time);
		mRelativeHighView =  findViewById(R.id.alert_relative_high_rl);   
		mRelativeHighEdit =  (EditText) findViewById(R.id.edit_relative_high);   
		mHighDescTv =  findViewById(R.id.edit_relative_high_desc);   
		mRelativeLowerView = findViewById(R.id.alert_relative_lower_rl);
    	mRelativeLowerEdit = (EditText) findViewById(R.id.edit_alert_relative_lower);
        mLowerDescTv = findViewById(R.id.tv_relative_lower_desc);
		
		mSubmitView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (checkValid()) {
					int coinIndex = mSpinnerCoins.getSelectedItemPosition() - EXTRA_SPINEER_HINT;
					final List<CoinBean> mBeans = CoinApplication.getInstance().getBterCoinList();
					final CoinBean toOperateCoin = mBeans.get(coinIndex);
					// 是否已保存
					boolean isSetWaring = toOperateCoin.getIsWarning();
					toOperateCoin.setIsWarning(true);
					if (mVibrateChb.isChecked()) {
					    toOperateCoin.setNotifyDefaults(Notification.DEFAULT_VIBRATE);
					} else {
						toOperateCoin.removeNotifyDefaults(Notification.DEFAULT_VIBRATE);
					}
					double highPrice = 0.0f;
					double lowerPrice = 0.0f;
					long refreshTime = 0;
					try {
					    highPrice = Double.parseDouble(trimToString(mAlertHightPriceTv));
					    lowerPrice = Double.parseDouble(trimToString(mAlertLowerPriceTv));
					    refreshTime = Long.parseLong(trimToString(mCoinRefreshTimeTv));
					} catch (NumberFormatException ex) {
						ex.printStackTrace();
					}
					toOperateCoin.setUpperPrice(highPrice);
					toOperateCoin.setLowerPrice(lowerPrice);
					toOperateCoin.setRefreshTime(refreshTime * PreferencesService.MILLIONS);
					Slog.v(TAG, "toOperateCoin = " + toOperateCoin);
					DBAdapter dbAdapter = CoinApplication.getInstance().getDbAdapter();
					long result = dbAdapter.updateCoinBeanWaringState(CoinsDatabaseHelper.BTER_TABLE_NAME, toOperateCoin);
					Slog.v(TAG, "result = " + result);
					if (isSetWaring) {
						Utils.showShortToast(getApplicationContext(), "保存成功");
						setResult(RESULT_OK);
					} else {
						// 添加
						Intent data = new Intent();
						data.putExtra(CoinWaringFragment.EXTRA_COIN_ID, toOperateCoin.getId());
						setResult(RESULT_OK, data);
					}
					finish();
				}
			}
		});
		
		mRelativeHighEdit.setOnFocusChangeListener(mRelativeFocusChangeListener);
		mRelativeLowerEdit.setOnFocusChangeListener(mRelativeFocusChangeListener);
	}
	
	private View.OnFocusChangeListener mRelativeFocusChangeListener = 
		new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus) {
					if (mRelativeHighEdit == v) {
						setLowerPriceAccordingToRelativePrice(true);
					} else if (mRelativeLowerEdit == v) {
						setLowerPriceAccordingToRelativePrice(false);
					}
				}
				
			}
		
	};
	
	//根据相对价格设置绝对价格
	private void setLowerPriceAccordingToRelativePrice(boolean isHigh) {
		if (isHigh) {
			// 设置绝对上限价格
			try {
			    double price_percent = Double.parseDouble(trimToString(mRelativeHighEdit));
			    double last_price =  Double.parseDouble(trimToString(mLastPriceTv));
			    if (price_percent > 1) {
			    	price_percent = price_percent / 100;
			    }
			    mAlertHightPriceTv.setText(getFormatCoinPrice(last_price * ( 1 + price_percent)));
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
			}
		} else {
			// 设置绝对下限价格
			try {
			    double price_percent = Double.parseDouble(trimToString(mRelativeLowerEdit));
			    double last_price =  Double.parseDouble(trimToString(mLastPriceTv));
			    if (price_percent > 1) {
			    	price_percent = price_percent / 100;
			    }
			    mAlertLowerPriceTv.setText(getFormatCoinPrice(last_price * ( 1 - price_percent)));
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	// 设置货币类型
	private void setSpinner() {
		final List<CoinBean> mBeans = CoinApplication.getInstance().getBterCoinList();
		
		SpinnerChooserAdapter adapter = new SpinnerChooserAdapter(this);
		mSpinnerCoins.setAdapter(adapter);
		mSpinnerCoins.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mLastPriceTv.setText("");
				if (position == 0) {
					// 没有进行选择
					mLastPriceTv.setHint("");
					showRefreshView(false);
					mAlertHightPriceTv.setText("");
					mAlertLowerPriceTv.setText("");
					mSubmitView.setText(R.string.add);
					return;
				}
				mLastPriceTv.setHint(R.string.toast_alert_price_get);
				showRefreshView(true);
				CoinBean selectedBean = mBeans.get(position - EXTRA_SPINEER_HINT);
				Slog.v(TAG, "selectedBean = " + selectedBean.toString());
				String apiUrl = selectedBean.getApiUrl();
				mThreadPool.submit(new RequestCoinPriceRunnable(mCoinPriceListener, false, apiUrl));
				if (selectedBean.getIsWarning()) {
					mSubmitView.setText(R.string.save);
					mAlertHightPriceTv.setText(getFormatCoinPrice(selectedBean.getUpperPrice()));
					mAlertLowerPriceTv.setText(getFormatCoinPrice(selectedBean.getLowerPrice()));
					mVibrateChb.setChecked((selectedBean.getNotifyDefaults() & Notification.DEFAULT_VIBRATE) != 0);
				} else {
					mAlertHightPriceTv.setText("");
					mAlertLowerPriceTv.setText("");
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		
		if (isSetWaringCoin()) {
			for (int i = 0; i < mBeans.size(); i++) {
				if (mBeans.get(i).getId() == setCoinId) {
					mSpinnerCoins.setSelection(i + EXTRA_SPINEER_HINT);
					mSpinnerCoins.setEnabled(false);
					break;
				}
			}
		}
	}
	
	/**
	 * 浮点型转换为字符串型，避免浮点表示
	 * @param price
	 * @return
	 */
	private String getFormatCoinPrice(double price) {
		return FormatUtils.makeFormatString(getString(R.string.coin_price_format), price);
	}
	
	private boolean checkValid() {
		int errorId = -1;
		int coinItemPosition = mSpinnerCoins.getSelectedItemPosition();
		if (coinItemPosition == 0) {
			errorId = R.string.toast_coin_not_set;
		} else {
			if (TextUtils.isEmpty(trimToString(mAlertHightPriceTv))
					|| TextUtils.isEmpty(trimToString(mAlertLowerPriceTv))) {
				errorId = R.string.toast_alert_price_not_set;
			} else {
				try {
					// 校准价格
					if (TextUtils.isEmpty(trimToString(mAlertHightPriceTv))) {
						setLowerPriceAccordingToRelativePrice(true);
					}
					if (TextUtils.isEmpty(trimToString(mAlertLowerPriceTv))) {
						setLowerPriceAccordingToRelativePrice(false);
					}
					double high_price = Double.parseDouble(trimToString(mAlertHightPriceTv));
				    double lower_price =  Double.parseDouble(trimToString(mAlertLowerPriceTv));
				    
				    if (Double.compare(lower_price, high_price) > 0) {
				    	errorId = R.string.toast_alert_price_invalie;
				    }
				} catch (Exception e) {
					errorId = R.string.toast_intput_full;
				}
			}
		}
		if (errorId != -1) {
			Utils.showShortToast(getApplicationContext(),errorId);
			return false ;
		}
		return true ;
	}
	
	/**
	 * 检查数据是否输入过大
	 * @return
	 */
	private boolean checkInputBig() {
		try {
			double refreshTime = Long.parseLong(trimToString(mCoinRefreshTimeTv));
			double high_price = Double.parseDouble(trimToString(mAlertHightPriceTv));
		    double lower_price =  Double.parseDouble(trimToString(mAlertLowerPriceTv));
		    StringBuilder sb = new StringBuilder();
		    if (refreshTime / PreferencesService.MILLIONS >= 5) {
		    	// 刷新时间设置过大
		    	sb.append("刷新时间:" + refreshTime);
		    } 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false ;
	}
	
	private String trimToString(TextView tv) {
		return tv.getText().toString().trim();
	}
	
	private void showRefreshView(boolean show) {
		mProgressRefresh.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_COIN_PRICE_CHANGED :
				handleEventChanged(msg);
				break;
			default :
				break;
			}
		};
		
		private void handleEventChanged(Message msg) {
			Slog.d(TAG, "handleEventChanged =" );
		    Params param = (Params) msg.obj;
		    int coinIndex = mSpinnerCoins.getSelectedItemPosition();
			final List<CoinBean> mBeans = CoinApplication.getInstance().getBterCoinList();
			final CoinBean coinBean = mBeans.get(coinIndex - EXTRA_SPINEER_HINT);
			if (coinBean.hasSameName(param.coinName)) {
				showRefreshView(false);
				mLastPriceTv.setText(FormatUtils.makeFormatString(getString(R.string.coin_last_price), 
						param.lastPrice));
				// 未设置则赋值
				if (!coinBean.getIsWarning() || TextUtils.isEmpty(trimToString(mAlertHightPriceTv))) {
					mAlertHightPriceTv.setText(String.valueOf(param.lastPrice));
					mAlertHightPriceTv.setSelection(mAlertHightPriceTv.length());
					
				}
				if (!coinBean.getIsWarning() ||TextUtils.isEmpty(trimToString(mAlertLowerPriceTv))) {
					mAlertLowerPriceTv.setText(String.valueOf(param.lastPrice));
					mAlertLowerPriceTv.setSelection(mAlertLowerPriceTv.length());
				}
				showRelativeControl(true);
			}
		}
	};
	
	public class SpinnerChooserAdapter extends BaseAdapter {

		Context context;
		private List<CoinBean> bterCoins = CoinApplication.getInstance().getBterCoinList();
		
		public SpinnerChooserAdapter(Context context) {
			this.context = context;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public int getCount() {
			return bterCoins.size() + EXTRA_SPINEER_HINT;
		}

		@Override
		public CoinBean getItem(int position) {
			return bterCoins.get(position - EXTRA_SPINEER_HINT);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			return createViewFromResource(position, convertView, parent, 
					R.layout.coin_alert_dropdown_item, true);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return createViewFromResource(position, convertView, parent, 
						R.layout.coin_alert_choose_item, false);
		}
		
		private View createViewFromResource(int position, View convertView, ViewGroup parent,
	            int resource, boolean isDropDown) {
	        View view = null;
	        ViewHolder viewHolder = null;
			if (null == convertView) {
				view = LayoutInflater.from(context).inflate(resource, null);
				viewHolder = ViewHolder.newViewHolder(view);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
				view = convertView;
			}
			if (position == 0) {
				viewHolder.title.setText("请您选择货币类型");
				if (viewHolder.tvSetStatus != null) {
					viewHolder.tvSetStatus.setVisibility(View.INVISIBLE);
				}
			} else if (getItem(position) != null) {
				CoinBean coinBean = getItem(position);
				viewHolder.title.setText(coinBean.getFullTradingName());
				if (isDropDown) {
					if (viewHolder.tvSetStatus != null) {
						viewHolder.tvSetStatus.setVisibility(View.VISIBLE);
						if (coinBean.getIsWarning()) {
							viewHolder.tvSetStatus.setText(R.string.alert_coin_set);
							viewHolder.tvSetStatus.setTextColor(getResources().getColor(R.color.blue));
						} else {
							viewHolder.tvSetStatus.setText(R.string.alert_coin_not_set);
							viewHolder.tvSetStatus.setTextColor(getResources().getColor(R.color.grey_light));
						}
					}
				} else {
					if (viewHolder.tvSetStatus != null) {
						viewHolder.tvSetStatus.setVisibility(View.INVISIBLE);
					}
				}
			}

	        return view;
	    }
	}
	
	public static class ViewHolder {
		TextView title;
		TextView tvSetStatus;
		
		private ViewHolder() {}
		
		public static ViewHolder newViewHolder(View view) {
			ViewHolder viewHolder = new ViewHolder();
	        viewHolder.title = (TextView) view.findViewById(R.id.tv_coin_name);
	        viewHolder.tvSetStatus = (TextView) view.findViewById(R.id.tv_coin_status);
	        
	        return viewHolder;
		}
	}
	

	private ICoinPriceChangedListener mCoinPriceListener = new ICoinPriceChangedListener() {

			@Override
			public void onCoinPriceChanged(CoinBean coinBean) {
				
			}

			@Override
			public void onCoinPriceChanged(double lastPrice, String coinName) {
				if (mHandler != null) {
					Message msg = mHandler.obtainMessage(MSG_COIN_PRICE_CHANGED, new Params(lastPrice, coinName));
					mHandler.sendMessageDelayed(msg, 500);
				}
				
				Slog.d(TAG, "onCoinPriceChanged =" + coinName);
			}
	};
	
	static class Params {
		private double lastPrice;
		private String coinName;
		public Params(double lastPrice, String coinName) {
			super();
			this.lastPrice = lastPrice;
			this.coinName = coinName;
		}
	}
}
