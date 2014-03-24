package com.coinwtb.coinassistant.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ViewFlipper;

import com.baidu.android.pushservice.CustomPushNotificationBuilder;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.coinwtb.coinassistant.R;
import com.coinwtb.coinassistant.bean.CoinBean;
import com.coinwtb.coinassistant.fragment.CoinWaringFragment;
import com.coinwtb.coinassistant.ui.base.CoinApplication;
import com.coinwtb.coinassistant.utils.PreferencesService;
import com.coinwtb.coinassistant.utils.Slog;
import com.coinwtb.coinassistant.utils.Utils;
import com.coinwtb.coinassistant.widget.CoinPriceShowLinearLayout;
import com.coinwtb.coinassistant.widget.CoinWarningRelativeLayout;
import com.coinwtb.coinassistant.widget.LeftCoinListView;
import com.coinwtb.coinassistant.widget.RightMenuLinearLayout;
import com.coinwtb.coinassistant.widget.slidingmenu.SlidingActivity;
import com.coinwtb.coinassistant.widget.slidingmenu.SlidingMenu;
/**
 * 
 * @author jun.qin
 *
 */
public class HomeActivity extends SlidingActivity {

	private static final String TAG = "HomeActivity";
	
	private RadioGroup mGroup;
	private SlidingMenu mSlidingMenu;
	
	private ViewFlipper mViewFlipper;
	
	private static int SHOW_HOME_STATE = 0;
	private static int SHOW_ALERT_STATE = 1;
	private int mCurShowState = SHOW_HOME_STATE; // set default to SHOW_MUISC_STATE
	
	// 左侧边栏
	private LeftCoinListView mLeftCoinListView;
	// 右侧边栏
	private RightMenuLinearLayout mRightMenuLinearLayout;
	
	// 首页看盘管理
	private CoinPriceShowLinearLayout mCoinPriceShowLL;
	// 预警管理
	private CoinWarningRelativeLayout mCoinWarningRL;
	// 列表价格
	private View mCoinPriceHistoryView;
	
	private long mFirstTouchDownTime; 
    private long mSecondTouchDownTime; 
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initViews();
        
        startBaiduPushServer(false);
        setBaiduPushNotificationBuilder();
        
    	CoinApplication.getInstance().addActivity(this);
		
		// 是否是第一次使用
		if (PreferencesService.getInstance().getBoolean(PreferencesService.PRE_FIRST_USER, true)) {
			ViewStub viewStub = (ViewStub) findViewById(R.id.viewstub);
			viewStub.setOnInflateListener(new ViewStub.OnInflateListener() {
				
				@Override
				public void onInflate(ViewStub stub, final View inflated) {
					 if (inflated != null) {
						 inflated.setOnTouchListener(new View.OnTouchListener() {
							
							@Override
							public boolean onTouch(View v, MotionEvent event) {
								inflated.setVisibility(View.GONE);
								return true;
							}
						});
					 }
				}
			});
			viewStub.inflate();
			PreferencesService.getInstance().putBoolean(PreferencesService.PRE_FIRST_USER, false);
		}
    }


	private void setBaiduPushNotificationBuilder() {
		// Push: 设置自定义的通知样式，具体API介绍见用户手册，如果想使用系统默认的可以不加这段代码
		// 请在通知推送界面中，高级设置->通知栏样式->自定义样式，选中并且填写值：1，
		// 与下方代码中 PushManager.setNotificationBuilder(this, 1, cBuilder)中的第二个参数对应
        CustomPushNotificationBuilder cBuilder = new CustomPushNotificationBuilder(
        		getApplicationContext(),
        		getResources().getIdentifier("notification_custom_builder", "layout", getPackageName()), 
        		getResources().getIdentifier("notification_icon", "id", getPackageName()), 
        		getResources().getIdentifier("notification_title", "id", getPackageName()), 
        		getResources().getIdentifier("notification_text", "id", getPackageName()));
        cBuilder.setNotificationFlags(Notification.FLAG_AUTO_CANCEL);
        cBuilder.setNotificationDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        cBuilder.setStatusbarIcon(this.getApplicationInfo().icon);
        cBuilder.setLayoutDrawable(getResources().getIdentifier("simple_notification_icon", "drawable", getPackageName()));
		PushManager.setNotificationBuilder(this, 1, cBuilder);
	}

	private void startBaiduPushServer(boolean check) {
		// Push: 以apikey的方式登录，一般放在主Activity的onCreate中。
		// 这里把apikey存放于manifest文件中，只是一种存放方式，
		// 您可以用自定义常量等其它方式实现，来替换参数中的Utils.getMetaValue(PushDemoActivity.this, "api_key")
		// 通过share preference实现的绑定标志开关，如果已经成功绑定，就取消这次绑定
		if (check && !Utils.hasBind(getApplicationContext())) {
			PushManager.startWork(getApplicationContext(),
					PushConstants.LOGIN_TYPE_API_KEY, 
					Utils.getBaiduApiKey(HomeActivity.this));
		}
	}
	
	public void initViews() {
		setContentView(R.layout.home_main);
		mViewFlipper = (ViewFlipper) findViewById(R.id.flipper_details);
		mCoinPriceShowLL = (CoinPriceShowLinearLayout) findViewById(R.id.coinpriceshow_ll);
		mCoinWarningRL = (CoinWarningRelativeLayout) findViewById(R.id.coinwarning_rl);
		mCoinWarningRL.setSupportManager(getSupportFragmentManager());
		mCoinWarningRL.setSlidingMenu(mSlidingMenu);
		mCoinPriceHistoryView = findViewById(R.id.img_coin_history);
		mCoinPriceHistoryView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, CoinPriceHistoryActivity.class);
				startActivity(intent);
			}
		});
		
		// 设置左侧边栏布局
		setBehindContentView(R.layout.main_sliding_menu);
		mLeftCoinListView = (LeftCoinListView) findViewById(android.R.id.list);
		mSlidingMenu = getSlidingMenu();
		mCoinWarningRL.setSlidingMenu(mSlidingMenu);
		
		// 设置右侧边栏布局
		mSlidingMenu.setSecondaryMenu(R.layout.main_secondary_sliding_menu);
        mRightMenuLinearLayout = (RightMenuLinearLayout) findViewById(R.id.right_menu_ll);
        mRightMenuLinearLayout.setSlidingMenu(mSlidingMenu);
        
		mGroup = (RadioGroup) findViewById(R.id.main_radio);
		
		setSlidingMenuAttribute();
		initTabs();
		
		initLeftListView();
	}

	public void setSlidingMenuAttribute() {
		// 设置侧边栏的属性
		mSlidingMenu.setShadowWidth(50);
		mSlidingMenu.setBehindOffset(Utils.dip2px(this, 75));
		mSlidingMenu.setTouchmodeMarginThreshold(Utils.dip2px(this, 75));
		mSlidingMenu.setFadeDegree(0.35f);

		// 设置侧边栏打开方式
		mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		// 设置左右侧边栏共存的方式
		mSlidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
	}
	
	// 初始化tabHost
	private void initTabs() {
		mGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				startBaiduPushServer(true);
				switch (checkedId) {
				case R.id.radio_button0:
					showHomeTab();
					break;
				case R.id.radio_button1:
					showWaringTab();
					break;
				case R.id.radio_button2:
					break;
				case R.id.radio_button3:
					break;
				default:
					break;
				}
			}
		});
	}
	
	private void initLeftListView() {
		findViewById(R.id.process).setVisibility(View.GONE);
		mLeftCoinListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				int lastIndex = CoinApplication.getInstance().getLastShowPriceIndex();
				//设置显示价格索引
				CoinApplication.getInstance().setShowPriceIndex(position);
				toggle();
				mGroup.check(R.id.radio_button0);
				if (lastIndex !=  position) {
					mCoinPriceShowLL.refreshCoin();
				}
			}
		});
		
		mLeftCoinListView.setEmptyView(findViewById(android.R.id.empty));
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getRepeatCount() == 0) {
			if (mSlidingMenu != null && mSlidingMenu.isMenuShowing()) {
				toggle();
				return true;
			} else {
				mFirstTouchDownTime = System.currentTimeMillis(); 
				long spaceTime = mFirstTouchDownTime - mSecondTouchDownTime; 
				mSecondTouchDownTime = mFirstTouchDownTime; 
				// 2s内
				if(spaceTime > 2000) { 
					Utils.showShortToast(getApplicationContext(), R.string.toast_keydown_exit);
					return true;
				}
			}
		}
		return super.dispatchKeyEvent(event);
	}
	
	private void showHomeTab() {
		mSlidingMenu.setSlidingEnabled(true);
		mSlidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
		if (mCurShowState == SHOW_ALERT_STATE) {
			mCurShowState = SHOW_HOME_STATE;
			mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.out_lefttoright));
			mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.in_lefttoright));
			mViewFlipper.showNext();
			mGroup.check(R.id.radio_button0);
		}
	}
	
	private void showWaringTab() {
		mSlidingMenu.setMode(SlidingMenu.LEFT);
		if (mCurShowState == SHOW_HOME_STATE) {
			mCurShowState = SHOW_ALERT_STATE;
			mCoinWarningRL.focus();
			mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.out_righttoleft));
			mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.in_righttoleft));
			mViewFlipper.showPrevious();
			mGroup.check(R.id.radio_button1);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		CoinApplication.getInstance().saveLastShowPriceIndex();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mSlidingMenu.isMenuShowing()) {
			mSlidingMenu.toggle(false);
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// 由Service中通知栏来的
		int coinId = intent.getIntExtra(CoinWaringFragment.EXTRA_COIN_ID, -1);
		Slog.d(TAG, "coinId = " + coinId);
		if (coinId != -1) {
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(coinId);
			CoinBean waringCoinBean = CoinApplication.getInstance().getWaringCoinById(coinId);
			Slog.d(TAG, "coinId = " + (waringCoinBean == null ? "null" : waringCoinBean.toString()));
			showWaringTab();
			if (waringCoinBean != null) {
				waringCoinBean.setVibrateWhenPopNofication(true);
	            mCoinWarningRL.setSelectedCoinBean(coinId);
			}
            
		}
	}
	
	@Override
	public void onDestroy() {
		Slog.d(TAG, this.getClass().getSimpleName() + " onDestroy() invoked!!");
		CoinApplication.getInstance().saveLastShowPriceIndex();
		super.onDestroy();
	}

}
