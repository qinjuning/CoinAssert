package com.coinwtb.coinassistant.ui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.coinwtb.coinassistant.R;
import com.coinwtb.coinassistant.ui.base.CoinApplication;
import com.coinwtb.coinassistant.utils.Slog;
import com.coinwtb.coinassistant.utils.Utils;

/**
 * 监听网络变化的Service
 * @author Administrator
 *
 */
public class NetChangeReceiver extends BroadcastReceiver {
	
	public static final String TAG = NetChangeReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
        	ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        	NetworkInfo info = connectivityManager.getActiveNetworkInfo();  
            if(info != null && info.isAvailable()) {
                String name = info.getTypeName();
                Slog.d(TAG, "当前网络名称：" + name);
            } else {
                Slog.d(TAG, "没有可用网络");
                Utils.showLongToast(CoinApplication.getInstance(), R.string.network_off);
            }
        }
    }

}
