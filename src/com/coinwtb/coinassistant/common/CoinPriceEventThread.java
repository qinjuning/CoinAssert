package com.coinwtb.coinassistant.common;


public class CoinPriceEventThread extends Thread{
	
	private static final String TAG = "CoinPriceEventThread";
	
	private RequestCoinPriceRunnable mCoinPriceTask;
	
	public CoinPriceEventThread(ICoinPriceChangedListener listener, boolean loop) {
		mCoinPriceTask = new RequestCoinPriceRunnable(listener, loop);
	}
	
	public void stopped() {
		interrupt();
		mCoinPriceTask.stopped();
	}
	
	public void refresh() {
		// 中断线程
		interrupt();
		mCoinPriceTask.refresh();
	}
	
	@Override
	public void run() {
		mCoinPriceTask.run();
	} 
	
}
