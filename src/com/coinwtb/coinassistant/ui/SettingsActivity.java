package com.coinwtb.coinassistant.ui;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.coinwtb.coinassistant.R;
import com.coinwtb.coinassistant.ui.base.BaseActivity;
import com.coinwtb.coinassistant.ui.base.CoinApplication;
import com.coinwtb.coinassistant.utils.UpdateManager;
import com.coinwtb.coinassistant.widget.MeetingDialog;
import com.coinwtb.coinassistant.widget.SetTimeRelativeLayout;

/**
 * 设置
 */
public class SettingsActivity extends BaseActivity implements View.OnClickListener {

	private final String TAG = "SettingsActivity";
	private static boolean DEBUG = false;
	private static final int DIALOG_SET_TIME_REFRESH = 1;                  // 设置时间
	private static final int DIALOG_LOGOUT_ACCOUNT = 2;                    // 注销
	
	private int[] mViewIDRefs = new int[]{
			R.id.rl_refresh_time, R.id.rl_app_version, 
			R.id.tv_help, R.id.tv_exit,};
	
	private TextView mTvAppVersion;                   // App版本号
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_view);
        setHeaderTitle(R.string.settings_title);
        
        for (int i = 0; i < mViewIDRefs.length; i++) {
        	findViewById(mViewIDRefs[i]).setOnClickListener(this);
        }
        
        mTvAppVersion = (TextView) findViewById(R.id.tv_app_version);
        
        setAppVersion();
	}

	private void setAppVersion() {
		mTvAppVersion.setText(String.format(getString(R.string.settings_version_name), 
	        		getString(R.string.versionname)));
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_refresh_time:
			showDialog(DIALOG_SET_TIME_REFRESH);
			break;
			
		case R.id.rl_app_version:
			UpdateManager updateManager = new UpdateManager();
			updateManager.showCheckVersionDialog(this);
			break;
		case R.id.tv_help:
			startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
			break;
		case R.id.tv_exit:
			showDialog(DIALOG_LOGOUT_ACCOUNT);
		    break;
		default:
		    break;
		}
	}
	
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_SET_TIME_REFRESH:
			return createSetRefreshDialog();
		case DIALOG_LOGOUT_ACCOUNT:
		    return createLogoutDialog();
	    default:
			return super.onCreateDialog(id);
		}
	}
	

	private Dialog createSetRefreshDialog() {
		
		final MeetingDialog dialog = new MeetingDialog(this);
		final SetTimeRelativeLayout rootView = (SetTimeRelativeLayout) getLayoutInflater().inflate(R.layout.dialog_set_refresh_time, null);
		dialog.setView(rootView);
		dialog.setPostiveClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.cancel();
				rootView.setTime();
			}
		});
       dialog.setNegativeClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		return dialog;
	}

	// 退出
	private Dialog createLogoutDialog() {
		
		final MeetingDialog dialog = new MeetingDialog(this);
		dialog.setMessage("退出后您将接受不到消息, 确定退出?");
		dialog.setPostiveClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CoinApplication.getInstance().exit();
				stopService(new Intent(SettingsActivity.this, CoinWaringManagerService.class));
                System.exit(0);
			}
		});
       dialog.setNegativeClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		return dialog;
	}
}
