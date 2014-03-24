package com.coinwtb.coinassistant.ui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.coinwtb.coinassistant.ui.CoinWaringManagerService;
import com.coinwtb.coinassistant.utils.Constants;

public class MyBootReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			// 扫描完成启动预警Service
			Intent service = new Intent(context, CoinWaringManagerService.class);
			service.setAction(Constants.ACTION_COIN_WARING_ALL);
			context.startService(service);
		}
	}
}
