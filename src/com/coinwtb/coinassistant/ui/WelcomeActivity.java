package com.coinwtb.coinassistant.ui;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

import com.coinwtb.coinassistant.R;
import com.coinwtb.coinassistant.ui.base.BaseActivity;
import com.coinwtb.coinassistant.ui.base.CoinApplication;

/**
 * 
 * @author jun.qin
 *
 */
public class WelcomeActivity extends BaseActivity{

	private static final String TAG = "WelcomeActivity";
	
	private static final int MSG_FLIPPER = 1;
	
	
	private static final float mAlpha_Start = 0.1f;
	private static final float mAlpha_End = 1.0f;

	private static final int NORAML_DURING_TIME = 2000;
	private static final int LONG_DURING_TIME = 5000;
	private int mDuringTime = NORAML_DURING_TIME;
	

	private ViewGroup animateGroup;
	private FlipperHandler mFlipperHandler;

	private boolean shouleFinish;   // 是否结束本Activity
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_main);
        animateGroup = (ViewGroup) findViewById(R.id.ll_start_animate);
        
        mFlipperHandler = new FlipperHandler(animateGroup, NORAML_DURING_TIME);
		mFlipperHandler.sendEmptyMessage(MSG_FLIPPER);
		new QueryDataBaseTask().execute();
		
		startAnimation();
    }
    
    //开始动画
    private void startAnimation() {
		View view = this.findViewById(R.id.rl_screen);
		AlphaAnimation animation = new AlphaAnimation(mAlpha_Start, mAlpha_End);
		animation.setDuration(mDuringTime);
		view.startAnimation(animation);


		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				shouldStartHomeActivity();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}

			@Override
			public void onAnimationStart(Animation animation) {

			}

		});
	}
    
    private void shouldStartHomeActivity() {
    	if (shouleFinish) {
			openActivity(HomeActivity.class);
			finish();
		}
    	shouleFinish = true;
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	mFlipperHandler.removeCallbacksAndMessages(null);
    	mFlipperHandler = null;
    }
    
    private class FlipperHandler extends Handler {
		private WeakReference<ViewGroup> refGroup;

		private int animate_icon_normal = R.drawable.bg_start;
		private int animate_icon_checked = R.drawable.bg_start_checked;

		private int delayTimeMs;
		private int flipperIndex = 0;

		public FlipperHandler(ViewGroup viewGroup, int timeMs) {
			delayTimeMs = timeMs / viewGroup.getChildCount();
			refGroup = new WeakReference<ViewGroup>(viewGroup);
		}

		@Override
		public void handleMessage(Message msg) {
			if (refGroup.get() == null) {
				return;
			}
			final ViewGroup viewGroup = refGroup.get();
			if (msg.what == MSG_FLIPPER) {
				for (int i = 0; i < viewGroup.getChildCount(); i++) {
					ImageView imageView = (ImageView) viewGroup.getChildAt(i);
					if (i == flipperIndex) {
						imageView.setImageResource(animate_icon_checked);
					} else {
						imageView.setImageResource(animate_icon_normal);
					}
				}
				flipperIndex++;
                if (flipperIndex == viewGroup.getChildCount()) {
                	flipperIndex = 0;
				}
				if (flipperIndex < viewGroup.getChildCount()) {
					sendEmptyMessageDelayed(MSG_FLIPPER, delayTimeMs);
				}
			}
		}
	}
    
    class QueryDataBaseTask extends AsyncTask<Void, Void, Void> {
    	@Override
    	protected Void doInBackground(Void... params) {
    		CoinApplication.getInstance().startQueryDatabase();
    		return null;
    	};
    	
    	@Override
    	protected void onPostExecute(Void result) {
    		shouldStartHomeActivity();
    	}
    }

}
