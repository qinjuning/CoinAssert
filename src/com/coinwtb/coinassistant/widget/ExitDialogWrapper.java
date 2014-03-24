package com.coinwtb.coinassistant.widget;

import android.app.Activity;
import android.view.View;

import com.coinwtb.coinassistant.R;
import com.coinwtb.coinassistant.ui.base.CoinApplication;

/*
 * 退出提示的dialog
 */
public class ExitDialogWrapper {
	private Activity activity = null;

	public ExitDialogWrapper(Activity activity) {
		this.activity = activity;
	}

	public void show() {
		
		final MeetingDialog dialog = new MeetingDialog(activity);
		dialog.setTitle(activity.getResources().getString(R.string.app_name));
		dialog.setMessage(activity.getResources().getString(R.string.exit_app_desc));
		dialog.setPostiveClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				activity.finish();
				CoinApplication.getInstance().exit();
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
