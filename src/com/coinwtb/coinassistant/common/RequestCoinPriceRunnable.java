package com.coinwtb.coinassistant.common;

import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.PendingIntent.CanceledException;
import android.text.TextUtils;

import com.coinwtb.coinassistant.bean.CoinBean;
import com.coinwtb.coinassistant.bean.CoinPriceBean;
import com.coinwtb.coinassistant.ui.base.CoinApplication;
import com.coinwtb.coinassistant.utils.PreferencesService;
import com.coinwtb.coinassistant.utils.Slog;

/**
 * 获取价格变化的 Runnable
 * @author Administrator
 *
 */
public class RequestCoinPriceRunnable implements Runnable{
	
	private static final String TAG = "RequestCoinPriceRunnable";
	
	private ExecutorService mFixedThreadPool = Executors.newFixedThreadPool(1);
	private DefaultHttpClient client;
	private HttpGet httpGet;
	private ICoinPriceChangedListener onCoinPriceChangedListener;
	
	private volatile boolean stopped = false;
	//是否设置为循环
	private boolean loop = false;
	
	private Future<String> mFutureTask;
	
	private CoinPriceProcessStrategy mCoinPriceStrategy;
	
	/**
	 * 请求默认看盘的货币地址
	 * @param listener
	 * @param loop
	 * @param url
	 */
	public RequestCoinPriceRunnable(ICoinPriceChangedListener listener, boolean loop) {
		this(listener, loop, "");
	}
	
	/**
	 * 请求指定的API URL
	 * @param listener
	 * @param loop
	 * @param url
	 */
	public RequestCoinPriceRunnable(ICoinPriceChangedListener listener, boolean loop, String url) {
		onCoinPriceChangedListener = listener;
		this.loop = loop;
		if (TextUtils.isEmpty(url)) {
			mCoinPriceStrategy = new CoinPriceProcessStrategy(listener);
		} else {
			mCoinPriceStrategy = new StringApiCoinPriceStrategy(listener, url);
		}
		init();
	}
	
	/**
	 * 请求指定的货币信息类
	 * @param listener
	 * @param loop
	 * @param url
	 */
	public RequestCoinPriceRunnable(ICoinPriceChangedListener listener, boolean loop, CoinBean coinBean) {
		onCoinPriceChangedListener = listener;
		this.loop = loop;
		init();
		mCoinPriceStrategy = new CoinPriceProcessStrategy(listener, coinBean);
	}
	
	/**
	 * 请求指定的货币信息类
	 * @param listener
	 * @param loop
	 * @param url
	 */
	public RequestCoinPriceRunnable(ICoinPriceChangedListener listener, boolean loop, 
			CoinBean coinBean, boolean isWaring) {
		onCoinPriceChangedListener = listener;
		this.loop = loop;
		init();
		mCoinPriceStrategy = new CoinPriceProcessStrategy(listener, coinBean, isWaring);
	}
	
	private void init() {
		initializeHttpGet();
		intializeHttpClient();
	}
	
	public void stopped() {
		stopped = true;
	}
	
	/**
	 * 刷新价格
	 */
	public void refresh() {
		if (mFutureTask != null) {
			mFutureTask.cancel(true);
		}
	}
	
	private void initializeHttpGet() {
		httpGet = new HttpGet();
		httpGet.setHeader( "Connection", "Keep-Alive" );  
	}

	private void intializeHttpClient() {
        HttpParams httpParams = new BasicHttpParams(); 
		HttpConnectionParams.setConnectionTimeout(httpParams, 15 * 10000); 
		HttpConnectionParams.setSoTimeout(httpParams, 15 * 1000); 
        client = new DefaultHttpClient(httpParams);  
	}

	@Override
	public void run() {
		Slog.d(TAG, " start run =" +  " @@ ");
		while (!stopped) {
			mFutureTask = mFixedThreadPool.submit(new Callable<String> (){

				@Override
				public String call() throws JSONException {
					Slog.d(TAG, "start call = " + " @@ ");
					try {
						return requestHttpGet(mCoinPriceStrategy.getApiUrl());
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
			});
			try {
				String result = null;
				
				try {
					result = mFutureTask.get();
				} catch (CancellationException  e) {
					e.printStackTrace();
				    continue;
				}
				Slog.d(TAG, "result=" + result + " @@ ");
				if (result == null) {
					// 获取失败后,避免多次访问, 暂停3s获取
				    TimeUnit.SECONDS.sleep(3);
				    continue;
				} 
				if (mFutureTask.isCancelled()) {
					if (loop) {
						continue;
					} else {
						break;
					}
				}
				JSONObject priceJson = new JSONObject(result);
				if (CoinPriceBean.isPriceResultSuccess(priceJson)) {
					CoinPriceBean coinPriceBean = CoinPriceBean.parseCoinPriceFromJson(priceJson);
					mCoinPriceStrategy.processCoinPrice(coinPriceBean);
				}
                if (!loop) {
					break;
				} else {
				    TimeUnit.MILLISECONDS.sleep(mCoinPriceStrategy.sleepTime());
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		Slog.d(TAG, " end run =" +  " @@ ");
	} 
	
	private String requestHttpGet(String uri) throws Exception {
		httpGet.setURI(URI.create(uri));
	    HttpResponse response = client.execute(httpGet);  
        int status = response.getStatusLine().getStatusCode();  
        String responseLine = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);  
        return responseLine;
	}
	
	private void sendPriceChanged(CoinBean coinBean) {
		if (onCoinPriceChangedListener != null) {
			Slog.d(TAG, "sendPriceChanged=" + coinBean);
			onCoinPriceChangedListener.onCoinPriceChanged(coinBean);
		}
	}
}
