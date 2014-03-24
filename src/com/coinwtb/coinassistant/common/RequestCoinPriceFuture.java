package com.coinwtb.coinassistant.common;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.coinwtb.coinassistant.bean.CoinBean;
import com.coinwtb.coinassistant.utils.Slog;

/**
 * 获取价格变化的 Future
 *
 */
public class RequestCoinPriceFuture implements RunnableFuture {
	
	private static final String TAG = "RequestCoinPriceFuture";
	
	private RequestCoinPriceRunnable mRequstRunnable;
	
	private volatile boolean isCanceled = false;
	
	/**
	 * 请求默认看盘的货币地址
	 * @param listener
	 * @param loop
	 * @param url
	 */
	public RequestCoinPriceFuture(boolean loop, CoinBean coinBean) {
		Slog.v(TAG, "coinBean = " + coinBean);
		mRequstRunnable = new RequestCoinPriceRunnable(null, true, coinBean, true); 
	}
	
	/**
	 * 刷新价格
	 */
	public void refresh() {
		mRequstRunnable.refresh();
	}

	public void cancel() {
		mRequstRunnable.stopped();
		refresh();
		isCanceled = true;
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		cancel();
		return true;
	}
	
	@Override
	public void run() {
		Slog.v(TAG, "run = ");
		mRequstRunnable.run();
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		return null;
	}

	@Override
	public boolean isCancelled() {
		return isCanceled;
	}

	@Override
	public boolean isDone() {
		return false;
	} 
}
