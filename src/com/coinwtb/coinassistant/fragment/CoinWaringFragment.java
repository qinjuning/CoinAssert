package com.coinwtb.coinassistant.fragment;


import java.util.List;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.coinwtb.coinassistant.R;
import com.coinwtb.coinassistant.bean.CoinBean;
import com.coinwtb.coinassistant.bean.CoinPriceBean;
import com.coinwtb.coinassistant.common.CoinsDatabaseHelper;
import com.coinwtb.coinassistant.common.DBAdapter;
import com.coinwtb.coinassistant.ui.CoinWaringAddActivity;
import com.coinwtb.coinassistant.ui.CoinWaringManagerService;
import com.coinwtb.coinassistant.ui.base.CoinApplication;
import com.coinwtb.coinassistant.utils.Constants;
import com.coinwtb.coinassistant.utils.FormatUtils;
import com.coinwtb.coinassistant.utils.Slog;
import com.coinwtb.coinassistant.utils.Utils;
import com.coinwtb.coinassistant.widget.CoinRealPriceRelativeLayout;
import com.coinwtb.coinassistant.widget.MeetingDialog;

public class CoinWaringFragment extends Fragment implements View.OnClickListener {
	
	String TAG = CoinWaringFragment.class.getSimpleName() ;
	
	protected static final int INVALID_INDEX = -1;
	
	public static final int REQUEST_ADD = 1;
	public static final int REQUEST_SET = REQUEST_ADD + 1;
	// 添加的预警管理ID
	public static final String EXTRA_COIN_ID = "extra_coin_id";
	
	private CoinBean mWaringCoinBean;
	private int position;                            // 当前索引
	
	protected View mView;
	
	private CoinRealPriceRelativeLayout mRealPriceLL;   // 实时价格表
	
	private TextView mTitleTv;       // 标题栏初始化
	private View addView;            // 添加
	private View setView;            // 设置
	private View delView;            // 删除
	private ProgressBar mProgressRefresh;
	
	private Handler mHandler = new Handler();
	private boolean mStartRefresh= false;                 
	private TextView mAlertStatusTv;         
	
	public CoinWaringFragment(int position) {
		List<CoinBean> waringCoinList = CoinApplication.getInstance().getBterWaringCoinList();
		mWaringCoinBean = position < waringCoinList.size() ? waringCoinList.get(position) : null;
		this.position = position;
		Slog.v(TAG, "position() = " + position + " @ " + mWaringCoinBean);
	}

	public CoinWaringFragment() {
		
	}
	
	protected CoinBean getCoinBean() {
		return mWaringCoinBean;
	}
	
	protected int getPosition() {
		return position;
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		Slog.v(TAG, "onPause() # " + position );
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Slog.v(TAG, "onStop() # " + position );
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Slog.v(TAG, "onDestroy() # " + position );
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.coin_waring_fragment, null);
		initViews();
		return mView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		updateState();
        startWaringManager(Constants.ACTION_COIN_WARING_SET);
        if (mWaringCoinBean != null) {
        	showRefreshView(true);
        }
	}

	private void initViews() {
		mTitleTv = (TextView) mView.findViewById(R.id.tv_title);
		mRealPriceLL = (CoinRealPriceRelativeLayout) mView.findViewById(R.id.coin_real_price_rl);
		mAlertStatusTv = (TextView) mView.findViewById(R.id.tv_alert_status);
		addView = mView.findViewById(R.id.btn_submit);  
		addView.setOnClickListener(this);
		setView = mView.findViewById(R.id.btn_set);
		setView.setOnClickListener(this);
		delView = mView.findViewById(R.id.btn_del);
		delView.setOnClickListener(this);
		
		mProgressRefresh  = (ProgressBar) mView.findViewById(R.id.progress_dialog);
	}
	
	private void showRefreshView(boolean show) {
		mProgressRefresh.setVisibility(show ? View.VISIBLE : View.GONE);
	}
	
	@Override
	public void onClick(View v) {
		if (addView == v) {
			startActivityForResult(new Intent(getActivity(), CoinWaringAddActivity.class), REQUEST_ADD);
		} else if (setView == v && mWaringCoinBean != null) {
			Intent intent = new Intent(getActivity(), CoinWaringAddActivity.class);
			intent.putExtra(CoinWaringFragment.EXTRA_COIN_ID, mWaringCoinBean.getId());
			startActivityForResult(intent, REQUEST_SET);
		} else if (delView == v) {
			showDeleteDialog();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Slog.v(TAG, "requestCode=" + requestCode + "# " + position );
		if (Activity.RESULT_OK == resultCode) {
			if (REQUEST_ADD == requestCode) {
				int coinId = data.getIntExtra(EXTRA_COIN_ID, -1);
				Slog.v(TAG, "coinId =" + coinId);
				if (coinId != -1) {
					CoinBean coinBean = CoinApplication.getInstance().getWaringCoinById(coinId);
					Slog.d(TAG, coinBean == null ? " null " :  coinBean.toString());
					if (coinBean != null) {
						mWaringCoinBean = coinBean;
						showRefreshView(true);
						// 添加预警管理
						startWaringManager(Constants.ACTION_COIN_WARING_ADD);
					}
				}
			} else if (REQUEST_SET == requestCode){
				startWaringManager(Constants.ACTION_COIN_WARING_SET);
			}
			updateState();	
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		stopRefreshPrice();
	}
	
	private void updateState() {
		if (!isAdded()) {
			return;
		}
		if (mWaringCoinBean == null) {
			mTitleTv.setText(R.string.toast_alert_not_title);
			mRealPriceLL.setVisibility(View.INVISIBLE);
			setView.setVisibility(View.GONE);
			delView.setVisibility(View.GONE);
			addView.setVisibility(View.VISIBLE);
			mAlertStatusTv.setVisibility(View.INVISIBLE);
			showRefreshView(false);
		} else {
			mTitleTv.setText(mWaringCoinBean.getFullTradingName());
			addView.setVisibility(View.GONE);
			setView.setVisibility(View.VISIBLE);
			delView.setVisibility(View.VISIBLE);
			mRealPriceLL.setVisibility(View.VISIBLE);
			CoinPriceBean lastCoinPrice = mWaringCoinBean.getLastCoinPrice();
			if (lastCoinPrice != null) {
				showRefreshView(false);
				mRealPriceLL.setLastPrice(lastCoinPrice.getLastPrice());
				mRealPriceLL.setHighPrice(lastCoinPrice.getHightPrice());
				mRealPriceLL.setLowerPrice(lastCoinPrice.getLowPrice());
				mRealPriceLL.setSellPrice(lastCoinPrice.getSelle1Price());
				mRealPriceLL.setBuy1Price(lastCoinPrice.getBuy1Price());
				mRealPriceLL.setCoinVolume(lastCoinPrice.getTradingVolume());
				mRealPriceLL.setRefreshTime();
				
				// 控制显示预警状态
				mAlertStatusTv.setVisibility(View.VISIBLE);
				boolean isHigh = false;
				boolean isLower = false;
				if (Double.compare(mWaringCoinBean.getUpperPrice(), 0.0f) > 0) {
					isHigh = Double.compare(lastCoinPrice.getLastPrice(), mWaringCoinBean.getUpperPrice()) >  0;
				} 
				if (Double.compare(mWaringCoinBean.getLowerPrice(), 0.0f) > 0) {
					isLower =  Double.compare(lastCoinPrice.getLastPrice(), mWaringCoinBean.getLowerPrice()) < 0;
				}
				int statusRes = R.string.coin_waring_status_ok;
				if (isHigh) {
					statusRes = R.string.coin_waring_status_high;
				} if (isLower) {
					statusRes = R.string.coin_waring_status_lower;
				} 
				// 判断是否为null，防止被销毁
				String value =FormatUtils.makeFormatString(
						getResources().getString(R.string.coin_waring_status_format), 
						getResources().getString(statusRes));
				if (isHigh || isLower) {
					mAlertStatusTv.setTextColor(Color.RED);
				} else {
					mAlertStatusTv.setTextColor(getResources().getColor(R.color.yellow_tu));
				}
				mAlertStatusTv.setText(value);
				
			}
			mRealPriceLL.setWaringHigh(mWaringCoinBean.getUpperPrice());
			mRealPriceLL.setWaringLower(mWaringCoinBean.getLowerPrice());
			
		}
	}
	
	private void startWaringManager(String action) {
		if (mWaringCoinBean != null) {
			Intent service = new Intent(CoinApplication.getInstance(), CoinWaringManagerService.class);
			service.setAction(action);
			service.putExtra(CoinWaringFragment.EXTRA_COIN_ID, mWaringCoinBean.getId());
			CoinApplication.getInstance().startService(service);
			
			if (Constants.ACTION_COIN_WARING_ADD.equals(action)) {
				startRefreshPrice();
			} else if (Constants.ACTION_COIN_WARING_SET.equals(action)) {
				startRefreshPrice();
			} else if (Constants.ACTION_COIN_WARING_REMOVED.equals(action)) {
				stopRefreshPrice();
			}
		}
	}
	
	private void startRefreshPrice() {
		if (!mStartRefresh) {
			mHandler.postDelayed(refreshPriceRunnable, 1000);
		}
		mStartRefresh = true;
	}
	
	private void stopRefreshPrice() {
		if (mHandler != null) {
		    mHandler.removeCallbacks(refreshPriceRunnable);
		}
		mStartRefresh = false;
	}
	
	private Runnable refreshPriceRunnable = new Runnable() {
		@Override
		public void run() {
			if (mStartRefresh) {
				if (mWaringCoinBean != null && isAdded()) {
					updateState();
					mHandler.postDelayed(this, mWaringCoinBean.getRefreshTime());
				}
			}
		}
	};
	
    protected String getFragmentStatus() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("isAdded=" + isAdded() + ", ");
    	sb.append("isDetached=" + isDetached() + ", ");
    	sb.append("isHidden=" + isHidden() + ", ");
    	sb.append("isRemoving=" + isRemoving() + ", ");
    	sb.append("isResumed=" + isResumed() + ", ");
    	sb.append("isVisible=" + isVisible()+ "");
    	return sb.toString();
    }
	
	protected int parseToInt(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException nfEx) {
			nfEx.printStackTrace();
		}
		return INVALID_INDEX;
	}
	
	private void showDeleteDialog() {
		final MeetingDialog dialog = new MeetingDialog(getActivity());
		dialog.setMessage("确定删除对该货币预警功能吗?");
		dialog.setPostiveClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mWaringCoinBean != null) {
					DBAdapter dbAdapter = CoinApplication.getInstance().getDbAdapter();
					mWaringCoinBean.setIsWarning(false);
					long result = dbAdapter.updateCoinBeanWaringState(CoinsDatabaseHelper.BTER_TABLE_NAME, mWaringCoinBean);
					Slog.v(CoinWaringFragment.class.getSimpleName(), "result = " + result);
					Utils.showShortToast(getActivity(), "删除成功...");
					// 取消预警管理
					startWaringManager(Constants.ACTION_COIN_WARING_REMOVED);
					NotificationManager nm = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
					nm.cancel(mWaringCoinBean.getId());
					mWaringCoinBean.setVibrateWhenPopNofication(true);
					mWaringCoinBean = null;
					updateState();
				}
				dialog.cancel();
			}
		});
       dialog.setNegativeClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
       
       dialog.show();
	}
}
