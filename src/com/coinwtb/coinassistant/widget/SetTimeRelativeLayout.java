package com.coinwtb.coinassistant.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coinwtb.coinassistant.R;
import com.coinwtb.coinassistant.utils.PreferencesService;

/**
 * 设置时间的
 *
 */
public class SetTimeRelativeLayout extends RelativeLayout {

	public static final String TAG = "LeftCoinListView";
	
	// 首页刷新时间
	private EditText mHomeTimeEdit;
	private View mHomeTimeInc;
	// 预警时间
	private EditText mAlertTimeEdit;
	private View mAlertTimeInc;
	
	public SetTimeRelativeLayout(Context context) {
		this(context, null);
	}
	
	public SetTimeRelativeLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public SetTimeRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * 设置时间
	 */
	public void setTime() {
		try {
			int homeTime = Integer.parseInt(mHomeTimeEdit.getText().toString().trim()) * PreferencesService.MILLIONS;
			int alertTime = Integer.parseInt(mAlertTimeEdit.getText().toString().trim()) * PreferencesService.MILLIONS;
			PreferencesService pservice = PreferencesService.getInstance();
			int correctHomeTime =  homeTime > PreferencesService.DEFAULT_HOME_TIME ? homeTime : PreferencesService.DEFAULT_HOME_TIME;
			int correctAlertTime =  alertTime > PreferencesService.DEFAULT_ALERT_TIME ? alertTime : PreferencesService.DEFAULT_ALERT_TIME;
			pservice.setHomeTime(homeTime);
		    pservice.setAlertTime(alertTime);
			setText(mHomeTimeEdit, String.valueOf(correctHomeTime / PreferencesService.MILLIONS));
			setText(mAlertTimeEdit, String.valueOf(correctAlertTime  / PreferencesService.MILLIONS));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mHomeTimeEdit = (EditText) findViewById(R.id.edit_home_time);
		mHomeTimeInc = findViewById(R.id.btn_home_time_inc);
		mAlertTimeEdit = (EditText) findViewById(R.id.edit_alert_time);
		mAlertTimeInc =  findViewById(R.id.btn_alert_time_inc);
		
		PreferencesService pservice = PreferencesService.getInstance();
		setText(mHomeTimeEdit, String.valueOf(pservice.getHomeTime()  / PreferencesService.MILLIONS));
		setText(mAlertTimeEdit, String.valueOf(pservice.getAlertTime() / PreferencesService.MILLIONS));
		
		setListeners();
	}
	
	private void setListeners() {
		mHomeTimeInc.setOnClickListener(mIncTimeListener);
		mAlertTimeInc.setOnClickListener(mIncTimeListener);
	}
	
	private View.OnClickListener mIncTimeListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			EditText eidtText = null;
			if (mHomeTimeInc == v) {
				eidtText = mHomeTimeEdit;
			} else {
				eidtText = mAlertTimeEdit;
			}
			try {
			    int value = Integer.parseInt(eidtText.getText().toString().trim());
			    setText(eidtText, String.valueOf(++value));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	
	private void setText(TextView tv, String value) {
		tv.setText(value);
	}
}
