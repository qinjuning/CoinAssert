package com.coinwtb.coinassistant.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.coinwtb.coinassistant.R;
import com.coinwtb.coinassistant.ui.SettingsActivity;
import com.coinwtb.coinassistant.utils.Utils;
import com.coinwtb.coinassistant.widget.slidingmenu.SlidingMenu;

/**
 * 右侧的菜单选项
 *
 */
public class RightMenuLinearLayout extends LinearLayout implements View.OnClickListener{
	
	private final String TAG = "RightMenuLinearLayout";
	
	private SlidingMenu mSlidingMenu;
	private View mSettingsView;
	private View mMediaListView;
	
	private View mPlatformBter;   // 比特儿
	private View mPlatformBt38;   // 比特时代
	
	public RightMenuLinearLayout(Context context) {
		this(context, null);
	}
	
	public RightMenuLinearLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public RightMenuLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mMediaListView = findViewById(R.id.media_list_rl);
		mMediaListView.setOnClickListener(this);
		mSettingsView = findViewById(R.id.settings_rl);;  
		mSettingsView.setOnClickListener(this);
		mPlatformBter = findViewById(R.id.platform_bter_ll);;
		mPlatformBter.setOnClickListener(this);
		mPlatformBt38 = findViewById(R.id.platform_bt38_ll);
		mPlatformBt38.setOnClickListener(this);
	}
	
	public void setSlidingMenu(SlidingMenu slidingMenu) {
		mSlidingMenu = slidingMenu;
	}

	@Override
	public void onClick(View v) {
		if (mSettingsView == v) {
			getContext().startActivity(new Intent(getContext(), SettingsActivity.class));
		} else if (mMediaListView == v) {
			// Push: 打开富媒体消息列表
			Intent sendIntent = new Intent();
			sendIntent.setClassName(getContext(), "com.baidu.android.pushservice.richmedia.MediaListActivity");
			sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getContext().startActivity(sendIntent);
		} else if (mPlatformBter == v) {
			toggleSlidingMenu();
		} else if (mPlatformBt38 == v) {
			Utils.showShortToast(getContext(), R.string.toast_bt38_not_open);
			toggleSlidingMenu();
		}
	}
	
	private void toggleSlidingMenu() {
		if (mSlidingMenu != null) {
			mSlidingMenu.toggle();
		}
	}
	
}
