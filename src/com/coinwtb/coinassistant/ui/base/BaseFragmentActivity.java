package com.coinwtb.coinassistant.ui.base;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.coinwtb.coinassistant.R;
import com.umeng.analytics.MobclickAgent;


public class BaseFragmentActivity extends FragmentActivity {

    private TextView mTitleTv;
    private TextView mRightView;
    
	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		initHeaderView();
	}
	

	private void initHeaderView() {
		// 标题栏初始化
		mTitleTv = (TextView) findViewById(R.id.tv_title);
		mRightView = (TextView) findViewById(R.id.btn_right);
		
		if (mRightView != null) {
			mRightView.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					if (mRightView == v) {
					    onRightViewClicked();
					}
				}
			});
		}
		
		final View leftView = findViewById(R.id.btn_left);
		if (leftView != null && leftView.getVisibility() == View.VISIBLE) {
			leftView.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					if (leftView == v) {
					    finish();
					}
				}
			});
		}
	}
	
	protected void onRightViewClicked() {
		
	}
	
	protected void setHeaderTitle(String title) {
		if (mTitleTv != null) {
	    	mTitleTv.setText(title);
		}
	}
	
	protected void setHeaderTitle(int resId) {
		setHeaderTitle(getResources().getString(resId));
	}
	
	protected void setRightViewTitle(String title) {
		mRightView.setText(title);
		setRightViewVisible(true);
	}
	
	protected void setRightViewTitle(int resId) {
		if (mRightView != null) {
		    mRightView.setText(resId);
		    setRightViewVisible(true);
		}
	}

	protected View getRightView() {
		return mRightView;
	}

	protected void setRightViewVisible(boolean visible) {
		if (mRightView != null) {
			mRightView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	public void finish() {
		super.finish();
	}
	
	public void defaultFinish() {
		super.finish();
	}
}
