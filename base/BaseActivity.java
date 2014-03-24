package com.coinwtb.coinassert.ui.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.coinwtb.coinassert.R;
import com.coinwtb.coinassert.utils.Slog;
import com.umeng.analytics.MobclickAgent;


public class BaseActivity extends Activity {

	private static final String TAG = "BaseActivity";

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
	
	/******************************** 【Activity LifeCycle For Debug】 *******************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Slog.d(TAG, this.getClass().getSimpleName() + " onCreate() invoked!!");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		CoinApplication.getInstance().addActivity(this);
		MobclickAgent.onError(this);
	}

	@Override
	protected void onStart() {
		Slog.d(TAG, this.getClass().getSimpleName() + " onStart() invoked!!");
		super.onStart();
	}

	@Override
	protected void onRestart() {
		Slog.d(TAG, this.getClass().getSimpleName() + " onRestart() invoked!!");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		Slog.d(TAG, this.getClass().getSimpleName() + " onResume() invoked!!");
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		Slog.d(TAG, this.getClass().getSimpleName() + " onPause() invoked!!");
		super.onPause();
		try {
			MobclickAgent.onPause(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onStop() {
		Slog.d(TAG, this.getClass().getSimpleName() + " onStop() invoked!!");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		Slog.d(TAG, this.getClass().getSimpleName() + " onDestroy() invoked!!");
		CoinApplication.getInstance().removeActivity(this);
		super.onDestroy();
	}
	
	public void finish() {
		super.finish();
	}
	
	protected void openActivity(Class<?> pClass) {
		openActivity(pClass, null);
	}

	protected void openActivity(Class<?> pClass, Bundle pBundle) {
		Intent intent = new Intent(this, pClass);
		if (pBundle != null) {
			intent.putExtras(pBundle);
		}
		startActivity(intent);
	}

}
