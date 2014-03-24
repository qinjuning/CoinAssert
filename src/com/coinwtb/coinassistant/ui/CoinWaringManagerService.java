package com.coinwtb.coinassistant.ui;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.SparseArray;
import android.widget.RemoteViews;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.coinwtb.coinassistant.R;
import com.coinwtb.coinassistant.bean.CoinBean;
import com.coinwtb.coinassistant.bean.CoinPriceBean;
import com.coinwtb.coinassistant.common.RequestCoinPriceFuture;
import com.coinwtb.coinassistant.fragment.CoinWaringFragment;
import com.coinwtb.coinassistant.ui.base.CoinApplication;
import com.coinwtb.coinassistant.utils.Constants;
import com.coinwtb.coinassistant.utils.FormatUtils;
import com.coinwtb.coinassistant.utils.PreferencesService;
import com.coinwtb.coinassistant.utils.Slog;
import com.coinwtb.coinassistant.utils.Utils;

/**
 * 货币预警
 *
 */
public class CoinWaringManagerService extends Service{

	private static final String TAG = "CoinWaringManagerService";
	
	private ExecutorService mThreadPool = null;
	
	NotificationManager mNotificationManager = null;
	
	Future<?> mDetectPriceFuture = null;
	
	/**
	 * 以Coin Id 和 对应的线程任务保存
	 */
	private SparseArray<RequestCoinPriceFuture> mWaringSparseArray = new SparseArray<RequestCoinPriceFuture>();
	
	/**
	 * 是否已启动所有预警项目
	 */
	private boolean mHasStartAll = false;
	
	@Override
	public void onCreate() {
		Slog.i(TAG, "onCreate = ");
		mThreadPool = Executors.newCachedThreadPool();
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);  
	    CoinApplication.getInstance().startQueryDatabase();
        startBaiduPushServer();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mDetectPriceFuture == null || mDetectPriceFuture.isCancelled() || mDetectPriceFuture.isDone()) {
			mDetectPriceFuture = mThreadPool.submit(mDetectPriceNotificationTask);
		}
		// intent如果为null，代表系统重启
		if (intent == null) {
			handleStartAllCommand();
			return START_STICKY;
		}
		String action = intent.getAction();
		Slog.i(TAG, "action = " +  action);
		
		if (!TextUtils.isEmpty(action)) {
			int coinId = intent.getIntExtra(CoinWaringFragment.EXTRA_COIN_ID, -1);
			Slog.i(TAG, " coinId = " +  coinId);
			if (Constants.ACTION_COIN_WARING_ALL.equals(action)) {
				handleStartAllCommand();
			} else if (coinId != -1) {
				if (Constants.ACTION_COIN_WARING_ADD.equals(action) 
						|| Constants.ACTION_COIN_WARING_SET.equals(action)) {
					setWarningCoin(CoinApplication.getInstance().getWaringCoinById(coinId));
					RequestCoinPriceFuture priceFuture= mWaringSparseArray.get(coinId);
					if (priceFuture != null) {
						priceFuture.refresh();
					} else {
						// 添加一个任务
						CoinBean waringCoinBean = CoinApplication.getInstance().getWaringCoinById(coinId);
						Slog.i(TAG, "set waringCoinBean 222");
						Slog.i(TAG, "set waringCoinBean = " +  (waringCoinBean == null ? " null" : waringCoinBean.toString()));
						if (waringCoinBean != null) {
							priceFuture= new RequestCoinPriceFuture(true, waringCoinBean);
							mWaringSparseArray.put(waringCoinBean.getId(), priceFuture);
							mThreadPool.submit(priceFuture);
						}
					}
				} else if (Constants.ACTION_COIN_WARING_REMOVED.equals(action))  {
					RequestCoinPriceFuture priceFuture = mWaringSparseArray.get(coinId);
					if (priceFuture != null) {
						priceFuture.cancel();
					}
					mWaringSparseArray.remove(coinId);
				} 
			}
		}
		return START_STICKY;
	}

	private void handleStartAllCommand() {
		if (!mHasStartAll) {
			mHasStartAll = true;
			//  启动所有预警设置项
			List<CoinBean> coinBeans = CoinApplication.getInstance().getBterWaringCoinList();
		    if (coinBeans != null && !coinBeans.isEmpty()) {
		    	int N = coinBeans.size();
				for (int i = 0; i < N; i++) {
					setWarningCoin(coinBeans.get(i));
			    }
		    }
		}
	}
	
	// 添加、刷新一个任务
	private void setWarningCoin(CoinBean coinBean) {
		if (coinBean == null || coinBean.getIsWarning()) {
			return;
		}
		RequestCoinPriceFuture priceFuture= mWaringSparseArray.get(coinBean.getId());
		if (priceFuture != null) {
			priceFuture.refresh();
			Slog.i(TAG, "refresh coinBean = " + coinBean);
		} else {
			// 添加一个任务
			if (coinBean != null) {
				priceFuture= new RequestCoinPriceFuture(true, coinBean);
				mWaringSparseArray.put(coinBean.getId(), priceFuture);
				mThreadPool.submit(priceFuture);
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Slog.i(TAG, "onDestroy = ");
		mDetectPriceFuture.cancel(true);
		mDetectPriceFuture = null;
		mThreadPool.shutdown();
        mHandler.removeCallbacks(null);
	}
	
	// 启动百度消息服务器
	private void startBaiduPushServer() {
		PushManager.startWork(getApplicationContext(),
				PushConstants.LOGIN_TYPE_API_KEY, 
				Utils.getBaiduApiKey(CoinWaringManagerService.this));
	}
	
	/**
	 * 检测价格变化的任务，发送通知等
	 */
	private Runnable mDetectPriceNotificationTask = new Runnable() {
		
		@Override
		public void run() {
			
			Slog.e(TAG, " isInterrupted  = " + Thread.currentThread().isInterrupted());
			
			while (!Thread.currentThread().isInterrupted()) {
				try {
					final List<CoinBean> waringList = CoinApplication.getInstance().getBterWaringCoinList();
					Slog.e(TAG, " start detect  = " + waringList);
					int N = waringList.size();
					for (int i = 0; i < N; i++) {
						final CoinBean coinBean = waringList.get(i);
						if (coinBean == null || !coinBean.getIsWarning()) {
							continue;
						}
						CoinPriceBean lastCoinPrice = coinBean.getLastCoinPrice();
						if (lastCoinPrice == null) {
							continue;
						}
						boolean isHigh = false;
						boolean isLower = false;
						if (Double.compare(coinBean.getUpperPrice(), 0.0f) > 0) {
							isHigh = Double.compare(lastCoinPrice.getLastPrice(), coinBean.getUpperPrice()) >  0;
						} 
						if (Double.compare(coinBean.getLowerPrice(), 0.0f) > 0) {
							isLower =  Double.compare(lastCoinPrice.getLastPrice(), coinBean.getLowerPrice()) < 0;
						}
						Slog.i(TAG, " isHigh=" +  isHigh + " @ isLower=" + isLower);
						int contentRes = -1;
						int tickerRes = -1;
						if (isHigh) {
							contentRes = R.string.notify_alert_coin_high_content;
							tickerRes = R.string.notify_alert_coin_higt_title;
						} else if (isLower) {
							contentRes = R.string.notify_alert_coin_lowe_content;
							tickerRes = R.string.notify_alert_coin_lowe_title;
						} else {
							// Nothing to do
						}
						Slog.e(TAG, "contentRes = " + contentRes);
						if (contentRes != -1) {
							Slog.e(TAG, "enter contentRes = " + contentRes);
							final String contentTitle = FormatUtils.makeFormatString(getResources().getString(contentRes), 
									coinBean.getFullTradingName(), lastCoinPrice.getLastPrice());
							Slog.e(TAG, "contentTitle=" + contentTitle);
									
							final String tickTitle = FormatUtils.makeFormatString(
									getResources().getString(tickerRes),
									isHigh ? coinBean.getUpperPrice() : coinBean.getLowerPrice());
							Slog.e(TAG, "contentTitle=" + contentTitle + " tickTitle = " + tickTitle);
							
							final Notification notification = createNotification(contentTitle, tickTitle, coinBean);
							
							mHandler.postDelayed(new Runnable() {
								
								@Override
								public void run() {
									mNotificationManager.notify(coinBean.getId(), notification);
								}
							}, 1000);
						} 
					}
					//每隔10S获取
					TimeUnit.MILLISECONDS.sleep(PreferencesService.getInstance().getAlertTime());
				}catch (InterruptedException e) {
					Slog.e(TAG, " stop detect  = " + e);
					e.printStackTrace();
					break;
				} 
				Slog.e(TAG, " stop isInterrupted  = " + Thread.currentThread().isInterrupted());
			}
			Slog.e(TAG, " end detect  = ");
		}
	};
	
	private Handler mHandler = new Handler() {
		
	};
	
	public Notification createNotification(String contentTitle, String tickTitle, CoinBean coinBean) {
		Notification notification = new Notification(); 
		if (coinBean.isVibrateWhenPopNofication()) {
			notification.defaults = coinBean.getNotifyDefaults();  
		} else {
			
		}
		coinBean.setVibrateWhenPopNofication(false);
		// 设置属性值  
		notification.when = System.currentTimeMillis(); // 立即发生此通知  
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		int iconRes = Utils.getCoinDrawableIdentifierSafely(getBaseContext(), coinBean.getShortName().toLowerCase());
		notification.icon = iconRes != -1 ? iconRes : R.drawable.ic_notification;
		notification.tickerText = contentTitle;
		
		RemoteViews remoteView = new RemoteViews(this.getPackageName(),R.layout.notification_coin_waring_builder);  
		remoteView.setImageViewResource(R.id.image_coin_icon, notification.icon);
		remoteView.setTextViewText(R.id.tv_coin_name , contentTitle); 
		remoteView.setTextViewText(R.id.tv_alert_price , tickTitle);
		notification.contentView = remoteView;  

		Intent intent = new Intent(CoinApplication.getInstance(), HomeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Slog.i(TAG, "createNotification= " + coinBean.getId());
		intent.putExtra(CoinWaringFragment.EXTRA_COIN_ID, coinBean.getId());
		PendingIntent contentIntent = PendingIntent.getActivity(CoinApplication.getInstance(), 
				0, 
				intent, 
				PendingIntent.FLAG_UPDATE_CURRENT);
		notification.contentIntent = contentIntent;
		return notification;
	}
	
}
